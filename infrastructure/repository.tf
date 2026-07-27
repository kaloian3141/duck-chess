resource "github_repository" "main" {
  name        = var.repository_name
  description = ""
  visibility  = "public"

  has_issues   = true
  has_projects = false
  has_wiki     = false

  allow_merge_commit     = false
  allow_squash_merge     = true
  allow_rebase_merge     = false
  delete_branch_on_merge = true

  lifecycle {
    prevent_destroy = true
  }
}