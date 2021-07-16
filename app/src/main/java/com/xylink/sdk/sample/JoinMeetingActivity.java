package com.xylink.sdk.sample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.log.L;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.SyncStateContract;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ainemo.sdk.module.rest.model.MeetingRoomResponse;
import com.ainemo.sdk.otf.ConnectNemoCallback;
import com.ainemo.sdk.otf.LoginResponseData;
import com.ainemo.sdk.otf.MakeCallResponse;
import com.ainemo.sdk.otf.NemoSDK;
import com.ainemo.sdk.otf.NemoSDKErrorCode;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.rokid.glass.instruct.InstructionManager;
import com.rokid.glass.instruct.Integrate.IInstruction;
import com.rokid.glass.instruct.entity.EntityKey;
import com.rokid.glass.instruct.entity.IInstructReceiver;
import com.rokid.glass.instruct.entity.InstructConfig;
import com.rokid.glass.instruct.entity.InstructEntity;
import com.rokid.glass.instruct.type.NumberKey;
import com.rokid.glass.instruct.type.NumberTypeControler;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.xylink.sdk.sample.net.DefaultHttpObserver;
import com.xylink.sdk.sample.utils.CommandExecution;
import com.xylink.sdk.sample.utils.RootCmd;
import com.xylink.sdk.sample.utils.TextUtils;
import com.xylink.uikit.PasswordEditText;
import com.xylink.uikit.dialog.SingleButtonDialog;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

/**
 * 加入会议界面:
 * <p> 小鱼SDK目前的通话可以认为分两步:
 * 1. makeCall: 呼叫准备工作, 验证号码跟密码的有效性等
 * {@link MakeCallResponse#onCallFail(int, String)}, {@link MakeCallResponse#onCallSuccess()}
 * success- 成功: 说明此号码有效可以呼叫-至此可以进入通话界面展示相关UI
 * fail   - 失败: 说明呼叫无法建立(无效号码, 网络不通, 密码错误<有密码的会议室>等)
 * <p>
 * 2. setNemoSDKListener: 具体通话业务, 建立呼叫之后进入通话界面, 在通话界面监听此回调, 实现通话,白板屏幕图片共享等具体业务
 * {@link com.xylink.sdk.sample.XyCallPresenter}
 * NemoSDK.getInstance().setNemoSDKListener(NemoSDKListener):
 *
 * <p>
 * 具体流程参考文档 <>http://openapi.xylink.com/android/</>
 */
