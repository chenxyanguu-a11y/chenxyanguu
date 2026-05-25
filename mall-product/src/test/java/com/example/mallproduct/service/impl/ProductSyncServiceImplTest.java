package com.example.mallproduct.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.util.ObjectBuilder;
import com.example.mallproduct.entity.Product;
import com.example.mallproduct.es.ProductDoc;
import com.example.mallproduct.exception.EsExcetpionHandler;
import com.example.mallproduct.mapper.ProductMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductSyncServiceImplTest {

    @Test
    void syncProductsToEsReturnsZeroWhenMysqlHasNoData() throws IOException {
        ProductMapper productMapper = mock(ProductMapper.class);
        ElasticsearchClient elasticsearchClient = mock(ElasticsearchClient.class);
        IndexOperations indexOperations = mockIndexOperations();
        ProductSyncServiceImpl syncService = syncService(productMapper, elasticsearchClient, indexOperations);

        when(productMapper.selectNextBatch(0L, 1000)).thenReturn(Collections.emptyList());

        long total = syncService.syncProductsToEs();

        assertThat(total).isZero();
        verify(indexOperations).exists();
        verify(indexOperations).delete();
        verify(indexOperations).createWithMapping();
        verify(productMapper).selectNextBatch(0L, 1000);
    }

    @Test
    void syncProductsToEsCreatesIndexWhenProductIndexDoesNotExist() throws IOException {
        ProductMapper productMapper = mock(ProductMapper.class);
        ElasticsearchClient elasticsearchClient = mock(ElasticsearchClient.class);
        IndexOperations indexOperations = mockIndexOperations();
        ProductSyncServiceImpl syncService = syncService(productMapper, elasticsearchClient, indexOperations);

        when(indexOperations.exists()).thenReturn(false);
        when(productMapper.selectNextBatch(0L, 1000)).thenReturn(Collections.emptyList());

        long total = syncService.syncProductsToEs();

        assertThat(total).isZero();
        verify(indexOperations, never()).delete();
        verify(indexOperations).createWithMapping();
    }

    @Test
    void syncProductsToEsReadsMysqlInBatchesAndBulkSavesUntilFinished() throws IOException {
        ProductMapper productMapper = mock(ProductMapper.class);
        ElasticsearchClient elasticsearchClient = mock(ElasticsearchClient.class);
        IndexOperations indexOperations = mockIndexOperations();
        ProductSyncServiceImpl syncService = syncService(productMapper, elasticsearchClient, indexOperations);
        List<BulkRequest> bulkRequests = new ArrayList<>();

        when(productMapper.selectNextBatch(0L, 1000)).thenReturn(products(1, 1000));
        when(productMapper.selectNextBatch(1000L, 1000)).thenReturn(products(1001, 2));
        when(elasticsearchClient.bulk(org.mockito.ArgumentMatchers
                .<Function<BulkRequest.Builder, ObjectBuilder<BulkRequest>>>any()))
                .thenAnswer(invocation -> {
                    Function<BulkRequest.Builder, ObjectBuilder<BulkRequest>> bulkBuilder = invocation.getArgument(0);
                    bulkRequests.add(bulkBuilder.apply(new BulkRequest.Builder()).build());
                    return bulkResponse(false);
                });

        long total = syncService.syncProductsToEs();

        assertThat(total).isEqualTo(1002);
        assertThat(bulkRequests).hasSize(2);
        assertThat(bulkRequests.get(0).index()).isEqualTo("product");
        assertThat(bulkRequests.get(0).operations()).hasSize(1000);
        assertThat(bulkRequests.get(1).operations()).hasSize(2);
        assertThat(bulkRequests.get(0).operations().get(0).index().document())
                .isInstanceOfSatisfying(ProductDoc.class,
                        productDoc -> assertThat(productDoc.getAvailableStock()).isEqualTo(88));
        verify(productMapper).selectNextBatch(0L, 1000);
        verify(productMapper).selectNextBatch(1000L, 1000);
        verify(elasticsearchClient, times(2)).bulk(org.mockito.ArgumentMatchers
                .<Function<BulkRequest.Builder, ObjectBuilder<BulkRequest>>>any());
    }

    @Test
    void syncProductsToEsThrowsWhenBulkHasErrors() throws IOException {
        ProductMapper productMapper = mock(ProductMapper.class);
        ElasticsearchClient elasticsearchClient = mock(ElasticsearchClient.class);
        IndexOperations indexOperations = mockIndexOperations();
        ProductSyncServiceImpl syncService = syncService(productMapper, elasticsearchClient, indexOperations);

        when(productMapper.selectNextBatch(0L, 1000)).thenReturn(products(1, 1));
        when(elasticsearchClient.bulk(org.mockito.ArgumentMatchers
                .<Function<BulkRequest.Builder, ObjectBuilder<BulkRequest>>>any()))
                .thenReturn(bulkResponse(true));

        assertThatThrownBy(syncService::syncProductsToEs)
                .isInstanceOf(EsExcetpionHandler.class)
                .hasMessageContaining("同步商品数据到 Elasticsearch 失败");
    }

    private static ProductSyncServiceImpl syncService(ProductMapper productMapper,
                                                      ElasticsearchClient elasticsearchClient,
                                                      IndexOperations indexOperations) {
        ElasticsearchOperations elasticsearchOperations = mock(ElasticsearchOperations.class);
        when(elasticsearchOperations.indexOps(com.example.mallproduct.es.ProductDoc.class)).thenReturn(indexOperations);
        return new ProductSyncServiceImpl(productMapper, elasticsearchClient, elasticsearchOperations);
    }

    private static IndexOperations mockIndexOperations() {
        IndexOperations indexOperations = mock(IndexOperations.class);
        when(indexOperations.exists()).thenReturn(true);
        when(indexOperations.delete()).thenReturn(true);
        when(indexOperations.createWithMapping()).thenReturn(true);
        return indexOperations;
    }

    private static BulkResponse bulkResponse(boolean errors) {
        return BulkResponse.of(response -> response
                .errors(errors)
                .took(1)
                .items(Collections.emptyList()));
    }

    private static List<Product> products(long startId, int count) {
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            products.add(product(startId + i));
        }
        return products;
    }

    private static Product product(long id) {
        Product product = new Product();
        product.setId(id);
        product.setMerchantId(10L);
        product.setCategoryId(20L);
        product.setProductName("product-" + id);
        product.setProductDesc("desc-" + id);
        product.setPrice(new BigDecimal("99.00"));
        product.setMainImage("image-" + id + ".png");
        product.setStatus(1);
        product.setAuditStatus(1);
        product.setDeleted(0);
        product.setCreateTime(LocalDateTime.of(2026, 5, 12, 15, 0));
        product.setAvailableStock(88);
        return product;
    }
}
