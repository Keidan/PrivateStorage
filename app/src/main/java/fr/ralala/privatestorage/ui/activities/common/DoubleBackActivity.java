package fr.ralala.privatestorage.ui.activities.common;

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
  private static long mLastBackPressed = -1;
  private SqlFactory mSql = null;
  private PrivateStorageApp mApp = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mApp = (PrivateStorageApp)getApplication();
    mSql = mApp.getSql();
  }

  public abstract void onExit();

  protected SqlFactory getSql() {
    return mSql;
  }

  protected PrivateStorageApp getApp() {
    return mApp;
  }

  @Override
  public void onBackPressed() {
    if (mLastBackPressed + BACK_TIME_DELAY > System.currentTimeMillis()) {
      onExit();
      super.onBackPressed();
    } else {
      UI.toast(this, R.string.on_double_back_exit_text);
    }
    mLastBackPressed = System.currentTimeMillis();
  }
}
