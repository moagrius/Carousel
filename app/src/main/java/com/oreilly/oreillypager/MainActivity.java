package com.oreilly.oreillypager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

  private ViewGroup mOverlay;
  private ViewGroup mCard;
  private ImageView mThumb;

  private boolean mIsInfoWindowShowing;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    CenterScalingPager cspt = findViewById(R.id.centerscalingpager_trending);
    cspt.setCellClickListener(this::onCellClick);
    CenterScalingPager cspr = findViewById(R.id.centerscalingpager_recommended);
    cspr.setCellClickListener(this::onCellClick);
    CenterScalingPager cspl = findViewById(R.id.centerscalingpager_recent);
    cspl.setCellClickListener(this::onCellClick);
    mOverlay = findViewById(R.id.framelayout_overlay);
    mCard = findViewById(R.id.framelayout_card);
    mThumb = findViewById(R.id.imageview_card_thumb);
    populateContinueReading();
  }

  private void populateContinueReading() {
    try {
      InputStream inputStream = getAssets().open("3.jpg");
      Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
      ImageView lastThumb = findViewById(R.id.imageview_continue_reading_thumb);
      lastThumb.setImageBitmap(bitmap);
      TextView lastTextView = findViewById(R.id.textview_continue_reading_title);
      String html = "<b>Continue:</b> Some title of some book";
      lastTextView.setText(Html.fromHtml(html));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void onCellClick(View cell) {
    mIsInfoWindowShowing = true;
    mOverlay.setVisibility(View.VISIBLE);
    ImageView imageView = (ImageView) cell;
    mThumb.setImageDrawable(imageView.getDrawable());
    float elevation = ViewCompat.getElevation(imageView) + ViewCompat.getElevation(mCard);
    ViewCompat.setElevation(mThumb, elevation);

  }

  @Override
  public void onBackPressed() {
    if (mIsInfoWindowShowing) {
      mIsInfoWindowShowing = false;
      mOverlay.setVisibility(View.GONE);
    } else {
      super.onBackPressed();
    }
  }
}
