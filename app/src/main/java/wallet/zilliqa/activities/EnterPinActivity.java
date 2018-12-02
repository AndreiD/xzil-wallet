package wallet.zilliqa.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import wallet.zilliqa.BaseActivity;
import wallet.zilliqa.BuildConfig;
import wallet.zilliqa.R;
import wallet.zilliqa.data.local.PreferencesHelper;
import wallet.zilliqa.fragments.NewAccountFragment;
import wallet.zilliqa.utils.Cryptography;
import wallet.zilliqa.utils.DUtils;
import wallet.zilliqa.utils.DialogFactory;

public class EnterPinActivity extends BaseActivity {

  @BindView(R.id.editText_pin1) EditText editText_pin1;

  @BindView(R.id.btn_verify_pin) Button btn_verify_pin;

  @BindView(R.id.textView_forgot_pin) TextView textView_forgot_pin;

  private EnterPinActivity mContext;
  private ProgressDialog progressDialog;
  private Cryptography cryptography;
  private PreferencesHelper preferencesHelper;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_enter_pin);
    ButterKnife.bind(this);
    mContext = EnterPinActivity.this;

    cryptography = new Cryptography(EnterPinActivity.this);
    preferencesHelper = new PreferencesHelper(mContext);

    if (BuildConfig.DEBUG) {
      editText_pin1.setText("123123");
      btn_verify_pin.performClick();
    }
  }

  @OnClick(R.id.btn_verify_pin) public void onClickSaveVerify() {

    if (editText_pin1.getText().toString().length() < 3) {
      DialogFactory.error_toast(EnterPinActivity.this, "Pin should be at least 4 numbers").show();
      return;
    }

    // makes it faster for debug builds
    long defaultInterval = 1000L;
    if (BuildConfig.DEBUG) {
      defaultInterval = 1L;
    }

    int invalidPins = preferencesHelper.getInvalidPins();
    defaultInterval = invalidPins * defaultInterval + 1;

    if (invalidPins > 10) {
      DialogFactory.createGenericErrorDialog(mContext,
          "Attention! The application data was wiped. Please import your seed or create a new wallet.")
          .show();
      preferencesHelper.clear();
      return;
    }

    progressDialog =
        DialogFactory.createProgressDialog(EnterPinActivity.this,
            "Unlocking Application...");
    progressDialog.show();
    progressDialog.setCancelable(false);

    // add a delay to prevent brute forcing
    new CountDownTimer(defaultInterval, defaultInterval) {
      @Override public void onTick(long l) {
      }

      @Override public void onFinish() {

        if (progressDialog != null && progressDialog.isShowing()) {
          progressDialog.dismiss();
        }

        //Check the pin
        String enteredPIN = editText_pin1.getText().toString();
        DUtils.hideKeyboard(EnterPinActivity.this);
        String encryptedPIN = preferencesHelper.getPIN();

        try {
          String decryptedPIN = cryptography.decryptData(encryptedPIN);
          if (decryptedPIN.equals(enteredPIN)) {
            preferencesHelper.setInvalidPins(0);

            //check if the wallet was created
            String encAddress = preferencesHelper.getAddress();
            if (encAddress == null) {
              FragmentManager fragmentManager = getSupportFragmentManager();
              fragmentManager.beginTransaction()
                  .replace(android.R.id.content, NewAccountFragment.newInstance())
                  .addToBackStack(null)
                  .commit();
            } else {
              startActivity(new Intent(EnterPinActivity.this, MainActivity.class));
            }
          } else {

            if (preferencesHelper.getInvalidPins() > 4) {
              finish();
            }

            DialogFactory.error_toast(mContext, "Incorrect PIN").show();
            preferencesHelper.setInvalidPins(preferencesHelper.getInvalidPins() + 1);

            if (preferencesHelper.getInvalidPins() == 3) {
              DialogFactory.createGenericErrorDialog(mContext,
                  "Attention! If you enter more than 10 times an incorrect pin the application will reset. After that the only way to get your money is by importing your seed.")
                  .show();
            }
          }
        } catch (NoSuchPaddingException | NoSuchAlgorithmException |
            UnrecoverableEntryException | CertificateException | KeyStoreException |
            IOException | InvalidAlgorithmParameterException | InvalidKeyException |
            NoSuchProviderException | BadPaddingException | IllegalBlockSizeException e) {
          e.printStackTrace();
          DialogFactory.createGenericErrorDialog(getApplication(), e.getLocalizedMessage()).show();
        }
      }
    }.start();
  }

  @OnClick(R.id.textView_forgot_pin) public void onClickForgotPin() {

    AlertDialog.Builder builder =
        new AlertDialog.Builder(EnterPinActivity.this, R.style.AppCompatAlertDialogErrorStyle);
    builder.setTitle("Attention");
    builder.setMessage(
        "You cannot recover a PIN. Create a new PIN & import your account again using your SEED & PASSPHRASE.");
    builder.setCancelable(false);
    builder.setNegativeButton(android.R.string.cancel,
        (dialog, id) -> dialog.cancel());
    builder.setPositiveButton("RESET PIN", (dialogInterface, i) -> {
      PreferencesHelper preferencesHelper = new PreferencesHelper(EnterPinActivity.this);
      preferencesHelper.clear();
      Toast.makeText(EnterPinActivity.this, "Please restart the app.", Toast.LENGTH_LONG).show();
      finish();
    });
    AlertDialog alertDialog = builder.create();
    alertDialog.show();
  }
}

