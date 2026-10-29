package exception;

public class PasswordsNotMatchException extends PasswordVaultException {
    public PasswordsNotMatchException(String message) {
        super(message);
    }

    public PasswordsNotMatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
