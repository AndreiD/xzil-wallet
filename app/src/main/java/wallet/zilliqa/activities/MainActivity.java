package wallet.zilliqa.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.roughike.bottombar.BottomBar;
import com.socks.library.KLog;
import hotchemi.android.rate.AppRate;
import wallet.zilliqa.BaseActivity;
import wallet.zilliqa.R;
import wallet.zilliqa.fragments.HomeFragment;
import wallet.zilliqa.fragments.ManageTokensFragment;
import wallet.zilliqa.fragments.ReceiveFragment;
import wallet.zilliqa.fragments.SendFragment;
import wallet.zilliqa.utils.DUtils;
import wallet.zilliqa.utils.DialogFactory;

public class MainActivity extends BaseActivity {

  @BindView(R.id.relayout_main) RelativeLayout relayout_main;
  @BindView(R.id.theToolbar) Toolbar theToolbar;
  @BindView(R.id.bottomBar) BottomBar bottomBar;
  private MainActivity mContext;
  private boolean isjustcreated = false;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    setSupportActionBar(theToolbar);
    getSupportActionBar().setElevation(0);
    mContext = MainActivity.this;

    Bundle bundle = getIntent().getExtras();
    if ((bundle != null) && bundle.containsKey("isjustcreated")) {
      isjustcreated = bundle.getBoolean("isjustcreated", false);
    }

    bottomBar.setDefaultTab(R.id.tab_home);
    bottomBar.setOnTabSelectListener(tabId -> {
      if (tabId == R.id.tab_send) {
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.contentContainer, SendFragment.newInstance(isjustcreated));
        t.addToBackStack("fragment_send");
        t.commit();
      }
      if (tabId == R.id.tab_home) {

        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.contentContainer, HomeFragment.newInstance(isjustcreated));
        t.addToBackStack("fragment_home");
        t.commit();

        if (DUtils.getVisibleFragment(MainActivity.this) instanceof HomeFragment) {
          KLog.d(">> it's home fragment...");
        } else {
          KLog.d(">> it's NOT home fragment...");
        }
      }
      if (tabId == R.id.tab_receive) {
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.contentContainer, ReceiveFragment.newInstance());
        t.addToBackStack("fragment_receive");
        t.commit();
      }
    });

    // disabled until it makes sense
    //rate_this_app_logic();
  }

  private void rate_this_app_logic() {
    AppRate.with(this)
        .setInstallDays(5)
        .setLaunchTimes(5)
        .setRemindInterval(2)
        .setShowLaterButton(true)
        .setDebug(false)
        .monitor();
    AppRate.showRateDialogIfMeetsConditions(this);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_add:
        DialogFactory.warning_toast(mContext,"todo....").show();
        break;
      case R.id.action_manage_tokens:
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.contentContainer, new ManageTokensFragment());
        ft.addToBackStack("fragment_manage_tokens");
        ft.commit();
        break;
      case R.id.action_exit:
        finish();
        break;
      case R.id.action_settings:
        startActivity(new Intent(mContext, SettingsActivity.class));
        break;
      default:
        break;
    }
    return true;
  }
}


