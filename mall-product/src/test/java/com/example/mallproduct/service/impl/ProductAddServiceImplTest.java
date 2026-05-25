package com.example.mallproduct.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.util.ObjectBuilder;
import com.example.mallproduct.entity.Product;
import com.example.mallproduct.es.ProductDoc;
import com.example.mallproduct.exception.BusinessException;
import com.example.mallproduct.mapper.ProductMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductAddServiceImplTest {

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @Mock
    private IndexOperations indexOperations;

    @InjectMocks
    private ProductAddServiceImpl productAddService;

    @Test
    void addProductSetsAuditPassedAndSyncsApprovedProductToEs() throws IOException {
        Product input = productWithoutStatus();
        Product savedProduct = savedProduct(100L, 1);
        AtomicReference<IndexRequest<ProductDoc>> indexRequest = new AtomicReference<>();

        doAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(100L);
            return 1;
        }).when(productMapper).insertProduct(input);
        when(productMapper.selectById(100L)).thenReturn(savedProduct);
        when(elasticsearchOperations.indexOps(ProductDoc.class)).thenReturn(indexOperations);
        when(indexOperations.exists()).thenReturn(true);
        when(elasticsearchClient.index(org.mockito.ArgumentMatchers
                .<Function<IndexRequest.Builder<ProductDoc>, ObjectBuilder<IndexRequest<ProductDoc>>>>any()))
                .thenAnswer(invocation -> {
                    Function<IndexRequest.Builder<ProductDoc>, ObjectBuilder<IndexRequest<ProductDoc>>> builder =
                            invocation.getArgument(0);
                    indexRequest.set(builder.apply(new IndexRequest.Builder<>()).build());
                    return null;
                });

        Product result = productAddService.addProduct(input, 20L, "MERCHANT");

        assertThat(input.getAuditStatus()).isEqualTo(1);
        assertThat(input.getStatus()).isEqualTo(1);
        assertThat(input.getDeleted()).isZero();
        assertThat(input.getMerchantId()).isEqualTo(20L);
        assertThat(result).isSameAs(savedProduct);
        assertThat(indexRequest.get().index()).isEqualTo("product");
        assertThat(indexRequest.get().id()).isEqualTo("100");
        assertThat(indexRequest.get().document().getProductName()).isEqualTo("phone");
        assertThat(indexRequest.get().document().getAvailableStock()).isEqualTo(88);
        verify(productMapper).insertProduct(input);
        verify(productMapper).selectById(100L);
    }

    @Test
    void addProductCreatesProductIndexWhenItDoesNotExist() throws IOException {
        Product input = productWithoutStatus();
        Product savedProduct = savedProduct(101L, 1);

        doAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(101L);
            return 1;
        }).when(productMapper).insertProduct(input);
        when(productMapper.selectById(101L)).thenReturn(savedProduct);
        when(elasticsearchOperations.indexOps(ProductDoc.class)).thenReturn(indexOperations);
        when(indexOperations.exists()).thenReturn(false);

        productAddService.addProduct(input, 20L, "MERCHANT");

        verify(indexOperations).createWithMapping();
    }

    @Test
    void addProductDoesNotSyncWhenSavedProductIsNotApproved() throws IOException {
        Product input = productWithoutStatus();
        Product savedProduct = savedProduct(102L, 0);

        doAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(102L);
            return 1;
        }).when(productMapper).insertProduct(input);
        when(productMapper.selectById(102L)).thenReturn(savedProduct);

        Product result = productAddService.addProduct(input, 20L, "MERCHANT");

        assertThat(result).isSameAs(savedProduct);
        verify(elasticsearchClient, never()).index(org.mockito.ArgumentMatchers
                .<Function<IndexRequest.Builder<ProductDoc>, ObjectBuilder<IndexRequest<ProductDoc>>>>any());
    }

    @Test
    void merchantAddProductOverridesRequestMerchantIdWithCurrentUserId() throws IOException {
        Product input = productWithoutStatus();
        input.setMerchantId(999L);
        Product savedProduct = savedProduct(103L, 1);
        savedProduct.setMerchantId(20L);

        doAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(103L);
            return 1;
        }).when(productMapper).insertProduct(input);
        when(productMapper.selectById(103L)).thenReturn(savedProduct);
        when(elasticsearchOperations.indexOps(ProductDoc.class)).thenReturn(indexOperations);
        when(indexOperations.exists()).thenReturn(true);

        Product result = productAddService.addProduct(input, 20L, "MERCHANT");

        assertThat(input.getMerchantId()).isEqualTo(20L);
        assertThat(result.getMerchantId()).isEqualTo(20L);
        verify(productMapper).insertProduct(input);
    }

    @Test
    void adminCannotAddProduct() {
        Product input = productWithoutStatus();

        assertThatThrownBy(() -> productAddService.addProduct(input, 1L, "ADMIN"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("\u65e0\u6743\u9650\u65b0\u589e\u5546\u54c1");

        verifyNoInteractions(productMapper, elasticsearchClient, elasticsearchOperations);
    }

    @Test
    void nonMerchantCannotAddProduct() {
        Product input = productWithoutStatus();

        assertThatThrownBy(() -> productAddService.addProduct(input, 1L, "USER"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("\u65e0\u6743\u9650\u65b0\u589e\u5546\u54c1");
        assertThatThrownBy(() -> productAddService.addProduct(input, 1L, ""))
                .isInstanceOf(BusinessException.class)
                .hasMessage("\u65e0\u6743\u9650\u65b0\u589e\u5546\u54c1");

        verifyNoInteractions(productMapper, elasticsearchClient, elasticsearchOperations);
    }

    private static Product productWithoutStatus() {
        Product product = new Product();
        product.setMerchantId(1L);
        product.setCategoryId(2L);
        product.setProductName("phone");
        product.setProductDesc("smart phone");
        product.setPrice(new BigDecimal("5999.00"));
        product.setMainImage("phone.jpg");
        return product;
    }

    private static Product savedProduct(Long id, Integer auditStatus) {
        Product product = productWithoutStatus();
        product.setId(id);
        product.setStatus(1);
        product.setAuditStatus(auditStatus);
        product.setDeleted(0);
        product.setCreateTime(LocalDateTime.of(2026, 5, 13, 10, 0));
        product.setAvailableStock(88);
        return product;
    }
}
