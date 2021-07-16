package com.xylink.sdk.sample.view;

import android.view.View;

public interface VideoCallback {
    /**
     * 单击
     *
     * @param cell
     * @return
     */
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
     * 白板消息发送
     *
     * @param msg
     */
    void onWhiteboardMessageSend(String msg);

    /**
     * 视频单元父视图被单击
     *
     * @param group
     */
    void onVideoCellGroupClicked(View group);

    /**
     * 挂断邀请用户
     *
     * @param cell
     */
    void onCancelAddother(VideoCell cell);
}
