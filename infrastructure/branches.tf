resource "github_branch" "develop" {
  repository    = github_repository.main.name
  branch        = "develop"
  source_branch = "main"
}

resource "github_branch_protection" "main" {
  repository_id = github_repository.main.node_id
  pattern       = "main"

  required_pull_request_reviews {
    required_approving_review_count = 1
    dismiss_stale_reviews           = true
    require_code_owner_reviews      = false
  }

  required_status_checks {
    strict   = true
    contexts = ["backend", "frontend"]
  }

  enforce_admins      = false
  allows_deletions    = false
  allows_force_pushes = false
}

resource "github_branch_protection" "develop" {
  repository_id = github_repository.main.node_id
  pattern       = "develop"

  required_status_checks {
    strict   = true
    contexts = ["backend", "frontend"]
  }

  enforce_admins      = false
  allows_deletions    = false
  allows_force_pushes = false

  depends_on = [github_branch.develop]
}