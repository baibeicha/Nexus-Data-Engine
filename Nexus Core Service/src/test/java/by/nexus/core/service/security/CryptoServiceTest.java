package by.nexus.core.service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CryptoServiceTest {

    private CryptoService cryptoService;

    @BeforeEach
    void setUp() {
        cryptoService = new CryptoService();
        ReflectionTestUtils.setField(cryptoService, "masterKey", "test-master-key-for-encryption");
    }

    @Test
    void generateFileKey_ShouldReturnNonEmptyKey() {
        String key = cryptoService.generateFileKey();
        
        assertNotNull(key);
        assertFalse(key.isEmpty());
        assertEquals(44, key.length()); // Base64 encoded 32 bytes
    }

    @Test
    void generateSalt_ShouldReturnNonEmptySalt() {
        String salt = cryptoService.generateSalt();
        
        assertNotNull(salt);
        assertFalse(salt.isEmpty());
    }

    @Test
    void encryptAndDecrypt_ShouldReturnOriginalData() throws Exception {
        String key = cryptoService.generateFileKey();
        String originalData = "Hello, World! This is a test message.";
        byte[] dataBytes = originalData.getBytes();
        
        byte[] encrypted = cryptoService.encrypt(dataBytes, key);
        byte[] decrypted = cryptoService.decrypt(encrypted, key);
        
        assertNotNull(encrypted);
        assertTrue(encrypted.length > dataBytes.length); // Encrypted data should be larger due to IV
        assertArrayEquals(dataBytes, decrypted);
    }

    @Test
    void encryptStringAndDecryptString_ShouldReturnOriginalString() throws Exception {
        String key = cryptoService.generateFileKey();
        String original = "Test string with special chars: !@#$%^&*()";
        
        String encrypted = cryptoService.encryptString(original, key);
        String decrypted = cryptoService.decryptString(encrypted, key);
        
        assertNotNull(encrypted);
        assertNotEquals(original, encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    void encryptFileKeyAndDecryptFileKey_ShouldReturnOriginalKey() throws Exception {
        String userMasterKey = cryptoService.generateFileKey();
        String fileKey = cryptoService.generateFileKey();
        
        String encryptedKey = cryptoService.encryptFileKey(fileKey, userMasterKey);
        String decryptedKey = cryptoService.decryptFileKey(encryptedKey, userMasterKey);
        
        assertNotNull(encryptedKey);
        assertNotEquals(fileKey, encryptedKey);
        assertEquals(fileKey, decryptedKey);
    }

    @Test
    void deriveMasterKeyFromPassword_ShouldReturnConsistentKey() throws Exception {
        String password = "mySecretPassword123";
        String salt = cryptoService.generateSalt();
        
        String key1 = cryptoService.deriveMasterKeyFromPassword(password, salt);
        String key2 = cryptoService.deriveMasterKeyFromPassword(password, salt);
        
        assertNotNull(key1);
        assertEquals(key1, key2); // Same password and salt should produce same key
    }

    @Test
    void deriveMasterKeyFromPassword_DifferentSaltsShouldProduceDifferentKeys() throws Exception {
        String password = "mySecretPassword123";
        String salt1 = cryptoService.generateSalt();
        String salt2 = cryptoService.generateSalt();
        
        String key1 = cryptoService.deriveMasterKeyFromPassword(password, salt1);
        String key2 = cryptoService.deriveMasterKeyFromPassword(password, salt2);
        
        assertNotNull(key1);
        assertNotNull(key2);
        assertNotEquals(key1, key2); // Different salts should produce different keys
    }

    @Test
    void decrypt_WithWrongKey_ShouldThrowException() throws Exception {
        String correctKey = cryptoService.generateFileKey();
        String wrongKey = cryptoService.generateFileKey();
        String originalData = "Test data";
        
        byte[] encrypted = cryptoService.encrypt(originalData.getBytes(), correctKey);
        
        assertThrows(Exception.class, () -> {
            cryptoService.decrypt(encrypted, wrongKey);
        });
    }

    @Test
    void encryptEmptyData_ShouldWork() throws Exception {
        String key = cryptoService.generateFileKey();
        byte[] emptyData = new byte[0];
        
        byte[] encrypted = cryptoService.encrypt(emptyData, key);
        byte[] decrypted = cryptoService.decrypt(encrypted, key);
        
        assertArrayEquals(emptyData, decrypted);
    }

    @Test
    void encryptLargeData_ShouldWork() throws Exception {
        String key = cryptoService.generateFileKey();
        byte[] largeData = new byte[1024 * 1024]; // 1MB
        new java.util.Random().nextBytes(largeData);
        
        byte[] encrypted = cryptoService.encrypt(largeData, key);
        byte[] decrypted = cryptoService.decrypt(encrypted, key);
        
        assertArrayEquals(largeData, decrypted);
    }
}
