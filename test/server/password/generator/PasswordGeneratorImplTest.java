package server.password.generator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordGeneratorImplTest {
    @Test
    void testGeneratePasswordTooShort() {
        PasswordGeneratorImpl generator = new PasswordGeneratorImpl();
        assertThrows(IllegalArgumentException.class, () -> generator.generatePassword(11), "IllegalArgumentException expected for short password");
    }

    @Test
    void testGeneratePasswordDefault() {
        PasswordGeneratorImpl generator = new PasswordGeneratorImpl();
        String pass = generator.generatePassword();
        assertEquals(16, pass.length(), "Expected default length of 16");
    }

    @Test
    void testGeneratePasswordLonger() {
        PasswordGeneratorImpl generator = new PasswordGeneratorImpl();
        String pass = generator.generatePassword(17);
        assertEquals(17, pass.length(), "Expected default length of 17");
    }

    @Test
    void testGeneratePasswordMinimumLength() {
        PasswordGeneratorImpl generator = new PasswordGeneratorImpl();
        String pass = generator.generatePassword(12);
        assertEquals(12, pass.length(), "Minimum length of 12 should be accepted");
    }
}