package com.selimhorri.app.config.feature;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.console.TogglzConsoleServlet;

@Configuration
public class TogglzConsoleConfig {

    @Bean
    public ServletRegistrationBean<TogglzConsoleServlet> togglzConsoleServlet() {
        // Asegúrate que esta ruta incluya el context-path si lo defines en application.yml
        return new ServletRegistrationBean<>(new TogglzConsoleServlet(), "/togglz-console/*");
    }
}