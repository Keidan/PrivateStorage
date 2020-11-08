package fr.ralala.privatestorage.utils;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 *******************************************************************************
 * <p><b>Project PrivateStorage</b><br/>
 * Blowfish Cipher (encrypt/decrypt).
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class BlowfishCipher {
  private final static String ALGORITHM = "Blowfish";
  private final static String BOUNCY_CASTLE = "BC";

  public String encrypt(String key, String plainText) throws Exception {
    return new String(Base64.encode(crypt(Cipher.ENCRYPT_MODE, key, plainText.getBytes(StandardCharsets.UTF_8)), Base64.DEFAULT));
  }

  public String decrypt(String key, String encryptedText) throws GeneralSecurityException {
    return new String(crypt(Cipher.DECRYPT_MODE, key, Base64.decode(encryptedText, Base64.DEFAULT)));
  }

  private byte[] crypt(int mode, String key, byte[] text) throws GeneralSecurityException {
    SecretKey secret_key = new SecretKeySpec(key.getBytes(), ALGORITHM);
    Cipher cipher = Cipher.getInstance(ALGORITHM, BOUNCY_CASTLE);
    cipher.init(mode, secret_key);
    return cipher.doFinal(text);
  }
}
