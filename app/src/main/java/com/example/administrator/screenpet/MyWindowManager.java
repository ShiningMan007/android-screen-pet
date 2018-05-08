package com.example.administrator.screenpet;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by Administrator on 2018/3/11/011.
 */

public class MyWindowManager {
    private static FloatWindowSmallView smallWindow;
    private static FloatWindowBigView bigWindow;
    private static WindowManager.LayoutParams smallWindowParams;
    private static WindowManager.LayoutParams bigWindowParams;
    private static WindowManager mWindowManager;
    private static ActivityManager mActivityManager;

    public static void createSmallWindow(Context context){
        WindowManager windowManager = getWindowManager(context);
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();
        if(smallWindow == null){
            smallWindow = new FloatWindowSmallView(context);
            if(smallWindowParams == null){
                smallWindowParams = new WindowManager.LayoutParams();
                smallWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
                smallWindowParams.format = PixelFormat.RGBA_8888;
                smallWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                smallWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
                smallWindowParams.width = FloatWindowSmallView.viewWidth;
                smallWindowParams.height = FloatWindowSmallView.viewHeight;
                smallWindowParams.x = screenWidth;
                smallWindowParams.y = screenHeight / 2;
            }
        }
        smallWindow.setParams(smallWindowParams);
        windowManager.addView(smallWindow, smallWindowParams);
    }

    public static void removeSmallWindow(Context context) {
        if (smallWindow != null) {
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(smallWindow);
            smallWindow = null;
        }
    }

    public static void createBigWindow(Context context) {
        WindowManager windowManager = getWindowManager(context);
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();
        if (bigWindow == null) {
            bigWindow = new FloatWindowBigView(context);
            if (bigWindowParams == null) {
                bigWindowParams = new WindowManager.LayoutParams();
                bigWindowParams.x = screenWidth / 2 - FloatWindowBigView.viewWidth / 2;
                bigWindowParams.y = screenHeight / 2 - FloatWindowBigView.viewHeight / 2;
                bigWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
                bigWindowParams.format = PixelFormat.RGBA_8888;
                bigWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
                bigWindowParams.width = FloatWindowBigView.viewWidth;
                bigWindowParams.height = FloatWindowBigView.viewHeight;
            }
            windowManager.addView(bigWindow, bigWindowParams);
        }
    }

    public static void removeBigWindow(Context context) {
        if (bigWindow != null) {
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(bigWindow);
            bigWindow = null;
        }
    }
    public static void updateUsedPercent(Context context) {
        if (smallWindow != null) {
            TextView percentView = (TextView) smallWindow.findViewById(R.id.percent);
            percentView.setText(getUsedPercentValue(context));
        }
    }

    public static boolean isWindowShowing() {
        return smallWindow != null || bigWindow != null;
    }

    private static WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

    private static ActivityManager getActivityManager(Context context) {
        if (mActivityManager == null) {
            mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        }
        return mActivityManager;
    }

    public static String getUsedPercentValue(Context context) {
        String dir = "/proc/meminfo";
        try {
            FileReader fr = new FileReader(dir);
            BufferedReader br = new BufferedReader(fr, 2048);
            String memoryLine = br.readLine();
            String subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
            br.close();
            long totalMemorySize = Integer.parseInt(subMemoryLine.replaceAll("\\D+", ""));
            long availableSize = getAvailableMemory(context) / 1024;
            int percent = (int) ((totalMemorySize - availableSize) / (float) totalMemorySize * 100);
            return percent + "%";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "悬浮窗";
    }

    private static long getAvailableMemory(Context context) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        getActivityManager(context).getMemoryInfo(mi);
        return mi.availMem;
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
    public static void refreshWechat(String str){
        smallWindow.updateMessage(str);
    }
    public static void refreshPet(Context context, int mood){
        String sad_filedir = CustomizeActivity.filedir_sad;
        String happy_filedir = CustomizeActivity.filedir_happy;
        ArrayList<String> sad_pathlist = readfile(context, sad_filedir);
        ArrayList<String> happy_pathlist = readfile(context, happy_filedir);
        ArrayList<String> choose = null;
        if(mood == FloatWindowService.HAPPY){
            choose = happy_pathlist;
        }
        else if(mood == FloatWindowService.SAD){
            choose = sad_pathlist;
        }
        int length = choose.size();
        if(length == 0){ return; }
        Random rand = new Random();
        int index = rand.nextInt(length);
        String dir = choose.get(index);
        boolean Res = smallWindow.updatePet(dir);
        if(Res) return;
        else{
            Log.e("MyWindowManager", "Reflesh pet failed");
            return;
        }
    }
}
