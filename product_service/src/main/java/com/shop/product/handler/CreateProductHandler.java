package com.shop.product.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.product.data.ProductRepository;
import com.shop.product.model.Product;

import java.util.Map;

public class CreateProductHandler implements
        RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ProductRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CreateProductHandler() {
        this.repository = new ProductRepository();
    }

    public CreateProductHandler(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent request, Context context) {

        if (context != null) {
            context.getLogger().log(
                    "CreateProduct invoked. Body: " + request.getBody()
            );
        }

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setHeaders(corsHeaders());

        try {
            String body = request.getBody();
            if (body == null || body.isBlank()) {
                response.setStatusCode(400);
                response.setBody("{\"message\": \"Request body is required\"}");
                return response;
            }

            Product product = objectMapper.readValue(body, Product.class);

            String validationError = validate(product);
            if (validationError != null) {
                response.setStatusCode(400);
                response.setBody("{\"message\": \"" + validationError + "\"}");
                return response;
            }

            Product created = repository.create(product);

            response.setStatusCode(201);
            response.setBody(objectMapper.writeValueAsString(created));

        } catch (Exception e) {
            if (context != null) context.getLogger().log("Error: " + e.getMessage());
            response.setStatusCode(500);
            response.setBody("{\"message\": \"Internal server error\"}");
        }

        return response;
    }

    private String validate(Product p) {
        if (p.getTitle() == null || p.getTitle().isBlank()) {
            return "title is required";
        }
        if (p.getPrice() <= 0) {
            return "price must be greater than 0";
        }
        if (p.getCount() < 0) {
            return "count cannot be negative";
        }
        return null;   // valid
    }

    private Map<String, String> corsHeaders() {
        return Map.of(
                "Access-Control-Allow-Origin",  "*",
                "Access-Control-Allow-Headers", "Content-Type",
                "Content-Type",                 "application/json"
        );
    }
}