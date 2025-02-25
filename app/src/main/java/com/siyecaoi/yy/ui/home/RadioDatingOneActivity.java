package com.siyecaoi.yy.ui.home;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.siyecaoi.yy.R;
import com.siyecaoi.yy.base.MyApplication;
import com.siyecaoi.yy.base.MyBaseActivity;
import com.siyecaoi.yy.base.MyBaseFragment;
import com.siyecaoi.yy.bean.ChatRoomMsgBean;
import com.siyecaoi.yy.bean.GetOneBean;
import com.siyecaoi.yy.control.ChatRoomMessage;
import com.siyecaoi.yy.control.VirifyCountDownTimer;
import com.siyecaoi.yy.model.ChatRoomMsgModel;
import com.siyecaoi.yy.netUtls.Api;
import com.siyecaoi.yy.netUtls.HttpManager;
import com.siyecaoi.yy.netUtls.MyObserver;
import com.siyecaoi.yy.ui.find.adapter.RadioOneRecyAdapter;
import com.siyecaoi.yy.ui.message.OtherHomeActivity;
import com.siyecaoi.yy.ui.mine.PersonHomeActivity;
import com.siyecaoi.yy.utils.ActivityCollector;
import com.siyecaoi.yy.utils.Const;
import com.siyecaoi.yy.utils.LogUtils;
import com.siyecaoi.yy.utils.MyUtils;
import com.siyecaoi.yy.utils.SharedPreferenceUtils;
import com.tencent.imsdk.TIMCallBack;
import com.tencent.imsdk.TIMConversation;
import com.tencent.imsdk.TIMConversationType;
import com.tencent.imsdk.TIMCustomElem;
import com.tencent.imsdk.TIMGroupManager;
import com.tencent.imsdk.TIMManager;
import com.tencent.imsdk.TIMMessage;
import com.tencent.imsdk.TIMMessageOfflinePushSettings;
import com.tencent.imsdk.TIMValueCallBack;
import com.tencent.qcloud.tim.uikit.TUIKit;
import com.tencent.qcloud.tim.uikit.base.IUIKitCallBack;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cn.sinata.xldutils.utils.StringUtils;

public class RadioDatingOneActivity extends MyBaseActivity {


    @BindView(R.id.mRecyclerView_radiodating)
    RecyclerView mRecyclerViewRadiodating;
    @BindView(R.id.tv_newdata_radiodating)
    TextView tvNewdataRadiodating;
    @BindView(R.id.edt_input_radiodating)
    EditText edtInputRadiodating;
    @BindView(R.id.btn_send_radiodating)
    Button btnSendRadiodating;
    @BindView(R.id.ll_chat_radiodating)
    LinearLayout llChatRadiodating;


    @BindView(R.id.root_view)
    RelativeLayout root_view;

    Unbinder unbinder;

    List<TIMMessage> msgs;
    RadioOneRecyAdapter radioOneRecyAdapter;

    VirifyCountDownTimer virifyCountDownTimer;

    List<String> noShowString;//敏感词汇

    TIMConversation timConversation;



    @Override
    public void initData() {

    }


    @Override
    public void setContentView() {
        setContentView(R.layout.fragment_radiodatingone);
    }

