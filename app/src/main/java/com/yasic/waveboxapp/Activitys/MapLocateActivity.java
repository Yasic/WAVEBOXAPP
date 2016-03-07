package com.yasic.waveboxapp.Activitys;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.yasic.waveboxapp.R;
import com.yasic.waveboxapp.Utils.MathUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ESIR on 2016/3/4.
 */
public class MapLocateActivity extends AppCompatActivity {
    /**
     * map实体
     */
    private AMap aMap;

    /**
     * 显示map的mapview
     */
    private MapView mapView;

    /**
     * 显示车辆时速的textview
     */
    private TextView tvSpeed;

    /**
     * 车辆坐标广播接收器
     */
    private BroadcastReceiver locationBrodcastReceiver;

    /**
     * 本车标志
     */
    private Marker localMarker;

    /**
     * 车辆id list
     */
    private List<Marker> carNameList = new ArrayList<>();

    /**
     * 本地上次地理位置信息
     */
    private double localLat = 0,localLng = 0 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maplocate);
        initMap(savedInstanceState);
        initView();
    }

    private void initView(){
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MapLocateActivity.this, MainActivity.class));
            }
        });
        tvSpeed = (TextView)findViewById(R.id.tv_speed);
    }

    /**
     * 初始化地图
     */
    private void initMap(Bundle savedInstanceState) {
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        if (aMap == null) {
            aMap = mapView.getMap();
            installLocationBrodcastReceiver();
            //setUpMap();
            //setLocationStyle();
        }
    }

    /**
     * 往地图上添加本车marker
     */
    private void setLocalMarker(LatLng destnation) {
        MarkerOptions markerOptions = new MarkerOptions().setFlat(true)
                .position(destnation)
                .title("I")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        if(localMarker == null){
            localMarker = aMap.addMarker(markerOptions);
        }
        else {
            localMarker.destroy();
            localMarker = aMap.addMarker(markerOptions);
        }
    }

    /**
     * 移动地图function
     */
    private void moveMapToDes(LatLng destnation) {
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(new CameraPosition(
                destnation, 18, 0, 30));
        if (localLat == 0 && localLng == 0){
            localLat = destnation.latitude;
            localLng = destnation.longitude;
            setLocalMarker(destnation);
            aMap.moveCamera(update);
        }
        else if (MathUtils.getDistance(localLat - destnation.latitude,localLng - destnation.longitude) < 10){
            return;
        }
        else {
            localLat = destnation.latitude;
            localLng = destnation.longitude;
            setLocalMarker(destnation);
            aMap.moveCamera(update);
        }
    }

    /**
     * 标记其他车辆
     * @param info 传入其他车辆信息
     */
    private void markOtherCar(String[] info){
        String carName = info[0];
        boolean findIt = false;
        LatLng location = new LatLng(Double.valueOf(info[4]),Double.valueOf(info[6]));
        MarkerOptions markerOptions = new MarkerOptions().setFlat(true)
                .position(location)
                .title(carName)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        for(int i = 0;i < carNameList.size();i++) {
            if (carName.equals(carNameList.get(i).getTitle())) {
                carNameList.get(i).destroy();
                carNameList.remove(i);
                Marker marker = aMap.addMarker(markerOptions.title(carName));
                carNameList.add(marker);
                findIt = true;
                break;
            }
        }
        if (findIt == false){
            Marker marker = aMap.addMarker(markerOptions.title(carName));
            carNameList.add(marker);
        }
    }

    /**
     * 注册地理位置广播接收器
     */
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

    /**
     * 处理收到的坐标消息
     * @param locationInfo 传入收到的坐标信息
     */
    private void dealLocationInfo(String locationInfo){
        String[] info = locationInfo.split(" ");
        if(info[0].equals("V")){
            moveMapToDes(new LatLng(Double.valueOf(info[2]),Double.valueOf(info[4])));
            tvSpeed.setText(info[6]);
            return;
        }
        if (info[2].equals("V")){
            markOtherCar(info);
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        unregisterReceiver(locationBrodcastReceiver);
        super.onDestroy();
        mapView.onDestroy();
    }
}
