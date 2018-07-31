package com.oreilly.oreillypager;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CenterScalingPager extends HorizontalScrollView {

  private LinearLayout mLinearLayout;

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

}
