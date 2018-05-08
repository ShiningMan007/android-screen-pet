package com.example.administrator.screenpet;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ScheduleActivity extends AppCompatActivity {
    public static final int REQUEST_SETTING_TIME = 1;
    private ListView mSchedule_listview;
    public static ArrayList<ScheduleItem> mArrayList;
    private ScheduleItemAdapter mAdapter;
    private Button mAddScheduleBtn;

    private boolean writefile(String writedir, String content ,int mode){
        try {
            FileOutputStream fos = openFileOutput(writedir, mode);
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
            FileInputStream fis = openFileInput(readdir);
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

    public static final String schedule_name_dir = "schedule_name.data";
    public static final String schedule_time_dir = "schedule_time.data";
    public static final String schedule_on_off_dir = "schedule_on_off.data";

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_layout);

        mArrayList = new ArrayList<>();
        mSchedule_listview = (ListView)findViewById(R.id.lv_schedule);
        ScheduleItem item1 = new ScheduleItem("Go to Eat", "Tonight", true);
        ScheduleItem item2 = new ScheduleItem("Go to Shop", "Afternoon", true);
        ScheduleItem item3 = new ScheduleItem("Study", "Afternoon", true);
        ScheduleItem item4 = new ScheduleItem("Make a file", "16:30", false);
        mArrayList.add(item1);
        mArrayList.add(item2);
        mArrayList.add(item3);
        mArrayList.add(item4);

        // Try to restore data
        ArrayList<String> schedule_name_list = readfile(schedule_name_dir);
        ArrayList<String> schedule_time_list = readfile(schedule_time_dir);
        ArrayList<String> schedule_on_off_list = readfile(schedule_on_off_dir);
        ArrayList<ScheduleItem> new_arrayList;
        if(schedule_name_list.size() != 0 && schedule_time_list.size() !=0 && schedule_on_off_list.size() != 0){
            new_arrayList = new ArrayList<>();
            for(int i=0; i<schedule_name_list.size(); i++){
                boolean kk = schedule_on_off_list.get(i).equals("true");
                ScheduleItem item = new ScheduleItem(schedule_name_list.get(i), schedule_time_list.get(i), kk);
                new_arrayList.add(item);
            }
            mArrayList = new_arrayList;
        }

        synchonization(mArrayList);

        mAdapter = new ScheduleItemAdapter(this, mArrayList);
        mSchedule_listview.setAdapter(mAdapter);
        mAddScheduleBtn = (Button) findViewById(R.id.btn_add_schedule);

        mAddScheduleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // add new item in the front of the list
                ScheduleItem item = new ScheduleItem("Click to Config", "My Pleasure Time", false);
                mArrayList.add(0, item);
                mAdapter.notifyDataSetChanged();
            }
        });

        mSchedule_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getBaseContext(), "Click item "+position, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ScheduleActivity.this, ClockActivity.class);
                intent.putExtra("Content", mArrayList.get(position).getSchedule_content());
                intent.putExtra("Time", mArrayList.get(position).getSchedule_time());
                intent.putExtra("Position", position);
                startActivityForResult(intent, REQUEST_SETTING_TIME);
            }
        });

        mSchedule_listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                mArrayList.remove(position);
                synchonization(mArrayList);
                mAdapter.notifyDataSetChanged();
                Toast.makeText(getBaseContext(),"You have removed this item.", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_SETTING_TIME){
            String content = data.getStringExtra("Content");
            String time = data.getStringExtra("Time");
            boolean onOrOff = data.getBooleanExtra("OnOrOff", false);
            int position = data.getIntExtra("Position", -1);
            if(position != -1) {
                if (resultCode == RESULT_OK) {
                    mArrayList.get(position).setSchedule_on_off(onOrOff);
                    mArrayList.get(position).setSchedule_content(content);
                    mArrayList.get(position).setSchedule_time(time);
                    mAdapter.notifyDataSetChanged();

                    synchonization(mArrayList);

                } else if (resultCode == RESULT_CANCELED) {
                    mArrayList.get(position).setSchedule_on_off(onOrOff);
                    mArrayList.get(position).setSchedule_content(content);
                    mArrayList.get(position).setSchedule_time(time);
                    mAdapter.notifyDataSetChanged();

                    synchonization(mArrayList);
                }
            }
        }else{

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        synchonization(mArrayList);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        synchonization(mArrayList);
    }

    private void synchonization(ArrayList<ScheduleItem> list){
        ArrayList<String> schedule_name_list = new ArrayList<>();
        ArrayList<String> schedule_time_list = new ArrayList<>();
        ArrayList<String> schedule_on_off_list = new ArrayList<>();

        if(list.size() != 0){
            for(int i=0; i<list.size(); i++){
                schedule_name_list.add(list.get(i).getSchedule_content());
                schedule_time_list.add(list.get(i).getSchedule_time());
                schedule_on_off_list.add(list.get(i).isSchedule_on_off()? "true": "false");
            }
            writefile(schedule_name_dir, schedule_name_list.toString(), MODE_PRIVATE);
            writefile(schedule_time_dir, schedule_time_list.toString(), MODE_PRIVATE);
            writefile(schedule_on_off_dir, schedule_on_off_list.toString(), MODE_PRIVATE);
        }
    }
}
