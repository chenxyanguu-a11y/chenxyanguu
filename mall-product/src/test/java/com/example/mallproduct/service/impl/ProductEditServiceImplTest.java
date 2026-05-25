package com.example.mallproduct.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import co.elastic.clients.util.ObjectBuilder;
import com.example.mallproduct.dto.ProductUpdateDTO;
import com.example.mallproduct.entity.Product;
import com.example.mallproduct.es.ProductDoc;
import com.example.mallproduct.exception.BusinessException;
import com.example.mallproduct.mapper.ProductEditMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductEditServiceImplTest {

    @Mock
    private ProductEditMapper productEditMapper;

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @InjectMocks
    private ProductEditServiceImpl productEditService;

    @AfterEach
    void clearTransactionSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void adminUpdateSearchableProductDeletesThenSyncsAfterCommit() throws IOException {
        ProductUpdateDTO dto = updateDTO();
        Product beforeUpdate = product(1L, 10L, 1, 1, 0);
        Product afterUpdate = product(1L, 10L, 1, 1, 0);
        AtomicReference<DeleteRequest> deleteRequest = captureDeleteRequest();
        AtomicReference<IndexRequest<ProductDoc>> indexRequest = captureIndexRequest();

        when(productEditMapper.selectById(1L)).thenReturn(beforeUpdate, afterUpdate);
        TransactionSynchronizationManager.initSynchronization();

        productEditService.updateProduct(1L, dto, 99L, "ADMIN");

        verify(productEditMapper).updateProductById(eq(1L), same(dto), eq(1), eq(1));
        verify(elasticsearchClient, never()).delete(org.mockito.ArgumentMatchers
                .<Function<DeleteRequest.Builder, ObjectBuilder<DeleteRequest>>>any());
        runAfterCommit();

        assertThat(deleteRequest.get().index()).isEqualTo("product");
        assertThat(deleteRequest.get().id()).isEqualTo("1");
        assertThat(indexRequest.get().index()).isEqualTo("product");
        assertThat(indexRequest.get().id()).isEqualTo("1");
        assertThat(indexRequest.get().document().getProductName()).isEqualTo("phone");
        assertThat(indexRequest.get().document().getAvailableStock()).isEqualTo(88);
    }

    @Test
    void adminUpdateDeletedProductOnlyDeletesEsAfterCommit() throws IOException {
        ProductUpdateDTO dto = updateDTO();
        Product beforeUpdate = product(2L, 10L, 1, 1, 0);
        Product afterUpdate = product(2L, 10L, 1, 1, 1);
        AtomicReference<DeleteRequest> deleteRequest = captureDeleteRequest();

        when(productEditMapper.selectById(2L)).thenReturn(beforeUpdate, afterUpdate);
        TransactionSynchronizationManager.initSynchronization();

        productEditService.updateProduct(2L, dto, 99L, "ADMIN");
        runAfterCommit();

        verify(productEditMapper).updateProductById(eq(2L), same(dto), eq(1), eq(1));
        assertThat(deleteRequest.get().id()).isEqualTo("2");
        verify(elasticsearchClient, never()).index(org.mockito.ArgumentMatchers
                .<Function<IndexRequest.Builder<ProductDoc>, ObjectBuilder<IndexRequest<ProductDoc>>>>any());
    }

    @Test
    void merchantUpdateOwnProductSetsWaitingAuditAndOnlyDeletesEsAfterCommit() throws IOException {
        ProductUpdateDTO dto = updateDTO();
        Product beforeUpdate = product(3L, 20L, 1, 1, 0);
        Product afterUpdate = product(3L, 20L, 1, 0, 0);
        AtomicReference<DeleteRequest> deleteRequest = captureDeleteRequest();

        when(productEditMapper.selectById(3L)).thenReturn(beforeUpdate, afterUpdate);
        TransactionSynchronizationManager.initSynchronization();

        productEditService.updateProduct(3L, dto, 20L, "MERCHANT");
        runAfterCommit();

        verify(productEditMapper).updateProductById(eq(3L), same(dto), eq(1), eq(0));
        assertThat(deleteRequest.get().id()).isEqualTo("3");
        verify(elasticsearchClient, never()).index(org.mockito.ArgumentMatchers
                .<Function<IndexRequest.Builder<ProductDoc>, ObjectBuilder<IndexRequest<ProductDoc>>>>any());
    }

    @Test
    void merchantCannotUpdateOtherMerchantProduct() {
        when(productEditMapper.selectById(4L)).thenReturn(product(4L, 30L, 1, 1, 0));

        assertThatThrownBy(() -> productEditService.updateProduct(4L, updateDTO(), 31L, "MERCHANT"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(403);
    }

    @Test
    void userCannotUpdateProduct() {
        when(productEditMapper.selectById(5L)).thenReturn(product(5L, 30L, 1, 1, 0));

        assertThatThrownBy(() -> productEditService.updateProduct(5L, updateDTO(), 31L, "USER"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(403);
    }

    @Test
    void updateProductThrowsWhenProductDoesNotExist() {
        when(productEditMapper.selectById(404L)).thenReturn(null);

        assertThatThrownBy(() -> productEditService.updateProduct(404L, updateDTO(), 31L, "ADMIN"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(404);
    }

    @Test
    void adminCanListAllProductsForEdit() {
        List<Product> products = List.of(product(1L, 10L, 1, 1, 0));
        when(productEditMapper.selectAllProducts()).thenReturn(products);

        assertThat(productEditService.listAllProductsForEdit("ADMIN")).isSameAs(products);
    }

    @Test
    void nonAdminCannotListAllProductsForEdit() {
        assertThatThrownBy(() -> productEditService.listAllProductsForEdit("MERCHANT"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(401);
    }

    @Test
    void adminSearchesProductsByNameFromEsForEdit() throws IOException {
        AtomicReference<SearchRequest> searchRequest = captureSearchRequest(productDoc(1L, 10L));

        List<ProductDoc> products = productEditService.searchProductsForEdit(" phone ", 10L, "ADMIN");

        assertThat(products).hasSize(1);
        assertThat(products.get(0).getId()).isEqualTo(1L);
        assertThat(searchRequest.get().index()).containsExactly("product");
        assertThat(searchRequest.get().size()).isEqualTo(20);
        assertThat(searchRequest.get().query().bool().filter()).hasSize(3);
    }

    @Test
    void merchantSearchesOwnProductsByNameFromEsForEdit() throws IOException {
        AtomicReference<SearchRequest> searchRequest = captureSearchRequest(productDoc(1L, 10L));

        List<ProductDoc> products = productEditService.searchProductsForEdit(" phone ", 10L, "MERCHANT");

        assertThat(products).hasSize(1);
        assertThat(products.get(0).getMerchantId()).isEqualTo(10L);
        assertThat(searchRequest.get().query().bool().filter()).hasSize(4);
    }

    @Test
    void otherRoleCannotSearchProductsForEdit() {
        assertThatThrownBy(() -> productEditService.searchProductsForEdit("phone", 10L, "USER"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(401);
    }

    private AtomicReference<DeleteRequest> captureDeleteRequest() throws IOException {
        AtomicReference<DeleteRequest> request = new AtomicReference<>();
        when(elasticsearchClient.delete(org.mockito.ArgumentMatchers
                .<Function<DeleteRequest.Builder, ObjectBuilder<DeleteRequest>>>any()))
                .thenAnswer(invocation -> {
                    Function<DeleteRequest.Builder, ObjectBuilder<DeleteRequest>> builder = invocation.getArgument(0);
                    request.set(builder.apply(new DeleteRequest.Builder()).build());
                    return null;
                });
        return request;
    }

    private AtomicReference<IndexRequest<ProductDoc>> captureIndexRequest() throws IOException {
        AtomicReference<IndexRequest<ProductDoc>> request = new AtomicReference<>();
        when(elasticsearchClient.index(org.mockito.ArgumentMatchers
                .<Function<IndexRequest.Builder<ProductDoc>, ObjectBuilder<IndexRequest<ProductDoc>>>>any()))
                .thenAnswer(invocation -> {
                    Function<IndexRequest.Builder<ProductDoc>, ObjectBuilder<IndexRequest<ProductDoc>>> builder =
                            invocation.getArgument(0);
                    request.set(builder.apply(new IndexRequest.Builder<>()).build());
                    return null;
                });
        return request;
    }

    private AtomicReference<SearchRequest> captureSearchRequest(ProductDoc productDoc) throws IOException {
        AtomicReference<SearchRequest> request = new AtomicReference<>();
        when(elasticsearchClient.search(org.mockito.ArgumentMatchers
                        .<Function<SearchRequest.Builder, ObjectBuilder<SearchRequest>>>any(),
                eq(ProductDoc.class)))
                .thenAnswer(invocation -> {
                    Function<SearchRequest.Builder, ObjectBuilder<SearchRequest>> builder = invocation.getArgument(0);
                    request.set(builder.apply(new SearchRequest.Builder()).build());
                    return searchResponse(productDoc);
                });
        return request;
    }

    private static SearchResponse<ProductDoc> searchResponse(ProductDoc productDoc) {
        return SearchResponse.of(response -> response
                .took(1)
                .timedOut(false)
                .shards(shards -> shards.total(1).successful(1).failed(0))
                .hits(hits -> hits
                        .total(total -> total.value(1).relation(TotalHitsRelation.Eq))
                        .hits(hit -> hit
                                .index("product")
                                .id(String.valueOf(productDoc.getId()))
                                .source(productDoc))));
    }

    private void runAfterCommit() {
        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCommit();
        }
    }

    private static ProductUpdateDTO updateDTO() {
        ProductUpdateDTO productUpdateDTO = new ProductUpdateDTO();
        productUpdateDTO.setCategoryId(2L);
        productUpdateDTO.setProductName("phone");
        productUpdateDTO.setProductDesc("smart phone");
        productUpdateDTO.setPrice(new BigDecimal("5999.00"));
        productUpdateDTO.setMainImage("phone.jpg");
        return productUpdateDTO;
    }

    private static Product product(Long id, Long merchantId, Integer status, Integer auditStatus, Integer deleted) {
        Product product = new Product();
        product.setId(id);
        product.setMerchantId(merchantId);
        product.setCategoryId(2L);
        product.setProductName("phone");
        product.setProductDesc("smart phone");
        product.setPrice(new BigDecimal("5999.00"));
        product.setMainImage("phone.jpg");
        product.setStatus(status);
        product.setAuditStatus(auditStatus);
        product.setDeleted(deleted);
        product.setCreateTime(LocalDateTime.of(2026, 5, 15, 13, 0));
        product.setAvailableStock(88);
        return product;
    }

    private static ProductDoc productDoc(Long id, Long merchantId) {
        ProductDoc productDoc = new ProductDoc();
        productDoc.setId(id);
        productDoc.setMerchantId(merchantId);
        productDoc.setCategoryId(2L);
        productDoc.setProductName("phone");
        productDoc.setProductDesc("smart phone");
        productDoc.setPrice(new BigDecimal("5999.00"));
        productDoc.setMainImage("phone.jpg");
        productDoc.setStatus(1);
        productDoc.setAuditStatus(1);
        productDoc.setDeleted(0);
        productDoc.setCreateTime(LocalDateTime.of(2026, 5, 15, 13, 0));
        productDoc.setAvailableStock(88);
        return productDoc;
    }
}
