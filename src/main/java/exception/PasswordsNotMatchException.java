package exception;

/** Indicates that account registration passwords did not match. */
public class PasswordsNotMatchException extends PasswordVaultException {
    public PasswordsNotMatchException(String message) {
        super(message);
    }

    public PasswordsNotMatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
