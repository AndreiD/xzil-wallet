package wallet.zilliqa.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import wallet.zilliqa.BaseActivity;
import wallet.zilliqa.R;

public class GetFreeZILs extends BaseActivity {

  @BindView(R.id.freezil_open_facebook_button) Button freezil_open_facebook_button;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_get_free_zils);

    ButterKnife.bind(this);
  }

  @OnClick(R.id.freezil_open_facebook_button) public void getFreeZils() {
    String url = "https://www.facebook.com/XZIL-Wallet-1526900810780431/";
    Intent i = new Intent(Intent.ACTION_VIEW);
    i.setData(Uri.parse(url));
    startActivity(i);
  }
}
