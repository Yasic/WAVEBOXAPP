package com.yasic.waveboxapp.Activitys;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.yasic.waveboxapp.Adapters.ChatlistAdapter;
import com.yasic.waveboxapp.Objects.User;
import com.yasic.waveboxapp.R;
import com.yasic.waveboxapp.Services.SocketService;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * 显示时速的textview
     */
    private TextView tvSpeed;

    /**
     * 车辆坐标广播接收器
     */
    private BroadcastReceiver locationBrodcastReceiver;

    /**
     * 显示消息列表的recyclerview
     */
    private RecyclerView rvChatlist;

    /**
     * 显示消息的adapter
     */
    private ChatlistAdapter chatlistAdapter;

    private List<User> userList = new ArrayList<>();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initMD();
        initViews();
        installLocationBrodcastReceiver();
        initAndCheckSpeechFunction();

    }

    private void installLocationBrodcastReceiver(){
        locationBrodcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String locationInfo = intent.getStringExtra("LOCATIONINFO");
                dealLocationInfo(locationInfo);
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("CARLOCATIONINFO");
        registerReceiver(locationBrodcastReceiver, intentFilter);//注册广播接收器
    }

    private void dealLocationInfo(String locationInfo){
        String[] info = locationInfo.split(" ");
        if(info[0].equals("V")){
            tvSpeed.setText(info[6]);
        }
        if(info[2].equals("V")){
            boolean isExit = false;
            if(userList.size() == 0){
                userList.add(new User(info[0]));
                chatlistAdapter.refresh(userList);
            }
            else{
                for(int i = 0; i < userList.size(); i++){
                    if(userList.get(i).getUserNickName().equals(info[0])){
                        isExit = true;
                    }
                }
                if(!isExit){
                    userList.add(new User(info[0]));
                    chatlistAdapter.refresh(userList);
                }
            }
        }
    }

    private void initAndCheckSpeechFunction(){
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=56dbd57a");
        if(!SpeechUtility.getUtility().checkServiceInstalled ()){
            String url = SpeechUtility.getUtility().getComponentUrl();
            Uri uri = Uri.parse(url);
            Intent it = new Intent(Intent.ACTION_VIEW, uri);
            this.startActivity(it);
        }
        //1.创建SpeechSynthesizer对象
        SpeechSynthesizer mTts= SpeechSynthesizer.createSynthesizer(MainActivity.this, mInitListener);
        //初始化监听器,同听写初始化监听器，使用云端的情况下不需要监听即可使用，本地需要监听
        //2.合成参数设置
        //设置引擎类型为本地
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        //可跳转到语音+发音人设置页面进行发音人下载
        //SpeechUtility.getUtility().openEngineSettings(SpeechConstant.ENG_TTS);
        //3.开始合成
        mTts.startSpeaking("", mSynListener);
    }

    private void initViews(){
        tvSpeed = (TextView)findViewById(R.id.tv_speed);
        rvChatlist = (RecyclerView)findViewById(R.id.rv_chatlist);
        chatlistAdapter = new ChatlistAdapter(this, userList);
        rvChatlist.setAdapter(chatlistAdapter);
        rvChatlist.setLayoutManager(new LinearLayoutManager(this));
        rvChatlist.setItemAnimator(new DefaultItemAnimator());
        chatlistAdapter.setOnItemClickListener(new ChatlistAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Intent intent = new Intent(MainActivity.this, SingleMessageInterface.class);
                startActivity(intent);
            }

            @Override
            public void onItemLongCick(View v, int position) {

            }
        });
    }

    private void startSocketService() {
        startService(new Intent(MainActivity.this, SocketService.class));
    }

    private void initMD() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, MapLocateActivity.class));
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        final SearchView searchView = (SearchView)menu.findItem(R.id.action_search).getActionView();
        searchView.setQueryHint("通过昵称查找用户");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_serviceswich){
            if (item.isChecked()){
                stopService(new Intent(MainActivity.this, SocketService.class));
                item.setChecked(false);
            }
            else {
                startSocketService();
                item.setChecked(true);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_camara) {

        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(locationBrodcastReceiver);
        super.onDestroy();
    }
}
