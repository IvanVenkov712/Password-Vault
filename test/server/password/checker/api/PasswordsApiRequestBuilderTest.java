package server.password.checker.api;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import server.password.checker.api.model.request.PasswordApiRequestBody;
import server.password.hash.PasswordHasher;

import java.net.http.HttpRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PasswordsApiRequestBuilderTest {
    @Test
    void getRequest() {
        PasswordHasher hasher = mock();
        when(hasher.getHash("pass")).thenReturn("aaaaabbbbb");

        PasswordsApiRequestBuilder builder = new PasswordsApiRequestBuilder("a", "b");

        HttpRequest request = builder.getRequest("pass");
        assertTrue(request.bodyPublisher().get().contentLength() > 0, "Expected non-empty body!");
    }
}