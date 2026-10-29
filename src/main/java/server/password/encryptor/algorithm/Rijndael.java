package server.password.encryptor.algorithm;

import exception.CipherException;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Rijndael implements SymmetricBlockCipher {

    private final SecretKey secretKey;

    /**
     * Encrypts/decrypts data using AES (Rijndael) algorithm with the provided secret key.
     *
     * @param secretKey the encryption/decryption key
     * @throws IllegalArgumentException if secretKey is null
     */
    public Rijndael(SecretKey secretKey) {
        if (secretKey == null) {
            throw new IllegalArgumentException("secretKey is null");
        }
        this.secretKey = secretKey;
    }

    @Override
    public void encrypt(InputStream inputStream, OutputStream outputStream) throws CipherException {
        try {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            var cipherStream = new CipherOutputStream(new BufferedOutputStream(outputStream), cipher);
            byte[] buffer = new byte[BUFF_SIZE];
            var buffInStream = new BufferedInputStream(inputStream);
            int read;
            while ((read = buffInStream.read(buffer)) != -1) {
                cipherStream.write(buffer, 0, read);
            }
            cipherStream.close();
        } catch (Exception e) {
            throw new CipherException("encrypt failed", e);
        }
    }

    @Override
    public void decrypt(InputStream inputStream, OutputStream outputStream) throws CipherException {
        try {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            InputStream encryptedInputStream = new BufferedInputStream(new BufferedInputStream(inputStream));
            OutputStream decryptedOutputStream =
                new CipherOutputStream(new BufferedOutputStream(outputStream), cipher);

            byte[] buffer = new byte[BUFF_SIZE];
            int read;
            while ((read = encryptedInputStream.read(buffer)) != -1) {
                decryptedOutputStream.write(buffer, 0, read);
            }
            decryptedOutputStream.close();
        } catch (Exception e) {
            throw new CipherException("decrypt failed", e);
        }
    }

    private static final int KILOBYTE = 1024;
    private static final int BUFF_SIZE = 8 * KILOBYTE;
    public static final String ENCRYPTION_ALGORITHM = "AES"; // //  Advanced Encryption Standard

}
