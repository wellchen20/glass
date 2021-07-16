package com.xylink.sdk.sample;

import android.log.L;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.ainemo.sdk.otf.RosterWrapper;
import com.ainemo.sdk.otf.VideoInfo;

import java.util.ArrayList;
import java.util.List;

public class VideoPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = "VideoPagerAdapter";

    private List<VideoFragment> fragments = new ArrayList<>();

    private VideoFragment.VideoCallback videoCallback;

    private int totalPage = 1;

    private boolean landscape;

    private int currentIndex;

    public VideoPagerAdapter(FragmentManager fm) {
        super(fm);
        fragments.add(SpeakerVideoFragment.newInstance(0));
        fragments.add(GalleryVideoFragment.newInstance(1));
        fragments.add(GalleryVideoFragment.newInstance(2));
        fragments.add(GalleryVideoFragment.newInstance(3));
        fragments.add(GalleryVideoFragment.newInstance(4));

        for (int i = 0; i < fragments.size(); i++) {
            fragments.get(i).setCurrentIndex(i);
        }
    }

    public void setCurrentIndex(int index) {
        L.i(TAG, "setCurrentIndex, index : " + index);
        if (currentIndex != index) {
            currentIndex = index;
            setVideoCallback(videoCallback);
            setLandscape(landscape);
        }
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setLandscape(boolean landscape) {
        this.landscape = landscape;
        if (fragments.size() > currentIndex && fragments.get(currentIndex) != null) {
            VideoFragment fragment = fragments.get(currentIndex);
            fragment.setLandscape(landscape);
        }
    }

    @Override
    public VideoFragment getItem(int position) {
        return fragments.get(position);
    }

    public List<VideoFragment> getFragments() {
        return fragments;
    }

    @Override
    public int getCount() {
        return totalPage;
    }

    public void setTotalMeetingMember(int totalMeetingMember) {
        L.i(TAG, "setTotalMeetingMember: " + totalMeetingMember);
        if (totalMeetingMember == 1) {
            totalPage = 1;
        } else if (totalMeetingMember > 1 && totalMeetingMember <= 7) {
            totalPage = 2;
        } else {
            int num = totalMeetingMember - 7;
            int count = num / 6;
            int lastPageNum = num % 6;
            if (count >= 3) {
                lastPageNum = 0;
            }
            totalPage = 2 + count + (lastPageNum != 0 ? 1 : 0);
            if (totalPage > 5) {
                totalPage = 5;
            }
        }
        notifyDataSetChanged();
    }

    public void setLocalVideoInfo(VideoInfo layoutInfo) {
        L.i(TAG, "setLocalVideoInfo, layoutInfo : " + layoutInfo + ", fragment.size : " + fragments.size());
        if (fragments != null && fragments.size() > 0) {
            for (VideoFragment fragment : fragments) {
                fragment.setLocalVideoInfo(layoutInfo);
            }
        }
    }

    public void setVideoCallback(VideoFragment.VideoCallback callback) {
        L.i(TAG, "setVideoCallback, callback : " + callback);
        videoCallback = callback;
        for (int i = 0; i < fragments.size(); i++) {
            VideoFragment fragment = fragments.get(i);
            if (i != currentIndex) {
                fragment.setVideoCallback(null);
            } else {
                fragment.setVideoCallback(callback);
            }
        }
    }

    public void setLocalVideoMute(boolean mute) {
        VideoFragment spkFragment = fragments.get(0);
        VideoFragment galleryFragment = fragments.get(1);
        spkFragment.setVideoMute(mute);
        galleryFragment.setVideoMute(mute);
    }

    public void setLocalMicMute(boolean mute) {
        VideoFragment spkFragment = fragments.get(0);
        VideoFragment galleryFragment = fragments.get(1);
        spkFragment.setMicMute(mute);
        galleryFragment.setMicMute(mute);
    }

    public void onWhiteboardStart() {
        SpeakerVideoFragment videoFragment = (SpeakerVideoFragment) fragments.get(0);
        videoFragment.onWhiteboardStart();
    }

    public void onWhiteboardStop() {
        SpeakerVideoFragment videoFragment = (SpeakerVideoFragment) fragments.get(0);
        videoFragment.onWhiteboardStop();
    }

    public void onWhiteboardMessage(String message) {
        SpeakerVideoFragment videoFragment = (SpeakerVideoFragment) fragments.get(0);
        videoFragment.onWhiteboardMessage(message);
    }

    public void onWhiteboardMessages(ArrayList<String> messages) {
        SpeakerVideoFragment videoFragment = (SpeakerVideoFragment) fragments.get(0);
        videoFragment.onWhiteboardMessages(messages);
    }
}
