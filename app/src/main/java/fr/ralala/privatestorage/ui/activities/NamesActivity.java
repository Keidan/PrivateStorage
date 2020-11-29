package fr.ralala.privatestorage.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fr.ralala.privatestorage.R;
import fr.ralala.privatestorage.PrivateStorageApp;
import fr.ralala.privatestorage.items.SpinnerIconItem;
import fr.ralala.privatestorage.items.SqlNameItem;
import fr.ralala.privatestorage.ui.adapters.RecyclerViewAdapter;
import fr.ralala.privatestorage.ui.adapters.SpinnerIconArrayAdapter;
import fr.ralala.privatestorage.ui.adapters.SqlNamesArrayAdapter;
import fr.ralala.privatestorage.ui.activities.common.DoubleBackActivity;
import fr.ralala.privatestorage.ui.activities.login.LoginActivity;
import fr.ralala.privatestorage.ui.utils.SwipeEditDeleteRecyclerViewItem;
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
public class NamesActivity extends DoubleBackActivity implements RecyclerViewAdapter.AdapterOnClickListener, SwipeEditDeleteRecyclerViewItem.SwipeEditDeleteRecyclerViewItemListener {
  private static final int REQ_ID_ADD = 0;
  private static final int REQ_ID_EDIT = 1;

  private SqlNamesArrayAdapter mAdapter = null;
  private SqlNameItem mCurrentItem = null;
  private PrivateStorageApp mApp;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mApp = (PrivateStorageApp)getApplicationContext();
    /* Disable back button */
    try {
      ActionBar ab = getSupportActionBar();
      if (ab != null) {
        ab.setDisplayShowCustomEnabled(false);
        ab.setDisplayHomeAsUpEnabled(false);
        ab.setTitle(R.string.app_name);
      }
    } catch(Exception e) {
      Log.e(getClass().getSimpleName(), "Exception " + e.getMessage(), e);
    }
    if (getIntent().getBooleanExtra("EXIT", false)) {
      finish();
      return;
    }
    setContentView(R.layout.activity_names);

    try {
      RecyclerView mRecyclerView = findViewById(R.id.content_names);
      mRecyclerView.setHasFixedSize(true);
      RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
      mRecyclerView.setLayoutManager(layoutManager);
      mRecyclerView.getRecycledViewPool().clear();
      mAdapter = new SqlNamesArrayAdapter(mRecyclerView, R.layout.menu_list_item_2, getSql().getNames());
      mAdapter.setClickListeners(this, null);
      mRecyclerView.setAdapter(mAdapter);
      mAdapter.safeNotifyDataSetChanged();
      new SwipeEditDeleteRecyclerViewItem(this, mRecyclerView, this);
    } catch(Exception e) {
      Log.e(getClass().getSimpleName(), "SQL: " + e.getMessage(), e);
      UI.showAlertDialog(this, R.string.error, "SQL: " + e.getMessage());
    }

    FloatingActionButton fab = findViewById(R.id.fab);
    fab.setOnClickListener((view) -> {
      mCurrentItem = null;
      showInputDialog2(R.string.new_data_title, SqlNameItem.Type.DISPLAY, null, REQ_ID_ADD);
    });
    ((PrivateStorageApp)getApplicationContext()).setFrom(this.getClass());

    getApp().setCurrentName(null);

    if(!getApp().isSwipeNamesDisplayed()) {
      UI.toastLong(this, R.string.swipe_information);
      getApp().swipeNamesDisplayed();
    }
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
  public void onDestroy() {
    super.onDestroy();
  }

  public boolean inputText2(int reqId, String text, int type) {
    if(!text.isEmpty()) {
      SqlNameItem sti = new SqlNameItem(SqlNameItem.Type.fromInt(type), text, "");
      try {
        if(reqId == REQ_ID_EDIT) {
          mAdapter.removeItem(mCurrentItem);
          mCurrentItem.setKey(text);
          mCurrentItem.setType(sti.getType());
          getSql().updateName(mCurrentItem);
          sti = mCurrentItem;
        } else {
          if(mAdapter.contains(sti)) {
            UI.toast(this, R.string.already_inserted);
            return false;
          }
          getSql().addName(sti);
        }
        mAdapter.addItem(sti);
        if(reqId == REQ_ID_ADD) {
          getApp().setCurrentName(sti.getKey());
          Sys.switchTo(this, EntriesActivity.class, false);
        }
      } catch(Exception e) {
        Log.e(getClass().getSimpleName(), "SQL: " + e.getMessage(), e);
        UI.showAlertDialog(this, R.string.error, "SQL: " + e.getMessage());
        //finish();
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
    }if (id == R.id.action_export) {
      if(mApp.getLastExportType().equals(PrivateStorageApp.PREFS_VAL_LAST_EXPORT_DEVICE)) {
        Sys.exportDevice(this);
      } else if(mApp.getLastExportType().equals(PrivateStorageApp.PREFS_VAL_LAST_EXPORT_DROPBOX)) {
        Sys.exportDropbox(mApp, this);
      }
      return true;
    } else if (id == android.R.id.home) {
      onBackPressed();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  /**
   * Called when a ViewHolder is swiped from left to right by the user.
   *
   * @param adapterPosition The position in the adapter.
   */
  @Override
  public void onClickEdit(int adapterPosition) {
    SqlNameItem entry = (SqlNameItem)mAdapter.getItem(adapterPosition);
    if (entry == null) return;
    mCurrentItem = entry;
    showInputDialog2(R.string.update_data_title, entry.getType(), entry.getKey(), REQ_ID_EDIT);
  }

  /**
   * Called when a ViewHolder is swiped from right to left by the user.
   *
   * @param adapterPosition The position in the adapter.
   */
  @Override
  public void onClickDelete(int adapterPosition) {
    SqlNameItem entry = (SqlNameItem)mAdapter.getItem(adapterPosition);
    if (entry == null) return;
    UI.showConfirmDialog(this, R.string.confirm_delete_name_title, R.string.confirm_delete_name_message, (view) -> {
      mAdapter.removeItem(entry);
      getSql().deleteName(entry);
    }, null);
  }

  @Override
  public void onItemClick(int position, View v) {
    SqlNameItem sti = (SqlNameItem)mAdapter.getItem(position);
    if(sti != null) {
      getApp().setCurrentName(sti.getKey());
      Sys.switchTo(this, EntriesActivity.class, false);
    }
  }

  public void showInputDialog2(final int title, final SqlNameItem.Type type, final String def, final int reqId) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(title);
    final TextInputEditText input = new TextInputEditText(this);
    final TextInputLayout inputLayout = new TextInputLayout(this);
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
    LinearLayout.LayoutParams lpInput = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    /* int left, int top, int right, int bottom */
    lpTop.setMargins(20, 40, 0, 20);
    parent.addView(tv, lpTop);
    parent.addView(spinner, lp);
    inputLayout.addView(input, 0, lpInput);
    parent.addView(inputLayout, lp);

    builder.setView(parent);
    // Set up the buttons
    builder.setPositiveButton(getString(R.string.ok), null);
    builder.setNegativeButton(getString(R.string.cancel), null);
    final AlertDialog mAlertDialog = builder.create();
    mAlertDialog.setOnShowListener((dialog) -> {
      Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
      b.setOnClickListener((view) -> {
        if(inputText2(reqId, input.getText().toString(), spinner.getSelectedItemPosition()))
          mAlertDialog.dismiss();
      });
    });
    mAlertDialog.setCanceledOnTouchOutside(false);
    mAlertDialog.show();
  }
}
