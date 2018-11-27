package wallet.zilliqa.fragments;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.web3j.protocol.Web3j;
import org.web3j.utils.Convert;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import wallet.zilliqa.BaseApplication;
import wallet.zilliqa.BaseFragment;
import wallet.zilliqa.Constants;
import wallet.zilliqa.R;
import wallet.zilliqa.data.local.PreferencesHelper;
import wallet.zilliqa.data.remote.ExchangeRatesAPI;
import wallet.zilliqa.dialogs.ChangeTokenDialog;
import wallet.zilliqa.utils.Cryptography;

public class HomeFragment extends BaseFragment {

  @BindView(R.id.textView_fragmentHome_tokenName) TextView textView_fragmentHome_tokenName;
  @BindView(R.id.textView_fragmentHome_balance_eth) TextView textView_fragmentHome_balance_eth;
  @BindView(R.id.textView_fragmentHome_balance_token) TextView textView_fragmentHome_balance_token;
  @BindView(R.id.textView_fragmentHome_status) TextView textView_fragmentHome_status;
  @BindView(R.id.textView_fragmentHome_greeting) TextView textView_fragmentHome_greeting;
  @BindView(R.id.linlayout_fragmenthome_balance_token) LinearLayout
      linlayout_fragmenthome_balance_token;
  @BindView(R.id.textView_fragmentHome_date) TextView textView_fragmentHome_date;
  @BindView(R.id.home_line_chart) LineChart home_line_chart;
  private Web3j web3j = null;
  private Disposable disposable;
  private PreferencesHelper preferencesHelper;

  public HomeFragment() {
  }

  public static HomeFragment newInstance(boolean isjustcreated) {
    HomeFragment fragment = new HomeFragment();
    Bundle args = new Bundle();
    args.putBoolean("isjustcreated", isjustcreated);
    fragment.setRetainInstance(true);
    fragment.setArguments(args);
    return fragment;
  }

