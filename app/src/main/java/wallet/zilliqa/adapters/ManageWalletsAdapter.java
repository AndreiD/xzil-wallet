package wallet.zilliqa.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.socks.library.KLog;
import io.reactivex.Completable;
import java.util.List;
import wallet.zilliqa.BaseApplication;
import wallet.zilliqa.R;
import wallet.zilliqa.activities.MainActivity;
import wallet.zilliqa.data.local.AppDatabase;
import wallet.zilliqa.data.local.PreferencesHelper;
import wallet.zilliqa.data.local.Wallet;
import wallet.zilliqa.utils.BlockiesIdenticon;
import wallet.zilliqa.utils.DialogFactory;

public class ManageWalletsAdapter extends RecyclerView.Adapter<ManageWalletsAdapter.TokenViewHolder> {

  private final List<Wallet> walletList;
  private final Context ctx;
  private PreferencesHelper preferencesHelper;

  public ManageWalletsAdapter(Context ctx, List<Wallet> walletList) {
    this.walletList = walletList;
    this.ctx = ctx;
  }

  @NonNull @Override
  public TokenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View itemView = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.row_wallet_list, parent, false);

    preferencesHelper = new PreferencesHelper(ctx);
    String defaultAddress = preferencesHelper.getDefaulAddress();
    return new TokenViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(@NonNull TokenViewHolder holder, int position) {

    Wallet wallet = walletList.get(position);
    String address = wallet.getAddress();

    holder.wallet_address.setText(address);
    holder.identicon.setAddress(address);
    holder.cardView_wallet.setOnClickListener(view -> {

      AlertDialog.Builder builder = new AlertDialog.Builder(ctx, R.style.AppCompatAlertDialogStyle);
      builder.setTitle("Select action for this address");
      builder.setMessage("If you remove it, you can always add it back later");
      builder.setCancelable(true);

      // if it's already DEFAULT, don't suggest it
      if (!address.equals(preferencesHelper.getDefaulAddress())) {
        builder.setNegativeButton("SET AS DEFAULT", (dialog, which) -> {
          preferencesHelper.setDefaultAddress(address);
          DialogFactory.success_toast(ctx, "address set as default").show();
          dialog.dismiss();
          ((MainActivity) ctx).getSupportFragmentManager().popBackStack();
        });
      }

      builder.setPositiveButton("REMOVE", (dialog, which) -> {

        if (address.equals(preferencesHelper.getDefaulAddress())) {
          DialogFactory.error_toast(ctx, "cannot remove your default address").show();
          return;
        }

        // delete it from the database
        AppDatabase db = BaseApplication.getAppDatabase(ctx);
        Completable.fromAction(() -> db.walletDao().delete(wallet)).subscribe(() -> {
          DialogFactory.success_toast(ctx, "address removed").show();

          //update UI
          walletList.remove(position);
          notifyDataSetChanged();

          //if it's the last one, set Null to default token
          if (getItemCount() == 0) {
            preferencesHelper.setDefaultAddress(null);
          }
          dialog.dismiss();
        }, throwable -> {
          KLog.e(throwable);
          DialogFactory.error_toast(ctx, address + " failed to be removed. This should never happen :)").show();
          dialog.dismiss();
        });
      });
      AlertDialog alert = builder.create();
      alert.show();
    });
  }

  @Override
  public int getItemCount() {
    return walletList.size();
  }

  public class TokenViewHolder extends RecyclerView.ViewHolder {
    public final TextView wallet_address;
    public final CardView cardView_wallet;
    public final BlockiesIdenticon identicon;

    public TokenViewHolder(View view) {
      super(view);
      cardView_wallet = view.findViewById(R.id.cardView_wallet);
      wallet_address = view.findViewById(R.id.wallet_address);
      identicon = view.findViewById(R.id.identicon);
    }
  }
}