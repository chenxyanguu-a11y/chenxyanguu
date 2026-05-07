package com.example.mallproduct.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import co.elastic.clients.util.ObjectBuilder;
import com.example.mallproduct.dto.ProductDto;
import com.example.mallproduct.es.ProductDoc;
import com.example.mallproduct.productVo.PageResult;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductEsServiceImplTest {

    @Test
    void searchUsesDefaultPagingAndBaseFiltersWhenConditionIsNull() throws IOException {
        CapturedSearch capturedSearch = new CapturedSearch();
        ProductEsServiceImpl productEsService = new ProductEsServiceImpl(capturedSearch.client());

        PageResult<ProductDoc> result = productEsService.search(null);

        SearchRequest request = capturedSearch.request();
        assertThat(request.index()).containsExactly("product");
        assertThat(request.from()).isZero();
        assertThat(request.size()).isEqualTo(10);
        assertThat(request.sort()).isEmpty();
        assertThat(request.query().bool().must()).isEmpty();
        assertThat(request.query().bool().filter()).hasSize(3);
        assertThat(result.getTotal()).isEqualTo(0L);
        assertThat(result.getPageNum()).isEqualTo(1);
        assertThat(result.getPageSize()).isEqualTo(10);
        assertThat(result.getList()).isEmpty();
    }

    @Test
    void searchBuildsKeywordFiltersPriceRangeAndSupportedSort() throws IOException {
        CapturedSearch capturedSearch = new CapturedSearch();
        ProductEsServiceImpl productEsService = new ProductEsServiceImpl(capturedSearch.client());
        ProductDto productDto = new ProductDto();
        productDto.setProductName("phone");
        productDto.setCategoryId(12L);
        productDto.setMinPrice(new BigDecimal("100.00"));
        productDto.setMaxPrice(new BigDecimal("999.00"));
        productDto.setSortBy("price");
        productDto.setSortOrder("asc");
        productDto.setPageNum(2);
        productDto.setPageSize(500);

        PageResult<ProductDoc> result = productEsService.search(productDto);

        SearchRequest request = capturedSearch.request();
        assertThat(request.from()).isEqualTo(100);
        assertThat(request.size()).isEqualTo(100);
        assertThat(request.query().bool().must()).hasSize(1);
        assertThat(request.query().bool().filter()).hasSize(5);
        assertThat(request.sort()).hasSize(1);
        assertThat(request.sort().get(0).field().field()).isEqualTo("price");
        assertThat(request.sort().get(0).field().order()).isEqualTo(SortOrder.Asc);
        assertThat(result.getPageNum()).isEqualTo(2);
        assertThat(result.getPageSize()).isEqualTo(100);
    }

    @Test
    void searchIgnoresUnsupportedSortField() throws IOException {
        CapturedSearch capturedSearch = new CapturedSearch();
        ProductEsServiceImpl productEsService = new ProductEsServiceImpl(capturedSearch.client());
        ProductDto productDto = new ProductDto();
        productDto.setSortBy("sales");

        productEsService.search(productDto);

        assertThat(capturedSearch.request().sort()).isEmpty();
    }

    private static SearchResponse<ProductDoc> emptyResponse() {
        return SearchResponse.of(response -> response
                .took(1)
                .timedOut(false)
                .shards(shards -> shards.total(1).successful(1).failed(0))
                .hits(hits -> hits
                        .total(total -> total.value(0).relation(TotalHitsRelation.Eq))
                        .hits(Collections.emptyList())));
    }

    private static class CapturedSearch {
        private final AtomicReference<SearchRequest> request = new AtomicReference<>();
        private final ElasticsearchClient client = mock(ElasticsearchClient.class);

        CapturedSearch() throws IOException {
            when(client.search(
                    org.mockito.ArgumentMatchers.<Function<SearchRequest.Builder, ObjectBuilder<SearchRequest>>>any(),
                    eq(ProductDoc.class)))
                    .thenAnswer(invocation -> {
                        Function<SearchRequest.Builder, ObjectBuilder<SearchRequest>> searchBuilder =
                                invocation.getArgument(0);
                        request.set(searchBuilder.apply(new SearchRequest.Builder()).build());
                        return emptyResponse();
                    });
        }

        ElasticsearchClient client() {
            return client;
        }

        SearchRequest request() {
            return request.get();
        }
    }
}
