package exception;

public class ArgumentCountMismatchException extends PasswordVaultException {
    public ArgumentCountMismatchException(String message) {
        super(message);
    }

    public ArgumentCountMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
