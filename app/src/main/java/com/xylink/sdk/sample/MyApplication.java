package com.xylink.sdk.sample;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Process;

import com.ainemo.sdk.otf.NemoSDK;
import com.ainemo.sdk.otf.Settings;
import com.ainemo.sdk.otf.VideoConfig;
import com.rokid.glass.instruct.VoiceInstruction;
import com.xylink.sdk.sample.gen.DaoMaster;
import com.xylink.sdk.sample.gen.DaoSession;

import java.util.List;

/**
 * 自定的Application
 *
 * @author zhangyazhou
 */
public class MyApplication extends Application {
    private static Context context;
    private static DaoSession daoSession;
    String db_meet = "meeting_data.db";
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        // 替换你自己的企业id
        Settings settings = new Settings("12e53a6df2e91e6177e627c8e336a6888ff98104");

        // Note: 默认或者不设置为360P, 360P满足大部分场景 如特殊场景需要720P, 请综合手机性能设置720P, 如果手机性
        // 能过差会出现卡顿,无法传输的情况, 请自己权衡.
        settings.setVideoMaxResolutionTx(VideoConfig.VD_1280x720);

        int pId = Process.myPid();
        String processName = "";
        ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> ps = am.getRunningAppProcesses();
        for (RunningAppProcessInfo p : ps) {
            if (p.pid == pId) {
                processName = p.processName;
                break;
            }
        }

        // 避免被初始化多次
        if (processName.equals(getPackageName())) {
            NemoSDK nemoSDK = NemoSDK.getInstance();
            nemoSDK.init(this, settings);
        }

        VoiceInstruction.init(this);
        setupDatabase();
    }

    public static Context getContext() {
        return context;
    }
    private void setupDatabase() {
        //创建数据库node_device_info.db
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, db_meet, null);
        //获取可写数据库
        SQLiteDatabase db = helper.getWritableDatabase();
        //获取数据库对象
        DaoMaster daoMaster = new DaoMaster(db);
        //获取dao对象管理者
        daoSession = daoMaster.newSession();
    }


    public static DaoSession getDaoInstant() {
        return daoSession;
    }

}

