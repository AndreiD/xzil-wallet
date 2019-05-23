package wallet.zilliqa.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.firestack.laksaj.utils.Validation;
import com.google.gson.JsonObject;
import com.socks.library.KLog;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import wallet.zilliqa.BaseApplication;
import wallet.zilliqa.BaseFragment;
import wallet.zilliqa.BuildConfig;
import wallet.zilliqa.Constants;
import wallet.zilliqa.R;
import wallet.zilliqa.data.local.AppDatabase;
import wallet.zilliqa.data.local.PreferencesHelper;
import wallet.zilliqa.data.remote.RpcMethod;
import wallet.zilliqa.data.remote.ZilliqaRPC;
import wallet.zilliqa.dialogs.ConfirmPaymentDialog;
import wallet.zilliqa.qrscanner.QRScannerActivity;
import wallet.zilliqa.utils.Convert;
import wallet.zilliqa.utils.DUtils;
import wallet.zilliqa.utils.DialogFactory;

public class SendFragment extends BaseFragment {

  @BindView(R.id.send_editText_to) EditText send_editText_to;
  @BindView(R.id.send_editText_amount) EditText send_editText_amount;
  @BindView(R.id.send_button_send) Button send_button_send;
  @BindView(R.id.send_imageButton_scanqr) ImageView send_imageButton_scanqr;
  @BindView(R.id.seekBar_fee) SeekBar seekBar_fee;
  @BindView(R.id.send_textView_amount) TextView send_textView_amount;
  @BindView(R.id.send_textView_currency) TextView send_textView_currency;
  @BindView(R.id.send_textView_fee) TextView send_textView_fee;
  private BigDecimal balanceZIL = new BigDecimal(0);
  private BigDecimal gasPriceInZil;
  private PreferencesHelper preferencesHelper;
  private Disposable disposable;
  private BigDecimal minimumGasPrice = new BigDecimal("1000000000"); // default min gas price

  public SendFragment() {
  }

  public static SendFragment newInstance(boolean isjustcreated) {
    Bundle args = new Bundle();
    args.putBoolean("isjustcreated", isjustcreated);
    SendFragment fragment = new SendFragment();
    fragment.setArguments(args);
    fragment.setRetainInstance(true);
    return fragment;
  }

