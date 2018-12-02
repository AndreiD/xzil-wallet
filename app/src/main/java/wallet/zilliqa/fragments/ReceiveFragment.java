package wallet.zilliqa.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Hashtable;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import wallet.zilliqa.BaseApplication;
import wallet.zilliqa.BaseFragment;
import wallet.zilliqa.BuildConfig;
import wallet.zilliqa.Constants;
import wallet.zilliqa.R;
import wallet.zilliqa.data.local.AppDatabase;
import wallet.zilliqa.data.local.Wallet;
import wallet.zilliqa.data.local.PreferencesHelper;
import wallet.zilliqa.utils.Cryptography;
import wallet.zilliqa.utils.DUtils;
import wallet.zilliqa.utils.DialogFactory;
import wallet.zilliqa.utils.MyClipboardManager;

public class ReceiveFragment extends BaseFragment {

  @BindView(R.id.btn_receive_share)
  Button btn_receive_share;
  @BindView(R.id.btn_copy_clipboard)
  Button btn_copy_clipboard;
  @BindView(R.id.imageView_qrcode_receive)
  ImageView imageView_qrcode_receive;
  @BindView(R.id.textView_receive_ethaddress)
  TextView textView_receive_ethaddress;

  public ReceiveFragment() {
  }

  public static ReceiveFragment newInstance() {
    ReceiveFragment fragment = new ReceiveFragment();
    fragment.setRetainInstance(true);
    return fragment;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_receive, container, false);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    try {

      PreferencesHelper preferencesHelper = new PreferencesHelper(getActivity());
      String encAddress = preferencesHelper.getAddress();

      Cryptography cryptography = new Cryptography(getActivity());
      String address = cryptography.decryptData(encAddress);
      textView_receive_ethaddress.setText(address);
      Bitmap qrCode = generateQrCode(address);
      imageView_qrcode_receive.setImageBitmap(qrCode);
    } catch (IOException | WriterException | InvalidKeyException | UnrecoverableEntryException | KeyStoreException | NoSuchPaddingException | NoSuchAlgorithmException | NoSuchProviderException | CertificateException | IllegalBlockSizeException | InvalidAlgorithmParameterException | BadPaddingException e) {
      e.printStackTrace();
      DialogFactory.createGenericErrorDialog(getActivity(),
          "Could not load your wallet. Please import it again using your saved mnemonic").show();
    }
  }

  @OnClick(R.id.btn_receive_share)
  public void onClickBtnShare() {

    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
    sharingIntent.setType("text/plain");
    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Please send crypto to this address");
    sharingIntent.putExtra(Intent.EXTRA_TEXT, textView_receive_ethaddress.getText().toString());
    startActivity(
        Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)));
  }

  @OnClick(R.id.btn_copy_clipboard)
  public void onClickCopyClipboaord() {
    android.content.ClipboardManager clipboard =
        (android.content.ClipboardManager) getActivity().getSystemService(
            Context.CLIPBOARD_SERVICE);
    android.content.ClipData clip = android.content.ClipData.newPlainText("eth address",
        textView_receive_ethaddress.getText().toString());
    clipboard.setPrimaryClip(clip);
    DialogFactory.simple_toast(getActivity(), "Address copied to clipboard").show();
  }

  public static Bitmap generateQrCode(String inputValue) throws WriterException {
    Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<>();
    hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // H = 30% damage

    QRCodeWriter qrCodeWriter = new QRCodeWriter();

    int size = 350;

    BitMatrix bitMatrix = qrCodeWriter.encode(inputValue, BarcodeFormat.QR_CODE, size, size);
    int width = bitMatrix.getWidth();
    int height = bitMatrix.getHeight();
    int[] pixels = new int[width * height];
    for (int y = 0; y < height; y++) {
      int offset = y * width;
      for (int x = 0; x < width; x++) {
        pixels[offset + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
      }
    }

    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
    return bitmap;
  }
}