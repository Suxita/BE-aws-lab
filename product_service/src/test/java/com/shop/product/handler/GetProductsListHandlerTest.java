package com.shop.product.handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GetProductsListHandlerTest {

    private final GetProductsListHandler handler = new GetProductsListHandler();

    @Test
    void shouldReturn200WithProductsList() {
        APIGatewayProxyResponseEvent response =
                handler.handleRequest(new APIGatewayProxyRequestEvent(), null);

        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("title"));
    }
}