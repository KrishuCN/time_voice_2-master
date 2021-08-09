package com.siyecaoi.yy.ui.mine.adapter;

import android.annotation.SuppressLint;

import androidx.core.content.ContextCompat;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.siyecaoi.yy.R;
import com.siyecaoi.yy.bean.DrawHisBean;
import com.siyecaoi.yy.utils.MyUtils;

public class DrawHisItemAdapter extends BaseQuickAdapter<DrawHisBean.DataEntity.WithdrawEntity, BaseViewHolder> {


    public DrawHisItemAdapter(int layoutResId) {
        super(layoutResId);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void convert(BaseViewHolder helper, DrawHisBean.DataEntity.WithdrawEntity item) {
////        TextView tv_name_drawhis = helper.getView(R.id.tv_name_drawhis);
//        if (item.getState() == 1) {
//            helper.setText(R.id.tv_name_drawhis, "钻石提现 ¥" + MyUtils.getInstans().doubleTwo(item.getMoney()) + "元");
//        } else if (item.getState() == 2) {
//            helper.setText(R.id.tv_name_drawhis, "分成提现 ¥" + MyUtils.getInstans().doubleTwo(item.getMoney()) + "元");
//        }
////        Typeface typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/impact.ttf");
////        tv_name_drawhis.setTypeface(typeface);
//        helper.setText(R.id.tv_time_drawhis, MyUtils.getInstans().showTimeHHmmss(MyUtils.getInstans().getLongTime(item.getCreateTime())));
//
        TextView tv_type_drawhis = helper.getView(R.id.tv_type_drawhis);
//        TextView tv_number_drawhis = helper.getView(R.id.tv_number_drawhis);
//        tv_number_drawhis.setText("提现账号：" + item.getPhone());
        helper.setText(R.id.tv_number_drawhis, "提现账号：" + item.getPhone());
////        tv_number_drawhis.setTextColor(ContextCompat.getColor(mContext, R.color.white6));
        if (item.getStatus() == 1) {
            tv_type_drawhis.setText("提现中");
            tv_type_drawhis.setTextColor(ContextCompat.getColor(mContext, R.color.tx_color));
        } else if (item.getStatus() == 2) {
            tv_type_drawhis.setText("已完成");
            tv_type_drawhis.setTextColor(ContextCompat.getColor(mContext, R.color.white4));
        } else if (item.getStatus() == 3) {
            tv_type_drawhis.setText("已驳回");
            tv_type_drawhis.setTextColor(ContextCompat.getColor(mContext, R.color.white4));
        }


        helper.setText(R.id.tv_name_drawhis, "-"+MyUtils.getInstans().doubleTwo(item.getMoney()));
        helper.setText(R.id.tv_time_drawhis, MyUtils.getInstans().showTimeYMDHM(MyUtils.getInstans().getLongTime(item.getCreateTime())));
    }
}
