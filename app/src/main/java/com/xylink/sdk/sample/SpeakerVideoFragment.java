package com.xylink.sdk.sample;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.log.L;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.ainemo.sdk.otf.NemoSDK;
import com.ainemo.sdk.otf.Orientation;
import com.ainemo.sdk.otf.VideoInfo;
import com.xylink.sdk.sample.share.whiteboard.view.WhiteBoardCell;
import com.xylink.sdk.sample.view.SpeakerVideoGroup;
import com.xylink.sdk.sample.view.VideoCell;
import com.xylink.sdk.sample.view.VideoCellLayout;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

public class SpeakerVideoFragment extends VideoFragment {
    private static final String TAG = "SpeakerVideoFragment";
    private SpeakerVideoGroup mVideoView;
    private boolean isShowingPip = true;
    private Activity mActivity;

    private View whiteboardLaodingView;
    private boolean mLandscape;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser && mVideoView != null) {
            L.i(TAG, "pauseRender");
            mVideoView.pauseRender();
        }
    }

    public static SpeakerVideoFragment newInstance(int position) {
        SpeakerVideoFragment fragment = new SpeakerVideoFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("args", position);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void startRender() {
        L.i(TAG, "startRender: " + mVideoView);
        if (mVideoView != null) {
            mVideoView.startRender();
        }
    }

    @Override
    public int getLayoutRes() {
        L.i(TAG, "onCreateView");
        return R.layout.fragment_speaker_video;
    }

    @Override
    public void initView(View view) {
        L.i(TAG, "onViewCreated");
        mVideoView = view.findViewById(R.id.speaker_video);
        mVideoView.setLocalVideoInfo(localVideoInfo);
        mVideoView.setOnVideoCellListener(videoCellListener);
        mVideoView.setShowingPip(isShowingPip);
        whiteboardLaodingView = view.findViewById(R.id.view_whiteboard_loading);
        mVideoView.setLocalVideoState(true);
    }

    @SuppressLint("CheckResult")
    public void onWhiteboardStart() {
        Observable.just(0).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                L.i(TAG, "onWhiteboardStart");
                if (mActivity.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    mVideoView.setLandscape(true);
                    NemoSDK.getInstance().setOrientation(Orientation.LANDSCAPE);
                }
                startWhiteboardView();
            }
        });
    }

    @SuppressLint("CheckResult")
    public void onWhiteboardStop() {
        Observable.just(0).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                stopWhiteboardView();
            }
        });
    }

    /**
     * 处理白板数据
     *
     * @param message 白板数据
     */
    @SuppressLint("CheckResult")
    public void onWhiteboardMessage(String message) {
        Observable.just(0).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                mVideoView.onWhiteBoardMessages(message);
            }
        });
    }

    @SuppressLint("CheckResult")
    public void onWhiteboardMessages(ArrayList<String> messages) {
        Observable.just(0).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                if (mActivity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        || mActivity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
                    mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    mVideoView.setLandscape(true);
                    NemoSDK.getInstance().setOrientation(Orientation.LANDSCAPE);
                }
                mVideoView.handleWhiteboardLinesMessage(messages);
            }
        });
    }

    @Override
    public void setLandscape(boolean landscape) {
        L.i(TAG, "setLandscape: " + landscape);
        mLandscape = landscape;
        if (mVideoView != null) {
            mVideoView.setLandscape(landscape);
        }
    }

    public boolean isLandscape() {
        return mLandscape;
    }

    public void unlockLayout() {
        mVideoView.unlockLayout();
    }

    @Override
    public void setVideoMute(boolean mute) {
        mVideoView.setMuteLocalVideo(mute, getString(R.string.call_video_mute));
    }

    @Override
    public void setAudioOnlyMode(boolean audioMode, boolean isLocalMute) {
        mVideoView.setAudioOnlyMode(audioMode, isLocalMute);
    }

    @Override
    public void setLocalVideoInfo(VideoInfo layoutInfo) {
        localVideoInfo = layoutInfo;
        if (mVideoView != null) {
            mVideoView.setLocalVideoInfo(layoutInfo);
        }
    }

    @Override
    public void setMicMute(boolean mute) {
        if (mVideoView != null) {
            mVideoView.setMuteLocalAudio(mute);
        }
    }

    @Override
    public void setRemoteVideoInfo(List<VideoInfo> videoInfos, boolean hasContent) {
        L.i(TAG, "setRemoteVideoInfos mVideoView: " + mVideoView);
        if (mVideoView != null) {
            mVideoView.setRemoteVideoInfos(videoInfos);
        }
    }

    @Override
    public void setCurrentIndex(int index) {

    }

    public boolean isShowingPip() {
        if (mVideoView != null) {
            return mVideoView.isShowingPip();
        }
        return true;
    }

    public void setShowingPip(boolean isShowingPip) {
        this.isShowingPip = isShowingPip;
        if (mVideoView != null) {
            mVideoView.setShowingPip(isShowingPip);
        }
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
    public void onResume() {
        super.onResume();
        L.i(TAG, "onResume");
        mVideoView.startRender();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        L.i(TAG, "onDestroy");
        if (mVideoView != null) {
            mVideoView.stopWhiteboard();
            mVideoView.destroy();
        }
    }

    private VideoCellLayout.SimpleVideoCellListener videoCellListener = new VideoCellLayout.SimpleVideoCellListener() {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e, WhiteBoardCell cell) {
            L.i("wang whiteboard click");
            if (videoCallback != null) {
                videoCallback.onWhiteboardClicked();
            }
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e, VideoCell cell) {
            Log.e(TAG, "speaker, cell : " + cell.getLayoutInfo());
            if (!SpeakerVideoGroup.isShowingWhiteboard()) {
                if (videoCallback != null) {
                    if (cell.isFullScreen() || cell.isLargeScreen()) {
                        videoCallback.onVideoCellSingleTapConfirmed(cell);
                    } else if (mVideoView.isLandscape()) {
                        mVideoView.lockLayout(cell.getLayoutInfo().getParticipantId());
                        videoCallback.onLockLayoutChanged(cell.getLayoutInfo().getParticipantId());
                    }
                }
            }
            return true;
        }

        @Override
        public void onFullScreenChanged(VideoCell cell) {
            if (videoCallback != null) {
                videoCallback.onFullScreenChanged(cell);
            }
        }

        @Override
        public void onWhiteboardMessageSend(String text) {
            NemoSDK.getInstance().sendWhiteboardData(text);
        }

        @Override
        public void onVideoCellGroupClicked(View group) {
            if (videoCallback != null) {
                videoCallback.onVideoCellGroupClicked(group);
            }
        }
    };

    /**
     * 开启白板
     */
    public void startWhiteboardView() {
        if (mVideoView != null) {
            if (whiteboardLaodingView.getVisibility() == View.VISIBLE) {
                whiteboardLaodingView.setVisibility(View.GONE);
            }
            mVideoView.startWhiteboard();
        }
    }

    /**
     * 关闭白板
     */
    public void stopWhiteboardView() {
        if (mVideoView != null) {
            mVideoView.stopWhiteboard();
            if (whiteboardLaodingView.getVisibility() == View.VISIBLE) {
                whiteboardLaodingView.setVisibility(View.GONE);
            }
        }
    }
}
