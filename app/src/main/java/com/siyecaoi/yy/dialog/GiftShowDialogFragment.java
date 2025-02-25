package com.siyecaoi.yy.dialog;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.siyecaoi.yy.R;
import com.siyecaoi.yy.utils.Const;
import com.siyecaoi.yy.utils.ImageUtils;
import com.siyecaoi.yy.utils.LogUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.sinata.xldutils.utils.StringUtils;

public class GiftShowDialogFragment extends DialogFragment {

    Unbinder unbinder;
    @BindView(R.id.mSVGAImageView_gift)
    SVGAImageView mSVGAImageViewGift;
    @BindView(R.id.iv_show_gift)
    SimpleDraweeView ivShowGift;
    @BindView(R.id.tv_show_gift)
    TextView tvShowGift;

    String imgShow;
    //    Context context;
    String msgShow;
    Timer timer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_giftshow, container);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        assert bundle != null;
        imgShow = bundle.getString(Const.ShowIntent.IMG);
        msgShow = bundle.getString(Const.ShowIntent.NUMBER);
        initShow();
    }

    private void initShow() {
        if (!StringUtils.isEmpty(msgShow)) {
            tvShowGift.setText(msgShow);
        }
        if (imgShow.endsWith(".svga")) {
            SVGAParser parser = new SVGAParser(Objects.requireNonNull(getContext()));
            try {
                parser.parse(new URL(imgShow), new SVGAParser.ParseCompletion() {
                    @Override
                    public void onComplete(SVGAVideoEntity svgaVideoEntity) {
                        SVGADrawable drawable = new SVGADrawable(svgaVideoEntity);
                        mSVGAImageViewGift.setImageDrawable(drawable);
                        mSVGAImageViewGift.startAnimation();
                    }

                    @Override
                    public void onError() {

                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            mSVGAImageViewGift.setCallback(new SVGACallback() {
                @Override
                public void onPause() {
                    LogUtils.e("svga", "onPause");
                }

                @Override
                public void onFinished() {
                    LogUtils.e("msg", "svga完成");
                    dismiss();
                }

                @Override
                public void onRepeat() {

                }

                @Override
                public void onStep(int i, double v) {

                }
            });
        } else if (imgShow.endsWith(".jpg") || imgShow.endsWith(".png") || imgShow.endsWith(".gif")) {
            ImageUtils.loadUri(ivShowGift, imgShow);
            if (timer != null) {
                timer.cancel();
            }
            timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    handler.sendEmptyMessage(101);
                }
            };
            timer.schedule(timerTask, 2000);
        } else {
            timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    handler.sendEmptyMessage(101);
                }
            };
            timer.schedule(timerTask, 2000);
        }

    }


    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 101:
                    if (!Objects.requireNonNull(getActivity()).isFinishing()) {
                        dismiss();
                    }
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((WindowManager.LayoutParams) params);
        super.onResume();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
