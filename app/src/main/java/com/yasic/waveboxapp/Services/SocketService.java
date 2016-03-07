package com.yasic.waveboxapp.Services;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.EditText;

import com.amap.api.maps.model.LatLng;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.yasic.waveboxapp.Utils.MathUtils;
import com.yasic.waveboxapp.Utils.UserUtils;

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
public class SocketService extends Service {

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

    /**
     * 接收缓冲区区大小
     */
    private int byteSize = 1024;

    /**
     * 信息缓冲区
     */
    private byte[] buf = new byte[byteSize];

    /**
     * 本地上次地理位置信息
     */
    private double localLat = 0,localLng = 0 ;

    /**
     * 是否计算被占用
     */
    private boolean isProcessBusy = false;

    /**
     * 临时计算的车辆gps信息
     */
    private String tempCarMessage;

    /**
     * 合成监听器
     */
    private SynthesizerListener mSynListener = new SynthesizerListener(){
        //会话结束回调接口，没有错误时，error为null
        public void onCompleted(SpeechError error) {

        }
        //缓冲进度回调
        //percent为缓冲进度0~100，beginPos为缓冲音频在文本中开始位置，endPos表示缓冲音频在文本中结束位置，info为附加信息。
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
        }
        //开始播放
        public void onSpeakBegin() {
        }
        //暂停播放
        public void onSpeakPaused() {
        }
        //播放进度回调
        //percent为播放进度0~100,beginPos为播放音频在文本中开始位置，endPos表示播放音频在文本中结束位置.
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
        }
        //恢复播放回调接口
        public void onSpeakResumed() {}
        //会话事件回调接口
        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {}
    };

    /**
     * 初始化监听器
     */
    InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int i) {
        }
    };

    private Handler handler = new Handler(){
        public void handleMessage(Message msg) {
                     switch (msg.what) {
                         case 1://其他车辆信息来到
                             caculateDEInfo();
                             break;
                     }
                     }
    };

    private void caculateDEInfo(){
        String[] info = tempCarMessage.split(" ");
        Double distance = MathUtils.getDistance(Double.valueOf(info[4]) - localLat, Double.valueOf(info[5]) - localLng);
    }

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        initsocket();//初始化sockekt
        initAndCheckSpeechFunction();
        return super.onStartCommand(intent,flags,startId);
    }

    public void initsocket(){
        new Thread(){
            @Override
            public void run(){
                try {
                    socketAppIp = InetAddress.getLocalHost();
                    DatagramSocket app_Socket = new DatagramSocket();
                    socketPort = 40000;
                    socketBoxIp = InetAddress.getByName("192.168.10.224");
                    datagramSocket = new DatagramSocket(socketPort);
                    datagramPacket = new DatagramPacket(buf,buf.length);
                    while (true){
                        datagramSocket.receive(datagramPacket);
                        String message = new String(datagramPacket.getData(),datagramPacket.getOffset(),datagramPacket.getLength());
                        String[] info = message.split(" ");
                        try {
                            if(info[0].equals("V")){
                                UserUtils.setLocalUsrNickName("Bob");
                                //Log.i("local",info[7]);
                                brodcastLocalLocation(message);
                                continue;
                            }
                            if(info[2].equals("V")){
                                //Log.i("other",info[9]);
                                if (isProcessBusy){
                                    tempCarMessage = message;
                                    Message msg = new Message();
                                    msg.what = 1;
                                    handler.sendMessage(msg);
                                }
                                brodcastOtherCarLocation(message);
                            }
                            if(info[2].equals("R")){
                                speechDangerCarInfo(message);
                            }
                            if(info[2].equals("E")){
                                speechErrorCarInfo(message);
                            }
                        }catch (Exception e){
                            Log.i("error",e.getMessage());
                        }
                    }
                } catch (IOException e) {
                    Log.i("error", e.toString());
                    e.printStackTrace();
                }
            }}.start();
    }

    private void brodcastLocalLocation(String message){
        String[] info = message.split(" ");
        LatLng destnation = new LatLng(Double.valueOf(info[2]),Double.valueOf(info[4]));
        if (localLat == 0 && localLng == 0){
            localLat = destnation.latitude;
            localLng = destnation.longitude;
        }
        else {
            localLat = destnation.latitude;
            localLng = destnation.longitude;
        }
        Intent localLocationIntent = new Intent("CARLOCATIONINFO");//action为localgps_RECEIVER
        localLocationIntent.putExtra("LOCATIONINFO", message);
        sendBroadcast(localLocationIntent);//发送广播信息
    }

    private void brodcastOtherCarLocation(String message){
        Intent localLocationIntent = new Intent("CARLOCATIONINFO");
        localLocationIntent.putExtra("LOCATIONINFO", message);
        sendBroadcast(localLocationIntent);
    }

    private void speechErrorCarInfo(String message){
        //1.创建SpeechSynthesizer对象
        SpeechSynthesizer mTts= SpeechSynthesizer.createSynthesizer(this, mInitListener);
        //初始化监听器,同听写初始化监听器，使用云端的情况下不需要监听即可使用，本地需要监听
        //2.合成参数设置
        //设置引擎类型为本地
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        //可跳转到语音+发音人设置页面进行发音人下载
        //SpeechUtility.getUtility().openEngineSettings(SpeechConstant.ENG_TTS);
        //3.开始合成
        String[] info = message.split(" ");
        mTts.startSpeaking("危险！前方车辆" + info[0] + "正在减速，可能发生碰撞", mSynListener);
    }

    private void speechDangerCarInfo(String message){
        //1.创建SpeechSynthesizer对象
        SpeechSynthesizer mTts= SpeechSynthesizer.createSynthesizer(this, mInitListener);
        //初始化监听器,同听写初始化监听器，使用云端的情况下不需要监听即可使用，本地需要监听
        //2.合成参数设置
        //设置引擎类型为本地
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        //可跳转到语音+发音人设置页面进行发音人下载
        //SpeechUtility.getUtility().openEngineSettings(SpeechConstant.ENG_TTS);
        //3.开始合成
        String[] info = message.split(" ");
        mTts.startSpeaking("小心！附近车辆" + info[0] + "与您会车，请小心行驶", mSynListener);
    }

    private void initAndCheckSpeechFunction(){
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=56dbd57a");
        if(!SpeechUtility.getUtility().checkServiceInstalled ()){
            String url = SpeechUtility.getUtility().getComponentUrl();
            Uri uri = Uri.parse(url);
            Intent it = new Intent(Intent.ACTION_VIEW, uri);
            this.startActivity(it);
        }
        SpeechSynthesizer mTts= SpeechSynthesizer.createSynthesizer(this, mInitListener);
        //初始化监听器,同听写初始化监听器，使用云端的情况下不需要监听即可使用，本地需要监听
        //2.合成参数设置
        //设置引擎类型为本地
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        //可跳转到语音+发音人设置页面进行发音人下载
        //SpeechUtility.getUtility().openEngineSettings(SpeechConstant.ENG_TTS);
        //3.开始合成
        mTts.startSpeaking("", mSynListener);
        mTts.startSpeaking("金溢手机助手正在运行，祝您行车愉快，一路平安。", mSynListener);
    }

    public void chatMessage_brodcast(String frompcmessage){
        Intent chatMessage_brodcast_Intent = new Intent("CHATMESSAGE_RECEIVER");
        chatMessage_brodcast_Intent.putExtra("CHAT_MESSAGE",frompcmessage);
        sendOrderedBroadcast(chatMessage_brodcast_Intent, null);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
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

}
