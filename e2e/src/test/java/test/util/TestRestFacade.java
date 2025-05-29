package test.util;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

@TestComponent
public class TestRestFacade {

    @Autowired
    private TestRestTemplate rest;

    public <T> ResponseEntity<T> post(String fullUrl, Object body, Class<T> responseType) {
        final var response = rest.exchange(fullUrl, POST, new HttpEntity<>(body), responseType);
        assertSuccess(response);
        return response;
    }

    public <T> ResponseEntity<T> get(String fullUrl, Class<T> responseType) {
        final var response = rest.exchange(fullUrl, GET, null, responseType);
        assertSuccess(response);
        return response;
    }

    public <T> ResponseEntity<T> put(String fullUrl, Object body, Class<T> responseType) {
        final var response = rest.exchange(fullUrl, PUT, new HttpEntity<>(body), responseType);
        assertSuccess(response);
        return response;
    }

    public <T> ResponseEntity<T> delete(String fullUrl, Class<T> responseType) {
        final var response = rest.exchange(fullUrl, DELETE, null, responseType);
        assertSuccess(response);
        return response;
    }

    private void assertSuccess(ResponseEntity<?> response) {
        assertTrue(response.getStatusCode().is2xxSuccessful(),
                "Unexpected status code: " + response.getStatusCode());
    }
}
