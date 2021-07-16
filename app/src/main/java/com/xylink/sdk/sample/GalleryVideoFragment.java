package com.xylink.sdk.sample;

import android.log.L;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.ainemo.sdk.otf.VideoInfo;
import com.xylink.sdk.sample.view.GalleryVideoGroup;
import com.xylink.sdk.sample.view.VideoCell;
import com.xylink.sdk.sample.view.VideoCellLayout;

import java.util.List;

public class GalleryVideoFragment extends VideoFragment {
    private static final String TAG = "GalleryVideoFragment";
    private GalleryVideoGroup galleryVideoView;
    private TextView tvRosterInfo;

    public static GalleryVideoFragment newInstance(int position) {
        GalleryVideoFragment galleryVideoFragment = new GalleryVideoFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("args", position);
        galleryVideoFragment.setArguments(bundle);
        return galleryVideoFragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        L.i(TAG, "onStart");
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        L.i(TAG, "setUserVisibleHint: " + isVisibleToUser);
        if (!isVisibleToUser && galleryVideoView != null) {
            L.i(TAG, "pauseRender");
            galleryVideoView.pauseRender();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        galleryVideoView.startRender();
        L.i(TAG, "onResume");
    }

    @Override
    public void startRender() {
        L.i(TAG, "startRender: " + galleryVideoView);
        if (galleryVideoView != null) {
            galleryVideoView.startRender();
        }
    }

    @Override
    public int getLayoutRes() {
        L.i(TAG, "onCreateView");
        return R.layout.fragment_gallery_video;
    }

    @Override
    public void initView(View view) {
        L.i(TAG, "onViewCreated");
        galleryVideoView = view.findViewById(R.id.gallery_video);
        galleryVideoView.setLocalVideoInfo(localVideoInfo);
        galleryVideoView.setOnVideoCellListener(galleryVideoCellListener);
        tvRosterInfo = view.findViewById(R.id.tv_roster_info);
    }

    @Override
    public void setLandscape(boolean landscape) {
        L.i(TAG, "setLandscape: " + landscape);
        if (galleryVideoView != null) {
            galleryVideoView.setLandscape(landscape);
        }
    }

    @Override
    public void setVideoMute(boolean mute) {
        if (galleryVideoView != null) {
            galleryVideoView.setMuteLocalVideo(mute, getString(R.string.call_video_mute));
        }
    }

    @Override
    public void setAudioOnlyMode(boolean audioMode, boolean isLocalMute) {
        if (galleryVideoView != null) {
            galleryVideoView.setAudioOnlyMode(audioMode, isLocalMute);
        }
    }

    @Override
    public void setLocalVideoInfo(VideoInfo layoutInfo) {
        localVideoInfo = layoutInfo;
        if (galleryVideoView != null) {
            galleryVideoView.setLocalVideoInfo(layoutInfo);
        }
    }

    @Override
    public void setMicMute(boolean mute) {
        if (galleryVideoView != null) {
            galleryVideoView.setMuteLocalAudio(mute);
        }
    }

    @Override
    public void setRemoteVideoInfo(List<VideoInfo> videoInfos, boolean hasVideoContent) {
        if (galleryVideoView != null) {
            StringBuilder sb = new StringBuilder();
            if (videoInfos != null) {
                L.i(TAG, "setRemoteVideoInfos: " + videoInfos.size());
                for (int i = 0; i < videoInfos.size(); i++) {
                    sb.append(videoInfos.get(i).getRemoteName()).append("\n");
                }
            }
            tvRosterInfo.setText(sb.toString());
            if (getArguments() != null && getArguments().getInt("args", 0) == 1) {
                // 第一页, add Local view to group
                galleryVideoView.addLocalCell();
            }
            galleryVideoView.setRemoteVideoInfos(videoInfos);
        }
    }

    @Override
    public void setCurrentIndex(int index) {
        currentIndex = index;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        L.i(TAG, "onActivityCreated");
    }

    @Override
    public void onPause() {
        super.onPause();
        L.i(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        L.i(TAG, "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        L.i(TAG, "onDestroyView");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        L.i(TAG, "onDetach");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        L.i(TAG, "onDestroy");
        if (galleryVideoView != null) {
            galleryVideoView.destroy();
        }
    }

    private VideoCellLayout.SimpleVideoCellListener galleryVideoCellListener = new VideoCellLayout.SimpleVideoCellListener() {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e, VideoCell cell) {
            if (videoCallback != null) {
                videoCallback.onVideoCellSingleTapConfirmed(cell);
            }
            return true;
        }

        @Override
        public void onFullScreenChanged(VideoCell cell) {
            super.onFullScreenChanged(cell);
            if (videoCallback != null) {
                videoCallback.onFullScreenChanged(cell);
            }
        }

        @Override
        public void onVideoCellGroupClicked(View group) {
            if (videoCallback != null) {
                videoCallback.onVideoCellGroupClicked(group);
            }
        }
    };
}
