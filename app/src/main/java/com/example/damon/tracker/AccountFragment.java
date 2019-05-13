package com.example.damon.tracker;



import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.qiaomu.lib.itemtouchhelper.ItemTouchHelper;
import com.qiaomu.lib.itemtouchhelper.ItemTouchHelperCallback;

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
public class AccountFragment extends Fragment {

    private String searchResult,usersInfo;
    private RecyclerView recyclerView;
    private String baseUrl = LoginActivity.baseUrl;
    private OkHttpClient okHttpClient;
    private List<String> info;
    private SwipeRefreshLayout swipeRefreshLayout;
    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_account,container,false);
        FloatingActionButton addingFloat = (FloatingActionButton)view.findViewById(R.id.account_add);
        addingFloat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(),AddAccountActivity.class);
                startActivity(intent);
            }
        });

        //get and set all user information according to search result
        searchResult = "";
        recyclerView = (RecyclerView)view.findViewById(R.id.add_recycle);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        //set information from server
        setInfo();
        //refresh
        swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.add_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        searchResult = "";
                        setInfo();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                },1000);

            }
        });
        return view;
    }

    public void setInfo(){
        okHttpClient = new OkHttpClient();
        Request.Builder builder=new Request.Builder();
        Request request = builder.get().url(baseUrl+"searchUsersInfo?searchResult="+searchResult).build();
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
                usersInfo = response.body().string(); //>username,name,mobile,url,>......
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        info = new ArrayList<String>();
                        Scanner in = new Scanner(usersInfo);
                        in.useDelimiter(">");
                        while (in.hasNext()) {
                            info.add(in.next());
                        }
                        UsersRecycleAdapter usersRecycleAdapter = new UsersRecycleAdapter(info, getActivity());
                        recyclerView.setAdapter(usersRecycleAdapter);
                        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(0,usersRecycleAdapter));
                        itemTouchHelper.attachToRecyclerView(recyclerView);
                    }
                });
            }
        });
    }
    public void setSearchResult(String searchResult) {
        this.searchResult = searchResult;
    }

}
