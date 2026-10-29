package server.password.checker.api.model.response;

import java.util.List;

public record PasswordApiResponseBody(List<Candidate> candidates) {
}
