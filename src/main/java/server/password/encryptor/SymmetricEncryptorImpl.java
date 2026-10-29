package server.password.encryptor;

import server.password.encryptor.algorithm.Rijndael;
import server.password.encryptor.algorithm.SymmetricBlockCipher;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static server.password.encryptor.algorithm.Rijndael.ENCRYPTION_ALGORITHM;

public class SymmetricEncryptorImpl implements SymmetricEncryptor {

    private static final String ENCODING = "UTF-8";
    private static final String DEFAULT_KEY_FILE_PATH = "SecretKey";
    private static final int KEY_SIZE_IN_BITS = 128; // Key sizes like 192 or 256 might not be available on all systems


    private final SymmetricBlockCipher cipher;

    public SymmetricEncryptorImpl(SymmetricBlockCipher cipher) {
        this.cipher = cipher;
    }

    public SymmetricEncryptorImpl() {
        SecretKey key;
        try {
            if (Files.exists(Path.of(DEFAULT_KEY_FILE_PATH))) {
                key = loadSecretKey();
            } else {
                key = generateSecretKey();
                persistSecretKey(key);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        SymmetricBlockCipher cipher = new Rijndael(key);
        this(cipher);
    }

    @Override
    public byte[] encrypt(String password) {
        try (var input = new ByteArrayInputStream(password.getBytes(ENCODING));
             var output = new ByteArrayOutputStream()) {

            cipher.encrypt(input, output);
            return output.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String decrypt(byte[] encryptedPassword) {
        if (encryptedPassword == null) {
            throw new IllegalArgumentException("encryptedPassword is null");
        }
        try (var input = new ByteArrayInputStream(encryptedPassword);
             var output = new ByteArrayOutputStream()) {

            cipher.decrypt(input, output);
            return output.toString(ENCODING);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static SecretKey generateSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM);
        keyGenerator.init(KEY_SIZE_IN_BITS);
        SecretKey secretKey = keyGenerator.generateKey();

        // In order to view the key in some text representation, we'll convert it to Base64 format
        // Comment the next two lines if that's not needed
        String base64Key = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        System.out.println("Generated Secret Key (Base64-encoded): " + base64Key);

        return secretKey;
    }

    private static void persistSecretKey(SecretKey secretKey) throws IOException {
        byte[] keyBytes = secretKey.getEncoded();
        Path keyFilePath = Path.of(DEFAULT_KEY_FILE_PATH);

        // Write key bytes to file
        Files.write(keyFilePath, keyBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // (Optional) Set file permissions to restrict access
        Files.setPosixFilePermissions(keyFilePath, PosixFilePermissions.fromString("rw-------"));
    }

    private static SecretKey loadSecretKey() throws IOException {
        byte[] keyBytes = Files.readAllBytes(Path.of(DEFAULT_KEY_FILE_PATH));
        return new SecretKeySpec(keyBytes, ENCRYPTION_ALGORITHM);
    }
}