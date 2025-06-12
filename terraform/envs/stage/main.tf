provider "kubernetes" {
  config_path    = "~/.kube/config"
  config_context = "minikube"
}

module "namespace" {
  source = "../../modules/namespace"
  name   = "ecommerce-stage"
}

# Lista de microservicios
module "api_gateway_deployment" {
  source    = "../../modules/k8s-deployment"
  yaml_path = "../../../k8s/api-gateway-kube/deployment.yaml"
}

module "api_gateway_service" {
  source    = "../../modules/k8s-service"
  yaml_path = "../../../k8s/api-gateway-kube/service.yaml"
}

module "cloud_config_deployment" {
  source    = "../../modules/k8s-deployment"
  yaml_path = "../../../k8s/cloud-config-kube/deployment.yaml"
}

module "cloud_config_service" {
  source    = "../../modules/k8s-service"
  yaml_path = "../../../k8s/cloud-config-kube/service.yaml"
}

module "order_service_deployment" {
  source    = "../../modules/k8s-deployment"
  yaml_path = "../../../k8s/order-service-kube/deployment.yaml"
}

module "order_service_service" {
  source    = "../../modules/k8s-service"
  yaml_path = "../../../k8s/order-service-kube/service.yaml"
}

module "payment_service_deployment" {
  source    = "../../modules/k8s-deployment"
  yaml_path = "../../../k8s/payment-service-kube/deployment.yaml"
}

module "payment_service_service" {
  source    = "../../modules/k8s-service"
  yaml_path = "../../../k8s/payment-service-kube/service.yaml"
}

module "product_service_deployment" {
  source    = "../../modules/k8s-deployment"
  yaml_path = "../../../k8s/product-service-kube/deployment.yaml"
}

module "product_service_service" {
  source    = "../../modules/k8s-service"
  yaml_path = "../../../k8s/product-service-kube/service.yaml"
}

module "service_discovery_deployment" {
  source    = "../../modules/k8s-deployment"
  yaml_path = "../../../k8s/service-discovery-kube/deployment.yaml"
}

module "service_discovery_service" {
  source    = "../../modules/k8s-service"
  yaml_path = "../../../k8s/service-discovery-kube/service.yaml"
}

module "user_service_deployment" {
  source    = "../../modules/k8s-deployment"
  yaml_path = "../../../k8s/user-service-kube/deployment.yaml"
}

module "user_service_service" {
  source    = "../../modules/k8s-service"
  yaml_path = "../../../k8s/user-service-kube/service.yaml"
}

module "zipkin_deployment" {
  source    = "../../modules/k8s-deployment"
  yaml_path = "../../../k8s/zipkin-kube/deployment.yaml"
}

module "zipkin_service" {
  source    = "../../modules/k8s-service"
  yaml_path = "../../../k8s/zipkin-kube/service.yaml"
}