  @Nullable @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_home, container, false);
  }

  @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    preferencesHelper = BaseApplication.getPreferencesHelper(getActivity());
    textView_fragmentHome_balance_eth.setText("");
    textView_fragmentHome_balance_token.setText("");

    textView_fragmentHome_status.setText("Updating...");
    showGreeting();

    boolean isjustcreated = getArguments().getBoolean("isjustcreated", false);

    if (isjustcreated) {
      textView_fragmentHome_status.setText("Thank you for creating a wallet with us.");
    }
  }

  private void setupChart(String tokenSymbol) {
    home_line_chart.setDrawGridBackground(false);
    home_line_chart.getDescription().setEnabled(false);
    home_line_chart.setTouchEnabled(false);
    home_line_chart.setDragEnabled(false);
    home_line_chart.setScaleEnabled(false);
    home_line_chart.setPinchZoom(false);
    home_line_chart.setBackgroundColor(Color.WHITE);
    home_line_chart.setDrawGridBackground(false);
    home_line_chart.setDrawBorders(false);

    ArrayList<Entry> values = new ArrayList<>();

    ExchangeRatesAPI exchangeRatesAPI = ExchangeRatesAPI.Factory.getIstance(getActivity());

    exchangeRatesAPI.getGraphData(tokenSymbol).enqueue(new Callback<JsonObject>() {
      @Override public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
        if (response.code() > 299) {
          KLog.e("failed to get historical data for the chart");
          home_line_chart.setVisibility(View.GONE);
          return;
        }

        JsonObject jsonObject = response.body();

        //try it again, but only if it's not ETH
        if (jsonObject.toString().toLowerCase().contains("error") && !tokenSymbol.equals("ETH")) {
          setupChart("ETH");
          return;
        }

        JsonArray jArrayData = jsonObject.get("Data").getAsJsonArray();
        for (int i = 0; i < jArrayData.size(); i++) {
          try {
            JsonObject dataJsonObject = jArrayData.get(i).getAsJsonObject();
            //long time = dataJsonObject.get("time").getAsLong();
            double close = dataJsonObject.get("close").getAsDouble();
            values.add(new Entry(i, (float) close));
          } catch (Exception ignored) {
          }
        }

        if (values.size() == 0) {
          return;
        }

        LineDataSet ethToUsdLine = new LineDataSet(values, tokenSymbol + " - last 3 months");
        ethToUsdLine.setDrawIcons(false);
        ethToUsdLine.setColor(Color.BLACK);
        ethToUsdLine.setLineWidth(2f);
        ethToUsdLine.setFormLineWidth(1f);
        ethToUsdLine.setDrawCircles(false);
        ethToUsdLine.setDrawValues(false);
        ethToUsdLine.setDrawCircleHole(false);
        ethToUsdLine.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        // remove axis
        YAxis rightAxis = home_line_chart.getAxisRight();
        rightAxis.setEnabled(false);

        YAxis leftAxis = home_line_chart.getAxisLeft();
        leftAxis.setTextColor(Color.parseColor("#2962ff"));
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawZeroLine(false);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setValueFormatter((value, axis) -> "$" + String.valueOf((int) value));

        XAxis xAxis = home_line_chart.getXAxis();
        xAxis.setEnabled(false);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(ethToUsdLine);
        LineData data = new LineData(dataSets);

        // set data
        home_line_chart.setData(data);
        home_line_chart.invalidate();
      }

      @Override public void onFailure(Call<JsonObject> call, Throwable t) {
        KLog.e(t.getLocalizedMessage());
        home_line_chart.setVisibility(View.GONE);
      }
    });
  }

  @Override public void onResume() {
    super.onResume();

    if (preferencesHelper.getDefaultToken() == null) {
      linlayout_fragmenthome_balance_token.setVisibility(View.GONE);
    } else {
      linlayout_fragmenthome_balance_token.setVisibility(View.VISIBLE);
    }

    //web3j = BaseApplication.getWeb3(getActivity());

    disposable = Observable.interval(100, 15000,
        TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::updateBalances);
  }

  @Override public void onPause() {
    super.onPause();
    disposable.dispose();
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
      return;
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
    //    BigDecimal balanceETH =
    //        Convert.fromWei(new BigDecimal(bigInteger.toString()), Convert.Unit.ETHER);
    //    String balanceETHFormatted = Constants.getDecimalFormat().format(balanceETH);
    //    textView_fragmentHome_balance_eth.setText(balanceETHFormatted);
    //    textView_fragmentHome_status.setText("all is good.");
    //
    //    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
    //    String network_preference = prefs.getString("network_preference", "mainnet");
    //    if (!network_preference.equals("mainnet")) {
    //      textView_fragmentHome_status.setText("you are using a test network");
    //      textView_fragmentHome_status.setTextColor(
    //          getResources().getColor(R.color.appcolor_red_darker));
    //    }
    //  }
    //
    //  @Override
    //  public void onError(Throwable e) {
    //    KLog.e(e);
    //  }
    //});

    if (preferencesHelper.getDefaultToken() == null) {
      setupChart("ETH");
      return;
    }

    ////get the default token from the db
    //AppDatabase db = BaseApplication.getAppDatabase(getActivity());
    //db.walletDao().findByAddress(preferencesHelper.getDefaultToken()).subscribe(
    //    ethToken -> {
    //      textView_fragmentHome_tokenName.setText(ethToken.getAddress());
    //      setupChart(ethToken.getAddress().toUpperCase());
    //    });
    //
    //// get balance for token
    //Credentials credentials = WalletUtils.loadBip39Credentials(decodedPassword, decodedSeed);
    //Token mToken;
    //try {
    //  mToken = Token.load(preferencesHelper.getDefaultToken(), web3j, credentials,
    //      Constants.getGasPrice(getActivity()),
    //      Constants.DEFAULT_GAS_LIMIT);
    //} catch (Exception ex) {
    //  KLog.e(ex);
    //  return;
    //}
    //
    //Single<Uint256> balanceForToken =
    //    QueryBlockchain.balanceOf(mToken, address);
    //balanceForToken.subscribe(new SingleObserver<Uint256>() {
    //  @Override
    //  public void onSubscribe(Disposable d) {
    //  }
    //
    //  @Override
    //  public void onSuccess(Uint256 uint256) {
    //
    //    BigInteger decimals = new BigInteger("18"); //defaults to 18
    //    try {
    //      decimals = mToken.decimals().sendAsync().get().getValue();
    //    } catch (Exception e) {
    //      e.printStackTrace();
    //      KLog.e("decimals...", e.getLocalizedMessage());
    //    }
    //    BigDecimal balanceBigDecimal = new BigDecimal(uint256.getValue().toString());
    //    balanceBigDecimal = balanceBigDecimal.divide(
    //        new BigDecimal(String.valueOf(Math.pow(10, decimals.doubleValue()))));
    //    String balanceTokenFormatted = Constants.getDecimalFormat().format(balanceBigDecimal);
    //    textView_fragmentHome_balance_token.setText(balanceTokenFormatted);
    //  }
    //
    //  @Override
    //  public void onError(Throwable e) {
    //    if (e.getMessage().contains("returned a null value")) {
    //      textView_fragmentHome_balance_token.setText("0");
    //    } else {
    //      KLog.e(e);
    //    }
    //  }
    //});
  }

  @OnClick(R.id.linlayout_fragmenthome_balance_token) public void onClickLayoutToken() {

    FragmentManager fm = getActivity().getSupportFragmentManager();
    ChangeTokenDialog changeTokenDialog = new ChangeTokenDialog();
    changeTokenDialog.show(fm, "change_token_dialog");

    fm.registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
      @Override
      public void onFragmentViewDestroyed(FragmentManager fm, Fragment f) {
        super.onFragmentViewDestroyed(fm, f);
        if (f instanceof ChangeTokenDialog) {
          KLog.d(">>> update balances triggered");
          textView_fragmentHome_balance_token.setText("");
          textView_fragmentHome_tokenName.setText("");
          updateBalances(-1L);
        }
        fm.unregisterFragmentLifecycleCallbacks(this);
      }
    }, false);
  }

  private void showGreeting() {
    Calendar c = Calendar.getInstance();
    int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

    if (timeOfDay >= 2 && timeOfDay < 12) {
      textView_fragmentHome_greeting.setText("Good Morning");
    } else if (timeOfDay >= 12 && timeOfDay < 16) {
      textView_fragmentHome_greeting.setText("Good Afternoon");
    } else if (timeOfDay >= 16 && timeOfDay < 22) {
      textView_fragmentHome_greeting.setText("Good Evening");
    } else if (timeOfDay >= 22 && timeOfDay < 2) {
      textView_fragmentHome_greeting.setText("Good Night");
    }

    DateFormat formatter = new SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault());
    textView_fragmentHome_date.setText(formatter.format(c.getTime()));
  }
}