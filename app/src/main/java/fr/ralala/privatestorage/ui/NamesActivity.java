package fr.ralala.privatestorage.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ListViewCompat;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fr.ralala.privatestorage.R;
import fr.ralala.privatestorage.PrivateStorageApp;
import fr.ralala.privatestorage.items.SpinnerIconItem;
import fr.ralala.privatestorage.items.SqlItem;
import fr.ralala.privatestorage.items.SqlNameItem;
import fr.ralala.privatestorage.ui.adapters.SpinnerIconArrayAdapter;
import fr.ralala.privatestorage.ui.adapters.SqlNamesArrayAdapter;
import fr.ralala.privatestorage.ui.common.DoubleBackActivity;
import fr.ralala.privatestorage.ui.adapters.SqlItemArrayAdapter;
import fr.ralala.privatestorage.ui.login.LoginActivity;
import fr.ralala.privatestorage.utils.Sys;
import fr.ralala.privatestorage.ui.utils.UI;

/**
 *******************************************************************************
 * <p><b>Project PrivateStorage</b><br/>
 * List of forms.
 * </p>
 *
 * @author Keidan
 *
 *******************************************************************************
 */
public class NamesActivity extends DoubleBackActivity implements AdapterView.OnItemClickListener, SqlItemArrayAdapter.SqlItemArrayAdapterMenuListener {
  private static final int REQ_ID_ADD = 0;
  private static final int REQ_ID_EDIT = 1;

  private SqlItemArrayAdapter adapter = null;
  private SqlNameItem currentItem = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    /* Disable back button */
    try {
      getSupportActionBar().setDisplayShowCustomEnabled(false);
      getSupportActionBar().setDisplayHomeAsUpEnabled(false);
      getSupportActionBar().setTitle(R.string.app_name);
    } catch(Exception e) {
      Log.e(getClass().getSimpleName(), "Exception " + e.getMessage(), e);

    }
    if (getIntent().getBooleanExtra("EXIT", false)) {
      finish();
      return;
    }
    setContentView(R.layout.activity_names);
    ListViewCompat list = (ListViewCompat)findViewById(R.id.content_names);
    try {
      adapter = new SqlNamesArrayAdapter(this,
        R.layout.menu_list_item_2, getSql().getNames(), this, R.menu.popup_listview_names);
      list.setAdapter(adapter);
      list.setOnItemClickListener(this);
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
        showInputDialog2(R.string.new_data_title, SqlNameItem.Type.DISPLAY, null, REQ_ID_ADD);
      }
    });
    ((PrivateStorageApp)getApplicationContext()).setFrom(this.getClass());

    getApp().setCurrentName(null);
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
  public void onDestroy() {
    super.onDestroy();
  }

  public boolean inputText2(int reqId, String text, int type) {
    if(!text.isEmpty()) {
      SqlNameItem sti = new SqlNameItem(SqlNameItem.Type.fromInt(type), text, "");
      try {
        if(reqId == REQ_ID_EDIT) {
          adapter.remove(currentItem);
          currentItem.setKey(text);
          currentItem.setType(sti.getType());
          getSql().updateName(currentItem);
          sti = currentItem;
        } else {
          if(adapter.contains(sti)) {
            UI.toast(this, R.string.already_inserted);
            return false;
          }
          getSql().addName(sti);
        }
        adapter.add(sti);
        if(reqId == REQ_ID_ADD) {
          getApp().setCurrentName(sti.getKey());
          Sys.switchTo(this, EntriesActivity.class, false);
        }
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

  public void onExit() {
    if(getSql() != null) getSql().close();
    Sys.kill(this);
    Intent intent = new Intent(getApplicationContext(), NamesActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    intent.putExtra("EXIT", true);
    startActivity(intent);
    Process.killProcess(android.os.Process.myPid());
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_names, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.action_settings) {
      Sys.switchTo(this, SettingsActivity.class, false);
      return true;
    } else if (id == android.R.id.home) {
      onBackPressed();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }


  public void onMenuEdit(SqlItem t) {
    currentItem = (SqlNameItem)t;
    showInputDialog2(R.string.update_data_title, ((SqlNameItem) t).getType(), t.getKey(), REQ_ID_EDIT);
  }

  public void onMenuDelete(final SqlItem t) {
    UI.showConfirmDialog(this, R.string.confirm_delete_name_title, R.string.confirm_delete_name_message, new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        adapter.remove(t);
        getSql().deleteName((SqlNameItem)t);
      }
    }, null);
  }

  @Override
  public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    SqlNameItem sti = (SqlNameItem)adapter.getItem(i);
    if(sti != null) {
      getApp().setCurrentName(sti.getKey());
      Sys.switchTo(this, EntriesActivity.class, false);
    }
  }

  public void showInputDialog2(final int title, final SqlNameItem.Type type, final String def, final int reqId) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(title);
    final EditText input = new EditText(this);
    final Spinner spinner = new Spinner(this);
    input.setHint(R.string.name);
    if(def != null)
      input.setText(def);
    input.setInputType(InputType.TYPE_CLASS_TEXT);

    List<SpinnerIconItem> list = new ArrayList<>();
    list.add(new SpinnerIconItem(R.mipmap.ic_menu_view, getString(R.string.view)));
    list.add(new SpinnerIconItem(R.mipmap.ic_menu_unview, getString(R.string.unview)));
    final SpinnerIconArrayAdapter ladapter = new SpinnerIconArrayAdapter(this, list);
    spinner.setAdapter(ladapter);
    spinner.setSelection(SqlNameItem.Type.toInt(type));
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
    parent.addView(input, lp);

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
            if(inputText2(reqId, input.getText().toString(), spinner.getSelectedItemPosition()))
              mAlertDialog.dismiss();
          }
        });
      }
    });
    mAlertDialog.setCanceledOnTouchOutside(false);
    mAlertDialog.show();
  }
}
