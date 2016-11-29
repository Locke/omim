package com.mapswithme.maps.widget.placepage;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.mapswithme.maps.R;
import com.mapswithme.maps.bookmarks.data.Banner;
import com.mapswithme.util.ConnectionState;
import com.mapswithme.util.UiUtils;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.mapswithme.util.SharedPropertiesUtils.isShowcaseSwitchedOnLocal;

final class BannerController implements View.OnClickListener
{
  @Nullable
  private Banner mBanner;
  @Nullable
  private OnBannerClickListener mListener;

  @NonNull
  private final View mFrame;
  @Nullable
  private final ImageView mIcon;
  @Nullable
  private final TextView mTitle;
  @Nullable
  private final TextView mMessage;
  @Nullable
  private final View mAdMarker;

  private final float mCloseFrameHeight;
  private final float mCloseIconSize;
  private final float mOpenIconSize;
  private final float mMarginBase;
  private final float mMarginHalfPlus;

  private boolean mIsOpened = false;

  BannerController(@NonNull View bannerView, @Nullable OnBannerClickListener listener)
  {
    mFrame = bannerView;
    mListener = listener;
    Resources res = mFrame.getResources();
    mCloseFrameHeight = res.getDimension(R.dimen.placepage_banner_height);
    mCloseIconSize = res.getDimension(R.dimen.placepage_banner_icon_size);
    mOpenIconSize = res.getDimension(R.dimen.placepage_banner_icon_size_full);
    mMarginBase = res.getDimension(R.dimen.margin_base);
    mMarginHalfPlus = res.getDimension(R.dimen.margin_half_plus);
    mIcon = (ImageView) bannerView.findViewById(R.id.iv__banner_icon);
    mTitle = (TextView) bannerView.findViewById(R.id.tv__banner_title);
    mMessage = (TextView) bannerView.findViewById(R.id.tv__banner_message);
    mAdMarker = bannerView.findViewById(R.id.tv__banner);
  }

  void updateData(@Nullable Banner banner)
  {
    mBanner = banner;
    boolean showBanner = banner != null && ConnectionState.isConnected()
                         && isShowcaseSwitchedOnLocal();
    UiUtils.showIf(showBanner, mFrame);
    if (!showBanner)
      return;

    loadIcon(banner);
    if (mTitle != null)
      mTitle.setText(banner.getTitle());
    if (mMessage != null)
      mMessage.setText(banner.getMessage());

    if (UiUtils.isLandscape(mFrame.getContext()))
      open();
  }

  boolean isShowing()
  {
    return !UiUtils.isHidden(mFrame);
  }

  void open()
  {
    if (!isShowing() || mBanner == null || mIsOpened)
      return;

    mIsOpened = true;
    setFrameHeight(WRAP_CONTENT);
    setIconParams(mOpenIconSize, 0, mMarginBase);
    UiUtils.show(mMessage, mAdMarker);
    loadIcon(mBanner);
    if (mTitle != null)
      mTitle.setMaxLines(2);
    mFrame.setOnClickListener(this);
  }

  void close()
  {
    if (!isShowing() || mBanner == null || !mIsOpened)
      return;

    mIsOpened = false;
    setFrameHeight((int) mCloseFrameHeight);
    setIconParams(mCloseIconSize, mMarginBase, mMarginHalfPlus);
    UiUtils.hide(mMessage, mAdMarker);
    loadIcon(mBanner);
    if (mTitle != null)
      mTitle.setMaxLines(1);
    mFrame.setOnClickListener(null);
  }

  private void setFrameHeight(int height)
  {
    ViewGroup.LayoutParams lp = mFrame.getLayoutParams();
    lp.height = height;
    mFrame.setLayoutParams(lp);
  }

  private void setIconParams(float size, float marginRight, float marginTop)
  {
    if (mIcon == null)
      return;

    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mIcon.getLayoutParams();
    lp.height = (int) size;
    lp.width = (int) size;
    lp.rightMargin = (int) marginRight;
    lp.topMargin = (int) marginTop;
    mIcon.setLayoutParams(lp);
  }

  private void loadIcon(@NonNull Banner banner)
  {
    if (mIcon == null || TextUtils.isEmpty(banner.getIconUrl()))
      return;

    Glide.with(mIcon.getContext())
         .load(banner.getIconUrl())
         .centerCrop()
         .listener(new RequestListener<String, GlideDrawable>()
         {
           @Override
           public boolean onException(Exception e, String model, Target<GlideDrawable> target,
                                      boolean isFirstResource)
           {
             UiUtils.hide(mIcon);
             return false;
           }

           @Override
           public boolean onResourceReady(GlideDrawable resource, String model,
                                          Target<GlideDrawable> target, boolean isFromMemoryCache,
                                          boolean isFirstResource)
           {
             UiUtils.show(mIcon);
             return false;
           }
         })
         .into(mIcon);
  }

  @Override
  public void onClick(View v)
  {
    if (mListener != null && mBanner != null)
      mListener.onBannerClick(mBanner);
  }

  interface OnBannerClickListener
  {
    void onBannerClick(@NonNull Banner banner);
  }
}