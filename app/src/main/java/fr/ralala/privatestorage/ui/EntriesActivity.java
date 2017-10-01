package fr.ralala.privatestorage.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ListViewCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import fr.ralala.privatestorage.ui.login.LoginActivity;
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
  protected final static String KEY_NAME = "EntriesActivity.KEY_NAME";

  private SqlFactory sql = null;
  private SqlItemArrayAdapter adapter = null;
  private SqlEntryItem currentItem = null;
  private SqlNameItem owner = null;
  private EditText searchET = null;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_entries);
    PrivateStorageApp app = (PrivateStorageApp)getApplication();
    Throwable t;
    if((t = app.install()) != null)
      UI.showAlertDialog(this, R.string.exception, t.getMessage());
    sql = app.getSql();

    try {
      owner = sql.getName(getIntent().getStringExtra(KEY_NAME));
    } catch(Exception e) {
      Log.e(getClass().getSimpleName(), "SQL: " + e.getMessage(), e);
      UI.showAlertDialog(this, R.string.error, "SQL: " + e.getMessage());
      return;
    }

    TextView tv = (TextView) findViewById(R.id.name);
    tv.setText(owner.getKey());
    ListViewCompat list = (ListViewCompat)findViewById(R.id.content_form);
    list.setOnItemLongClickListener(this);
    try {
      adapter = new SqlEntriesArrayAdapter(this,
        R.layout.menu_list_item_3, sql.getEntries(owner), this, R.menu.popup_listview_form);
      list.setAdapter(adapter);
    } catch(Exception e) {
      Log.e(getClass().getSimpleName(), "SQL: " + e.getMessage(), e);
      UI.showAlertDialog(this, R.string.error, "SQL: " + e.getMessage());
      return;
    }


    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        currentItem = null;
        showInputDialog3(R.string.new_entry_title, R.string.new_entry_hint_key, R.string.new_entry_hint_value, REQ_ID_ADD);
      }
    });
    ((PrivateStorageApp)getApplicationContext()).setFrom(this.getClass());

    int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());

    android.support.v7.app.ActionBar actionBar = getDelegate().getSupportActionBar();
    // add the custom view to the action bar
    if(actionBar != null) {
      actionBar.setCustomView(R.layout.actionbar_view_entries);
      final ImageView iview = ((ImageView) actionBar.getCustomView().findViewById(R.id.view));
      iview.setImageResource(owner.getType() == SqlNameItem.Type.DISPLAY ? R.mipmap.ic_menu_view : R.mipmap.ic_menu_unview);
      adapter.setViewAll(owner.getType() == SqlNameItem.Type.DISPLAY);
      iview.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          if(adapter.isViewAll()) {
            iview.setImageResource(R.mipmap.ic_menu_unview);
          } else {
            iview.setImageResource(R.mipmap.ic_menu_view);
          }
          adapter.setViewAll(!adapter.isViewAll());
        }
      });
      ImageView iv = ((ImageView) actionBar.getCustomView().findViewById(R.id.icon));
      iv.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          onBackPressed();
        }
      });
      searchET = ((EditText) actionBar.getCustomView().findViewById(R.id.searchET));
      searchET.addTextChangedListener(new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
          // When user changed the Text
          final String text = searchET.getText().toString()
            .toLowerCase(Locale.getDefault());
          adapter.filter(text);
          if(text.isEmpty() && !adapter.isViewAll())
            adapter.setViewAll(adapter.isViewAll()); /* force refresh */
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        @Override
        public void afterTextChanged(Editable arg0) {
        }
      });
      searchET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
          if (actionId == EditorInfo.IME_ACTION_DONE) {
            InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            in.hideSoftInputFromWindow(v
                .getApplicationWindowToken(),
              InputMethodManager.HIDE_NOT_ALWAYS);
            return true;
          }
          return false;
        }
      });


      actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
    }

  }

  @Override
  public void onResume() {
    searchET.setText("");
    super.onResume();
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
  public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
    SqlEntryItem item = (SqlEntryItem)adapter.getItem(position);
    if(item != null) {
      switch (item.getType()) {
        case EMAIL:
          ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(50);
          Sys.openMail(this, item.getValue());
          break;
        case PHONE:
          ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(50);
          Sys.openDialer(this, item.getValue());
          break;
        case COMPOSE:
        case TEXT:
          ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(50);
          Sys.clipboard(this, item.getValue());
          UI.toast(this, R.string.text_copied);
          break;
        case URL:
          ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(50);
          Sys.openBrowser(this, item.getValue());
          break;
      }
    }
    return true;
  }

  public boolean inputText3(int reqId, int type, String text1, String text2) {
    if(!text1.isEmpty() && !text2.isEmpty()) {
      SqlEntryItem sti = new SqlEntryItem(currentItem == null ? 0 : currentItem.getId(), SqlEntryItem.Type.fromInt(type + 1), text1, text2);

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
          adapter.remove(currentItem);
          currentItem.set(sti);
          sql.updateEntry(currentItem);
          sti = currentItem;
        } else {
          if(adapter.contains(sti)) {
            UI.toast(this, R.string.already_inserted);
            return false;
          }
          sql.addEntry(owner, sti);
        }
        adapter.add(sti);
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
    currentItem = (SqlEntryItem)t;
    showInputDialog3(R.string.new_entry_title, R.string.new_entry_hint_key, R.string.new_entry_hint_value, currentItem.getType(), t.getKey(), t.getValue(), REQ_ID_EDIT);
  }

  public void onMenuDelete(final SqlItem t) {
    UI.showConfirmDialog(this, R.string.confirm_delete_name_title, R.string.confirm_delete_name_message,
      new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          try {
            adapter.remove(t);
            sql.deleteEntry(owner, (SqlEntryItem)t);
          } catch(Exception e) {
            Log.e(getClass().getSimpleName(), "SQL: " + e.getMessage(), e);
            UI.showAlertDialog(EntriesActivity.this, R.string.error, "SQL: " + e.getMessage());
          }
        }
    }, null);
  }


  public void showInputDialog3(final int title, final int hint1, final int hint2, final int reqId) {
    showInputDialog3(title, hint1, hint2, SqlEntryItem.Type.TEXT, null, null, reqId);
  }

  public void showInputDialog3(final int title, final int hint1, final int hint2, final SqlEntryItem.Type type, final String def1, final String def2, final int reqId) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);

    builder.setTitle(title);
    final EditText input1 = new EditText(this);
    final EditText input2 = new EditText(this);
    final Spinner spinner = new Spinner(this);
    input1.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
    if(hint1 != Integer.MAX_VALUE)
      input1.setHint(hint1);
    if(hint2 != Integer.MAX_VALUE)
      input2.setHint(hint2);
    if(def1 != null)
      input1.setText(def1);
    if(def2 != null)
      input2.setText(def2);
    List<SpinnerIconItem> list = new ArrayList<>();
    list.add(new SpinnerIconItem(R.mipmap.ic_mail, getString(R.string.email)));
    list.add(new SpinnerIconItem(R.mipmap.ic_url, getString(R.string.url)));
    list.add(new SpinnerIconItem(R.mipmap.ic_phone, getString(R.string.phone)));
    list.add(new SpinnerIconItem(R.mipmap.ic_copy, getString(R.string.text)));
    list.add(new SpinnerIconItem(R.mipmap.ic_compose, getString(R.string.compose)));
    final SpinnerIconArrayAdapter ladapter = new SpinnerIconArrayAdapter(this, list);
    spinner.setAdapter(ladapter);
    spinner.setSelection(SqlEntryItem.Type.toInt(type) - 1);
    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        SpinnerIconItem item = ladapter.getItem(i);
        if(item != null) {
          switch (item.getIcon()) {
            case R.mipmap.ic_mail:
              input2.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
              break;
            case R.mipmap.ic_phone:
              input2.setInputType(InputType.TYPE_CLASS_PHONE);
              break;
            case R.mipmap.ic_url:
              input2.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
              if(input2.getText().toString().isEmpty())
                input2.setText(R.string.text_start_https);
              break;
            case R.mipmap.ic_compose:
              input2.setInputType(InputType.TYPE_CLASS_NUMBER);
              break;
            default:
              input2.setInputType(InputType.TYPE_CLASS_TEXT);
              break;
          }
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {

      }
    });

    final TextView tv = new TextView(this);
    tv.setText(R.string.type);

    LinearLayout parent = new LinearLayout(this);

    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    lp.setMargins(20, 0, 0, 20);
    parent.setLayoutParams(lp);
    parent.setOrientation(LinearLayout.VERTICAL);

    LinearLayout.LayoutParams lpTop = new LinearLayout.LayoutParams(
      LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    /* int left, int top, int right, int bottom */
    lpTop.setMargins(20, 40, 0, 20);
    parent.addView(tv, lpTop);
    parent.addView(spinner, lp);
    parent.addView(input1, lp);
    parent.addView(input2, lp);

    builder.setView(parent);

    // Set up the buttons
    builder.setPositiveButton(getString(R.string.ok), null);
    builder.setNegativeButton(getString(R.string.cancel), null);
    final AlertDialog mAlertDialog = builder.create();
    mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

      @Override
      public void onShow(DialogInterface dialog) {

        Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        b.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            if(inputText3(reqId, spinner.getSelectedItemPosition(), input1.getText().toString(), input2.getText().toString()))
              mAlertDialog.dismiss();
          }
        });
      }
    });
    mAlertDialog.setCanceledOnTouchOutside(false);
    mAlertDialog.show();
  }
}
