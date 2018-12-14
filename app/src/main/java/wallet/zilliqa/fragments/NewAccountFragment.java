package wallet.zilliqa.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import butterknife.BindView;
import butterknife.OnClick;
import com.socks.library.KLog;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import wallet.zilliqa.BaseApplication;
import wallet.zilliqa.BaseFragment;
import wallet.zilliqa.BuildConfig;
import wallet.zilliqa.Constants;
import wallet.zilliqa.R;
import wallet.zilliqa.activities.CreateNewAccountActivity;
import wallet.zilliqa.activities.MainActivity;
import wallet.zilliqa.data.local.AppDatabase;
import wallet.zilliqa.data.local.PreferencesHelper;
import wallet.zilliqa.data.local.Wallet;
import wallet.zilliqa.utils.Cryptography;
import wallet.zilliqa.utils.DialogFactory;

public class NewAccountFragment extends BaseFragment {

  @BindView(R.id.button_new_account) Button button_new_account;
  @BindView(R.id.button_new_account_import) Button button_new_account_import;
  @BindView(R.id.editText_seed) EditText editText_seed;
  @BindView(R.id.toolbar) android.support.v7.widget.Toolbar toolbar;
  @BindView(R.id.theWebView) WebView theWebView;
  private String address;
  private String privateKey;
  private ProgressDialog progressDialog;
  private PreferencesHelper preferencesHelper;
  private Cryptography cryptography;

  public NewAccountFragment() {
  }

  public static NewAccountFragment newInstance() {
    NewAccountFragment fragment = new NewAccountFragment();
    fragment.setRetainInstance(true);
    return fragment;
  }

  @Nullable @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_new_account, container, false);
  }

  @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    preferencesHelper = new PreferencesHelper(getActivity());
    cryptography = new Cryptography(getActivity());

    theWebView.getSettings().setJavaScriptEnabled(true);
    theWebView.getSettings().setAppCacheEnabled(false);
    theWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
    theWebView.setBackgroundColor(Color.TRANSPARENT);
    theWebView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);

    theWebView.addJavascriptInterface(new WebAppInterface(getActivity()), "Android");
    theWebView.loadUrl("file:///android_asset/javascript/accounts.html");

    toolbar.setTitle(getString(R.string.wallet_creation));
    toolbar.setNavigationOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());

    if(BuildConfig.DEBUG){
      editText_seed.setText(Constants.newWalletSeed);
    }
  }

  @OnClick(R.id.button_new_account) public void onClickNewAccount() {
    startActivity(new Intent(getActivity(), CreateNewAccountActivity.class));
  }

  @OnClick(R.id.button_new_account_import) public void onClickImportAccount() {

    progressDialog =
        DialogFactory.createProgressDialog(getActivity(),
            "Importing wallet...");
    progressDialog.show();

    String mnemonic = editText_seed.getText().toString();

    if(mnemonic.length()<10){
      DialogFactory.error_toast(getActivity(),"Invalid Mnemonic").show();
      return;
    }

    new CountDownTimer(500, 500) {
      @Override public void onTick(long l) {
      }

      @Override public void onFinish() {
        progressDialog.dismiss();
        theWebView.loadUrl("javascript:generateAccount(\"" + mnemonic + "\")");
      }
    }.start();

  }

  @Override public void onPause() {
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


      // encrypt the private key &  stores it encrypted
      Cryptography cryptography = new Cryptography(getActivity());
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
}