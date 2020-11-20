package fr.ralala.privatestorage.ui.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fr.ralala.privatestorage.R;
import fr.ralala.privatestorage.PrivateStorageApp;
import fr.ralala.privatestorage.items.SpinnerIconItem;
import fr.ralala.privatestorage.items.SqlItem;
import fr.ralala.privatestorage.items.SqlNameItem;
import fr.ralala.privatestorage.sql.SqlFactory;
import fr.ralala.privatestorage.ui.adapters.SpinnerIconArrayAdapter;
import fr.ralala.privatestorage.ui.adapters.SqlEntriesArrayAdapter;
import fr.ralala.privatestorage.ui.adapters.SqlItemArrayAdapter;
import fr.ralala.privatestorage.items.SqlEntryItem;
import fr.ralala.privatestorage.ui.activities.login.LoginActivity;
import fr.ralala.privatestorage.utils.Sys;
import fr.ralala.privatestorage.ui.utils.UI;

/**
 *******************************************************************************
 * <p><b>Project PrivateStorage</b><br/>
 * Form item.
 * </p>
 *
 * @author Keidan
 *
 *******************************************************************************
 */
public class EntriesActivity extends AppCompatActivity implements SqlItemArrayAdapter.SqlItemArrayAdapterMenuListener, AdapterView.OnItemLongClickListener {
  private static final int REQ_ID_ADD = 0;
  private static final int REQ_ID_EDIT = 1;

