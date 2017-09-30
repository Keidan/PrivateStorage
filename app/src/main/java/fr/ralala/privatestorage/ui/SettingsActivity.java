package fr.ralala.privatestorage.ui;

import android.content.Context;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.ralala.privatestorage.R;
import fr.ralala.privatestorage.PrivateStorageApp;
import fr.ralala.privatestorage.ui.filechooser.AbstractFileChooserActivity;
import fr.ralala.privatestorage.ui.filechooser.FileChooserActivity;
import fr.ralala.privatestorage.sql.SqlHelper;
import fr.ralala.privatestorage.ui.login.LoginActivity;
import fr.ralala.privatestorage.utils.Sys;
import fr.ralala.privatestorage.ui.utils.UI;


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
  public static final String       PREFS_KEY_SECURITY_PARANOIAC               = "prefSecurityParanoList";
  public static final String       PREFS_KEY_EXPORT_TO_DEVICE                 = "prefExportToDevice";
  public static final String       PREFS_KEY_IMPORT_FROM_DEVICE               = "prefImportFromDevice";

  private MyPreferenceFragment prefFrag                       = null;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    prefFrag = new MyPreferenceFragment();
    getFragmentManager().beginTransaction()
      .replace(android.R.id.content, prefFrag).commit();
    getFragmentManager().executePendingTransactions();
    android.support.v7.app.ActionBar actionBar = AppCompatDelegate.create(this, null).getSupportActionBar();
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setDisplayHomeAsUpEnabled(true);

    prefFrag.findPreference(PREFS_KEY_EXPORT_TO_DEVICE).setOnPreferenceClickListener(this);
    prefFrag.findPreference(PREFS_KEY_IMPORT_FROM_DEVICE).setOnPreferenceClickListener(this);
    prefFrag.findPreference(PREFS_KEY_SECURITY_PARANOIAC).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        ((PrivateStorageApp)getApplicationContext()).setParanoiacMode(""+newValue);
        return true;
      }
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
    switch (item.getItemId())
    {
      case android.R.id.home:
        onBackPressed();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }


  @Override
  public boolean onPreferenceClick(final Preference preference) {
    if (preference.equals(prefFrag.findPreference(PREFS_KEY_EXPORT_TO_DEVICE))) {
      Map<String, String> extra = new HashMap<>();
      extra.put(AbstractFileChooserActivity.FILECHOOSER_TYPE_KEY, "" + AbstractFileChooserActivity.FILECHOOSER_TYPE_DIRECTORY_ONLY);
      extra.put(AbstractFileChooserActivity.FILECHOOSER_TITLE_KEY, getString(R.string.pref_title_export));
      extra.put(AbstractFileChooserActivity.FILECHOOSER_MESSAGE_KEY, getString(R.string.use_folder) + ":? ");
      extra.put(AbstractFileChooserActivity.FILECHOOSER_DEFAULT_DIR, Environment
        .getExternalStorageDirectory().getAbsolutePath());
      extra.put(AbstractFileChooserActivity.FILECHOOSER_SHOW_KEY, "" + AbstractFileChooserActivity.FILECHOOSER_SHOW_DIRECTORY_ONLY);
      myStartActivity(extra, FileChooserActivity.class, FileChooserActivity.FILECHOOSER_SELECTION_TYPE_DIRECTORY);
    } else if (preference.equals(prefFrag.findPreference(PREFS_KEY_IMPORT_FROM_DEVICE))) {
      Map<String, String> extra = new HashMap<>();
      extra.put(AbstractFileChooserActivity.FILECHOOSER_TYPE_KEY, "" + AbstractFileChooserActivity.FILECHOOSER_TYPE_FILE_AND_DIRECTORY);
      extra.put(AbstractFileChooserActivity.FILECHOOSER_TITLE_KEY, getString(R.string.pref_title_import));
      extra.put(AbstractFileChooserActivity.FILECHOOSER_MESSAGE_KEY, getString(R.string.use_file) + ":? ");
      extra.put(AbstractFileChooserActivity.FILECHOOSER_DEFAULT_DIR, Environment
        .getExternalStorageDirectory().getAbsolutePath());
      extra.put(AbstractFileChooserActivity.FILECHOOSER_SHOW_KEY, "" + AbstractFileChooserActivity.FILECHOOSER_SHOW_FILE_AND_DIRECTORY);
      myStartActivity(extra, FileChooserActivity.class, FileChooserActivity.FILECHOOSER_SELECTION_TYPE_FILE);
    }
    return true;
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    // Check which request we're responding to
    if (requestCode == FileChooserActivity.FILECHOOSER_SELECTION_TYPE_DIRECTORY) {
      if (resultCode == RESULT_OK) {
        String dir = data.getStringExtra(FileChooserActivity.FILECHOOSER_SELECTION_KEY);
        try {
          SqlHelper.copyDatabase(this, SqlHelper.DB_NAME, dir);
          UI.toast(this, getString(R.string.export_success));
        } catch(Exception e) {
          UI.toast_long(this, getString(R.string.error) + ": " + e.getMessage());
          Log.e(getClass().getSimpleName(), "Error: " + e.getMessage(), e);
        }
      }
    } else if (requestCode == FileChooserActivity.FILECHOOSER_SELECTION_TYPE_FILE) {
      if (resultCode == RESULT_OK) {
        String file = data.getStringExtra(FileChooserActivity.FILECHOOSER_SELECTION_KEY);
        Log.d(getClass().getSimpleName(), "Selected file: '" + file + "'");
        List<File> files = new ArrayList<>();
        for(File f : new File(file).listFiles()) {
          if(f.getName().endsWith(".sqlite3"))
            files.add(f);
        }
        Collections.sort(files, new Comparator<File>() {
          @Override
          public int compare(File f1, File f2) {
            return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified()) ;
          }
        });

        computeAndLoad(this, files, new UI.AlertDialogListListener<String>() {
          @Override
          public void onClick(String s) {
            try {
              Context c = SettingsActivity.this;
              SqlHelper.loadDatabase(c, SqlHelper.DB_NAME, new File(s));
              UI.toast(c, c.getString(R.string.import_success));
              Sys.restartApplication(c, -1);
            } catch (Exception e) {
              UI.toast_long(SettingsActivity.this, getString(R.string.error) + ": " + e.getMessage());
              Log.e(getClass().getSimpleName(), "Error: " + e.getMessage(), e);
            }
          }
        });
      }
    }
  }

  public static void computeAndLoad(final Context c, final List<File> list, UI.AlertDialogListListener<String> yes) {
    List<String> files = compute(list);
    if(files.isEmpty())
      UI.toast_long(c, c.getString(R.string.error_no_files));
    else {
      UI.showAlertDialog(c, R.string.box_select_db_file, files, yes);
    }
  }

  private static List<String> compute(final List<File> list) {
    List<String> files = new ArrayList<>();
    for(File f : list) {
      if (f.getName().endsWith(".sqlite3"))
        files.add(f.getAbsolutePath());
    }
    return files;
  }

  private void myStartActivity(Map<String, String> extra, Class<?> c, int code) {
    final Intent i = new Intent(getApplicationContext(), c);
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
