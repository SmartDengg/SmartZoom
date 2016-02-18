package com.joker.smartdengg_smatrzoom;

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
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.joker.smartdengg_smatrzoom.util.Utils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * Created by Joker on 2016/2/18.
 */
public class HeroActivity extends AppCompatActivity {

  private static final String RECT = "RECT";
  private static final String POINT = "POINT";

  @NonNull @Bind((R.id.hero_layout_square_iv)) protected ImageView heroIv;

  private AnimatorSet animatorSet;

  public static void navigateToHeroActivity(Activity startingActivity, @NonNull Rect startBounds,
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
    Rect startBounds = extras.getParcelable(RECT);

    Rect finalBounds = new Rect();
    this.heroIv.getGlobalVisibleRect(finalBounds);
    finalBounds.offset(-globalOffset.x, -globalOffset.y);

    float scale = Utils.calculateScale(startBounds, finalBounds);

    this.heroIv.setLayerType(View.LAYER_TYPE_HARDWARE, null);

    ViewCompat.setPivotX(heroIv, 0.0f);
    ViewCompat.setPivotY(heroIv, 0.0f);

    animatorSet = new AnimatorSet();
    animatorSet.play(ObjectAnimator.ofFloat(this.heroIv, View.X, startBounds.left, finalBounds.left))
               .with(ObjectAnimator.ofFloat(this.heroIv, View.Y, startBounds.top, finalBounds.top))
               .with(ObjectAnimator.ofFloat(this.heroIv, View.SCALE_X, scale, 1.0f))
               .with(ObjectAnimator.ofFloat(this.heroIv, View.SCALE_Y, scale, 1.0f))
               .with(ObjectAnimator.ofFloat(this.heroIv, View.ALPHA, 0.4f, 1.0f));
    animatorSet.setDuration(666);/*:)*/
    animatorSet.setInterpolator(new OvershootInterpolator(1.4f));
    animatorSet.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {

        heroIv.setLayerType(View.LAYER_TYPE_NONE, null);
        HeroActivity.this.animatorSet = null;
      }

      @Override public void onAnimationCancel(Animator animation) {
        HeroActivity.this.animatorSet = null;
      }
    });

    Picasso.with(HeroActivity.this).load(R.drawable.template).into(heroIv, new Callback.EmptyCallback() {
      @Override public void onSuccess() {
        animatorSet.start();
      }
    });
  }

  @NonNull @OnClick(R.id.hero_layout_cancel_iv) void onCancelClick() {
    HeroActivity.this.finish();
  }

  @Override public void finish() {
    super.finish();
    overridePendingTransition(0, 0);
  }

  @Override protected void onDestroy() {
    super.onDestroy();

    if (animatorSet != null && animatorSet.isRunning()) animatorSet.cancel();

    ButterKnife.unbind(HeroActivity.this);
  }
}
