package fr.ralala.privatestorage.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.MenuItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.ralala.privatestorage.R;
import fr.ralala.privatestorage.PrivateStorageApp;
import fr.ralala.privatestorage.dropbox.DropboxImportExport;
import fr.ralala.privatestorage.ui.activities.login.LoginActivity;
import fr.ralala.privatestorage.utils.Sys;
import fr.ralala.privatestorage.ui.utils.UI;


import fr.ralala.privatestorage.ui.activities.fchooser.AbstractFileChooserActivity;
import fr.ralala.privatestorage.ui.activities.fchooser.FileChooserActivity;

/**
 *******************************************************************************
 * <p><b>Project PrivateStorage</b><br/>
 * Management of the db import/export
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener {
  public static final String PREFS_KEY_SECURITY_PARANOIAC = "prefSecurityParanoList";
  public static final String PREFS_KEY_EXPORT_TO_DEVICE = "prefExportToDevice";
  public static final String PREFS_KEY_IMPORT_FROM_DEVICE = "prefImportFromDevice";
  public static final String PREFS_KEY_EXPORT_TO_DROPBOX = "prefExportToDropbox";
  public static final String PREFS_KEY_IMPORT_FROM_DROPBOX = "prefImportFromDropbox";

  private MyPreferenceFragment mPrefFrag = null;
  private PrivateStorageApp mApp = null;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mApp = (PrivateStorageApp)getApplicationContext();
    mPrefFrag = new MyPreferenceFragment();
    getFragmentManager().beginTransaction()
      .replace(android.R.id.content, mPrefFrag).commit();
    getFragmentManager().executePendingTransactions();
    android.support.v7.app.ActionBar actionBar = AppCompatDelegate.create(this, null).getSupportActionBar();
    if(actionBar != null) {
      actionBar.setDisplayShowHomeEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
    mPrefFrag.findPreference(PREFS_KEY_EXPORT_TO_DEVICE).setOnPreferenceClickListener(this);
    mPrefFrag.findPreference(PREFS_KEY_IMPORT_FROM_DEVICE).setOnPreferenceClickListener(this);
    mPrefFrag.findPreference(PREFS_KEY_EXPORT_TO_DROPBOX).setOnPreferenceClickListener(this);
    mPrefFrag.findPreference(PREFS_KEY_IMPORT_FROM_DROPBOX).setOnPreferenceClickListener(this);
    mPrefFrag.findPreference(PREFS_KEY_SECURITY_PARANOIAC).setOnPreferenceChangeListener((preference, newValue) -> {
      ((PrivateStorageApp)getApplicationContext()).setParanoiacMode(""+newValue);
      return true;
    });
    ((PrivateStorageApp)getApplicationContext()).setFrom(NamesActivity.class);
  }

  @Override
  public void onPause() {
    super.onPause();
    PrivateStorageApp app = (PrivateStorageApp)getApplicationContext();
    PrivateStorageApp.ParanoiacMode pm = app.getParanoiacMode();
    if(((pm == PrivateStorageApp.ParanoiacMode.BOTH || pm == PrivateStorageApp.ParanoiacMode.SCREEN_OFF) && !app.isScreenOn()) ||
      ((pm == PrivateStorageApp.ParanoiacMode.BOTH || pm == PrivateStorageApp.ParanoiacMode.BACKGROUND) && app.isInBackground())) {
      finish();
      Sys.switchTo(this, LoginActivity.class, true);
    }
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    /* Workaround caused by onPause behaviour */
    //Sys.switchTo(this, NamesActivity.class, true);
    finish();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }


  @Override
  public boolean onPreferenceClick(final Preference preference) {
    if (preference.equals(mPrefFrag.findPreference(PREFS_KEY_EXPORT_TO_DROPBOX))) {
      mApp.setLastExportType(PrivateStorageApp.PREFS_VAL_LAST_EXPORT_DROPBOX);
      Sys.exportDropbox(mApp, this);
    } else if (preference.equals(mPrefFrag.findPreference(PREFS_KEY_IMPORT_FROM_DROPBOX))) {
      mApp.getDropboxImportExport().importDatabase(this);
    } else if (preference.equals(mPrefFrag.findPreference(PREFS_KEY_EXPORT_TO_DEVICE))) {
      Map<String, String> extra = new HashMap<>();
      extra.put(AbstractFileChooserActivity.FILECHOOSER_TYPE_KEY, "" + AbstractFileChooserActivity.FILECHOOSER_TYPE_DIRECTORY_ONLY);
      extra.put(AbstractFileChooserActivity.FILECHOOSER_TITLE_KEY, getString(R.string.pref_title_export));
      extra.put(AbstractFileChooserActivity.FILECHOOSER_MESSAGE_KEY, getString(R.string.use_folder) + ":? ");
      extra.put(AbstractFileChooserActivity.FILECHOOSER_DEFAULT_DIR, Environment.getExternalStorageDirectory().getAbsolutePath());
      extra.put(AbstractFileChooserActivity.FILECHOOSER_SHOW_KEY, "" + AbstractFileChooserActivity.FILECHOOSER_SHOW_DIRECTORY_ONLY);
      startChooserActivity(extra, FileChooserActivity.FILECHOOSER_SELECTION_TYPE_DIRECTORY);
    } else if (preference.equals(mPrefFrag.findPreference(PREFS_KEY_IMPORT_FROM_DEVICE))) {
      Map<String, String> extra = new HashMap<>();
      extra.put(AbstractFileChooserActivity.FILECHOOSER_TYPE_KEY, "" + AbstractFileChooserActivity.FILECHOOSER_TYPE_FILE_AND_DIRECTORY);
      extra.put(AbstractFileChooserActivity.FILECHOOSER_TITLE_KEY, getString(R.string.pref_title_import));
      extra.put(AbstractFileChooserActivity.FILECHOOSER_MESSAGE_KEY, getString(R.string.use_file) + ":? ");
      extra.put(AbstractFileChooserActivity.FILECHOOSER_DEFAULT_DIR, Environment.getExternalStorageDirectory().getAbsolutePath());
      extra.put(AbstractFileChooserActivity.FILECHOOSER_SHOW_KEY, "" + AbstractFileChooserActivity.FILECHOOSER_SHOW_FILE_AND_DIRECTORY);
      startChooserActivity(extra, FileChooserActivity.FILECHOOSER_SELECTION_TYPE_FILE);
    }
    return true;
  }

  /**
   * Called when the file chooser is disposed with a result.
   * @param requestCode The request code.
   * @param resultCode The result code.
   * @param data The Intent data.
   */
  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    // Check which request we're responding to
    if (!Sys.exportDeviceActivityResult(this, requestCode, resultCode, data) && requestCode == FileChooserActivity.FILECHOOSER_SELECTION_TYPE_FILE) {
      if (resultCode == RESULT_OK) {
        String file = data.getStringExtra(FileChooserActivity.FILECHOOSER_SELECTION_KEY);
        Log.d(getClass().getSimpleName(), "Selected file: '" + file + "'");
        List<File> files = new ArrayList<>();
        for(File f : new File(file).listFiles()) {
          if(f.getName().endsWith(".sqlite3"))
            files.add(f);
        }
        files.sort(Comparator.comparing(File::lastModified));
        DropboxImportExport.computeAndLoad(this, files, new DropboxImportExport.AlertDialogListListener<String>() {
          @Override
          public void onClick(String s) {
            try {
              DropboxImportExport.loadDb(SettingsActivity.this, new File(s));
            } catch (Exception e) {
              UI.toastLong(SettingsActivity.this, getString(R.string.error) + ": " + e.getMessage());
              Log.e(getClass().getSimpleName(), "Error: " + e.getMessage(), e);
            }
          }
        });
      }
    }
  }

  private void startChooserActivity(Map<String, String> extra, int code) {
    final Intent i = new Intent(getApplicationContext(), FileChooserActivity.class);
    if(extra != null) {
      Set<String> keysSet = extra.keySet();
      for (String key : keysSet) {
        i.putExtra(key, extra.get(key));
      }
    }
    startActivityForResult(i, code);
  }

  public static class MyPreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setRetainInstance(true);
      addPreferencesFromResource(R.xml.preferences);
    }
  }
}
