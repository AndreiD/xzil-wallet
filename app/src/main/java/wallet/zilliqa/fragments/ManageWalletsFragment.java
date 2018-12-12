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
import wallet.zilliqa.R;
import wallet.zilliqa.adapters.ManageWalletsAdapter;
import wallet.zilliqa.data.local.AppDatabase;
import wallet.zilliqa.utils.DialogFactory;

public class ManageWalletsFragment extends BaseFragment {

  @BindView(R.id.recycler_view_wallets) RecyclerView recycler_view_wallets;
  @BindView(R.id.toolbar) android.support.v7.widget.Toolbar toolbar;
  private AppDatabase db;

  public ManageWalletsFragment() {
  }

  public static ManageWalletsFragment newInstance() {
    ManageWalletsFragment fragment = new ManageWalletsFragment();
    fragment.setRetainInstance(true);
    return fragment;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_manage_wallets, container, false);
  }

  @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    toolbar.setNavigationOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());

    recycler_view_wallets.setLayoutManager(new LinearLayoutManager(getActivity()));
    recycler_view_wallets.setItemAnimator(new DefaultItemAnimator());
    db = BaseApplication.getAppDatabase(getActivity());
  }

  @Override public void onResume() {
    super.onResume();

    //populate wallets list
    db.walletDao().getAll().subscribe(wallets -> {

      if (wallets.size() == 0) {
        DialogFactory.error_toast(getActivity(), "You don't have any wallets added. Use the + button to add.").show();
        getActivity().onBackPressed();
      }

      ManageWalletsAdapter mAdapter = new ManageWalletsAdapter(getActivity(), wallets);
      recycler_view_wallets.setAdapter(mAdapter);
    }, throwable -> KLog.e(throwable));
  }
}