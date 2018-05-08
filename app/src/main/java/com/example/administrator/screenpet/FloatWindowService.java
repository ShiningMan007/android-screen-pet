package com.example.administrator.screenpet;


import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2018/3/11/011.
 */

public class FloatWindowService extends Service implements WechatMsg{
    private ComeWxMessage comeWxMessage;
    private WechatMsg myMessage;
    public TextView textView ;
    public static final int UPDATE_TEXT = 1;
    public static final String SEND_WX_BROADCAST="SEND_WX_BROADCAST";
    public static final String WX="com.tencent.mm";

    public static final int HAPPY = 1;
    public static final int SAD = -1;
    private Handler handler = new Handler();
    private Handler pet_handler = new Handler();
    private Timer timer;
    private Handler displaywechat_handler = new Handler();

    private BroadcastReceiver b=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle=intent.getExtras();
            String pachageName=bundle.getString("packageName");
            String message = bundle.getString("message");
            switch (pachageName){
                case WX:
                    MyWindowManager.refreshWechat(message);
                    if(timer != null){
                        timer.schedule(new RefreshWechatTask(), 5000);
                    }
                    break;
                default:
                    break;
            }
        }
    };
    private void registBroadCast() {
        IntentFilter filter=new IntentFilter(SEND_WX_BROADCAST);
        registerReceiver(b,filter);
    }
    public void unRegistBroadcast() {
        unregisterReceiver(b);
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 开启定时器，每隔0.5秒刷新一次
        if (timer == null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new RefreshTask(), 0, 500);
            timer.scheduleAtFixedRate(new RefreshPetTask(), 0, 10000);
        }
        myMessage=new FloatWindowService();
        comeWxMessage=new ComeWxMessage(myMessage,this);
        comeWxMessage.toggleNotificationListenerService();
        registBroadCast();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Service被终止的同时也停止定时器继续运行
        timer.cancel();
        timer = null;
        unRegistBroadcast();
    }

    @Override
    public void comeWxMessage(String message) {

    }

    class RefreshWechatTask extends  TimerTask{
        public void run(){
            if (isHome() && MyWindowManager.isWindowShowing()) {
                pet_handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyWindowManager.refreshWechat("Ihateyouqzy");
                    }
                });
            }
        }
    }

    class RefreshTask extends TimerTask {

        @Override
        public void run() {
            // 当前界面是桌面，且没有悬浮窗显示，则创建悬浮窗。
            if (isHome() && !MyWindowManager.isWindowShowing()) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyWindowManager.createSmallWindow(getApplicationContext());
                    }
                });
            }
            // 当前界面不是桌面，且有悬浮窗显示，则移除悬浮窗。
            else if (!isHome() && MyWindowManager.isWindowShowing()) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyWindowManager.removeSmallWindow(getApplicationContext());
                        MyWindowManager.removeBigWindow(getApplicationContext());
                    }
                });
            }
            // 当前界面是桌面，且有悬浮窗显示，则更新内存数据。
            else if (isHome() && MyWindowManager.isWindowShowing()) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyWindowManager.updateUsedPercent(getApplicationContext());
                    }
                });
            }
        }

    }

    private int determineMood(){
        return HAPPY;
    }

    class RefreshPetTask extends TimerTask{
        public void run(){
            if (isHome() && MyWindowManager.isWindowShowing()) {
                pet_handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyWindowManager.refreshPet(getApplicationContext(), determineMood());
                    }
                });
            }
        }
    }

    /**
     * 判断当前界面是否是桌面

    private boolean isHome() {
        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
        return getHomes().contains(rti.get(0).topActivity.getPackageName());
    }*/

    public boolean isHome(){
        ActivityManager mActivityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
        List<String> strs = getHomes();
        if(strs != null && strs.size() > 0){
            return strs.contains(rti.get(0).topActivity.getPackageName());
        }else{
            return false;
        }
    }

    /**
     * 获得属于桌面的应用的应用包名称
     *
     * @return 返回包含所有包名的字符串列表
     */
    private List<String> getHomes() {
        List<String> names = new ArrayList<String>();
        PackageManager packageManager = this.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri : resolveInfo) {
            names.add(ri.activityInfo.packageName);
        }
        return names;
    }
}