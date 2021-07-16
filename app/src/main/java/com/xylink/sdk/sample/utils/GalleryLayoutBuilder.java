package com.xylink.sdk.sample.utils;

import android.log.L;

import com.ainemo.sdk.otf.LayoutElement;
import com.ainemo.sdk.otf.LayoutPolicy;
import com.ainemo.sdk.otf.LayoutPolicy.LayoutBuilder;
import com.ainemo.sdk.otf.ResolutionRatio;

import java.util.ArrayList;
import java.util.List;

import vulture.module.call.sdk.CallSdkJniListener.MiniRosterInfo;
import vulture.module.call.sdk.CallSdkJniListener.PostRosterInfo;

/**
 * 画廊模式(多个相同尺寸的屏的排列组合，如，吅，品，田等)
 */
public class GalleryLayoutBuilder implements LayoutBuilder {

    private static final String TAG = "GalleryLayoutBuilder";

    private final int PAGER_COUNT = 6;

    private LayoutPolicy layoutPolicy;

    private int pagerIndex;

    public GalleryLayoutBuilder(int pagerIndex) {
        this.pagerIndex = pagerIndex;
    }

    private int rosterCount = 0;

    public int getRoster() {
        return rosterCount;
    }

    @Override
    public List<LayoutElement> compute(LayoutPolicy policy) {
        layoutPolicy = policy;

        List<LayoutElement> layoutElements = new ArrayList<>();

        PostRosterInfo rosterInfo = layoutPolicy.getRosterInfo();

        if (rosterInfo != null) {
            List<MiniRosterInfo> rosterElements = rosterInfo.getPeopleRosterElements();
            if (rosterElements != null && rosterElements.size() > 0) {
                L.i(TAG, "rosterElements.size : " + rosterElements.size());

                for (int i = 0; i < rosterElements.size(); i++) {
                    MiniRosterInfo ros = rosterElements.get(i);
                    LayoutElement layoutElement = new LayoutElement();
                    layoutElement.setParticipantId(ros.getParticipantId());
                    layoutElement.setResolutionRatio(ResolutionRatio.RESO_180P_BASE);
                    layoutElements.add(layoutElement);
                }
                // 分页
                // [0 , 5) + local, [5,  11), [11, 17)
                //      第二页         第三页    第四页
                int size = layoutElements.size();
                if (pagerIndex == 1) {
                    layoutElements = layoutElements.subList(0, Math.min(5, size));
                } else {
                    if (size > (pagerIndex - 1) * PAGER_COUNT - 1) {
                        int startIndex = (pagerIndex - 1) * PAGER_COUNT - 1;
                        int endIntex = Math.min(startIndex + PAGER_COUNT, rosterElements.size());
                        layoutElements = layoutElements.subList(startIndex, endIntex);
                        L.i(TAG, pagerIndex + " --> subList: [" + startIndex + ", " + endIntex + "]");
                    }
                }
                L.i(TAG, "layoutElements.size : " + rosterElements);
            }
        }
        rosterCount = layoutElements.size();
        L.i(TAG, "layoutElements.size rosterCount : " + rosterCount);
        return layoutElements;
    }

}
