package wallet.zilliqa.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.math.BigInteger;
import wallet.zilliqa.Constants;
import wallet.zilliqa.R;
import wallet.zilliqa.data.local.PreferencesHelper;
import wallet.zilliqa.utils.Cryptography;

public class ConfirmPaymentDialog extends DialogFragment {

  public static final String TOADDRESS = "toaddress";
  public static final String AMOUNT = "amount";
  public static final String IS_ETH = "is_eth";
  public static final String TOKEN_SYMBOL = "token_symbol";
  public static final String TOKEN_ADDRESS = "token_address";
  BigInteger nonce = null;
  private ProgressDialog progressDialog;

  public static ConfirmPaymentDialog newInstance(boolean isETH, String toAddress, double amount,
      String tokenSymbol, String tokenAddress) {
    ConfirmPaymentDialog frag = new ConfirmPaymentDialog();
    Bundle args = new Bundle();
    args.putString(TOADDRESS, toAddress);
    args.putDouble(AMOUNT, amount);
    args.putBoolean(IS_ETH, isETH);
    args.putString(TOKEN_SYMBOL, tokenSymbol);
    args.putString(TOKEN_ADDRESS, tokenAddress);
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
    boolean isEth = getArguments().getBoolean(IS_ETH, true);
    String tokenSymbol = getArguments().getString(TOKEN_SYMBOL, null);
    String tokenAddress = getArguments().getString(TOKEN_ADDRESS, null);

    TextView txt_dlg_confirm_to = view.findViewById(R.id.txt_dlg_confirm_to);
    TextView txt_dlg_confirm_from = view.findViewById(R.id.txt_dlg_confirm_from);
    TextView txt_dlg_confirm_amount = view.findViewById(R.id.txt_dlg_confirm_amount);
    TextView txt_dlg_confirm_fee = view.findViewById(R.id.txt_dlg_confirm_fee);
    TextView txt_dlg_confirm_total = view.findViewById(R.id.txt_dlg_confirm_total);

    Button btn_dlg_confirm_send = view.findViewById(R.id.btn_dlg_confirm_send);

    txt_dlg_confirm_to.setText(toAddress);

    txt_dlg_confirm_amount.setText(String.format("%s ETH",
        Constants.getDecimalFormat().format(amount)));

    if (!isEth) {
      txt_dlg_confirm_amount.setText(amount + " " + tokenSymbol);
    }

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
    String network_preference = prefs.getString("network_preference", "mainnet");
    if (!network_preference.equals("mainnet")) {
      txt_dlg_confirm_amount.setText(amount + " ETH (testnet)");
      txt_dlg_confirm_amount.setTextColor(getResources().getColor(R.color.appcolor_red_darker));
    }

    getDialog().setCancelable(true);
    getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    getDialog().getWindow()
        .setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

    getDialog().getWindow().setSoftInputMode(
        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    PreferencesHelper preferencesHelper = new PreferencesHelper(getActivity());

    Cryptography cryptography = new Cryptography(getActivity());

    //BigDecimal value = Convert.toWei(String.valueOf(amount), Convert.Unit.ETHER);

    //String decodedPassword = cryptography.decryptData(preferencesHelper.getPassword());
    //String decodedSeed = cryptography.decryptData(preferencesHelper.getSeed());

  }
}