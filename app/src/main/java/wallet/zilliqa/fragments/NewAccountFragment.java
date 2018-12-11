package wallet.zilliqa.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
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
import wallet.zilliqa.R;
import wallet.zilliqa.activities.CreateNewAccountActivity;
import wallet.zilliqa.data.local.PreferencesHelper;
import wallet.zilliqa.utils.Cryptography;
import wallet.zilliqa.utils.DialogFactory;

public class NewAccountFragment extends BaseFragment {

  @BindView(R.id.button_new_account) Button button_new_account;
  @BindView(R.id.button_new_account_import) Button button_new_account_import;
  @BindView(R.id.editText_password) EditText editText_password;
  @BindView(R.id.editText_seed) EditText editText_seed;
  @BindView(R.id.toolbar) android.support.v7.widget.Toolbar toolbar;

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

    //Intent intent = new Intent(getActivity(), MainActivity.class);
    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    //startActivity(intent);
    //getActivity().finish();

  }

  @Override public void onPause() {
    super.onPause();
    if ((progressDialog != null) && progressDialog.isShowing()) {
      progressDialog.dismiss();
    }
  }
}