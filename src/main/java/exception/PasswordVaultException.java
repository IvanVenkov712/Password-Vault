package exception;

/** Base checked exception for business errors returned to the client by the command executor. */
public class PasswordVaultException extends Exception {
    public PasswordVaultException(String message) {
        super(message);
    }

    public PasswordVaultException(String message, Throwable cause) {
        super(message, cause);
    }
}
