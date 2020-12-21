package cn.ninanina.wushanvideo.adapter.holder;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.formats.UnifiedNativeAdView;

import cn.ninanina.wushanvideo.R;

public class AdHolder extends RecyclerView.ViewHolder {
    private UnifiedNativeAdView adView;

    public UnifiedNativeAdView getAdView() {
        return adView;
    }

    public AdHolder(@NonNull View itemView) {
        super(itemView);
        adView = itemView.findViewById(R.id.ad_view);

        // The MediaView will display a video asset if one is present in the ad, and the
        // first image asset otherwise.
        adView.setMediaView(adView.findViewById(R.id.ad_media));
        adView.getMediaView().setImageScaleType(ImageView.ScaleType.CENTER_INSIDE);

        // Register the view used for each individual asset.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
    }
}
