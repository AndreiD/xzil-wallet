package wallet.zilliqa.dialogs;

import android.content.DialogInterface;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.gson.JsonObject;
import com.socks.library.KLog;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import wallet.zilliqa.Constants;
import wallet.zilliqa.R;
import wallet.zilliqa.data.remote.RpcMethod;
import wallet.zilliqa.data.remote.ZilliqaRPC;

public class TxHashDialog extends DialogFragment {

  public static final String TXID = "txid";
  private Disposable disposable;
  private TextView textView_dlg_status;
  private String txID;
  private ProgressBar progressBar_dlg_hash;
  private DialogInterface.OnDismissListener onDismissListener;
  private int totalTries = 200;

  public static TxHashDialog newInstance(String txID) {
    TxHashDialog frag = new TxHashDialog();
    Bundle args = new Bundle();
    args.putString(TXID, txID);
    frag.setArguments(args);
    return frag;
  }

  public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
    this.onDismissListener = onDismissListener;
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    return inflater.inflate(R.layout.dialog_tx_hash, container, false);
  }

  @Override public void onResume() {
    super.onResume();
    disposable = Observable.interval(5000, 3000,
        TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::checkConfirmedTransaction);
    progressBar_dlg_hash.setVisibility(View.VISIBLE);
  }

  private void checkConfirmedTransaction(Long aLong) {

    ZilliqaRPC zilliqaRPC = ZilliqaRPC.Factory.getIstance(getActivity());
    RpcMethod rpcMethod = new RpcMethod();
    rpcMethod.setId("1");
    rpcMethod.setJsonrpc("2.0");
    rpcMethod.setMethod("GetTransaction");
    List<String> emptyList = new ArrayList<>();
    emptyList.add(txID);
    rpcMethod.setParams(emptyList);
    zilliqaRPC.executeRPCCall(rpcMethod).enqueue(new Callback<JsonObject>() {
      @Override public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
        totalTries -= 1;
        if (totalTries == 0) {
          disposable.dispose();
          textView_dlg_status.setText("Status: Unknown!");
          progressBar_dlg_hash.setVisibility(View.GONE);
          return;
        }
        if (response.body() != null ? response.body().toString().toLowerCase().contains("txn hash not present") : false) {
          return;
        }
        if (response.code() == 200) {
          try {
            boolean isSuccess = response.body().getAsJsonObject("result").getAsJsonObject("receipt").get("success").getAsBoolean();
            if (isSuccess) {
              disposable.dispose();
              textView_dlg_status.setText("Status: Success");
              progressBar_dlg_hash.setVisibility(View.GONE);
            }
          } catch (Exception ex) {
            KLog.e(ex);
          }
        } else {
          KLog.e("GetTransaction: response code is not 200!");
        }
      }

      @Override public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
        KLog.e(t);
      }
    });
  }

  @Override public void onPause() {
    super.onPause();
    try {
      disposable.dispose();
    } catch (Exception ignored) {
    }
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    getDialog().getWindow().setSoftInputMode(
        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    txID = getArguments() != null ? getArguments().getString(TXID, "") : null;

    TextView textView_dlg_txHash = view.findViewById(R.id.textView_dlg_txHash);
    textView_dlg_txHash.setText("Transaction ID: " + txID);
    progressBar_dlg_hash = view.findViewById(R.id.progressBar_dlg_hash);
    textView_dlg_status = view.findViewById(R.id.textView_dlg_status);
    Button btn_dlg_hash_etherscan = view.findViewById(R.id.btn_dlg_hash_etherscan);
    Button btn_dlg_hash_close = view.findViewById(R.id.btn_dlg_hash_close);

    btn_dlg_hash_etherscan.setOnClickListener(view1 -> {

      String url = Constants.EXPLORER_URL + txID + "?network=testnet";
      Intent i = new Intent(Intent.ACTION_VIEW);
      i.setData(Uri.parse(url));
      startActivity(i);

      dismiss();
    });

    btn_dlg_hash_close.setOnClickListener(view12 -> {
      dismiss();
    });
  }

  @Override
  public void onDismiss(DialogInterface dialog) {
    super.onDismiss(dialog);
    if (onDismissListener != null) {
      onDismissListener.onDismiss(dialog);
    }
  }
}