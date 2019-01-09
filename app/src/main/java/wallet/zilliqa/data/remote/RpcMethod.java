package wallet.zilliqa.data.remote;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RpcMethod {

  @SerializedName("id")
  @Expose
  private String id;
  @SerializedName("jsonrpc")
  @Expose
  private String jsonrpc;
  @SerializedName("method")
  @Expose
  private String method;
  @SerializedName("params")
  @Expose
  private List<String> params = null;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getJsonrpc() {
    return jsonrpc;
  }

  public void setJsonrpc(String jsonrpc) {
    this.jsonrpc = jsonrpc;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public List<String> getParams() {
    return params;
  }

  public void setParams(List<String> params) {
    this.params = params;
  }
}
