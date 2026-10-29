package exception;

public class WebsiteNotExistsException extends PasswordVaultException {
    public WebsiteNotExistsException(String message) {
        super(message);
    }

    public WebsiteNotExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
