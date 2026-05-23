package com.shop.product.handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.shop.product.data.ProductRepository;
import com.shop.product.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CreateProductHandlerTest {

    private ProductRepository mockRepo;
    private CreateProductHandler handler;

    @BeforeEach
    void setUp() {
        mockRepo = Mockito.mock(ProductRepository.class);
        handler  = new CreateProductHandler(mockRepo);
    }

    @Test
    void shouldReturn201ForValidProduct() {
        when(mockRepo.create(any())).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId("new-uuid");
            return p;
        });

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setBody("""
            {"title":"New Book","description":"Desc","price":19.99,"count":5}
            """);

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, null);

        assertEquals(201, response.getStatusCode());
        assertTrue(response.getBody().contains("New Book"));
    }

    @Test
    void shouldReturn400ForMissingTitle() {
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setBody("{\"price\":19.99,\"count\":5}");

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, null);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("title"));
    }

    @Test
    void shouldReturn400ForInvalidPrice() {
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setBody("{\"title\":\"Book\",\"price\":-1,\"count\":5}");

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, null);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("price"));
    }

    @Test
    void shouldReturn400ForNegativeCount() {
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setBody("{\"title\":\"Book\",\"price\":10,\"count\":-1}");

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, null);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("count"));
    }

    @Test
    void shouldReturn400ForEmptyBody() {
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        // body is null

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, null);

        assertEquals(400, response.getStatusCode());
    }

    @Test
    void shouldReturn500WhenRepositoryThrows() {
        when(mockRepo.create(any())).thenThrow(new RuntimeException("DynamoDB error"));

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setBody("{\"title\":\"Book\",\"price\":10,\"count\":1}");

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, null);

        assertEquals(500, response.getStatusCode());
    }
}