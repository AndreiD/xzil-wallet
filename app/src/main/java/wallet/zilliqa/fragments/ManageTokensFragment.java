package wallet.zilliqa.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import com.socks.library.KLog;
import wallet.zilliqa.BaseApplication;
import wallet.zilliqa.BaseFragment;
import wallet.zilliqa.BuildConfig;
import wallet.zilliqa.Constants;
import wallet.zilliqa.R;
import wallet.zilliqa.adapters.ManageTokensAdapter;
import wallet.zilliqa.data.local.AppDatabase;
import wallet.zilliqa.data.local.Wallet;
import wallet.zilliqa.data.local.PreferencesHelper;
import wallet.zilliqa.utils.Cryptography;
import wallet.zilliqa.utils.DUtils;
import wallet.zilliqa.utils.DialogFactory;
import wallet.zilliqa.utils.MyClipboardManager;

public class ManageTokensFragment extends BaseFragment {

  @BindView(R.id.recycler_view_tokens)
  RecyclerView recycler_view_tokens;
  private AppDatabase db;

  public ManageTokensFragment() {
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_manage_tokens, container, false);
  }

  @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    recycler_view_tokens.setLayoutManager(new LinearLayoutManager(getActivity()));
    recycler_view_tokens.setItemAnimator(new DefaultItemAnimator());
    db = BaseApplication.getAppDatabase(getActivity());
  }

  @Override public void onResume() {
    super.onResume();

    //populate token list

    //db.tokenDao().getAll().subscribe(tokens -> {
    //
    //  if (tokens.size() == 0) {
    //    DialogFactory.error_toast(getActivity(), "You don't have any tokens added. Use the + button to add.").show();
    //    getActivity().onBackPressed();
    //  }
    //
    //  ManageTokensAdapter mAdapter = new ManageTokensAdapter(getActivity(), tokens);
    //  recycler_view_tokens.setAdapter(mAdapter);
    //}, throwable -> KLog.e(throwable));
  }
}