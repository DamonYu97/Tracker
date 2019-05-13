package com.example.damon.tracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameEdit,passwordEdit;
    private Button loginButton;
    private String username,password,userInfo,userID,userPicUrl;
    private OkHttpClient okHttpClient;
    public static String baseUrl="http://47.106.104.159:8080/Tracker/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //check whether it is the first login
        boolean isFirst=false;
        SharedPreferences preferences=getSharedPreferences("USER",MODE_PRIVATE);
        isFirst=preferences.getBoolean("isFirst",true);
        okHttpClient=new OkHttpClient();
        if(!isFirst){ //if it is not the first login
            username = preferences.getString("username","");
            setLastLogin();
        }else{
            usernameEdit=(EditText)findViewById(R.id.login_username);
            passwordEdit=(EditText)findViewById(R.id.login_password);
            loginButton=(Button)findViewById(R.id.login_sign_in);
            //Login
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //read username and password
                    username = usernameEdit.getText().toString().trim();
                    password = passwordEdit.getText().toString().trim();
                    if ((username.equals("") || password.equals(""))){
                        Toast.makeText(LoginActivity.this,"账号或密码不能为空",Toast.LENGTH_SHORT).show();
                    }else{
                        login();
                    }

                }
            });

        }

    }
    public void login(){
        //check the username and password in database through web service
        Request.Builder builder=new Request.Builder();
        Request request = builder.get().url(baseUrl+"login?account="+username+"&password="+password).build(); //request Server with username and password
        Call call=okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Login Failure!",e.getMessage());
                Looper.prepare();
                Toast.makeText(LoginActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                userInfo=response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(!userInfo.equals("null")){
                            Scanner in = new Scanner(userInfo);
                            userID = in.next();
                            userPicUrl = in.next();
                            //store user information
                            SharedPreferences preferences=getSharedPreferences("USER",MODE_PRIVATE);
                            SharedPreferences.Editor editor=preferences.edit();
                            editor.putString("username",username);
                            editor.putString("userID",userID);
                            editor.putString("userPicUrl",userPicUrl);
                            editor.putBoolean("isFirst",false);
                            editor.commit();
                            setLastLogin();;
                        }else{
                            Toast.makeText(LoginActivity.this,"账号或密码错误",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }

        });
    }

    public void setLastLogin(){
        //get current time
        DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = dateFormat.format(Calendar.getInstance().getTime());
        //send to server
        Request.Builder builder=new Request.Builder();
        final Request request = builder.get().url(baseUrl+"setLastLogin?currentTime="+date+"&account="+username).build();
        Call call=okHttpClient.newCall(request);
        Log.e("username",username);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Login Failure!",e.getMessage());
                Looper.prepare();
                Toast.makeText(LoginActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
                //release user login status
                SharedPreferences preferences=getSharedPreferences("USER",MODE_PRIVATE);
                SharedPreferences.Editor editor=preferences.edit();
                editor.putBoolean("isFirst",true);
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result.equals("success")) {
                            Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            //release user login status
                            SharedPreferences preferences=getSharedPreferences("USER",MODE_PRIVATE);
                            SharedPreferences.Editor editor=preferences.edit();
                            editor.putBoolean("isFirst",true);
                        }
                    }
                });
            }
        });
    }
}
