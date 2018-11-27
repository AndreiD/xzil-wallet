package wallet.zilliqa.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import com.socks.library.KLog;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import wallet.zilliqa.BaseApplication;
import wallet.zilliqa.BaseFragment;
import wallet.zilliqa.BuildConfig;
import wallet.zilliqa.Constants;
import wallet.zilliqa.R;
import wallet.zilliqa.data.local.AppDatabase;
import wallet.zilliqa.data.local.Wallet;
import wallet.zilliqa.data.local.PreferencesHelper;
import wallet.zilliqa.dialogs.ConfirmPaymentDialog;
import wallet.zilliqa.qrscanner.QRScannerActivity;
import wallet.zilliqa.utils.Cryptography;
import wallet.zilliqa.utils.DUtils;
import wallet.zilliqa.utils.DialogFactory;
import wallet.zilliqa.utils.MyClipboardManager;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.utils.Convert;

public class SendFragment extends BaseFragment {

  @BindView(R.id.send_editText_to) EditText send_editText_to;
  @BindView(R.id.send_editText_amount) EditText send_editText_amount;
  @BindView(R.id.send_button_send) Button send_button_send;
  @BindView(R.id.send_imageButton_scanqr) ImageView send_imageButton_scanqr;
  @BindView(R.id.send_eth_radioButton) RadioButton send_eth_radioButton;
  @BindView(R.id.send_token_radioButton) RadioButton send_token_radioButton;

  @BindView(R.id.send_textView_amount) TextView send_textView_amount;
  @BindView(R.id.send_textView_currency) TextView send_textView_currency;
  @BindView(R.id.send_textView_fee) TextView send_textView_fee;
  Disposable disposable;
  private BigDecimal balanceETH;
  private BigDecimal balanceToken;
  private Web3j web3j = null;
  private BigInteger feeToken;
  private BigInteger feeEth;
  private PreferencesHelper preferencesHelper;
  private String token_symbol;
  private AppDatabase db;

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
    db = BaseApplication.getAppDatabase(getActivity());

    if (preferencesHelper.getDefaultToken() == null) {
      send_token_radioButton.setVisibility(View.GONE);
      send_eth_radioButton.performClick();
      send_textView_currency.setText("ETH");
    } else {
      //get the default token from the db

      //db.tokenDao().findByAddress(preferencesHelper.getDefaultToken()).subscribe(
      //    ethToken -> {
      //      token_symbol = ethToken.getSymbol();
      //      send_token_radioButton.setText(token_symbol);
      //      send_textView_currency.setText(token_symbol);
      //    });
    }

    //TODO: remove me
    if (BuildConfig.DEBUG) {
      send_editText_to.setText(
          "0x000000591672c2Ad77D99f62BE38Eb2C995bb09c");
      send_editText_amount.setText("0.1");
    }



    //send_textView_fee.setText(
    //    String.format("Fee (~): %s ETH",
    //        Convert.fromWei(feeToken.toString(), Convert.Unit.ETHER).toString()));

