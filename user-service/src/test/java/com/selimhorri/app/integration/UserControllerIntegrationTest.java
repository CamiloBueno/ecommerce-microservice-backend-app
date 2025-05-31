package com.selimhorri.app.integration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.domain.RoleBasedAuthority;
import com.selimhorri.app.dto.CredentialDto;
import com.selimhorri.app.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.profiles.active=test",
                "spring.datasource.url=jdbc:h2:mem:testdb",
                "spring.datasource.driver-class-name=org.h2.Driver"})
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private String getBaseUrl() {
        return "http://localhost:" + port + "/user-service/api/users";
    }

    @Test
    void testSaveUserAndFetchByUsername() {
        UserDto userDto = UserDto.builder()
                .firstName("Camilo")
                .lastName("Bueno")
                .email("camilo@example.com")
                .phone("3000000000")
                .credentialDto(CredentialDto.builder()
                        .username("camilob")
                        .password("123456")
                        .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                        .isEnabled(true)
                        .isAccountNonExpired(true)
                        .isAccountNonLocked(true)
                        .isCredentialsNonExpired(true)
                        .build())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserDto> request = new HttpEntity<>(userDto, headers);

        ResponseEntity<UserDto> saveResponse = restTemplate
                .exchange(getBaseUrl(), HttpMethod.POST, request, UserDto.class);

        assertEquals(HttpStatus.OK, saveResponse.getStatusCode());
        assertNotNull(saveResponse.getBody());
        assertEquals("Camilo", saveResponse.getBody().getFirstName());
    }

    @Test
    void testFindById() throws IOException {
        String userId = "1";
        String url = getBaseUrl() + "/" + userId;

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                String.class
        );

        System.out.println("Raw JSON for findById:\n" + response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        UserDto userDto = objectMapper.readValue(response.getBody(), UserDto.class);
        assertEquals("selim", userDto.getFirstName());
    }

    @Test
    void testFindByUsername() throws IOException {
        String username = "selimhorri";
        String url = getBaseUrl() + "/username/" + username;

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                String.class
        );

        System.out.println("Raw JSON for findByUsername:\n" + response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        UserDto userDto = objectMapper.readValue(response.getBody(), UserDto.class);
        assertEquals(username, userDto.getCredentialDto().getUsername());
    }

    @Test
    void testDeleteById() {
        String userId = "1";
        String url = getBaseUrl() + "/" + userId;

        ResponseEntity<Boolean> response = restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                null,
                Boolean.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Boolean.TRUE.equals(response.getBody()));
    }
}
