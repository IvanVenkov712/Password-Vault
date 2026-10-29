package server.password.checker.api.model.response;

public record Candidate(String sha256, boolean revealedInExposure, int exposureCount) {
}
