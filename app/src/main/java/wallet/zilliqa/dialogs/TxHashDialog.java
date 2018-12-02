package wallet.zilliqa.dialogs;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import wallet.zilliqa.Constants;
import wallet.zilliqa.R;

public class TxHashDialog extends DialogFragment {

  public static final String TXHASH = "txhash";

  public static TxHashDialog newInstance(String txHash) {
    TxHashDialog frag = new TxHashDialog();
    Bundle args = new Bundle();
    args.putString(TXHASH, txHash);
    frag.setArguments(args);
    return frag;
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    return inflater.inflate(R.layout.dialog_tx_hash, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    getDialog().getWindow().setSoftInputMode(
        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    String txHash = getArguments().getString(TXHASH, "");

    TextView textView_dlg_txHash = view.findViewById(R.id.textView_dlg_txHash);
    textView_dlg_txHash.setText("Hash: " + txHash);

    Button btn_dlg_hash_etherscan = view.findViewById(R.id.btn_dlg_hash_etherscan);
    Button btn_dlg_hash_close = view.findViewById(R.id.btn_dlg_hash_close);

    btn_dlg_hash_etherscan.setOnClickListener(view1 -> {

      String baseUrl = Constants.getExplorerUrl(getActivity());

      String url = baseUrl + txHash;
      Intent i = new Intent(Intent.ACTION_VIEW);
      i.setData(Uri.parse(url));
      startActivity(i);

      dismiss();
    });

    btn_dlg_hash_close.setOnClickListener(view12 -> dismiss());
  }
}