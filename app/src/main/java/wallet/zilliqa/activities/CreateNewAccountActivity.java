package wallet.zilliqa.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.firestack.laksaj.crypto.KeyTools;
import com.firestack.laksaj.utils.ByteUtil;
import com.socks.library.KLog;
import java.io.IOException;
import java.math.BigInteger;
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
import org.web3j.crypto.ECKeyPair;
import wallet.zilliqa.BaseActivity;
import wallet.zilliqa.BaseApplication;
import wallet.zilliqa.R;
import wallet.zilliqa.data.local.AppDatabase;
import wallet.zilliqa.data.local.PreferencesHelper;
import wallet.zilliqa.data.local.Wallet;
import wallet.zilliqa.utils.AndroidKeyTool;
import wallet.zilliqa.utils.Cryptography;
import wallet.zilliqa.utils.DialogFactory;

public class CreateNewAccountActivity extends BaseActivity {

  @BindView(R.id.textView_private_key) TextView textView_private_key;
  @BindView(R.id.checkBox_seed) CheckBox checkBox_seed;
  @BindView(R.id.btn_private_key_clipboard) Button btn_seed_clipboard;
  @BindView(R.id.btn_continue) Button btn_seed_continue;
  @BindView(R.id.linLayout_new_account_all) LinearLayout linLayout_new_account_all;

  private CreateNewAccountActivity mContext;
  private Cryptography cryptography;
  private PreferencesHelper preferencesHelper;
  private String address;
  private String privateKey;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_new_account);
    ButterKnife.bind(this);

    mContext = CreateNewAccountActivity.this;

    cryptography = new Cryptography(mContext);
    preferencesHelper = new PreferencesHelper(mContext);

    linLayout_new_account_all.setVisibility(View.INVISIBLE);

    try {
      ECKeyPair keyPair = AndroidKeyTool.generateKeyPair();
      BigInteger privateInteger = keyPair.getPrivateKey();
      BigInteger publicInteger = keyPair.getPublicKey();

      address = KeyTools.getAddressFromPublicKey(ByteUtil.byteArrayToHexString(publicInteger.toByteArray()));
      privateKey = ByteUtil.byteArrayToHexString(privateInteger.toByteArray());

      KLog.d("private key is: " + privateKey);
      KLog.d("address is: " + address);

      textView_private_key.setText(privateKey);
      linLayout_new_account_all.setVisibility(View.VISIBLE);
    } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
      e.printStackTrace();
    }
  }

  @OnClick(R.id.btn_private_key_clipboard) public void onClickCopyToClipboard() {
    android.content.ClipboardManager clipboard =
        (android.content.ClipboardManager) mContext.getSystemService(
            Context.CLIPBOARD_SERVICE);
    android.content.ClipData clip = android.content.ClipData.newPlainText("", textView_private_key.getText().toString());
    if (clipboard != null) {
      clipboard.setPrimaryClip(clip);
      DialogFactory.simple_toast(mContext, "copied to clipboard").show();
    }
  }

  @OnClick(R.id.btn_continue) public void onClickContinue() {
    if (!checkBox_seed.isChecked()) {
      checkBox_seed.setTextColor(getResources().getColor(R.color.material_red));
      DialogFactory.error_toast(mContext, "Please check that you stored the seed safely!")
          .show();
    } else {

      // encrypt the private key &  stores it encrypted
      Cryptography cryptography = new Cryptography(getApplication());
      try {
        String encryptedPrivateKey = cryptography.encryptData(privateKey);

        AppDatabase appDatabase = BaseApplication.getAppDatabase(mContext);
        appDatabase.walletDao().insertAll(new Wallet(address, encryptedPrivateKey));

        //set it as default
        preferencesHelper.setDefaultAddress(address);

        KLog.d(">>> new wallet with address " + address + " stored in the db");
      } catch (NoSuchPaddingException | NoSuchAlgorithmException |
          UnrecoverableEntryException | CertificateException | KeyStoreException |
          IOException | InvalidAlgorithmParameterException | InvalidKeyException |
          NoSuchProviderException | BadPaddingException | IllegalBlockSizeException e) {
        e.printStackTrace();
        DialogFactory.error_toast(mContext, e.getLocalizedMessage()).show();
        return;
      }

      Intent intent = new Intent(mContext, MainActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(intent);
    }
  }
}
