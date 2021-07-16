package com.xylink.sdk.sample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.log.L;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.ainemo.sdk.otf.ConnectNemoCallback;
import com.ainemo.sdk.otf.LoginResponseData;
import com.ainemo.sdk.otf.NemoSDK;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.InputStream;

import io.reactivex.functions.Consumer;

/**
 * 小鱼账户登录界面
 *
 * @author zhangyazhou
 */
public class LoginActivity extends AppCompatActivity {

    public static final String LOGIN_TYPE_KEY = "login_type";

    /**
     * 用小鱼账号登录
     */
    public static final int LOGIN_TYPE_XYLINK = 0;
    /**
     * 用第三方账号登录
     */
    public static final int LOGIN_TYPE_EXTERNAL = 1;
    /**
     * 第三方鉴权登录
     */
    public static final int LOGIN_TYPE_THIRD_AUTH = 2;

    private static final String TAG = "LoginActivity";

    private NemoSDK nemoSDK = NemoSDK.getInstance();
    private int loginType;
    //    private EditText displayName;
//    private EditText externalId;
    private ProgressDialog loginDialog;
    /*  private String name = "18710018302";
     private String password = "018302";
   private String name = "15010322959";
     private String password = "cw6501281";*/
    private String callNumber;
    private boolean islogin = false;

    private ProgressDialog progressDialog;
    boolean isFirst = false;
    String json;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_background);
        checkPermission();
        loginXyAccount();
    }

    public void inMeetingList(){
        Intent intent = new Intent(LoginActivity.this, JoinMeetingActivity.class);
        startActivity(intent);
        finish();
    }


    @SuppressLint("CheckResult")
    private void checkPermission() {
        RxPermissions permissions = new RxPermissions(this);
        permissions.request(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        L.i(TAG, "request permission result:" + aBoolean);
                    }
                });
    }

    private void showLoginDialog() {
        loginDialog = new ProgressDialog(this);
        loginDialog.setTitle("登录");
        loginDialog.setMessage("正在登录,请稍后...");
        loginDialog.setCancelable(false);
        loginDialog.show();
    }

    private void dismissDialog() {
        if (loginDialog != null && loginDialog.isShowing()) {
            loginDialog.dismiss();
        }
    }

    private void loginXyAccount() {
        showLoginDialog();
        SharedPreferences sp = getSharedPreferences("xyDemo",Context.MODE_PRIVATE);
        String name = sp.getString("name","");
        String password = sp.getString("password","");
        nemoSDK.loginXYlinkAccount(name, password, new ConnectNemoCallback() {
            @Override
            public void onFailed(final int i) {
                dismissDialog();
                L.e(TAG, "使用小鱼账号登录失败，错误码：" + i);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                        inMeetingList();
                    }
                });
            }

            @Override
            public void onSuccess(LoginResponseData data, boolean isDetectingNetworkTopology) {
                L.i(TAG, "使用小鱼账号登录成功，号码为：" + data.getCallNumber());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissDialog();
                        Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                        inMeetingList();
                    }
                });

            }

            @Override
            public void onNetworkTopologyDetectionFinished(LoginResponseData resp) {
                L.i(TAG, "net detect onNetworkTopologyDetectionFinished 2");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        Toast.makeText(LoginActivity.this, "网络探测已完成", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }



}
