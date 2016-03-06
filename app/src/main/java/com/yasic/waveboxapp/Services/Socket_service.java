package com.yasic.waveboxapp.Services;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ESIR on 2015/7/24.
 */
public class Socket_service extends Service {

    /**
     * App的Ip地址
     */
    private InetAddress socketAppIp;

    /**
     * socket端口
     */
    private static int socketPort = 40000;

    /**
     * WAVEBOX的Ip地址
     */
    private InetAddress socketBoxIp;

    /**
     * datagram报文包
     */
    private DatagramPacket datagramPacket;

    /**
     * datagram socket实体
     */
    private DatagramSocket datagramSocket;

    private int byteSize = 1024;

    /**
     * 信息缓冲区
     */
    private byte[] buf = new byte[byteSize];



    private Handler handler = new Handler(){
        public void handleMessage(Message msg) {
                     switch (msg.what) {
                         case 1:
                             break;
                     }
                     }
    };

    @Override
    public void onCreate(){

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        initsocket();//初始化sockekt
        return super.onStartCommand(intent,flags,startId);
    }

    public void initsocket(){
        new Thread(){
            @Override
            public void run(){
                try {
                    socketAppIp = InetAddress.getLocalHost();
                    Log.i("ip",socketAppIp+"");
                    DatagramSocket app_Socket = new DatagramSocket();
                    socketPort = 40000;
                    socketBoxIp = InetAddress.getByName("192.168.10.224");
                    datagramSocket = new DatagramSocket(socketPort);
                    datagramPacket = new DatagramPacket(buf,buf.length);
                    while (true){
                        datagramSocket.receive(datagramPacket);
                        //String message = new String(bytes, 0, datagramPacket.getLength(),"GBK");
                        String message = new String(datagramPacket.getData(),datagramPacket.getOffset(),datagramPacket.getLength());
                       // Log.i("message", message);
                        String[] info = message.split(" ");
                        try {
                            if(info[0].equals("V")){
                                brodcastLocalLocation(message);
                                continue;
                            }
                            if(info[2].equals("V")){
                                //Log.i("info",info[2] + "3");
                                brodcastOtherCarLocation(message);
                            }
                        }catch (Exception e){
                            Log.i("error",e.getMessage());
                        }
                        //buf = new byte[byteSize];
                    }
                } catch (IOException e) {
                    Log.i("error", e.toString());
                    e.printStackTrace();
                }
            }}.start();
    }

    public void chatDB_Add(String[] info){
        /*JinyiSQLite_dbhelper jinyiSQLite_dbhelper = new JinyiSQLite_dbhelper(this,"JINYICHAT_DB",1);
        ContentValues values = new ContentValues();
        values.put("NAME",info[0]);//姓名
        values.put("MESSAGE",info[3]);//消息
        values.put("TIME",info[1]);
        values.put("ISCOMMSG","true");//来自外部
        jinyiSQLite_dbhelper.getWritableDatabase().insert("JINYICHAT",null,values);
        jinyiSQLite_dbhelper.close();*/
    }

    public void listDB_Add(String[] info){
        /*JinyiSQLite_listdbhelper jinyiSQLite_listdbhelper = new JinyiSQLite_listdbhelper(this,"JINYICHATLIST_DB",1);
        //Cursor cursor = jinyiSQLite_listdbhelper.getReadableDatabase().rawQuery("SELECT * FROM JINYICHATLISTTABLE", null);
        Cursor cursor  = jinyiSQLite_listdbhelper.getReadableDatabase().query("JINYICHATLIST", null, null, null, null, null, null);
        int flag = 0;//默认非重复
        while (cursor.moveToNext()) {
            if(info[0].equals(cursor.getString(cursor.getColumnIndex("NAME")))){
                flag = 1;//已在数据库重复
                break;
            }
        }
        if(flag == 1){
            //Log.i("name and message",info[0] + info[3]);
            ContentValues cv = new ContentValues();
            cv.put("NAME", info[0]);
            cv.put("MESSAGE",info[3]);
            cv.put("TIME",info[1]);
            cv.put("ISNEW", "true");
            jinyiSQLite_listdbhelper.getWritableDatabase().update("JINYICHATLIST", cv, "NAME = ?", new String[]{info[0]});//更新数据库
        }
        else{
            ContentValues cv = new ContentValues();
            cv.put("NAME", info[0]);
            cv.put("MESSAGE",info[3]);
            cv.put("TIME",info[1]);
            cv.put("ISNEW", "true");
            jinyiSQLite_listdbhelper.getWritableDatabase().insert("JINYICHATLIST",null,cv);
        }
        jinyiSQLite_listdbhelper.close();*/
    }

    public void brodcastLocalLocation(String message){
        Intent localLocationIntent = new Intent("CARLOCATIONINFO");//action为localgps_RECEIVER
        localLocationIntent.putExtra("LOCATIONINFO", message);
        sendBroadcast(localLocationIntent);//发送广播信息
    }

    public void brodcastOtherCarLocation(String message){
        Intent localLocationIntent = new Intent("CARLOCATIONINFO");
        localLocationIntent.putExtra("LOCATIONINFO", message);
        sendBroadcast(localLocationIntent);
    }

    public void chatMessage_brodcast(String frompcmessage){
        Intent chatMessage_brodcast_Intent = new Intent("CHATMESSAGE_RECEIVER");
        chatMessage_brodcast_Intent.putExtra("CHAT_MESSAGE",frompcmessage);
        sendOrderedBroadcast(chatMessage_brodcast_Intent,null);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
