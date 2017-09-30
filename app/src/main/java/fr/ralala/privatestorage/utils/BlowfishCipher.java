package fr.ralala.privatestorage.utils;

import android.util.Base64;

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
  private final static String ALGORITM = "Blowfish";
  private final static String BOUNCY_CASTLE = "BC";

  public String encrypt(String key, String plainText) throws Exception {
    return new String(Base64.encode(crypt(Cipher.ENCRYPT_MODE, key, plainText.getBytes("UTF-8")), Base64.DEFAULT));
  }

  public String decrypt(String key, String encryptedText) throws GeneralSecurityException {
    return new String(crypt(Cipher.DECRYPT_MODE, key, Base64.decode(encryptedText, Base64.DEFAULT)));
  }

  private byte[] crypt(int mode, String key, byte[] text) throws GeneralSecurityException {
    SecretKey secret_key = new SecretKeySpec(key.getBytes(), ALGORITM);
    Cipher cipher = Cipher.getInstance(ALGORITM, BOUNCY_CASTLE);
    cipher.init(mode, secret_key);
    return cipher.doFinal(text);
  }
}
