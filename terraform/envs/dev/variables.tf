variable "environment" {
  default = "dev"
}

variable "microservices" {
  type = list(string)
  default = [
    "zipkin", "service-discovery", "cloud-config", "api-gateway",
    "proxy-client", "order-service", "payment-service",
    "product-service", "shipping-service", "user-service",
    "favourite-service"
  ]
}