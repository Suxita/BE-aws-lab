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

    private final ProductRepository repository = new ProductRepository();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent request, Context context) {

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setHeaders(Map.of(
                "Access-Control-Allow-Origin", "*",
                "Access-Control-Allow-Headers", "Content-Type",
                "Content-Type", "application/json"
        ));

        try {
            Map<String, String> pathParameters = request.getPathParameters();

            if (pathParameters == null || !pathParameters.containsKey("productId")) {
                response.setStatusCode(400);
                response.setBody("{\"message\": \"Missing productId\"}");
                return response;
            }

            String productId = pathParameters.get("productId");
            Optional<Product> product = repository.findById(productId);

            if (product.isPresent()) {
                response.setStatusCode(200);
                response.setBody(objectMapper.writeValueAsString(product.get()));
            } else {
                response.setStatusCode(404);
                response.setBody("{\"message\": \"Product not found\"}");
            }
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setBody("{\"message\": \"Internal server error\"}");
        }

        return response;
    }
}