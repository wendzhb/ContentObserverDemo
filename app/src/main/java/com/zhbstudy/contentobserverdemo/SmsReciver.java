package com.zhbstudy.contentobserverdemo;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mtf on 2017/4/8.
 */

public class SmsReciver extends BroadcastReceiver {
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            this.abortBroadcast();

            Bundle bundle = intent.getExtras();
            SmsMessage msg = null;
            if (null != bundle) {
                Object[] smsObj = (Object[]) bundle.get("pdus");
                for (Object object : smsObj) {
                    msg = SmsMessage.createFromPdu((byte[]) object);
                    Date date = new Date(msg.getTimestampMillis());//时间
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String receiveTime = format.format(date);

                    //在这里写自己的逻辑
                    if (msg.getOriginatingAddress().contains("18330234903")) {
                        //TODO
                        deleteSMS("12345");
                        Log.e("test", "number:" + msg.getOriginatingAddress()
                                + "   body:" + msg.getDisplayMessageBody() + "  time:"
                                + msg.getTimestampMillis());
                    }
                }
            }
        }
    }

    public void deleteSMS(String smscontent) {
        ContentResolver CR = context.getContentResolver();
        try {
            // 准备系统短信收信箱的uri地址
            Uri uri = Uri.parse("content://sms/inbox");
            // 查询收信箱里所有的短信
            Cursor cursor = CR.query(uri, new String[]{"_id", "address", "person", "body", "date", "type"}, null, null, null);
            int count = cursor.getCount();
            if (count > 0) {
                while (cursor.moveToNext()) {
                    String body = cursor.getString(cursor.getColumnIndex("body"));// 获取信息内容
                    if (body.contains(smscontent)) {
                        int id = cursor.getInt(cursor.getColumnIndex("_id"));
                        CR.delete(Uri.parse("content://sms"), "_id=" + id, null);
                    }
                }
            }
        } catch (Exception e) {
            Log.v("e", e.getMessage());
        }
    }

    //根据最新的短信实现删除信息(删除的对象是联系人而非短信)
    public void deleteSMS() {
        ContentResolver CR = context.getContentResolver();
        // 查询收信箱里所有的短信
        Cursor cursor = CR.query(Uri.parse("content://sms/inbox"), new String[]{"_id", "thread_id"}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int a = cursor.getCount();
            int b = cursor.getColumnCount();
            long threadId = cursor.getLong(1);
            CR.delete(Uri.parse("content://sms/conversations/" + threadId), null, null);
        }
    }
}
