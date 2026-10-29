package server.password.algorithm;

import org.junit.jupiter.api.Test;
import server.password.encryptor.algorithm.Rijndael;

import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static server.password.encryptor.SymmetricEncryptorImpl.generateSecretKey;

class RijndaelTest {
    @Test
    void testEncryptMatchesTheOriginalNonEmpty() throws Exception {
        SecretKey key = generateSecretKey();
        Rijndael cipher = new Rijndael(key);

        byte[] original = "Hello, World! We will encrypt and then decrypt this very important message and hope it remains the same!".getBytes();

        try(ByteArrayInputStream input = new ByteArrayInputStream(original);
            ByteArrayOutputStream encrypted = new ByteArrayOutputStream()
        ){
            cipher.encrypt(input, encrypted);
            try(
                ByteArrayInputStream encryptedInput = new ByteArrayInputStream(encrypted.toByteArray());
                ByteArrayOutputStream decrypted = new ByteArrayOutputStream()){
                cipher.decrypt(encryptedInput, decrypted);
                byte[] actual = decrypted.toByteArray();

                assertArrayEquals(original, actual, "Not what was expected!");
            }
        }
        assertTrue(true);
    }

    @Test
    void testEncryptEmpty() throws Exception {
        SecretKey key = generateSecretKey();
        Rijndael cipher = new Rijndael(key);

        byte[] original = new byte[0];

        try(ByteArrayInputStream input = new ByteArrayInputStream(original);
            ByteArrayOutputStream output = new ByteArrayOutputStream()
        ){
            cipher.encrypt(input, output);
            try(
                ByteArrayInputStream encryptedInput = new ByteArrayInputStream(output.toByteArray());
                ByteArrayOutputStream decrypted = new ByteArrayOutputStream()){
                cipher.decrypt(encryptedInput, decrypted);
                byte[] actual = decrypted.toByteArray();

                assertArrayEquals(original, actual, "Not what was expected!");
            }
        }
        assertTrue(true);
    }


    @Test
    void testEncryptedDataDifferentFromOriginal() throws Exception {
        SecretKey key = generateSecretKey();
        Rijndael cipher = new Rijndael(key);

        byte[] original = "Secret Data".getBytes();

        try(ByteArrayInputStream input = new ByteArrayInputStream(original);
            ByteArrayOutputStream encrypted = new ByteArrayOutputStream()
        ){
            cipher.encrypt(input, encrypted);
            byte[] encryptedData = encrypted.toByteArray();

            assertNotEquals(new String(original), new String(encryptedData),
                "Encrypted data should be different from original");
        }
    }

    @Test
    void testMultipleEncryptDecryptCycles() throws Exception {
        SecretKey key = generateSecretKey();
        Rijndael cipher = new Rijndael(key);

        String[] testData = {"Hello", "World", "Test", "Password", "123456"};

        for (String data : testData) {
            byte[] original = data.getBytes();

            try(ByteArrayInputStream input = new ByteArrayInputStream(original);
                ByteArrayOutputStream encrypted = new ByteArrayOutputStream()
            ){
                cipher.encrypt(input, encrypted);
                try(
                    ByteArrayInputStream encryptedInput = new ByteArrayInputStream(encrypted.toByteArray());
                    ByteArrayOutputStream decrypted = new ByteArrayOutputStream()){
                    cipher.decrypt(encryptedInput, decrypted);
                    byte[] actual = decrypted.toByteArray();

                    assertArrayEquals(original, actual, "Data " + data + " should be preserved");
                }
            }
        }
    }

    @Test
    void testDifferentKeysProduceDifferentCiphertexts() throws Exception {
        SecretKey key1 = generateSecretKey();
        SecretKey key2 = generateSecretKey();
        Rijndael cipher1 = new Rijndael(key1);
        Rijndael cipher2 = new Rijndael(key2);

        byte[] data = "TestData".getBytes();

        try(ByteArrayInputStream input1 = new ByteArrayInputStream(data);
            ByteArrayOutputStream encrypted1 = new ByteArrayOutputStream()
        ){
            cipher1.encrypt(input1, encrypted1);

            try(ByteArrayInputStream input2 = new ByteArrayInputStream(data);
                ByteArrayOutputStream encrypted2 = new ByteArrayOutputStream()
            ){
                cipher2.encrypt(input2, encrypted2);

                byte[] ciphertext1 = encrypted1.toByteArray();
                byte[] ciphertext2 = encrypted2.toByteArray();

                assertNotEquals(new String(ciphertext1), new String(ciphertext2),
                    "Different keys should produce different ciphertexts");
            }
        }
    }

    @Test
    void testBinaryData() throws Exception {
        SecretKey key = generateSecretKey();
        Rijndael cipher = new Rijndael(key);

        byte[] original = new byte[256];
        for (int i = 0; i < 256; i++) {
            original[i] = (byte) i;
        }

        try(ByteArrayInputStream input = new ByteArrayInputStream(original);
            ByteArrayOutputStream encrypted = new ByteArrayOutputStream()
        ){
            cipher.encrypt(input, encrypted);
            try(
                ByteArrayInputStream encryptedInput = new ByteArrayInputStream(encrypted.toByteArray());
                ByteArrayOutputStream decrypted = new ByteArrayOutputStream()){
                cipher.decrypt(encryptedInput, decrypted);
                byte[] actual = decrypted.toByteArray();

                assertArrayEquals(original, actual, "All byte values should be preserved");
            }
        }
    }
}