package wallet.zilliqa.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.firestack.laksaj.account.Account;
import com.firestack.laksaj.account.Wallet;
import com.firestack.laksaj.jsonrpc.HttpProvider;
import com.firestack.laksaj.transaction.Transaction;
import com.firestack.laksaj.transaction.TransactionFactory;
import com.google.gson.JsonObject;
import com.socks.library.KLog;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import wallet.zilliqa.BaseApplication;
import wallet.zilliqa.R;
import wallet.zilliqa.data.local.AppDatabase;
import wallet.zilliqa.data.local.PreferencesHelper;
import wallet.zilliqa.data.remote.RpcMethod;
import wallet.zilliqa.data.remote.ZilliqaRPC;
import wallet.zilliqa.utils.BlockiesIdenticon;
import wallet.zilliqa.utils.Convert;
import wallet.zilliqa.utils.Cryptography;
import wallet.zilliqa.utils.DUtils;
import wallet.zilliqa.utils.DialogFactory;
import wallet.zilliqa.utils.crypto.ECKey;
import wallet.zilliqa.utils.crypto.KeyPair;

import static com.firestack.laksaj.account.Wallet.pack;

public class ConfirmPaymentDialog extends DialogFragment {

  public static final String TOADDRESS = "toaddress";
  public static final String AMOUNT = "amount";
  public static final String GAS_PRICE = "gas_price";
  private ProgressDialog progressDialog;
  private PreferencesHelper preferencesHelper;
  private AppDatabase db;

  public static ConfirmPaymentDialog newInstance(String toAddress, BigDecimal amount, BigDecimal gasPriceInZil) {
    ConfirmPaymentDialog frag = new ConfirmPaymentDialog();
    Bundle args = new Bundle();
    args.putString(TOADDRESS, toAddress);
    args.putString(AMOUNT, amount.toString());
    args.putString(GAS_PRICE, gasPriceInZil.toString());
    frag.setArguments(args);
    return frag;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
  }

  @Override
  public void onStart() {
    super.onStart();
    Dialog dialog = getDialog();
    if (dialog != null) {
      int width = ViewGroup.LayoutParams.MATCH_PARENT;
      int height = ViewGroup.LayoutParams.MATCH_PARENT;
      dialog.getWindow().setLayout(width, height);
    }
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    super.onCreateView(inflater, container, savedInstanceState);
    View view = inflater.inflate(R.layout.dialog_confirm_send, container, false);

    Toolbar toolbar = view.findViewById(R.id.toolbar);
    toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
    toolbar.setNavigationOnClickListener(view1 -> dismiss());
    toolbar.setTitle(getString(R.string.confirm_payment));
    DUtils.hideKeyboard(getActivity()); //hides the keyboard, TODO:// shows for a fraction of a second. use somethign else else
    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    String toAddress = getArguments().getString(TOADDRESS, "");
    BigDecimal amountInZIL = new BigDecimal(getArguments().getString(AMOUNT, "0"));
    BigDecimal gasPriceInZIL = new BigDecimal(getArguments().getString(GAS_PRICE, "0"));

    TextView txt_dlg_confirm_to = view.findViewById(R.id.txt_dlg_confirm_to);
    TextView txt_dlg_confirm_from = view.findViewById(R.id.txt_dlg_confirm_from);
    TextView txt_dlg_confirm_amount = view.findViewById(R.id.txt_dlg_confirm_amount);
    TextView txt_dlg_confirm_fee = view.findViewById(R.id.txt_dlg_confirm_fee);
    TextView txt_dlg_confirm_total = view.findViewById(R.id.txt_dlg_confirm_total);
    BlockiesIdenticon identicon_to = view.findViewById(R.id.identicon_to);
    BlockiesIdenticon identicon_from = view.findViewById(R.id.identicon_from);

    preferencesHelper = BaseApplication.getPreferencesHelper(getActivity());

    identicon_to.setAddress(toAddress);
    identicon_from.setAddress(preferencesHelper.getDefaulAddress());


    WebView theWebView = view.findViewById(R.id.theWebView);

    // update the balance
    theWebView.getSettings().setJavaScriptEnabled(true);
    theWebView.getSettings().setAppCacheEnabled(false);
    theWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
    theWebView.setBackgroundColor(Color.TRANSPARENT);
    theWebView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);

    theWebView.addJavascriptInterface(new WebAppInterface(getActivity()), "Android");
    theWebView.loadUrl("file:///android_asset/javascript/transaction.html");

    Button btn_dlg_confirm_send = view.findViewById(R.id.btn_dlg_confirm_send);

    txt_dlg_confirm_to.setText(toAddress);


    db = BaseApplication.getAppDatabase(getActivity());

    // Setup Initial Views
    txt_dlg_confirm_from.setText(preferencesHelper.getDefaulAddress());
    txt_dlg_confirm_amount.setText(String.format("%s ZIL",
        amountInZIL));
    txt_dlg_confirm_fee.setText(String.format("%s ZIL", gasPriceInZIL));
    txt_dlg_confirm_total.setText(String.format("%s ZIL", amountInZIL.add(gasPriceInZIL)));

    getDialog().setCancelable(true);
    getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    getDialog().getWindow()
        .setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

