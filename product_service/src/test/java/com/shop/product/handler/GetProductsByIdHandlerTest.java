package com.shop.product.handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class GetProductsByIdHandlerTest {

    private final GetProductsByIdHandler handler = new GetProductsByIdHandler();

    @Test
    void shouldReturn200ForExistingProduct() {
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setPathParameters(Map.of("productId", "1"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, null);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("Lord of the Rings"));
    }

    @Test
    void shouldReturn404ForMissingProduct() {
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setPathParameters(Map.of("productId", "999"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, null);

        assertEquals(404, response.getStatusCode());
        assertTrue(response.getBody().contains("not found"));
    }

    @Test
    void shouldReturn400WhenNoProductId() {
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, null);

        assertEquals(400, response.getStatusCode());
    }
}