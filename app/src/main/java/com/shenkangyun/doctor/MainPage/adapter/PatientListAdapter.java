package com.shenkangyun.doctor.MainPage.adapter;

import android.text.TextUtils;
import android.util.Log;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.shenkangyun.doctor.BeanFolder.PatientBean;
import com.shenkangyun.doctor.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2018/7/31.
 */

public class PatientListAdapter extends BaseQuickAdapter<PatientBean.DataBean.ListBean, BaseViewHolder> {

    public PatientListAdapter() {
        super(R.layout.item_patient, null);
    }

    @Override
    protected void convert(BaseViewHolder helper, PatientBean.DataBean.ListBean item) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        if (TextUtils.isEmpty(item.getName()) || "null".equals(item.getName())) {
            helper.setText(R.id.hz_name, item.getLoginName());
        } else {
            helper.setText(R.id.hz_name, item.getName());
        }
        if (TextUtils.isEmpty(String.valueOf(item.getUpdateTime())) || "0".equals(String.valueOf(item.getUpdateTime()))) {
            Date date = new Date(item.getCreateTime());
            String format = dateFormat.format(date);
            helper.setText(R.id.hz_time, format);
        } else {
            Date date = new Date(item.getUpdateTime());
            String format = dateFormat.format(date);
            helper.setText(R.id.hz_time, format);
        }

        helper.addOnClickListener(R.id.content);
        helper.addOnClickListener(R.id.right);
    }
}
