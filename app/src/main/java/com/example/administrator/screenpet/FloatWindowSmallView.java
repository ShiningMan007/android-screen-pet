package com.example.administrator.screenpet;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by Administrator on 2018/3/11/011.
 */

public class FloatWindowSmallView extends LinearLayout {
    public static int viewWidth;
    public static int viewHeight;
    private static int statusBarHeight;
    private WindowManager windowManager;
    private WindowManager.LayoutParams mParams;
    private float xInScreen;
    private float yInScreen;
    private float xDownInScreen;
    private float yDownInScreen;
    private float xInView;
    private float yInView;
    private GifImageView gifImageView;
    private TextView wechat_textview;

    public FloatWindowSmallView(Context context) {
        super(context);
        windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater.from(context).inflate(R.layout.float_window_small, this);
        View view = findViewById(R.id.small_window_layout);
        viewWidth = view.getLayoutParams().width;
        viewHeight = view.getLayoutParams().height;
        TextView percentView =(TextView) findViewById(R.id.percent);
        percentView.setText(MyWindowManager.getUsedPercentValue(context));
        gifImageView = (GifImageView)findViewById(R.id.float_small_pet);
        wechat_textview = (TextView)findViewById(R.id.wechat_textview);
        ArrayList<String> pathdir_sad = readfile(getContext(), CustomizeActivity.filedir_sad);
        ArrayList<String> pathdir_happy = readfile(getContext(), CustomizeActivity.filedir_happy);
        ArrayList<String> pathdir_all = pathdir_happy;
        for(int i=0; i< pathdir_sad.size(); i++){
            pathdir_all.add(pathdir_sad.get(i));
        }
        Random rand = new Random();
        try {
            GifDrawable gifDrawable = new GifDrawable(pathdir_all.get(rand.nextInt(pathdir_all.size())));
            gifImageView.setImageDrawable(gifDrawable);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    private static ArrayList<String> str2list(String str){
        String pattern = ",[\\s]*|\\[|\\]";
        String[] splits = str.split(pattern);
        ArrayList<String> res = new ArrayList<>();
        for(int i =1; i<splits.length; i++){
            res.add(splits[i]);
        }
        return res;
    }
    private static ArrayList<String> readfile(Context context, String readdir){
        ArrayList<String> res = new ArrayList<>();
        byte[] bytes = {};
        try{
            FileInputStream fis = context.openFileInput(readdir);
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
    public boolean onTouchEvent(MotionEvent event){
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                xInView = event.getX();
                yInView = event.getY();
                xDownInScreen = event.getRawX();
                yDownInScreen = event.getRawY() - getStatusBarHeight();
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
                break;
            case MotionEvent.ACTION_MOVE:
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
                updateViewPosition();
                break;
            case MotionEvent.ACTION_UP:
                if(xDownInScreen == xInScreen && yDownInScreen == yInScreen){
                    openBigWindow();
                }
                break;
            default:
                break;
        }
        return true;
    }

    public void setParams(WindowManager.LayoutParams params){
        mParams = params;
    }
    private void updateViewPosition(){
        mParams.x = (int) (xInScreen - xInView);
        mParams.y = (int) (yInScreen - yInView);
        windowManager.updateViewLayout(this, mParams);
    }
    public boolean updatePet(String str){
        try {
            GifDrawable gifDrawable = new GifDrawable(str);
            gifImageView.setImageDrawable(gifDrawable);
            gifImageView.invalidate();
        }catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public boolean updateMessage(String str){
        if(!str.equals("Ihateyouqzy")) {
            wechat_textview.setVisibility(VISIBLE);
            wechat_textview.setText(str);
            wechat_textview.invalidate();
            return true;
        }else{
            wechat_textview.setVisibility(INVISIBLE);
            wechat_textview.invalidate();
            return true;
        }
    }
    private void openBigWindow() {
        MyWindowManager.createBigWindow(getContext());
        MyWindowManager.removeSmallWindow(getContext());
    }
    private int getStatusBarHeight(){
        if(statusBarHeight == 0){
            try{
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (Integer) field.get(o);
                statusBarHeight = getResources().getDimensionPixelSize(x);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return statusBarHeight;
    }
}
