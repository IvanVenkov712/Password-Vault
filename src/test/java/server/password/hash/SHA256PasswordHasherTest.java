package server.password.hash;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SHA256PasswordHasherTest {

    private SHA256PasswordHasher hasher;

    @BeforeEach
    void setUp() {
        hasher = new SHA256PasswordHasher();
    }

    @Test
    void testGetHashDifferentPasswordsReturnsDifferentHashes() {
        String password1 = "Password123!";
        String password2 = "Password456!";

        String hash1 = hasher.getHash(password1);
        String hash2 = hasher.getHash(password2);

        assertNotEquals(hash1, hash2, "Different passwords should produce different hashes");
    }

    @Test
    void testGetHashEmptyPasswordReturnsValidHash() {
        String emptyPassword = "";

        String hash = hasher.getHash(emptyPassword);

        assertNotNull(hash, "Hash should not be null");
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", hash, "Not correct hash for empty");
    }

    @Test
    void testGetHashNonEmptyPasswordReturnsValidHash() {
        String emptyPassword = "MyPass";

        String hash = hasher.getHash(emptyPassword);

        assertNotNull(hash, "Hash should not be null");
        assertEquals("7c0b46b25fb11caa0ae435e8c14fa400a062bc37443ba996dbb84479e6ce40cd", hash, "Not correct hash for non-empty");
    }

    @Test
    void testGetHashMultipleInstancesSameResult() {
        SHA256PasswordHasher hasher1 = new SHA256PasswordHasher();
        SHA256PasswordHasher hasher2 = new SHA256PasswordHasher();

        String password = "TestPassword";
        String hash1 = hasher1.getHash(password);
        String hash2 = hasher2.getHash(password);

        assertEquals(hash1, hash2, "Different hasher instances should produce same hash");
    }
}

