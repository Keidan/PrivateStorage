package fr.ralala.privatestorage;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

import fr.ralala.privatestorage.sql.SqlFactory;
import fr.ralala.privatestorage.ui.SettingsActivity;
import fr.ralala.privatestorage.utils.BlowfishCipher;

/**
 *******************************************************************************
 * <p><b>Project PrivateStorage</b><br/>
 * Application context
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class PrivateStorageApp extends Application implements Application.ActivityLifecycleCallbacks {

  private static final String TOKEN_KEY = "token";
  private SharedPreferences m_sharedPref;
  private SqlFactory sql;
  private boolean installed = false;
  private BlowfishCipher blowfishCipher = null;
  private Class<?> from = null;
  private int numStarted = 0;
  private boolean isInBackground = false;
  private String currentName = null;
  public enum ParanoiacMode {
    BOTH,
    BACKGROUND,
    SCREEN_OFF;

    public static ParanoiacMode fromString(Context c, String s) {
      String array[] = c.getResources().getStringArray(R.array.values_security_mode);
      return s.equals(array[1]) ? BACKGROUND : s.equals(array[2]) ? SCREEN_OFF : BOTH;
    }
  }

  public Throwable install() {
    if(installed) return null;
    try {
      blowfishCipher = new BlowfishCipher();
      sql = new SqlFactory(blowfishCipher, this);
      m_sharedPref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
      installed = true;
      registerActivityLifecycleCallbacks(this);
      return null;
    } catch (final Exception e) {
      Log.e(getClass().getSimpleName(), "Error: " + e.getMessage(), e);
      return e;
    }
  }

  public BlowfishCipher getBlowfishCipher() {
    return blowfishCipher;
  }

  public SqlFactory getSql() {
    return sql;
  }

  public ParanoiacMode getParanoiacMode() {
    return ParanoiacMode.fromString(this, m_sharedPref.getString(
      SettingsActivity.PREFS_KEY_SECURITY_PARANOIAC,
      getResources().getStringArray(R.array.values_security_mode)[0]));
  }

  public void setParanoiacMode(String mode) {
    SharedPreferences.Editor e = m_sharedPref.edit();
    e.putString(SettingsActivity.PREFS_KEY_SECURITY_PARANOIAC, mode);
    e.apply();
  }

  public boolean isValidToken() {
    return m_sharedPref.contains(TOKEN_KEY);
  }

  public boolean isValidToken(String s) {
    try {
      return !s.isEmpty() && m_sharedPref.contains(TOKEN_KEY) && m_sharedPref.getString(TOKEN_KEY, "").equals(
        blowfishCipher.encrypt(getString(R.string.blowfish_cipher_key), s));
    } catch(Exception e) {
      Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
      return false;
    }
  }

  public String getToken() {
    return m_sharedPref.getString(TOKEN_KEY, "");
  }

  /**
   * Changes the token.
   * @param s Null to delete.
   */
  public void setToken(String s) {
    SharedPreferences.Editor ed = m_sharedPref.edit();
    if(s == null || s.isEmpty())
      ed.remove(TOKEN_KEY);
    else {
      try {
        ed.putString(TOKEN_KEY, blowfishCipher.encrypt(getString(R.string.blowfish_cipher_key), s));
      } catch(Exception e) {
        Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
        ed.remove(TOKEN_KEY);
      }
    }
    ed.apply();
  }

  public Class<?> getFrom() {
    return from;
  }

  public void setFrom(Class<?> from) {
    this.from = from;
  }

  public boolean isScreenOn() {
    // If the screen is off then the device has been locked
    // approach from https://stackoverflow.com/questions/1588061/android-how-to-receive-broadcast-intents-action-screen-on-off
    PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
    boolean on;
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
      on = powerManager.isInteractive();
    } else {
      on = powerManager.isScreenOn();
    }
    return on;
  }

  public boolean isInBackground() {
    return isInBackground;
  }

  public String getCurrentName() {
    return currentName;
  }

  public void setCurrentName(String currentName) {
    this.currentName = currentName;
  }

  /******* IMPL ********/

  @Override
  public void onActivityCreated(Activity activity, Bundle bundle) {
  }

  @Override
  public void onActivityStarted(Activity activity) {
    if (numStarted == 0) {
      isInBackground = false;
      Log.e(getClass().getSimpleName(), "app went to foreground");
    }
    numStarted++;
  }

  @Override
  public void onActivityResumed(Activity activity) {
  }

  @Override
  public void onActivityPaused(Activity activity) {
  }

  @Override
  public void onActivityStopped(Activity activity) {
    numStarted--;
    if (numStarted == 0) {
      isInBackground = true;
      Log.e(getClass().getSimpleName(), "app went to background");
    }
  }

  @Override
  public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
  }

  @Override
  public void onActivityDestroyed(Activity activity) {
  }
}
