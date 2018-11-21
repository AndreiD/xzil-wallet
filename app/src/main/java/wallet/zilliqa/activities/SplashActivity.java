package wallet.zilliqa.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import wallet.zilliqa.BaseActivity;
import wallet.zilliqa.R;

public class SplashActivity extends BaseActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);


  }


}
