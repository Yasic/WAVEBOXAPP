package com.yasic.waveboxapp.Activitys;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.yasic.waveboxapp.Adapters.MessageAdapter;
import com.yasic.waveboxapp.Objects.UserMessage;
import com.yasic.waveboxapp.R;
import com.yasic.waveboxapp.Utils.AsyncHttpUtils;
import com.yasic.waveboxapp.Utils.TimeUtils;
import com.yasic.waveboxapp.Utils.UserUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

/**
 * Created by ESIR on 2016/2/24.
 */
public class SingleMessageInterface extends AppCompatActivity {

    /**
     * 消息显示rv
     */
    private RecyclerView rvMessageList;

    /**
     * asynchttp实体
     */
    private AsyncHttpClient client;

    /**
     * cookiestore实体
     */
    private PersistentCookieStore cookieStore;

    /**
     * 消息列表
     */
    private List<UserMessage> messageList = new ArrayList<UserMessage>();

    /**
     * 对方账号
     */
    private String chaterAccount;

    /**
     * 本地账号
     */
    private String localAccount;

    /**
     * 对方昵称
     */
    private String chaterNickName;

    /**
     * 处理消息的adapter
     */
    private MessageAdapter messageAdapter;

    /**
     * 显示空消息的textview
     */
    private TextView tvEmptyMessage;

    /**
     * 发消息的button
     */
    private Button btPostMessage;

    /**
     * 输入消息的edittext
     */
    private EditText etMassageInput;

    /**
     * 要发送的消息
     */
    private String messange;

    /**
     * 发送时间
     */
    private String sendTime;

    /**
     * 控制时间
     */
    private Timer timer = new Timer();

    /**
     * 是否获取数据超时
     */
    private boolean isGetDataTimeOut = true;

    /**
     * 是否发送消息超时
     */
    private boolean isSendMessageTimeOut = true;

    private Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1://getdata timeout error
                    if (isGetDataTimeOut){
                        //Toast.makeText(getApplicationContext(), R.string.Timeouterror,Toast.LENGTH_LONG).show();
                    }
                    isGetDataTimeOut = true;
                    btPostMessage.setClickable(true);
                    btPostMessage.setAlpha(1f);
                    break;
                case 2://sendmessage timeout error
                    if (isSendMessageTimeOut){
                        //Toast.makeText(getApplicationContext(), R.string.Timeouterror,Toast.LENGTH_LONG).show();
                    }
                    isSendMessageTimeOut = true;
                    btPostMessage.setClickable(true);
                    btPostMessage.setAlpha(1f);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singlemessageinterface);
        getInfo();
        initRecyclerView();
        initAsyncHttp();
        setSendFunction();
    }

    private void getInfo(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        try {
            chaterAccount = bundle.getString("chaterAccount");
            localAccount = bundle.getString("localAccount");
            chaterNickName = bundle.getString("chaterNickName");
            setTitle(chaterNickName);
        }catch (Exception e){
            Log.i("intenterror",e.getMessage());
        }
    }

    private void initRecyclerView() {
        rvMessageList = (RecyclerView)findViewById(R.id.rv_messagelist);
        rvMessageList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,true));
        rvMessageList.setItemAnimator(new DefaultItemAnimator());
        messageAdapter = new MessageAdapter(this, messageList, localAccount);
        rvMessageList.setAdapter(messageAdapter);

        tvEmptyMessage = (TextView)findViewById(R.id.tv_emptymessage);
        tvEmptyMessage.setVisibility(View.GONE);
    }

    private void setSendFunction(){
        btPostMessage = (Button)findViewById(R.id.bt_postmessage);
        etMassageInput = (EditText)findViewById(R.id.et_massageinput);
        btPostMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void initAsyncHttp(){
        client = new AsyncHttpClient();
        cookieStore = new PersistentCookieStore(getApplicationContext());
        client.setCookieStore(AsyncHttpUtils.getUtilsInstance().getCookieStoreInstance());
        client.setTimeout(12000);
    }
}
