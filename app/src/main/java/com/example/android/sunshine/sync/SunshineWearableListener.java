package com.example.android.sunshine.sync;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.sunshine.ForecastAdapter;
import com.example.android.sunshine.MainActivity;
import com.example.android.sunshine.utilities.SunshineWeatherUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

public class SunshineWearableListener extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient mGoogleApiClient;
    public SunshineWearableListener() {
        Log.e("aaa","aaa");

    }





    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        super.onDataChanged(dataEventBuffer);
        Log.e("Request:",dataEventBuffer.get(0).toString());
        for (DataEvent dataEvent : dataEventBuffer) {
            boolean isWearableUpdateRequest = dataEvent.getDataItem().getUri().
                    toString().contains("/sync/weather");
            if (isWearableUpdateRequest)
                SunshineSyncTask.syncWeather(getApplicationContext());
            updateWearable();
        }


    }

    public void updateWearable(){

        mGoogleApiClient = new GoogleApiClient.Builder(getBaseContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
        SunshineSyncTask.syncWeather(getBaseContext());
        Cursor cursor=com.example.android.sunshine.ForecastAdapter.getCursor();
        if(cursor.moveToFirst()){
            Double max=cursor.getDouble(1);
            Double min=cursor.getDouble(2);
            int weatherId = cursor.getInt(MainActivity.INDEX_WEATHER_CONDITION_ID);
            String condition=SunshineWeatherUtils.getStringForWeatherCondition(getBaseContext(), weatherId);

            PutDataMapRequest weatherForecastPutDataMapRequest = PutDataMapRequest.create(
                    "/data/weather_forecast" + System.currentTimeMillis());
            DataMap weatherForecastDataMap = weatherForecastPutDataMapRequest.getDataMap();
            weatherForecastDataMap.putDouble("high", max);
            weatherForecastDataMap.putDouble("low", min);
            weatherForecastDataMap.putString("cond",condition);
            weatherForecastDataMap.putInt("id",weatherId);
            weatherForecastPutDataMapRequest.setUrgent();

            Wearable.DataApi.putDataItem(mGoogleApiClient,
                    weatherForecastPutDataMapRequest.asPutDataRequest());

        }

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("app Service",connectionResult.toString());
    }
}
