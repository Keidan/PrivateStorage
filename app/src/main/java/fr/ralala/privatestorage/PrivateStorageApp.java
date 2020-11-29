package fr.ralala.privatestorage;

import android.app.Application;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import fr.ralala.privatestorage.dropbox.DropboxImportExport;
import fr.ralala.privatestorage.sql.SqlFactory;
import fr.ralala.privatestorage.ui.activities.SettingsActivity;
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
public class PrivateStorageApp extends Application implements LifecycleObserver {

  private static final String PREFS_KEY_LAST_EXPORT = "pKeyLastExportType";
  public static final String PREFS_VAL_LAST_EXPORT_DROPBOX = "dropbox";
  public static final String PREFS_VAL_LAST_EXPORT_DEVICE = "device";
  public static final String PREFS_VAL_SWIPE_NAMES = "swipeNames";
  public static final String PREFS_VAL_SWIPE_ENTRIES = "swipeEntires";
  private static final String TOKEN_KEY = "token";
  private SharedPreferences mSharedPref;
  private SqlFactory mSql;
  private BlowfishCipher mBlowfishCipher = null;
  private Class<?> mFrom = null;
  private boolean mIsInBackground = false;
  private String mCurrentName = null;
  private DropboxImportExport mDropboxImportExport = null;

  public enum ParanoiacMode {
    BOTH,
    BACKGROUND,
    SCREEN_OFF;

    public static ParanoiacMode fromString(Context c, String s) {
      String[] array = c.getResources().getStringArray(R.array.values_security_mode);
      return s.equals(array[1]) ? BACKGROUND : s.equals(array[2]) ? SCREEN_OFF : BOTH;
    }
  }

  /**
   * Called by Android to create the application context.
   */
  @Override
  public void onCreate() {
    super.onCreate();
    mDropboxImportExport = new DropboxImportExport();
    String last = getLastExportType();
    if(last == null || last.isEmpty())
      setLastExportType(PREFS_VAL_LAST_EXPORT_DROPBOX);
    mBlowfishCipher = new BlowfishCipher();
    mSql = new SqlFactory(mBlowfishCipher, this);
    mSharedPref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
    ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
  }

  public SqlFactory getSql() {
    return mSql;
  }

  public ParanoiacMode getParanoiacMode() {
    return ParanoiacMode.fromString(this, mSharedPref.getString(
      SettingsActivity.PREFS_KEY_SECURITY_PARANOIAC,
      getResources().getStringArray(R.array.values_security_mode)[0]));
  }

  public void setParanoiacMode(String mode) {
    SharedPreferences.Editor e = mSharedPref.edit();
    e.putString(SettingsActivity.PREFS_KEY_SECURITY_PARANOIAC, mode);
    e.apply();
  }

  public boolean isValidToken() {
    return mSharedPref.contains(TOKEN_KEY);
  }

  public boolean isValidToken(String s) {
    try {
      return !s.isEmpty() && mSharedPref.contains(TOKEN_KEY) && mSharedPref.getString(TOKEN_KEY, "").equals(
          mBlowfishCipher.encrypt(getString(R.string.blowfish_cipher_key), s));
    } catch(Exception e) {
      Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
      return false;
    }
  }

  public String getToken() {
    return mSharedPref.getString(TOKEN_KEY, "");
  }

  /**
   * Changes the token.
   * @param s Null to delete.
   */
  public void setToken(String s) {
    SharedPreferences.Editor ed = mSharedPref.edit();
    if(s == null || s.isEmpty())
      ed.remove(TOKEN_KEY);
    else {
      try {
        ed.putString(TOKEN_KEY, mBlowfishCipher.encrypt(getString(R.string.blowfish_cipher_key), s));
      } catch(Exception e) {
        Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
        ed.remove(TOKEN_KEY);
      }
    }
    ed.apply();
  }

  public Class<?> getFrom() {
    return mFrom;
  }

  public void setFrom(Class<?> from) {
    mFrom = from;
  }

  public boolean isScreenOff() {
    // If the screen is off then the device has been locked
    // approach from https://stackoverflow.com/questions/1588061/android-how-to-receive-broadcast-intents-action-screen-on-off
    PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
    return !powerManager.isInteractive();
  }

  public boolean isInBackground() {
    return mIsInBackground;
  }

  public String getCurrentName() {
    return mCurrentName;
  }

  public void setCurrentName(String currentName) {
    mCurrentName = currentName;
  }

  /**
   * Returns the DropboxImportExport object.
   * @return DropboxImportExport
   */
  public DropboxImportExport getDropboxImportExport() {
    return mDropboxImportExport;
  }

  public void leaveFromBackground() {
    mIsInBackground = false;
  }

  /******* IMPL ********/
  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
  public void onAppBackgrounded() {
    //App in background
    mIsInBackground = true;
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_START)
  public void onAppForegrounded() {
    // App in foreground
  }

  /* ----------------------------------
   * Global configuration
   * ----------------------------------
   */

  /**
   * Tests if the toast used for the swipe information is displayed or not (names activity).
   * @return boolean
   */
  public boolean isSwipeNamesDisplayed() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return prefs.getBoolean(PREFS_VAL_SWIPE_NAMES, false);
  }

  /**
   * Tests if the toast used for the swipe information is displayed or not (entries activity).
   * @return boolean
   */
  public boolean isSwipeEntriesDisplayed() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return prefs.getBoolean(PREFS_VAL_SWIPE_ENTRIES, false);
  }
  /**
   * Changes the display value of the toast used for the swipe information (names activity).
   */
  public void swipeNamesDisplayed() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    SharedPreferences.Editor e = prefs.edit();
    e.putBoolean(PREFS_VAL_SWIPE_NAMES, true);
    e.apply();
  }

  /**
   * Changes the display value of the toast used for the swipe information (entries activity).
   */
  public void swipeEntriesDisplayed() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    SharedPreferences.Editor e = prefs.edit();
    e.putBoolean(PREFS_VAL_SWIPE_ENTRIES, true);
    e.apply();
  }

  /**
   * Returns the last export mode used.
   * Will change the behavior of the export icon
   * @return String (see PREF_VAL_LAST_EXPORT_xxxxxxx)
   */
  public String getLastExportType() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return prefs.getString(PREFS_KEY_LAST_EXPORT, PREFS_VAL_LAST_EXPORT_DROPBOX);
  }

  /**
   * Sets the last export mode used.
   * Will change the behavior of the export icon
   * @param last see PREF_VAL_LAST_EXPORT_xxxxxxx
   */
  public void setLastExportType(String last) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    SharedPreferences.Editor e = prefs.edit();
    e.putString(PREFS_KEY_LAST_EXPORT, last);
    e.apply();
  }
}
