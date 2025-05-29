package test.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.lifecycle.Startables;
import test.config.TestClientConfig;

import java.time.Duration;
import java.util.Map;
import java.util.stream.Stream;

@Import({TestRestFacade.class, TestClientConfig.class})

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(initializers = E2ESuite.Initializer.class)
public class E2ESuite {

    protected static final Network network = Network.newNetwork();

    @Autowired
    protected TestRestFacade rest;

    protected static GenericContainer<?> zipkinContainer;
    protected static GenericContainer<?> serviceDiscoveryContainer;
    protected static GenericContainer<?> cloudConfigContainer;
    protected static GenericContainer<?> userServiceContainer;
    protected static GenericContainer<?> productServiceContainer;
    protected static GenericContainer<?> orderServiceContainer;
    protected static GenericContainer<?> paymentServiceContainer;
    protected static GenericContainer<?> favouriteServiceContainer;

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            zipkinContainer = new GenericContainer<>("openzipkin/zipkin")
                    .withNetwork(network)
                    .withNetworkAliases("zipkin-container")
                    .withExposedPorts(9411)
                    .waitingFor(Wait.forHttp("/").forStatusCode(200))
                    .withLogConsumer(new Slf4jLogConsumer(org.slf4j.LoggerFactory.getLogger("zipkin")));

            serviceDiscoveryContainer = new GenericContainer<>("selimhorri/service-discovery-ecommerce-boot:0.1.0")
                    .withNetwork(network)
                    .withNetworkAliases("service-discovery-container")
                    .withExposedPorts(8761)
                    .withEnv("SPRING_ZIPKIN_BASE_URL", "http://zipkin-container:9411")
                    .waitingFor(Wait.forHttp("/actuator/health").forStatusCode(200))
                    .withLogConsumer(new Slf4jLogConsumer(org.slf4j.LoggerFactory.getLogger("service-discovery")));

            cloudConfigContainer = new GenericContainer<>("cloud-config:0.1.0")
                    .withNetwork(network)
                    .withNetworkAliases("cloud-config-container")
                    .withExposedPorts(9296)
                    .withEnv("SPRING_ZIPKIN_BASE_URL", "http://zipkin-container:9411")
                    .withEnv("EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE", "http://service-discovery-container:8761/eureka/")
                    .withEnv("EUREKA_INSTANCE", "cloud-config-container")
                    .waitingFor(Wait.forHttp("/actuator/health").forStatusCode(200))
                    .withLogConsumer(new Slf4jLogConsumer(org.slf4j.LoggerFactory.getLogger("cloud-config")));


            userServiceContainer = createService("selimhorri/user-service-ecommerce-boot:0.1.0", 8700, "user-service-container", "/user-service/actuator/health");
            productServiceContainer = createService("selimhorri/product-service-ecommerce-boot:0.1.0", 8500, "product-service-container", "/product-service/actuator/health");
            orderServiceContainer = createService("selimhorri/order-service-ecommerce-boot:0.1.0", 8300, "order-service-container", "/order-service/actuator/health");
            paymentServiceContainer = createService("selimhorri/payment-service-ecommerce-boot:0.1.0", 8400, "payment-service-container", "/payment-service/actuator/health");
            favouriteServiceContainer = createService("selimhorri/favourite-service-ecommerce-boot:0.1.0", 8800, "favourite-service-container", "/favourite-service/actuator/health");

            // Fase 1: Core
            Startables.deepStart(Stream.of(zipkinContainer)).join();
            Startables.deepStart(Stream.of(serviceDiscoveryContainer)).join();
            Startables.deepStart(Stream.of(cloudConfigContainer)).join();

            // Fase 2: Microservicios
            Startables.deepStart(Stream.of(
                    userServiceContainer,
                    productServiceContainer,
                    orderServiceContainer,
                    paymentServiceContainer,
                    favouriteServiceContainer
            )).join();

            Map<String, Object> props = Map.of(
                    "zipkin.url", "http://" + zipkinContainer.getHost() + ":" + zipkinContainer.getMappedPort(9411),
                    "service.discovery.url", "http://" + serviceDiscoveryContainer.getHost() + ":" + serviceDiscoveryContainer.getMappedPort(8761),
                    "cloud.config.url", "http://" + cloudConfigContainer.getHost() + ":" + cloudConfigContainer.getMappedPort(9296),
                    "user.service.url", "http://" + userServiceContainer.getHost() + ":" + userServiceContainer.getMappedPort(8700),
                    "product.service.url", "http://" + productServiceContainer.getHost() + ":" + productServiceContainer.getMappedPort(8500),
                    "order.service.url", "http://" + orderServiceContainer.getHost() + ":" + orderServiceContainer.getMappedPort(8300),
                    "payment.service.url", "http://" + paymentServiceContainer.getHost() + ":" + paymentServiceContainer.getMappedPort(8400),
                    "favourite.service.url", "http://" + favouriteServiceContainer.getHost() + ":" + favouriteServiceContainer.getMappedPort(8800)
            );

            ConfigurableEnvironment environment = context.getEnvironment();
            environment.getPropertySources().addFirst(new MapPropertySource("testcontainers", props));
        }

        private GenericContainer<?> createService(String image, int port, String alias, String healthPath) {
            return new GenericContainer<>(image)
                    .withNetwork(network)
                    .withNetworkAliases(alias)
                    .withExposedPorts(port)
                    .withEnv("SPRING_PROFILES_ACTIVE", "dev")
                    .withEnv("SPRING_ZIPKIN_BASE_URL", "http://zipkin-container:9411")
                    .withEnv("SPRING_CONFIG_IMPORT", "optional:configserver:http://cloud-config-container:9296")
                    .withEnv("EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE", "http://service-discovery-container:8761/eureka")
                    .withEnv("EUREKA_INSTANCE", alias)
                    .waitingFor(Wait.forHttp(healthPath).forStatusCode(200))
                    .withStartupTimeout(Duration.ofMinutes(3))
                    .withLogConsumer(new Slf4jLogConsumer(org.slf4j.LoggerFactory.getLogger(alias)));
        }
    }
}
