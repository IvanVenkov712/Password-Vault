package server.password.generator;

/** Generates website passwords without storing them or checking exposure status. */
public interface PasswordGenerator {
    /**
     * Generates a password of the requested length.
     *
     * @param length desired number of characters
     * @return a generated password
     * @throws IllegalArgumentException if the implementation does not support the length
     */
    String generatePassword(int length);

    /** @return a password using the implementation's default length */
    String generatePassword();
}
