package com.xylink.sdk.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ainemo.sdk.otf.NemoSDK;
import com.ainemo.sdk.otf.Settings;
import com.xylink.sdk.sample.utils.TextUtils;

/**
 * 设置服务器界面
 *
 * @author zhangyazhou
 */
public class SetServerActivity extends AppCompatActivity {

    private EditText extIdEt;
    private CheckBox privateModeCb;
    private RadioButton devHostRb;
    private EditText privateHostEt;
    private AppConfigSp appConfigSp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_server);

        LinearLayout xyPrdExtIdLl = findViewById(R.id.xy_prd_ext_id_ll);
        LinearLayout xyDevExtIdLl = findViewById(R.id.xy_dev_ext_id_ll);
        TextView prdExtTv = findViewById(R.id.prd_ext_tv);
        TextView devExtTv = findViewById(R.id.dev_ext_tv);
        extIdEt = findViewById(R.id.ext_id_et);
        privateModeCb = findViewById(R.id.private_mode_cb);
        LinearLayout hostSelectLl = findViewById(R.id.host_select_ll);
        devHostRb = findViewById(R.id.dev_host_rb);
        RadioButton prdHostRb = findViewById(R.id.prd_host_rb);
        LinearLayout privateHostLl = findViewById(R.id.private_host_ll);
        privateHostEt = findViewById(R.id.private_host_et);
        Button saveBtn = findViewById(R.id.save_config_btn);

        prdExtTv.setText(AppConfigSp.XY_PRD_EXT_ID);
        xyPrdExtIdLl.setOnLongClickListener(v -> {
            extIdEt.setText(AppConfigSp.XY_PRD_EXT_ID);
            return true;
        });
        devExtTv.setText(AppConfigSp.XY_DEV_EXT_ID);
        xyDevExtIdLl.setOnLongClickListener(v -> {
            extIdEt.setText(AppConfigSp.XY_DEV_EXT_ID);
            return true;
        });

        appConfigSp = AppConfigSp.getInstance();
        extIdEt.setText(appConfigSp.getExtId());
        if (appConfigSp.isPrivateMode()) {
            privateModeCb.setChecked(true);
            hostSelectLl.setVisibility(View.GONE);
            privateHostLl.setVisibility(View.VISIBLE);
            privateHostEt.setText(appConfigSp.getPrivateHost());
        } else {
            privateModeCb.setChecked(false);
            hostSelectLl.setVisibility(View.VISIBLE);
            privateHostLl.setVisibility(View.GONE);
            if (appConfigSp.isDebugMode()) {
                devHostRb.setChecked(true);
                prdHostRb.setChecked(false);
            } else {
                devHostRb.setChecked(false);
                prdHostRb.setChecked(true);
            }
        }

        privateModeCb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                hostSelectLl.setVisibility(View.GONE);
                privateHostLl.setVisibility(View.VISIBLE);
            } else {
                hostSelectLl.setVisibility(View.VISIBLE);
                privateHostLl.setVisibility(View.GONE);
            }
        });

        saveBtn.setOnClickListener(v -> saveAppConfig());
    }

    private void saveAppConfig() {
        String extId = extIdEt.getText().toString();
        String privateHost = privateHostEt.getText().toString();
        if (privateModeCb.isChecked()) {
            if (TextUtils.isEmpty(privateHost)) {
                Toast.makeText(SetServerActivity.this,
                        "请设置私有云服务器地址", Toast.LENGTH_LONG).show();
            } else {
                saveConfig(extId, devHostRb.isChecked(), true,
                        privateHostEt.getText().toString());
                initSDK(extId, false, privateHost);
            }
        } else {
            if (devHostRb.isChecked()) {
                if (AppConfigSp.XY_PRD_EXT_ID.equals(extIdEt.getText().toString())) {
                    Toast.makeText(SetServerActivity.this,
                            "当前为Dev环境但是企业id为Prd环境小鱼企业Id, 请重新设置.", Toast.LENGTH_LONG)
                            .show();
                } else {
                    saveConfig(extId, true, false, "");
                    initSDK(extId, true, "");
                }
            } else {
                if (AppConfigSp.XY_DEV_EXT_ID.equals(extIdEt.getText().toString())) {
                    Toast.makeText(SetServerActivity.this,
                            "当前为Prd环境但是企业id为Dev环境小鱼企业Id, 请重新设置.", Toast.LENGTH_LONG)
                            .show();
                } else {
                    saveConfig(extId, false, false, "");
                    initSDK(extId, false, "");
                }
            }
        }
    }

    private void saveConfig(String extId, boolean debugMode, boolean privateMode, String privateHost) {
        appConfigSp.saveExtId(extId);
        appConfigSp.saveDebugMode(debugMode);
        appConfigSp.savePrivateMode(privateMode);
        appConfigSp.savePrivateHost(privateHost);
    }

    private void initSDK(String extId, boolean debugMode, String privateHost) {
        Settings settings = new Settings(extId);
        settings.setDebug(debugMode);
        settings.setPrivateCloudAddress(privateHost);
        NemoSDK.getInstance().init(MyApplication.getContext(), settings);
        finish();
    }
}