    getDialog().getWindow().setSoftInputMode(
        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    btn_dlg_confirm_send.setOnClickListener(v -> {

      progressDialog = DialogFactory.createProgressDialog(getActivity(),
          "Sending " + txt_dlg_confirm_total.getText().toString() + ". Please wait...");
      progressDialog.show();

      //TODO: remove me after...
      new CountDownTimer(329000,329000){
        @Override public void onTick(long millisUntilFinished) {
        }
        @Override public void onFinish() {
          progressDialog.dismiss();
          DialogFactory.warning_toast(getActivity(),"transaction should be sent by now. On the next app update I'll try to display the tx id.").show();
        }
      }.start();

      db.walletDao().findByAddress(preferencesHelper.getDefaulAddress()).subscribe(wallet -> {

        //Cryptography cryptography = new Cryptography(getActivity());
        //String decryptedPrivateKey = cryptography.decryptData(wallet.getEncrypted_private_key());
        String amountToSendInQA =  Convert.toQa(amountInZIL,Convert.Unit.ZIL).toString();
        String gasPriceToSendInQA =  Convert.toQa(gasPriceInZIL,Convert.Unit.ZIL).toString();

       //  theWebView.loadUrl("javascript:sendTransaction('" + toAddress + "','" + amountToSendInQA + "','" + gasPriceToSendInQA + "','" + preferencesHelper.getPrivateKey() + "')");

        KeyPair keyPair = new KeyPair();
        keyPair.setPriKey(preferencesHelper.getPrivateKey());
        ECKey ecKey = new ECKey(keyPair, false);
        String pubKeyAsHex = ecKey.getPublicKeyAsHex();

        // Getting the nonce!
        //ZilliqaRPC zilliqaRPC = ZilliqaRPC.Factory.getIstance(getActivity());
        //RpcMethod rpcMethod = new RpcMethod();
        //rpcMethod.setId("1");
        //rpcMethod.setJsonrpc("2.0");
        //rpcMethod.setMethod("GetBalance");
        //List<String> emptyList = new ArrayList<>();
        //emptyList.add(preferencesHelper.getDefaulAddress());
        //rpcMethod.setParams(emptyList);
        //zilliqaRPC.executeRPCCall(rpcMethod).enqueue(new Callback<JsonObject>() {
        //  @Override public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
        //    if (response.code() == 200) {
        //      try {
        //        String nonce = response.body().getAsJsonObject("result").get("nonce").getAsString();
        //        KLog.d("NONCE = " + nonce);
        //      } catch (Exception ex) {
        //        KLog.e(ex);
        //      }
        //    } else {
        //      KLog.e("getBalance: response code is not 200!");
        //    }
        //  }
        //
        //  @Override public void onFailure(Call<JsonObject> call, Throwable t) {
        //    KLog.e(t);
        //  }
        //});

        //broadcast transaction

      new SendTxAsyncTask().execute();

      }, throwable -> {
        KLog.e(throwable);
        try {
          progressDialog.dismiss();
        } catch (Exception ignored) {
        }
      });
    });
    //gets the private key

  }

  class SendTxAsyncTask extends android.os.AsyncTask {
    @Override
    protected Object doInBackground(Object[] objects) {

      String amountToSendInQA =  Convert.toQa("50",Convert.Unit.ZIL).toString();
      String gasPriceToSendInQA =  Convert.toQa("1001",Convert.Unit.Li).toString();

      //  theWebView.loadUrl("javascript:sendTransaction('" + toAddress + "','" + amountToSendInQA + "','" + gasPriceToSendInQA + "','" + preferencesHelper.getPrivateKey() + "')");

      KeyPair keyPair = new KeyPair();
      keyPair.setPriKey(preferencesHelper.getPrivateKey());
      ECKey ecKey = new ECKey(keyPair, false);
      String pubKeyAsHex = ecKey.getPublicKeyAsHex();


      Transaction transaction = Transaction.builder()
          .version(String.valueOf(pack(333, 8)))  //dev
          .toAddr("0x4baf5fada8e5db92c3d3242618c5b47133ae003c".toLowerCase())
          .senderPubKey("027cedcfb845e69dbc1d8c362fc4283489aee1bd596138471b3cc885d31310b5b4")
          .amount(amountToSendInQA)
          .gasPrice(gasPriceToSendInQA)
          .gasLimit("1")
          .code("")
          .data("")
          .nonce("2")
          .provider(new HttpProvider("https://dev-api.zilliqa.com"))
          .build();

      Wallet xwallet = new Wallet();
      xwallet.setProvider(new HttpProvider("https://dev-api.zilliqa.com"));
      xwallet.addByPrivateKey(preferencesHelper.getPrivateKey());



      //sign transaction
      transaction = xwallet.signWith(transaction, new Account("4239019d82a8615b510fdeeaf7dac9afe4864c96badc8113366d7ddfd140cdfa"));
      System.out.println("signature is: " + transaction.getSignature());

      HttpProvider.CreateTxResult result = null;
      try {
        result = TransactionFactory.createTransaction(transaction);
      } catch (IOException e) {
        e.printStackTrace();
      }
      System.out.println(result);



      return null;
    }
  }


  private class WebAppInterface {
    Context mContext;

    WebAppInterface(Context c) {
      mContext = c;
    }

    @JavascriptInterface
    public void showError(String error) {
      try {
        progressDialog.dismiss();
      } catch (Exception ignored) {
      }
      DialogFactory.createGenericErrorDialog(getActivity(), error).show();
    }

    @JavascriptInterface
    public void showHash(String hash) {
      KLog.d("hash = " + hash);
      try {
        progressDialog.dismiss();
      } catch (Exception ignored) {
      }
      // Disable it for now...
      //FragmentManager fm = getActivity().getSupportFragmentManager();
      //TxHashDialog txHashDialog =
      //    TxHashDialog.newInstance(hash);
      //txHashDialog.show(fm, "tx_hash_dialog");
      //getActivity().getSupportFragmentManager().popBackStack();
    }
  }


}