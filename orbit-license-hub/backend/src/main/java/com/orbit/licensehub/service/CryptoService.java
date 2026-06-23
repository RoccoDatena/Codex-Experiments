package com.orbit.licensehub.service;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CryptoService {

  private static final int IV_LENGTH = 12;
  private static final int TAG_BITS = 128;

  @Value("${app.crypto.secret-key}")
  private String secretBase64;

  private SecretKey secretKey;

  @PostConstruct
  void init() {
    byte[] keyBytes = Base64.getDecoder().decode(secretBase64);
    this.secretKey = new SecretKeySpec(keyBytes, "AES");
  }

  public String encrypt(String value) {
    try {
      byte[] iv = new byte[IV_LENGTH];
      new SecureRandom().nextBytes(iv);

      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_BITS, iv));
      byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

      byte[] payload = new byte[iv.length + encrypted.length];
      System.arraycopy(iv, 0, payload, 0, iv.length);
      System.arraycopy(encrypted, 0, payload, iv.length, encrypted.length);

      return Base64.getEncoder().encodeToString(payload);
    } catch (GeneralSecurityException ex) {
      throw new IllegalStateException("Unable to encrypt secret", ex);
    }
  }

  public String decrypt(String value) {
    try {
      byte[] payload = Base64.getDecoder().decode(value);
      byte[] iv = new byte[IV_LENGTH];
      byte[] encrypted = new byte[payload.length - IV_LENGTH];
      System.arraycopy(payload, 0, iv, 0, IV_LENGTH);
      System.arraycopy(payload, IV_LENGTH, encrypted, 0, encrypted.length);

      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_BITS, iv));
      return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
    } catch (GeneralSecurityException ex) {
      throw new IllegalStateException("Unable to decrypt secret", ex);
    }
  }
}
