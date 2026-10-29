package server;

/** Shared validation and failure messages returned by repository operations and the executor. */
public final class Messages {

    public static final String NULL_ARGS_ARE_NOT_ALLOWED = "Null args are not allowed";

    public static final String NULL_SELECTION_KEY_NOT_ALLOWED = "null selection key not allowed";

    public static final String CANNOT_REGISTER_WHEN_LOGGED_IN =
        "You cannot register when you are already logged in as %s";

    public static final String USER_ALREADY_REGISTERED = "This user is already registered";

    public static final String PASSWORDS_NOT_MATCH = "Password and repeated password not match";

    public static final String USER_ALREADY_LOGGED_IN_EXCLAMATION = "User %s has already been logged in!";

    public static final String USER_NOT_REGISTERED_EXCLAMATION = "This user is not registered!";

    public static final String INCORRECT_PASSWORD = "The password you typed was incorrect";

    public static final String USER_NOT_LOGGED_IN = "The user is not logged in";

    public static final String USER_MUST_BE_LOGGED_IN_TO_CHANGE_PASSWORD =
        "The user must be logged in to change password!";

    public static final String USER_MUST_BE_LOGGED_IN_TO_RETRIEVE_CREDENTIALS =
        "User must be logged in to retrieve credentials";

    public static final String PASSWORD_COMPROMISED = "This password has been compromised. Please choose another!";

    public static final String WEBSITE_ALREADY_EXISTS = "User %s already has a password for website '%s'.";

    public static final String WEBSITE_NOT_EXISTS_EXCLAMATION = "User %s has no credentials for website '%s'!";

    public static final String WEBSITE_USERNAME_NOT_MATCH =
        "User %s at website '%s': provided username '%s' does not match stored username '%s'.";

    public static final String INTERNAL_SERVER_ERROR = "Internal server error";
}
