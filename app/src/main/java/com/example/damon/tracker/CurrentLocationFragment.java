package com.example.damon.tracker;



import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.lapism.searchview.Search;
import com.lapism.searchview.widget.SearchView;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class CurrentLocationFragment extends Fragment {

    private MapView mapView;
    private AMap aMap;
    private MarkerOptions markerOptions;
    private SearchView searchView;
    private String searchResult;
    private String uid;
    private OkHttpClient okHttpClient;
    private String baseUrl = LoginActivity.baseUrl;
    private String locationInfo;
    private List<Marker> markers;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_current_location,container,false);
        searchView = (SearchView)view.findViewById(R.id.currentLocation_search);
        searchView.setShadowColor(Color.parseColor("#BFFFFFFF"));
        SharedPreferences preferences = getActivity().getSharedPreferences("USER",Context.MODE_PRIVATE);
        uid = preferences.getString("userID","");
        mapView = (MapView)view.findViewById(R.id.currentLocation_map);
        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();
        markers = new ArrayList<Marker>();
       initialSearch();
        mark();
        searchView.setOnQueryTextListener(new Search.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(CharSequence query) {
                if (Permission.isPermittedMap.get("06000")) { //search request devices created by all users
                    searchResult = query+",,";
                } else {       //search request devices created by current user only
                    searchResult = query+","+uid;
                }
               mark();
                searchView.close();
                return true;
            }

            @Override
            public void onQueryTextChange(CharSequence newText) {

            }
        });
        return view;
    }

    public void mark(){
        okHttpClient = new OkHttpClient();
        Request.Builder builder=new Request.Builder();
        Request request = builder.get().url(baseUrl+"getCurrentLocation?searchResult="+searchResult).build();
        Call call=okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Looper.prepare();
                Toast.makeText(getContext(),"网络错误",Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                locationInfo = response.body().string();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0;i<markers.size();i++){
                            markers.get(i).remove();
                        }
                        Scanner line = new Scanner(locationInfo);
                        line.useDelimiter(">");
                        while (line.hasNext()){
                            Scanner in = new Scanner(line.next());
                            in.useDelimiter(",");
                            String deviceID = in.next();
                            String date = in.next();
                            String latitude = in.next();
                            String longitude = in.next();
                            int battery = in.nextInt();
                            int onOff = in.nextInt();
                            String status = (onOff == 2)?"关":"开";
                            Log.e("location",latitude+" "+longitude);
                            markerOptions = new MarkerOptions();
                            markerOptions.position(new LatLng(Double.parseDouble(latitude),Double.parseDouble(longitude)));
                            markerOptions.title("设备ID: "+deviceID);//设置标题
                            String content = "更新时间: "+date+"\n剩余电量: "+battery+"%\n"+"开关门状态: "+status;
                            markerOptions.snippet(content);//设置内容
                            markers.add(aMap.addMarker(markerOptions));
                        }
                    }
                });
            }
        });
    }

    public void initialSearch() {
        //initialize
        if (Permission.isPermittedMap.get("06000")) { //search all devices
            searchResult = ",,,";
        } else {    //search all devices created by current user only
            searchResult = ",,"+uid;
        }
    }
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onCreate(outState);
    }
    /**
     * 方法必须重写
     */
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

}
