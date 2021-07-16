package com.xylink.sdk.sample.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xylink.sdk.sample.R;
import com.xylink.sdk.sample.bean.MeettingInfoData;

import java.util.List;

public class MeetingNameAdapter extends BaseAdapter {
    List<MeettingInfoData> meetList;
    Context context;
    public MeetingNameAdapter(Context context, List<MeettingInfoData> meetList){
        this.context = context;
        this.meetList = meetList;
    }
    @Override
    public int getCount() {
        return meetList.size();
    }

    @Override
    public Object getItem(int i) {
        return meetList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        convertView = View.inflate(context, R.layout.item_meeting_detail,null);
        TextView tv_name = convertView.findViewById(R.id.tv_name);
        TextView tv_num = convertView.findViewById(R.id.tv_num);
        TextView tv_order = convertView.findViewById(R.id.tv_order);
        tv_name.setText(meetList.get(position).getMeettingName());
        tv_num.setText(meetList.get(position).getMeettingNum());
        tv_order.setText(position+1+"");
        return convertView;
    }
}
