package com.shop.product.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.product.data.ProductRepository;
import com.shop.product.model.Product;

import java.util.List;
import java.util.Map;

public class GetProductsListHandler implements
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
            List<Product> products = repository.findAll();
            String body = objectMapper.writeValueAsString(products);
            response.setStatusCode(200);
            response.setBody(body);
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setBody("{\"message\": \"Internal server error\"}");
        }

        return response;
    }
}