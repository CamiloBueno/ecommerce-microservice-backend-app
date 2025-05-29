package com.selimhorri.app.integration;

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

    private String getBaseUrl() {
        return "http://localhost:" + port +  "/user-service" + "/api/users";
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
    void testFindByUsername() {
        String username = "selimhorri";
        String url = getBaseUrl() + "/username/" + username;

        ResponseEntity<UserDto> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                UserDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(username, response.getBody().getCredentialDto().getUsername());
    }

    @Test
    void testFindById() {
        String userId = "1";
        String url = getBaseUrl() + "/" + userId;

        ResponseEntity<UserDto> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                UserDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("selim", response.getBody().getFirstName());
    }

    @Test
    void testUpdateUserById() {
        String userId = "1";

        UserDto updatedUser = UserDto.builder()
                .userId(1)
                .firstName("CamiloUpdated")
                .lastName("Bueno")
                .email("camilo_updated@example.com")
                .phone("3010000000")
                .credentialDto(CredentialDto.builder()
                        .username("camilob")
                        .password("654321")
                        .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                        .isEnabled(true)
                        .isAccountNonExpired(true)
                        .isAccountNonLocked(true)
                        .isCredentialsNonExpired(true)
                        .build())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserDto> request = new HttpEntity<>(updatedUser, headers);

        ResponseEntity<UserDto> response = restTemplate.exchange(
                getBaseUrl() + "/" + userId,
                HttpMethod.PUT,
                request,
                UserDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testDeleteById() {
        String userId = "1";

        ResponseEntity<Boolean> response = restTemplate.exchange(
                getBaseUrl() + "/" + userId,
                HttpMethod.DELETE,
                null,
                Boolean.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Boolean.TRUE.equals(response.getBody()));
    }

}
