package com.shop.product.data;

import com.shop.product.model.Product;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ProductRepository {

    private static final List<Product> PRODUCTS = Arrays.asList(
            new Product("1", "The Lord of the Rings", "Fantasy novel by J.R.R. Tolkien", 29.99, 10),
            new Product("2", "Harry Potter", "Fantasy novel by J.K. Rowling", 19.99, 25),
            new Product("3", "Clean Code", "A handbook of agile software craftsmanship", 39.99, 5),
            new Product("4", "The Pragmatic Programmer", "From journeyman to master", 44.99, 8),
            new Product("5", "Design Patterns", "Elements of reusable object-oriented software", 49.99, 3)
    );

    public List<Product> findAll() {
        return PRODUCTS;
    }

    public Optional<Product> findById(String id) {
        return PRODUCTS.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }
}