package by.nexus.auth.model.dto;

/**
 * DTO для передачи зашифрованного master key пользователю.
 * Клиент должен использовать пароль пользователя и PBKDF2 с предоставленной солью
 * для генерации derived key, которым можно расшифровать encryptedMasterKey.
 *
 * @param encryptedMasterKey зашифрованный master key (wrapped key)
 * @param keySalt соль для PBKDF2
 */
public record MasterKeyResponse(
        String encryptedMasterKey,
        String keySalt
) {
}