  private SqlFactory mSql = null;
  private SqlEntriesArrayAdapter mAdapter = null;
  private SqlEntryItem mCurrentItem = null;
  private SqlNameItem mOwner = null;
  private EditText mSearchET = null;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_entries);
    PrivateStorageApp app = (PrivateStorageApp)getApplication();
    mSql = app.getSql();

    try {
      mOwner = mSql.getName(app.getCurrentName());
    } catch(Exception e) {
      Log.e(getClass().getSimpleName(), "SQL: " + e.getMessage(), e);
      UI.showAlertDialog(this, R.string.error, "SQL: " + e.getMessage());
      return;
    }

    TextView tv = findViewById(R.id.name);
    tv.setText(mOwner.getKey());
    ListView list = findViewById(R.id.content_form);
    list.setOnItemLongClickListener(this);
    try {
      mAdapter = new SqlEntriesArrayAdapter(this,
        R.layout.menu_list_item_3, mSql.getEntries(mOwner), this, R.menu.popup_listview_form);
      list.setAdapter(mAdapter);
    } catch(Exception e) {
      Log.e(getClass().getSimpleName(), "SQL: " + e.getMessage(), e);
      UI.showAlertDialog(this, R.string.error, "SQL: " + e.getMessage());
    }


    FloatingActionButton fab = findViewById(R.id.fab);
    fab.setOnClickListener((view) -> {
      mCurrentItem = null;
      showInputDialog(R.string.new_entry_title, R.string.new_entry_hint_key, REQ_ID_ADD);
    });
    ((PrivateStorageApp)getApplicationContext()).setFrom(this.getClass());

    android.support.v7.app.ActionBar actionBar = getDelegate().getSupportActionBar();
    // add the custom view to the action bar
    if(actionBar != null) {
      actionBar.setCustomView(R.layout.actionbar_view_entries);
      final ImageView iview = actionBar.getCustomView().findViewById(R.id.view);
      iview.setImageResource(mOwner.getType() == SqlNameItem.Type.DISPLAY ? R.mipmap.ic_menu_view : R.mipmap.ic_menu_unview);
      mAdapter.setViewAll(mOwner.getType() == SqlNameItem.Type.DISPLAY);
      iview.setOnClickListener((view) -> {
        if(mAdapter.isViewAll()) {
          iview.setImageResource(R.mipmap.ic_menu_unview);
        } else {
          iview.setImageResource(R.mipmap.ic_menu_view);
        }
        mAdapter.setViewAll(!mAdapter.isViewAll());
      });
      ImageView iv = actionBar.getCustomView().findViewById(R.id.icon);
      iv.setOnClickListener((v) -> onBackPressed());
      mSearchET = actionBar.getCustomView().findViewById(R.id.searchET);
      Runnable r = () -> {
        // When user changed the Text
        final String text = mSearchET.getText().toString()
            .toLowerCase(Locale.getDefault());
        mAdapter.filter(text);
        if(text.isEmpty() && !mAdapter.isViewAll())
          mAdapter.setViewAll(mAdapter.isViewAll()); /* force refresh */
      };
      mSearchET.addTextChangedListener(new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
         r.run();
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        @Override
        public void afterTextChanged(Editable arg0) {
        }
      });
      mSearchET.setOnEditorActionListener((v, actionId, event) -> {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
          InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
          if(in != null)
            in.hideSoftInputFromWindow(v
                .getApplicationWindowToken(),
              InputMethodManager.HIDE_NOT_ALWAYS);
          r.run();
          return true;
        }
        return false;
      });
      actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
    }

  }

  @Override
  public void onResume() {
    mSearchET.setText("");
    super.onResume();
  }

  @Override
  public void onPause() {
    super.onPause();
    PrivateStorageApp app = (PrivateStorageApp)getApplicationContext();
    PrivateStorageApp.ParanoiacMode pm = app.getParanoiacMode();
    if(((pm == PrivateStorageApp.ParanoiacMode.BOTH || pm == PrivateStorageApp.ParanoiacMode.SCREEN_OFF) && app.isScreenOff()) ||
      ((pm == PrivateStorageApp.ParanoiacMode.BOTH || pm == PrivateStorageApp.ParanoiacMode.BACKGROUND) && app.isInBackground())) {
      finish();
      Sys.switchTo(this, LoginActivity.class, true);
    }
  }

  @Override
  public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
    SqlEntryItem item = (SqlEntryItem)mAdapter.getItem(position);
    if(item != null) {
      Vibrator vibrator = ((Vibrator) getSystemService(VIBRATOR_SERVICE));
      switch (item.getType()) {
        case EMAIL:
          if(vibrator != null)
            vibrator.vibrate(50);
          Sys.openMail(this, item.getValue());
          break;
        case PHONE:
          if(vibrator != null)
            vibrator.vibrate(50);
          Sys.openDialer(this, item.getValue());
          break;
        case PASSWORD: {
          if (vibrator != null)
            vibrator.vibrate(50);
          TextView tv = view.findViewById(R.id.value);
          mAdapter.setValueVisible(tv.getText().toString().equals(item.getValue()) ? -1 : position);
          break;
        }
        case LOGIN: {
          if (vibrator != null)
            vibrator.vibrate(50);
          TextView tv = view.findViewById(R.id.value);
          String [] split = tv.getText().toString().split("\n");
          String val = "";
          if(split[0].startsWith(getString(R.string.login) + ": "))
            val += split[0].substring((getString(R.string.login) + ": ").length());
          if(split[1].startsWith(getString(R.string.password) + ": "))
            val += "\n" + split[1].substring((getString(R.string.password) + ": ").length());
          mAdapter.setValueVisible(val.equals(item.getValue()) ? -1 : position);
          break;
        }
        case COMPOSE:
        case TEXT:
          if(vibrator != null)
            vibrator.vibrate(50);
          Sys.clipboard(this, item.getValue());
          UI.toast(this, R.string.text_copied);
          break;
        case URL:
          if(vibrator != null)
            vibrator.vibrate(50);
          Sys.openBrowser(this, item.getValue());
          break;
      }
    }
    return true;
  }

  public boolean validateInputText(int reqId, int type, String text1, String text2, String text3) {
    if(!text1.isEmpty() && !text2.isEmpty()) {
      String value = text2;
      if(text3 != null && !text3.isEmpty())
        value += "\n" + text3;
      SqlEntryItem sti = new SqlEntryItem(mCurrentItem == null ? 0 : mCurrentItem.getId(), SqlEntryItem.Type.fromInt(type + 1), text1, value);

      switch (sti.getType()) {
        case EMAIL:
          if(!Sys.validateEmail(sti.getValue())) {
            UI.showAlertDialog(this, R.string.error, R.string.error_invalid_email);
            return false;
          }
          break;
        case URL:
          if(!Sys.validateURL(sti.getValue())) {
            UI.showAlertDialog(this, R.string.error, R.string.error_invalid_url);
            return false;
          }
          break;
      }
      try {
        if(reqId == REQ_ID_EDIT) {
          mAdapter.remove(mCurrentItem);
          mCurrentItem.set(sti);
          mSql.updateEntry(mCurrentItem);
          sti = mCurrentItem;
        } else {
          if(mAdapter.contains(sti)) {
            UI.toast(this, R.string.already_inserted);
            return false;
          }
          mSql.addEntry(mOwner, sti);
        }
        mAdapter.add(sti);
      } catch(Exception e) {
        Log.e(getClass().getSimpleName(), "SQL: " + e.getMessage(), e);
        UI.showAlertDialog(this, R.string.error, "SQL: " + e.getMessage());
        finish();
        return false;
      }
      return true;
    }
    return false;
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    Sys.switchTo(this, NamesActivity.class, true);
    finish();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == android.R.id.home) {
      onBackPressed();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }


  public void onMenuEdit(SqlItem t) {
    mCurrentItem = (SqlEntryItem)t;
    String [] split = t.getValue().split("\n");
    showInputDialog(R.string.new_entry_title, R.string.new_entry_hint_key, mCurrentItem.getType(),
        t.getKey(), split[0], split.length == 2 ? split[1] : null, REQ_ID_EDIT);
  }

  public void onMenuDelete(final SqlItem t) {
    UI.showConfirmDialog(this, R.string.confirm_delete_name_title, R.string.confirm_delete_name_message,
      new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          try {
            mAdapter.remove(t);
            mSql.deleteEntry(mOwner, (SqlEntryItem)t);
          } catch(Exception e) {
            Log.e(getClass().getSimpleName(), "SQL: " + e.getMessage(), e);
            UI.showAlertDialog(EntriesActivity.this, R.string.error, "SQL: " + e.getMessage());
          }
        }
    }, null);
  }


  public void showInputDialog(final int title, final int hint, final int reqId) {
    showInputDialog(title, hint, SqlEntryItem.Type.TEXT, null, null, null, reqId);
  }

  public void showInputDialog(final int title, final int hint, final SqlEntryItem.Type type, final String def1, final String def2, final String def3, final int reqId) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(title);
    View alertLayout = getLayoutInflater().inflate(R.layout.entry_input_dialog, null);
    builder.setView(alertLayout);
    final TextInputEditText tietName = alertLayout.findViewById(R.id.tietName);
    final TextInputEditText tietValue1 = alertLayout.findViewById(R.id.tietValue1);
    final TextInputEditText tietValue2 = alertLayout.findViewById(R.id.tietValue2);
    final Spinner spinner = alertLayout.findViewById(R.id.spinner);
    final TextInputLayout tilName = alertLayout.findViewById(R.id.tilName);
    final TextInputLayout tilValue1 = alertLayout.findViewById(R.id.tilValue1);
    final TextInputLayout tilValue2 = alertLayout.findViewById(R.id.tilValue2);
    tietName.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
    if(hint != Integer.MAX_VALUE)
      tilName.setHint(getString(hint));
    if(def1 != null)
      tietName.setText(def1);
    if(def2 != null)
      tietValue1.setText(def2);
    if(def3 != null)
      tietValue2.setText(def3);
    List<SpinnerIconItem> list = new ArrayList<>();
    list.add(new SpinnerIconItem(R.mipmap.ic_mail, getString(R.string.email)));
    list.add(new SpinnerIconItem(R.mipmap.ic_url, getString(R.string.url)));
    list.add(new SpinnerIconItem(R.mipmap.ic_phone, getString(R.string.phone)));
    list.add(new SpinnerIconItem(R.mipmap.ic_copy, getString(R.string.text)));
    list.add(new SpinnerIconItem(R.mipmap.ic_compose, getString(R.string.compose)));
    list.add(new SpinnerIconItem(R.mipmap.ic_password, getString(R.string.password)));
    list.add(new SpinnerIconItem(R.mipmap.ic_login, getString(R.string.login)));
    final SpinnerIconArrayAdapter ladapter = new SpinnerIconArrayAdapter(this, list);
    spinner.setAdapter(ladapter);
    spinner.setSelection(SqlEntryItem.Type.toInt(type) - 1);
    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        SpinnerIconItem item = ladapter.getItem(i);
        if(item != null) {
          int icon = item.getIcon();
          if(icon == R.mipmap.ic_mail) {
            tietValue1.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
            tilValue1.setPasswordVisibilityToggleEnabled(false);
            tilValue2.setVisibility(View.GONE);
            tilValue1.setHint(getString(R.string.email));
          }
          else if(icon == R.mipmap.ic_phone) {
            tietValue1.setInputType(InputType.TYPE_CLASS_PHONE);
            tilValue1.setPasswordVisibilityToggleEnabled(false);
            tilValue2.setVisibility(View.GONE);
            tilValue1.setHint(getString(R.string.phone));
          }
          else if(icon == R.mipmap.ic_url) {
            tietValue1.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
            tilValue1.setPasswordVisibilityToggleEnabled(false);
            tilValue2.setVisibility(View.GONE);
            if (tietValue1.getText().toString().isEmpty())
              tietValue1.setText(R.string.text_start_https);
            tilValue1.setHint(getString(R.string.url));
          }
          else if(icon == R.mipmap.ic_compose) {
            tietValue1.setInputType(InputType.TYPE_CLASS_NUMBER);
            tilValue1.setPasswordVisibilityToggleEnabled(false);
            tilValue2.setVisibility(View.GONE);
            tilValue1.setHint(getString(R.string.compose));
          }
          else if(icon == R.mipmap.ic_password) {
            tietValue1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            tilValue1.setPasswordVisibilityToggleEnabled(true);
            tilValue1.setPasswordVisibilityToggleTintList(getColorStateList(R.color.textColor));
            tilValue2.setVisibility(View.GONE);
            tilValue1.setHint(getString(R.string.password));
          }
          else if(icon == R.mipmap.ic_login) {
            tietValue1.setInputType(InputType.TYPE_CLASS_TEXT);
            tietValue2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            tilValue1.setPasswordVisibilityToggleEnabled(false);
            tilValue2.setPasswordVisibilityToggleEnabled(true);
            tilValue2.setPasswordVisibilityToggleTintList(getColorStateList(R.color.textColor));
            tilValue2.setVisibility(View.VISIBLE);
            tilValue1.setHint(getString(R.string.login));
            tilValue2.setHint(getString(R.string.password));
          }
          else {
            tietValue1.setInputType(InputType.TYPE_CLASS_TEXT);
            tilValue1.setPasswordVisibilityToggleEnabled(false);
            tilValue2.setVisibility(View.GONE);
            tilValue1.setHint(getString(R.string.new_entry_hint_value));
          }
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {

      }
    });

    // Set up the buttons
    builder.setPositiveButton(getString(R.string.ok), null);
    builder.setNegativeButton(getString(R.string.cancel), null);
    final AlertDialog mAlertDialog = builder.create();
    mAlertDialog.setOnShowListener((dialog) -> {
      Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
      b.setOnClickListener((view) -> {
        if(validateInputText(reqId, spinner.getSelectedItemPosition(), tietName.getText().toString(), tietValue1.getText().toString(), tietValue2.getText().toString()))
          mAlertDialog.dismiss();
      });
    });
    mAlertDialog.setCanceledOnTouchOutside(false);
    mAlertDialog.show();
  }
}
