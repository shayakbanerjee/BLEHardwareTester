package com.weartrons.hammerheaddevicetest;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.app.Activity;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by shayak on 10/3/14.
 */
public class DeviceTestAdapter extends ArrayAdapter<SingleTest> {
    private Context context;
    private int layoutResourceId;
    private List<SingleTest> deviceTests = null;

    public DeviceTestAdapter(Context c, int resId, List<SingleTest> input) {
        super(c, resId, input);
        this.layoutResourceId = resId;
        this.context = c;
        this.deviceTests = input;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        DeviceTestHolder holder = null;

        if(row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new DeviceTestHolder();
            holder.passIcon = (ImageView)row.findViewById(R.id.passIndicator);
            holder.failIcon = (ImageView)row.findViewById(R.id.failIndicator);
            holder.failCountView = (TextView)row.findViewById(R.id.passFailCount);
            holder.testNameView = (TextView)row.findViewById(R.id.deviceTestName);
            row.setTag(holder);
        } else {
            holder = (DeviceTestHolder)row.getTag();
        }

        SingleTest deviceTest = deviceTests.get(position);
        holder.testNameView.setText(deviceTest.getTestName());
        if(!deviceTest.didTestRun()) {
            holder.passIcon.setImageResource(R.drawable.unknownpass);
            holder.failIcon.setImageResource(R.drawable.unknownfail);
        } else if (deviceTest.didTestPass()) {
            holder.passIcon.setImageResource(R.drawable.greenpass);
        } else {
            holder.failIcon.setImageResource(R.drawable.redfail);
        }
        holder.failCountView.setText(Integer.toString(
                DeviceTestHistory.getFailCount(deviceTest.getTestName())));
        return row;
    }

    static class DeviceTestHolder
    {
        ImageView passIcon;
        ImageView failIcon;
        TextView failCountView;
        TextView testNameView;
    }
}
