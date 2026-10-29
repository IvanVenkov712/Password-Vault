package exception;

/** Indicates that the configured checker rejected a website password. */
public class PasswordNotSecureException extends PasswordVaultException {
    public PasswordNotSecureException(String message) {
        super(message);
    }

    public PasswordNotSecureException(String message, Throwable cause) {
        super(message, cause);
    }
}
