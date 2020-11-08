package fr.ralala.privatestorage.ui.activities.login;

import fr.ralala.privatestorage.R;
import fr.ralala.privatestorage.PrivateStorageApp;
import fr.ralala.privatestorage.sql.SqlFactory;
import fr.ralala.privatestorage.utils.Sys;
import fr.ralala.privatestorage.ui.utils.UI;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 *******************************************************************************
 * <p><b>Project PrivateStorage</b><br/>
 * Management of the forgot part
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class ForgotActivity extends AppCompatActivity {
  private SqlFactory mSql = null;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.content_forgot);
    PrivateStorageApp app = (PrivateStorageApp)getApplication();
    Throwable t;
    if((t = app.install()) != null)
      UI.showAlertDialog(this, R.string.exception, t.getMessage());
    mSql = app.getSql();
    ((PrivateStorageApp)getApplicationContext()).setFrom(null);
  }
  
  public void actionReset(final View v) {
    final android.view.View.OnClickListener yes = (vv) -> {
      mSql.removeAll();
      onBackPressed();
    };
    UI.showConfirmDialog(this, R.string.reset,
        R.string.confirm_erase_config, yes, null);
    
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    Sys.switchTo(this, LoginActivity.class, true);
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
}
