package by.nexus.core.service.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

@Slf4j
@Service
public class CryptoService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int KEY_LENGTH = 256;
    private static final int ITERATION_COUNT = 65536;
    
    @Value("${nexus.encryption.master-key:}")
    private String masterKey;

    /**
     * Генерирует случайный ключ для шифрования файла
     */
    public String generateFileKey() {
        byte[] key = new byte[32]; // 256 bits
        new SecureRandom().nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }

    /**
     * Шифрует данные с использованием ключа
     */
    public byte[] encrypt(byte[] data, String key) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        
        SecretKey secretKey = deriveKey(key);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
        
        byte[] encryptedData = cipher.doFinal(data);
        
        // Combine IV + encrypted data
        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedData.length);
        byteBuffer.put(iv);
        byteBuffer.put(encryptedData);
        
        return byteBuffer.array();
    }

    /**
     * Расшифровывает данные с использованием ключа
     */
    public byte[] decrypt(byte[] encryptedData, String key) throws Exception {
        ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedData);
        
        byte[] iv = new byte[GCM_IV_LENGTH];
        byteBuffer.get(iv);
        
        byte[] cipherText = new byte[byteBuffer.remaining()];
        byteBuffer.get(cipherText);
        
        SecretKey secretKey = deriveKey(key);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
        
        return cipher.doFinal(cipherText);
    }

    /**
     * Шифрует строку и возвращает Base64
     */
    public String encryptString(String data, String key) throws Exception {
        byte[] encrypted = encrypt(data.getBytes(StandardCharsets.UTF_8), key);
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * Расшифровывает Base64 строку
     */
    public String decryptString(String encryptedData, String key) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(encryptedData);
        byte[] decrypted = decrypt(decoded, key);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * Шифрует ключ файла с использованием мастер-ключа пользователя
     */
    public String encryptFileKey(String fileKey, String userMasterKey) throws Exception {
        return encryptString(fileKey, userMasterKey);
    }

    /**
     * Расшифровывает ключ файла с использованием мастер-ключа пользователя
     */
    public String decryptFileKey(String encryptedFileKey, String userMasterKey) throws Exception {
        return decryptString(encryptedFileKey, userMasterKey);
    }

    /**
     * Генерирует ключ из строки с использованием PBKDF2
     */
    private SecretKey deriveKey(String password) throws Exception {
        byte[] salt = new byte[16];
        // Use first 16 bytes of password as salt for simplicity
        // In production, use a proper salt storage mechanism
        System.arraycopy(password.getBytes(StandardCharsets.UTF_8), 0, salt, 0, Math.min(16, password.length()));
        
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    /**
     * Генерирует мастер-ключ из пароля пользователя
     */
    public String deriveMasterKeyFromPassword(String password, String salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(StandardCharsets.UTF_8), ITERATION_COUNT, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        return Base64.getEncoder().encodeToString(tmp.getEncoded());
    }

    /**
     * Генерирует случайную соль
     */
    public String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}
