package fr.ralala.privatestorage.ui.utils;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.Manifest;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;


@TargetApi(Build.VERSION_CODES.M)
public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {
  private final Context mContext;
  private final FingerprintHandlerListener mListener;
  private Cipher mCipher;
  private KeyStore mKeyStore;
  private FingerprintManager mFingerprintManager;
  private KeyguardManager mKeyguardManager;

  public interface FingerprintHandlerListener {
    void onAuthenticationError(String err);
    void onAuthenticationFailed();
    void onAuthenticationHelp(String help);
    void onAuthenticationSucceeded();
  }

  public FingerprintHandler(Context context, FingerprintHandlerListener listener) {
    mContext = context;
    mListener = listener;
  }

  private void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {
    CancellationSignal cancellationSignal = new CancellationSignal();
    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
      return;
    }
    manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
  }

  public boolean loadAuthentication(String key) {

    mFingerprintManager = (FingerprintManager) mContext.getSystemService(Context.FINGERPRINT_SERVICE);
    mKeyguardManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
    boolean useFingerprint = false;
    if (isFullGranted()) {
      try {
        generateKey(key);
      } catch (Exception e) {
        e.printStackTrace();
      }

      if (initCipher(key)) {
        FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(mCipher);
        startAuth(mFingerprintManager, cryptoObject);
        useFingerprint = true;
      }
    }
    return useFingerprint;
  }


  private boolean isFingerprintAuthAvailable() {
    // The line below prevents the false positive inspection from Android Studio
    // noinspection ResourceType
    return mFingerprintManager.isHardwareDetected() && mFingerprintManager.hasEnrolledFingerprints();
  }

  private boolean isFullGranted() {
    return isFingerprintAuthAvailable() && mKeyguardManager.isKeyguardSecure() &&
      ActivityCompat.checkSelfPermission(mContext, Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED;
  }

  @Override
  public void onAuthenticationError(int errMsgId, CharSequence errString) {
    mListener.onAuthenticationError(errString.toString());
  }

  @Override
  public void onAuthenticationFailed() {
    mListener.onAuthenticationFailed();
  }

  @Override
  public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
    mListener.onAuthenticationHelp(helpString.toString());
  }

  @Override
  public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
    mListener.onAuthenticationSucceeded();
  }

  //Create the generateKey method that we’ll use to gain access to the Android keystore and generate the encryption key//

  private void generateKey(String userKey) throws Exception {
    try {
      // Obtain a reference to the Keystore using the standard Android keystore container identifier (“AndroidKeystore”)//
      mKeyStore = KeyStore.getInstance("AndroidKeyStore");

      //Generate the key//
      KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

      //Initialize an empty KeyStore//
      mKeyStore.load(null);

      //Initialize the KeyGenerator//
      keyGenerator.init(new

        //Specify the operation(s) this key can be used for//
        KeyGenParameterSpec.Builder(userKey,
        KeyProperties.PURPOSE_ENCRYPT |
          KeyProperties.PURPOSE_DECRYPT)
        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)

        //Configure this key so that the user has to confirm their identity with a fingerprint each time they want to use it//
        .setUserAuthenticationRequired(true)
        .setEncryptionPaddings(
          KeyProperties.ENCRYPTION_PADDING_PKCS7)
        .build());

      //Generate the key//
      keyGenerator.generateKey();

    } catch (KeyStoreException
      | NoSuchAlgorithmException
      | NoSuchProviderException
      | InvalidAlgorithmParameterException
      | CertificateException
      | IOException exc) {
      throw new Exception(exc);
    }
  }

  //Create a new method that we’ll use to initialize our cipher//
  private boolean initCipher(String userKey) {
    try {
      //Obtain a cipher instance and configure it with the properties required for fingerprint authentication//
      mCipher = Cipher.getInstance(
        KeyProperties.KEY_ALGORITHM_AES + "/"
          + KeyProperties.BLOCK_MODE_CBC + "/"
          + KeyProperties.ENCRYPTION_PADDING_PKCS7);
    } catch (NoSuchAlgorithmException |
      NoSuchPaddingException e) {
      throw new RuntimeException("Failed to get Cipher", e);
    }

    try {
      mKeyStore.load(null);
      SecretKey key = (SecretKey) mKeyStore.getKey(userKey,
        null);
      mCipher.init(Cipher.ENCRYPT_MODE, key);
      //Return true if the cipher has been initialized successfully//
      return true;
    } catch (KeyPermanentlyInvalidatedException e) {

      //Return false if cipher initialization failed//
      return false;
    } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
      throw new RuntimeException("Failed to init Cipher", e);
    }
  }
}
