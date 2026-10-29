package server.password.generator;

public interface PasswordGenerator {
    String generatePassword(int length);

    String generatePassword();
}
