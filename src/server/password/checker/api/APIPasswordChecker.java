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

public class APIPasswordChecker implements PasswordChecker {
    private static final String API_KEY_PATH = "ApiKey";
    private static final String API_SECRET_PATH = "ApiSecret";
    private static final int STATUS_NOT_FOUND = 404;

    private final PasswordsApiRequestBuilder requestBuilder;
    private final HttpClient client;

    public APIPasswordChecker(PasswordsApiRequestBuilder requestBuilder,
                              HttpClient client) {
        this.requestBuilder = requestBuilder;
        this.client = client;
    }

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

    public APIPasswordChecker() {
        this(Path.of(API_KEY_PATH), Path.of(API_SECRET_PATH));
    }

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

    @Override
    public void close() throws IOException {
        client.close();
    }
}
