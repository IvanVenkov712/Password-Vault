package server.password.checker;

import java.io.Closeable;

/** Checks website passwords against a configured policy or external exposure service. */
public interface PasswordChecker extends Closeable {
    /**
     * Evaluates a plaintext password using the configured checker.
     *
     * @param password candidate password
     * @return true if the checker accepts it; this is not a general strength guarantee
     */
    boolean isPasswordSecure(String password);
}
