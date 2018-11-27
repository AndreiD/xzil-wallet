package wallet.zilliqa.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.socks.library.KLog;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import wallet.zilliqa.BaseApplication;
import wallet.zilliqa.BaseFragment;
import wallet.zilliqa.BuildConfig;
import wallet.zilliqa.R;
import wallet.zilliqa.data.local.AppDatabase;
import wallet.zilliqa.data.local.PreferencesHelper;
import wallet.zilliqa.utils.DUtils;
import wallet.zilliqa.utils.DialogFactory;
import wallet.zilliqa.utils.MyClipboardManager;

public class AddNewTokenFragment extends BaseFragment {

  @BindView(R.id.button_save_new_token)
  Button button_save_new_token;

  @BindView(R.id.button_token_paste)
  Button button_token_paste;

  @BindView(R.id.autoCompleteTextView_search_tokens)
  AutoCompleteTextView autoCompleteTextView_search_tokens;

  @BindView(R.id.edit_token_address)
  EditText edit_token_address;

  private JSONArray jsonArrayTokens;
  private String selected_address = null;
  private String selected_name;
  private String selected_symbol;
  private int selected_decimals;
  private AppDatabase db;
  private Web3j web3j;
  private PreferencesHelper preferencesHelper;
  private Credentials credentials;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_add_new_token, container, false);
  }

  @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);


    db = BaseApplication.getAppDatabase(getActivity());
    preferencesHelper = new PreferencesHelper(getActivity());

    if (BuildConfig.DEBUG) {
      edit_token_address.setText("0x3758a452fd5139db395fdb7253c919d26086a378");
    }

    ArrayList<String> suggestionsList = new ArrayList<>();
    try {
      String jsonDetails = DUtils.loadFileFromAssets(getActivity(), "token_detail_dict.json");
      jsonArrayTokens = new JSONArray(jsonDetails);
      for (int i = 0; i < jsonArrayTokens.length(); i++) {
        JSONObject jsonObject = jsonArrayTokens.getJSONObject(i);
        String name = jsonObject.getString("name");
        String symbol = jsonObject.getString("symbol");
        String address = jsonObject.getString("address");
        suggestionsList.add(symbol + " (" + name + ") " + address);
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }

    ArrayAdapter<String> adapter = new ArrayAdapter<>
        (getActivity(), android.R.layout.select_dialog_item, suggestionsList);
    autoCompleteTextView_search_tokens.setThreshold(1);
    autoCompleteTextView_search_tokens.setAdapter(adapter);

    autoCompleteTextView_search_tokens.setOnItemClickListener((adapterView, view, i, l) -> {

      int selectedPos = suggestionsList.indexOf((((TextView) view).getText()).toString());
      String selectedToken = suggestionsList.get(selectedPos);
      String selectedAddress = selectedToken.substring(selectedToken.indexOf(") ") + 2);
      getTokenDetails(selectedAddress);
    });
  }

  private void getTokenDetails(String selectedAddress) {
    for (int i = 0; i < jsonArrayTokens.length(); i++) {
      try {
        JSONObject jsonObject = jsonArrayTokens.getJSONObject(i);
        String mAddress = jsonObject.getString("address");
        if (mAddress.equals(selectedAddress)) {
          selected_name = jsonObject.getString("name");
          selected_symbol = jsonObject.getString("symbol");
          selected_decimals = jsonObject.getInt("decimals");
          selected_address = jsonObject.getString("address");
          return;
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
  }

  @OnClick(R.id.button_token_paste) public void onClickPaste() {
    edit_token_address.setText(new MyClipboardManager().readFromClipboard(getActivity()));
  }

  @SuppressLint("CheckResult") @OnClick(R.id.button_save_new_token) public void onClickSave() {
    String customToken = edit_token_address.getText().toString();

    if (selected_address != null && !TextUtils.isEmpty(customToken)) {
      DialogFactory.error_toast(getActivity(), "Please, either select one from the list or enter a custom one").show();
      return;
    }

    if (selected_address != null && !TextUtils.isEmpty(selected_address)) {
      KLog.d(">>> loading selected  >>>" + selected_address);

      //EthToken ethToken = new EthToken(selected_address, selected_name, selected_symbol, selected_decimals);
      //Completable.fromAction(() -> db.tokenDao().insertAll(ethToken)).subscribe(() -> {
      //  KLog.d("done inserting..");
      //  DialogFactory.success_toast(getActivity(), selected_name + " added successfully").show();
      //  preferencesHelper.setDefaultToken(ethToken.getAddress());
      //  getActivity().onBackPressed();
      //}, throwable -> {
      //  KLog.e(throwable);
      //  DialogFactory.error_toast(getActivity(), "There's already a token with this address added.").show();
      //});

      return;
    }

    if (!WalletUtils.isValidAddress(customToken)) {
      DialogFactory.error_toast(getActivity(), "invalid ETH token address").show();
      return;
    }

    //web3j = BaseApplication.getWeb3(getActivity());
    //try {
    //  Cryptography cryptography = new Cryptography(getActivity());
    //  String decodedPassword = cryptography.decryptData(preferencesHelper.getPassword());
    //  String decodedSeed = cryptography.decryptData(preferencesHelper.getSeed());
    //
    //  credentials = WalletUtils.loadBip39Credentials(decodedPassword, decodedSeed);
    //} catch (NoSuchPaddingException | NoSuchAlgorithmException | UnrecoverableEntryException | CertificateException | KeyStoreException | InvalidKeyException | BadPaddingException | NoSuchProviderException | IllegalBlockSizeException | InvalidAlgorithmParameterException | IOException e) {
    //  e.printStackTrace();
    //}
    //Token mToken = Token.load(customToken, web3j, credentials,
    //    Constants.getGasPrice(getActivity()),
    //    Constants.DEFAULT_GAS_LIMIT);
    //Single<EthToken> ethTokenSingle = QueryBlockchain.detailsOf(mToken, customToken);
    //ethTokenSingle.subscribe(new SingleObserver<EthToken>() {
    //  @Override
    //  public void onSubscribe(Disposable d) {
    //  }
    //
    //  @Override
    //  public void onSuccess(EthToken ethToken) {
    //
    //    KLog.d("Detected -> " + ethToken);
    //    Completable.fromAction(() -> db.tokenDao().insertAll(ethToken)).subscribe(() -> {
    //      KLog.d("done inserting..");
    //      DialogFactory.success_toast(getActivity(), ethToken.getName() + " added successfully").show();
    //      preferencesHelper.setDefaultToken(ethToken.getAddress());
    //      getActivity().onBackPressed();
    //    }, throwable -> {
    //      KLog.e(throwable);
    //      DialogFactory.error_toast(getActivity(), "There's already a token with this address added.").show();
    //    });
    //  }
    //
    //  @Override
    //  public void onError(Throwable e) {
    //    DialogFactory.error_toast(getActivity(), e.getLocalizedMessage()).show();
    //  }
    //});
  }
}