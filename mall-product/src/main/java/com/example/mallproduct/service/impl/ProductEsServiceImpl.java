package com.example.mallproduct.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.example.mallproduct.dto.ProductDto;
import com.example.mallproduct.es.ProductDoc;
import com.example.mallproduct.productVo.PageResult;
import com.example.mallproduct.service.ProductEsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ProductEsServiceImpl implements ProductEsService {

    private static final String PRODUCT_INDEX = "product";
    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    private final ElasticsearchClient elasticsearchClient;

    public ProductEsServiceImpl(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    @Override
    public PageResult<ProductDoc> search(ProductDto productDto) throws IOException {
        ProductDto condition = productDto == null ? new ProductDto() : productDto;
        int pageNum = normalizePageNum(condition.getPageNum());
        int pageSize = normalizePageSize(condition.getPageSize());
        int from = (pageNum - 1) * pageSize;

        SearchResponse<ProductDoc> response = elasticsearchClient.search(request -> {
            request.index(PRODUCT_INDEX)
                    .from(from)
                    .size(pageSize)
                    .query(buildQuery(condition));

            if (isSupportedSortField(condition.getSortBy())) {
                request.sort(sort -> sort.field(field -> field
                        .field(condition.getSortBy())
                        .order(resolveSortOrder(condition.getSortOrder()))));
            }

            return request;
        }, ProductDoc.class);

        List<ProductDoc> products = response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .toList();
        long total = response.hits().total() == null ? 0L : response.hits().total().value();

        return new PageResult<>(total, pageNum, pageSize, products);
    }

    private Query buildQuery(ProductDto productDto) {
        List<Query> mustQueries = new ArrayList<>();
        List<Query> filterQueries = new ArrayList<>();

        if (StringUtils.hasText(productDto.getProductName())) {
            mustQueries.add(Query.of(query -> query.multiMatch(multiMatch -> multiMatch
                    .fields("productName", "productDesc")
                    .query(productDto.getProductName().trim()))));
        }

        filterQueries.add(termQuery("status", 1));
        filterQueries.add(termQuery("auditStatus", 1));
        filterQueries.add(termQuery("deleted", 0));

        if (productDto.getCategoryId() != null) {
            filterQueries.add(termQuery("categoryId", productDto.getCategoryId()));
        }

        Query priceRangeQuery = buildPriceRangeQuery(productDto.getMinPrice(), productDto.getMaxPrice());
        if (priceRangeQuery != null) {
            filterQueries.add(priceRangeQuery);
        }

        return Query.of(query -> query.bool(bool -> bool
                .must(mustQueries)
                .filter(filterQueries)));
    }

    private Query termQuery(String field, long value) {
        return Query.of(query -> query.term(term -> term
                .field(field)
                .value(value)));
    }

    private Query buildPriceRangeQuery(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null && maxPrice == null) {
            return null;
        }

        return Query.of(query -> query.range(range -> range.number(number -> {
            number.field("price");
            if (minPrice != null) {
                number.gte(minPrice.doubleValue());
            }
            if (maxPrice != null) {
                number.lte(maxPrice.doubleValue());
            }
            return number;
        })));
    }

    private int normalizePageNum(Integer pageNum) {
        if (pageNum == null || pageNum < DEFAULT_PAGE_NUM) {
            return DEFAULT_PAGE_NUM;
        }
        return pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    private boolean isSupportedSortField(String sortBy) {
        return "price".equals(sortBy) || "createTime".equals(sortBy);
    }

    private SortOrder resolveSortOrder(String sortOrder) {
        return "asc".equalsIgnoreCase(sortOrder) ? SortOrder.Asc : SortOrder.Desc;
    }
}
