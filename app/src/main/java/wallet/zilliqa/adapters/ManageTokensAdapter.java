package wallet.zilliqa.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;
import wallet.zilliqa.R;
import wallet.zilliqa.data.local.PreferencesHelper;
import wallet.zilliqa.data.local.Wallet;

public class ManageTokensAdapter extends RecyclerView.Adapter<ManageTokensAdapter.TokenViewHolder> {

  private List<Wallet> ethTokenList;
  private Context ctx;
  private PreferencesHelper preferencesHelper;

  public ManageTokensAdapter(Context ctx, List<Wallet> ethTokenList) {
    this.ethTokenList = ethTokenList;
    this.ctx = ctx;
  }

  @Override
  public TokenViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.row_token_list, parent, false);

    preferencesHelper = new PreferencesHelper(ctx);
    return new TokenViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(TokenViewHolder holder, int position) {

    //EthToken ethToken = ethTokenList.get(position);
    //String address = ethToken.getAddress();
    //String name = ethToken.getName();
    //String symbol = ethToken.getSymbol();
    //
    //Picasso.with(ctx)
    //    .load("https://raw.githubusercontent.com/TrustWallet/tokens/master/images/" + address.toLowerCase() + ".png")
    //    .error(R.drawable.ic_launcher)
    //    .placeholder(R.drawable.ic_launcher)
    //    .into(holder.token_picture);
    //
    //holder.token_name.setText(name + " (" + symbol + ")");
    //
    //holder.cardView_token.setOnClickListener(view -> {
    //
    //  AlertDialog.Builder builder = new AlertDialog.Builder(ctx, R.style.AppCompatAlertDialogStyle);
    //  builder.setTitle("Select action for " + name);
    //  builder.setMessage("If you remove it, you can always add it back later");
    //  builder.setCancelable(true);
    //
    //  // if it's already DEFAULT, don't suggest it
    //  if (!ethToken.getAddress().equals(preferencesHelper.getDefaultToken())) {
    //    builder.setNegativeButton("SET AS DEFAULT", (dialog, which) -> {
    //      preferencesHelper.setDefaultToken(address);
    //      DialogFactory.success_toast(ctx, name + " set as default").show();
    //      dialog.dismiss();
    //    });
    //  }
    //
    //  builder.setPositiveButton("REMOVE", (dialog, which) -> {
    //
    //    // delete it from the database
    //    AppDatabase db = BaseApplication.getAppDatabase(ctx);
    //    Completable.fromAction(() -> db.tokenDao().delete(ethToken)).subscribe(() -> {
    //
    //      DialogFactory.success_toast(ctx, name + " removed").show();
    //
    //      //update UI
    //      ethTokenList.remove(position);
    //      notifyDataSetChanged();
    //
    //      //if it's the last one, set Null to default token
    //      if (getItemCount() == 0) {
    //        preferencesHelper.setDefaultToken(null);
    //      }
    //      dialog.dismiss();
    //    }, throwable -> {
    //      KLog.e(throwable);
    //      DialogFactory.error_toast(ctx, name + " failed to be removed. This should never happen :)").show();
    //      dialog.dismiss();
    //    });
    //  });
    //  AlertDialog alert = builder.create();
    //  alert.show();
    //});
  }

  @Override
  public int getItemCount() {
    return ethTokenList.size();
  }

  public class TokenViewHolder extends RecyclerView.ViewHolder {
    public ImageView token_picture;
    public TextView token_name;
    public CardView cardView_token;

    public TokenViewHolder(View view) {
      super(view);
      cardView_token = view.findViewById(R.id.cardView_token);
      token_picture = view.findViewById(R.id.token_picture);
      token_name = view.findViewById(R.id.token_name);
    }
  }
}