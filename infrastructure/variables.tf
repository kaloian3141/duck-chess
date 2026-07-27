variable "github_token" {
  type      = string
  sensitive = true
}

variable "github_owner" {
  type = string
}

variable "repository_name" {
  type    = string
  default = "duck-chess"
}