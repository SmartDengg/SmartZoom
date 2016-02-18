package com.joker.smartdengg_smatrzoom;

import android.app.Application;

import android.graphics.Bitmap;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import java.util.concurrent.Executors;

/**
 * Created by Joker on 2016/2/18.
 */
public class MyApplication extends Application {

  @Override public void onCreate() {
    super.onCreate();

    Picasso picasso = new Picasso.Builder(MyApplication.this).downloader(new OkHttpDownloader(new OkHttpClient()))
                                                             .executor(Executors.newCachedThreadPool())
                                                             .defaultBitmapConfig(Bitmap.Config.ARGB_8888)
                                                             .build();

    Picasso.setSingletonInstance(picasso);
  }
}
