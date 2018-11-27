package wallet.zilliqa.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import butterknife.BindView;
import butterknife.OnClick;
import java.io.File;
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
import wallet.zilliqa.data.local.Wallet;
import wallet.zilliqa.data.local.PreferencesHelper;
import wallet.zilliqa.utils.Cryptography;
import wallet.zilliqa.utils.DUtils;
import wallet.zilliqa.utils.DialogFactory;
import wallet.zilliqa.utils.MyClipboardManager;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

public class NewAccountFragment extends BaseFragment {

  @BindView(R.id.button_new_account) Button button_new_account;
  @BindView(R.id.button_new_account_import) Button button_new_account_import;
  @BindView(R.id.editText_password) EditText editText_password;
  @BindView(R.id.editText_seed) EditText editText_seed;

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
  }

  @OnClick(R.id.button_new_account) public void onClickNewAccount() {
    startActivity(new Intent(getActivity(), CreateNewAccountActivity.class));
  }

  @OnClick(R.id.button_new_account_import) public void onClickImportAccount() {

    progressDialog =
        DialogFactory.createProgressDialog(getActivity(),
            "Importing wallet...");
    progressDialog.show();

    String password = editText_password.getText().toString();
    String seed = editText_seed.getText().toString();

    if ((password.length() < 1) || (seed.length() < 10)) {
      DialogFactory.error_toast(getActivity(), "Invalid password or seed").show();
      return;
    }
    Credentials credentials = WalletUtils.loadBip39Credentials(password, seed);
    Log.d("Importing account", credentials.getAddress());
    try {
      String encryptedPassword = cryptography.encryptData(password);
      String encryptedMnemonic = cryptography.encryptData(seed);
      String encryptedAddress = cryptography.encryptData(credentials.getAddress());
      preferencesHelper.setSeed(encryptedMnemonic);
      preferencesHelper.setPassword(encryptedPassword);
      preferencesHelper.setAddress(encryptedAddress);

      if ((progressDialog != null) && progressDialog.isShowing()) {
        progressDialog.dismiss();
      }

      Intent intent = new Intent(getActivity(), MainActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(intent);
      getActivity().finish();
    } catch (NoSuchPaddingException | NoSuchAlgorithmException |
        UnrecoverableEntryException | CertificateException | KeyStoreException |
        IOException | InvalidAlgorithmParameterException | InvalidKeyException |
        NoSuchProviderException | BadPaddingException | IllegalBlockSizeException e) {
      e.printStackTrace();
      DialogFactory.createGenericErrorDialog(getActivity(), e.getLocalizedMessage()).show();
    }
  }

  @Override public void onPause() {
    super.onPause();
    if ((progressDialog != null) && progressDialog.isShowing()) {
      progressDialog.dismiss();
    }
  }


}