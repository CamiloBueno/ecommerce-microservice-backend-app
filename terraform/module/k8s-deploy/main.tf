variable "yaml_path" {
  type = string
}

resource "null_resource" "apply_deployment" {
  provisioner "local-exec" {
    command = "kubectl apply -f ${var.yaml_path}"
  }

  triggers = {
    always_run = "${timestamp()}"
  }
}