  @Nullable @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_send, container, false);
  }

  @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    preferencesHelper = BaseApplication.getPreferencesHelper(getActivity());
    AppDatabase db = BaseApplication.getAppDatabase(getActivity());

    //TODO: remove me
    if (BuildConfig.DEBUG) {
      send_editText_to.setText(Constants.secondWalletAddress);
      send_editText_amount.setText("0.1");
    }

    // update the balance

    seekBar_fee.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        gasPriceInZil = minimumGasPrice;
        switch (progress) {
          case 0:
            gasPriceInZil = Convert.fromQa(minimumGasPrice, Convert.Unit.ZIL); //10% more
            break;
          case 1:
            gasPriceInZil = Convert.fromQa(minimumGasPrice.multiply(new BigDecimal("1.1")),
                Convert.Unit.ZIL); //10% more
            break;
          case 2:
            gasPriceInZil = Convert.fromQa(minimumGasPrice.multiply(new BigDecimal("2")),
                Convert.Unit.ZIL); //100% more
            break;
          default:
            KLog.w("unknown seekbar value!");
            break;
        }
        send_textView_fee.setText(
            String.format("Gas Price: %s ZIL", gasPriceInZil.toString()));
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    getMinimumGasPrice();

    //set default gas price (10% more)
    gasPriceInZil =
        Convert.fromQa(minimumGasPrice.multiply(new BigDecimal("1.1")), Convert.Unit.ZIL);
    send_textView_fee.setText(
        String.format("Gas Price: %s ZIL", gasPriceInZil.toString()));
  }

  private void getMinimumGasPrice() {
    ZilliqaRPC zilliqaRPC = ZilliqaRPC.Factory.getInstance(getActivity());
    RpcMethod rpcMethod = new RpcMethod();
    rpcMethod.setId("1");
    rpcMethod.setJsonrpc("2.0");
    rpcMethod.setMethod("GetMinimumGasPrice");
    List<String> emptyList = new ArrayList<>();
    emptyList.add("");
    rpcMethod.setParams(emptyList);
    zilliqaRPC.executeRPCCall(rpcMethod).enqueue(new Callback<JsonObject>() {
      @Override public void onResponse(@NonNull Call<JsonObject> call,
          @NonNull Response<JsonObject> response) {
        if (response.code() == 200) {
          String resp =
              response.body() != null ? response.body().get("result").getAsString() : null;
          minimumGasPrice = new BigDecimal(resp); // update min gas price
          //set default gas price (10% more)
          gasPriceInZil =
              Convert.fromQa(minimumGasPrice.multiply(new BigDecimal("1.1")), Convert.Unit.ZIL);
          send_textView_fee.setText(
              String.format("Gas Price: %s ZIL", gasPriceInZil.toString()));
        } else {
          KLog.e("getMinimumGasPrice: response code is not 200!");
        }
      }

      @Override public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
        KLog.e(t);
      }
    });
  }

  @Override public void onResume() {
    super.onResume();

    if (!Constants.lastScanAddress.isEmpty()) {
      send_editText_to.setText(Constants.lastScanAddress);
    }

    disposable = Observable.interval(500, 10000,
        TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::updateBalances);
  }

  @OnClick(R.id.send_button_send) public void onClickSend() {

    // hides the keyboard
    InputMethodManager imm =
        (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(send_editText_amount.getWindowToken(), 0);
    imm.hideSoftInputFromWindow(send_editText_to.getWindowToken(), 0);

    BigDecimal amount_to_send = new BigDecimal(0);
    if (send_editText_amount.getText().toString().trim().length() > 0) {
      try {
        amount_to_send = new BigDecimal(send_editText_amount.getText().toString().trim());
      } catch (Exception ignored) {
      }
    } else {
      DialogFactory.warning_toast(getActivity(), "Please enter the amount you want to send").show();
      return;
    }

    if (!Validation.isAddress(send_editText_to.getText().toString().trim())) {
      DialogFactory.warning_toast(getActivity(), "The destination address is not valid.")
          .show();
      return;
    }

    if (balanceZIL.compareTo(amount_to_send) < 0) {
      DialogFactory.warning_toast(getActivity(),
          "Seems you don't have enough ZIL for this transaction.").show();
      send_textView_amount.setTextColor(getResources().getColor(R.color.material_red));
      return;
    }

    sendTheMoney(send_editText_to.getText().toString().trim(), amount_to_send, gasPriceInZil);
  }

  private void sendTheMoney(String destinationAddress, BigDecimal amount,
      BigDecimal gasPriceInZil) {
    DUtils.hideKeyboard(getActivity());
    FragmentManager fm = getActivity().getSupportFragmentManager();
    ConfirmPaymentDialog confirmPaymentDialog =
        ConfirmPaymentDialog.newInstance(destinationAddress, amount, gasPriceInZil);
    confirmPaymentDialog.show(fm, "confirm_dialog_fragment");
  }

  @OnClick(R.id.send_imageButton_scanqr) public void onClickScanQR() {
    Intent iScan = new Intent(getActivity(), QRScannerActivity.class);
    iScan.putExtra("type", "address");
    startActivity(iScan);
  }

  private void updateBalances(Long aLong) {
    ZilliqaRPC zilliqaRPC = ZilliqaRPC.Factory.getInstance(getActivity());
    RpcMethod rpcMethod = new RpcMethod();
    rpcMethod.setId("1");
    rpcMethod.setJsonrpc("2.0");
    rpcMethod.setMethod("GetBalance");
    List<String> emptyList = new ArrayList<>();
    emptyList.add(preferencesHelper.getDefaulAddress());
    rpcMethod.setParams(emptyList);
    zilliqaRPC.executeRPCCall(rpcMethod).enqueue(new Callback<JsonObject>() {
      @Override public void onResponse(@NonNull Call<JsonObject> call,
          @NonNull Response<JsonObject> response) {
        if (response.code() == 200) {
          try {
            JsonObject body = response.body();
            String resp;
            if (body != null) {
              resp = body.getAsJsonObject("result").get("balance").getAsString();
              BigDecimal balBD = new BigDecimal(resp);
              balanceZIL = Convert.fromQa(balBD, Convert.Unit.ZIL);
              getActivity().runOnUiThread(() -> {
                send_textView_amount.setText("Amount: " + balanceZIL.toString() + " ZIL");
              });
            }
          } catch (Exception ex) {
            KLog.e(ex);
            send_textView_amount.setText("Amount: 0 ZIL");
          }
        } else {
          KLog.e("getBalance: response code is not 200!");
          send_textView_amount.setText("Amount: ??? ZIL");
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
}