package pn3.sunshineface;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.telecom.Connection;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by pulkitnarwani on 16/03/17.
 */

public class SunshineSync implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener {

    GoogleApiClient mGoogleApiClient;
    MyWatchFace.Engine a;

    public SunshineSync(Context mContext){
        Log.e("SunshineSync","Constructor");
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
        //Wearable.DataApi.addListener(mGoogleApiClient, this);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e("SunshineSync","Connected");
        Wearable.DataApi.addListener(mGoogleApiClient, this);


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("Connection Failed", connectionResult.toString());

    }




    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

        for(DataEvent d : dataEventBuffer ){
            if(d.getDataItem().getUri().toString().contains("/data/weather_forecast")){
                DataMapItem dmi=DataMapItem.fromDataItem(d.getDataItem());
                DataMap dm=dmi.getDataMap();
                Double high=dm.getDouble("high");
                Double low=dm.getDouble("low");
                String cond=dm.getString("cond");
                int id=dm.getInt("id");
                a.onTemperatureFetched(high.toString(),low.toString(),cond,id);

            }
        }

    }

    public void requestWeatherForecast(MyWatchFace.Engine cont) {
        a=cont;
        Log.e("SunshineSync","Request");
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(
                "/sync/weather/" + System.currentTimeMillis());
        putDataMapRequest.setUrgent();
        putDataMapRequest.getDataMap().putInt("aa", 1);
        PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest);
    }

}
