package fr.ralala.privatestorage.ui.activities.login;

import fr.ralala.privatestorage.R;
import fr.ralala.privatestorage.PrivateStorageApp;
import fr.ralala.privatestorage.ui.activities.common.RuntimePermissionsActivity;
import fr.ralala.privatestorage.utils.Sys;
import fr.ralala.privatestorage.ui.utils.UI;

import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

/**
 *******************************************************************************
 * <p><b>Project PrivateStorage</b><br/>
 * Management of the init part
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class InitActivity extends RuntimePermissionsActivity {
  private EditText mTokenET = null;
  private EditText mTokenConfirmET = null;
  private static final int PERMISSIONS_REQUEST = 30;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Set View to init.xml
    setContentView(R.layout.content_init);
    mTokenET = findViewById(R.id.tokenET);
    mTokenConfirmET = findViewById(R.id.tokenConfirmET);

    String[] perms = new String[]{
      Manifest.permission.WRITE_EXTERNAL_STORAGE,
      Manifest.permission.READ_EXTERNAL_STORAGE,
      Manifest.permission.INTERNET,
    };
    super.requestAppPermissions(perms, R.string.permissions , PERMISSIONS_REQUEST);
    ((PrivateStorageApp)getApplicationContext()).setFrom(null);
  }

  @Override
  public void onPermissionsGranted(final int requestCode) {
  }

  @Override
  public void onResume() {
    super.onResume();
    mTokenET.setText("");
    mTokenConfirmET.setText("");
  }

  public void actionCreate(final View v) {
    final String token = mTokenET.getText().toString().trim();
    final String tokenConfirm = mTokenConfirmET.getText().toString()
        .trim();
    if (token.isEmpty()) {
      UI.showAlertDialog(this, R.string.error, R.string.error_invalid_token_value);
    } else if (token.compareTo(tokenConfirm) != 0) {
      UI.showAlertDialog(this, R.string.error, R.string.error_token_not_match);
    } else {
      getApp().setToken(token);
      Sys.switchTo(this, LoginActivity.class, true);
      finish();
    }
  }

  @Override
  public void onExit() {
    if(getSql() != null) getSql().close();
    Sys.kill(this);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if(getSql() != null) getSql().close();
  }
}
