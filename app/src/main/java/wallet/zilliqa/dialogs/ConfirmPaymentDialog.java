package wallet.zilliqa.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
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
import com.socks.library.KLog;
import java.math.BigInteger;
import wallet.zilliqa.BaseApplication;
import wallet.zilliqa.Constants;
import wallet.zilliqa.R;
import wallet.zilliqa.data.local.AppDatabase;
import wallet.zilliqa.data.local.PreferencesHelper;
import wallet.zilliqa.utils.Cryptography;
import wallet.zilliqa.utils.DialogFactory;

public class ConfirmPaymentDialog extends DialogFragment {

  public static final String TOADDRESS = "toaddress";
  public static final String AMOUNT = "amount";
  public static final String GAS_PRICE = "gas_price";
  BigInteger nonce = null;
  private ProgressDialog progressDialog;
  private PreferencesHelper preferencesHelper;
  private AppDatabase db;

  public static ConfirmPaymentDialog newInstance(String toAddress, double amount, String gasPrice) {
    ConfirmPaymentDialog frag = new ConfirmPaymentDialog();
    Bundle args = new Bundle();
    args.putString(TOADDRESS, toAddress);
    args.putDouble(AMOUNT, amount);
    args.putString(GAS_PRICE, gasPrice);
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

    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    String toAddress = getArguments().getString(TOADDRESS, "");
    double amount = getArguments().getDouble(AMOUNT, 0.0);
    String gasPrice = getArguments().getString(GAS_PRICE, "20");

    TextView txt_dlg_confirm_to = view.findViewById(R.id.txt_dlg_confirm_to);
    TextView txt_dlg_confirm_from = view.findViewById(R.id.txt_dlg_confirm_from);
    TextView txt_dlg_confirm_amount = view.findViewById(R.id.txt_dlg_confirm_amount);
    TextView txt_dlg_confirm_fee = view.findViewById(R.id.txt_dlg_confirm_fee);
    TextView txt_dlg_confirm_total = view.findViewById(R.id.txt_dlg_confirm_total);
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

    preferencesHelper = BaseApplication.getPreferencesHelper(getActivity());

    db = BaseApplication.getAppDatabase(getActivity());

    // Setup Initial Views
    txt_dlg_confirm_from.setText(preferencesHelper.getDefaulAddress());
    txt_dlg_confirm_amount.setText(String.format("%s ZIL",
        Constants.getDecimalFormat().format(amount)));
    txt_dlg_confirm_fee.setText(String.format("%s ZIL", gasPrice));
    txt_dlg_confirm_total.setText(String.format("%s ZIL", Constants.getDecimalFormat().format(amount + Double.valueOf(gasPrice))));

    getDialog().setCancelable(true);
    getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    getDialog().getWindow()
        .setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

    getDialog().getWindow().setSoftInputMode(
        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    btn_dlg_confirm_send.setOnClickListener(v -> {

      progressDialog = DialogFactory.createProgressDialog(getActivity(),
          "Sending " + String.valueOf(amount) + " to be mined. Please wait");
      progressDialog.show();
      db.walletDao().findByAddress(preferencesHelper.getDefaulAddress()).subscribe(wallet -> {

        Cryptography cryptography = new Cryptography(getActivity());
        String decryptedPrivateKey = cryptography.decryptData(wallet.getEncrypted_private_key());

        theWebView.loadUrl("javascript:sendTransaction('" + toAddress + "','" + String.valueOf(amount) + "','" + gasPrice + "','" + decryptedPrivateKey + "')");
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
      try {
        progressDialog.dismiss();
      } catch (Exception ignored) {
      }
      FragmentManager fm = getActivity().getSupportFragmentManager();
      TxHashDialog txHashDialog =
          TxHashDialog.newInstance(hash);
      txHashDialog.show(fm, "tx_hash_dialog");
      getActivity().getSupportFragmentManager().popBackStack();
    }
  }
}