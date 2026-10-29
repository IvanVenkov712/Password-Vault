package exception;

public class PasswordNotSecureException extends PasswordVaultException {
    public PasswordNotSecureException(String message) {
        super(message);
    }

    public PasswordNotSecureException(String message, Throwable cause) {
        super(message, cause);
    }
}
