package wallet.zilliqa.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.socks.library.KLog;
import io.github.novacrypto.bip39.MnemonicGenerator;
import io.github.novacrypto.bip39.Words;
import io.github.novacrypto.bip39.wordlists.English;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import wallet.zilliqa.BaseActivity;
import wallet.zilliqa.BaseApplication;
import wallet.zilliqa.R;
import wallet.zilliqa.data.local.AppDatabase;
import wallet.zilliqa.data.local.PreferencesHelper;
import wallet.zilliqa.data.local.Wallet;
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
  private String address;
  private String privateKey;

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
    theWebView.setBackgroundColor(Color.TRANSPARENT);
    theWebView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);

    theWebView.addJavascriptInterface(new WebAppInterface(this), "Android");
    theWebView.loadUrl("file:///android_asset/javascript/accounts.html");

    StringBuilder sb = new StringBuilder();
    byte[] entropy = new byte[Words.TWELVE.byteLength()];
    new SecureRandom().nextBytes(entropy);
    new MnemonicGenerator(English.INSTANCE)
        .createMnemonic(entropy, sb::append);
    String mnemonic = sb.toString();

    progressDialog = DialogFactory.createProgressDialog(mContext, "working...");
    progressDialog.show();

    new CountDownTimer(500, 500) {
      @Override public void onTick(long l) {
      }

      @Override public void onFinish() {
        progressDialog.dismiss();
        textView_the_seed.setText(mnemonic);
        linLayout_new_account_all.setVisibility(View.VISIBLE);
        theWebView.loadUrl("javascript:generateAccount(\"" + mnemonic + "\")");
      }
    }.start();
  }

  @OnClick(R.id.btn_seed_clipboard) public void onClickCopyToClipboard() {
    android.content.ClipboardManager clipboard =
        (android.content.ClipboardManager) mContext.getSystemService(
            Context.CLIPBOARD_SERVICE);
    android.content.ClipData clip = android.content.ClipData.newPlainText("", textView_the_seed.getText().toString());
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

      // encrypt the private key &  stores it encrypted
      Cryptography cryptography = new Cryptography(getApplication());
      try {
        String encryptedPrivateKey = cryptography.encryptData(privateKey);

        AppDatabase appDatabase = BaseApplication.getAppDatabase(mContext);
        appDatabase.walletDao().insertAll(new Wallet(address, encryptedPrivateKey));

        //set it as default
        preferencesHelper.setDefaultAddress(address);

        KLog.d(">>> new wallet with address " + address + " stored in the db");
      } catch (NoSuchPaddingException | NoSuchAlgorithmException |
          UnrecoverableEntryException | CertificateException | KeyStoreException |
          IOException | InvalidAlgorithmParameterException | InvalidKeyException |
          NoSuchProviderException | BadPaddingException | IllegalBlockSizeException e) {
        e.printStackTrace();
        DialogFactory.error_toast(mContext, e.getLocalizedMessage()).show();
        return;
      }

      Intent intent = new Intent(mContext, MainActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(intent);
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
    public void setAccount(String maddress, String mprivateKey) {
      KLog.d(">>>>>>>>>>>>>>> Address & Private Key Generated >>>>>>>>>>>>>>>");
      address = maddress;
      privateKey = mprivateKey;
    }
  }
}
