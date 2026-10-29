package exception;

public class WebsiteExistingException extends PasswordVaultException {
    public WebsiteExistingException(String message) {
        super(message);
    }

    public WebsiteExistingException(String message, Throwable cause) {
        super(message, cause);
    }
}
