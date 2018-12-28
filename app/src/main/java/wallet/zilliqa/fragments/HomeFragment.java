package wallet.zilliqa.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;
import butterknife.BindView;
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
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import wallet.zilliqa.BaseApplication;
import wallet.zilliqa.BaseFragment;
import wallet.zilliqa.R;
import wallet.zilliqa.data.local.PreferencesHelper;
import wallet.zilliqa.data.remote.ExchangeRatesAPI;
import wallet.zilliqa.utils.BlockiesIdenticon;
import wallet.zilliqa.utils.Convert;

public class HomeFragment extends BaseFragment {

  @BindView(R.id.textView_fragmentHome_balance_zil) TextView textView_fragmentHome_balance_zil;
  @BindView(R.id.textView_fragmentHome_status) TextView textView_fragmentHome_status;
  @BindView(R.id.textView_fragmentHome_greeting) TextView textView_fragmentHome_greeting;
  @BindView(R.id.textView_fragmentHome_date) TextView textView_fragmentHome_date;
  @BindView(R.id.home_line_chart) LineChart home_line_chart;
  @BindView(R.id.identicon_home) BlockiesIdenticon identicon_home;
  @BindView(R.id.home_webview) WebView theWebView;
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

    theWebView.getSettings().setJavaScriptEnabled(true);
    theWebView.getSettings().setAppCacheEnabled(false);
    theWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
    theWebView.setBackgroundColor(Color.TRANSPARENT);
    theWebView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);

    theWebView.addJavascriptInterface(new WebAppInterface(getActivity()), "Android");
    theWebView.loadUrl("file:///android_asset/javascript/balance.html");

    textView_fragmentHome_balance_zil.setVisibility(View.GONE);
    textView_fragmentHome_status.setText("Updating...");
    showGreeting();

    boolean isjustcreated = getArguments().getBoolean("isjustcreated", false);

    if (isjustcreated) {
      textView_fragmentHome_status.setText("Thank you for creating a wallet with us.");
    }

    setupChart();
  }

  private void setupChart() {
    home_line_chart.setDrawGridBackground(false);
    home_line_chart.getDescription().setEnabled(false);
    home_line_chart.setTouchEnabled(false);
    home_line_chart.setDragEnabled(false);
    home_line_chart.setScaleEnabled(false);
    home_line_chart.setPinchZoom(false);
    home_line_chart.setBackgroundColor(Color.TRANSPARENT);
    home_line_chart.setDrawGridBackground(false);
    home_line_chart.setDrawBorders(false);

    ArrayList<Entry> values = new ArrayList<>();

    ExchangeRatesAPI exchangeRatesAPI = ExchangeRatesAPI.Factory.getIstance(getActivity());

    exchangeRatesAPI.getGraphData("ZIL").enqueue(new Callback<JsonObject>() {
      @Override public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
        if (response.code() > 299) {
          KLog.e("failed to get historical data for the chart");
          home_line_chart.setVisibility(View.GONE);
          return;
        }

        JsonObject jsonObject = response.body();

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

        LineDataSet ethToUsdLine = new LineDataSet(values, "ZIL (ERC-20) - last 3 months");
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

    identicon_home.setAddress(preferencesHelper.getDefaulAddress());
    disposable = Observable.interval(500, 10000,
        TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::updateBalances);
  }

  @Override public void onPause() {
    super.onPause();
    try {
      disposable.dispose();
    } catch (Exception ignored) {
    }
  }

  private void updateBalances(Long aLong) {
    theWebView.loadUrl("javascript:getBalance(\"" + preferencesHelper.getDefaulAddress() + "\")");
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

  private class WebAppInterface {
    Context mContext;

    WebAppInterface(Context c) {
      mContext = c;
    }

    @JavascriptInterface
    public void balance(String balance) {

      getActivity().runOnUiThread(() -> {
        textView_fragmentHome_status.setText("all looks good.");
        textView_fragmentHome_balance_zil.setVisibility(View.VISIBLE);
        if(balance.contains("undefined")){
          textView_fragmentHome_balance_zil.setText("0 ZIL");
        }else {
          BigDecimal balanceZil = Convert.fromQa(balance, Convert.Unit.ZIL);
          textView_fragmentHome_balance_zil.setText(balanceZil.toString() + " ZIL");
        }
      });

    }
  }
}