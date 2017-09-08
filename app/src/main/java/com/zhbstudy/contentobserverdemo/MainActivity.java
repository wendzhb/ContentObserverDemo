package com.zhbstudy.contentobserverdemo;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
/*
    http://blog.csdn.net/qinjuning/article/details/7047607

    ContentObserver——内容观察者，目的是观察(捕捉)特定Uri引起的数据库的变化，继而做一些相应的处理，它类似于
    数据库技术中的触发器(Trigger)，当ContentObserver所观察的Uri发生变化时，便会触发它。触发器分为表触发器、行触发器，
    相应地ContentObserver也分为“表“ContentObserver、“行”ContentObserver，当然这是与它所监听的Uri MIME Type有关的。

    熟悉Content Provider(内容提供者)的应该知道，我们可以通过UriMatcher类注册不同类型的Uri，我们可以通过这些不同的
    Uri来查询不同的结果。根据Uri返回的结果，Uri Type可以分为：返回多条数据的Uri、返回单条数据的Uri。

*/

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST = 1;
    private TextView mobileText;
    private SmsObserver smsObserver;
    public Handler smsHandler = new Handler() {
        //这里可以进行回调的操作
        //TODO

    };
    private ContentResolver contentResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.SEND_SMS)) {
                    Toast.makeText(this, "test", Toast.LENGTH_SHORT).show();
                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS, Manifest.permission.BROADCAST_SMS},
                            MY_PERMISSIONS_REQUEST);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else {
//                getSmsInPhone();
                sendMessage(this,"123","18330234903");
            }
        }

        mobileText = (TextView) findViewById(R.id.tv);
        Log.e("test", "onCreate: mobileText");

        smsObserver = new SmsObserver(this, smsHandler);
        Log.e("test", "onCreate: smsObserver");

        contentResolver = getContentResolver();
        Log.e("test", "onCreate: contentResolver");



//        public final void registerContentObserver(Uri uri, boolean notifyForDescendents, ContentObserver observer)
//        功能：为指定的Uri注册一个ContentObserver派生类实例，当给定的Uri发生改变时，回调该实例对象去处理。
//        参数：
//        uri                   需要观察的Uri(需要在UriMatcher里注册，否则该Uri也没有意义了)
//        notifyForDescendents  为false 表示精确匹配，即只匹配该Uri为true 表示可以同时匹配其派生的Uri，举例如下：
//                              假设UriMatcher 里注册的Uri共有一下类型：
//                              1 、content://com.qin.cb/student (学生)
//                              2 、content://com.qin.cb/student/#
//                              3、 content://com.qin.cb/student/schoolchild(小学生，派生的Uri)
//
//        假设我们当前需要观察的Uri为content://com.qin.cb/student，如果发生数据变化的 Uri 为
//        content://com.qin.cb/student/schoolchild ，当notifyForDescendents为 false，那么该ContentObserver会监听不到，
//        但是当notifyForDescendents 为ture，能捕捉该Uri的数据库变化。
//        observer              ContentObserver的派生类实例

//        短信的Uri共有一下几种：
//        content://sms/inbox     收件箱
//        content://sms/sent        已发送
//        content://sms/draft        草稿
//        content://sms/outbox    发件箱           (正在发送的信息)
//        content://sms/failed      发送失败
//        content://sms/queued  待发送列表  (比如开启飞行模式后，该短信就在待发送列表里)
        contentResolver.registerContentObserver(SMS_INBOX, true, smsObserver);
        Log.e("test", "onCreate: registerContentObserver");


    }

    public static void sendMessage (Context context, String content, String phoneNumber){
        SmsManager sms = SmsManager.getDefault();
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(), 0);
        sms.sendTextMessage(phoneNumber, null, content, pi, null);
        Log.e("tag","send");
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.e("test", "权限已给");

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.e("test", "权限拒绝");
                }
                return;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        public final void  unregisterContentObserver(ContentObserver observer)
//        功能：取消对给定Uri的观察
//        参数： observer ContentObserver的派生类实例
        contentResolver.unregisterContentObserver(smsObserver);
    }

    private Uri SMS_INBOX = Uri.parse("content://sms/");

