package com.example.damon.tracker;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.qiaomu.lib.itemtouchhelper.ItemTouchActionCallback;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UsersRecycleAdapter extends RecyclerView.Adapter<UsersRecycleAdapter.VH> implements ItemTouchActionCallback {

    public static class VH extends RecyclerView.ViewHolder{
        public final CircleImageView profile;
        public final TextView usernameTV;
        public final TextView nameTV;
        public final TextView mobileTV;
        public final RelativeLayout relativeLayout;
        public final TextView passwordButton,deleteButton;
        public VH(View v) {
            super(v);
            profile = (CircleImageView)v.findViewById(R.id.item_account_profile);
            usernameTV = (TextView)v.findViewById(R.id.item_account_username);
            nameTV = (TextView)v.findViewById(R.id.item_account_name);
            mobileTV = (TextView)v.findViewById(R.id.item_account_mobile);
            relativeLayout = (RelativeLayout)v.findViewById(R.id.item_content);
            passwordButton = (TextView) v.findViewById(R.id.item_update_password);
            deleteButton = (TextView) v.findViewById(R.id.item_delete);
        }
    }

    private List<String> mDatas;
    private OkHttpClient okHttpClient;
    private String baseUrl;
    private Activity activity;
    private String currentAccount;
    public UsersRecycleAdapter(List<String> data,Activity activity) {
        this.mDatas = data;
        this.activity = activity;
        okHttpClient = new OkHttpClient();
        baseUrl = LoginActivity.baseUrl;
        SharedPreferences preferences = activity.getSharedPreferences("USER",Context.MODE_PRIVATE);
        currentAccount = preferences.getString("username","");
    }

    @Override
    public void onBindViewHolder(VH holder, final int position) {
        Scanner in =new Scanner(mDatas.get(position));
        in.useDelimiter(",");
        final String username = in.next();
        final String name = in.next();
        final String mobile = in.next();
        final String path = in.next();
        //more info
        final String telephone = in.next();
        final String email = in.next();
        final String createTime =in.next();
        final String lastLogin = in.next();
        final String roleNo = in.next();
        holder.usernameTV.setText(username);
        holder.nameTV.setText(name);
        holder.mobileTV.setText(mobile);
        if (!path.equals("")){
            setProfile(path,holder.profile);
        }
        if (username.equals(currentAccount)) {
            holder.deleteButton.setVisibility(View.GONE);
        }
        holder.passwordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText newPasswordEdit = new EditText(activity);
                new AlertDialog.Builder(activity).setTitle("请输入新密码")
                        .setView(newPasswordEdit)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String password = newPasswordEdit.getText().toString().trim();
                                if (!password.equals("")) {
                                    //update password
                                    updatePassword(username,password);
                                    Toast.makeText(activity,"密码重置成功",Toast.LENGTH_SHORT).show();
                                    if (username.equals(currentAccount)) {
                                        //jump to login page
                                        SharedPreferences preferences = activity.getSharedPreferences("USER",Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = preferences.edit();
                                        editor.putBoolean("isFirst",true);
                                        editor.putString("username","");
                                        editor.putString("userID","");
                                        editor.commit();
                                        Intent signOut = new Intent(activity,LoginActivity.class);
                                        activity.startActivity(signOut);
                                        activity.finish();
                                    }
                                } else {
                                    Toast.makeText(activity,"密码重置失败",Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).setNegativeButton("取消",null).show();
            }
        });
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //remove account and delete it
                mDatas.remove(position);
                notifyItemRemoved(position);
                deleteAccount(username);
            }
        });
        //click the account info to see more detail or edit it
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity,UsersInfoActivity.class);
                intent.putExtra("username",username);
                intent.putExtra("profile",path);
                intent.putExtra("name",name);
                intent.putExtra("mobile",mobile);
                intent.putExtra("telephone",telephone);
                intent.putExtra("email",email);
                intent.putExtra("createTime",createTime);
                intent.putExtra("lastLogin",lastLogin);
                intent.putExtra("roleNo",roleNo);
                activity.startActivity(intent);
            }
        });
        /*holder.title.setText(mDatas.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });*/
    }

    @Override
    public View getContentView(RecyclerView.ViewHolder holder) {
        VH vh = (VH) holder;
        return vh.relativeLayout;
    }

    @Override
    public int getMenuWidth(RecyclerView.ViewHolder holder) {
        VH vh = (VH) holder;
        return vh.passwordButton.getWidth() + vh.deleteButton.getWidth();
    }

    @Override
    public void onMove(int fromPos, int toPos) {
        Collections.swap(mDatas, fromPos, toPos);
        notifyItemMoved(fromPos, toPos);
    }

    @Override
    public void onMoved(int fromPos, int toPos) {
        //move action finished
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account, parent, false);
        return new VH(v);
    }

    public void setProfile(String path, final CircleImageView profile) {
        Request.Builder builder = new Request.Builder();
        Request request = builder.get().url(baseUrl+"profile/"+path).build(); //request Server with user id
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Looper.prepare();
                Toast.makeText(activity,"网络错误",Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e("download:","waiting");
                InputStream is = response.body().byteStream();
                final Bitmap bitmap = BitmapFactory.decodeStream(is);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        profile.setImageBitmap(bitmap);
                    }
                });
                Log.e("download:","success");
            }
        });
    }

    public void deleteAccount(String username) {
        Request.Builder builder = new Request.Builder();
        Request request = builder.get().url(baseUrl+"deleteAccount?account="+username).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Looper.prepare();
                Toast.makeText(activity,"网络错误",Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

    public void updatePassword(String username,String password) {
        Request.Builder builder = new Request.Builder();
        Request request = builder.get().url(baseUrl+"updatePassword?account="+username+"&password="+password).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Looper.prepare();
                Toast.makeText(activity,"网络错误",Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

}