public class JoinMeetingActivity extends AppCompatActivity implements IInstruction {
    private static final String TAG = "JoinMeetingActivity";
    private String mCallNumber;
    private ProgressDialog progressDialog;
    private AlertDialog passwordDialog;
    private TextView tv_user;//账号900442
    private TextView tv_expert;//专家号
    private TextView tv_meetting;//会议号918710018302
    private String string_name;
    private String string_meetting;
    private String string_expert;
    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_meeting);
        tv_user = findViewById(R.id.tv_user);
        tv_expert = findViewById(R.id.tv_expert);
        tv_meetting = findViewById(R.id.tv_meetting);
        passwordDialog = new AlertDialog.Builder(this, R.style.AlertDialogStyle).create();
        View view = View.inflate(this, R.layout.view_meeting_password, null);
        view.findViewById(R.id.iv_close).setOnClickListener(v -> passwordDialog.dismiss());
        passwordDialog.setView(view);
        passwordDialog.setCancelable(false);
        passwordDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        NemoSDK.getInstance().setOnStateChangeListener(new NemoSDK.OnStateChangeListener() {
            @Override
            public void onConnectStateChanged(boolean connected) {
                L.i(TAG, "连接状态: " + connected);
            }

            @Override
            public void unauthorized() {
                L.i(TAG, "unauthorized: 需要重新登录下");
            }
        });

        mInstructionManager = new InstructionManager(JoinMeetingActivity.this, closeInstruction(), configInstruct(), mInstructionListener);
        initData();
    }

    private void initData() {
        SharedPreferences sp = getSharedPreferences("xyDemo",Context.MODE_PRIVATE);
        string_name = sp.getString("name","");
        string_meetting = sp.getString("meettingNumber","");
        string_expert = sp.getString("expert","");
        tv_expert.setText(string_expert);
        tv_user.setText(string_name);
        tv_meetting.setText(string_meetting);
        if (string_name.equals("")){
            showToast("请扫码登录");
        }
    }

    private void jointMeeting(String meetingPassword,String meetNum) {
        mCallNumber = meetNum;
        Log.i("baichaoqun", "mCallNumber  :"+mCallNumber);
        showLoading();
        NemoSDK.getInstance().makeCall(mCallNumber, meetingPassword, new MakeCallResponse() {
            @Override
            public void onCallSuccess() {
                // 查询号码成功, 进入通话界面
                L.i(TAG, "success go XyCallActivity");
                hideLoading();
                Intent callIntent = new Intent(JoinMeetingActivity.this, XyCallActivity.class);
                callIntent.putExtra("number", mCallNumber);
                // 如果需要初始化默认这是关闭摄像头或者麦克风, 将callPresenter.start()移至XyCallActivity#onCreate()下
 /*               if (!videoSwitch.isChecked()) {
                    callIntent.putExtra("muteVideo", true);
                }
                if (!audioSwitch.isChecked()) {
                    callIntent.putExtra("muteAudio", true);
                }*/
                startActivity(callIntent);
            }

            @SuppressLint("CheckResult")
            @Override
            public void onCallFail(int error, String msg) {
                Observable.just(0).observeOn(AndroidSchedulers.mainThread()).subscribe(integer -> {
                    hideLoading();
                    if (NemoSDKErrorCode.WRONG_PASSWORD.getCode() == error) {
                        passwordDialog.show();
                    } else {
                        showToast("Error Code: " + error + ", msg: " + msg);
                    }
                });
            }
        });
        // query record permission: 如果要使用录制功能 请务必调此接口
        NemoSDK.getInstance().getRecordingUri(mCallNumber);
    }

    private void showLoading() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(JoinMeetingActivity.this);
            progressDialog.setTitle(R.string.alert_waiting);
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }

    private void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @SuppressLint("CheckResult")
    private void checkPermission(String meetNum) {
        RxPermissions permissions = new RxPermissions(this);
        permissions.request(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                .subscribe(aBoolean -> {
                    L.i(TAG, "request permission result:" + aBoolean);
                    jointMeeting(null,meetNum);
                });
    }

    private void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        }
    }

    private void init(String userName, String passWord) {
        showLoading();
        NemoSDK.getInstance().loginXYlinkAccount(userName, passWord, new ConnectNemoCallback() {
            @Override
            public void onFailed(int i) {
                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(JoinMeetingActivity.this, "初始化失败，请检查网络", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onSuccess(LoginResponseData loginResponseData, boolean b) {
                runOnUiThread(() -> {
                    hideLoading();
                    String deviceDisplayName = loginResponseData.getDeviceDisplayName();
                    AppConfigSp.getInstance().setUserName(deviceDisplayName);
                });

            }

            @Override
            public void onNetworkTopologyDetectionFinished(LoginResponseData loginResponseData) {

            }
        });
    }

    private void showAlert(Type type) {
        switch (type) {
            case INIT:
                SingleButtonDialog initDialog = new SingleButtonDialog.Builder()
                        .setTitleVisible(false)
                        .setCloseVisible(false)
                        .setContentVisible(true)
                        .setContent(getString(R.string.alert_init_error))
                        .setButtonText(getString(R.string.alert_finish))
                        .build();
                initDialog.setCancelable(false);
                if (!initDialog.isVisible() && !initDialog.isAdded()) {
                    initDialog.showNow(getSupportFragmentManager(), "initError");
                }
                initDialog.setCallback(new SingleButtonDialog.OnDialogCallback() {
                    @Override
                    public void onButtonClicked(Button button) {
                        finish();
                    }

                    @Override
                    public void onCloseClicked(ImageView view) {

                    }
                });
                break;
            case CREATE_MEETING_ROOM:
                SingleButtonDialog dialog = new SingleButtonDialog.Builder()
                        .setTitleVisible(false)
                        .setCloseVisible(false)
                        .setContentVisible(true)
                        .setContent(getString(R.string.alert_meeting_effective_time))
                        .setButtonText(getString(R.string.alert_know))
                        .build();
                dialog.setCancelable(false);
                if (!dialog.isVisible() && !dialog.isAdded()) {
                    dialog.showNow(getSupportFragmentManager(), "createMeetingRoom");
                }
                dialog.setCallback(new SingleButtonDialog.OnDialogCallback() {
                    @Override
                    public void onButtonClicked(Button button) {
                        createMeetingRoom();
                    }

                    @Override
                    public void onCloseClicked(ImageView view) {

                    }
                });
                break;
            default:
        }
    }

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
                                .addEntityKey(new EntityKey("修改会议室", "xiu gai hui yi shi"))
                                .addEntityKey(new EntityKey(EntityKey.Language.en, "change meetting"))
                                .setShowTips(true)
                                .setCallback(new IInstructReceiver() {
                                    @Override
                                    public void onInstructReceive(Activity act, String key, InstructEntity instruct) {
                                        Intent intent = new Intent(Intent.ACTION_MAIN);
                                        ComponentName componentName = new ComponentName("com.rokid.glass.scan2","com.rokid.glass.scan2.activity.QrCodeActivity");
                                        intent.setComponent(componentName);
                                        startActivityForResult(intent,100);
                                    }
                                })
                ).addInstructEntity(
                new InstructEntity()
                        .addEntityKey(new EntityKey("修改专家信息", "xiu gai zhuan jia xin xi"))
                        .addEntityKey(new EntityKey(EntityKey.Language.en, "change expert"))
                        .setShowTips(true)
                        .setCallback(new IInstructReceiver() {
                            @Override
                            public void onInstructReceive(Activity act, String key, InstructEntity instruct) {

                                Intent intent = new Intent(Intent.ACTION_MAIN);
                                ComponentName componentName = new ComponentName("com.rokid.glass.scan2","com.rokid.glass.scan2.activity.QrCodeActivity");
                                intent.setComponent(componentName);
                                startActivityForResult(intent,200);


                            }
                        })
        ). addInstructEntity(
                new InstructEntity()
                        .addEntityKey(new EntityKey("加入会议", "jia ru hui yi"))
                        .addEntityKey(new EntityKey(EntityKey.Language.en, "join meetting"))
                        .setShowTips(true)
                        .setCallback(new IInstructReceiver() {
                            @Override
                            public void onInstructReceive(Activity act, String key, InstructEntity instruct) {
                                if (android.text.TextUtils.isEmpty(string_meetting)) {
                                    showToast(R.string.text_input_meeting_number);
                                    return;
                                }
                                hideSoftKeyboard();
                                checkPermission(string_meetting);
                                Log.i("baichaoqun", "changan..."+key);
//
                            }
                        })
        ). addInstructEntity(
                new InstructEntity()
                        .addEntityKey(new EntityKey("呼叫专家", "hu jiao zhuan jia"))
                        .addEntityKey(new EntityKey(EntityKey.Language.en, "call expert"))
                        .setShowTips(true)
                        .setCallback(new IInstructReceiver() {
                            @Override
                            public void onInstructReceive(Activity act, String key, InstructEntity instruct) {
                                if (android.text.TextUtils.isEmpty(string_expert)) {
                                    showToast(R.string.text_input_meeting_number);
                                    return;
                                }
                                hideSoftKeyboard();
                                checkPermission(string_expert);
                                Log.i("baichaoqun", "专家..."+key);
//
                            }
                        })
        ). addInstructEntity(
                new InstructEntity()
                        .addEntityKey(new EntityKey("修改代理", "xiu gai dai li"))
                        .addEntityKey(new EntityKey(EntityKey.Language.en, "change proxy"))
                        .setShowTips(true)
                        .setCallback(new IInstructReceiver() {
                            @Override
                            public void onInstructReceive(Activity act, String key, InstructEntity instruct) {
                                Intent intent = new Intent(Intent.ACTION_MAIN);
                                ComponentName componentName = new ComponentName("com.rokid.glass.scan2","com.rokid.glass.scan2.activity.QrCodeActivity");
                                intent.setComponent(componentName);
                                startActivityForResult(intent,300);
                            }
                        })
        ). addInstructEntity(
                new InstructEntity()
                        .addEntityKey(new EntityKey("删除代理", "shan chu dai li"))
                        .addEntityKey(new EntityKey(EntityKey.Language.en, "delete proxy"))
                        .setShowTips(true)
                        .setCallback(new IInstructReceiver() {
                            @Override
                            public void onInstructReceive(Activity act, String key, InstructEntity instruct) {
                                Settings.Global.putString(JoinMeetingActivity.this.getContentResolver(), "http_proxy",null);
                                Settings.Global.putString(JoinMeetingActivity.this.getContentResolver(), "global_http_proxy_host",null);
                                Settings.Global.putString(JoinMeetingActivity.this.getContentResolver(), "global_http_proxy_port",null);
                                String proxy = System.getProperty( "http.proxyHost" );
                                String port = System.getProperty( "http.proxyPort" );
                                Log.e(TAG, "proxy: "+proxy+" port:"+port);
                                showToast("请重启设备后生效" );
                            }
                        })
        ). addInstructEntity(
                new InstructEntity()
                        .addEntityKey(new EntityKey("会议室列表", "hui yi shi lie biao"))
                        .addEntityKey(new EntityKey(EntityKey.Language.en, "open meetting list"))
                        .setShowTips(true)
                        .setCallback(new IInstructReceiver() {
                            @Override
                            public void onInstructReceive(Activity act, String key, InstructEntity instruct) {
                                Intent intent = new Intent(JoinMeetingActivity.this,MeettingListActivity.class);
                                intent.putExtra("MY_NUMBER",string_name);
                                startActivity(intent);
                            }
                        })
        );
        return config;
//        return null;
    }

    @Override
    public boolean doReceiveCommand(String command) {
        return false;
    }

    @Override
    public void onInstrucUiReady() {
        if (mInstructionManager != null) {
            mInstructionManager.setLeftBackShowing(false);
        }
        hideTipsLayer();
    }
    public void hideTipsLayer()
    {
        if (mInstructionManager != null) {
            mInstructionManager.hideTipsLayer();
        }

    }

    public enum Type {
        // 初始化弹框
        INIT,
        // 创建随机会议室弹框
        CREATE_MEETING_ROOM
    }

    private void createMeetingRoom() {
        showLoading();
        // replace with your own token
        final String token = "a8208f6495ba1d468343cc63c13c5202837b5ba8614bdc699f46e91b4df09fd0";
        long startTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis() + 24 * 60 * 1000;
        NemoSDK.getInstance().createMeetingRoom(token, "哦哈呦", startTime, endTime)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultHttpObserver<MeetingRoomResponse>("createMeetingRoom") {
                    @Override
                    public void onNext(MeetingRoomResponse o, boolean isJson) {
                        super.onNext(o, isJson);
                        hideLoading();
                        L.i(TAG, "meetingRoomResponse: " + o.toString());
                        tv_meetting.setText(o.getMeetingNumber());
                        AppConfigSp.getInstance().setMeetingNumber(o.getMeetingNumber());
                        AppConfigSp.getInstance().setMeetingEndTime(endTime);
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        super.onException(throwable);
                        hideLoading();
                        showToast(R.string.toast_create_meeting_fail);
                    }

                    @Override
                    public void onHttpError(HttpException exception, String errorData, boolean isJSON) {
                        super.onHttpError(exception, errorData, isJSON);
                        hideLoading();
                        showToast(R.string.toast_create_meeting_fail);
                    }
                });
    }

    private void showToast(@StringRes int message) {
        Toast.makeText(JoinMeetingActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void showToast(String message) {
        Toast.makeText(JoinMeetingActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mInstructionManager != null) {
            mInstructionManager.onStart();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mInstructionManager != null) {
            mInstructionManager.onDestroy();
            mInstructionManager = null;
        }

    }

    @Override
    protected void onResume() {
        if (mInstructionManager != null) {
            mInstructionManager.onResume();
        }

        super.onResume();
    }

    @Override
    protected void onPause() {

        if (mInstructionManager != null) {
            mInstructionManager.onPause();
        }

        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data!=null)
        {
            String datas=data.getStringExtra("com.blikoon.qrcodescanner.got_qr_scan_relult");
            if(datas.length()>0)
            {
                if (requestCode==100){
                    String[] str=datas.split(";");
                    if(str!=null&&str.length==3)
                    {
                        Log.i("baichaoqun", str[0]+"  "+str[1]+"  " +str[2]);
                        saveNumber(str[0],str[1],str[2]);
                        tv_user.setText(str[0]);
                        tv_meetting.setText(str[2]);
                        string_name = str[0];
                        string_meetting = str[2];
                        init(str[0],str[1]);
                    }
                }else if (requestCode==200){
                    saveNumber(datas);
                    tv_expert.setText(datas);
                }else if (requestCode==300){
                    Settings.Global.putString(JoinMeetingActivity.this.getContentResolver(), "http_proxy",datas);
                    String proxy = System.getProperty( "http.proxyHost" );
                    String port = System.getProperty( "http.proxyPort" );
                    Log.e(TAG, "proxy: "+proxy+" port:"+port);
                    showToast("代理已生效"+datas);
                }else if (requestCode==400){
//                    String[] command ={"adb shell settings delete global http_proxy","adb shell settings delete global global_http_proxy_host","adb shell settings delete global global_http_proxy_port"};
//                    CommandExecution.execCommand(command,true);

                }
            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    public void saveNumber(String name, String password, String meettingNumber){
        SharedPreferences preferences = getSharedPreferences("xyDemo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("name",name);
        editor.putString("password",password);
        editor.putString("meettingNumber",meettingNumber);
        editor.commit();
    }

    public void saveNumber(String expert){
        SharedPreferences preferences = getSharedPreferences("xyDemo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("expert",expert);
        editor.commit();
    }
}