    send_button_send.setClickable(false);
  }

  @Override public void onResume() {
    super.onResume();

    if (!Constants.lastScanAddress.isEmpty()) {
      send_editText_to.setText(Constants.lastScanAddress);
    }

    disposable = Observable.interval(100, 15000,
        TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::updateBalances);
  }

  @Override public void onPause() {
    super.onPause();
    disposable.dispose();
  }

  @OnClick(R.id.send_button_send) public void onClickSend() {
    double amount_to_send = 0;
    if (send_editText_amount.getText().toString().trim().length() > 0) {
      try {
        //TODO: transform this to BigDecimal
        amount_to_send = Double.valueOf(send_editText_amount.getText().toString().trim());
      } catch (Exception ignored) {
      }
    }
    if (amount_to_send > 9999999) {
      DialogFactory.warning_toast(getActivity(),
          "This app doesn't believe that you have so much ETH/Tokens so it blocks this call")
          .show();
      return;
    }

    if (send_editText_to.getText().toString().length() < 30) {
      DialogFactory.warning_toast(getActivity(), "You need to enter the destination address.")
          .show();
      return;
    }

    if (amount_to_send <= 0) {
      DialogFactory.warning_toast(getActivity(), "Please enter the amount you want to send").show();
      return;
    }

    if (send_eth_radioButton.isChecked()) {
      if (balanceETH.compareTo(new BigDecimal(amount_to_send)) < 0) {
        DialogFactory.warning_toast(getActivity(),
            "Seems you don't have enough ETH for this transaction.").show();
        send_textView_amount.setTextColor(getResources().getColor(R.color.material_red));
        return;
      }
      sendTheMoney(true, send_editText_to.getText().toString().trim(), amount_to_send, null, null);
    } else {
      if (balanceToken.compareTo(new BigDecimal(amount_to_send)) < 0) {
        DialogFactory.warning_toast(getActivity(),
            "Seems you don't have enough tokens for this transaction.").show();
        send_textView_amount.setTextColor(getResources().getColor(R.color.material_red));
        return;
      }
      sendTheMoney(false, send_editText_to.getText().toString().trim(), amount_to_send, token_symbol, preferencesHelper.getDefaultToken());
    }
  }

  private void sendTheMoney(boolean isEth, String destinationAddress, double amount, String tokenSymbol, String tokenAddress) {

    FragmentManager fm = getActivity().getSupportFragmentManager();
    ConfirmPaymentDialog confirmPaymentDialog =
        ConfirmPaymentDialog.newInstance(isEth, destinationAddress, amount, tokenSymbol, tokenAddress);
    confirmPaymentDialog.show(fm, "confirm_dialog_fragment");
  }

  @OnClick(R.id.send_imageButton_scanqr) public void onClickScanQR() {
    Intent iScan = new Intent(getActivity(), QRScannerActivity.class);
    iScan.putExtra("type", "address");
    startActivity(iScan);
  }

  @OnCheckedChanged(R.id.send_eth_radioButton) public void onCheckedChangedRadioEth() {

    if (send_eth_radioButton.isChecked()) {
      send_textView_currency.setText("ETH");
      if (balanceETH != null) {
        send_textView_amount.setText(String.format("Amount (balance: %s ETH)",
            Constants.getDecimalFormat().format(balanceETH)));
      }

      if (feeEth != null) {
        send_textView_fee.setText(
            String.format("Fee (~): %s ETH",
                Convert.fromWei(feeEth.toString(), Convert.Unit.ETHER).toString()));
      }
    }

    send_textView_amount.setTextColor(getResources().getColor(R.color.app_grey));
  }

  @OnClick(R.id.send_token_radioButton) public void onClickToken() {

    // if we have more than 1 token, show a token picker dialog
    //db.walletDao().totalTokens().subscribe(totalTokensDb -> {
    //  if (totalTokensDb > 1) {
    //
    //    FragmentManager fm = getActivity().getSupportFragmentManager();
    //    ChangeTokenDialog changeTokenDialog = new ChangeTokenDialog();
    //    changeTokenDialog.show(fm, "change_token_dialog");
    //
    //    fm.registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
    //      @Override
    //      public void onFragmentViewDestroyed(FragmentManager fm, Fragment f) {
    //        super.onFragmentViewDestroyed(fm, f);
    //        if (f instanceof ChangeTokenDialog) {
    //          db.tokenDao().findByAddress(preferencesHelper.getDefaultToken()).subscribe(
    //              ethToken -> {
    //                token_symbol = ethToken.getSymbol();
    //                send_token_radioButton.setText(token_symbol);
    //                send_textView_currency.setText(token_symbol);
    //              });
    //          onCheckedChangedRadioToken();
    //          updateBalances(-1L);
    //        }
    //        fm.unregisterFragmentLifecycleCallbacks(this);
    //      }
    //    }, false);
    //  }
    //}, throwable -> KLog.e(throwable));
  }

  @OnCheckedChanged(R.id.send_token_radioButton) public void onCheckedChangedRadioToken() {

    if (send_token_radioButton.isChecked()) {

      if (token_symbol != null) {
        send_textView_currency.setText(token_symbol);
      } else {
        KLog.e("token symbol is null");
        return;
      }
      if (balanceToken != null) {
        send_textView_amount.setText(
            String.format("Amount (balance: %s  %s)", Constants.getDecimalFormat().format(
                balanceToken), token_symbol));
      }

      send_textView_fee.setText(
          String.format("Fee (~): %s ETH",
              Convert.fromWei(feeToken.toString(), Convert.Unit.ETHER).toString()));
    }
  }

  private void updateBalances(Long aLong) {

    // get the coinbase
    String encAddress = preferencesHelper.getAddress();

    String address = "";
    String decodedPassword = "";
    String decodedSeed = "";
    Cryptography cryptography = new Cryptography(getActivity());
    try {
      decodedPassword = cryptography.decryptData(preferencesHelper.getPassword());
      decodedSeed = cryptography.decryptData(preferencesHelper.getSeed());

      address = cryptography.decryptData(encAddress);
    } catch (NoSuchPaddingException | NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException | CertificateException | InvalidAlgorithmParameterException | IOException | InvalidKeyException | NoSuchProviderException | IllegalBlockSizeException | BadPaddingException e) {
      e.printStackTrace();
    }

    // get balance for ETH
    //Single<BigInteger> balanceForETH =
    //    QueryBlockchain.getBalanceForETH(web3j, address);
    //balanceForETH.subscribe(new SingleObserver<BigInteger>() {
    //  @Override
    //  public void onSubscribe(Disposable d) {
    //  }
    //
    //  @Override
    //  public void onSuccess(BigInteger bigInteger) {
    //    balanceETH =
    //        Convert.fromWei(new BigDecimal(bigInteger.toString()), Convert.Unit.ETHER);
    //    refreshUI();
    //  }
    //
    //  @Override
    //  public void onError(Throwable e) {
    //    KLog.e(e);
    //  }
    //});

    if (preferencesHelper.getDefaultToken() == null) {
      return;
    }

    // get balance for token
    Credentials credentials = WalletUtils.loadBip39Credentials(decodedPassword, decodedSeed);

  }

  private void refreshUI() {
    onCheckedChangedRadioToken();
    onCheckedChangedRadioEth();
    send_button_send.setClickable(true);
  }
}