package server.password.checker.api;

import com.google.gson.Gson;
import server.password.checker.PasswordChecker;
import server.password.checker.api.model.response.PasswordApiResponseBody;
import server.password.hash.SHA256PasswordHasher;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Performs a blocking Enzoic lookup using a partial SHA-256 hash.
 * A 404 response is accepted; other responses are parsed for an exact hash match
 * marked as revealed in an exposure. No request timeout or retry is configured.
 */
public class APIPasswordChecker implements PasswordChecker {
    private static final String API_KEY_PATH = "ApiKey";
    private static final String API_SECRET_PATH = "ApiSecret";
    private static final int STATUS_NOT_FOUND = 404;

    private final PasswordsApiRequestBuilder requestBuilder;
    private final HttpClient client;

    /**
     * Creates a checker with injectable request and HTTP dependencies.
     *
     * @param requestBuilder creates authenticated partial-hash requests
     * @param client HTTP client owned and closed by this checker
     */
    public APIPasswordChecker(PasswordsApiRequestBuilder requestBuilder,
                              HttpClient client) {
        this.requestBuilder = requestBuilder;
        this.client = client;
    }

    /**
     * Reads API credentials exactly as stored, including any trailing whitespace.
     *
     * @param apiKeyPath file containing the API key
     * @param apiSecretPath file containing the API secret
     * @throws RuntimeException if either credential file cannot be read
     */
    public APIPasswordChecker(Path apiKeyPath, Path apiSecretPath) {
        String apiKey;
        String apiSecret;
        try {
            apiKey = Files.readString(apiKeyPath);
            apiSecret = Files.readString(apiSecretPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this(new PasswordsApiRequestBuilder(apiKey, apiSecret), HttpClient.newHttpClient());
    }

    /** Loads {@code ApiKey} and {@code ApiSecret} from the working directory. */
    public APIPasswordChecker() {
        this(Path.of(API_KEY_PATH), Path.of(API_SECRET_PATH));
    }

    /**
     * Accepts passwords with no matching candidate marked as revealed in an exposure.
     * Non-404 HTTP statuses are parsed without separate error-status validation.
     *
     * @param password plaintext candidate; only its hash prefix is sent in the request body
     * @return true for HTTP 404 or when no candidate matches both hash and exposure flag
     * @throws RuntimeException if the request fails or the response cannot be processed
     */
    @Override
    public boolean isPasswordSecure(String password) {
        HttpRequest request = requestBuilder.getRequest(password);
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong with the api", e);
        }
        if (response.statusCode() == STATUS_NOT_FOUND) {
            return true;
        }
        String passwordHash = new SHA256PasswordHasher().getHash(password);
        PasswordApiResponseBody body = new Gson().fromJson(response.body(), PasswordApiResponseBody.class);
        return body.candidates().stream()
            .noneMatch(c -> c.sha256().equals(passwordHash) && c.revealedInExposure());
    }

    /**
     * Closes the HTTP client owned by this checker.
     *
     * @throws IOException as allowed by the password checker lifecycle contract
     */
    @Override
    public void close() throws IOException {
        client.close();
    }
}
