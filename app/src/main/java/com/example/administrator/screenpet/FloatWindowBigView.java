package com.example.administrator.screenpet;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Created by Administrator on 2018/3/11/011.
 */

public class FloatWindowBigView extends LinearLayout {
    public static int viewHeight;
    public static int viewWidth;

    public FloatWindowBigView(final Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.float_window_big, this);
        View view = findViewById(R.id.big_window_layout);
        viewWidth = view.getLayoutParams().width;
        viewHeight = view.getLayoutParams().height;
        Button close = (Button)findViewById(R.id.btn_close);
        Button back = (Button)findViewById(R.id.btn_back);
        Button main = (Button)findViewById(R.id.btn_main);
        Button bluetooth = (Button)findViewById(R.id.small_bluetooth);
        close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                MyWindowManager.removeBigWindow(context);
                MyWindowManager.removeSmallWindow(context);
                Intent intent = new Intent(getContext(), FloatWindowService.class);
                context.stopService(intent);
            }
        });

        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                MyWindowManager.removeBigWindow(context);
                MyWindowManager.createSmallWindow(context);
            }
        });

        main.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                MyWindowManager.removeBigWindow(context);
                MyWindowManager.createSmallWindow(context);
                Intent intent = new Intent(getContext(), MainSettingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        bluetooth.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                MyWindowManager.removeBigWindow(context);
                MyWindowManager.createSmallWindow(context);
                Intent intent = new Intent(getContext(),ScheduleActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }
}
