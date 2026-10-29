package server.password.encryptor;

/** Reversible encryption used for website passwords that must later be retrieved. */
public interface SymmetricEncryptor {
    /**
     * Encrypts a plaintext website password.
     *
     * @param password plaintext input
     * @return ciphertext produced by the configured cipher
     */
    byte[] encrypt(String password);

    /**
     * Decrypts bytes produced with a compatible cipher and the same key.
     *
     * @param encryptedPassword ciphertext to decrypt
     * @return plaintext website password
     */
    String decrypt(byte[] encryptedPassword);
}
