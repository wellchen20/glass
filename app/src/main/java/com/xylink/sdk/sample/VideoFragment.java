package com.xylink.sdk.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ainemo.sdk.otf.RosterWrapper;
import com.ainemo.sdk.otf.VideoInfo;
import com.xylink.sdk.sample.view.VideoCell;

import java.util.List;

public abstract class VideoFragment extends Fragment {
    protected int currentIndex;

    protected VideoCallback videoCallback;

    protected VideoInfo localVideoInfo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutRes(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    public abstract void setLandscape(boolean landscape);

    public abstract int getLayoutRes();

    public abstract void initView(View view);

    public abstract void setVideoMute(boolean mute);

    public abstract void setMicMute(boolean mute);

    public abstract void setRemoteVideoInfo(List<VideoInfo> videoInfos, boolean hasContent);

    public abstract void setCurrentIndex(int index);

    public abstract void startRender();

    public abstract void setAudioOnlyMode(boolean audioMode, boolean isLocalMute);

    public void setVideoCallback(VideoCallback callback) {
        videoCallback = callback;
    }

    public abstract void setLocalVideoInfo(VideoInfo layoutInfo);

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public interface VideoCallback {

        boolean onVideoCellSingleTapConfirmed(VideoCell cell);

        /**
         * 双击
         *
         * @param cell
         * @return
         */
        boolean onVideoCellDoubleTap(VideoCell cell);

        /**
         * 锁屏改变
         *
         * @param pid
         */
        void onLockLayoutChanged(int pid);

        /**
         * 全屏改变
         *
         * @param cell
         */
        void onFullScreenChanged(VideoCell cell);

        /**
         * 视频单元父视图被单击
         *
         * @param group
         */
        void onVideoCellGroupClicked(View group);

        void onWhiteboardClicked();
    }
}
