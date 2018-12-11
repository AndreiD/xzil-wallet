package wallet.zilliqa.activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.socks.library.KLog;
import io.github.novacrypto.bip39.MnemonicGenerator;
import io.github.novacrypto.bip39.Words;
import io.github.novacrypto.bip39.wordlists.English;
import java.io.File;
import java.security.SecureRandom;
import wallet.zilliqa.BaseActivity;
import wallet.zilliqa.R;
import wallet.zilliqa.data.local.PreferencesHelper;
import wallet.zilliqa.utils.Cryptography;
import wallet.zilliqa.utils.DialogFactory;

public class CreateNewAccountActivity extends BaseActivity {

  @BindView(R.id.textView_the_seed) TextView textView_the_seed;
  @BindView(R.id.checkBox_seed) CheckBox checkBox_seed;
  @BindView(R.id.btn_seed_clipboard) Button btn_seed_clipboard;
  @BindView(R.id.btn_seed_continue) Button btn_seed_continue;
  @BindView(R.id.linLayout_new_account_all) LinearLayout linLayout_new_account_all;
  @BindView(R.id.theWebView) WebView theWebView;

  private ProgressDialog progressDialog;
  private CreateNewAccountActivity mContext;
  private Cryptography cryptography;
  private PreferencesHelper preferencesHelper;
  private String mnemonic;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_new_account);
    ButterKnife.bind(this);

    mContext = CreateNewAccountActivity.this;

    cryptography = new Cryptography(mContext);
    preferencesHelper = new PreferencesHelper(mContext);

    linLayout_new_account_all.setVisibility(View.INVISIBLE);

    theWebView.getSettings().setJavaScriptEnabled(true);
    theWebView.getSettings().setAppCacheEnabled(false);
    theWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

    theWebView.addJavascriptInterface(new WebAppInterface(this), "Android");
    theWebView.loadUrl("file:///android_asset/javascript/accounts.html");

    new EnterPasswordDialog(CreateNewAccountActivity.this).show();
  }

  private class EnterPasswordDialog extends Dialog {

    public Activity activity;
    private EditText editText_wallet_password;
    private Button btn_dialog_password_continue;

    public EnterPasswordDialog(Activity activity) {
      super(activity);
      this.activity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.dialog_wallet_password);
      editText_wallet_password = findViewById(R.id.editText_wallet_password);
      btn_dialog_password_continue = findViewById(R.id.btn_dialog_password_continue);

      btn_dialog_password_continue.setOnClickListener(
          view -> {

            if (editText_wallet_password.getText().toString().length() < 4) {
              DialogFactory.error_toast(CreateNewAccountActivity.this,
                  "Please enter at least a 4 characters passphrase").show();
              return;
            }

            StringBuilder sb = new StringBuilder();
            byte[] entropy = new byte[Words.TWELVE.byteLength()];
            new SecureRandom().nextBytes(entropy);
            new MnemonicGenerator(English.INSTANCE)
                .createMnemonic(entropy, sb::append);
            String seed = sb.toString();
            String passPhrase =  editText_wallet_password.getText().toString();
            theWebView.loadUrl("javascript:generateAccount(\""+seed+"###"+ passPhrase +"\")");

            //new CreateAccountAsyncTask().execute(editText_wallet_password.getText().toString());

            dismiss();
          });
    }
  }

  @OnClick(R.id.btn_seed_clipboard) public void onClickCopyToClipboard() {
    android.content.ClipboardManager clipboard =
        (android.content.ClipboardManager) mContext.getSystemService(
            Context.CLIPBOARD_SERVICE);
    android.content.ClipData clip = android.content.ClipData.newPlainText("", mnemonic);
    if (clipboard != null) {
      clipboard.setPrimaryClip(clip);
      DialogFactory.simple_toast(mContext, "copied to clipboard").show();
    }
  }

  @OnClick(R.id.btn_seed_continue) public void onClickContinue() {
    if (!checkBox_seed.isChecked()) {
      checkBox_seed.setTextColor(getResources().getColor(R.color.material_red));
      DialogFactory.error_toast(mContext, "Please check that you stored the seed safely!")
          .show();
    } else {
      Intent intent = new Intent(mContext, MainActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(intent);
    }
  }

  private class CreateAccountAsyncTask extends AsyncTask<String, String, String> {
    @Override protected void onPreExecute() {
      super.onPreExecute();
      progressDialog =
          DialogFactory.createProgressDialog(mContext,
              "Creating new wallet...");
      progressDialog.show();
    }

    @Override protected String doInBackground(String... params) {
      String password = params[0];

      File folder = new File(mContext.getFilesDir(), "MyTokenApp");
      if (!folder.exists()) {
        Log.d("NewAccount", "folder did not exist, creating the key folder");
        folder.mkdirs();
      }

      //preferencesHelper.setSeed(encryptedMnemonic);
      //preferencesHelper.setPassword(encryptedPassword);
      //preferencesHelper.setAddress(encryptedAddress);

      return null;
    }

    @Override protected void onPostExecute(String mnemonic) {
      super.onPostExecute(mnemonic);
      if ((progressDialog != null) && progressDialog.isShowing()) {
        progressDialog.dismiss();
      }

      textView_the_seed.setText(mnemonic);
      linLayout_new_account_all.setVisibility(View.VISIBLE);
    }
  }

  @Override protected void onPause() {
    super.onPause();
    if ((progressDialog != null) && progressDialog.isShowing()) {
      progressDialog.dismiss();
    }
  }

  private class WebAppInterface {
    Context mContext;

    WebAppInterface(Context c) {
      mContext = c;
    }

    @JavascriptInterface
    public void getKeystore(String payload) {
      KLog.d(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
      KLog.d("KeyStore to save -> " + payload);
      KLog.d(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    @JavascriptInterface
    public void privateKey(String payload) {
      KLog.d(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
      KLog.d("Private key: " + payload);
      KLog.d(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }
  }
}
