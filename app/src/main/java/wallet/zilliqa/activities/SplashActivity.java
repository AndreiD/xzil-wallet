package wallet.zilliqa.activities;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import com.crashlytics.android.Crashlytics;
import com.socks.library.KLog;
import java.util.List;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import wallet.zilliqa.BaseActivity;
import wallet.zilliqa.BuildConfig;
import wallet.zilliqa.R;
import wallet.zilliqa.data.local.PreferencesHelper;
import wallet.zilliqa.utils.DUtils;
import wallet.zilliqa.utils.DialogFactory;

public class SplashActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks {

  private static final String[] APP_PERMISSIONS =
      { Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE };
  private static final int RC_APP_PERMISSIONS = 1337;
  private AppCompatImageView imageView_logo;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_splash);

    imageView_logo = findViewById(R.id.imageView_logo);

    // get phone model for crash reporting
    Crashlytics.setUserIdentifier(DUtils.getDeviceInfo());

    if (BuildConfig.DEBUG) {
      appPermissionsRequest();
      return;
    }

    Animation fadeIn = new AlphaAnimation(0, 1);
    fadeIn.setInterpolator(new DecelerateInterpolator());
    fadeIn.setDuration(300);

    Animation fadeOut = new AlphaAnimation(1, 0);
    fadeOut.setInterpolator(new AccelerateInterpolator());
    fadeOut.setDuration(300);

    fadeIn.setAnimationListener(new Animation.AnimationListener() {
      @Override
      public void onAnimationStart(Animation animation) {

      }

      @Override
      public void onAnimationEnd(Animation animation) {
        imageView_logo.setVisibility(View.VISIBLE);
        new CountDownTimer(600, 600) {

          @Override
          public void onTick(long l) {

          }

          @Override
          public void onFinish() {
            imageView_logo.startAnimation(fadeOut);
          }
        }.start();
      }

      @Override
      public void onAnimationRepeat(Animation animation) {

      }
    });

    fadeOut.setAnimationListener(new Animation.AnimationListener() {
      @Override
      public void onAnimationStart(Animation animation) {

      }

      @Override
      public void onAnimationEnd(Animation animation) {
        imageView_logo.setVisibility(View.INVISIBLE);
        //checkNews();
        appPermissionsRequest();
      }

      @Override
      public void onAnimationRepeat(Animation animation) {

      }
    });

    new CountDownTimer(200, 200) {
      @Override
      public void onTick(long l) {
      }

      @Override
      public void onFinish() {
        imageView_logo.startAnimation(fadeIn);
      }
    }.start();
  }

  @AfterPermissionGranted(RC_APP_PERMISSIONS)
  public void appPermissionsRequest() {
    if (hasRequiredPermissions()) {
      check_confirmed_tos();
    } else {

      AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this, R.style.AppCompatAlertDialogStyle);
      builder.setTitle(getString(R.string.before_we_start));
      builder.setMessage(getString(R.string.permissions_are_needed));
      builder.setCancelable(false);
      builder.setPositiveButton(getString(android.R.string.ok), (dialog, which) -> {
        dialog.dismiss();
        EasyPermissions.requestPermissions(this, getString(R.string.permissions_are_needed),
            RC_APP_PERMISSIONS, APP_PERMISSIONS);
      });
      AlertDialog alert = builder.create();
      alert.show();
    }
  }

  private void check_confirmed_tos() {

    //terms and agreements
    PreferencesHelper preferencesHelper = new PreferencesHelper(SplashActivity.this);
    if (!preferencesHelper.getConfirmedTos()) {
      //show tos dialog

      new AlertDialog.Builder(SplashActivity.this, R.style.AppCompatAlertDialogStyle)
          .setTitle("Terms & Conditions")
          .setPositiveButton("I Agree", (dialog, which) -> {
            preferencesHelper.setConfirmedTos(true);
            dialog.dismiss();
            check_pin_present();
          })
          .setNegativeButton("Cancel", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            finish();
          })
          .setMessage(getString(R.string.tos_terms))
          .show();
    } else {
      check_pin_present();
    }
  }

  private void check_pin_present() {

    PreferencesHelper preferencesHelper = new PreferencesHelper(SplashActivity.this);

    if (!preferencesHelper.getPinCreated()) {
      KLog.d("we don't have a PIN");
      startActivity(new Intent(this, NewPinActivity.class));
    } else {
      KLog.d("a PIN is already present");
      startActivity(new Intent(this, EnterPinActivity.class));
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    // EasyPermissions handles the request result.
    EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
  }

  //we have 2 permissions in total.
  @Override
  public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
    if (perms.size() >= 2) {
      Log.d("permissions", "all permissions were granted");
      check_confirmed_tos();
    } else {
      Log.w("permissions", "not all permissions were granted!");
    }
  }

  @Override
  public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
    Log.e("onPermissionsDenied",
        perms.toString() + " >>> " + requestCode + ":" + perms.size());

    DialogFactory.simple_toast(SplashActivity.this, getString(R.string.permissions_are_needed))
        .show();

    if (EasyPermissions.somePermissionPermanentlyDenied(SplashActivity.this, perms)) {
      new AppSettingsDialog.Builder(SplashActivity.this).build().show();
    } else {
      finish();
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
      appPermissionsRequest();
    }
  }

  // returns false if a permission from the list fails.
  private boolean hasRequiredPermissions() {
    return EasyPermissions.hasPermissions(SplashActivity.this, APP_PERMISSIONS);
  }
}
