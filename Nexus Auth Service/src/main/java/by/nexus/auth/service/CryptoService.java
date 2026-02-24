package by.nexus.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Сервис для криптографических операций в Auth Service.
 * Используется для шифрования/расшифровки master key пользователей.
 */
@Slf4j
@Service
public class CryptoService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int KEY_LENGTH = 256;
    private static final int ITERATION_COUNT = 65536;

    /**
     * Генерирует случайный master key (256 бит).
     *
     * @return Base64-encoded master key
     */
    public String generateMasterKey() {
        byte[] key = new byte[32]; // 256 bits
        new SecureRandom().nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }

    /**
     * Генерирует случайную соль для PBKDF2.
     *
     * @return Base64-encoded salt
     */
    public String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Шифрует master key с использованием derived key из пароля пользователя.
     *
     * @param masterKey master key для шифрования
     * @param password пароль пользователя
     * @param salt соль для PBKDF2
     * @return Base64-encoded зашифрованный master key
     */
    public String encryptMasterKey(String masterKey, String password, String salt) {
        try {
            SecretKey derivedKey = deriveKey(password, salt);
            byte[] encrypted = encrypt(masterKey.getBytes(StandardCharsets.UTF_8), derivedKey);
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("Failed to encrypt master key", e);
            throw new RuntimeException("Failed to encrypt master key", e);
        }
    }

    /**
     * Расшифровывает master key с использованием derived key из пароля пользователя.
     *
     * @param encryptedMasterKey Base64-encoded зашифрованный master key
     * @param password пароль пользователя
     * @param salt соль для PBKDF2
     * @return расшифрованный master key
     */
    public String decryptMasterKey(String encryptedMasterKey, String password, String salt) {
        try {
            SecretKey derivedKey = deriveKey(password, salt);
            byte[] encrypted = Base64.getDecoder().decode(encryptedMasterKey);
            byte[] decrypted = decrypt(encrypted, derivedKey);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to decrypt master key", e);
            throw new RuntimeException("Failed to decrypt master key", e);
        }
    }

    /**
     * Шифрует данные с использованием AES-256-GCM.
     */
    private byte[] encrypt(byte[] data, SecretKey key) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

        byte[] encryptedData = cipher.doFinal(data);

        // Combine IV + encrypted data
        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedData.length);
        byteBuffer.put(iv);
        byteBuffer.put(encryptedData);

        return byteBuffer.array();
    }

    /**
     * Расшифровывает данные с использованием AES-256-GCM.
     */
    private byte[] decrypt(byte[] encryptedData, SecretKey key) throws Exception {
        ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedData);

        byte[] iv = new byte[GCM_IV_LENGTH];
        byteBuffer.get(iv);

        byte[] cipherText = new byte[byteBuffer.remaining()];
        byteBuffer.get(cipherText);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

        return cipher.doFinal(cipherText);
    }

    /**
     * Генерирует ключ из пароля с использованием PBKDF2.
     */
    private SecretKey deriveKey(String password, String salt) throws Exception {
        byte[] saltBytes = Base64.getDecoder().decode(salt);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, ITERATION_COUNT, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }
}
