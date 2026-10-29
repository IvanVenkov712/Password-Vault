package server.password.hash;

import java.security.MessageDigest;

public class SHA256PasswordHasher implements PasswordHasher {
    private static final String ALGORITHM_NAME = "SHA-256";
    private static final String ENCODING = "UTF-8";
    private static final int MASK = 0xff;

    @Override
    public String getHash(String password) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(ALGORITHM_NAME);
            messageDigest.update(password.getBytes(ENCODING));
            return bytesToHex(messageDigest.digest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(MASK & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
