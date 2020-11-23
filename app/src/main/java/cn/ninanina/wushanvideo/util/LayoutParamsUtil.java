package cn.ninanina.wushanvideo.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import cn.ninanina.wushanvideo.adapter.CollectItemAdapter;

public class LayoutParamsUtil {
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 自适应收藏夹的ListView高度
     */
    public static void adaptCollectListViewHeight(ListView listView) {
        CollectItemAdapter adapter = (CollectItemAdapter) listView.getAdapter();
        int height = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View view = adapter.getView(i, null, listView);
            view.measure(0, 0);
            height += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams layoutParams = listView.getLayoutParams();
        layoutParams.height = height + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(layoutParams);
    }
}
