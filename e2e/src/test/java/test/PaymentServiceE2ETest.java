package test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import test.util.E2ESuite;
import test.util.TestRestFacade;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = E2ESuite.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class PaymentServiceE2ETest extends E2ESuite {

    @Autowired
    private TestRestFacade restFacade;

    @Value("${payment.service.url}")
    private String paymentServiceUrl;

    @Test
    void shouldDeletePaymentById() {
        int paymentId = 1;
        String url = paymentServiceUrl + "/payment-service/api/payments/" + paymentId;
        ResponseEntity<String> response = restFacade.delete(url, String.class);

        System.out.println("Delete Payment Response: " + response.getBody());
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Failed to delete paymentByID");
}

    @Test
    void shouldGetPaymentById() {
        int paymentId = 2; // usa un ID válido en tu entorno
        String url = paymentServiceUrl + "/payment-service/api/payments/" + paymentId;
        ResponseEntity<String> response = restFacade.get(url, String.class);

        System.out.println("Payment Response: " + response.getBody());
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Failed to get payment by ID");
    }
}
