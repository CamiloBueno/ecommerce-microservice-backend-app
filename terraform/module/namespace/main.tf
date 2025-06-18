variable "name" {
  description = "Namespace to create"
  type        = string
}

resource "kubernetes_namespace" "this" {
  metadata {
    name = var.name
  }
}