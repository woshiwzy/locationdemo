package com.example.wangzy.locationdemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;

import java.util.Collection;

public class LocationUtils {

    private static final long REFRESH_TIME = 1000L;
    private static final float METER_POSITION = 0.0f;
    private static ILocationListener mLocationListener;
    private static LocationListener listener = new MyLocationListener();
    LocationManager manager;
    public static String tag = "location";

    private static class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {//定位改变监听

            Log.e("tag", "====");

            if (mLocationListener != null) {
                mLocationListener.onSuccessLocation(location);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }


        @Override
        public void onProviderEnabled(String provider) {//定位状态可用监听

        }

        @Override
        public void onProviderDisabled(String provider) {//定位状态不可用监听

        }
    }


    /**
     * network获取定位方式
     */
    public static Location getNetWorkLocation(Context context) {
        Location location = null;
        LocationManager manager = getLocationManager(context);
        //高版本的权限检查
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {//是否支持Network定位
            //获取最后的network定位信息
            location = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        return location;
    }

    /**
     * 获取最好的定位方式
     */
    public static Location getBestLocation(Context context, Criteria criteria) {
        Location location;
        LocationManager manager = getLocationManager(context);
        if (criteria == null) {
            criteria = new Criteria();
        }
        String provider = manager.getBestProvider(criteria, true);
        if (TextUtils.isEmpty(provider)) {
            //如果找不到最适合的定位，使用network定位
            location = getNetWorkLocation(context);
        } else {
            //高版本的权限检查
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            //获取最适合的定位方式的最后的定位权限
            location = manager.getLastKnownLocation(provider);
        }
        return location;
    }

    /**
     * 定位监听
     */
    public static void addLocationListener(Context context, String provider, ILocationListener locationListener) {

        addLocationListener(context, provider, REFRESH_TIME, METER_POSITION, locationListener);
    }

    /**
     * 定位监听
     */
    public static void addLocationListener(Context context, String provider, long time, float meter, ILocationListener locationListener) {
        if (locationListener != null) {
            mLocationListener = locationListener;
        }
        if (listener == null) {
            listener = new MyLocationListener();
        }
        LocationManager manager = getLocationManager(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.requestLocationUpdates(provider, time, meter, listener);
        manager.registerGnssMeasurementsCallback(new GnssMeasurementsEvent.Callback() {
            @Override
            public void onGnssMeasurementsReceived(GnssMeasurementsEvent eventArgs) {
                Log.e(tag, "onGnssMeasurementsReceived:------------");
                if (null != eventArgs.getMeasurements()) {
                    Collection<GnssMeasurement> xx = eventArgs.getMeasurements();
                    for (GnssMeasurement x:xx) {
                        Log.e(tag,"xx"+x.toString());
                    }
                }

            }

            @Override
            public void onStatusChanged(int status) {
                Log.e(tag, "onStatusChanged:------------");
            }
        });

        manager.addNmeaListener(new OnNmeaMessageListener() {
            @Override
            public void onNmeaMessage(String message, long timestamp) {

                Log.e(tag, "nemadata:" + message);

            }
        });

    }

    /**
     * 取消定位监听
     */
    public static void unRegisterListener(Context context) {
        if (listener != null) {
            LocationManager manager = getLocationManager(context);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //移除定位监听
            manager.removeUpdates(listener);
        }
    }

    private static LocationManager getLocationManager(@NonNull Context context) {
        return (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     * 自定义接口
     */
    public interface ILocationListener {
        void onSuccessLocation(Location location);
    }
}