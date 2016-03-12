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
import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.joker.smartdengg_smatrzoom.R;
import com.joker.smartdengg_smatrzoom.transformation.CropCircleTransformation;
import com.joker.smartdengg_smatrzoom.util.BestBlur;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

  protected static final int BLUR_RADIUS = 6;
  protected static final float BLUR_DESATURATE = 0.0f;
  protected static final int BLUR_SCALE = BLUR_RADIUS / 2;

  @BindString(R.string.app_name) protected String Title;

  @NonNull @Bind(R.id.main_layout_root_view) protected RelativeLayout rootView;
  @NonNull @Bind(R.id.main_layout_toolbar) protected Toolbar toolbar;
  @NonNull @Bind(R.id.main_layout_profile_iv) protected ImageView profileIv;
  @NonNull @Bind(R.id.main_layout_viewstub) protected ViewStub viewStub;

  private Canvas canvas = null;
  private Paint paint = null;
  private Bitmap screenBitmap;
  private Point globalOffset = null;
  private View inflate;

  private ViewStub.OnInflateListener inflateListener = new ViewStub.OnInflateListener() {
    @Override public void onInflate(ViewStub stub, View inflated) {
      if (profileIv != null) MainActivity.this.navigateToHero(inflated);
    }
  };

  private void navigateToHero(View inflated) {

    this.profileIv.setVisibility(View.GONE);

    BestBlur bestBlur = new BestBlur(MainActivity.this);

    Bitmap screenSnapshot = MainActivity.this.catchScreen();
    Bitmap scaledBitmap = Bitmap.createScaledBitmap(screenSnapshot, screenSnapshot.getWidth() / BLUR_SCALE,
                                                    screenSnapshot.getHeight() / BLUR_SCALE, true);
    Bitmap blurBitmap = bestBlur.blurBitmap(scaledBitmap, BLUR_RADIUS, BLUR_DESATURATE);
    ((ImageView) inflated).setImageBitmap(blurBitmap);

    if (screenSnapshot != scaledBitmap) scaledBitmap.recycle();
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

    Picasso
        .with(MainActivity.this)
        .load(R.drawable.one)
        .noFade()
        .transform(new CropCircleTransformation())
        .into(profileIv);

    this.viewStub.setOnInflateListener(inflateListener);
  }

  private void setupToolbar() {
    this.toolbar.setTitle(Title);
  }

  @NonNull @OnClick(R.id.main_layout_profile_iv) void onProfileClick() {
    if (viewStub.getParent() != null) {
      inflate = viewStub.inflate();
    } else {
      viewStub.setVisibility(View.VISIBLE);
      MainActivity.this.navigateToHero(inflate);
    }
  }

  @CheckResult private Bitmap catchScreen() {

    View rootView = MainActivity.this.getWindow().getDecorView().findViewById(android.R.id.content);

    rootView.setDrawingCacheEnabled(true);
    Bitmap drawingCache = rootView.getDrawingCache();

    if (screenBitmap == null || canvas == null || paint == null) {
      screenBitmap = Bitmap.createBitmap(drawingCache.getWidth(), drawingCache.getHeight(), Bitmap.Config.ARGB_8888);
      canvas = new Canvas(screenBitmap);
      paint = new Paint();
      paint.setAntiAlias(true);
      paint.setFlags(Paint.FILTER_BITMAP_FLAG);
    }

    canvas.drawBitmap(drawingCache, 0, 0, paint);

    rootView.destroyDrawingCache();

    return screenBitmap;
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
