package cn.ninanina.wushanvideo.network;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import cn.ninanina.wushanvideo.WushanApp;
import cn.ninanina.wushanvideo.ui.MainActivity;

/**
 * 加载原生广告并且在适当的时候展示
 */
public class AdManager {
    public static AdManager getInstance() {
        return instance;
    }

    private static AdManager instance = new AdManager();
    private AdLoader adLoader;
    private Queue<UnifiedNativeAd> adQueue = new LinkedList<>();

    private AdManager() {
        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(true)
                .build();
        adLoader = new AdLoader.Builder(WushanApp.getInstance(), "ca-app-pub-2117487515590175/2795818027")
                .forUnifiedNativeAd(unifiedNativeAd -> adQueue.offer(unifiedNativeAd))
                .withAdListener(new AdListener() {

                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        System.out.println(errorCode);
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_LEFT)
                        .setVideoOptions(videoOptions)
                        .build())
                .build();
    }

    public void loadAd() {
        adLoader.loadAd(new AdRequest.Builder().build());
    }

    public void loadAds(int n) {
        for (int i = 0; i < n; i++) adLoader.loadAd(new AdRequest.Builder().build());
    }

    public UnifiedNativeAd nextAd() {
        if (adQueue.size() > 0)
            return adQueue.poll();
        else return null;
    }

    public List<UnifiedNativeAd> nextAds(int n) {
        List<UnifiedNativeAd> result = new ArrayList<>();
        if (adQueue.size() >= n) {
            for (int i = 0; i < n; i++) result.add(adQueue.poll());
        }
        return result;
    }

    public int size() {
        return adQueue.size();
    }
}
