package server.password.encryptor;

public interface SymmetricEncryptor {
    byte[] encrypt(String password);

    String decrypt(byte[] encryptedPassword);
}
