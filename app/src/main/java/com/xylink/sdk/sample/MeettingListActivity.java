package com.xylink.sdk.sample;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.log.L;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ainemo.sdk.otf.MakeCallResponse;
import com.ainemo.sdk.otf.NemoSDK;
import com.ainemo.sdk.otf.VideoInfo;
import com.rokid.glass.instruct.InstructionManager;
import com.rokid.glass.instruct.Integrate.IInstruction;
import com.rokid.glass.instruct.entity.EntityKey;
import com.rokid.glass.instruct.entity.IInstructReceiver;
import com.rokid.glass.instruct.entity.InstructConfig;
import com.rokid.glass.instruct.entity.InstructEntity;
import com.rokid.glass.instruct.type.NumberKey;
import com.rokid.glass.instruct.type.NumberTypeControler;
import com.xylink.sdk.sample.adapters.MeetingNameAdapter;
import com.xylink.sdk.sample.bean.MeettingInfoData;
import com.xylink.sdk.sample.dao.MeettingDao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

public class MeettingListActivity extends AppCompatActivity implements IInstruction {

    ListView lv_meeting;
    TextView tv_user;
    String myNumber = "";
    private String mCallNumber;
    MeetingNameAdapter adapter;
    public List<MeettingInfoData> mList = new ArrayList<>();
    private ProgressDialog progressDialog;
    private int AddrequestCode = 100;
    String TAG = "MeettingListActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetting_list);
        initData();
        setViews();
        setAdapers();
        setListeners();
        mInstructionManager = new InstructionManager(MeettingListActivity.this, closeInstruction(), configInstruct(), mInstructionListener);
    }

    private void initData() {
        Intent intent = getIntent();
        myNumber = intent.getStringExtra("MY_NUMBER");
    }

    private void setViews() {
        lv_meeting = findViewById(R.id.lv_meeting);
        tv_user = findViewById(R.id.tv_user);
        if (myNumber.equals("")||myNumber==""){
            tv_user.setText("请登录");
        }else {
            tv_user.setText(myNumber);
        }
    }

    private void setAdapers() {
        mList = MeettingDao.getAllMeetting();
        adapter = new MeetingNameAdapter(this,mList);
        lv_meeting.setAdapter(adapter);
    }

    private void setListeners() {

    }

    public void inRoom(String number,String password){
        if (android.text.TextUtils.isEmpty(number)) {
            Toast.makeText(MeettingListActivity.this, "请输入呼叫号码", Toast.LENGTH_SHORT).show();
            return;
        }
        mCallNumber = number;
        showLoading();
        String pwd = password;

        NemoSDK.getInstance().makeCall(mCallNumber, pwd, new MakeCallResponse() {
            @Override
            public void onCallSuccess() {
                // 查询号码成功, 进入通话界面
                L.i("inroom", "success go XyCallActivity");
                hideLoading();
                Intent callIntent = new Intent(MeettingListActivity.this, XyCallActivity.class);
                callIntent.putExtra("number", mCallNumber);
                startActivity(callIntent);
                insertOrReplace();
                finish();
            }



            @Override
            public void onCallFail(final int error, final String msg) {
                Observable.just(0).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        hideLoading();
                        Toast.makeText(MeettingListActivity.this,
                                "Error Code: " + error + ", msg: " + msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // query record permission
        NemoSDK.getInstance().getRecordingUri(mCallNumber);
    }

    private void insertOrReplace() {
        try {
            MeettingInfoData data = new MeettingInfoData();
            data.setMeettingName("最近的会议室");
            data.setMeettingNum(mCallNumber);
            data.setTime(new Date().getTime());
            MeettingDao.insertMeetting(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showLoading() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(MeettingListActivity.this);
            progressDialog.setTitle("请稍后...");
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

    private void updateList(){
        mList.clear();
        List<MeettingInfoData> linshi = MeettingDao.getAllMeetting();
        for (int i=0;i<linshi.size();i++){
            mList.add(linshi.get(i));
        }
        linshi = null;
        adapter.notifyDataSetChanged();
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
                                .addEntityKey(new EntityKey("新增会议室", "xin zeng hui yi shi"))
                                .addEntityKey(new EntityKey(EntityKey.Language.en, "add meetting"))
                                .setShowTips(true)
                                .setCallback(new IInstructReceiver() {
                                    @Override
                                    public void onInstructReceive(Activity act, String key, InstructEntity instruct) {
                                        Intent intent = new Intent(Intent.ACTION_MAIN);
                                        ComponentName componentName = new ComponentName("com.rokid.glass.scan2", "com.rokid.glass.scan2.activity.QrCodeActivity");
                                        intent.setComponent(componentName);
                                        startActivityForResult(intent, AddrequestCode);
                                    }
                                })
                ).addInstructList(NumberTypeControler.doTypeControl(1, 10,
                new NumberTypeControler.NumberTypeCallBack() {
                    @Override
                    public void onInstructReceive(Activity act, String key, int number, InstructEntity instruct) {
                        if (number>0 && number<=mList.size()){
                            String meetingNum = mList.get(number-1).getMeettingNum();
                            inRoom(meetingNum,null);
                        }else {
                            Toast.makeText(MeettingListActivity.this,"会议号错误",Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new NumberKey(EntityKey.Language.zh, "打开第", "个", "可以说打开第1/2.../10个"),
                new NumberKey(EntityKey.Language.en, "open", "", "open 1/2.../10")
                )
                ).addInstructList(NumberTypeControler.doTypeControl(1, 10,
                new NumberTypeControler.NumberTypeCallBack() {
                    @Override
                    public void onInstructReceive(Activity act, String key, int number, InstructEntity instruct) {
                        if (number>0 && number<=mList.size()){
                            String meetingNum = mList.get(number-1).getMeettingNum();
                            //删除会议号
                            MeettingInfoData meeting = MeettingDao.getMeetingByNum(meetingNum);
                            MeettingDao.deleteMeettingByKey(meeting.getId());
                            updateList();
                            Toast.makeText(MeettingListActivity.this,"删除成功",Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(MeettingListActivity.this,"会议号错误",Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new NumberKey(EntityKey.Language.zh, "删除第", "个", "可以说删除第1/2.../10个"),
                new NumberKey(EntityKey.Language.en, "delete", "", "delete 1/2.../10")
                )
        );
        return config;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data!=null){
            if (requestCode == AddrequestCode ){
                String datas=data.getStringExtra("com.blikoon.qrcodescanner.got_qr_scan_relult");
                if(datas.length()>0) {
                    String[] str=datas.split(";");
                    if (str.length>0){
                        for (int i=0;i<str.length;i++){
                            String[] meet = str[i].split(":");
                            if (meet.length>1){
                                String meetName = meet[0];
                                String meetNum = meet[1];
                                if (meetNum.contains(";")){
                                    meetNum = meetNum.replace(";","");
                                }
                                Log.e(TAG, "meetName: "+meetName );
                                Log.e(TAG, "meetNum: "+meetNum );
                                MeettingInfoData mData = new MeettingInfoData();
                                mData.setMeettingName(meetName);
                                mData.setMeettingNum(meetNum);
                                mData.setTime(new Date().getTime());
                                MeettingDao.insertMeetting(mData);
                            }
                        }
                    }
                }
                updateList();
            }
        }

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
}
