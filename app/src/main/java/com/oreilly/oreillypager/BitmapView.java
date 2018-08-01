package com.oreilly.oreillypager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class BitmapView extends View {

  private Bitmap mBitmap;
  private Rect mDrawingRect = new Rect();

  public BitmapView(Context context) {
    this(context, null);
  }

  public BitmapView(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public BitmapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    setClipToOutline(false);
  }

  public void setBitmap(Bitmap bitmap) {
    mBitmap = bitmap;
    invalidate();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    if (mBitmap == null) {
      return;
    }
    int width = MeasureSpec.getSize(widthMeasureSpec);
    int height = MeasureSpec.getSize(heightMeasureSpec);
    Log.d("CSP", "onMeasure, width=" + width + ", height=" + height);
    float scale = height / (float) mBitmap.getHeight();
    Log.d("CSP", "scale=" + scale);
    mDrawingRect.set(0, 0, (int) (mBitmap.getWidth() * scale), height);
    setMeasuredDimension(mDrawingRect.width(), mDrawingRect.height());
  }

  @Override
  protected void onDraw(Canvas canvas) {
    Log.d("CSP", "onDraw");
    super.onDraw(canvas); // backgrounds, shadows, etc
    if (mBitmap == null) {
      return;
    }
    canvas.drawBitmap(mBitmap, mDrawingRect, mDrawingRect, null);
  }
}
