package server.password.generator;

import java.security.SecureRandom;

/**
 * Uses {@link SecureRandom} to sample letters, digits, and punctuation independently.
 * The default length is 16 and the minimum is 12; character categories are not guaranteed.
 */
public class PasswordGeneratorImpl implements PasswordGenerator {
    private static final String CHARACTERS =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "abcdefghijklmnopqrstuvwxyz" +
            "0123456789" +
            "!@#$%^&*()-_=+[]{}|;:,.<>?~";

    private static final int MIN_LENGTH = 12;
    private static final int DEFAULT_LENGTH = 16;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String generatePassword(int length) {
        if (length < MIN_LENGTH) {
            throw new IllegalArgumentException("Password length should be at least 12 characters");
        }

        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(index));
        }
        return password.toString();
    }

    @Override
    public String generatePassword() {
        return generatePassword(DEFAULT_LENGTH);
    }
}
