/**
 * Copyright (C) 2019 XYLink Android SDK Source Project
 * <p>
 * Created by wanghui on 2019/1/22.
 */
package com.xylink.sdk.sample.view;

import android.content.Context;
import android.log.L;
import android.util.AttributeSet;

import com.ainemo.module.call.data.Enums;
import com.ainemo.sdk.otf.VideoInfo;

import java.util.ArrayList;
import java.util.List;


/**
 * 画廊模式视频布局视图类（多个相同尺寸的屏的排列组合，如，吅，品，田等）
 */
public class GalleryVideoGroup extends VideoCellGroup {
    private static final String TAG = "GalleryVideoGroup";

    private static final int RATIO_HEIGHT = 9;

    private static final int RATIO_WIDTH = 16;

    private static final int THUMBNAIL_CELL_NUM = 6;
    private static final boolean IS_NINE_CELL_PER_PAGE = false;

    /**
     * 当前讲话者ID
     */
    private int activeSpeakerPid;

    /**
     * 锁定参会者ID
     */
    private int lockedPid;

    /**
     * 是否缩放
     */
    private boolean isZooming = false;

    public GalleryVideoGroup(Context context) {
        super(context);
    }

    public GalleryVideoGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GalleryVideoGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        super.init();
        mCellPadding = 0;
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mLandscape && (getWidth() > getHeight())) {
            computeLocalCellOrder();
            layoutChilds(l, t, r, b);
        } else if (!mLandscape && (getHeight() > getWidth())) {
            computeLocalCellOrder();
            layoutChilds(l, t, r, b);
        }
    }

    /**
     * 计算视频单元顺序
     */
    private synchronized void computeLocalCellOrder() {
        L.i(TAG, "before computeLocalCellOrder, mRemoteVideoCells.size : " + mRemoteVideoCells.size());
        for (VideoCell cell : mRemoteVideoCells) {
            if (cell.getId() == VideoCell.LOCAL_VIEW_ID) {
                mRemoteVideoCells.remove(cell);
                mRemoteVideoCells.add(0, cell);
                break;
            }
        }

        //第一屏和第二屏共享信息，需要删除邀请通话的信息
        for (VideoCell cell : mRemoteVideoCells) {
            if (cell.getLayoutInfo() != null && cell.getLayoutInfo().getLayoutVideoState() == Enums.LAYOUT_STATE_ADDOTHER) {
                mRemoteVideoCells.remove(cell);
            }
        }

        //UI just support max six channel video, such as one local and five remote
        if (mRemoteVideoCells.size() > THUMBNAIL_CELL_NUM) {
            List<VideoCell> toDel = mRemoteVideoCells.subList(THUMBNAIL_CELL_NUM, mRemoteVideoCells.size());

            for (VideoCell cell : toDel) {
                removeView(cell);
                mRemoteVideoCells.remove(cell);
            }
        }
        L.i(TAG, "after computeLocalCellOrder, mRemoteVideoCells.size : " + mRemoteVideoCells.size());
    }


    private void layoutChilds(int l, int t, int r, int b) {
        L.i(TAG, "layoutChilds, childCount : " + getChildCount() + ", lockedPid : " + lockedPid + ", mCurrentIndex : " + mCurrentIndex + ", mLandscape : " + mLandscape);

        if (lockedPid > 0) {
            layoutFullScreen(l, t, r, b);
        } else {
            mFullScreenVideoCell = null;
            if (onVideoCellListener != null) {
                onVideoCellListener.onFullScreenChanged(null);
            }

            //第二屏特殊对称布局,第三屏以后总是9宫格布局
            if (mCurrentIndex == 1) {
                switch (mRemoteVideoCells.size()) {
                    case 0:
                        break;
                    case 1:
                        layoutOneCell(l, t, r, b);
                        break;
                    case 2:
                        layoutTwoCells(l, t, r, b);
                        break;
                    case 3:
                        layoutThreeCells(l, t, r, b);
                        break;
                    case 4:
                        layoutFourCells(l, t, r, b);
                        break;
                    case 5:
                    case 6:
                        layoutFiveCells(l, t, r, b);
                        break;
                    default:
                        layoutNineCells(l, t, r, b);
                        break;
                }
            } else {
                if (mRemoteVideoCells.size() == 1 && mRemoteVideoCells.contains(mLocalVideoCell)) {
                    layoutOneCell(l, t, r, b);
                } else {
                    if (IS_NINE_CELL_PER_PAGE) {
                        layoutNineCells(l, t, r, b);
                    } else {
                        layoutFiveCells(l, t, r, b);
                    }
                }
            }

        }
    }

    /**
     * 布局全屏显示
     *
     * @param l
     * @param t
     * @param r
     * @param b
     */
    private void layoutFullScreen(int l, int t, int r, int b) {
        if (mRemoteVideoCells.size() >= 0) {
            //防止出现拖尾现象,先隐藏所有画面,在开启大画面
            for (VideoCell cell : mRemoteVideoCells) {
                cell.setVisibility(GONE);
            }

            for (VideoCell cell : mRemoteVideoCells) {
                if (cell.getLayoutInfo() != null && cell.getLayoutInfo().getParticipantId() == lockedPid) {
                    cell.setFullScreen(true);
                    cell.setRectVisible(false);
                    cell.setVisibility(VISIBLE);
                    cell.bringToFront();

                    if (mLandscape) {
                        if (cell.getLayoutInfo() != null && cell.getLayoutInfo().getVideoWidth() < cell.getLayoutInfo().getVideoHeight()) {
                            int height = b - t;
                            int width = height * 9 / 16;
                            if (cell.getLayoutInfo() != null && cell.getLayoutInfo().getVideoHeight() * 3 == cell.getLayoutInfo().getVideoWidth() * 4) {
                                width = height * 3 / 4;
                            }
                            cell.layout((r - l - width) / 2, t, (r - l + width) / 2, b);
                        } else {
                            cell.layout(l, t, r, b);
                        }
                    } else {
                        if (cell.getLayoutInfo() != null && cell.getLayoutInfo().getVideoWidth() > cell.getLayoutInfo().getVideoHeight()
                                || cell.getLayoutInfo() != null
                                && !cell.getLayoutInfo().getRemoteID().contains(Enums.DEVICE_TYPE_SOFT)) {
                            int height = (r - l) * RATIO_HEIGHT / RATIO_WIDTH;
                            int start = ((b - t) - height) / 2;
                            cell.layout(l, start, r, start + height);
                        } else {
                            cell.layout(l, t, r, b);
                        }
                    }

                    mFullScreenVideoCell = cell;
                    if (onVideoCellListener != null) {
                        onVideoCellListener.onFullScreenChanged(cell);
                    }

                    break;
                }
            }
        }
    }

    private void layoutCell(VideoCell cell, int l, int t, int r, int b) {
        L.i(TAG, "layoutCell, info : " + cell.getLayoutInfo() + ", l : " + l + ", t : " + t + ", r : " + r + "， b : " + b);
        cell.setFullScreen(false);
        //cell.setRectVisible(activeSpeakerPid == cell.getLayoutInfo().getParticipantId());
        cell.setRectVisible(false);
        cell.setVisibility(VISIBLE);
        cell.layout(l, t, r, b);
    }

    private void layoutOneCell(int l, int t, int r, int b) {
        int height = b - t;
        int width = height * RATIO_WIDTH / RATIO_HEIGHT;
        int left = (r - l - width) / 2;

        L.i(TAG, "layoutOneCell, cellWidth : " + width + ", cellHeight : " + height);

        if (mRemoteVideoCells.size() > 0) {
            //只有一屏时总是缩放
            //if (isZooming) {
            mRemoteVideoCells.get(0).layout(l, t, r, b);
            //} else {
            //    mRemoteVideoCells.get(0).layout(left, t, left + width, b);
            //}
            mRemoteVideoCells.get(0).setRectVisible(false);
            mRemoteVideoCells.get(0).setFullScreen(true);
            mRemoteVideoCells.get(0).setVisibility(VISIBLE);
        }
    }

    private void layoutTwoCells(int l, int t, int r, int b) {
        if (mRemoteVideoCells.size() == 2) {
            if (mLandscape) {
                int cellWidth = ((r - l) - mCellPadding) / 2;
                int cellHeight = cellWidth * RATIO_HEIGHT / RATIO_WIDTH;

                //local video
                layoutCell(mRemoteVideoCells.get(0), l, (b - t - cellHeight) / 2, cellWidth, (b - t - cellHeight) / 2 + cellHeight);
                //one remote
                layoutCell(mRemoteVideoCells.get(1), cellWidth + mCellPadding, (b - t - cellHeight) / 2, r, (b - t - cellHeight) / 2 + cellHeight);
            } else {
                int cellWidth = r - l;
                int cellHeight = cellWidth * RATIO_HEIGHT / RATIO_WIDTH;

                int left = l;
                int top = ((b - t) - (2 * cellHeight + mCellPadding)) / 2;
                int right = r;
                int bottom = top + cellHeight;

                layoutCell(mRemoteVideoCells.get(0), left, top, right, bottom);

                top = bottom + mCellPadding;
                bottom = top + cellHeight;

                layoutCell(mRemoteVideoCells.get(1), left, top, right, bottom);
            }
        }
    }

    private void layoutThreeCells(int l, int t, int r, int b) {
        if (mRemoteVideoCells.size() == 3) {
            if (mLandscape) {
                int cellHeight = ((b - t) - mCellPadding) / 2;
                int cellWidth = isZooming ? ((r - l) - mCellPadding) / 2 : cellHeight * RATIO_WIDTH / RATIO_HEIGHT;

                if (cellWidth * 2 > (r - l)) {
                    cellWidth = (r - l) / 2;
                }

                if (isZooming) {
                    //local video
                    layoutCell(mRemoteVideoCells.get(0), (r - l - cellWidth) / 2, t, (r - l - cellWidth) / 2 + cellWidth, cellHeight);
                    //one remote
                    layoutCell(mRemoteVideoCells.get(1), l, cellHeight + mCellPadding, cellWidth, b);
                    //two remote
                    layoutCell(mRemoteVideoCells.get(2), cellWidth + mCellPadding, cellHeight + mCellPadding, r, b);
                } else {
                    int left = (r - l - (2 * cellWidth + mCellPadding)) / 2;
                    int right = left + cellWidth + mCellPadding + cellWidth;
                    //local video
                    layoutCell(mRemoteVideoCells.get(0), (r - l - cellWidth) / 2, t, (r - l - cellWidth) / 2 + cellWidth, cellHeight);
                    //one remote
                    layoutCell(mRemoteVideoCells.get(1), left, cellHeight + mCellPadding, left + cellWidth, b);
                    //two remote
                    layoutCell(mRemoteVideoCells.get(2), left + cellWidth + mCellPadding, cellHeight + mCellPadding, right, b);
                }
            } else {
                int cellWidth = r - l;
                int cellHeight = cellWidth * RATIO_HEIGHT / RATIO_WIDTH;

                if (cellHeight * 3 > (b - t)) {
                    cellHeight = ((b - t) - 2 * mCellPadding) / 3;
                }

                int left = l;
                int top = ((b - t) - (3 * cellHeight + 2 * mCellPadding)) / 2;
                int right = r;
                int bottom = top + cellHeight;

                //local video
                layoutCell(mRemoteVideoCells.get(0), left, top, right, bottom);

                //one remote
                top = bottom + mCellPadding;
                bottom = top + cellHeight;
                layoutCell(mRemoteVideoCells.get(1), left, top, right, bottom);

                //two remot
                top = bottom + mCellPadding;
                bottom = top + cellHeight;
                layoutCell(mRemoteVideoCells.get(2), left, top, right, bottom);
            }
        }
    }

    private void layoutFourCells(int l, int t, int r, int b) {
        if (mRemoteVideoCells.size() == 4) {
            if (mLandscape) {
                int cellHeight = ((b - t) - mCellPadding) / 2;
                int cellWidth = isZooming ? ((r - l) - mCellPadding) / 2 : cellHeight * RATIO_WIDTH / RATIO_HEIGHT;

                if (cellWidth * 2 > (r - l)) {
                    cellWidth = (r - l) / 2;
                }

                if (isZooming) {
                    //local video
                    layoutCell(mRemoteVideoCells.get(0), l, t, cellWidth, cellHeight);
                    //one remote
                    layoutCell(mRemoteVideoCells.get(1), cellWidth + mCellPadding, t, r, cellHeight);
                    //two remote
                    layoutCell(mRemoteVideoCells.get(2), l, cellHeight + mCellPadding, cellWidth, b);
                    //three remote
                    layoutCell(mRemoteVideoCells.get(3), cellWidth + mCellPadding, cellHeight + mCellPadding, r, b);
                } else {
                    int left = (r - l - (2 * cellWidth + mCellPadding)) / 2;
                    int right = left + cellWidth + mCellPadding + cellWidth;

                    //local video
                    layoutCell(mRemoteVideoCells.get(0), left, t, left + cellWidth, cellHeight);
                    //one remote
                    layoutCell(mRemoteVideoCells.get(1), left + cellWidth + mCellPadding, t, right, cellHeight);
                    //two remote
                    layoutCell(mRemoteVideoCells.get(2), left, cellHeight + mCellPadding, left + cellWidth, b);
                    //three remote
                    layoutCell(mRemoteVideoCells.get(3), left + cellWidth + mCellPadding, cellHeight + mCellPadding, right, b);
                }
            } else {
                int cellWidth = (r - l - mCellPadding) / 2;
                int cellHeight = cellWidth * RATIO_HEIGHT / RATIO_WIDTH;

                int left = l;
                int top = ((b - t) - (2 * cellHeight + mCellPadding)) / 2;
                int right = cellWidth;
                int bottom = top + cellHeight;

                layoutCell(mRemoteVideoCells.get(0), left, top, right, bottom);

                left = right + mCellPadding;
                right = left + cellWidth;

                layoutCell(mRemoteVideoCells.get(1), left, top, right, bottom);

                left = l;
                top = bottom + mCellPadding;
                right = cellWidth;
                bottom = top + cellHeight;

                layoutCell(mRemoteVideoCells.get(2), left, top, right, bottom);

                left = right + mCellPadding;
                right = left + cellWidth;

                layoutCell(mRemoteVideoCells.get(3), left, top, right, bottom);
            }
        }
    }

    /**
     * 5-6个
     *
     * @param l
     * @param t
     * @param r
     * @param b
     */
    private void layoutFiveCells(int l, int t, int r, int b) {
        if (mRemoteVideoCells.size() >= 0) {
            if (mLandscape) {
                int cellWidth = ((r - l) - mCellPadding * 2) / 3;
                int cellHeight = cellWidth * RATIO_HEIGHT / RATIO_WIDTH;

                t = ((b - t) - (2 * cellHeight + mCellPadding)) / 2;

                for (int i = 0; i < mRemoteVideoCells.size(); i++) {
                    int left = l + (cellWidth + mCellPadding) * (i % 3);
                    int top = t + (cellHeight + mCellPadding) * (i / 3);
                    int right = left + cellWidth;
                    int bottom = top + cellHeight;
                    layoutCell(mRemoteVideoCells.get(i), left, top, right, bottom);
                }

            } else {
                int cellWidth = ((r - l) - mCellPadding) / 2;
                int cellHeight = cellWidth * RATIO_HEIGHT / RATIO_WIDTH;

                t = ((b - t) - (3 * cellHeight + 2 * mCellPadding)) / 2;

                for (int i = 0; i < mRemoteVideoCells.size(); i++) {
                    int left = l + (cellWidth + mCellPadding) * (i % 2);
                    int top = t + (cellHeight + mCellPadding) * (i / 2);
                    int right = left + cellWidth;
                    int bottom = top + cellHeight;
                    if (i % 2 == 0) {
                        layoutCell(mRemoteVideoCells.get(i), left, top, right, bottom);
                    } else {
                        layoutCell(mRemoteVideoCells.get(i), left, top, right, bottom);
                    }

                }
            }
        }
    }

    /**
     * 9宫格
     *
     * @param l
     * @param t
     * @param r
     * @param b
     */
    private void layoutNineCells(int l, int t, int r, int b) {
        int cellHeight = ((b - t) - mCellPadding * 2) / 3;
        int cellWidth = isZooming ? ((r - l) - mCellPadding * 2) / 3 : cellHeight * RATIO_WIDTH / RATIO_HEIGHT;

        L.i(TAG, "layoutNineCells, cellWidth : " + cellWidth + ", cellHeight : " + cellHeight);

        if (mRemoteVideoCells.size() >= 0) {
            if (isZooming) {
                for (int i = 0; i < mRemoteVideoCells.size(); i++) {
                    int left = l + (cellWidth + mCellPadding) * (i % 3);
                    int top = t + (cellHeight + mCellPadding) * (i / 3);
                    int right = left + cellWidth;
                    int bottom = top + cellHeight;
                    layoutCell(mRemoteVideoCells.get(i), left, top, right, bottom);
                }
            } else {
                l = (r - l - (3 * cellWidth + 2 * mCellPadding)) / 2;
                for (int i = 0; i < mRemoteVideoCells.size(); i++) {
                    int left = l + (cellWidth + mCellPadding) * (i % 3);
                    int top = t + (cellHeight + mCellPadding) * (i / 3);
                    int right = left + cellWidth;
                    int bottom = top + cellHeight;
                    layoutCell(mRemoteVideoCells.get(i), left, top, right, bottom);
                }
            }

        }

    }

    /**
     * 锁定布局
     *
     * @param pid
     */
    public void lockLayout(int pid) {
        L.i(TAG, "lockLayout, lockedPid : " + pid);
        if (lockedPid != pid) {
            lockedPid = pid;
            post(mLayoutRunnabler);
        }
    }

    /**
     * 解锁布局
     */
    public void unlockLayout() {
        L.i(TAG, "unlockLayout");
        if (lockedPid != 0) {
            lockedPid = 0;
            postDelayed(mLayoutRunnabler, 500);
        }
    }

    /**
     * 设置是否缩放
     *
     * @param zooming
     */
    public void setZooming(boolean zooming) {
        if (isZooming != zooming) {
            isZooming = zooming;
            L.i(TAG, "isZooming : " + isZooming);
            post(mLayoutRunnabler);
        }
    }

    /**
     * 是否缩放
     *
     * @return
     */
    public boolean isZooming() {
        return isZooming;
    }

    /**
     * 获取讲话者
     *
     * @return
     */
    public int getActiveSpeakerPid() {
        return activeSpeakerPid;
    }

    /**
     * 获取锁定者
     *
     * @return
     */
    public int getLockedPid() {
        return lockedPid;
    }


    /**
     * 检测讲话者和锁定全屏者ID
     *
     * @param infos
     */
    private void computeActiveSpeakerPid(List<VideoInfo> infos) {
        L.i(TAG, "before computeActiveSpeakerPid, activeSpeakerPid : " + activeSpeakerPid);

        //计算讲话者ID
        for (VideoInfo info : infos) {
            if (info.isActiveSpeaker()) {
                activeSpeakerPid = info.getParticipantId();
                break;
            }
            activeSpeakerPid = 0;
        }
        L.i(TAG, "after computeActiveSpeakerPid, activeSpeakerPid : " + activeSpeakerPid);
    }

    /**
     * 移除本地视图
     */
    public void removeLocalCell() {
        if (mRemoteVideoCells.contains(mLocalVideoCell)) {
            removeView(mLocalVideoCell);
            mRemoteVideoCells.remove(mLocalVideoCell);
        }
    }

    /**
     * 添加本地视图
     */
    public void addLocalCell() {
        if (!mRemoteVideoCells.contains(mLocalVideoCell)) {
            addView(mLocalVideoCell, 0);
            mRemoteVideoCells.add(0, mLocalVideoCell);
        }
    }


    private VideoCell createRemoteCell(VideoInfo layoutInfo, boolean playCreateAnimation) {
        L.i(TAG, "createRemoteCell, remoteVidoeInfo : " + layoutInfo);
        VideoCell cell = new VideoCell(playCreateAnimation, getContext(), this);
        cell.setLayoutInfo(layoutInfo);
        cell.bringToFront();
        addView(cell);
        return cell;
    }

    @Override
    public void createLocalCell(boolean isUvc) {
        L.i(TAG, "createLocalCell, isUvc : " + isUvc);
        mLocalVideoCell = new VideoCell(isUvc, false, getContext(), this);
        mLocalVideoCell.setId(VideoCell.LOCAL_VIEW_ID);
        mLocalVideoCell.bringToFront();
        mLocalVideoCell.setLayoutInfo(mLocalVideoInfo);
    }


    @Override
    public void setRemoteVideoInfos(List<VideoInfo> infos) {
        if (infos != null && infos.size() > 0) {
            mRemoteVideoInfos = infos;

            //调整local顺序
            if (mRemoteVideoCells.contains(mLocalVideoCell)) {
                mRemoteVideoCells.remove(mLocalVideoCell);
                mRemoteVideoCells.add(0, mLocalVideoCell);
            }

            //check the active speaker & fullscreen ID
            computeActiveSpeakerPid(infos);

            L.i(TAG, "setRemoteVideoInfos, mRemoteVideoInfos.size : " + mRemoteVideoInfos.size() + ", mRemoteVideoCells.size : " + mRemoteVideoCells.size());

            if (mRemoteVideoCells.size() > 0) {
                //1.compute none existed video cell
                List<VideoCell> toDel = new ArrayList<>();

                for (VideoCell cell : mRemoteVideoCells) {
                    for (int i = 0; i < mRemoteVideoInfos.size(); i++) {
                        VideoInfo info = mRemoteVideoInfos.get(i);
                        if (cell.getLayoutInfo() != null && cell.getLayoutInfo().getParticipantId() == info.getParticipantId()) {
                            break;
                        }

                        if (cell.getLayoutInfo() != null
                                && cell.getLayoutInfo().getLayoutVideoState().contains(Enums.LAYOUT_STATE_ADDOTHER)) {
                            break;
                        }

                        if (i == mRemoteVideoInfos.size() - 1) {
                            toDel.add(cell);
                        }
                    }
                }

                L.i(TAG, "setRemoteVideoInfos, toDel.size : " + toDel.size());

                //2.delete none existed video cell
                for (VideoCell cell : toDel) {
                    if (cell.getId() != VideoCell.LOCAL_VIEW_ID) {
                        removeView(cell);
                        mRemoteVideoCells.remove(cell);
                    }
                }

                toDel.clear();
            }


            //3.update already exist cell & add new cell
            if (mRemoteVideoCells.size() > 0) {
                for (VideoInfo info : mRemoteVideoInfos) {
                    for (int i = 0; i < mRemoteVideoCells.size(); i++) {
                        VideoCell cell = mRemoteVideoCells.get(i);

                        //update already exit cell
                        if (cell.getLayoutInfo() != null && info.getParticipantId() == cell.getLayoutInfo().getParticipantId()) {
                            android.util.Log.d(TAG, "oldInfo : " + cell.getLayoutInfo());
                            android.util.Log.d(TAG, "newInfo : " + info);
                            if (Enums.LAYOUT_STATE_MUTE.equals(info.getLayoutVideoState())
                                    || Enums.LAYOUT_STATE_REQUESTING.equals(info.getLayoutVideoState())
                                    || Enums.LAYOUT_STATE_AUDIO_ONLY.equals(info.getLayoutVideoState())) {
                                info.setVideoHeight(cell.getLayoutInfo().getVideoHeight());
                                info.setVideoWidth(cell.getLayoutInfo().getVideoWidth());
                            }
                            cell.setLayoutInfo(info);
                            break;
                        }

                        //add new cell
                        if (i == mRemoteVideoCells.size() - 1) {
                            mRemoteVideoCells.add(createRemoteCell(info, false));
                        }
                    }
                }

                //reorder video cell
                for (int i = 0; i < mRemoteVideoInfos.size(); i++) {
                    for (int j = 0; j < mRemoteVideoCells.size(); j++) {
                        VideoCell cell = mRemoteVideoCells.get(j);
                        if (cell.getLayoutInfo() != null && mRemoteVideoInfos.get(i).getParticipantId() == cell.getLayoutInfo().getParticipantId()) {
                            if (i < j) {
                                mRemoteVideoCells.remove(cell);
                                mRemoteVideoCells.add(i, cell);
                                break;
                            }
                        }
                    }
                }
            } else {
                for (VideoInfo info : mRemoteVideoInfos) {
                    mRemoteVideoCells.add(createRemoteCell(info, false));
                }
            }

            L.i(TAG, "setRemoteVideoInfos,  mRemoteVideoCells.size : " + mRemoteVideoCells.size());

            //4.relayout accordingly child count & others
            post(mLayoutRunnabler);
            //postDelayed(mLayoutRunnabler, 200);
        } else {
            //1. clean ui element
            for (VideoCell cell : mRemoteVideoCells) {
                if (cell.getId() != VideoCell.LOCAL_VIEW_ID) {
                    removeView(cell);
                    mRemoteVideoCells.remove(cell);
                }
            }

            //2. relayout accordingly child count & others
            post(mLayoutRunnabler);
            //postDelayed(mLayoutRunnabler, 200);

            //3. clean up data
            if (mRemoteVideoInfos != null) {
                mRemoteVideoInfos.clear();
            }
        }
    }
}
