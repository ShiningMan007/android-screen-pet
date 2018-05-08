package com.example.administrator.screenpet;

import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class WechatNotifyService extends NotificationListenerService {
    public static final String SEND_WX_BROADCAST="SEND_WX_BROADCAST";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn, RankingMap rankingMap) {
        String packageName=sbn.getPackageName();
        String message = sbn.getNotification().tickerText.toString();
        Intent intent=new Intent();
        intent.setAction(SEND_WX_BROADCAST);
        Bundle bundle=new Bundle();
        bundle.putString("packageName",packageName);
        bundle.putString("message",message);
        intent.putExtras(bundle);
        this.sendBroadcast(intent);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn, RankingMap rankingMap) {
    }
}
