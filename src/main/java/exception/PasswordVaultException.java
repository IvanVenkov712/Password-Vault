package exception;

public class PasswordVaultException extends Exception {
    public PasswordVaultException(String message) {
        super(message);
    }

    public PasswordVaultException(String message, Throwable cause) {
        super(message, cause);
    }
}