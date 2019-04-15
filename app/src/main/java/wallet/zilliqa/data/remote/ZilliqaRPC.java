package wallet.zilliqa.data.remote;

import android.content.Context;
import com.google.gson.JsonObject;
import java.util.concurrent.TimeUnit;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;
import wallet.zilliqa.BuildConfig;
import wallet.zilliqa.Constants;

public interface ZilliqaRPC {

  @POST("/") Call<JsonObject> executeRPCCall(@Body RpcMethod rpcMethod);

  class Factory {
    public static ZilliqaRPC service;

    public static ZilliqaRPC getInstance(Context context) {
      if (service == null) {

        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        builder.readTimeout(25, TimeUnit.SECONDS);
        builder.connectTimeout(20, TimeUnit.SECONDS);
        builder.writeTimeout(20, TimeUnit.SECONDS);

        if (BuildConfig.DEBUG) {
          HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
          interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
          builder.addInterceptor(interceptor);
        }

        int cacheSize = 1024 * 1024; // 1 MiB
        Cache cache = new Cache(context.getCacheDir(), cacheSize);
        builder.cache(cache);

        Retrofit retrofit =
            new Retrofit.Builder().client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(Constants.getNetworkAPIURL(context))
                .build();
        service = retrofit.create(ZilliqaRPC.class);
        return service;
      } else {
        return service;
      }
    }
  }
}
