package server.password.checker.api.model.response;

/**
 * Candidate password returned by the exposure service.
 *
 * @param sha256 full password hash used for local comparison
 * @param revealedInExposure whether the service marks the password as revealed
 * @param exposureCount reported number of exposures; not used by the current checker
 */
public record Candidate(String sha256, boolean revealedInExposure, int exposureCount) {
}
