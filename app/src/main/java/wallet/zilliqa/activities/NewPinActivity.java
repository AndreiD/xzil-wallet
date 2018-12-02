package wallet.zilliqa.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.widget.Button;
import android.widget.EditText;
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
import wallet.zilliqa.R;
import wallet.zilliqa.data.local.PreferencesHelper;
import wallet.zilliqa.fragments.NewAccountFragment;
import wallet.zilliqa.utils.Cryptography;
import wallet.zilliqa.utils.DUtils;
import wallet.zilliqa.utils.DialogFactory;

public class NewPinActivity extends BaseActivity {

  @BindView(R.id.editText_pin1) EditText editText_pin1;

  @BindView(R.id.editText_pin2) EditText editText_pin2;

  @BindView(R.id.btn_save_new_pin) Button btn_save_new_pin;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_create_pin);
    ButterKnife.bind(this);
  }

  @OnClick(R.id.btn_save_new_pin) public void onClickSaveNewPin() {
    String pin1 = editText_pin1.getText().toString();
    String pin2 = editText_pin2.getText().toString();
    if (!pin1.equals(pin2)) {
      DialogFactory.error_toast(NewPinActivity.this, "The PIN codes you entered do not match.")
          .show();
      return;
    }
    if (pin1.length() < 3) {
      DialogFactory.error_toast(NewPinActivity.this, "PIN is too short. Minimum 4 characters")
          .show();
      return;
    }

    DUtils.hideKeyboard(NewPinActivity.this);


    // saves it to shared preferences
    PreferencesHelper preferencesHelper = new PreferencesHelper(NewPinActivity.this);

    //deletes the previous app data
    preferencesHelper.clear();

    preferencesHelper.setPinCreated(true);

    // stores it encrypted
    Cryptography cryptography = new Cryptography(getApplication());
    try {
      String encrypted = cryptography.encryptData(pin1);
      preferencesHelper.setPIN(encrypted);
    } catch (NoSuchPaddingException | NoSuchAlgorithmException |
        UnrecoverableEntryException | CertificateException | KeyStoreException |
        IOException | InvalidAlgorithmParameterException | InvalidKeyException |
        NoSuchProviderException | BadPaddingException | IllegalBlockSizeException e) {
      e.printStackTrace();
      DialogFactory.createGenericErrorDialog(getApplication(), e.getLocalizedMessage()).show();
    }

    FragmentManager fragmentManager = getSupportFragmentManager();
    fragmentManager.beginTransaction()
        .replace(android.R.id.content, NewAccountFragment.newInstance())
        .addToBackStack(null)
        .commit();
  }
}
