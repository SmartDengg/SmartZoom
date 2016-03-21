package com.joker.smartdengg_smatrzoom.activity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.joker.smartdengg_smatrzoom.R;
import com.joker.smartdengg_smatrzoom.transformation.CropCircleTransformation;
import com.joker.smartdengg_smatrzoom.util.BestBlur;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

  protected static final int BLUR_RADIUS = 10;
  protected static final float BLUR_DESATURATE = 0.0f;
  protected static final int BLUR_SCALE = BLUR_RADIUS / 3;

  @BindString(R.string.app_name) protected String Title;

  @NonNull @Bind(R.id.main_layout_root_view) protected RelativeLayout rootView;
  @NonNull @Bind(R.id.main_layout_toolbar) protected Toolbar toolbar;
  @NonNull @Bind(R.id.main_layout_profile_iv) protected ImageView profileIv;
  @NonNull @Bind(R.id.main_layout_viewstub) protected ViewStub viewStub;

  private Point globalOffset = null;

  private View inflate;

  private boolean isDone;

  private Callback.EmptyCallback picassoCallback = new Callback.EmptyCallback() {
    @Override public void onSuccess() {
      MainActivity.this.isDone = true;
    }
  };

  private ViewStub.OnInflateListener inflateListener = new ViewStub.OnInflateListener() {
    @Override public void onInflate(ViewStub stub, View inflated) {

      if (profileIv != null) MainActivity.this.navigateToHero(inflated);
    }
  };

  private void navigateToHero(View inflated) {

    this.profileIv.setVisibility(View.GONE);

    BestBlur bestBlur = new BestBlur(MainActivity.this);
    Bitmap screen = MainActivity.this.catchScreen();

    Bitmap screenBitmap =
        Bitmap.createScaledBitmap(screen, screen.getWidth() / BLUR_SCALE, screen.getHeight() / BLUR_SCALE, true);
    if (screenBitmap != null) screen.recycle();

    Bitmap blurBitmap = bestBlur.blurBitmap(screenBitmap, BLUR_RADIUS, BLUR_DESATURATE);
    ((ImageView) inflated).setImageBitmap(blurBitmap);
    bestBlur.destroy();

    Rect startBounds = new Rect();
    profileIv.getGlobalVisibleRect(startBounds);

    if (globalOffset == null) {
      globalOffset = new Point();
      Rect rootRect = new Rect();
      rootView.getGlobalVisibleRect(rootRect, globalOffset);
    }

    startBounds.offset(-globalOffset.x, -globalOffset.y);

    HeroActivity.navigateToHeroActivity(MainActivity.this, startBounds, globalOffset);
    overridePendingTransition(0, 0);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    getWindow().setBackgroundDrawable(null);
    ButterKnife.bind(MainActivity.this);

    MainActivity.this.setupToolbar();

    Picasso.with(MainActivity.this)
           .load(R.drawable.one)
           .noFade()
           .transform(new CropCircleTransformation())
           .into(profileIv, picassoCallback);

    this.viewStub.setOnInflateListener(inflateListener);
  }

  private void setupToolbar() {
    this.toolbar.setTitle(Title);
  }

  @NonNull @OnClick(R.id.main_layout_profile_iv) void onProfileClick() {
    if (!isDone) {
      Toast.makeText(MainActivity.this, "头像加载中......", Toast.LENGTH_SHORT).show();
    } else {
      if (viewStub.getParent() != null) {
        inflate = viewStub.inflate();
      } else {
        viewStub.setVisibility(View.VISIBLE);
        MainActivity.this.navigateToHero(inflate);
      }
    }
  }

  @CheckResult private Bitmap catchScreen() {

    View rootView = MainActivity.this.getWindow().getDecorView().findViewById(android.R.id.content);

    rootView.setDrawingCacheEnabled(true);
    Bitmap drawingCache = rootView.getDrawingCache();

    Bitmap tempBitmap = Bitmap.createBitmap(drawingCache.getWidth(), drawingCache.getHeight(), Bitmap.Config.ARGB_8888);

    Canvas canvas = new Canvas(tempBitmap);
    Paint paint = new Paint();
    paint.setAntiAlias(true);
    paint.setFlags(Paint.FILTER_BITMAP_FLAG);

    canvas.drawBitmap(drawingCache, 0, 0, paint);

    rootView.destroyDrawingCache();

    return tempBitmap;
  }

  @Override protected void onPostResume() {
    super.onPostResume();

    this.profileIv.setVisibility(View.VISIBLE);
    this.viewStub.setVisibility(View.GONE);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    Picasso.with(MainActivity.this).cancelRequest(profileIv);
    this.viewStub.setOnInflateListener(null);
    ButterKnife.unbind(MainActivity.this);
  }
}
