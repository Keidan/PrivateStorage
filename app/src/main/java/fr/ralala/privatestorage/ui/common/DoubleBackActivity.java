package fr.ralala.privatestorage.ui.common;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import fr.ralala.privatestorage.R;
import fr.ralala.privatestorage.PrivateStorageApp;
import fr.ralala.privatestorage.sql.SqlFactory;
import fr.ralala.privatestorage.ui.utils.UI;

/**
 *******************************************************************************
 * <p><b>Project PrivateStorage</b><br/>
 * Double back pressed activity
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public abstract class DoubleBackActivity extends AppCompatActivity {
  private static final int BACK_TIME_DELAY = 2000;
  private static long lastBackPressed = -1;
  private SqlFactory sql = null;
  private PrivateStorageApp app = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    app = (PrivateStorageApp)getApplication();
    Throwable t;
    if((t = app.install()) != null)
      UI.showAlertDialog(this, R.string.exception, t.getMessage());
    sql = app.getSql();
  }

  public abstract void onExit();

  protected SqlFactory getSql() {
    return sql;
  }

  protected PrivateStorageApp getApp() {
    return app;
  }

  @Override
  public void onBackPressed() {
    if (lastBackPressed + BACK_TIME_DELAY > System.currentTimeMillis()) {
      onExit();
      super.onBackPressed();
    } else {
      UI.toast(this, R.string.on_double_back_exit_text);
    }
    lastBackPressed = System.currentTimeMillis();
  }
}
