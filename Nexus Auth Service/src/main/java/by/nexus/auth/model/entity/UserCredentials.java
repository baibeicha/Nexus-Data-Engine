package by.nexus.auth.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
@Table(name = "user_credentials")
public class UserCredentials {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role = "USER";

    @Column(nullable = false)
    private boolean enabled = true;

    /**
     * Зашифрованный master key пользователя (wrapped key).
     * Ключ шифруется с использованием derived key из пароля пользователя.
     */
    @Column(name = "encrypted_master_key", nullable = false)
    private String encryptedMasterKey;

    /**
     * Соль для PBKDF2 при генерации derived key из пароля.
     * Используется для расшифровки encryptedMasterKey на клиенте.
     */
    @Column(name = "key_salt", nullable = false)
    private String keySalt;

    /**
     * @deprecated Используйте getEncryptedMasterKey()
     */
    @Deprecated
    public String getKey() {
        return encryptedMasterKey;
    }

    /**
     * @deprecated Используйте setEncryptedMasterKey()
     */
    @Deprecated
    public void setKey(String key) {
        this.encryptedMasterKey = key;
    }
}
