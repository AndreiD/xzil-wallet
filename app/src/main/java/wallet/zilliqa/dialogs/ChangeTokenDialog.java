package wallet.zilliqa.dialogs;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.socks.library.KLog;
import wallet.zilliqa.BaseApplication;
import wallet.zilliqa.R;
import wallet.zilliqa.data.local.AppDatabase;
import wallet.zilliqa.utils.DialogFactory;

public class ChangeTokenDialog extends DialogFragment {

  private AppDatabase db;

  public ChangeTokenDialog() {
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    return inflater.inflate(R.layout.dialog_change_token, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    getDialog().getWindow().setSoftInputMode(
        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    RecyclerView recyclerview_change_token = view.findViewById(R.id.recyclerview_change_token);

    recyclerview_change_token.setLayoutManager(new LinearLayoutManager(getActivity()));
    recyclerview_change_token.setItemAnimator(new DefaultItemAnimator());
    db = BaseApplication.getAppDatabase(getActivity());

    db.walletDao().getAll().subscribe(tokens -> {

      if (tokens.size() == 0) {
        DialogFactory.error_toast(getActivity(), "You don't have any tokens added. Use the + button to add.").show();
        dismiss();
        return;
      }

      //ChangeTokensAdapter mAdapter = new ChangeTokensAdapter(getActivity(), ChangeTokenDialog.this, tokens);
      //recyclerview_change_token.setAdapter(mAdapter);
    }, throwable -> KLog.e(throwable));
  }
}