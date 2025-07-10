package yj.AutoTrade.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Service
@Slf4j
public class ApiKeyEncryptionService {

    private final String secretKey;
    private final String algorithm = "AES";

    public ApiKeyEncryptionService(@Value("${app.encryption.secret-key:defaultSecretKey123456}") String secretKey) {
        this.secretKey = secretKey;
    }

    public String encrypt(String plainText) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), algorithm);
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("API Key 암호화 실패: {}", e.getMessage());
            throw new RuntimeException("API Key 암호화 실패", e);
        }
    }

    public String decrypt(String encryptedText) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), algorithm);
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            log.error("API Key 복호화 실패: {}", e.getMessage());
            throw new RuntimeException("API Key 복호화 실패", e);
        }
    }

    public boolean isValidKey(String key) {
        try {
            String decrypted = decrypt(key);
            return decrypted != null && !decrypted.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}