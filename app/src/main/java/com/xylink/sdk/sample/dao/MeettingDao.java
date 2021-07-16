package com.xylink.sdk.sample.dao;

import android.util.Log;

import com.xylink.sdk.sample.MyApplication;
import com.xylink.sdk.sample.bean.MeettingInfoData;
import com.xylink.sdk.sample.gen.MeettingInfoDataDao;

import java.util.List;

public class MeettingDao {
    /**
     * 添加会议信息
     * */
    public static void insertMeetting(MeettingInfoData alarmData){
        Log.e("MeettingDao", "insertMeetting ");
        List<MeettingInfoData> list =  MyApplication.getDaoInstant().getMeettingInfoDataDao().queryBuilder().list();
        boolean flag = true;
        for (int i = 0;i<list.size();i++){
            if (alarmData.getMeettingNum().equals(list.get(i).getMeettingNum())){
//                MyApplication.getDaoInstant().getMeettingInfoDataDao().update(alarmData);
                flag = false;
                Log.e("flag", "flag: "+flag);
            }
        }
        if (flag){
            MyApplication.getDaoInstant().getMeettingInfoDataDao().insertOrReplace(alarmData);
        }

    }
    /**
     * 删除会议信息
     * */
    public static void deleteMeetting(MeettingInfoData alarmData){
        MyApplication.getDaoInstant().getMeettingInfoDataDao().delete(alarmData);
    }
    /**
     * 删除会议信息byId
     * */
    public static void deleteMeettingByKey(long id){
        MyApplication.getDaoInstant().getMeettingInfoDataDao().deleteByKey(id);
    }
    /**
     * 删除所有会议信息
     * */
    public static void deleteAllMeetting(){
        MyApplication.getDaoInstant().getMeettingInfoDataDao().deleteAll();
    }
    /**
     * 按时间倒序查询所有会议信息
     * */
    public static List<MeettingInfoData> getAllMeetting(){
        Log.e("MeettingDao", "getAllMeetting ");
        List<MeettingInfoData> list =  MyApplication.getDaoInstant().getMeettingInfoDataDao().queryBuilder()
                .orderDesc(MeettingInfoDataDao.Properties.Time)
                .list();
        return list;
    }

    public static MeettingInfoData getMeetingByNum(String num){
        return MyApplication.getDaoInstant().getMeettingInfoDataDao().queryBuilder().where(MeettingInfoDataDao.Properties.MeettingNum.eq(num)).build().unique();
    }
}
