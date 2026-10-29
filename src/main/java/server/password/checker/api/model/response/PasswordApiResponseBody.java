package server.password.checker.api.model.response;

import java.util.List;

/**
 * JSON response payload parsed by the password checker.
 *
 * @param candidates possible matches to the requested hash prefix
 */
public record PasswordApiResponseBody(List<Candidate> candidates) {
}
