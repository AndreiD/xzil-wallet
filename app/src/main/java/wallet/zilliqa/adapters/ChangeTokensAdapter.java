package wallet.zilliqa.adapters;

import android.content.Context;
import android.support.v4.app.DialogFragment;
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

public class ChangeTokensAdapter extends RecyclerView.Adapter<ChangeTokensAdapter.ChangeTokenViewHolder> {

  private final DialogFragment dialogFragment;
  private List<Wallet> walletList;
  private Context ctx;
  private PreferencesHelper preferencesHelper;

  public ChangeTokensAdapter(Context ctx, DialogFragment dialogFragment, List<Wallet> walletList) {
    this.walletList = walletList;
    this.ctx = ctx;
    this.dialogFragment = dialogFragment;
  }
  //
  @Override
  public ChangeTokenViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.row_token_list, parent, false);

    preferencesHelper = new PreferencesHelper(ctx);
    return new ChangeTokenViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(ChangeTokenViewHolder holder, int position) {

    //String address = ethTokenList.get(position).getAddress();
    //String name = ethTokenList.get(position).getName();
    //String symbol = ethTokenList.get(position).getSymbol();
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
    //  KLog.d(">>> selected >>> " + holder.token_name);
    //  preferencesHelper.setDefaultToken(address);
    //  dialogFragment.dismiss();
    //});
  }

  @Override
  public int getItemCount() {
    return walletList.size();
  }

  public class ChangeTokenViewHolder extends RecyclerView.ViewHolder {
    ImageView token_picture;
    public TextView token_name;
    public CardView cardView_token;

    public ChangeTokenViewHolder(View view) {
      super(view);
      cardView_token = view.findViewById(R.id.cardView_token);
      token_picture = view.findViewById(R.id.token_picture);
      token_name = view.findViewById(R.id.token_name);
    }
  }
}