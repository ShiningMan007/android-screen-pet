package com.example.administrator.screenpet;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

public class ClockActivity extends AppCompatActivity{

    AlarmManager alarmManager;
    TimePicker timePicker;
    TextView update_textview;
    Button turn_off_btn;
    Button turn_on_btn;
    Context context;
    Intent Alarm_intent;
    PendingIntent pendingIntent;
    Spinner ringtone_spinner;
    long music_id_selected = 1;
    EditText mEdittext;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock);

        this.context = this;
        // initialize our timepicker
        timePicker = (TimePicker)findViewById(R.id.timepicker);
        // initialize our alarm manager
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        // initlialize
        update_textview = (TextView)findViewById(R.id.textview_update);
        // initialize button
        turn_off_btn = (Button)findViewById(R.id.btn_turn_off);
        turn_on_btn = (Button)findViewById(R.id.btn_turn_on);

        final Calendar calendar = Calendar.getInstance();
        Alarm_intent = new Intent(this.context, AlarmReceiver.class);
        mEdittext = (EditText) findViewById(R.id.et_clock_schedule_content);

        turn_on_btn = (Button)findViewById(R.id.btn_turn_on);
        turn_on_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int version = Build.VERSION.SDK_INT;
                int hour, minute;
                String hour_string, minute_string;
                if (version >= Build.VERSION_CODES.M) {
                    calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());

                    calendar.set(Calendar.MINUTE, timePicker.getMinute());

                    hour = timePicker.getHour();
                    minute = timePicker.getMinute();
                }
                else{
                    hour = timePicker.getCurrentHour();
                    minute = timePicker.getCurrentMinute();
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                }
                hour_string = String.valueOf(hour);
                minute_string = String.valueOf(minute);
                String AMPM = "AM";
                if(hour > 12) {
                    hour = hour - 12;
                    hour_string = String.valueOf(hour);
                    AMPM = "PM";
                }
                if(minute < 10){
                    minute_string = "0"+String.valueOf(minute);
                }
                set_alarm_text("Alarm set to: "+hour_string + ":" + minute_string+" "+AMPM);

                Alarm_intent.putExtra("extra", true);
                Alarm_intent.putExtra("music_id", music_id_selected);
                Alarm_intent.putExtra("content", mEdittext.getText().toString());

                pendingIntent = PendingIntent.getBroadcast(context, 0, Alarm_intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                long a = calendar.getTimeInMillis();
                alarmManager.set(AlarmManager.RTC_WAKEUP, a, pendingIntent);

                if(shouldBack){
                    Intent return_intent = new Intent(ClockActivity.this, ScheduleActivity.class);
                    return_intent.putExtra("OnOrOff", true);
                    String content_str = (String)mEdittext.getText().toString();
                    return_intent.putExtra("Content", content_str);
                    String time_str = hour_string +":"+ minute_string + " "+AMPM;
                    return_intent.putExtra("Time", time_str);
                    return_intent.putExtra("Position", position);
                    setResult(RESULT_OK, return_intent);
                    shouldBack = false;
                    finish();
                }
            }
        });
        turn_off_btn = (Button)findViewById(R.id.btn_turn_off);
        turn_off_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( pendingIntent != null){
                    set_alarm_text("Alarm off");
                    alarmManager.cancel(pendingIntent);
                    Alarm_intent.putExtra("extra", false);
                    Alarm_intent.putExtra("music_id", music_id_selected);
                    Alarm_intent.putExtra("content", schedule_content);
                    sendBroadcast(Alarm_intent);
                }

                if(shouldBack){
                    Intent return_intent = new Intent(ClockActivity.this, ScheduleActivity.class);
                    return_intent.putExtra("OnOrOff", false);
                    String content_str = (String)mEdittext.getText().toString();
                    return_intent.putExtra("Content", content_str);
                    String time_str = "My Pleasure Time";
                    return_intent.putExtra("Time", time_str);
                    return_intent.putExtra("Position", position);
                    setResult(RESULT_CANCELED, return_intent);
                    shouldBack = false;
                    finish();
                }
            }
        });

        ringtone_spinner = (Spinner)findViewById(R.id.spinner_choose);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.clock_ringtone,
                android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ringtone_spinner.setAdapter(adapter);
        ringtone_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                music_id_selected = id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Intent intent = getIntent();
        position = intent.getIntExtra("Position", -1);
        if(position != -1){
            shouldBack = true;
            schedule_content = intent.getStringExtra("Content");
            schedule_time = intent.getStringExtra("Time");
            mEdittext.setText(schedule_content);
            shouldBack = true;
        }else{
            schedule_content = "";
        }
    }
    int position = -1;
    private boolean shouldBack = false;
    private String schedule_content;
    private String schedule_time = "My Pleasure Time";
    private void set_alarm_text(String text){
        update_textview.setText(text);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == ScheduleActivity.REQUEST_SETTING_TIME){
            String schedule_content = data.getStringExtra("Content");
            String time = data.getStringExtra("Time");
            mEdittext.setText(schedule_content + time);
            shouldBack = true;

        }

    }
}
