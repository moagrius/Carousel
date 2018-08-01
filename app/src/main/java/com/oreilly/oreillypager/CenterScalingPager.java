package com.oreilly.oreillypager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;

import java.io.IOException;
import java.io.InputStream;

public class CenterScalingPager extends HorizontalScrollView {

  private LinearLayout mLinearLayout;
  private GestureDetector mGestureDetector;
  private Scroller mScroller;
  private ImageView mActive;
  private int mCenter;

  public CenterScalingPager(@NonNull Context context) {
    this(context, null);
  }

  public CenterScalingPager(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public CenterScalingPager(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    setFillViewport(true);
    mScroller = new Scroller(context);
    mGestureDetector = new GestureDetector(context, new FlingListener());
    mLinearLayout = new LinearLayout(context);
    mLinearLayout.setClipToPadding(false);
    LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    addView(mLinearLayout, lp);
    LayoutInflater inflater = LayoutInflater.from(context);
    for (int i = 1; i < 10; i++) {
      ImageView imageView = (ImageView) inflater.inflate(R.layout.cell_horizontal_pager, mLinearLayout, false);
      Bitmap bitmap = getBitmapFromAssets(i + ".jpg");
      imageView.setImageBitmap(bitmap);
      mLinearLayout.addView(imageView);
    }
    mLinearLayout.addOnLayoutChangeListener(mOnLayoutChangeListener);
  }

  private Bitmap getBitmapFromAssets(String name) {
    try {
      InputStream inputStream = getContext().getAssets().open(name);
      return BitmapFactory.decodeStream(inputStream);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (mGestureDetector.onTouchEvent(event)) {
      return true;
    }
    int action = event.getActionMasked();
    if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
      snapToActive();
    }
    return super.onTouchEvent(event);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    mCenter = getMeasuredWidth() / 2;
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);
    updateActiveChild();
  }

  @Override
  protected void onScrollChanged(int l, int t, int oldl, int oldt) {
    super.onScrollChanged(l, t, oldl, oldt);
    updateActiveChild();
  }

  @Override
  public void computeScroll() {
    if (mScroller.computeScrollOffset()) {
      scrollTo(mScroller.getCurrX(), 0);
      if (!mScroller.isFinished()) {
        ViewCompat.postInvalidateOnAnimation(this);
      }
    }
  }

  private void slideTo(int x) {
    if (mActive == null) {
      return;
    }
    int delta = x - getScrollX();
    float distance = Math.abs(delta);
    // go 1px per ms
    mScroller.startScroll(getScrollX(), 0, delta, 0, (int) distance);
    ViewCompat.postInvalidateOnAnimation(this);
  }

  private void updateActiveChild() {
    resetLastActiveChild();
    mActive = findCenterMostChild();
    decorateActiveChild();
  }

  private void resetLastActiveChild() {
    if (mActive == null) {
      return;
    }
    mActive.setScaleX(1);
    mActive.setScaleY(1);
  }

  private void decorateActiveChild() {
    if (mActive == null) {
      return;
    }
    mLinearLayout.removeOnLayoutChangeListener(mOnLayoutChangeListener);
    int width = mActive.getMeasuredWidth();
    int center = mCenter + getScrollX();
    float half = width * 0.5f;
    float middle = mActive.getLeft() + half;
    float distance = Math.abs(middle - center);
    float scale = 1 + (1 - (distance / half));
    mActive.setScaleX(scale);
    mActive.setScaleY(scale);
    //mActive.bringToFront();
    float wider = (width * scale) - width;
    float widerHalf = wider * 0.5f;
    boolean isOnRightOfActive = false;
    // TODO: infinite loop!
    for (int i = 0; i < mLinearLayout.getChildCount(); i++) {
      View child = mLinearLayout.getChildAt(i);
      if (child == mActive) {
        isOnRightOfActive = true;
        continue;
      }
      float offset = isOnRightOfActive ? widerHalf : -widerHalf;
      child.setTranslationX(offset);
    }
    mLinearLayout.addOnLayoutChangeListener(mOnLayoutChangeListener);
  }

  private ImageView findCenterMostChild() {
    int center = mCenter + getScrollX();
    for (int i = 0; i < mLinearLayout.getChildCount(); i++) {
      View child = mLinearLayout.getChildAt(i);
      if (child.getLeft() < center && child.getRight() > center) {
        return (ImageView) child;
      }
    }
    return null;
  }

  private View.OnLayoutChangeListener mOnLayoutChangeListener = new View.OnLayoutChangeListener() {
    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
      if (mLinearLayout.getChildCount() <= 1) {
        return;
      }
      View firstChild = mLinearLayout.getChildAt(0);
      View lastChild = mLinearLayout.getChildAt(mLinearLayout.getChildCount() - 1);
      int paddingLeft = mCenter - (firstChild.getWidth() / 2);
      int paddingRight = mCenter - (lastChild.getWidth() / 2);
      mLinearLayout.setPadding(paddingLeft, 0, paddingRight, 0);
    }
  };

  private void snapToActive() {
    if (mActive == null) {
      return;
    }
    int x = mActive.getLeft() + (mActive.getWidth() / 2) - mCenter;
    slideTo(x);
  }

  private void fling(float velocityX, float velocityY) {
    // positive is going right to left (toward start), negative is left to right (toward end)
    Log.d("CSP", "fling, vx=" + velocityX);
    mScroller.fling(getScrollX(), 0, (int) -velocityX, (int) -velocityY, 0, computeHorizontalScrollRange(), 0, 0);
    int originalX = mScroller.getFinalX();
    int count = mLinearLayout.getChildCount();
    View target = null;
    for (int i = 0; i < count; i++) {
      View child = mLinearLayout.getChildAt(i);
      if (child.getLeft() <= originalX && child.getRight() >= originalX) {
        Log.d("CSP", "found child at " + i);
        if (child == mActive) {
          Log.d("CSP", "child is the active, move one place to left or right");
          int offset = (int) (i - Math.signum(velocityX));
          Log.d("CSP", "active is at " + i + ", move to " + offset);
          offset = Math.min(offset, count -1);
          offset = Math.max(offset, 0);
          child = mLinearLayout.getChildAt(offset);
        }
        target = child;
        break;
      }
    }

    if (target == null) {
      Log.d("CSP", "did NOT find child, use first or last");
      int position = Math.signum(velocityX) == 1 ? 0 : (count - 1);
      target = mLinearLayout.getChildAt(position);
    }
    int x = target.getLeft() + (target.getWidth() / 2) - mCenter;
    Log.d("CSP", "target center is " + x);
    mScroller.setFinalX(x);
    ViewCompat.postInvalidateOnAnimation(this);
  }

  private class FlingListener extends GestureDetector.SimpleOnGestureListener {
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      fling(velocityX, velocityY);
      return true;
    }
  }
}
