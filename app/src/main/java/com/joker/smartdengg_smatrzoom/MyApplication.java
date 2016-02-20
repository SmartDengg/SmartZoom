package com.joker.smartdengg_smatrzoom;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import java.util.concurrent.Executors;

/**
 * Created by Joker on 2016/2/18.
 */
public class MyApplication extends Application {

  private Picasso.Listener picassoListener = new Picasso.Listener() {
    public final String TAG = Picasso.class.getSimpleName();
    @Override public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
      Log.e(TAG, "onImageLoadFailed: " + exception.getMessage());
    }
  };

  @Override public void onCreate() {
    super.onCreate();

    Picasso picasso = new Picasso.Builder(MyApplication.this)
        .downloader(new OkHttpDownloader(new OkHttpClient()))
        .executor(Executors.newCachedThreadPool())
        .listener(picassoListener)
        .defaultBitmapConfig(Bitmap.Config.ARGB_8888)
        .build();

    Picasso.setSingletonInstance(picasso);
  }
}
