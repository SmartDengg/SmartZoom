package com.joker.smartdengg_smatrzoom.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.joker.smartdengg_smatrzoom.Constant;
import com.joker.smartdengg_smatrzoom.R;
import com.joker.smartdengg_smatrzoom.util.Utils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * Created by Joker on 2016/2/18.
 */
public class HeroActivity extends AppCompatActivity {

  private static final String RECT = "RECT";
  private static final String POINT = "POINT";
  private static final long DURATION = 666;

  @NonNull @Bind((R.id.hero_layout_square_iv)) protected ImageView heroIv;

  private Rect startBounds;
  private float scale;
  private AnimatorSet animatorSet;

  public static void navigateToHeroActivity(@NonNull Activity startingActivity, @NonNull Rect startBounds,
                                            @NonNull Point globalOffset) {

    Intent intent = new Intent(startingActivity, HeroActivity.class);
    intent.putExtra(RECT, startBounds).putExtra(POINT, globalOffset);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.hero_layout);
    ButterKnife.bind(HeroActivity.this);

    HeroActivity.this.initView(savedInstanceState);
  }

  private void initView(Bundle savedInstanceState) {

    if (savedInstanceState == null) {
      heroIv.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override public boolean onPreDraw() {

          heroIv.getViewTreeObserver().removeOnPreDrawListener(this);
          HeroActivity.this.runEnterAnimation(getIntent().getExtras());

          return true;
        }
      });
    }
  }

  private void runEnterAnimation(Bundle extras) {

    Point globalOffset = extras.getParcelable(POINT);
    startBounds = extras.getParcelable(RECT);

    Rect finalBounds = new Rect();
    this.heroIv.getGlobalVisibleRect(finalBounds);
    finalBounds.offset(-globalOffset.x, -globalOffset.y);

    scale = Utils.calculateScale(startBounds, finalBounds);

    this.heroIv.setLayerType(View.LAYER_TYPE_HARDWARE, null);

    ViewCompat.setPivotX(heroIv, 0.0f);
    ViewCompat.setPivotY(heroIv, 0.0f);

    animatorSet = new AnimatorSet();
    animatorSet
        .play(ObjectAnimator.ofFloat(this.heroIv, View.X, startBounds.left, finalBounds.left))
        .with(ObjectAnimator.ofFloat(this.heroIv, View.Y, startBounds.top, finalBounds.top))
        .with(ObjectAnimator.ofFloat(this.heroIv, View.SCALE_X, scale, 1.0f))
        .with(ObjectAnimator.ofFloat(this.heroIv, View.SCALE_Y, scale, 1.0f));
    animatorSet.setDuration(DURATION);
    animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
    animatorSet.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {

        heroIv.setLayerType(View.LAYER_TYPE_NONE, null);
        HeroActivity.this.animatorSet = null;
      }
    });

    Picasso.with(HeroActivity.this).load(Constant.URL).noFade().into(heroIv, new Callback.EmptyCallback() {
      @Override public void onSuccess() {
        animatorSet.start();
      }
    });
  }

  protected void runExitAnimator() {

    if (animatorSet != null) animatorSet.cancel();

    this.heroIv.setLayerType(View.LAYER_TYPE_HARDWARE, null);

    animatorSet = new AnimatorSet();
    animatorSet
        .play(ObjectAnimator.ofFloat(this.heroIv, View.X, startBounds.left))
        .with(ObjectAnimator.ofFloat(this.heroIv, View.Y, startBounds.top))
        .with(ObjectAnimator.ofFloat(this.heroIv, View.SCALE_X, scale))
        .with(ObjectAnimator.ofFloat(this.heroIv, View.SCALE_Y, scale));
    animatorSet.setDuration(DURATION);
    animatorSet.setInterpolator(new DecelerateInterpolator());
    animatorSet.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {

        HeroActivity.this.animatorSet = null;
        HeroActivity.this.heroIv.setLayerType(View.LAYER_TYPE_NONE, null);
        HeroActivity.this.finish();
      }
    });
    animatorSet.start();
  }

  @NonNull @OnClick(R.id.hero_layout_cancel_iv) void onCancelClick() {
    HeroActivity.this.runExitAnimator();
  }

  @Override public void finish() {
    super.finish();
    overridePendingTransition(0, 0);
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
      HeroActivity.this.runExitAnimator();
    }
    return false;
  }

  @Override protected void onDestroy() {
    super.onDestroy();

    Picasso.with(HeroActivity.this).cancelRequest(heroIv);
    if (animatorSet != null && animatorSet.isRunning()) animatorSet.cancel();
    ButterKnife.unbind(HeroActivity.this);
  }
}
