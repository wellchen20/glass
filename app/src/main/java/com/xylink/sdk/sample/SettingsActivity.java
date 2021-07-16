package com.xylink.sdk.sample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.log.L;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ainemo.sdk.otf.NemoSDK;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.xylink.sdk.sample.net.DefaultHttpObserver;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class SettingsActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> finish());

        EditText etName = findViewById(R.id.tv_name);
        etName.setText(AppConfigSp.getInstance().getUserName());

        TextView tvSave = findViewById(R.id.tv_save);

        TextView tvSDKVersion = findViewById(R.id.tv_sdk_version);
        tvSDKVersion.setText(NemoSDK.getInstance().getSDKVersion());

        findViewById(R.id.fl_feedback).setOnClickListener(v ->
                startActivity(new Intent(SettingsActivity.this, FeedbackActivity.class)));

        RxTextView.textChanges(etName).subscribe(charSequence -> {
            if (charSequence != null && charSequence.length() > 0) {
                tvSave.setEnabled(true);
                tvSave.setTextColor(Color.parseColor("#ff3876ff"));
            } else {
                tvSave.setEnabled(false);
                tvSave.setTextColor(Color.parseColor("#4d3876ff"));
            }
        });
        tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString().trim();
                if (name.equals(AppConfigSp.getInstance().getUserName())) {
                    showToast("请输入新的名称");
                    return;
                }
                showLoading();
                NemoSDK.getInstance().changeName(name)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DefaultHttpObserver<String>("changeName") {
                            @Override
                            public void onNext(String o, boolean isJson) {
                                super.onNext(o, isJson);
                                L.i("o: " + o);
                                hideLoading();
                                showToast("修改成功");
                                AppConfigSp.getInstance().setUserName(name);
                            }

                            @Override
                            public void onException(Throwable throwable) {
                                super.onException(throwable);
                                hideLoading();
                                showToast("修改失败");
                            }

                            @Override
                            public void onHttpError(HttpException exception, String errorData, boolean isJSON) {
                                super.onHttpError(exception, errorData, isJSON);
                                hideLoading();
                                showToast("修改失败");
                            }
                        });
            }
        });
    }

    private void showLoading() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(SettingsActivity.this);
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

    private void showToast(String message) {
        Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
