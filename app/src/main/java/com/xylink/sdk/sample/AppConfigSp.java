package com.xylink.sdk.sample;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * App配置
 *
 * @author zhangyazhou
 */
public class AppConfigSp {

    private static final String SP_NAME = "config_sp";
    private static final String EXT_ID = "ext_id";
    private static final String PRIVATE_HOST = "private_host";
    private static final String PRIVATE_MODE = "private_mode";
    private static final String DEBUG_MODE = "debug_mode";
    private static final String MEETING_NUMBER = "meeting_number";
    private static final String MEETING_END_TIME = "meeting_end_time";
    private static final String USER_NAME = "user_name";
    private static final String USER_UUID = "user_uuid";
    private static final String USER_PHONE = "user_phone";
    public static final String XY_PRD_EXT_ID = "12e53a6df2e91e6177e627c8e336a6888ff98104";
    public static final String XY_DEV_EXT_ID = "40260e9046bae2da238ac0b0c572326b91726a83";

    private SharedPreferences prefs;

    private static class InstanceHolder {
        private static final AppConfigSp INSTANCE = new AppConfigSp();
    }

    public static AppConfigSp getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private AppConfigSp() {
        prefs = MyApplication.getContext().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    public void saveExtId(String extId) {
        prefs.edit().putString(EXT_ID, extId).apply();
    }

    public String getExtId() {
        return prefs.getString(EXT_ID, XY_PRD_EXT_ID);
    }

    public void saveDebugMode(boolean debugMode) {
        prefs.edit().putBoolean(DEBUG_MODE, debugMode).apply();
    }

    public boolean isDebugMode() {
        return prefs.getBoolean(DEBUG_MODE, false);
    }

    public void savePrivateMode(boolean privateMode) {
        prefs.edit().putBoolean(PRIVATE_MODE, privateMode).apply();
    }

    public boolean isPrivateMode() {
        return prefs.getBoolean(PRIVATE_MODE, false);
    }

    public void savePrivateHost(String host) {
        prefs.edit().putString(PRIVATE_HOST, host).apply();
    }

    public String getPrivateHost() {
        return prefs.getString(PRIVATE_HOST, "");
    }

    public void setMeetingNumber(String meetingNumber) {
        prefs.edit().putString(MEETING_NUMBER, meetingNumber).apply();
    }

    public String getMeetingRoom() {
        return prefs.getString(MEETING_NUMBER, "");
    }

    public void setMeetingEndTime(long meetingNumber) {
        prefs.edit().putLong(MEETING_END_TIME, meetingNumber).apply();
    }

    public long getMeetingEndTime() {
        return prefs.getLong(MEETING_END_TIME, 0);
    }

    public void setUserName(String name) {
        prefs.edit().putString(USER_NAME, name).apply();
    }

    public String getUserName() {
        return prefs.getString(USER_NAME, "");
    }

    public void setUserUuid(String name) {
        prefs.edit().putString(USER_UUID, name).apply();
    }

    public String getUserUuid() {
        return prefs.getString(USER_UUID, "");
    }

    public void setUserPhone(String name) {
        prefs.edit().putString(USER_PHONE, name).apply();
    }

    public String getUserPhone() {
        return prefs.getString(USER_PHONE, "");
    }
}
