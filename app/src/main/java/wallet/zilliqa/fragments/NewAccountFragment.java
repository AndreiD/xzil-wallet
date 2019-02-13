package wallet.zilliqa.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import butterknife.BindView;
import butterknife.OnClick;
import wallet.zilliqa.BaseFragment;
import wallet.zilliqa.BuildConfig;
import wallet.zilliqa.Constants;
import wallet.zilliqa.R;
import wallet.zilliqa.activities.CreateNewAccountActivity;
import wallet.zilliqa.data.local.PreferencesHelper;
import wallet.zilliqa.utils.Cryptography;
import wallet.zilliqa.utils.DialogFactory;

public class NewAccountFragment extends BaseFragment {

  @BindView(R.id.button_new_account) Button button_new_account;
  @BindView(R.id.button_new_account_import) Button button_new_account_import;
  @BindView(R.id.editText_private_key) EditText editText_private_key;
  @BindView(R.id.toolbar) android.support.v7.widget.Toolbar toolbar;
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

    toolbar.setTitle(getString(R.string.wallet_creation));
    toolbar.setNavigationOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());

    if (BuildConfig.DEBUG) {
      editText_private_key.setText(Constants.newWalletPrivateKeyX);
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

    String privateKey = editText_private_key.getText().toString();

    // --- import it here ----

    if (privateKey.length() < 66) {
      DialogFactory.error_toast(getActivity(), "Invalid Private Key").show();
      return;
    }

    new CountDownTimer(500, 500) {
      @Override public void onTick(long l) {
      }

      @Override public void onFinish() {
        progressDialog.dismiss();
      }
    }.start();
  }

  @Override public void onPause() {
    super.onPause();
    if (progressDialog != null) {
      progressDialog.dismiss();
    }
  }
}