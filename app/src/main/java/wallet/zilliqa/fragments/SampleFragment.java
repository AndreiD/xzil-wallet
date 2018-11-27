package wallet.zilliqa.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import wallet.zilliqa.BaseApplication;
import wallet.zilliqa.BaseFragment;
import wallet.zilliqa.BuildConfig;
import wallet.zilliqa.Constants;
import wallet.zilliqa.R;
import wallet.zilliqa.data.local.AppDatabase;
import wallet.zilliqa.data.local.Wallet;
import wallet.zilliqa.data.local.PreferencesHelper;
import wallet.zilliqa.utils.Cryptography;
import wallet.zilliqa.utils.DUtils;
import wallet.zilliqa.utils.DialogFactory;
import wallet.zilliqa.utils.MyClipboardManager;

public class SampleFragment extends BaseFragment {

  //@BindView(R.id.btn_receive_share)
  //Button btn_receive_share;
  private static final String ARG_TEXT = "ARG_TEXT";

  public SampleFragment() {
  }

  public static SampleFragment newInstance(String text) {
    Bundle args = new Bundle();
    args.putString(ARG_TEXT, text);

    SampleFragment sampleFragment = new SampleFragment();
    sampleFragment.setArguments(args);

    return sampleFragment;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_receive, container, false);
  }

  @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    //boolean isjustcreated = getArguments().getBoolean("isjustcreated", false);

  }
}