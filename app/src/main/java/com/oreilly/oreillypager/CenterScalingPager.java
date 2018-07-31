package com.oreilly.oreillypager;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CenterScalingPager extends HorizontalScrollView {

  private LinearLayout mLinearLayout;
  private GestureDetector mGestureDetector;
  private List<Integer> mCenters = new ArrayList<>();
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
    mGestureDetector = new GestureDetector(context, new FlingListener());
    mLinearLayout = new LinearLayout(context);
    LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    addView(mLinearLayout, lp);
    for (int i = 0; i < 10; i++) {
      TextView cell = new TextView(context);
      cell.setPadding(100,100,100,100);
      cell.setTextSize(72);
      cell.setText(String.valueOf(i));
      mLinearLayout.addView(cell);
    }
    mLinearLayout.addOnLayoutChangeListener(mOnLayoutChangeListener);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (mGestureDetector.onTouchEvent(event)) {
      return true;
    }
    if (event.getActionMasked() == MotionEvent.ACTION_UP) {
      Log.d("CSP", "onUp");
      onUp();
    }
    return super.onTouchEvent(event);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    mCenter = getMeasuredWidth() / 2;
  }

  @Override
  protected void onScrollChanged(int l, int t, int oldl, int oldt) {
    super.onScrollChanged(l, t, oldl, oldt);
    resetLastActiveChild();
    mActive = findCenterMostChild();
    decorateActiveChild();
  }

  private void resetLastActiveChild() {
    if (mActive == null) {
      return;
    }
    mActive.setTextColor(Color.GRAY);
    mActive.setScaleX(1);
    mActive.setScaleY(1);
  }

  private void decorateActiveChild() {
    if (mActive == null) {
      return;
    }
    int center = mCenter + getScrollX();
    float half = mActive.getMeasuredWidth() * 0.5f;
    float middle = mActive.getLeft() + half;
    float distance = Math.abs(middle - center);
    float scale = 1 + (1 - (distance / half));
    mActive.setScaleX(scale);
    mActive.setScaleY(scale);
  }

  private TextView mActive = null;
  private TextView findCenterMostChild() {
    int center = mCenter + getScrollX();
    Log.d("CSP", "offset center: " + center);
    for (int i = 0; i < mLinearLayout.getChildCount(); i++) {
      View child = mLinearLayout.getChildAt(i);
      Log.d("CSP", "i=" + i + ", left=" + child.getLeft() + ", right=" + child.getRight());
      if (child.getLeft() < center && child.getRight() > center) {
        // TODO: how far from center
        return (TextView) child;
      }
    }
    return null;
  }

  private int getChildCenter(View view) {
    return view.getLeft() + (view.getMeasuredWidth() / 2);
  }

  private void populateCenters() {
    for (int i = 0; i < mLinearLayout.getChildCount(); i++) {
      View child = mLinearLayout.getChildAt(i);
      setCenterPosition(i, child.getLeft() + (child.getWidth() / 2));
    }
  }

  private void setCenterPosition(int index, int position) {
    if (mCenters.size() >= index) {
      mCenters.add(position);
    } else {
      mCenters.set(index, position);
    }
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
      populateCenters();
    }
  };

  private void onUp() {
    //findCenterMostChild();
    if (mActive == null) {
      return;
    }
    int center = mCenter + getScrollX();
    int x = mActive.getLeft() + mActive.getWidth() / 2;
    //Log.d("CSP", "before, x=" + getScrollX() + ", center=" + mCenter);
    scrollTo(x, 0);
    //Log.d("CSP", "after, x=" + getScrollX() + ", center=" + mCenter);
  }

  /*
  private void fling(float velocityX, float velocityY) {
    ChapterView currentChapterView = getCurrentChapterView();
    currentChapterView.getSlider().stopScroller();
    Scroller scroller = currentChapterView.getSlider().getScroller();
    scroller.fling(currentChapterView.getScrollX(), 0, (int) velocityX, (int) velocityY, 0, currentChapterView.getContentSize(), 0, 0);
    int originalX = scroller.getFinalX();
    int initialPage = currentChapterView.getPage();
    int destinationPage = (int) (originalX / getNormalizedWidth());
    int pageDelta = Math.abs(initialPage - destinationPage);
    if (pageDelta == 0) {
      destinationPage = (int) (initialPage + Math.signum(velocityX));
      pageDelta = 1;
    }
    currentChapterView.setPage(destinationPage);
    double actualX = currentChapterView.getCurrentPagePosition();
    int originalDistance = Math.abs(originalX - currentChapterView.getScrollX());
    double actualDistance = Math.abs(actualX - currentChapterView.getScrollX());
    double percent = actualDistance / originalDistance;
    int duration = (int) (percent * scroller.getDuration());
    int maximumPermissibleDuration = pageDelta * MAXIMUM_TRANSITION_DURATION_PER_PAGE;
    duration = Math.min(duration, maximumPermissibleDuration);
    scroller.extendDuration(duration);
    scroller.setFinalX((int) actualX);
    ViewCompat.postInvalidateOnAnimation(currentChapterView);
  }
  */

  private class FlingListener extends GestureDetector.SimpleOnGestureListener {
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      return super.onFling(e1, e2, velocityX, velocityY);
    }
  }
}
