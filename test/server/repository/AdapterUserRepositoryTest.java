package server.repository;

import exception.WebsiteUsernameNotExistsException;
import exception.PasswordNotSecureException;
import exception.PasswordsNotMatchException;
import exception.UserAlreadyExistsException;
import exception.UserLoggedInException;
import exception.UserNotExistsException;
import exception.IncorrectPasswordException;
import exception.UserNotLoggedInException;
import exception.WebsiteExistingException;
import exception.WebsiteNotExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import server.adapter.UserAdapter;
import server.model.User;
import server.model.WebsitePair;
import server.password.checker.PasswordChecker;
import server.password.encryptor.SymmetricEncryptor;
import server.password.generator.PasswordGenerator;
import server.password.hash.PasswordHasher;
import server.model.Session;

import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdapterUserRepositoryTest {
    private static final String DEFAULT_USER = "Pesho";
    private static final String DEFAULT_PASS = "PeshoP";

    private AdapterUserRepository userRepository;

    private PasswordHasher hasher;
    private SymmetricEncryptor encryptor;
    private PasswordChecker checker;
    private UserAdapter adapter;
    private PasswordGenerator generator;
    private Map<String, User> users;
    private Map<SelectionKey, Session> sessions;
    private SelectionKey sessionKey;

    @BeforeEach
    void setUp() {
        hasher = mock();
        encryptor = mock();
        checker = mock();
        adapter = mock();
        generator = mock();
        users = new HashMap<>();
        sessions = new HashMap<>();
        userRepository = new AdapterUserRepository(users, sessions, hasher, encryptor, checker, generator, adapter);
        sessionKey = mock();
        when(hasher.getHash(DEFAULT_PASS)).thenReturn(DEFAULT_PASS);
        when(encryptor.encrypt(DEFAULT_PASS)).thenReturn(DEFAULT_PASS.getBytes());
        when(encryptor.decrypt(DEFAULT_PASS.getBytes())).thenReturn(DEFAULT_PASS);
    }

    private User addUser(String username, String password, boolean loggedIn) {
        User user = new User(username, password, new HashMap<>());
        users.put(username, user);
        if (loggedIn) {
            sessions.put(sessionKey, new Session(sessionKey, user, true));
        }
        return user;
    }

    private User addDefaultUserWithPassword(boolean loggedIn) {
        User u = addUser(DEFAULT_USER, DEFAULT_PASS, loggedIn);
        when(encryptor.encrypt(DEFAULT_PASS)).thenReturn(DEFAULT_PASS.getBytes());
        when(checker.isPasswordSecure(DEFAULT_PASS)).thenReturn(true);
        return u;
    }

    @Test
    void testRegisterUserValidInput()
        throws UserLoggedInException, PasswordsNotMatchException, UserAlreadyExistsException {

        userRepository.registerUser(mock(SelectionKey.class), "Pesho", "PeshoPass", "PeshoPass");
    }

    @Test
    void testRegisterUserNotMatchingPasswords() {
        assertThrows(
            PasswordsNotMatchException.class,
            () -> userRepository.registerUser(sessionKey, "Pesho", "PeshoPass", "PeshoPass1"),
            "Expected PasswordsNotMatchException when password != passwordRepeat!"
        );
    }

    @Test
    void testRegisterUserExistingUser() {
        addUser("Gosho", "", false);
        assertThrows(
            UserAlreadyExistsException.class,
            () -> userRepository.registerUser(sessionKey, "Gosho", "GoshoPass", "GoshoPass"),
            "Expected to throw UserAlreadyExistsException when registering with existing username."
        );

    }

    @Test
    void testRegisterUserLoggedUser() {
        addUser("Gosho", "", true);

        assertThrows(
            UserLoggedInException.class,
            () -> userRepository.registerUser(sessionKey, "Pesho", "PeshoPass", "PeshoPass"),
            "Expected to throw UserAlreadyLoggedException when registering with existing username."
        );

    }

    @Test
    void testLoginUserValidInput()
        throws UserLoggedInException, UserNotExistsException, IncorrectPasswordException {

        addUser("Pesho", "PeshoP", false);

        userRepository.loginUser(mock(SelectionKey.class), "Pesho", "PeshoP");
    }

    @Test
    void testLoginUserUserAlreadyLogged() {
        addUser("Pesho", "PeshoP", true);

        assertThrows(
            UserLoggedInException.class,
            () -> userRepository.loginUser(sessionKey, "Pesho", "PeshoP"),
            "UserAlreadyLoggedException expected when trying to log when already been logged in"
        );
    }

    @Test
    void testLoginUserUserNotExisting() {
        assertThrows(
            UserNotExistsException.class,
            () -> userRepository.loginUser(sessionKey, "Pesho", "PeshoP"),
            "UserNotExistsException expected when trying to login with unexisting username"
        );
    }

    @Test
    void testLoginUserUserIncorrectPassword() {
        addUser("Pesho", "PeshoP", false);

        when(hasher.getHash("PeshoP")).thenReturn("PeshoP");
        when(hasher.getHash("PeshoP1")).thenReturn("PeshoP1");

        assertThrows(
            IncorrectPasswordException.class,
            () -> userRepository.loginUser(sessionKey, "Pesho", "PeshoP1"),
            "IncorrectPasswordException expected when trying to login with incorrect password"
        );
    }

    @Test
    void testLogoutUserLoggedInUser() throws UserNotLoggedInException {
        addUser("Pesho", "PeshoP", true);

        userRepository.logoutUser(sessionKey);
    }

    @Test
    void testLogoutUserNotLoggedInUser() {
        addUser("Pesho", "PeshoP", false);

        assertThrows(UserNotLoggedInException.class, () -> userRepository.logoutUser(sessionKey),
            "UserNotLoggedInException expected when logging out without been logged in"
        );
    }

    @Test
    void testAddPasswordValidInput()
        throws WebsiteExistingException, PasswordNotSecureException, UserNotLoggedInException {

        addDefaultUserWithPassword(true);

        userRepository.addPassword(sessionKey, "facebook", "Pesho@gmail", "PeshoP");
    }

    @Test
    void testAddPasswordNotSecurePassword() {
        addDefaultUserWithPassword(true);

        when(checker.isPasswordSecure("PeshoP")).thenReturn(false);

        assertThrows(PasswordNotSecureException.class,
            () -> userRepository.addPassword(sessionKey, "facebook", "Pesho@gmail", "PeshoP"),
            "PasswordNotSecureException expected when password insecure"
        );
    }

    @Test
    void testAddPasswordExistingSite() {
        User user = addDefaultUserWithPassword(true);
        user.passwords().put("facebook", new WebsitePair("Pesho@gmail", "PeshoP".getBytes()));

        assertThrows(WebsiteExistingException.class,
            () -> userRepository.addPassword(sessionKey, "facebook", "Pesho@gmail", "PeshoP"),
            "WebsiteExistingException expected when adding password for existing site"
        );
    }

    @Test
    void testAddPasswordUserNotLoggedIn() {
        addDefaultUserWithPassword(false);

        assertThrows(UserNotLoggedInException.class,
            () -> userRepository.addPassword(sessionKey, "facebook", "Pesho@gmail", "PeshoP"),
            "UserNotLoggedInException expected when adding password as not logged in user"
        );
    }

    @Test
    void testGeneratePassword() throws PasswordNotSecureException, UserNotLoggedInException, WebsiteExistingException {

        addDefaultUserWithPassword(true);
        when(generator.generatePassword()).thenReturn("PeshoP");

        assertEquals("PeshoP", userRepository.generatePassword(sessionKey, "facebook", "Pesho@gmail"),
            "PeshoP expected"
        );
    }

    @Test
    void testRemovePasswordValidInput()
        throws WebsiteNotExistsException, WebsiteUsernameNotExistsException, UserNotLoggedInException {

        User user = addDefaultUserWithPassword(true);
        user.passwords().put("facebook", new WebsitePair("Pesho@gmail", "PeshoP".getBytes()));

        userRepository.removePassword(sessionKey, "facebook", "Pesho@gmail");
        assertEquals(0, user.passwords().size(), "Expected empty passwords list");
    }

    @Test
    void testRemovePasswordNotExistingSite() {
        addDefaultUserWithPassword(true);

        assertThrows(
            WebsiteNotExistsException.class, () -> userRepository.removePassword(sessionKey, "facebook", "Pesho@gmail"),
            "Expected WebsiteNotExistsException when removing a non-existing site password"
        );
    }

    @Test
    void testRemovePasswordNotExistingCredentials() {
        User user = addDefaultUserWithPassword(true);
        user.passwords().put("facebook", new WebsitePair("Pesho@gmail", "PeshoP".getBytes()));

        assertThrows(WebsiteUsernameNotExistsException.class,
            () -> userRepository.removePassword(sessionKey, "facebook", "Pesho1@gmail"),
            "Expected CredentialsNotExistException when existing site and non-existing uname"
        );
    }

    @Test
    void testRemovePasswordUserNotLoggedIn() {
        User user = addDefaultUserWithPassword(false);
        user.passwords().put("facebook", new WebsitePair("Pesho@gmail", "PeshoP".getBytes()));

        assertThrows(UserNotLoggedInException.class,
            () -> userRepository.removePassword(sessionKey, "facebook", "Pesho1@gmail"),
            "Expected UserNotLoggedInException when removing password as not logged-in user"
        );
    }

    @Test
    void testRetrieveCredentialsValidInput()
        throws WebsiteNotExistsException, WebsiteUsernameNotExistsException, UserNotLoggedInException {

        User user = addDefaultUserWithPassword(true);
        user.passwords().put("facebook", new WebsitePair("Pesho@gmail", "PeshoP".getBytes()));

        String credentials = userRepository.retrieveCredentials(sessionKey, "facebook", "Pesho@gmail");
        assertEquals("Pesho@gmail PeshoP", credentials, "Not the expected credentials");
    }

    @Test
    void testRetrieveCredentialsNotExistingWebsite() {
        addDefaultUserWithPassword(true);

        assertThrows(WebsiteNotExistsException.class,
            () -> userRepository.retrieveCredentials(sessionKey, "facebook", "Pesho@gmail"),
            "Expected WebsiteNotExistsException when retrieving password of non-existing site"
        );
    }

    @Test
    void testRetrieveCredentialsNotExistingUsername() {
        User user = addDefaultUserWithPassword(true);
        user.passwords().put("facebook", new WebsitePair("Pesho@gmail", "PeshoP".getBytes()));

        assertThrows(WebsiteUsernameNotExistsException.class,
            () -> userRepository.retrieveCredentials(sessionKey, "facebook", "Pesho1@gmail"),
            "Expected WebsiteUsernameNotExistsException when retrieving password of non-existing username and existing site"
        );
    }

    @Test
    void testRetrieveCredentialsNotLoggedInUser() {
        User user = addDefaultUserWithPassword(false);
        user.passwords().put("facebook", new WebsitePair("Pesho@gmail", "PeshoP".getBytes()));

        assertThrows(UserNotLoggedInException.class,
            () -> userRepository.retrieveCredentials(sessionKey, "facebook", "Pesho@gmail"),
            "Expected UserNotLoggedInException when retrieving password as not logged in user"
        );
    }
}