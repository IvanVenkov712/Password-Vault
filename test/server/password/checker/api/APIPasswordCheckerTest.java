package server.password.checker.api;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.password.checker.api.model.response.Candidate;
import server.password.checker.api.model.response.PasswordApiResponseBody;
import server.password.hash.SHA256PasswordHasher;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class APIPasswordCheckerTest {

    private HttpClient mockHttpClient;
    private APIPasswordChecker apiPasswordChecker;

    @BeforeEach
    void setUp() {
        mockHttpClient = mock();
        PasswordsApiRequestBuilder requestBuilder = new PasswordsApiRequestBuilder("testApiKey", "testApiSecret");
        apiPasswordChecker = new APIPasswordChecker(requestBuilder, mockHttpClient);
    }

    private void setupMockResponse(int statusCode, String body) throws IOException, InterruptedException {
        HttpResponse<String> mockResponse = mock();
        when(mockResponse.statusCode()).thenReturn(statusCode);
        if (body != null) {
            when(mockResponse.body()).thenReturn(body);
        }
        when(mockHttpClient.<String>send(any(HttpRequest.class), any())).thenReturn(mockResponse);
    }

    @Test
    void testIsPasswordSecureReturns404() throws IOException, InterruptedException {
        String password = "TestPassword123!";

        setupMockResponse(404, null);

        boolean result = apiPasswordChecker.isPasswordSecure(password);

        assertTrue(result, "Password should be considered secure when API returns 404");
        verify(mockHttpClient).send(any(HttpRequest.class), any());
    }

    @Test
    void testIsPasswordSecurePasswordNotInExposure() throws IOException, InterruptedException {
        String password = "SecurePassword456!";

        List<Candidate> candidates = List.of(
            new Candidate("differentHash1", true, 5),
            new Candidate("differentHash2", false, 0)
        );
        PasswordApiResponseBody responseBody = new PasswordApiResponseBody(candidates);
        String jsonResponse = new Gson().toJson(responseBody);

        setupMockResponse(200, jsonResponse);

        boolean result = apiPasswordChecker.isPasswordSecure(password);

        assertTrue(result, "Password should be secure when not found in exposed candidates");
        verify(mockHttpClient).send(any(HttpRequest.class), any());
    }

    @Test
    void testIsPasswordSecurePasswordExposedAndRevealed() throws IOException, InterruptedException {
        String password = "ExposedPassword789!";
        String passwordHash = new SHA256PasswordHasher().getHash(password);

        List<Candidate> candidates = List.of(
            new Candidate(passwordHash, true, 10),
            new Candidate("otherHash", false, 0)
        );
        PasswordApiResponseBody responseBody = new PasswordApiResponseBody(candidates);
        String jsonResponse = new Gson().toJson(responseBody);

        setupMockResponse(200, jsonResponse);

        boolean result = apiPasswordChecker.isPasswordSecure(password);

        assertFalse(result, "Password should be insecure when exposed and revealed");
        verify(mockHttpClient).send(any(HttpRequest.class), any());
    }

    @Test
    void testIsPasswordSecurePasswordExposedButNotRevealed() throws IOException, InterruptedException {
        String password = "HiddenExposedPassword!";
        String passwordHash = new SHA256PasswordHasher().getHash(password);

        List<Candidate> candidates = List.of(
            new Candidate(passwordHash, false, 5),
            new Candidate("anotherHash", true, 8)
        );
        PasswordApiResponseBody responseBody = new PasswordApiResponseBody(candidates);
        String jsonResponse = new Gson().toJson(responseBody);

        setupMockResponse(200, jsonResponse);

        boolean result = apiPasswordChecker.isPasswordSecure(password);

        assertTrue(result, "Password should be secure when exposed but not revealed");
        verify(mockHttpClient).send(any(HttpRequest.class), any());
    }

    @Test
    void testIsPasswordSecureMultipleExposedCandidates() throws IOException, InterruptedException {
        String password = "CommonPassword123!";
        String passwordHash = new SHA256PasswordHasher().getHash(password);

        List<Candidate> candidates = List.of(
            new Candidate("hash1", true, 15),
            new Candidate("hash2", true, 8),
            new Candidate(passwordHash, true, 20),
            new Candidate("hash3", false, 3)
        );
        PasswordApiResponseBody responseBody = new PasswordApiResponseBody(candidates);
        String jsonResponse = new Gson().toJson(responseBody);

        setupMockResponse(200, jsonResponse);

        boolean result = apiPasswordChecker.isPasswordSecure(password);

        assertFalse(result, "Password should be insecure when it matches a revealed exposure");
    }

    @Test
    void testIsPasswordSecureEmptyCandidateList() throws IOException, InterruptedException {
        String password = "UniquePassword999!";

        PasswordApiResponseBody responseBody = new PasswordApiResponseBody(List.of());
        String jsonResponse = new Gson().toJson(responseBody);

        setupMockResponse(200, jsonResponse);

        boolean result = apiPasswordChecker.isPasswordSecure(password);

        assertTrue(result, "Password should be secure when no candidates exist");
    }

    @Test
    void testIsPasswordSecureHttpClientThrowsIOException() throws IOException, InterruptedException {
        String password = "AnyPassword!";

        when(mockHttpClient.send(any(HttpRequest.class), any())).thenThrow(new IOException("Network error"));

        assertThrows(RuntimeException.class, () -> apiPasswordChecker.isPasswordSecure(password));
        verify(mockHttpClient).send(any(HttpRequest.class), any());
    }

    @Test
    void testIsPasswordSecureHttpClientThrowsInterruptedException() throws IOException, InterruptedException {
        String password = "AnyPassword!";

        when(mockHttpClient.send(any(HttpRequest.class), any())).thenThrow(new InterruptedException("Thread interrupted"));

        assertThrows(RuntimeException.class, () -> apiPasswordChecker.isPasswordSecure(password));
        verify(mockHttpClient).send(any(HttpRequest.class), any());
    }

    @Test
    void testClose() throws IOException {
        apiPasswordChecker.close();

        verify(mockHttpClient).close();
    }

    @Test
    void testCloseHttpClientThrowsException() {
        doThrow(new IOException("Close failed")).when(mockHttpClient).close();

        assertThrows(IOException.class, () -> apiPasswordChecker.close());
        verify(mockHttpClient).close();
    }
}

