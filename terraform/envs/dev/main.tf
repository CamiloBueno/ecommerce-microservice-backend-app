provider "kubernetes" {
  config_path    = "~/.kube/config"
  config_context = "minikube"
}

module "namespace" {
  source = "../../module/namespace"
  name   = "ecommerce-dev"
}

module "deployments" {
  for_each = toset(var.microservices)

  source    = "../../module/k8s-deploy"
  yaml_path = "../../../k8s/${each.key}-kube/deployment.yaml"
}

module "services" {
  for_each = toset(var.microservices)

  source    = "../../module/k8s-service"
  yaml_path = "../../../k8s/${each.key}-kube/service.yaml"
}
