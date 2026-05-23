package com.shop.product.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.product.data.ProductRepository;
import com.shop.product.model.Product;

import java.util.Map;
import java.util.Optional;

public class GetProductsByIdHandler implements
        RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ProductRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GetProductsByIdHandler() {
        this.repository = new ProductRepository();
    }

    public GetProductsByIdHandler(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent request, Context context) {

        Map<String, String> pathParams = request.getPathParameters();
        if (context != null) {
            context.getLogger().log("GetProductsById invoked. Path params: " + pathParams);
        }

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setHeaders(corsHeaders());

        try {
            if (pathParams == null || !pathParams.containsKey("productId")) {
                response.setStatusCode(400);
                response.setBody("{\"message\": \"Missing productId path parameter\"}");
                return response;
            }

            String productId = pathParams.get("productId");
            Optional<Product> product = repository.findById(productId);

            if (product.isPresent()) {
                response.setStatusCode(200);
                response.setBody(objectMapper.writeValueAsString(product.get()));
            } else {
                response.setStatusCode(404);
                response.setBody("{\"message\": \"Product not found\"}");
            }

        } catch (Exception e) {
            if (context != null) context.getLogger().log("Error: " + e.getMessage());
            response.setStatusCode(500);
            response.setBody("{\"message\": \"Internal server error\"}");
        }

        return response;
    }

    private Map<String, String> corsHeaders() {
        return Map.of(
                "Access-Control-Allow-Origin",  "*",
                "Access-Control-Allow-Headers", "Content-Type",
                "Content-Type",                 "application/json"
        );
    }
}