package com.xylink.uikit.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.xylink.uikit.R;

/**
 * 单按钮Dialog
 *
 * @author zhangyazhou
 * @date 2018/12/10
 */
public class SingleButtonDialog extends DialogFragment {

    private static final String TAG = "SingleButtonDialog";

    private ImageView closeIv;
    private Button primaryBtn;

    private OnDialogCallback callback;

    private @DrawableRes
    int topImageRes;

    private String title;

    private boolean titleVisible = true;

    private boolean contentVisible = true;

    private String content;

    private Spanned contentSpanned;

    private String buttonText;

    private @DrawableRes
    int buttonBgRes;

    private boolean closeVisible;

    private @DrawableRes
    int closeImageRes;

    private boolean topImageVisible;

    public SingleButtonDialog() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            //see the detail @DoubleButtonDialog.onCreate(Bundle)
            dismissAllowingStateLoss();
        }
        Bundle arguments = getArguments();
        if (arguments != null) {
            topImageRes = arguments.getInt(Builder.TOP_IMAGE, 0);
            title = arguments.getString(Builder.TITLE);
            titleVisible = arguments.getBoolean(Builder.TITLE_VISIBLE, true);
            contentVisible = arguments.getBoolean(Builder.CONTENT_VISIBLE, true);
            content = arguments.getString(Builder.CONTENT);
            contentSpanned = (Spanned) arguments.getCharSequence(Builder.CONTENT_SPANNED);
            buttonText = arguments.getString(Builder.BUTTON_TEXT);
            buttonBgRes = arguments.getInt(Builder.BUTTON_BG_RES, 0);
            closeVisible = arguments.getBoolean(Builder.CLOSE_VISIBLE, false);
            closeImageRes = arguments.getInt(Builder.CLOSE_IMAGE_RES, 0);
            topImageVisible = arguments.getBoolean(Builder.TOP_IMAGE_VISIBLE, false);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.drawable.bg_dialog);
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 288,
                    getResources().getDisplayMetrics());
            window.setAttributes(params);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return getLayoutInflater().inflate(R.layout.fragment_single_button_dialog, container);
    }

    public static void showDialog(FragmentManager manager, String title, String content,
                                  String button, OnDialogCallback callback, String key) {
        SingleButtonDialog dialog = new Builder()
                .setTitle(title)
                .setContent(content)
                .setButtonText(button)
                .build();
        //dialog.show(manager, key);
        dialog.setCallback(callback);
        manager.beginTransaction().add(dialog, key).commitAllowingStateLoss();
    }

    public static void showDialog(FragmentManager manager, boolean cancelable, String title, String content,
                                  String button, OnDialogCallback callback, String key) {
        SingleButtonDialog dialog = new Builder()
                .setTitle(title)
                .setContent(content)
                .setButtonText(button)
                .build();
        //dialog.show(manager, key);
        dialog.setCallback(callback);
        dialog.setCancelable(cancelable);
        manager.beginTransaction().add(dialog, key).commitAllowingStateLoss();
    }

    public static void showDialogWithClose(FragmentManager manager, String title, String content,
                                           String button, OnDialogCallback callback, String key) {
        SingleButtonDialog dialog = new Builder()
                .setTitle(title)
                .setContent(content)
                .setButtonText(button)
                .setCloseVisible(true)
                .build();
        //dialog.show(manager, key);
        dialog.setCallback(callback);
        manager.beginTransaction().add(dialog, key).commitAllowingStateLoss();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView topImageIv = view.findViewById(R.id.dialog_top_image_iv);
        TextView titleTv = view.findViewById(R.id.dialog_title_tv);
        TextView contentTv = view.findViewById(R.id.dialog_content_tv);
        closeIv = view.findViewById(R.id.dialog_close_iv);
        primaryBtn = view.findViewById(R.id.dialog_primary_button);
        primaryBtn.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                dismissAllowingStateLoss();
                if (callback != null) {
                    callback.onButtonClicked(primaryBtn);
                }
            }
        });
        closeIv.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                dismissAllowingStateLoss();
                if (callback != null) {
                    callback.onCloseClicked(closeIv);
                }
            }
        });

        topImageIv.setVisibility(topImageVisible ? View.VISIBLE : View.GONE);
        if (topImageRes != 0) {
            topImageIv.setImageResource(topImageRes);
        }
        closeIv.setVisibility(closeVisible ? View.VISIBLE : View.GONE);
        if (closeImageRes != 0) {
            closeIv.setImageResource(closeImageRes);
        }

        titleTv.setText(title);

        titleTv.setVisibility(titleVisible ? View.VISIBLE : View.GONE);
        contentTv.setVisibility(contentVisible ? View.VISIBLE : View.GONE);
        contentTv.setText(contentSpanned == null ? content : contentSpanned);

        if (buttonText != null) {
            primaryBtn.setText(buttonText);
        }

        if (buttonBgRes != 0) {
            primaryBtn.setBackgroundResource(buttonBgRes);
        }
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(this, tag);
        transaction.commitAllowingStateLoss();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void setCallback(OnDialogCallback callback) {
        this.callback = callback;
    }

    public static class Builder {
        private static final String TOP_IMAGE = "topImageRes";
        private static final String TITLE = "title";
        private static final String TITLE_VISIBLE = "titleVisible";
        private static final String CONTENT_VISIBLE = "contentVisible";
        private static final String CONTENT = "content";
        private static final String CONTENT_SPANNED = "contentSpanned";
        private static final String BUTTON_TEXT = "buttonText";
        private static final String BUTTON_BG_RES = "buttonBgRes";
        private static final String CLOSE_VISIBLE = "closeVisible";
        private static final String CLOSE_IMAGE_RES = "closeImageRes";
        private static final String TOP_IMAGE_VISIBLE = "topImageVisible";
        private Bundle mDataBundle = new Bundle();

        public Builder setTopImageRes(int topImageRes) {
            mDataBundle.putInt(TOP_IMAGE, topImageRes);
            return this;
        }

        public Builder setTitle(String title) {
            mDataBundle.putString(TITLE, title);
            return this;
        }

        public Builder setContentVisible(boolean contentVisible) {
            mDataBundle.putBoolean(CONTENT_VISIBLE, contentVisible);
            return this;
        }

        public Builder setContent(String content) {
            mDataBundle.putString(CONTENT, content);
            return this;
        }

        public Builder setContentSpanned(Spanned contentSpanned) {
            mDataBundle.putCharSequence(CONTENT_SPANNED, contentSpanned);
            return this;
        }

        public Builder setButtonText(String buttonText) {
            mDataBundle.putString(BUTTON_TEXT, buttonText);
            return this;
        }

        public Builder setButtonBgRes(int buttonBgRes) {
            mDataBundle.putInt(BUTTON_BG_RES, buttonBgRes);
            return this;
        }

        public Builder setCloseVisible(boolean closeVisible) {
            mDataBundle.putBoolean(CLOSE_VISIBLE, closeVisible);
            return this;
        }

        public Builder setCloseImageRes(int closeImageRes) {
            mDataBundle.putInt(CLOSE_IMAGE_RES, closeImageRes);
            return this;
        }

        public Builder setTopImageVisible(boolean topImageVisible) {
            mDataBundle.putBoolean(TOP_IMAGE_VISIBLE, topImageVisible);
            return this;
        }

        public Builder setTitleVisible(boolean titleVisible) {
            mDataBundle.putBoolean(TITLE_VISIBLE, titleVisible);
            return this;
        }

        public SingleButtonDialog build() {
            SingleButtonDialog dialog = new SingleButtonDialog();
            dialog.setArguments(mDataBundle);
            return dialog;
        }
    }

    public interface OnDialogCallback {
        /**
         * 按钮点击回调
         *
         * @param button 按钮对象本身
         */
        void onButtonClicked(Button button);

        /**
         * 关闭按钮返回回调
         *
         * @param view 关闭按钮
         */
        void onCloseClicked(ImageView view);
    }
}
