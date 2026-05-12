package com.example.mallproduct.service;

import java.io.IOException;

public interface ProductSyncService {

    long syncProductsToEs() throws IOException;
}
