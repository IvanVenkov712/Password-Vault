package server.password.hash;

/** Converts plaintext passwords to a deterministic hash representation. */
public interface PasswordHasher {
    /**
     * Hashes a password for account verification or exposure lookup.
     *
     * @param password plaintext input
     * @return the hash representation defined by the implementation
     */
    String getHash(String password);
}
