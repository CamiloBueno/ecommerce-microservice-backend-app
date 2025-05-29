package test;

import test.util.E2ESuite;
import test.util.TestRestFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = E2ESuite.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class OrderServiceE2ETest extends E2ESuite {

    @Autowired
    private TestRestFacade restFacade;

    @Value("${order.service.url}")
    private String orderServiceUrl;

    @Test
    void shouldGetOrderById() {
        int orderId = 2;
        String url = orderServiceUrl + "/order-service/api/orders/" + orderId;
        ResponseEntity<String> response = restFacade.get(url, String.class);

        System.out.println("Response: " + response.getBody());
        System.out.println("Status Code: " + response.getStatusCode());

        assertTrue(
                response.getStatusCode().is2xxSuccessful(),
                "Unexpected status code: " + response.getStatusCode()
        );
    }

    @Test
    void shouldGetAllOrders() {
        String url = orderServiceUrl + "/order-service/api/orders";
        ResponseEntity<String> response = restFacade.get(url, String.class);

        System.out.println("All Orders Response: " + response.getBody());
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Failed to get all orders");
    }

    @Test
    void shouldDeleteOrderById() {
        int orderId = 3; // usa un ID que sepas que existe o que puedes controlar en test
        String url = orderServiceUrl + "/order-service/api/orders/" + orderId;
        ResponseEntity<String> response = restFacade.delete(url, String.class);

        System.out.println("Delete Response: " + response.getBody());
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Failed to delete order");
    }
}
