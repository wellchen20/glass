package com.xylink.sdk.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ainemo.sdk.otf.NemoSDK;

/**
 * 问题反馈界面
 *
 * @author zhangyazhou
 */
public class FeedbackActivity extends AppCompatActivity {

    private Button mSendFeedbackButton;
    private EditText feedbackEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> finish());

        mSendFeedbackButton = findViewById(R.id.send_feedback_bt);
        TextWatcher textWatcher = new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (feedbackEditText.getText().length() > 0) {
                    mSendFeedbackButton.setEnabled(true);
                } else {
                    mSendFeedbackButton.setEnabled(false);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        feedbackEditText = findViewById(R.id.FeedbackEditText);
        feedbackEditText.addTextChangedListener(textWatcher);

        mSendFeedbackButton.setOnClickListener(v -> sendFeedback());

        mSendFeedbackButton.setEnabled(false);

        getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        setTitle(R.string.feedback);
    }

    private void sendFeedback() {
        Log.i("TAG", "sendFeedback Android_feedback_=" + feedbackEditText.getText().toString());
        NemoSDK.getInstance().sendFeedbackLog(feedbackEditText.getText().toString());
        Toast.makeText(this, R.string.feedback_success, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
