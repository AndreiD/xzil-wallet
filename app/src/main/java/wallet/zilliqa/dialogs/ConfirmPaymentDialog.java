package wallet.zilliqa.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firestack.laksaj.account.Account;
import com.firestack.laksaj.account.Wallet;
import com.firestack.laksaj.crypto.KeyTools;
import com.firestack.laksaj.jsonrpc.HttpProvider;
import com.firestack.laksaj.transaction.Transaction;
import com.firestack.laksaj.transaction.TransactionFactory;
import com.google.gson.JsonObject;
import com.socks.library.KLog;

import org.checkerframework.checker.units.qual.K;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import wallet.zilliqa.BaseApplication;
import wallet.zilliqa.Constants;
import wallet.zilliqa.R;
import wallet.zilliqa.activities.MainActivity;
import wallet.zilliqa.data.local.AppDatabase;
import wallet.zilliqa.data.local.PreferencesHelper;
import wallet.zilliqa.data.remote.RpcMethod;
import wallet.zilliqa.data.remote.ZilliqaRPC;
import wallet.zilliqa.utils.BlockiesIdenticon;
import wallet.zilliqa.utils.Convert;
import wallet.zilliqa.utils.Cryptography;
import wallet.zilliqa.utils.DUtils;
import wallet.zilliqa.utils.DialogFactory;
import wallet.zilliqa.utils.crypto.Bech32;

public class ConfirmPaymentDialog extends DialogFragment {

    public static final String TOADDRESS = "toaddress";
    public static final String AMOUNT = "amount";
    public static final String GAS_PRICE = "gas_price";
    private ProgressDialog progressDialog;
    private PreferencesHelper preferencesHelper;
    private AppDatabase db;
    private String amountToSendInQA;
    private String gasPriceToSendInQA;
    private String toAddress;
    private String nonce = "1";
    private String decryptedPrivateKey;
    private String fromAddress = "";

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

        DUtils.hideKeyboard(getActivity());
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

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

        toAddress = getArguments() != null ? getArguments().getString(TOADDRESS, "") : null;

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

        try {
            fromAddress = Bech32.toBech32Address(preferencesHelper.getDefaulAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }

        identicon_to.setAddress(toAddress);

            identicon_from.setAddress(fromAddress);


        Button btn_dlg_confirm_send = view.findViewById(R.id.btn_dlg_confirm_send);

        txt_dlg_confirm_to.setText(toAddress);

        db = BaseApplication.getAppDatabase(getActivity());

        // Setup Initial Views
        txt_dlg_confirm_from.setText(fromAddress);
        txt_dlg_confirm_amount.setText(String.format("%s ZIL",
                amountInZIL));
        txt_dlg_confirm_fee.setText(String.format("%s ZIL", gasPriceInZIL));

        if (Constants.isMaiNet(getActivity())) {
            txt_dlg_confirm_total.setText(String.format("%s ZIL", amountInZIL.add(gasPriceInZIL)));
        } else {
            txt_dlg_confirm_total.setText(String.format("%s ZIL (Test Net)", amountInZIL.add(gasPriceInZIL)));
        }

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

            db.walletDao().findByAddress(preferencesHelper.getDefaulAddress()).subscribe(wallet -> {

                Cryptography cryptography = new Cryptography(getActivity());
                decryptedPrivateKey = cryptography.decryptData(wallet.getEncrypted_private_key());

                amountToSendInQA = Convert.toQa(amountInZIL, Convert.Unit.ZIL).toString();
                gasPriceToSendInQA = Convert.toQa(gasPriceInZIL, Convert.Unit.ZIL).toString();

                ZilliqaRPC zilliqaRPC = ZilliqaRPC.Factory.getInstance(getActivity());
                RpcMethod rpcMethod = new RpcMethod();
                rpcMethod.setId("1");
                rpcMethod.setJsonrpc("2.0");
                rpcMethod.setMethod("GetBalance");
                List<String> emptyList = new ArrayList<>();
                emptyList.add(preferencesHelper.getDefaulAddress());
                rpcMethod.setParams(emptyList);
                zilliqaRPC.executeRPCCall(rpcMethod).enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (response.code() == 200) {
                            try {
                                String nonceResp = response.body() != null ? response.body().getAsJsonObject("result").get("nonce").getAsString() : null;
                                KLog.d("got nonce = ", nonceResp);
                                nonce = String.valueOf(Integer.valueOf(nonceResp) + 1);

                                new SendTxAsyncTask().execute();
                            } catch (Exception ex) {
                                KLog.e(ex);
                            }
                        } else {
                            KLog.e("getBalance: response code is not 200!");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        KLog.e(t);
                    }
                });
            }, throwable -> {
                KLog.e(throwable);
                try {
                    progressDialog.dismiss();
                } catch (Exception ignored) {
                }
            });
        });
    }

    class SendTxAsyncTask extends android.os.AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {

            String pubKey = KeyTools.getPublicKeyFromPrivateKey(decryptedPrivateKey, false);
            KLog.d("got pubKey from private..");

            try {
                toAddress = Bech32.fromBech32Address(toAddress);
            } catch (Exception e) {
                KLog.e(e);
            }

            Transaction transaction = Transaction.builder()
                    .version(Constants.getVersion(getActivity()))
                    .toAddr(toAddress.toLowerCase())
                    .senderPubKey(pubKey)
                    .amount(amountToSendInQA)
                    .gasPrice(gasPriceToSendInQA)
                    .gasLimit("1")
                    .code("")
                    .nonce(nonce)
                    .data("")
                    .provider(new HttpProvider(Constants.getNetworkAPIURL(getActivity())))
                    .build();

            Wallet wallet = new Wallet();
            wallet.setProvider(new HttpProvider(Constants.getNetworkAPIURL(getActivity())));
            wallet.addByPrivateKey(decryptedPrivateKey);

            Transaction signedTransaction = wallet.signWith(transaction, new Account(decryptedPrivateKey));

            try {
                HttpProvider.CreateTxResult result = TransactionFactory.createTransaction(signedTransaction);
                if (result != null && result.getTranID() != null) {
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    TxHashDialog txHashDialog =
                            TxHashDialog.newInstance(result.getTranID());
                    txHashDialog.show(fm, "tx_id_dialog");
                    txHashDialog.setOnDismissListener(dialogInterface -> {
                        KLog.d(">>> on dismiss listener called");
                        try {
                            getDialog().dismiss();
                            ((MainActivity) getActivity()).selectHome();
                        } catch (Exception ignored) {
                        }
                    });

                } else {
                    getActivity().runOnUiThread(() -> DialogFactory.warning_toast(getActivity(), "Please try again.").show());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            try {
                progressDialog.dismiss();
            } catch (Exception ignored) {
            }
        }
    }


}