    @Override
    public void initView() {
        setTitleText("公聊大厅");
        root_view.setBackgroundColor(getResources().getColor(R.color.nav_noselct_color));

        msgs = new ArrayList<>();
        setRecycler();

        if (Const.msgSendTime != 0) { //判断是否可以发送消息
            long timeEnd = System.currentTimeMillis() - Const.msgSendTime;
            if (timeEnd > 60000) {
                Const.msgSendTime = 0;
            } else {
                setEditShow(60000 - timeEnd);
            }
        }
        addOnMessageCall();
        getCall();
        try {
            noShowString = readFile02("ReservedWords-utf8.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> readFile02(String path) throws IOException {
        // 使用一个字符串集合来存储文本中的路径 ，也可用String []数组
        List<String> list = new ArrayList<String>();
//        FileInputStream fis = new FileInputStream(path);
        InputStream in = getResources().getAssets().open(path);
        // 防止路径乱码   如果utf-8 乱码  改GBK     eclipse里创建的txt  用UTF-8，在电脑上自己创建的txt  用GBK
        InputStreamReader isr = new InputStreamReader(in, "UTF-8");
        BufferedReader br = new BufferedReader(isr);
        String line = "";
        while ((line = br.readLine()) != null) {
            // 如果 t x t文件里的路径 不包含---字符串       这里是对里面的内容进行一个筛选
            if (line.lastIndexOf("---") < 0) {
                list.add(line);
            }
        }
        br.close();
        isr.close();
        in.close();
        return list;
    }

    private void getCall() {
        HashMap<String, Object> map = HttpManager.getInstance().getMap();
        HttpManager.getInstance().post(Api.BroadList, map, new MyObserver(this) {
            @Override
            public void success(String responseString) {
                ChatRoomMsgBean chatRoomMsgBean = JSON.parseObject(responseString, ChatRoomMsgBean.class);
                if (chatRoomMsgBean.getCode() == Api.SUCCESS) {
                    List<ChatRoomMsgModel.DataBean> data = chatRoomMsgBean.getData();
                    Collections.reverse(data);//对列表倒叙
                    if (radioOneRecyAdapter != null) {
                        radioOneRecyAdapter.setNewData(data);
                        if (mRecyclerViewRadiodating != null) {
                            mRecyclerViewRadiodating.scrollToPosition(radioOneRecyAdapter.getItemCount() - 1);
                        }
                    }

                } else {
                    showToast(chatRoomMsgBean.getMsg());
                }
            }
        });
    }

    /**
     * 更新页面显示
     *
     * @param millisInfuture 倒计时毫秒值
     */
    private void setEditShow(long millisInfuture) {

        edtInputRadiodating.setText("");
        MyUtils.getInstans().hideKeyboard(edtInputRadiodating);
        virifyCountDownTimer =
                new VirifyCountDownTimer(btnSendRadiodating, getString(R.string.tv_send),
                        millisInfuture, 1000, timerFinish);
        virifyCountDownTimer.start();
        btnSendRadiodating.setAlpha(0.5f);
    }

    VirifyCountDownTimer.TimerFinish timerFinish = new VirifyCountDownTimer.TimerFinish() {
        @Override
        public void onFinish() {
            btnSendRadiodating.setAlpha(1.0f);
        }
    };

    private void addOnMessageCall() {
//        TIMGroupManager.CreateGroupParam createGroupParam = new TIMGroupManager.CreateGroupParam("AVChatRoom", "广播交友");
//        TIMGroupManager.getInstance().createGroup(createGroupParam, new TIMValueCallBack<String>() {
//            @Override
//            public void onError(int i, String s) {
//                LogUtils.e(i+s);
//            }
//
//            @Override
//            public void onSuccess(String s) {
//                LogUtils.e(s+"广播交友");
//            }
//        });
        TIMGroupManager.getInstance().applyJoinGroup(Const.chatRoom, "", new TIMCallBack() {
            @Override
            public void onError(int i, String s) {
                if (!isFinishing()) {
                    LogUtils.e(i + s);
                    //重新登录
                    if (i == 6014 && StringUtils.isEmpty(TIMManager.getInstance().getLoginUser())) {
                        String userSig = (String) SharedPreferenceUtils.get(mContext, Const.User.USER_SIG, "");
                        onRecvUserSig(userToken + "", userSig);
                    } else {
                        showToast("进入聊天室失败，请稍后再试");
                    }
                }
            }

            @Override
            public void onSuccess() {
                LogUtils.e("加入聊天室成功");
                setMegReaded();
            }
        });
        MyApplication.getInstance().addChatRoomMessage(chatRoomMessage);
    }

    private void setMegReaded() {
        timConversation = TIMManager.getInstance().getConversation(
                TIMConversationType.Group, Const.chatRoom);
        timConversation.setReadMessage(timConversation.getLastMsg(),null);
    }

    private void onRecvUserSig(String userId, String userSig) {
        TUIKit.login(userId, userSig, new IUIKitCallBack() {
            @Override
            public void onSuccess(Object data) {
                /**
                 * IM 登录成功后的回调操作，一般为跳转到应用的主页（这里的主页内容为下面章节的会话列表）
                 */
                LogUtils.e(LogUtils.TAG, "登录腾讯云成功");
                addOnMessageCall();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                LogUtils.e(LogUtils.TAG, errCode + "登录腾讯云失败" + errMsg);
            }
        });
    }

    ChatRoomMessage chatRoomMessage = new ChatRoomMessage() {
        @Override
        public void onNewMessage(TIMMessage timMsg) {
            msgs.add(timMsg);
            if (timMsg.getElement(0) instanceof TIMCustomElem) {
                JSONObject jsonObject = null;
                try {
                    ChatRoomMsgModel chatRoomMsgModel = new Gson().fromJson(new String(((TIMCustomElem) timMsg.getElement(0)).getData(),
                            "UTF-8"), ChatRoomMsgModel.class);
                    if (chatRoomMsgModel.getState() == 3) {
                        radioOneRecyAdapter.addData(chatRoomMsgModel.getData());

                        if (mRecyclerViewRadiodating != null && mRecyclerViewRadiodating.canScrollVertically(1)) {
                            tvNewdataRadiodating.setVisibility(View.VISIBLE);
                        } else {
                            mRecyclerViewRadiodating.scrollToPosition(radioOneRecyAdapter.getItemCount() - 1);
                        }
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void setRecycler() {
        radioOneRecyAdapter = new RadioOneRecyAdapter(R.layout.item_list_radioda);
        radioOneRecyAdapter.setUserId(userToken);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        mRecyclerViewRadiodating.setLayoutManager(layoutManager);
        mRecyclerViewRadiodating.setAdapter(radioOneRecyAdapter);

        radioOneRecyAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                switch (view.getId()) {
                    case R.id.iv_header_radioda:
                        ChatRoomMsgModel.DataBean dataBean = (ChatRoomMsgModel.DataBean) adapter.getItem(position);
                        Bundle bundle = new Bundle();
                        assert dataBean != null;
                        bundle.putInt(Const.ShowIntent.ID, dataBean.getUid());
                        if (dataBean.getUid() == userToken) {
                            ActivityCollector.getActivityCollector().toOtherActivity(PersonHomeActivity.class, bundle);
                        } else {
                            ActivityCollector.getActivityCollector().toOtherActivity(OtherHomeActivity.class, bundle);
                        }
                        break;
                }
            }
        });

        mRecyclerViewRadiodating.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int lastPosition = -1;
                //当前状态为停止滑动状态SCROLL_STATE_IDLE时
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                    ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                }
                //时判断界面显示的最后item的position是否等于itemCount总数-1也就是最后一个item的position
                //如果相等则说明已经滑动到最后了
                if (lastPosition >= recyclerView.getLayoutManager().getItemCount() - 1) {
                    tvNewdataRadiodating.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void setResume() {

    }

    @Override
    public void setOnclick() {

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (virifyCountDownTimer != null) {
            virifyCountDownTimer.cancel();
        }
        MyApplication.getInstance().delChatRoomMessage(chatRoomMessage);
    }



    @OnClick({R.id.tv_newdata_radiodating, R.id.btn_send_radiodating})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_newdata_radiodating:
                tvNewdataRadiodating.setVisibility(View.GONE);
                mRecyclerViewRadiodating.scrollToPosition(radioOneRecyAdapter.getItemCount() - 1);
                break;
            case R.id.btn_send_radiodating:
                String mesShow = edtInputRadiodating.getText().toString();
                if (StringUtils.isEmpty(mesShow)) {
                    showToast("请输入聊天内容");
                    return;
                }
                if (noShowString != null) {
                    for (String noShow : noShowString) {
                        if (noShow.equals(mesShow)) {
                            showToast("您输入的内容带有敏感词汇");
                            return;
                        }
                    }
                }
                btnSendRadiodating.setClickable(false);
                getMegCall(mesShow);
                break;
        }
    }

    private void getMegCall(String mesShow) {
        HashMap<String, Object> map = HttpManager.getInstance().getMap();
        map.put("uid", userToken);
        map.put("content", mesShow);
        HttpManager.getInstance().post(Api.Broad, map, new MyObserver(this) {
            @Override
            public void success(String responseString) {
                GetOneBean baseBean = JSON.parseObject(responseString, GetOneBean.class);
                if (baseBean.getCode() == Api.SUCCESS) {
                    if (baseBean.getData().getState() == 1) {
                        btnSendRadiodating.setClickable(true);
                        showToast("发广播需要在房间的麦上哦~");
                    } else if (baseBean.getData().getState() == 2) {
                        Const.msgSendTime = System.currentTimeMillis();
                        setEditShow(60000);
                        setMesgSend(mesShow);
                    }
                } else {
                    showToast(baseBean.getMsg());
                    btnSendRadiodating.setClickable(true);
                }
            }
        });
    }

    private void setMesgSend(String mesShow) {
        TIMConversation conversation = TIMManager.getInstance().getConversation(
                TIMConversationType.Group,    //会话类型：群聊
                Const.chatRoom);

        /**
         * 构建消息体
         */
        int gradeShow = (int) SharedPreferenceUtils.get(mContext, Const.User.GRADE_T, 0);
        int charmGrade = (int) SharedPreferenceUtils.get(mContext, Const.User.CharmGrade, 0);
        int sex = (int) SharedPreferenceUtils.get(mContext, Const.User.SEX, 0);
        String name = (String) SharedPreferenceUtils.get(mContext, Const.User.NICKNAME, "");
        String header = (String) SharedPreferenceUtils.get(mContext, Const.User.IMG, "");
        ChatRoomMsgModel.DataBean dataBean = new ChatRoomMsgModel.DataBean();
        dataBean.setGrade(gradeShow);
        dataBean.setCharm(charmGrade);
        dataBean.setUid(userToken);
        dataBean.setMessageShow(mesShow);
        dataBean.setName(name);
        dataBean.setSex(sex);
        dataBean.setHeader(header);
        ChatRoomMsgModel chatRoomMsgModel = new ChatRoomMsgModel(113, dataBean, 3);

        //构造一条消息
        TIMMessage msg = new TIMMessage();
        TIMMessageOfflinePushSettings timMessageOfflinePushSettings = new TIMMessageOfflinePushSettings();
        timMessageOfflinePushSettings.setDescr("[广播消息]");
        msg.setOfflinePushSettings(timMessageOfflinePushSettings);
        String msgShow = JSON.toJSONString(chatRoomMsgModel);
        //向 TIMMessage 中添加自定义内容
        TIMCustomElem elem = new TIMCustomElem();
        elem.setData(msgShow.getBytes());      //自定义 byte[]
        elem.setDesc("[广播消息]"); //自定义描述信息
        //将 elem 添加到消息
        if (msg.addElement(elem) != 0) {
            LogUtils.d(LogUtils.TAG, "addElement failed");
            return;
        }
//        MessageInfo messageInfo = new MessageInfo();
//        messageInfo.setTIMMessage(msg);
//        messageInfo.setMsgType(MessageInfo.MSG_TYPE_SHARE);
//        chatPanel.sendMessage(messageInfo);

        conversation.sendMessage(msg, new TIMValueCallBack<TIMMessage>() {
            @Override
            public void onError(int i, String s) {
                //错误码 code 和错误描述 desc，可用于定位请求失败原因
                //错误码 code 含义请参见错误码表
                LogUtils.e(i + s);
                showToast("消息发送失败" + s);
            }

            @Override
            public void onSuccess(TIMMessage timMessage) { //发送消息成功
                radioOneRecyAdapter.addData(dataBean);
                mRecyclerViewRadiodating.scrollToPosition(radioOneRecyAdapter.getItemCount() - 1);
            }
        });
    }
}
