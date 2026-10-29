package server.password.checker.api.model.request;

/**
 * JSON request payload for a partial-hash password lookup.
 *
 * @param partialSHA256 leading hexadecimal characters of the password's SHA-256 hash
 */
public record PasswordApiRequestBody(String partialSHA256) {

}
