package com.oreilly.oreillypager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  private HorizontalScrollView mScrollView;
  private LinearLayout mLinearLayout;

  private List<Integer> mCenters = new ArrayList<>(10);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mScrollView = findViewById(R.id.scrollview);
    mLinearLayout = findViewById(R.id.linearlayout);
    for (int i = 0; i < 10; i++) {
      TextView cell = new TextView(this);
      cell.setPadding(20,20,20,20);
      cell.setTextSize(72);
      cell.setText(String.valueOf(i));
      mLinearLayout.addView(cell);
    }
    mLinearLayout.addOnLayoutChangeListener(mOnLayoutChangeListener);
  }

  private void populateCenters() {
    for (int i = 0; i < mLinearLayout.getChildCount(); i++) {
      View child = mLinearLayout.getChildAt(i);
      mCenters.set(i, child.getLeft() + (child.getWidth() / 2));
    }
  }

  private View.OnScrollChangeListener mOnScrollChangeListener = new View.OnScrollChangeListener() {
    @Override
    public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

    }
  };

  private View.OnLayoutChangeListener mOnLayoutChangeListener = new View.OnLayoutChangeListener() {
    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
      if (mLinearLayout.getChildCount() <= 1) {
        return;
      }
      int totalWidth = mScrollView.getWidth();
      int halfWidth = totalWidth / 2;
      View firstChild = mLinearLayout.getChildAt(0);
      View lastChild = mLinearLayout.getChildAt(mLinearLayout.getChildCount() - 1);
      int firstChildWidth = firstChild.getWidth();
      int lastChildWidth = lastChild.getWidth();
      int paddingLeft = halfWidth - (firstChildWidth / 2);
      int paddingRight = halfWidth - (lastChildWidth / 2);
      mLinearLayout.setPadding(paddingLeft, 0, paddingRight, 0);
      populateCenters();
    }
  };
}