//      sms主要结构：
//            　　
//            　　_id：短信序号，如100
//　　
//        　thread_id：对话的序号，如100，与同一个手机号互发的短信，其序号是相同的
//　　
//        　　address：发件人地址，即手机号，如+86138138000
//            　　
//         　　person：发件人，如果发件人在通讯录中则为具体姓名，陌生人为null
//　　
//           　　date：日期，long型，如1346988516，可以对日期显示格式进行设置
//　　
//        　 protocol：协议0SMS_RPOTO短信，1MMS_PROTO彩信
//　　
//        　　   read：是否阅读0未读，1已读
//　　
//        　　 status：短信状态-1接收，0complete,64pending,128failed
//　　
//        　　   type：短信类型1是接收到的，2是已发出
//　　
//        　　   body：短信具体内容
//　　
//     service_center：短信服务中心号码编号，如+8613800755500

    public void getSmsFromPhone() {
        ContentResolver cr = getContentResolver();
//        String[] projection = new String[] { "body" };//"_id", "address", "person",, "date", "type
        String[] projection = new String[]{"_id", "address", "person", "body", "date", "type"};
        String where = " address = '10086' AND date >  "
                + (System.currentTimeMillis() - 10 * 60 * 1000);
//        Cursor cur = cr.query(SMS_INBOX, projection, where, null, "date desc");
        Cursor cur = cr.query(SMS_INBOX, projection, null, null, "date desc");
        Log.e("test", "cur");
        if (null == cur)
            return;
        if (cur.moveToNext()) {
            String number = cur.getString(cur.getColumnIndex("address"));//手机号
            Log.e("test", "number");
            String name = cur.getString(cur.getColumnIndex("person"));//联系人姓名列表
            Log.e("test", "name");
            String body = cur.getString(cur.getColumnIndex("body"));
            Log.e("test", number + name + body);
//            //这里我是要获取自己短信服务号码中的验证码~~
//            Pattern pattern = Pattern.compile(" [a-zA-Z0-9]{10}");
//            Matcher matcher = pattern.matcher(body);
//            if (matcher.find()) {
//                String res = matcher.group().substring(1, 11);
//                mobileText.setText(res);
//            }
        }
    }

    //    ContentObserver类介绍
//
//    构造方法 public void ContentObserver(Handler handler)
//    说明：所有   ContentObserver的派生类都需要调用该构造方法
//　　　  　　 参数：　handler　 Handler对象。可以是主线程Handler(这时候可以更新UI 了)，也可以是任何Handler对象。
//    常用方法
//    void onChange(boolean selfChange)
//    功能：当观察到的Uri发生变化时，回调该方法去处理。所有ContentObserver的派生类都需要重载该方法去处理逻辑。
//    参数：selfChange　回调后，其值一般为false，该参数意义不大(我也不懂，理解方法最重要)。
//
//    另外两个方法，用处不大，我也不懂，大家参照SDK自行理解，冒昧了。
//    boolean  deliverSelfNotifications()
//    说明：Returns true if this observer is interested in notifications for changes made through the cursor the observer is registered with.
//
//    final void dispatchChange(boolean selfChange)
//
//
//    观察特定Uri的步骤如下：
//
//     1、    创建我们特定的ContentObserver派生类，必须重载父类构造方法，必须重载onChange()方法去处理回调后的功能实现
//     2、    利用context.getContentResolover()获得ContentResolove对象，接着调用registerContentObserver()方法去注册内容观察者
//     3、    由于ContentObserver的生命周期不同步于Activity和Service等，因此，在不需要时，需要手动的调用unregisterContentObserver()去取消注册。
    class SmsObserver extends ContentObserver {

        public SmsObserver(Context context, Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            //每当有新短信到来时，使用我们获取短消息的方法
            getSmsFromPhone();
            Log.e("test", "SmsObserver");

        }
    }


    public String getSmsInPhone() {
        final String SMS_URI_ALL = "content://sms/";
        final String SMS_URI_INBOX = "content://sms/inbox";
        final String SMS_URI_SEND = "content://sms/sent";
        final String SMS_URI_DRAFT = "content://sms/draft";
        final String SMS_URI_OUTBOX = "content://sms/outbox";
        final String SMS_URI_FAILED = "content://sms/failed";
        final String SMS_URI_QUEUED = "content://sms/queued";

        StringBuilder smsBuilder = new StringBuilder();

        try {
            Uri uri = Uri.parse(SMS_URI_ALL);
            String[] projection = new String[]{"_id", "address", "person", "body", "date", "type"};
            Cursor cur = getContentResolver().query(uri, projection, null, null, "date desc");      // 获取手机内部短信

            if (cur.moveToFirst()) {
                int index_Address = cur.getColumnIndex("address");
                int index_Person = cur.getColumnIndex("person");
                int index_Body = cur.getColumnIndex("body");
                int index_Date = cur.getColumnIndex("date");
                int index_Type = cur.getColumnIndex("type");

                do {
                    String strAddress = cur.getString(index_Address);
                    int intPerson = cur.getInt(index_Person);
                    String strbody = cur.getString(index_Body);
                    long longDate = cur.getLong(index_Date);
                    int intType = cur.getInt(index_Type);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    Date d = new Date(longDate);
                    String strDate = dateFormat.format(d);

                    String strType = "";
                    if (intType == 1) {
                        strType = "接收";
                    } else if (intType == 2) {
                        strType = "发送";
                    } else {
                        strType = "null";
                    }

                    smsBuilder.append("[ ");
                    smsBuilder.append(strAddress + ", ");
                    smsBuilder.append(intPerson + ", ");
                    smsBuilder.append(strbody + ", ");
                    smsBuilder.append(strDate + ", ");
                    smsBuilder.append(strType);
                    smsBuilder.append(" ]\n\n");
                } while (cur.moveToNext());

                if (!cur.isClosed()) {
                    cur.close();
                    cur = null;
                }
            } else {
                smsBuilder.append("no result!");
            } // end if

            smsBuilder.append("getSmsInPhone has executed!");

        } catch (SQLiteException ex) {
            Log.e("getSmsInPhone", "SQLiteException in getSmsInPhone" + ex.getMessage());
        }
        Log.e("getSmsInPhone", "getSmsInPhone: " + smsBuilder.toString());
        return smsBuilder.toString();
    }

}
