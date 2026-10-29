package server.password.checker.api;

import com.google.gson.Gson;
import server.password.checker.api.model.request.PasswordApiRequestBody;
import server.password.hash.PasswordHasher;
import server.password.hash.SHA256PasswordHasher;

import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** Builds authenticated JSON requests containing the first ten hexadecimal SHA-256 characters. */
public class PasswordsApiRequestBuilder {
    private static final String ENDPOINT = "https://api.enzoic.com/v1/passwords";
    private static final int HASH_SYMBOLS_COUNT = 10;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";
    private static final String CONTENT_TYPE_HEADER_VALUE = "application/json";
    private static final String ACCEPT_HEADER_NAME = "Accept";
    private static final String ACCEPT_HEADER_VALUE = "*/*";
    private static final String COLON = ":";
    private static final String BASIC = "basic";

    private final Gson gson;
    private final String authorizationHeaderString;
    private final PasswordHasher hasher;

    PasswordsApiRequestBuilder(String apiKey, String apiSecret, PasswordHasher hasher) {
        gson = new Gson();
        String authorizationEncoded =
            Base64.getEncoder().encodeToString((apiKey + COLON + apiSecret).getBytes(StandardCharsets.UTF_8));

        authorizationHeaderString = BASIC + " " + authorizationEncoded;
        this.hasher = hasher;
    }

    /**
     * Configures Basic authentication and the SHA-256 password hasher.
     *
     * @param apiKey Enzoic API key
     * @param apiSecret Enzoic API secret
     */
    public PasswordsApiRequestBuilder(String apiKey, String apiSecret) {
        this(apiKey, apiSecret, new SHA256PasswordHasher());
    }

    /**
     * Builds a POST request without sending it or configuring a request timeout.
     *
     * @param password plaintext input to hash locally
     * @return an authenticated request containing only the hash prefix in its JSON body
     */
    public HttpRequest getRequest(String password) {
        String passwordHash = hasher.getHash(password).substring(0, HASH_SYMBOLS_COUNT);
        PasswordApiRequestBody requestBody = new PasswordApiRequestBody(passwordHash);

        return HttpRequest.newBuilder(URI.create(ENDPOINT))
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
            .header(AUTHORIZATION_HEADER, authorizationHeaderString)
            .header(CONTENT_TYPE_HEADER_NAME, CONTENT_TYPE_HEADER_VALUE)
            .header(ACCEPT_HEADER_NAME, ACCEPT_HEADER_VALUE)
            .build();
    }
}
