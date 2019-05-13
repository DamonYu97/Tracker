package com.example.damon.tracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UsersInfoActivity extends AppCompatActivity {
    private String username,profile,name,mobile,telephone,email,createTime,lastLogin,roleNo;
    private String newUrl,newProfile,newName,newMobile,newTelephone,newEmail,newRoleNo;
    private EditText usernameEdit,nameEdit,mobileEdit,telephoneEdit,emailEdit,createTimeEdit,lastLoginEdit;
    private Spinner roleSpinner;
    private ImageButton backButton;
    private CircleImageView profileImageView;
    private Button functionButton;
    private OkHttpClient okHttpClient;
    private ArrayList<String> imagePath;
    private final int REQUEST_CAMERA = 1;
    private String baseUrl = LoginActivity.baseUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_info);

        backButton = (ImageButton)findViewById(R.id.userInfo_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        //get data
        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        profile = intent.getStringExtra("profile");
        name = intent.getStringExtra("name");
        mobile = intent.getStringExtra("mobile");
        telephone = intent.getStringExtra("telephone");
        email = intent.getStringExtra("email");
        createTime = intent.getStringExtra("createTime");
        lastLogin = intent.getStringExtra("lastLogin");
        roleNo = intent.getStringExtra("roleNo");

        okHttpClient = new OkHttpClient();
        profileImageView = (CircleImageView)findViewById(R.id.userInfo_profile);
        usernameEdit = (EditText)findViewById(R.id.userInfo_username);
        nameEdit = (EditText)findViewById(R.id.userInfo_name);
        mobileEdit = (EditText)findViewById(R.id.userInfo_mobilePhone);
        telephoneEdit = (EditText)findViewById(R.id.userInfo_telephone);
        emailEdit = (EditText)findViewById(R.id.userInfo_email);
        createTimeEdit = (EditText)findViewById(R.id.userInfo_createTime);
        lastLoginEdit = (EditText)findViewById(R.id.userInfo_lastLogin);
        roleSpinner = (Spinner)findViewById(R.id.userInfo_role);
        int position =0; //record the position in role_list
        switch (roleNo){
            case "0000": //管理员
                position = 1;
                break;
            case "0001": //客户
                position = 0;
                break;
            case "0002": //测试人员
                position = 2;
                break;
        }
        //set data
        if (!profile.equals("")){
            setProfile();
        }
        usernameEdit.setHint(username);
        nameEdit.setHint(name);
        mobileEdit.setHint(mobile);
        telephoneEdit.setHint(telephone);
        emailEdit.setHint(email);
        createTimeEdit.setHint(createTime);
        if (!lastLogin.equals("null")){
            lastLoginEdit.setHint(lastLogin);
        }
        roleSpinner.setSelection(position);
        roleSpinner.setEnabled(false);
        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String role = adapterView.getSelectedItem().toString();
                switch (role){
                    case "管理员":
                        newRoleNo = "0000";
                        break;
                    case "客户":
                        newRoleNo = "0001";
                        break;
                    case "测试角色":
                        newRoleNo = "0002";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                newRoleNo = "";
            }
        });
        profileImageView.setEnabled(false);
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imagePath = new ArrayList<String>();
                selectImage();
            }
        });
        //edit and confirm
        functionButton = (Button)findViewById(R.id.userInfo_button);
        functionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (functionButton.getText().toString().equals("编辑")) {
                    nameEdit.setClickable(true);
                    nameEdit.setFocusableInTouchMode(true);
                    mobileEdit.setFocusable(true);
                    mobileEdit.setFocusableInTouchMode(true);
                    telephoneEdit.setFocusable(true);
                    telephoneEdit.setFocusableInTouchMode(true);
                    emailEdit.setFocusable(true);
                    emailEdit.setFocusableInTouchMode(true);
                    roleSpinner.setEnabled(true);
                    profileImageView.setEnabled(true);
                    functionButton.setText("提交");
                } else {
                    //submit all updated information
                    String info = "";
                    newName = nameEdit.getText().toString();
                    newTelephone = telephoneEdit.getText().toString();
                    newMobile = mobileEdit.getText().toString();
                    newEmail = emailEdit.getText().toString();

                    if (!newName.equals("")){
                        info += ",name,"+newName;
                    }
                    if (!newTelephone.equals("")){
                        info += ",telephone,"+newTelephone;
                    }
                    if (!newMobile.equals("")){
                        info += ",mobile,"+newMobile;
                    }
                    if (!newEmail.equals("")){
                        info += ",email,"+newEmail;
                    }
                    if (!(newRoleNo.equals("")||newRoleNo.equals(roleNo))) {
                        info += ",roleNo,"+newRoleNo;
                    }
                    newUrl = "";
                    //add image url
                    if ((imagePath != null) && (imagePath.get(0) != null)) {
                        newUrl = imagePath.get(0);
                        int length = newUrl.length();
                        newProfile = username+newUrl.substring(length-4);
                        info += ",picurl,"+newProfile;
                    }
                    if (!info.equals("")) { //update information
                        okHttpClient = new OkHttpClient();
                        Request.Builder builder = new Request.Builder();
                        Request request = builder.get().url(baseUrl+"updateUserInfo?userUpdateInfo="+info+"&account="+username).build();
                        Call call = okHttpClient.newCall(request);
                        call.enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Looper.prepare();
                                Toast.makeText(UsersInfoActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                final String result = response.body().string();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (result.equals("success")) {
                                            if (!newUrl.equals("")){
                                                File file = new File(newUrl);
                                                RequestBody requestBody = RequestBody.create(MediaType.parse("application/octet-stream"),file);
                                                Request.Builder build = new Request.Builder();
                                                Request re = build.url(baseUrl + "postImage?imageUrl="+newProfile).post(requestBody).build();
                                                okHttpClient.newCall(re).enqueue(new Callback() {
                                                    @Override
                                                    public void onFailure(Call call, IOException e) {
                                                        Looper.prepare();
                                                        Toast.makeText(UsersInfoActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
                                                        Looper.loop();
                                                    }

                                                    @Override
                                                    public void onResponse(Call call, Response response) throws IOException {

                                                    }
                                                });
                                            }
                                            finish();
                                            Toast.makeText(UsersInfoActivity.this,"提交成功",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });
                    }
                }
            }
        });
    }

    public void setProfile() {
        Request.Builder builder = new Request.Builder();
        Request request = builder.get().url(baseUrl+"profile/"+profile).build(); //request Server with user id
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Looper.prepare();
                Toast.makeText(UsersInfoActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e("download:","waiting");
                InputStream is = response.body().byteStream();
                final Bitmap bitmap = BitmapFactory.decodeStream(is);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        profileImageView.setImageBitmap(bitmap);
                    }
                });
                Log.e("download:","success");
            }
        });
    }
    private void selectImage(){
        imagePath.clear();
        final String[] items={"拍照","图库","取消"};
        AlertDialog.Builder builder=new AlertDialog.Builder(UsersInfoActivity.this);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(items[i].equals("拍照")){
                    Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent,REQUEST_CAMERA);
                }else if(items[i].equals("图库")){
                    FilePickerBuilder.getInstance()
                            .setSelectedFiles(imagePath).setMaxCount(1)
                            .setActivityTheme(R.style.AppTheme)
                            .pickPhoto(UsersInfoActivity.this);
                }else if(items[i].equals("取消")){
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == FilePickerConst.REQUEST_CODE) {
                imagePath.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_PHOTOS));
                try {
                    Picasso.with(this).load(Uri.fromFile(new File(imagePath.get(0)))).placeholder(R.drawable.man).into(profileImageView);
                } catch (Exception e){

                }

            } else if (requestCode == REQUEST_CAMERA) {
                Bundle bundle = data.getExtras();
                final Bitmap bmp = (Bitmap) bundle.get("data");
                File appDir = new File(Environment.getExternalStorageDirectory(), "Pictures");
                if (!appDir.exists()) {
                    appDir.mkdir();
                }
                String fileName = System.currentTimeMillis() + ".jpg";
                File file = new File(appDir, fileName);
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    profileImageView.setImageBitmap(bmp);
                    fos.flush();
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // add image file into system storage
                try {
                    MediaStore.Images.Media.insertImage(getContentResolver(),
                            file.getAbsolutePath(), fileName, null);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                String path = "/storage/emulated/0/Pictures/" + fileName;
                imagePath.add(path);
            }
        }
    }
}
