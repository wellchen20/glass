package com.xylink.sdk.sample.view;

public interface VolumeRequester {
    void onVolumeSlide(float percent);

    void onVolumeSlideEnd();
}
