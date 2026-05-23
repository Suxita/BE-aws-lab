package com.shop.product.data;

import com.shop.product.model.Product;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;


public record ProductRepository(DynamoDbClient dynamo, String productsTable, String stocksTable) {

    public ProductRepository() {
        this(
                DynamoDbClient.builder()
                        .httpClientBuilder(UrlConnectionHttpClient.builder())
                        .region(Region.of(System.getenv().getOrDefault("AWS_REGION", "eu-north-1")))
                        .build(),
                System.getenv().getOrDefault("PRODUCTS_TABLE", "products"),
                System.getenv().getOrDefault("STOCKS_TABLE", "stocks")
        );
    }

    public List<Product> findAll() {
        List<Map<String, AttributeValue>> productItems = scanTable(productsTable);

        Map<String, Integer> stockMap = buildStockMap();

        List<Product> result = new ArrayList<>();
        for (Map<String, AttributeValue> item : productItems) {
            Product p = mapToProduct(item);
            p.setCount(stockMap.getOrDefault(p.getId(), 0));
            result.add(p);
        }
        return result;
    }

    public Optional<Product> findById(String id) {
        GetItemResponse productResponse = dynamo.getItem(GetItemRequest.builder()
                .tableName(productsTable)
                .key(Map.of("id", str(id)))
                .build());

        if (!productResponse.hasItem()) {
            return Optional.empty();
        }

        Product product = mapToProduct(productResponse.item());

        GetItemResponse stockResponse = dynamo.getItem(GetItemRequest.builder()
                .tableName(stocksTable)
                .key(Map.of("product_id", str(id)))
                .build());

        if (stockResponse.hasItem()) {
            product.setCount(num(stockResponse.item().get("count")));
        }

        return Optional.of(product);
    }

    public Product create(Product product) {
        String id = UUID.randomUUID().toString();
        product.setId(id);

        TransactWriteItemsRequest tx = TransactWriteItemsRequest.builder()
                .transactItems(
                        TransactWriteItem.builder()
                                .put(Put.builder()
                                        .tableName(productsTable)
                                        .item(Map.of(
                                                "id", str(product.getId()),
                                                "title", str(product.getTitle()),
                                                "description", str(product.getDescription()),
                                                "price", numStr(product.getPrice())
                                        ))
                                        .build())
                                .build(),

                        TransactWriteItem.builder()
                                .put(Put.builder()
                                        .tableName(stocksTable)
                                        .item(Map.of(
                                                "product_id", str(id),
                                                "count", numStr(product.getCount())
                                        ))
                                        .build())
                                .build()
                )
                .build();

        dynamo.transactWriteItems(tx);
        return product;
    }

    private List<Map<String, AttributeValue>> scanTable(String tableName) {
        List<Map<String, AttributeValue>> items = new ArrayList<>();
        String lastKey = null;

        do {
            ScanRequest.Builder req = ScanRequest.builder().tableName(tableName);
            if (lastKey != null) {
                req.exclusiveStartKey(Map.of("id", str(lastKey)));
            }
            ScanResponse response = dynamo.scan(req.build());
            items.addAll(response.items());

            lastKey = response.hasLastEvaluatedKey()
                    ? response.lastEvaluatedKey().get("id").s()
                    : null;
        } while (lastKey != null);

        return items;
    }

    private Map<String, Integer> buildStockMap() {
        Map<String, Integer> map = new HashMap<>();

        ScanResponse response = dynamo.scan(ScanRequest.builder()
                .tableName(stocksTable)
                .build());

        for (Map<String, AttributeValue> item : response.items()) {
            String productId = item.get("product_id").s();
            int count = num(item.get("count"));
            map.put(productId, count);
        }
        return map;
    }

    private Product mapToProduct(Map<String, AttributeValue> item) {
        Product p = new Product();
        p.setId(item.containsKey("id") ? item.get("id").s() : null);
        p.setTitle(item.containsKey("title") ? item.get("title").s() : null);
        p.setDescription(item.containsKey("description") ? item.get("description").s() : null);
        p.setPrice(item.containsKey("price") ? Double.parseDouble(item.get("price").n()) : 0.0);
        return p;
    }

    private static AttributeValue str(String value) {
        return AttributeValue.builder().s(value).build();
    }

    private static AttributeValue numStr(double value) {
        return AttributeValue.builder().n(String.valueOf(value)).build();
    }

    private static AttributeValue numStr(int value) {
        return AttributeValue.builder().n(String.valueOf(value)).build();
    }

    private static int num(AttributeValue av) {
        if (av == null) return 0;
        try {
            return Integer.parseInt(av.n());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}