package fr.ralala.privatestorage.ui.activities.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import fr.ralala.privatestorage.ui.activities.common.DoubleBackActivity;
import fr.ralala.privatestorage.ui.activities.NamesActivity;
import fr.ralala.privatestorage.ui.utils.FingerprintHandler;
import fr.ralala.privatestorage.R;
import fr.ralala.privatestorage.utils.Sys;
import fr.ralala.privatestorage.ui.utils.UI;

/**
 *******************************************************************************
 * <p><b>Project PrivateStorage</b><br/>
 * Management of the login part
 * </p>
 *
 * @author Keidan
 *
 *******************************************************************************
 */
public class LoginActivity extends DoubleBackActivity implements OnEditorActionListener, FingerprintHandler.FingerprintHandlerListener {
  private EditText mTokenET = null;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getIntent().getBooleanExtra("EXIT", false)) {
      finish();
      return;
    }

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    setContentView(R.layout.content_login);

    mTokenET = findViewById(R.id.tokenET);
    mTokenET.setOnEditorActionListener(this);
  }

  public void onResume() {
    super.onResume();
    mTokenET.setText("");
    Button loginBT = findViewById(R.id.loginBT);
    Button linkToForgotBT = findViewById(R.id.linkToForgotBT);
    ImageView fingerprintIV = findViewById(R.id.fingerprintIV);
    TextView fingerprintTVDisplay = findViewById(R.id.fingerprintTVDisplay);
    TextView tokenTV = findViewById(R.id.tokenTV);
    TextInputLayout tokenLayoutET = findViewById(R.id.tokenLayoutET);
    TextView fingerprintTV = findViewById(R.id.fingerprintTV);
    FingerprintHandler helper = new FingerprintHandler(this, this);
    boolean useFingerprint = helper.loadAuthentication(getApp().getToken());
    int vF = useFingerprint ? View.VISIBLE : View.GONE;
    int vP = useFingerprint ? View.GONE : View.VISIBLE;
    mTokenET.setVisibility(vP);
    tokenTV.setVisibility(vP);
    tokenLayoutET.setVisibility(vP);
    loginBT.setVisibility(vP);
    linkToForgotBT.setVisibility(vP);
    fingerprintTV.setVisibility(vF);
    fingerprintIV.setVisibility(vF);
    fingerprintTVDisplay.setVisibility(vF);
  }


  @Override
  public void onAuthenticationError(String err) {
  }

  @Override
  public void onAuthenticationFailed() {
    UI.toast(this, R.string.fingerprint_authentication_failed);
  }

  @Override
  public void onAuthenticationHelp(String help) {
    UI.toast(this, getString(R.string.fingerprint_authentication_help) + "\n" + help);
  }

  @Override
  public void onAuthenticationSucceeded() {
    Class<?> clazz = getApp().getFrom();
    if(clazz == null)
      Sys.switchTo(this, NamesActivity.class, true);
    else
      Sys.switchTo(this, clazz, true);
  }

  @Override
  public void onStart() {
    super.onStart();
    if (!getApp().isValidToken()) {
      Sys.switchTo(this, InitActivity.class, true);
      finish();
    }
  }

  @Override
  public boolean onEditorAction(final TextView v, final int actionId,
                                final KeyEvent event) {
    if (actionId == EditorInfo.IME_ACTION_DONE) {
      final String pwd = ((EditText) findViewById(R.id.tokenET)).getText()
        .toString().trim();
      if (!pwd.isEmpty())
        actionLogin(findViewById(R.id.loginBT));
    }
    return false;
  }

  public void actionLogin(final View v) {
    final String pwd = ((TextView) findViewById(R.id.tokenET)).getText()
      .toString().trim();
    if (pwd.isEmpty()) {
      UI.showAlertDialog(this, R.string.error, R.string.error_invalid_token_value);
    } else {
      if (!getApp().isValidToken(pwd)) {
        UI.showAlertDialog(this, R.string.error, R.string.error_invalid_token_value);
      } else
        Sys.switchTo(this, NamesActivity.class, true);
    }
  }

  public void actionForgot(final View v) {
    Sys.switchTo(this, ForgotActivity.class, true);
  }

  @Override
  public void onExit() {
    if(getSql() != null) getSql().close();
    Sys.kill(this);
    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    intent.putExtra("EXIT", true);
    startActivity(intent);
    Process.killProcess(android.os.Process.myPid());
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (getSql() != null) getSql().close();
  }
}
