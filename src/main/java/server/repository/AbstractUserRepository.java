package server.repository;

import exception.WebsiteUsernameNotExistsException;
import exception.PasswordNotSecureException;
import exception.PasswordsNotMatchException;
import exception.UserAlreadyExistsException;
import exception.UserLoggedInException;
import exception.UserNotExistsException;
import exception.UserNotLoggedInException;
import exception.WebsiteExistingException;
import exception.WebsiteNotExistsException;
import exception.IncorrectPasswordException;
import server.Messages;
import server.model.User;
import server.model.WebsitePair;
import server.password.checker.PasswordChecker;
import server.password.encryptor.SymmetricEncryptor;
import server.password.generator.PasswordGenerator;
import server.password.hash.PasswordHasher;
import server.model.Session;

import java.nio.channels.SelectionKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements account validation and connection-bound sessions in memory.
 * Successful account and credential mutations call {@link #saveChanges()}.
 * Session changes are not persisted, and registration does not create a session.
 */
public abstract class AbstractUserRepository implements UserRepository {
    private final Map<String, User> users;
    private final Map<SelectionKey, Session> sessions;
    private final PasswordHasher hasher;
    private final SymmetricEncryptor encryptor;
    private final PasswordChecker checker;
    private final PasswordGenerator generator;

    /**
     * Creates a repository with injectable storage and password services.
     *
     * @param users mutable accounts indexed by vault username
     * @param sessions mutable sessions indexed by connection key
     * @param hasher account password hashing service
     * @param encryptor website password encryption service
     * @param checker checker applied to added and generated website passwords
     * @param generator website password generator
     */
    public AbstractUserRepository(Map<String, User> users, Map<SelectionKey, Session> sessions, PasswordHasher hasher,
                                  SymmetricEncryptor encryptor, PasswordChecker checker, PasswordGenerator generator) {
        this.users = users;
        this.sessions = sessions;
        this.hasher = hasher;
        this.encryptor = encryptor;
        this.checker = checker;
        this.generator = generator;
    }

    @Override
    public void registerUser(SelectionKey selectionKey, String user, String password, String passwordRepeat)
        throws UserAlreadyExistsException, UserLoggedInException, PasswordsNotMatchException {
        if (selectionKey == null || user == null || password == null || passwordRepeat == null) {
            throw new IllegalArgumentException(Messages.NULL_ARGS_ARE_NOT_ALLOWED);
        }
        if (isUserLoggedIn(selectionKey)) {
            String loggedName = sessions.get(selectionKey).user().userName();
            throw new UserLoggedInException(String.format(Messages.CANNOT_REGISTER_WHEN_LOGGED_IN, loggedName));
        }
        if (users.containsKey(user)) {
            throw new UserAlreadyExistsException(Messages.USER_ALREADY_REGISTERED);
        }
        if (!password.equals(passwordRepeat)) {
            throw new PasswordsNotMatchException(Messages.PASSWORDS_NOT_MATCH);
        }
        String hashedPassword = hasher.getHash(password);
        users.put(user, new User(user, hashedPassword, new ConcurrentHashMap<>()));

        saveChanges();
    }

    @Override
    public void loginUser(SelectionKey selectionKey, String userName, String password)
        throws UserNotExistsException, IncorrectPasswordException, UserLoggedInException {
        if (selectionKey == null || userName == null || password == null) {
            throw new IllegalArgumentException(Messages.NULL_ARGS_ARE_NOT_ALLOWED);
        }
        if (isUserLoggedIn(selectionKey)) {
            String loggedName = sessions.get(selectionKey).user().userName();
            throw new UserLoggedInException(String.format(Messages.USER_ALREADY_LOGGED_IN_EXCLAMATION, loggedName));
        }
        if (!users.containsKey(userName)) {
            throw new UserNotExistsException(Messages.USER_NOT_REGISTERED_EXCLAMATION);
        }
        User user = users.get(userName);
        if (!hasher.getHash(password).equals(user.hashedPassword())) {
            throw new IncorrectPasswordException(Messages.INCORRECT_PASSWORD);
        }
        sessions.put(selectionKey, new Session(selectionKey, user, true));
    }

    @Override
    public void logoutUser(SelectionKey selectionKey) throws UserNotLoggedInException {
        if (selectionKey == null) {
            throw new IllegalArgumentException(Messages.NULL_SELECTION_KEY_NOT_ALLOWED);
        }
        if (!isUserLoggedIn(selectionKey)) {
            throw new UserNotLoggedInException(Messages.USER_NOT_LOGGED_IN);
        }
        sessions.remove(selectionKey);
    }

    @Override
    public void addPassword(SelectionKey selectionKey, String website, String userName, String password)
        throws PasswordNotSecureException, UserNotLoggedInException, WebsiteExistingException {
        if (selectionKey == null || userName == null || password == null || website == null) {
            throw new IllegalArgumentException(Messages.NULL_ARGS_ARE_NOT_ALLOWED);
        }
        if (!isUserLoggedIn(selectionKey)) {
            throw new UserNotLoggedInException(Messages.USER_MUST_BE_LOGGED_IN_TO_CHANGE_PASSWORD);
        }

        if (!checker.isPasswordSecure(password)) {
            throw new PasswordNotSecureException(Messages.PASSWORD_COMPROMISED);
        }

        User user = sessions.get(selectionKey).user();
        if (user.passwords().containsKey(website)) {
            throw new WebsiteExistingException(String.format(
                Messages.WEBSITE_ALREADY_EXISTS, user.userName(), website)
            );
        }
        user.passwords().put(website, new WebsitePair(userName, encryptor.encrypt(password)));
        saveChanges();
    }

    @Override
    public String generatePassword(SelectionKey selectionKey, String website, String userName)
        throws UserNotLoggedInException, WebsiteExistingException, PasswordNotSecureException {
        String generatedPassword = generator.generatePassword();
        addPassword(selectionKey, website, userName, generatedPassword);
        saveChanges();
        return generatedPassword;
    }

    @Override
    public String retrieveCredentials(SelectionKey selectionKey, String website, String userName)
        throws UserNotLoggedInException, WebsiteUsernameNotExistsException, WebsiteNotExistsException {
        if (selectionKey == null || userName == null || website == null) {
            throw new IllegalArgumentException(Messages.NULL_ARGS_ARE_NOT_ALLOWED);
        }
        if (!isUserLoggedIn(selectionKey)) {
            throw new UserNotLoggedInException(Messages.USER_MUST_BE_LOGGED_IN_TO_RETRIEVE_CREDENTIALS);
        }
        User user = sessions.get(selectionKey).user();
        if (!user.passwords().containsKey(website)) {
            throw new WebsiteNotExistsException(
                String.format(Messages.WEBSITE_NOT_EXISTS_EXCLAMATION, user.userName(), website)
            );
        }
        WebsitePair pair = user.passwords().get(website);
        if (!userName.equals(pair.userName())) {
            throw new WebsiteUsernameNotExistsException(
                String.format(Messages.WEBSITE_USERNAME_NOT_MATCH, user.userName(), website, userName, pair.userName())
            );
        }
        return pair.userName() + " " + encryptor.decrypt(pair.encryptedPassword());
    }

    @Override
    public void removePassword(SelectionKey selectionKey, String website, String userName)
        throws WebsiteUsernameNotExistsException, UserNotLoggedInException, WebsiteNotExistsException {
        if (selectionKey == null || userName == null || website == null) {
            throw new IllegalArgumentException(Messages.NULL_ARGS_ARE_NOT_ALLOWED);
        }
        if (!isUserLoggedIn(selectionKey)) {
            throw new UserNotLoggedInException(Messages.USER_MUST_BE_LOGGED_IN_TO_RETRIEVE_CREDENTIALS);
        }
        User user = sessions.get(selectionKey).user();
        if (!user.passwords().containsKey(website)) {
            throw new WebsiteNotExistsException(
                String.format(Messages.WEBSITE_NOT_EXISTS_EXCLAMATION, user.userName(), website));
        }
        WebsitePair pair = user.passwords().get(website);
        if (!userName.equals(pair.userName())) {
            throw new WebsiteUsernameNotExistsException(
                String.format(
                    Messages.WEBSITE_USERNAME_NOT_MATCH, user.userName(), website, userName, pair.userName()));
        }
        user.passwords().remove(website);

        saveChanges();
    }

    private boolean isUserLoggedIn(SelectionKey selectionKey) {
        if (!sessions.containsKey(selectionKey)) {
            return false;
        }
        return sessions.get(selectionKey).loggedIn() && sessions.get(selectionKey).user() != null;
    }

    /** @return the live mutable account map to be persisted by subclasses */
    protected Map<String, User> getUsers() {
        return users;
    }

    /** @return the configured password checker, which must be closed with the repository */
    protected PasswordChecker getChecker() {
        return checker;
    }
}
