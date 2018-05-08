package com.example.administrator.screenpet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ScheduleItemAdapter extends BaseAdapter {
    private LayoutInflater mlayoutInflater;
    private ArrayList<ScheduleItem> mArrayList;
    Context mContext;

    public void update(int position){
        boolean old = ScheduleActivity.mArrayList.get(position).isSchedule_on_off();
        ScheduleActivity.mArrayList.get(position).setSchedule_on_off(!old);
    }

    public ScheduleItemAdapter(Context context, ArrayList<ScheduleItem> arrayList){
        mContext = context;
        mlayoutInflater = LayoutInflater.from(mContext);
        mArrayList = arrayList;
    }

    @Override
    public int getCount() {
        return mArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return mArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ScheduleViewHolder holder = null;
        if(convertView == null){
            convertView = mlayoutInflater.inflate(R.layout.schedule_item, parent, false);
            holder = new ScheduleViewHolder();

            holder.tv_schedule_content =(TextView) convertView.findViewById(R.id.tv_schedule_content);
            holder.tv_schedule_time =(TextView) convertView.findViewById(R.id.tv_schedule_time);
            holder.btn_on_off = (Button)convertView.findViewById(R.id.btn_schedule);

            convertView.setTag(holder);
        }else{
            holder = (ScheduleViewHolder) convertView.getTag();
        }
        final ScheduleItem item = mArrayList.get(position);

        holder.tv_schedule_content.setText(item.getSchedule_content());
        holder.tv_schedule_time.setText(item.getSchedule_time());
        if(item.isSchedule_on_off()){
            holder.btn_on_off.setText("ON");
            holder.btn_on_off.setTextColor(mContext.getResources().getColor(R.color.blue));
        }
        else{
            holder.btn_on_off.setText("OFF");
            holder.btn_on_off.setTextColor(mContext.getResources().getColor(R.color.white));
        }

        holder.btn_on_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button btn = (Button)v;
                String oldstr = (String)btn.getText();
                if(oldstr == "OFF") {
                    oldstr = "ON";
                    btn.setTextColor(mContext.getResources().getColor(R.color.blue));
                    synchronization(position);
                }else{
                    oldstr = "OFF";
                    btn.setTextColor(mContext.getResources().getColor(R.color.white));
                    synchronization(position);
                }
                btn.setText(oldstr);
            }
        });

        return convertView;
    }

    private class ScheduleViewHolder{
        TextView tv_schedule_content;
        TextView tv_schedule_time;
        Button btn_on_off;
        boolean onOrOff;
    }

    private void synchronization(int position){
        ArrayList<String> on_off_list = readfile(ScheduleActivity.schedule_on_off_dir);
        String item = on_off_list.get(position);
        if(item.equals("false")){
            on_off_list.remove(position);
            if(position != 0){
                on_off_list.add(position -1, "true");
            }else{
                on_off_list.add(0, "true");
            }
        }else{
            on_off_list.remove(position);
            if(position != 0){
                on_off_list.add(position -1, "false");
            }else{
                on_off_list.add(0, "false");
            }
        }
        update(position);
        boolean res = writefile(ScheduleActivity.schedule_on_off_dir, on_off_list.toString(), mContext.MODE_PRIVATE);
    }
    private boolean writefile(String writedir, String content ,int mode){
        try {
            FileOutputStream fos = mContext.openFileOutput(writedir, mode);
            fos.write(content.getBytes());
            fos.close();
        }catch (FileNotFoundException e){
            e.printStackTrace();
            return false;
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
    ArrayList<String> str2list(String str){
        String pattern = ",[\\s]*|\\[|\\]";
        String[] splits = str.split(pattern);
        ArrayList<String> res = new ArrayList<>();
        for(int i =1; i<splits.length; i++){
            res.add(splits[i]);
        }
        return res;
    }
    private ArrayList<String> readfile(String readdir){
        ArrayList<String> res = new ArrayList<>();
        byte[] bytes = {};
        try{
            FileInputStream fis = mContext.openFileInput(readdir);
            bytes = new byte[fis.available()];
            fis.read(bytes);
            String pathstr = new String(bytes);
            res = str2list(pathstr);
        }catch (FileNotFoundException e){
            e.printStackTrace();
            return res;
        }catch (IOException e){
            e.printStackTrace();
            return res;
        }
        return res;
    }
}
