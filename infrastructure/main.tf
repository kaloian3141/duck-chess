terraform {
  required_version = ">= 1.6"
  required_providers {
    github = {
      source  = "integrations/github"
      version = "~> 6.3"
    }
  }
}

provider "github" {
  token = var.github_token
  owner = var.github_owner
}