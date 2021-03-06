package com.xylink.sdk.sample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.log.L;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringDef;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ainemo.module.call.data.Enums;
import com.ainemo.module.call.data.FECCCommand;
import com.ainemo.module.call.data.NewStatisticsInfo;
import com.ainemo.module.call.data.RemoteUri;
import com.ainemo.sdk.model.AIParam;
import com.ainemo.sdk.otf.ContentType;
import com.ainemo.sdk.otf.LayoutElement;
import com.ainemo.sdk.otf.LayoutPolicy;
import com.ainemo.sdk.otf.NemoSDK;
import com.ainemo.sdk.otf.NemoSDKListener;
import com.ainemo.sdk.otf.Orientation;
import com.ainemo.sdk.otf.RecordCallback;
import com.ainemo.sdk.otf.RosterWrapper;
import com.ainemo.sdk.otf.VideoInfo;
import com.ainemo.sdk.otf.WhiteboardChangeListener;
import com.ainemo.shared.UserActionListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.rokid.glass.instruct.InstructionManager;
import com.rokid.glass.instruct.Integrate.IInstruction;
import com.rokid.glass.instruct.entity.EntityKey;
import com.rokid.glass.instruct.entity.IInstructReceiver;
import com.rokid.glass.instruct.entity.InstructConfig;
import com.rokid.glass.instruct.entity.InstructEntity;
import com.rokid.glass.instruct.type.NumberKey;
import com.rokid.glass.instruct.type.NumberTypeControler;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.xylink.sdk.sample.face.FaceView;
import com.xylink.sdk.sample.share.ShareState;
import com.xylink.sdk.sample.share.SharingValues;
import com.xylink.sdk.sample.share.picture.CirclePageIndicator;
import com.xylink.sdk.sample.share.picture.Glide4Engine;
import com.xylink.sdk.sample.share.picture.PicturePagerAdapter;
import com.xylink.sdk.sample.share.screen.ScreenPresenter;
import com.xylink.sdk.sample.utils.ActivityUtils;
import com.xylink.sdk.sample.utils.CommonTime;
import com.xylink.sdk.sample.utils.GalleryLayoutBuilder;
import com.xylink.sdk.sample.utils.LayoutMode;
import com.xylink.sdk.sample.utils.SpeakerLayoutBuilder;
import com.xylink.sdk.sample.utils.TextUtils;
import com.xylink.sdk.sample.utils.VolumeManager;
import com.xylink.sdk.sample.uvc.UVCCameraPresenter;
import com.xylink.sdk.sample.view.CustomAlertDialog;
import com.xylink.sdk.sample.view.Dtmf;
import com.xylink.sdk.sample.view.FeccBar;
import com.xylink.sdk.sample.view.SpeakerVideoGroup;
import com.xylink.sdk.sample.view.StatisticsRender;
import com.xylink.sdk.sample.view.VideoCell;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import vulture.module.call.nativemedia.NativeDataSourceManager;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * ????????????demo:
 * ???????????????????????????, ????????????, ??????, ????????????, ????????????, ????????????????????????, ????????????????????????????????????????????????
 * <p>
 * ??????????????????, ??????????????????????????????, ???CallPresenter(????????????), ScreenPresenter(????????????)???????????????,
 * {@link XyCallPresenter#start()} ????????????, XyCallPresenter??????????????????{@link XyCallActivity}????????????
 * <p>
 * Note: ????????????: ??????, ??????, ???????????????????????????, ?????????????????????content, ????????????????????????????????????????????????????????????
 * ???????????????????????????????????????????????????????????????. demo?????????????????????, ???????????????????????????, ???????????????.
 * <p>
 * ???????????????????????? <>http://openapi.xylink.com/android/</>
 */
public class XyCallActivity extends AppCompatActivity implements View.OnClickListener,
        XyCallContract.View, VideoFragment.VideoCallback, IInstruction {
    private static final String TAG = "XyCallActivity";
    private final static int REFRESH_STATISTICS_INFO_DELAYED = 2000;
    private XyCallContract.Presenter callPresenter;
    private View viewToolbar;
    private ImageView ivNetworkState; // ??????
    private TextView tvCallDuration; // ????????????
    private TextView toolbarCallNumber; // ??????
    private ImageButton ibDropCall; // ??????
    private ImageButton btMore; // ??????
    private ImageButton btnMoreShare; // ??????
    private ImageButton btnMoreHostMeeting; // ????????????
    private ImageButton btMuteMic; // ??????
    private TextView tvMuteMic; // ??????
    private ImageButton btCloseVideo; // ????????????
    private TextView tvCloseVideo; // ????????????
    private LinearLayoutCompat llMoreDialog; // ??????dialog
    private TextView tvMoreRecord;
    private TextView tvMoreCallMode;
    private TextView tvKeyboared; // ??????
    private TextView tvClosePip; // ???????????????
    private TextView tvWhiteboard; // ??????
    private TextView tvShareScreen; // ????????????
    private TextView tvSharePhoto; // ????????????
    private LinearLayout llRecording;
    private TextView tvRecordingDuration; // ????????????
    private LinearLayout llLockPeople; // ???????????????
    private LinearLayout llSwitchCamera; // ???????????????
    private ImageButton btSwitchCamera; // ???????????????

    private View shareScreenView;
    private View volumeView; // ???????????????
    private View viewInvite; // ???????????????
    private TextView tvInviteNumber; // ???????????????
    private View viewCallDetail; // ??????/????????????UI
    private TextView tvCallNumber; // number
    private TextView tvCallTips;
    private ImageButton btCallAccept; // ????????????
    private ViewPager pagerPicture; // ????????????
    private CirclePageIndicator pageIndicator;
    private ImageView ivRecordStatus;
    private FeccBar feccBar;
    private View dtmfLayout;
    private Dtmf dtmf;
    private ConstraintLayout root;
    private WebView webView; // ??????
    private Toolbar hostMeetingToolbar;
    private LinearLayout llHostMeeting;

    private boolean isToolbarShowing = false; // toolbar????????????
    private boolean audioMode = false;
    private boolean isMuteBtnEnable = true;
    private String muteStatus = null;
    private boolean defaultCameraFront = false; // ?????????????????????
    private boolean isVideoMute = false;
    private boolean isStartRecording = true;
    private boolean isShowingPip = true;
    private boolean isSharePicture = false;
    private int inviteCallIndex = -1;
    private LayoutMode layoutMode = LayoutMode.MODE_SPEAKER;
    private VideoInfo fullVideoInfo;
    private boolean isCallStart;
    private List<VideoInfo> mRemoteVideoInfos;
    // ?????????????????????, ???????????????
    private List<VideoInfo> firstPagerVideoInfo;

    private static final int sDefaultTimeout = 5000;
    private Handler handler = new Handler();

    private CompositeDisposable compositeDisposable;
    private VolumeManager mVolumeManager;

    // ??????????????????
    private OrientationEventListener orientationEventListener;
    private boolean enableScreenOrientation = false;

    // share screen
    private ScreenPresenter screenPresenter;
    private static final int REQUEST_CODE_CHOOSE = 23;

    // ????????????
    private PicturePagerAdapter picturePagerAdapter;
    private List<String> picturePaths;
    private String outgoingNumber;
    private ShareState shareState = ShareState.NONE;

    // uvc
    private boolean isNeedUVC = true;
    private UVCCameraPresenter uvcCameraPresenter;
    private LinearLayoutCompat llShareMore;

    private StatisticsRender mStatisticsRender;
    private ViewPagerNoSlide videoPager;
    private VideoPagerAdapter videoPagerAdapter;
    private int currentPagerIndex = 0;
    private CirclePageIndicator videoPagerIndicator;
    private MyVideoPagerListener myVideoPagerListener;
    private Observable<Boolean> confMgmtStateObserver;


    protected InstructionManager mInstructionManager;
    protected InstructionManager.IInstructionListener mInstructionListener = new InstructionManager.IInstructionListener() {
        @Override
        public boolean onReceiveCommand(String command) {
            return doReceiveCommand(command);
        }

        @Override
        public void onHelpLayerShow(boolean show) {

        }
    };




    @Override
    public boolean closeInstruction() {
        return false;
    }

    @Override
    public InstructConfig configInstruct() {
        InstructConfig config = new InstructConfig();
        config.setActionKey(JoinMeetingActivity.class.getName() + InstructConfig.ACTION_SUFFIX)
                .addInstructEntity(
                        new InstructEntity()
                                .addEntityKey(new EntityKey("????????????", "jie shu hui yi"))
                                .addEntityKey(new EntityKey(EntityKey.Language.en, "last one"))
                                .setShowTips(true)
                                .setCallback(new IInstructReceiver() {
                                    @Override
                                    public void onInstructReceive(Activity act, String key, InstructEntity instruct) {
                                        NemoSDK.getInstance().hangup();
                                        NemoSDK.getInstance().releaseLayout();
                                        NemoSDK.getInstance().releaseCamera();
                                        finish();
                                    }
                                })

        )
                .addInstructList(NumberTypeControler.doTypeControl(1, 20,
                        new NumberTypeControler.NumberTypeCallBack() {
                            @Override
                            public void onInstructReceive(Activity act, String key, int number, InstructEntity instruct) {
//                                Log.e(TAG, "AudioAi Number onInstructReceive command = " + key + ", number = " + number);
                                List<VideoInfo> videoInfoList =null;
                                videoInfoList= NemoSDK.getInstance().getLastVideoInfos();
                                videoInfoList.add(0,buildLocalLayoutInfo());
                                for (int i=0;i<videoInfoList.size();i++){
                                    Log.e(TAG, "getParticipantId: "+videoInfoList.get(i).getParticipantId()+"|"+videoInfoList.get(i).getRemoteName()+"|"+videoInfoList.get(i).getDataSourceID()+"|"+videoInfoList.get(i).getRemoteID());
                                }
                                Log.e(TAG, "videoInfoList: "+videoInfoList.size());
                                if (number<=videoInfoList.size()){
                                    Log.e(TAG, "onInstructReceive: "+ videoInfoList.get(number-1).getParticipantId());
                                    NemoSDK.getInstance().forceLayout(videoInfoList.get(number-1).getParticipantId());
                                    /*if (number==1){

                                        NemoSDK.getInstance().forceLayout(0);
                                        onVideoCellSingleTapConfirmed(null);
//                                        myVideoPagerListener.onPageSelected(1);
                                    }
                                    else {
                                        NemoSDK.getInstance().forceLayout(videoInfoList.get(number-1).getParticipantId());
                                    }*/
                                }
                            }
                        },
                        new NumberKey(EntityKey.Language.zh, "?????????", "???", "??????????????????1/2.../20???"),
                        new NumberKey(EntityKey.Language.en, "the", "page", "the 1/2.../20 page")
                        )
                ). addInstructEntity(
                new InstructEntity()
                        .addEntityKey(new EntityKey("??????", "jing yin"))
                        .addEntityKey(new EntityKey(EntityKey.Language.en, "close mike"))
                        .setShowTips(false)
                        .setCallback(new IInstructReceiver() {
                            @Override
                            public void onInstructReceive(Activity act, String key, InstructEntity instruct) {
                                hideOrShowToolbar(isToolbarShowing);
                                updateMuteStatus(true);
                                Toast.makeText(XyCallActivity.this,"??????",Toast.LENGTH_SHORT).show();
                            }
                        })
        ). addInstructEntity(
                new InstructEntity()
                        .addEntityKey(new EntityKey("????????????", "qu xiao jing yin"))
                        .addEntityKey(new EntityKey(EntityKey.Language.en, "open mike"))
                        .setShowTips(false)
                        .setCallback(new IInstructReceiver() {
                            @Override
                            public void onInstructReceive(Activity act, String key, InstructEntity instruct) {
                                hideOrShowToolbar(isToolbarShowing);
                                updateMuteStatus(false);
                                Toast.makeText(XyCallActivity.this,"????????????",Toast.LENGTH_SHORT).show();
                            }
                        })
        ). addInstructEntity(
                new InstructEntity()
                        .addEntityKey(new EntityKey("????????????", "da kai shi pin"))
                        .addEntityKey(new EntityKey(EntityKey.Language.en, "open video"))
                        .setShowTips(false)
                        .setCallback(new IInstructReceiver() {
                            @Override
                            public void onInstructReceive(Activity act, String key, InstructEntity instruct) {
                                hideOrShowToolbar(isToolbarShowing);
                                NemoSDK.getInstance().setVideoMute(false);
                                setVideoState(false);
                            }
                        })
        ). addInstructEntity(
                new InstructEntity()
                        .addEntityKey(new EntityKey("????????????", "guan bi shi pin"))
                        .addEntityKey(new EntityKey(EntityKey.Language.en, "close video"))
                        .setShowTips(false)
                        .setCallback(new IInstructReceiver() {
                            @Override
                            public void onInstructReceive(Activity act, String key, InstructEntity instruct) {
                                hideOrShowToolbar(isToolbarShowing);
                                isVideoMute = !isVideoMute;
                                NemoSDK.getInstance().setVideoMute(true);
                                setVideoState(true);
                            }
                        })
        ). addInstructEntity(
                new InstructEntity()
                        .addEntityKey(new EntityKey("????????????", "kai shi lu zhi"))
                        .addEntityKey(new EntityKey(EntityKey.Language.en, "start record"))
                        .setShowTips(false)
                        .setCallback(new IInstructReceiver() {
                            @Override
                            public void onInstructReceive(Activity act, String key, InstructEntity instruct) {
                                hideOrShowToolbar(isToolbarShowing);
                                if (NemoSDK.getInstance().isAuthorize()) {
                                    setRecordVideo(true);
                                } else {
                                    Toast.makeText(XyCallActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
        ). addInstructEntity(
                new InstructEntity()
                        .addEntityKey(new EntityKey("????????????", "ting zhi lu zhi"))
                        .addEntityKey(new EntityKey(EntityKey.Language.en, "stop record"))
                        .setShowTips(false)
                        .setCallback(new IInstructReceiver() {
                            @Override
                            public void onInstructReceive(Activity act, String key, InstructEntity instruct) {
                                hideOrShowToolbar(isToolbarShowing);
                                if (NemoSDK.getInstance().isAuthorize()) {
                                    setRecordVideo(false);
                                } else {
                                    Toast.makeText(XyCallActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
        );
        return config;
    }
//    @Override
//    protected void onStart() {
//        super.onStart();
//        if (mInstructionManager != null) {
//            mInstructionManager.onStart();
//        }
//
//    }+--
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (mInstructionManager != null) {
//            mInstructionManager.onDestroy();
//            mInstructionManager = null;
//        }
//
//    }
//
//    @Override
//    protected void onResume() {
//        if (mInstructionManager != null) {
//            mInstructionManager.onResume();
//        }
//
//        super.onResume();
//    }
//
//    @Override
//    protected void onPause() {
//
//        if (mInstructionManager != null) {
//            mInstructionManager.onPause();
//        }
//
//        super.onPause();
//    }

    @Override
    public boolean doReceiveCommand(String command) {
        return false;
    }

    @Override
    public void onInstrucUiReady() {

    }

    @StringDef({
            MuteStatus.HAND_UP, MuteStatus.HAND_DOWN, MuteStatus.END_SPEACH
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface MuteStatus {
        String HAND_UP = "HAND_UP";
        String HAND_DOWN = "HAND_DOWN";
        String END_SPEACH = "END_SPEACH";
    }

    @IntDef({
            VideoStatus.VIDEO_STATUS_NORMAL, VideoStatus.VIDEO_STATUS_LOW_AS_LOCAL_BW,
            VideoStatus.VIDEO_STATUS_LOW_AS_LOCAL_HARDWARE, VideoStatus.VIDEO_STATUS_LOW_AS_REMOTE,
            VideoStatus.VIDEO_STATUS_NETWORK_ERROR, VideoStatus.VIDEO_STATUS_LOCAL_WIFI_ISSUE
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface VideoStatus {
        int VIDEO_STATUS_NORMAL = 0;
        int VIDEO_STATUS_LOW_AS_LOCAL_BW = 1;
        int VIDEO_STATUS_LOW_AS_LOCAL_HARDWARE = 2;
        int VIDEO_STATUS_LOW_AS_REMOTE = 3;
        int VIDEO_STATUS_NETWORK_ERROR = 4;
        int VIDEO_STATUS_LOCAL_WIFI_ISSUE = 5;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_call);
        new XyCallPresenter(this); // init presenter
        compositeDisposable = new CompositeDisposable();
        initView();
        initListener();
        initData();
        callPresenter.start(); // Note: business start here,??????????????????
        mInstructionManager = new InstructionManager(XyCallActivity.this, closeInstruction(), configInstruct(), mInstructionListener);
    }

    @Override
    public void setPresenter(XyCallContract.Presenter presenter) {
        callPresenter = presenter;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (uvcCameraPresenter != null) {
            uvcCameraPresenter.onStart();
        }
        defaultCameraFront = NemoSDK.defaultCameraId() == 1;
        NemoSDK.getInstance().releaseCamera();
        NemoSDK.getInstance().requestCamera();

        if (mInstructionManager != null) {
            mInstructionManager.onStart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (screenPresenter != null) {
            screenPresenter.hideFloatView();
        }
        if (mInstructionManager != null) {
            mInstructionManager.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mInstructionManager != null) {
            mInstructionManager.onPause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && screenPresenter != null && screenPresenter.isSharingScreen()) {
            screenPresenter.onStop();
        }
        if (uvcCameraPresenter != null) {
            uvcCameraPresenter.onStop();
        }
        // ??????????????????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!ActivityUtils.isAppForeground(this)) {
                Toast.makeText(XyCallActivity.this, "??????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Intercept back event
        hideHostMeeting();
    }

    // remember to release resource when destroy
    @Override
    public void onDestroy() {
        L.i(TAG, "wang on destroy");
        if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
        if (screenPresenter != null) {
            screenPresenter.onDestroy();
        }
        orientationEventListener.disable();
        pictureData = null;
        mVolumeManager.onDestory();
        List<VideoFragment> fragments = videoPagerAdapter.getFragments();
        for (VideoFragment videoFragment : fragments) {
            videoFragment.onDestroy();
        }
        unbindService(xyCallConnection);
        if (mInstructionManager != null) {
            mInstructionManager.onDestroy();
            mInstructionManager = null;
        }
        super.onDestroy();
    }

    private void initView() {
        root = findViewById(R.id.root);

        viewToolbar = findViewById(R.id.group_visibility);
        ivNetworkState = findViewById(R.id.network_state);
        tvCallDuration = findViewById(R.id.network_state_timer);
        toolbarCallNumber = findViewById(R.id.tv_call_number);
        ibDropCall = findViewById(R.id.drop_call);
        btMore = findViewById(R.id.hold_meeting_more);
        btnMoreShare = findViewById(R.id.btn_more_share);
        btnMoreHostMeeting = findViewById(R.id.btn_more_host_meeting);
        btMuteMic = findViewById(R.id.mute_mic_btn);
        tvMuteMic = findViewById(R.id.mute_mic_btn_label);
        btCloseVideo = findViewById(R.id.close_video);
        tvCloseVideo = findViewById(R.id.video_mute_text);
        llMoreDialog = findViewById(R.id.more_layout_dialog);
        tvKeyboared = findViewById(R.id.keyboard);
        tvMoreRecord = findViewById(R.id.tv_more_record);
        tvMoreCallMode = findViewById(R.id.tv_nore_call_mode);
        tvClosePip = findViewById(R.id.textView2);
        tvWhiteboard = findViewById(R.id.tv_whiteboard);
        tvShareScreen = findViewById(R.id.tv_share_screen);
        tvSharePhoto = findViewById(R.id.tv_share_photo);
        ivRecordStatus = findViewById(R.id.video_recording_icon);
        llRecording = findViewById(R.id.conversation_recording_layout);
        tvRecordingDuration = findViewById(R.id.video_recording_timer);
        llLockPeople = findViewById(R.id.layout_lock_people);
        llSwitchCamera = findViewById(R.id.switch_camera_layout);
        btSwitchCamera = findViewById(R.id.switch_camera);
//        whiteboardLaodingView = findViewById(R.id.view_whiteboard_loading);
        shareScreenView = findViewById(R.id.share_screen);
        volumeView = findViewById(R.id.operation_volume_brightness);
        llShareMore = findViewById(R.id.ll_share_more);
        // ???????????????
        viewInvite = findViewById(R.id.view_call_invite);
        viewInvite.findViewById(R.id.bt_invite_accept).setOnClickListener(this);
        viewInvite.findViewById(R.id.bt_invite_drop).setOnClickListener(this);
        tvInviteNumber = viewInvite.findViewById(R.id.tv_invite_number);
        // ??????/??????UI
        viewCallDetail = findViewById(R.id.view_call_detail);
        viewCallDetail.findViewById(R.id.bt_call_drop).setOnClickListener(this);
        btCallAccept = viewCallDetail.findViewById(R.id.bt_call_accept);
        tvCallNumber = viewCallDetail.findViewById(R.id.tv_call_name);
        tvCallTips = viewCallDetail.findViewById(R.id.tv_call_tips);
        // ????????????
        pagerPicture = findViewById(R.id.pager_picture);
        pageIndicator = findViewById(R.id.pager_indicator);
        //FECC
        feccBar = findViewById(R.id.fecc_bar);
        feccBar.setFeccListener(new FeccActionListener());
        // ??????
        dtmfLayout = findViewById(R.id.dtmf);
        llHostMeeting = findViewById(R.id.ll_host_meeting);
        hostMeetingToolbar = findViewById(R.id.hold_meeting_toolbar);
        hostMeetingToolbar.setNavigationOnClickListener(v -> onBackPressed());
        webView = findViewById(R.id.webview);
        WebSettings settings = webView.getSettings();
        webView.removeJavascriptInterface("accessibility");
        webView.removeJavascriptInterface("searchBoxJavaBridge");
        webView.removeJavascriptInterface("accessibilityTraversal");
        settings.setSavePassword(false);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setLoadsImagesAutomatically(true); // ????????????
        settings.setAllowFileAccess(true);
        settings.setAppCacheEnabled(true);
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);

        // ????????????
        ViewStub stub = (ViewStub) findViewById(R.id.view_statistics_info);
        mStatisticsRender = new StatisticsRender(stub, this::stopRefreshStatisticsInfo);

        videoPagerIndicator = findViewById(R.id.pager_indicator_video);
        videoPager = findViewById(R.id.video_pager);
        videoPagerAdapter = new VideoPagerAdapter(getSupportFragmentManager());
        videoPagerAdapter.setVideoCallback(this);
        videoPager.setAdapter(videoPagerAdapter);

        videoPagerIndicator.setViewPager(videoPager);
        myVideoPagerListener = new MyVideoPagerListener();
        videoPagerIndicator.setOnPageChangeListener(myVideoPagerListener);
    }

    private void initListener() {
        ibDropCall.setOnClickListener(this);
        btCallAccept.setOnClickListener(this);
        btnMoreShare.setOnClickListener(this);
        btnMoreHostMeeting.setOnClickListener(this);
        btMuteMic.setOnClickListener(this);
        btCloseVideo.setOnClickListener(this);
        btMore.setOnClickListener(this);
        tvKeyboared.setOnClickListener(this);
        tvMoreRecord.setOnClickListener(this);
        tvMoreCallMode.setOnClickListener(this);
        tvClosePip.setOnClickListener(this);
        tvWhiteboard.setOnClickListener(this);
        tvShareScreen.setOnClickListener(this);
        tvSharePhoto.setOnClickListener(this);
        llLockPeople.setOnClickListener(this);
        btSwitchCamera.setOnClickListener(this);
        feccBar.initFeccEventListeners();

        ivNetworkState.setOnClickListener(v -> startRefreshStatisticsInfo());
    }

    private void initData() {
        // ??????
        dtmf = new Dtmf(dtmfLayout, key -> {
            if (buildLocalLayoutInfo() != null) {
                if (mRemoteVideoInfos != null && mRemoteVideoInfos.size() > 0) {
                    NemoSDK.getInstance().sendDtmf(mRemoteVideoInfos.get(0).getRemoteID(), key);
                }
            }
        });

        // ?????? & ??????
        Intent intent = getIntent();
        boolean isIncomingCall = intent.getBooleanExtra("isIncomingCall", false);
        if (isIncomingCall) {
            final int callIndex = intent.getIntExtra("callIndex", -1);
            inviteCallIndex = callIndex;
            String callerName = intent.getStringExtra("callerName");
            String callNumber = intent.getStringExtra("callerNumber");
            toolbarCallNumber.setText(callNumber);
            Log.i(TAG, "showIncomingCallDialog=" + callIndex);
            showCallIncoming(callIndex, callNumber, callerName);
        } else {
            outgoingNumber = intent.getStringExtra("number");
            showCallOutGoing(outgoingNumber);
            L.i(TAG, "outgoing number: " + outgoingNumber);
        }

        mVolumeManager = new VolumeManager(this, volumeView, AudioManager.STREAM_VOICE_CALL);
        mVolumeManager.setMuteCallback(mute -> NemoSDK.getInstance().setSpeakerMute(mute));

        // ??????????????????(??????????????????, ???????????????????????????????????????)
        NemoSDK.getInstance().registerWhiteboardChangeListener(whiteboardChangeListener);

        // ???????????????, ????????????????????????????????? ,  enableScreenOrientation = false
        orientationEventListener = new YourOrientationEventListener(XyCallActivity.this);
        orientationEventListener.enable();
        enableScreenOrientation = true;

        // add for: uvc, ???????????????????????????
//        if (isNeedUVC) {
//            uvcCameraPresenter = new UVCCameraPresenter(this);
//        }

        Intent backgroundCallService = new Intent(this, BackgroundCallService.class);
        bindService(backgroundCallService, xyCallConnection, Context.BIND_AUTO_CREATE);
    }

    private void hideOrShowToolbar(boolean show) {
        if (show) {
            hideToolbar();
        } else {
            showToolbar(sDefaultTimeout);
        }
    }

    private final Runnable mFadeOut = this::hideToolbar;

    private void hideToolbar() {
        viewToolbar.setVisibility(GONE);
        llSwitchCamera.setVisibility(GONE);
        isToolbarShowing = false;
        llMoreDialog.setVisibility(GONE);
        llShareMore.setVisibility(GONE);
        feccBar.setVisibility(GONE);
    }

    private void showToolbar(int timeout) {
        if (!isToolbarShowing) { // show toolbar
            viewToolbar.setVisibility(View.VISIBLE);
            llSwitchCamera.setVisibility(View.VISIBLE);
            isToolbarShowing = true;
            // fecc
            feccBar.setVisibility(VISIBLE);
            updateFeccStatus();
        }
        if (timeout != 0) {
            handler.removeCallbacks(mFadeOut);
            handler.postDelayed(mFadeOut, timeout);
        }
    }

    // ????????????
    private void initCallDuration() {
        if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
            compositeDisposable.clear();
        }
        compositeDisposable.add(Observable.interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> tvCallDuration.setText(CommonTime.formatTime(aLong))));
    }

    private void checkPip() {
        SpeakerVideoFragment videoFragment = (SpeakerVideoFragment) videoPagerAdapter.getItem(0);
        if (videoFragment.isShowingPip()) {
            videoFragment.setShowingPip(false);
            tvClosePip.setText("????????????");
        } else {
            videoFragment.setShowingPip(true);
            tvClosePip.setText("????????????");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.drop_call:
            case R.id.bt_call_drop:
                NemoSDK.getInstance().hangup();
                NemoSDK.getInstance().releaseLayout();
                NemoSDK.getInstance().releaseCamera();
                finish();
                break;
            case R.id.bt_call_accept:
                L.i(TAG, "inviteCallIndex::: " + inviteCallIndex);
                NemoSDK.getInstance().answerCall(inviteCallIndex, true);
                break;
            case R.id.hold_meeting_more:
                if (layoutMode == LayoutMode.MODE_GALLERY) {
                    tvKeyboared.setVisibility(GONE);
                    tvClosePip.setVisibility(GONE);
                } else {
                    tvKeyboared.setVisibility(VISIBLE);
                    tvClosePip.setVisibility(VISIBLE);
                }
                SpeakerVideoFragment videoFragment = (SpeakerVideoFragment) videoPagerAdapter.getItem(0);
                // only landscape & meeting member > 0 & speaker mode -> closePip enable
                boolean isClosePipEnable = videoFragment.isLandscape() && mRemoteVideoInfos != null
                        && mRemoteVideoInfos.size() > 0 && currentPagerIndex == 0;
                tvClosePip.setEnabled(isClosePipEnable);
                tvClosePip.setTextColor(isClosePipEnable ? Color.WHITE : Color.GRAY);
                llMoreDialog.setVisibility(llMoreDialog.getVisibility() == View.VISIBLE ? GONE : View.VISIBLE);
                break;
            case R.id.mute_mic_btn:
                if (isMuteBtnEnable) {
                    updateMuteStatus(!NemoSDK.getInstance().isMicMuted());
                } else {
                    // ??????/????????????/????????????
                    switch (muteStatus) {
                        case MuteStatus.HAND_UP:
                            NemoSDK.getInstance().handUp();
                            muteStatus = MuteStatus.HAND_DOWN;
                            btMuteMic.setImageResource(R.mipmap.ic_toolbar_handdown);
                            tvMuteMic.setText("????????????");
                            break;
                        case MuteStatus.HAND_DOWN:
                            NemoSDK.getInstance().handDown();
                            muteStatus = MuteStatus.HAND_UP;
                            btMuteMic.setImageResource(R.mipmap.ic_toolbar_hand_up);
                            tvMuteMic.setText("????????????");
                            break;
                        case MuteStatus.END_SPEACH:
                            NemoSDK.getInstance().endSpeech();
                            muteStatus = MuteStatus.HAND_UP;
                            btMuteMic.setImageResource(R.mipmap.ic_toolbar_hand_up);
                            tvMuteMic.setText("????????????");
                            break;
                    }
                }
                break;
            case R.id.close_video:
                isVideoMute = !isVideoMute;
                NemoSDK.getInstance().setVideoMute(isVideoMute);
                setVideoState(isVideoMute);
                break;
            case R.id.tv_more_record:
                L.i(TAG, "is recording: " + isStartRecording);
                if (NemoSDK.getInstance().isAuthorize()) {
                    setRecordVideo(isStartRecording);
                    isStartRecording = !isStartRecording;
                } else {
                    Toast.makeText(XyCallActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.tv_nore_call_mode:
                audioMode = !audioMode;
                setSwitchCallState(audioMode);
                NemoSDK.getInstance().switchCallMode(audioMode);
                break;
            case R.id.btn_more_host_meeting:
                llMoreDialog.setVisibility(GONE);
                hideToolbar();
                if (llHostMeeting.getVisibility() == View.VISIBLE) {
                    hideHostMeeting();
                } else {
                    showHostMeeting();
                }
                break;
            case R.id.btn_more_share:
                handleShareEvent(shareState);
                break;
            case R.id.keyboard:
                llMoreDialog.setVisibility(GONE);
                dtmfLayout.setVisibility(VISIBLE);
                break;
            case R.id.textView2:
                llMoreDialog.setVisibility(GONE);
                hideToolbar();
                checkPip();
                break;
            case R.id.tv_whiteboard:
                llShareMore.setVisibility(GONE);
                NemoSDK.getInstance().startWhiteboard();
                L.i("wang ????????????");
                break;
            case R.id.tv_share_screen:
                llShareMore.setVisibility(GONE);
                if (screenPresenter == null) {
                    screenPresenter = new ScreenPresenter(XyCallActivity.this);
                }
                screenPresenter.startShare();
                break;
            case R.id.tv_share_photo:
                llShareMore.setVisibility(GONE);
                sharePhoto();
                break;
            case R.id.layout_lock_people:
                llMoreDialog.setVisibility(GONE);
                ((SpeakerVideoFragment) videoPagerAdapter.getItem(0)).unlockLayout();
                llLockPeople.setVisibility(GONE);
                break;
            case R.id.switch_camera:
                if (uvcCameraPresenter != null && uvcCameraPresenter.hasUvcCamera()) {
                    uvcCameraPresenter.switchCamera();
                } else {
                    NemoSDK.getInstance().switchCamera(defaultCameraFront ? 0 : 1);  // 0????????? 1?????????
                    defaultCameraFront = !defaultCameraFront;
                }
                break;
            case R.id.bt_invite_accept: // ?????????????????????
                L.i(TAG, "wang invite accept");
                NemoSDK.getInstance().answerCall(inviteCallIndex, true);
                viewInvite.setVisibility(GONE);
                break;
            case R.id.bt_invite_drop: // ?????????????????????
                L.i(TAG, "wang invite drop");
                NemoSDK.getInstance().answerCall(inviteCallIndex, false);
                viewInvite.setVisibility(GONE);
                break;
            case R.id.pager_picture:
                L.i(TAG, "wang pager clicked");
                hideOrShowToolbar(isToolbarShowing);
                break;
        }
    }

    private void sharePhoto() {
        new RxPermissions(this).request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(enable -> {
                    if (enable) {
                        Matisse.from(XyCallActivity.this)
                                .choose(MimeType.of(MimeType.PNG, MimeType.GIF, MimeType.JPEG), false)
                                .countable(true)
                                .maxSelectable(9)
                                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                                .thumbnailScale(0.85f)
                                .imageEngine(new Glide4Engine())
                                .forResult(REQUEST_CODE_CHOOSE);
                    }
                });
    }

    private void handleShareEvent(ShareState shareState) {
        switch (shareState) {
            case NONE:
                llShareMore.setVisibility(llShareMore.getVisibility() == View.VISIBLE ? GONE : View.VISIBLE);
            case IMAGE:
                // ??????????????????, Note: remove pictureHandler
                NemoSDK.getInstance().dualStreamStop(ContentType.CONTENT_TYPE_PICTURE);
                break;
            case SCREEN:
                if (screenPresenter != null && screenPresenter.isSharingScreen()) {
                    NemoSDK.getInstance().dualStreamStop(ContentType.CONTENT_TYPE_SCREEN_WITH_AUDIO);
                }
                break;
            case WHITEBOARD:
                new CustomAlertDialog(XyCallActivity.this).builder()
                        .setTitle(getString(R.string.exit_white_board_title))
                        .setMsg(getString(R.string.exit_white_board_content))
                        .setPositiveButton(getString(R.string.sure), v1 -> {
                            NemoSDK.getInstance().stopWhiteboard();
                        })
                        .setNegativeButton(getString(R.string.cancel), v12 -> {
                        }).setCancelable(false).show();
                break;
            default:
        }
    }

    //????????????????????????
    private void setVideoState(boolean videoMute) {
        videoPagerAdapter.setLocalVideoMute(videoMute);
        if (videoMute) {
            btCloseVideo.setImageResource(R.drawable.close_video);
            tvCloseVideo.setText(getResources().getString(R.string.open_video));
        } else {
            btCloseVideo.setImageResource(R.drawable.video);
            tvCloseVideo.setText(getResources().getString(R.string.close_video));
        }
    }

    public void setRecordVideo(boolean isStartRecording) {
        if (isStartRecording) {
            NemoSDK.getInstance().startRecord(outgoingNumber, new RecordCallback() {
                @Override
                public void onFailed(final int errorCode) {
                    Observable.just(0).observeOn(AndroidSchedulers.mainThread()).subscribe(integer -> {
                        Toast.makeText(XyCallActivity.this, "Record fail: " + errorCode, Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onSuccess() {
                    Observable.just(0).observeOn(AndroidSchedulers.mainThread()).subscribe(
                            integer -> showRecordStatusNotification(true, NemoSDK.getInstance().getUserName(), true)
                    );
                }
            });
        } else {
            NemoSDK.getInstance().stopRecord();
            showRecordStatusNotification(false, NemoSDK.getInstance().getUserName(), true);
            Toast.makeText(XyCallActivity.this, getString(R.string.third_conf_record_notice), Toast.LENGTH_LONG).show();
        }
    }

    // ??????
    @Override
    public void showCallOutGoing(String outgoingNumber) {
        viewCallDetail.setVisibility(VISIBLE);
        if (getIntent().getBooleanExtra("muteVideo", false)) {
            viewCallDetail.setBackgroundResource(R.drawable.cell_bg_default);
        } else {
            viewCallDetail.setBackgroundResource(R.drawable.bg_outgoing_shadow);
        }
        tvCallTips.setText("?????????????????????...");
        btCallAccept.setVisibility(GONE);
        L.i(TAG, "showCallOutGoing callNumber: " + outgoingNumber);
        tvCallNumber.setText(outgoingNumber);
        toolbarCallNumber.setText(outgoingNumber);
    }

    // ??????
    @Override
    public void showCallIncoming(int callIndex, String callNumber, String callName) {
        viewCallDetail.setVisibility(VISIBLE);
        viewCallDetail.setBackgroundColor(Color.BLACK);
        tvCallTips.setText("?????????????????????...");
        tvCallNumber.setText(!TextUtils.isEmpty(callName) ? callName : callNumber);
        btCallAccept.setVisibility(VISIBLE);
    }

    @Override
    public void showCallDisconnected(String reason) {
        Toast.makeText(this, "Call disconnected reason: " + reason, Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * ????????????, ?????? ??????toolbar???
     */
    @Override
    public void showCallConnected() {
        isCallStart = true;
        viewCallDetail.setVisibility(GONE);
        initCallDuration();
        showToolbar(sDefaultTimeout);
        videoPagerAdapter.setLocalVideoInfo(buildLocalLayoutInfo());
        if (getIntent().getBooleanExtra("muteVideo", false)) {
            isVideoMute = true;
            NemoSDK.getInstance().setVideoMute(isVideoMute);
            setVideoState(isVideoMute);
        }
        if (getIntent().getBooleanExtra("muteAudio", false) && isMuteBtnEnable) {
            updateMuteStatus(true);
        }
        // ????????????
        if (confMgmtStateObserver != null) {
            confMgmtStateObserver.subscribe(
                    aBoolean -> videoPagerAdapter.setLocalMicMute(aBoolean),
                    throwable -> {
                    });
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mVolumeManager.onVolumeDown();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            mVolumeManager.onVolumeUp();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * ??????????????????
     * ?????????????????????????????????
     * ?????????????????????????????????????????????????????????
     *
     * @param operation        ?????????mute/unmute
     * @param isMuteIsDisabled ????????????????????? true????????????
     */
    @Override
    public void showConfMgmtStateChanged(String operation, boolean isMuteIsDisabled, String chairmanUri) {
        isMuteBtnEnable = !isMuteIsDisabled;
        findViewById(R.id.ll_chairman_mode).setVisibility(TextUtils.isEmpty(chairmanUri) ? GONE : VISIBLE);
        if ("mute".equalsIgnoreCase(operation)) {
            NemoSDK.getInstance().enableMic(true, isMuteIsDisabled);
            if (isMuteIsDisabled) {
                // ????????????
                Toast.makeText(XyCallActivity.this, "?????????????????????, ???????????????", Toast.LENGTH_LONG).show();
                muteStatus = MuteStatus.HAND_UP;
                btMuteMic.setImageResource(R.mipmap.ic_toolbar_hand_up);
                tvMuteMic.setText("????????????");
            } else {
                Toast.makeText(XyCallActivity.this, "???????????????", Toast.LENGTH_LONG).show();
                btMuteMic.setImageResource(R.mipmap.ic_toolbar_mic_muted);
                tvMuteMic.setText("????????????");
            }
            if (isCallStart) {
                videoPagerAdapter.setLocalMicMute(true);
            }
            confMgmtStateObserver = Observable.just(true);
        } else if ("unmute".equalsIgnoreCase(operation)) {
            NemoSDK.getInstance().enableMic(false, false);
            if (isMuteIsDisabled) {
                muteStatus = MuteStatus.END_SPEACH;
                btMuteMic.setImageResource(R.mipmap.ic_toolbar_end_speech);
                tvMuteMic.setText("????????????");
            } else {
                btMuteMic.setImageResource(R.mipmap.ic_toolbar_mic);
                tvMuteMic.setText("??????");
            }
            if (isCallStart) {
                videoPagerAdapter.setLocalMicMute(false);
            }
            confMgmtStateObserver = Observable.just(false);
        }
        compositeDisposable.add(compositeDisposable);
    }

    @Override
    public void showKickout(int code, String reason) {
        Observable.just(0).observeOn(AndroidSchedulers.mainThread()).subscribe(integer -> {
                    Toast.makeText(this, "kick out reason: " + reason, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(XyCallActivity.this, JoinMeetingActivity.class));
                    finish();
                }
        );
    }

    private void updateMuteStatus(boolean isMute) {
        NemoSDK.getInstance().enableMic(isMute, true);
        videoPagerAdapter.setLocalMicMute(isMute);
        if (isMute) {
            btMuteMic.setImageResource(R.mipmap.ic_toolbar_mic_muted);
            tvMuteMic.setText("????????????");
        } else {
            btMuteMic.setImageResource(R.mipmap.ic_toolbar_mic);
            tvMuteMic.setText("??????");
        }
    }

    /**
     * ????????????????????????
     *
     * @param level 1???2???3???4?????????,???-???-???-???
     */
    @Override
    public void showNetLevel(int level) {
        if (ivNetworkState == null) {
            return;
        }
        switch (level) {
            case 4:
                ivNetworkState.setImageResource(R.drawable.network_state_four);
                break;
            case 3:
                ivNetworkState.setImageResource(R.drawable.network_state_three);
                break;
            case 2:
                ivNetworkState.setImageResource(R.drawable.network_state_two);
                break;
            case 1:
                ivNetworkState.setImageResource(R.drawable.network_state_one);
                break;
        }
    }

    @Override
    public void showVideoStatusChange(int videoStatus) {
        if (videoStatus == VideoStatus.VIDEO_STATUS_NORMAL) {
            Toast.makeText(XyCallActivity.this, "????????????", Toast.LENGTH_SHORT).show();
        } else if (videoStatus == VideoStatus.VIDEO_STATUS_LOW_AS_LOCAL_BW) {
            Toast.makeText(XyCallActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
        } else if (videoStatus == VideoStatus.VIDEO_STATUS_LOW_AS_LOCAL_HARDWARE) {
            Toast.makeText(XyCallActivity.this, "??????????????????????????????", Toast.LENGTH_SHORT).show();
        } else if (videoStatus == VideoStatus.VIDEO_STATUS_LOW_AS_REMOTE) {
            Toast.makeText(XyCallActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
        } else if (videoStatus == VideoStatus.VIDEO_STATUS_NETWORK_ERROR) {
            Toast.makeText(XyCallActivity.this, "???????????????????????????", Toast.LENGTH_SHORT).show();
        } else if (videoStatus == VideoStatus.VIDEO_STATUS_LOCAL_WIFI_ISSUE) {
            Toast.makeText(XyCallActivity.this, "WiFi???????????????", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showIMNotification(String values) {
        if ("[]".equals(values)) {
            Toast.makeText(XyCallActivity.this, R.string.im_notification_ccs_transfer, Toast.LENGTH_SHORT).show();
        } else {
            String val = values.replace("[", "");
            val = val.replace("]", "");
            val = val.replace('"', ' ');
            val = val.replace('"', ' ');
            String str = String.format("%s%s%s", getResources().getString(R.string.queen_top_part), val, getResources().getString(R.string.queen_bottom_part));
            Toast.makeText(XyCallActivity.this, str, Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("CheckResult")
    @Override
    public void showAiFace(AIParam aiParam, boolean isLocalFace) {
//        L.i(TAG, "aiParam:" + aiParam);
//        if (aiParam == null || aiParam.getParticipantId() < 0) {
//            return;
//        }
//        Observable.just(0).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Integer>() {
//            @Override
//            public void accept(Integer integer) throws Exception {
//                L.i(TAG, "fullVideoInfo:: " + fullVideoInfo.toString());
//                L.i(TAG, "fullVideoInfo is Local:: " + isLocalFace);
//                if (isLocalFace) {
//                    callPresenter.dealLocalAiParam(aiParam, fullVideoInfo != null
//                            && fullVideoInfo.getParticipantId() == NemoSDK.getInstance().getUserId());
//                } else {
//                    callPresenter.dealAiParam(aiParam, fullVideoInfo != null
//                            && fullVideoInfo.getParticipantId() == aiParam.getParticipantId());
//                }
//            }
//        });
    }

    /**
     * ???????????????laid
     *
     * @param callNumber
     * @param callName
     */
    @Override
    public void showInviteCall(int callIndex, String callNumber, String callName) {
        inviteCallIndex = callIndex;
        viewInvite.setVisibility(VISIBLE);
        toolbarCallNumber.setText(callNumber);
        tvInviteNumber.setText(TextUtils.isEmpty(callName) ? callNumber : callName);
    }

    @Override
    public void showCaptionNotification(String content, String action) {
        TextView tvCaptionNotification = findViewById(R.id.tv_caption_notification);
        if ("push".equals(action)) {
            tvCaptionNotification.setVisibility(View.VISIBLE);
            tvCaptionNotification.setText(content);
        } else if ("cancel".equals(action)) {
            tvCaptionNotification.setVisibility(GONE);
        }
    }

    @Override
    public void hideInviteCall() {
        viewInvite.setVisibility(GONE);
    }

    @Override
    public void showRecordStatusNotification(boolean isStart, String displayName, boolean canStop) {
        Log.i(TAG, "showRecordStatusNotification: " + isStart);
        if (isStart) {
            Animation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
            alphaAnimation.setDuration(500);
            alphaAnimation.setFillBefore(true);
            alphaAnimation.setInterpolator(new LinearInterpolator());
            alphaAnimation.setRepeatCount(Animation.INFINITE);
            alphaAnimation.setRepeatMode(Animation.REVERSE);
            llRecording.setVisibility(View.VISIBLE);
            ivRecordStatus.startAnimation(alphaAnimation);
            tvMoreRecord.setEnabled(canStop);
            tvRecordingDuration.setText(displayName + "????????????");
            tvMoreRecord.setText(R.string.button_text_stop);
        } else {
            ivRecordStatus.clearAnimation();
            tvMoreRecord.setEnabled(true);
            llRecording.setVisibility(GONE);
            tvMoreRecord.setText(R.string.start_record_video);
        }
    }

    //????????????
    private void setSwitchCallState(boolean audioMode) {
        List<VideoFragment> fragments = videoPagerAdapter.getFragments();
        for (VideoFragment fragment : fragments) {
            fragment.setAudioOnlyMode(audioMode, isVideoMute);
        }
        if (audioMode) {
            btCloseVideo.setEnabled(false);
            tvMoreCallMode.setText(R.string.close_switch_call_module);
        } else {
            btCloseVideo.setEnabled(true);
            tvMoreCallMode.setText(R.string.switch_call_module);
        }
    }

    private VideoInfo buildLocalLayoutInfo() {
        VideoInfo li = new VideoInfo();
        li.setLayoutVideoState(Enums.LAYOUT_STATE_RECEIVED);
        li.setDataSourceID(NemoSDK.getLocalVideoStreamID());
        li.setVideoMuteReason(Enums.MUTE_BY_USER);
        li.setRemoteName(NemoSDK.getInstance().getUserName());
        li.setParticipantId((int) NemoSDK.getInstance().getUserId());
        li.setRemoteID(RemoteUri.generateUri(String.valueOf(NemoSDK.getInstance().getUserId()), Enums.DEVICE_TYPE_SOFT));
        return li;
    }

    //=========================================================================================
    // face view
    //=========================================================================================
    @Override
    public void showFaceView(List<FaceView> faceViews) {
//        mVideoView.showFaceView(faceViews);
    }

    @Override
    public Activity getCallActivity() {
        return this;
    }

    @Override
    public int[] getMainCellSize() {
//        return new int[]{mVideoView.getWidth(), mVideoView.getHeight()};
        return new int[]{0, 0};
    }

    //=========================================================================================
    // share picture demo: ????????????
    // NOTE: bitmap only support ARGB_8888
    //=========================================================================================
    private byte[] pictureData;
    private int width;
    private int height;
    private static final int MSG_SHARE_PICTURE = 6002;

    @SuppressLint("HandlerLeak")
    private Handler pictureHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_SHARE_PICTURE) {
                String dataSourceId = NemoSDK.getInstance().getDataSourceId();
                if (!TextUtils.isEmpty(dataSourceId) && pictureData != null) {
                    L.i(TAG, "send data to remote: " + pictureData.length + " W. " + width + " h." + height);
                    NativeDataSourceManager.putContentData2(dataSourceId,
                            pictureData, pictureData.length, width, height, 0, 0, 0, true);
                }
                pictureHandler.sendEmptyMessageDelayed(MSG_SHARE_PICTURE, 200);
                // 9711360   wang x. 1080 y. 2029
            }
        }
    };

    private class MyPagerListener extends ViewPager.SimpleOnPageChangeListener {
        boolean first = true;

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            // start share
            L.i(TAG, "wang onPageSelected: " + position);
            if (picturePaths != null && picturePaths.size() > 0) {
                pictureHandler.removeMessages(MSG_SHARE_PICTURE);
                String picturePath = picturePaths.get(position);
                Glide.with(XyCallActivity.this).asBitmap().apply(new RequestOptions().override(1280, 720))
                        .load(picturePath).into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Matrix matrix = new Matrix();
                        matrix.setScale(0.5f, 0.5f);
                        Bitmap bitmap = Bitmap.createBitmap(resource, 0, 0, resource.getWidth(), resource.getHeight(), matrix, true);
                        if (bitmap != null) {
                            width = bitmap.getWidth();
                            height = bitmap.getHeight();
                            int byteCount = bitmap.getByteCount();
                            ByteBuffer b = ByteBuffer.allocate(byteCount);
                            bitmap.copyPixelsToBuffer(b);
                            pictureData = b.array();
                            pictureHandler.sendEmptyMessage(MSG_SHARE_PICTURE);
                            bitmap.recycle();
                        }
                    }
                });
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            L.i(TAG, "onPageScrolled:: " + first);
            if (first && positionOffset == 0 && positionOffsetPixels == 0) {
                onPageSelected(0);
                first = false;
            }
            hideToolbar();
        }
    }

    /**
     * ???????????????????????????????????????????????????
     *
     * @param state
     */
    @Override
    public void updateSharePictures(NemoSDKListener.NemoDualState state) {
        if (state == NemoSDKListener.NemoDualState.NEMO_DUAL_STAT_IDLE) {
            pictureHandler.removeMessages(MSG_SHARE_PICTURE);
            pictureData = null;
            pagerPicture.setVisibility(GONE);
            pageIndicator.setVisibility(GONE);
            isSharePicture = false;
            resetShareStates(false, ShareState.IMAGE);
        } else if (state == NemoSDKListener.NemoDualState.NEMO_DUAL_STATE_RECEIVING) {
            resetShareStates(true, ShareState.IMAGE);
            picturePagerAdapter = new PicturePagerAdapter(getSupportFragmentManager());
            picturePagerAdapter.setOnPagerListener(() -> hideOrShowToolbar(isToolbarShowing));
            pagerPicture.setAdapter(picturePagerAdapter);
            pageIndicator.setViewPager(pagerPicture);
            pageIndicator.setOnPageChangeListener(new MyPagerListener());
            picturePagerAdapter.setPicturePaths(picturePaths);
            picturePagerAdapter.notifyDataSetChanged();
            pageIndicator.setVisibility(VISIBLE);
            pagerPicture.setVisibility(VISIBLE);
        } else if (state == NemoSDKListener.NemoDualState.NEMO_DUAL_STATE_NOBANDWIDTH) {
            Toast.makeText(this, "????????????, ???????????????, ????????????", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "??????, ????????????", Toast.LENGTH_SHORT).show();
        }
    }

    //=========================================================================================
    // share screen demo: ????????????????????????????????????????????????
    //=========================================================================================
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (SharingValues.REQUEST_SHARE_SCREEN == requestCode) {
            if (resultCode == RESULT_OK) {
                if (screenPresenter != null) {
                    screenPresenter.onResult(requestCode, resultCode, intent);
                }
            } else {
                // user did not grant permissions
                Toast.makeText(XyCallActivity.this, "share screen cancel", Toast.LENGTH_LONG).show();
            }
        } else if (SharingValues.REQUEST_FLOAT_PERMISSION == requestCode) {
            // home screen float view
            if (Settings.canDrawOverlays(XyCallActivity.this)) {
                if (screenPresenter != null) {
                    screenPresenter.gotPermissionStartShare();
                }
            } else {
                Toast.makeText(XyCallActivity.this, "???????????????????????????", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            picturePaths = Matisse.obtainPathResult(intent);
            L.i(TAG, "wang::: paths: " + picturePaths.size() + " ;; " + picturePaths);
            if (picturePaths.size() > 0) {
                // start share picture
                NemoSDK.getInstance().dualStreamStart(ContentType.CONTENT_TYPE_PICTURE);
            }
        }
    }

    @Override
    public void updateShareScreen(NemoSDKListener.NemoDualState state) {
        if (state == NemoSDKListener.NemoDualState.NEMO_DUAL_STAT_IDLE) {
            if (screenPresenter != null && screenPresenter.isSharingScreen()) {
                L.i(TAG, "updateShareScreen stop");
                screenPresenter.stopShare();
            }
            shareScreenView.setVisibility(GONE);
            resetShareStates(false, ShareState.SCREEN);
        } else if (state == NemoSDKListener.NemoDualState.NEMO_DUAL_STATE_RECEIVING) {
            resetShareStates(false, ShareState.SCREEN);
            // show floating view
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityUtils.goHome(this);
                if (screenPresenter != null) {
                    screenPresenter.showFloatView(); // ???????????????
                }
                shareScreenView.setVisibility(VISIBLE);
            }
        } else {
            Toast.makeText(this, "????????????, ?????????", Toast.LENGTH_SHORT).show();
        }
    }

    //=========================================================================================
    // whiteboard demo
    //=========================================================================================
    private WhiteboardChangeListener whiteboardChangeListener = new WhiteboardChangeListener() {

        @SuppressLint("CheckResult")
        @Override
        public void onWhiteboardStart() {
            Observable.just(0).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Integer>() {
                @Override
                public void accept(Integer integer) throws Exception {
                    L.i(TAG, "onWhiteboardStart");
                    // fix: ?????????????????????, ????????????????????????, ?????????????????????
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!ActivityUtils.isAppForeground(XyCallActivity.this)
                                && !(screenPresenter != null && screenPresenter.isSharingScreen())) {
                            ActivityUtils.moveTaskToFront(XyCallActivity.this);
                        }
                    }
                    videoPager.setScanScroll(false);
                    myVideoPagerListener.onPageSelected(0);
                    videoPager.setCurrentItem(0, false);
                    videoPagerAdapter.getItem(currentPagerIndex).setLandscape(true);
                    NemoSDK.getInstance().setOrientation(Orientation.LANDSCAPE);
                    videoPagerAdapter.onWhiteboardStart();
                    resetShareStates(true, ShareState.WHITEBOARD);
                }
            }, throwable -> {
            });
        }

        @SuppressLint("CheckResult")
        @Override
        public void onWhiteboardStop() {
            Observable.just(0).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Integer>() {
                @Override
                public void accept(Integer integer) throws Exception {
                    videoPager.setScanScroll(true);
                    videoPagerAdapter.onWhiteboardStop();
                    resetShareStates(false, ShareState.WHITEBOARD);
                }
            }, throwable -> {
            });
        }

        /**
         * ??????????????????
         *
         * @param message ????????????
         */
        @SuppressLint("CheckResult")
        @Override
        public void onWhiteboardMessage(String message) {

            Observable.just(0).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Integer>() {
                @Override
                public void accept(Integer integer) throws Exception {
                    videoPagerAdapter.onWhiteboardMessage(message);
                }
            }, throwable -> {
            });
        }

        @SuppressLint("CheckResult")
        @Override
        public void onWhiteboardMessages(ArrayList<String> messages) {
            Observable.just(0).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Integer>() {
                @Override
                public void accept(Integer integer) throws Exception {
                    if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            || getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        NemoSDK.getInstance().setOrientation(Orientation.LANDSCAPE);
                    }
                    videoPagerAdapter.onWhiteboardMessages(messages);
                }
            }, throwable -> {
            });
        }
    };

    //=========================================================================================
    // ?????????????????????demo
    //=========================================================================================

    /**
     * ????????????????????????
     *
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        L.i("VideoFragment onConfigChanged:: " + newConfig.orientation);
        int orientation = getResources().getConfiguration().orientation;
        L.i("VideoFragment orientation:: " + orientation);
        layoutMeetingView();
    }

    private static final int MSG_ORIENTATION_CHANGED = 60001;
    private Handler orientationHanler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_ORIENTATION_CHANGED) {
                handleOrientationChanged(msg.arg1);
            }
        }
    };

    private void handleOrientationChanged(int rotation) {
        rotation=175;
        int screenOrientation = getResources().getConfiguration().orientation;
        if (((rotation >= 0) && (rotation < 45)) || (rotation > 315)) {
            // ?????? 0?????????????????????????????????home??????????????????
            // NOTE: ?????????????????????????????? ?????????????????????,  ???????????????????????????(???, ???, ???)
            if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT && screenOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
                if (!SpeakerVideoGroup.isShowingWhiteboard()) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    videoPagerAdapter.setLandscape(false);
                    NemoSDK.getInstance().setOrientation(Orientation.PORTRAIT);
                }
            }
        } else if (rotation > 225 && rotation < 315) {
            // ?????? 270???????????????????????????270???????????????home???????????????
            if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                videoPagerAdapter.setLandscape(true);
                NemoSDK.getInstance().setOrientation(Orientation.LANDSCAPE);
            }
        } else if (rotation > 45 && rotation < 135) {
            // ???????????? 90???????????????????????????90????????????home???????????????
            if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                videoPagerAdapter.setLandscape(true);
                NemoSDK.getInstance().setOrientation(Orientation.REVERSE_LANDSCAPE);
            }
        } else if (rotation > 135 && rotation < 225) {
            if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                videoPagerAdapter.setLandscape(true);
                NemoSDK.getInstance().setOrientation(Orientation.LANDSCAPE);
            }

        }
    }

    private class YourOrientationEventListener extends OrientationEventListener {

        public YourOrientationEventListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (enableScreenOrientation) {
                orientationHanler.removeMessages(MSG_ORIENTATION_CHANGED);
                Message msg = handler.obtainMessage(MSG_ORIENTATION_CHANGED, orientation, 0);
                orientationHanler.sendMessageDelayed(msg, 100);
            }
        }
    }

    //=========================================================================================
    // fecc
    //=========================================================================================
    private void updateFeccStatus() {
        if (fullVideoInfo != null) {
            int feccOri = fullVideoInfo.getFeccOri();
            boolean isAudioOnly = Enums.LAYOUT_STATE_RECEIVED_AUDIO_ONLY.equals(fullVideoInfo.getLayoutVideoState());
            // allowControlCamera & feccDisable ???????????????FECC???????????? --> ????????????
            boolean isFeccSupport = feccBar.isSupportHorizontalFECC(feccOri) || feccBar.isSupportVerticalFECC(feccOri);
            L.i(TAG, "isFeccSupport: " + isFeccSupport);
            feccBar.setFECCButtonVisible(!fullVideoInfo.isVideoMute() && !isAudioOnly && isFeccSupport && !isSharePicture);
            feccBar.setZoomInOutVisible(feccBar.isSupportZoomInOut(feccOri));
            feccBar.setFeccTiltControl(feccBar.isSupportHorizontalFECC(feccOri), feccBar.isSupportVerticalFECC(feccOri));
        } else {
            feccBar.setFECCButtonVisible(false);
        }
    }

    private class FeccActionListener implements UserActionListener {

        @Override
        public void onUserAction(int action, Bundle args) {
            switch (action) {
                case UserActionListener.USER_ACTION_FECC_LEFT:
                    NemoSDK.getInstance().farEndHardwareControl(fullVideoInfo.getParticipantId(), FECCCommand.FECC_TURN_LEFT, 10);
                    break;
                case UserActionListener.USER_ACTION_FECC_RIGHT:
                    NemoSDK.getInstance().farEndHardwareControl(fullVideoInfo.getParticipantId(), FECCCommand.FECC_TURN_RIGHT, 10);
                    break;
                case UserActionListener.USER_ACTION_FECC_STOP:
                    NemoSDK.getInstance().farEndHardwareControl(fullVideoInfo.getParticipantId(), FECCCommand.FECC_TURN_STOP, 10);
                    break;
                case UserActionListener.USER_ACTION_FECC_STEP_LEFT:
                    NemoSDK.getInstance().farEndHardwareControl(fullVideoInfo.getParticipantId(), FECCCommand.FECC_STEP_LEFT, 10);
                    break;
                case UserActionListener.USER_ACTION_FECC_STEP_RIGHT:
                    NemoSDK.getInstance().farEndHardwareControl(fullVideoInfo.getParticipantId(), FECCCommand.FECC_STEP_RIGHT, 10);
                    break;
                case UserActionListener.USER_ACTION_FECC_UP:
                    NemoSDK.getInstance().farEndHardwareControl(fullVideoInfo.getParticipantId(), FECCCommand.TILT_CAMERA_TURN_UP, 10);
                    break;
                case UserActionListener.USER_ACTION_FECC_DOWN:
                    NemoSDK.getInstance().farEndHardwareControl(fullVideoInfo.getParticipantId(), FECCCommand.TILT_CAMERA_TURN_DOWN, 10);
                    break;
                case UserActionListener.USER_ACTION_FECC_STEP_UP:
                    NemoSDK.getInstance().farEndHardwareControl(fullVideoInfo.getParticipantId(), FECCCommand.TILT_CAMERA_STEP_UP, 10);
                    break;
                case UserActionListener.USER_ACTION_FECC_STEP_DOWN:
                    NemoSDK.getInstance().farEndHardwareControl(fullVideoInfo.getParticipantId(), FECCCommand.TILT_CAMERA_STEP_DOWN, 10);
                    break;
                case UserActionListener.USER_ACTION_FECC_UP_DOWN_STOP:
                    NemoSDK.getInstance().farEndHardwareControl(fullVideoInfo.getParticipantId(), FECCCommand.TILT_CAMERA_TURN_STOP, 10);
                    break;
                case UserActionListener.FECC_ZOOM_IN:
                    NemoSDK.getInstance().farEndHardwareControl(fullVideoInfo.getParticipantId(), FECCCommand.FECC_ZOOM_IN, 10);
                    break;
                case UserActionListener.FECC_STEP_ZOOM_IN:
                    NemoSDK.getInstance().farEndHardwareControl(fullVideoInfo.getParticipantId(), FECCCommand.FECC_STEP_ZOOM_IN, 10);
                    break;
                case UserActionListener.FECC_ZOOM_OUT:
                    NemoSDK.getInstance().farEndHardwareControl(fullVideoInfo.getParticipantId(), FECCCommand.FECC_ZOOM_OUT, 10);
                    break;
                case UserActionListener.FECC_STEP_ZOOM_OUT:
                    NemoSDK.getInstance().farEndHardwareControl(fullVideoInfo.getParticipantId(), FECCCommand.FECC_STEP_ZOOM_OUT, 10);
                    break;
                case UserActionListener.FECC_ZOOM_TURN_STOP:
                    NemoSDK.getInstance().farEndHardwareControl(fullVideoInfo.getParticipantId(), FECCCommand.FECC_ZOOM_TURN_STOP, 10);
                    break;
            }
        }
    }

    //=========================================================================================
    // ??????
    //=========================================================================================
    private void showHostMeeting() {
        llHostMeeting.setVisibility(VISIBLE);
        layoutMeetingView();
        String meetingHost = NemoSDK.getInstance().getMeetingHost();
        webView.loadUrl(meetingHost);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                webView.loadUrl(url);
                return true;
            }
        });
    }

    private void hideHostMeeting() {
        if (llHostMeeting != null && llHostMeeting.getVisibility() == View.VISIBLE) {
            llHostMeeting.setVisibility(View.GONE);
            webView.loadUrl("");
        }
    }

    private void layoutMeetingView() {
        if (llHostMeeting != null && llHostMeeting.getVisibility() == View.VISIBLE) {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(root);
            int hostMeetingId = llHostMeeting.getId();
            constraintSet.clear(hostMeetingId);
            constraintSet.connect(hostMeetingId, ConstraintSet.END, root.getId(), ConstraintSet.END);
            constraintSet.connect(hostMeetingId, ConstraintSet.TOP, root.getId(), ConstraintSet.TOP);
            constraintSet.connect(hostMeetingId, ConstraintSet.BOTTOM, root.getId(), ConstraintSet.BOTTOM);
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                constraintSet.setDimensionRatio(hostMeetingId, "h,1:1");
                hostMeetingToolbar.setVisibility(GONE);
            } else {
                constraintSet.connect(hostMeetingId, ConstraintSet.START, root.getId(), ConstraintSet.START);
                hostMeetingToolbar.setVisibility(VISIBLE);
            }
            constraintSet.applyTo(root);
        }
    }

    private void resetShareStates(boolean isSharing, ShareState shareState) {
        if (isSharing) {
            this.shareState = shareState;
            ((TextView) findViewById(R.id.tv_share)).setText("????????????");
            btnMoreShare.setImageResource(R.drawable.finish_share);
        } else {
            this.shareState = ShareState.NONE;
            btnMoreShare.setImageResource(R.drawable.share);
            ((TextView) findViewById(R.id.tv_share)).setText("??????");
        }
    }

    private Runnable refreshStatisticsInfoRunnable = this::startRefreshStatisticsInfo;

    private void stopRefreshStatisticsInfo() {
        handler.removeCallbacks(refreshStatisticsInfoRunnable);
    }

    private void startRefreshStatisticsInfo() {
        NewStatisticsInfo newInfo = NemoSDK.getInstance().getStatisticsInfo();
        if (null == newInfo) {
            return;
        }
        mStatisticsRender.show();
        mStatisticsRender.onValue(newInfo);

        handler.removeCallbacks(refreshStatisticsInfoRunnable);
        handler.postDelayed(refreshStatisticsInfoRunnable, REFRESH_STATISTICS_INFO_DELAYED);
    }

    @Override
    public void onRosterChanged(int totalNumber, RosterWrapper rosters) {
        ((TextView) findViewById(R.id.tv_meeting_members)).setText(String.valueOf(totalNumber));
        videoPagerAdapter.setTotalMeetingMember(totalNumber);
        videoPagerIndicator.notifyDataSetChanged();
    }

    @Override
    public void showVideoDataSourceChange(List<VideoInfo> videoInfos, boolean hasVideoContent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // case-fix: ????????????????????????, ??????content???APP????????????
            if (hasVideoContent && !ActivityUtils.isAppForeground(this)
                    && !(screenPresenter != null && screenPresenter.isSharingScreen())) {
                ActivityUtils.moveTaskToFront(this);
            }
        }
        L.i(TAG, "showVideoDataSourceChange currentPagerIndex: " + currentPagerIndex);
        L.i(TAG, "showVideoDataSourceChange videoInfos size: " + videoInfos.size());
        mRemoteVideoInfos = videoInfos;
        if ((currentPagerIndex == 0 || currentPagerIndex == 1) && !hasVideoContent) {
            firstPagerVideoInfo = videoInfos;
        }
        VideoFragment fragment = (VideoFragment) videoPagerAdapter.getItem(currentPagerIndex);
        fragment.setRemoteVideoInfo(videoInfos, hasVideoContent);
    }

    public class MyVideoPagerListener extends ViewPager.SimpleOnPageChangeListener {
        boolean first = true;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            L.i(TAG, "onPageScrolled:: " + first);
            if (first && positionOffset == 0 && positionOffsetPixels == 0) {
                onPageSelected(0);
                first = false;
            }
        }

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            if (videoPagerAdapter.getCurrentIndex() != position) {
                videoPagerAdapter.setCurrentIndex(position);
            }
            currentPagerIndex = position;
            // 0 / 1 ??????List<VideoInfo>
            VideoFragment videoFragment = videoPagerAdapter.getItem(position);
            L.e(TAG, "onPageSelected position: " + position + ", fragment: " + videoFragment);
            if (position == 0) {
                videoFragment.setRemoteVideoInfo(firstPagerVideoInfo, false);
                NemoSDK.getInstance().setLayoutBuilder(new SpeakerLayoutBuilder());
//                NemoSDK.getInstance().setLayoutBuilder(new LayoutPolicy.LayoutBuilder() {
//                    @Override
//                    public List<LayoutElement> compute(LayoutPolicy layoutPolicy) {
//                        List<LayoutElement> layoutElements = new ArrayList<>();
//
//                        for(int i=0;i<layoutElements.size();i++)
//                        {
//                            if(i==0)
//                            {
//
//                            }else
//                                {
//
//                                }
//                        }
//
//                        return layoutElements;
//                    }
//                });
            } else if (position == 1) {
                videoFragment.setRemoteVideoInfo(firstPagerVideoInfo, false);
                NemoSDK.getInstance().setLayoutBuilder(new GalleryLayoutBuilder(1));
            } else {
                // other pager
                NemoSDK.getInstance().setLayoutBuilder(new GalleryLayoutBuilder(position));
            }
            videoFragment.startRender();

            // only landscape & meeting member > 0 & speaker mode -> closePip enable
            boolean isClosePipEnable = false;
            if (position == 0) {
                isClosePipEnable = ((SpeakerVideoFragment) videoFragment).isLandscape() && mRemoteVideoInfos != null
                        && mRemoteVideoInfos.size() > 0 && currentPagerIndex == 0;
            }
            tvClosePip.setEnabled(isClosePipEnable);
            tvClosePip.setTextColor(isClosePipEnable ? Color.WHITE : Color.GRAY);
        }
    }

    @Override
    public boolean onVideoCellSingleTapConfirmed(VideoCell cell) {
        Log.e("onVideoCellSingleTap", "cell.id: "+cell.getLayoutInfo().getParticipantId());
        hideOrShowToolbar(isToolbarShowing);
        if (dtmfLayout.getVisibility() == VISIBLE) {
            dtmfLayout.setVisibility(GONE);
            dtmf.clearText();
        }
        hideHostMeeting();
        return false;
    }

    @Override
    public boolean onVideoCellDoubleTap(VideoCell cell) {
        Log.e("onVideoCellDoubleTap", "cell.id: "+cell.getLayoutInfo().getParticipantId());
        return false;
    }

    @Override
    public void onLockLayoutChanged(int pid) {
        if (currentPagerIndex == 0) {
            llLockPeople.setVisibility(VISIBLE);
        } else {
            llLockPeople.setVisibility(GONE);
        }
    }

    @Override
    public void onFullScreenChanged(VideoCell cell) {
        if (cell != null) {
            fullVideoInfo = cell.getLayoutInfo();
        }
    }

    @Override
    public void onVideoCellGroupClicked(View group) {
        hideOrShowToolbar(isToolbarShowing);
        if (dtmfLayout.getVisibility() == VISIBLE) {
            dtmfLayout.setVisibility(GONE);
            dtmf.clearText();
        }
        hideHostMeeting();
    }

    @Override
    public void onWhiteboardClicked() {
        hideOrShowToolbar(isToolbarShowing);
        if (dtmfLayout.getVisibility() == VISIBLE) {
            dtmfLayout.setVisibility(GONE);
            dtmf.clearText();
        }
        hideHostMeeting();
    }

    private ServiceConnection xyCallConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };
}
