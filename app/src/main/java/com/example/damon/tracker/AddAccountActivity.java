package com.example.damon.tracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class AddAccountActivity extends AppCompatActivity {
    private Spinner spinner;
    private OkHttpClient okHttpClient;
    private String baseUrl = LoginActivity.baseUrl;
    private String path,url;
    private final int REQUEST_CAMERA = 1;
    private ArrayList<String> imagePath;
    private CircleImageView profileImage;
    private String username,password,verification,name,telephone,mobilePhone,email,roleNo;
    private EditText usernameEdit,passwordEdit,verifyEdit,nameEdit,telephoneEdit,mobileEdit,emailEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);
        //set back button
        ImageButton backButton = (ImageButton) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        //set profile image
        profileImage = (CircleImageView)findViewById(R.id.add_profile);
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imagePath = new ArrayList<String>();
                selectImage();
            }
        });
        //initialize edit text
        usernameEdit = (EditText)findViewById(R.id.add_username);
        passwordEdit = (EditText)findViewById(R.id.add_password);
        verifyEdit = (EditText)findViewById(R.id.add_verification);
        nameEdit = (EditText)findViewById(R.id.add_name);
        telephoneEdit = (EditText)findViewById(R.id.add_telephone);
        mobileEdit = (EditText)findViewById(R.id.add_mobilePhone);
        emailEdit = (EditText)findViewById(R.id.add_email);
        //set role
        spinner = (Spinner) findViewById(R.id.add_role);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String role = adapterView.getSelectedItem().toString();
                switch (role){
                    case "管理员":
                        roleNo = "0000";
                        break;
                    case "客户":
                        roleNo = "0001";
                        break;
                    case "测试角色":
                        roleNo = "0002";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                roleNo = "0001"; //set role as general user by default
            }
        });
        //submit the user information
        Button submitButton = (Button)findViewById(R.id.button_add_account);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String info = "";
                //get information
                username = usernameEdit.getText().toString().trim();
                password = passwordEdit.getText().toString().trim();
                verification = verifyEdit.getText().toString();
                name = nameEdit.getText().toString();
                telephone = telephoneEdit.getText().toString();
                mobilePhone = mobileEdit.getText().toString();
                email = emailEdit.getText().toString();
                //check whether important information is filled
                boolean filled = true;
                if (username.equals("")){
                    usernameEdit.setHint("必填");
                    usernameEdit.setHintTextColor(Color.parseColor("#fa3636"));
                    filled = false;
                }
                if (password.equals("")){
                    passwordEdit.setHint("必填");
                    passwordEdit.setHintTextColor(Color.parseColor("#fa3636"));
                    filled = false;
                }
                if (verification.equals("")){
                    verifyEdit.setHint("必填");
                    verifyEdit.setHintTextColor(Color.parseColor("#fa3636"));
                    filled = false;
                }
                if (name.equals("")){
                    nameEdit.setHint("必填");
                    nameEdit.setHintTextColor(Color.parseColor("#fa3636"));
                    filled = false;
                }
                if (mobilePhone.equals("")){
                    mobileEdit.setHint("必填");
                    mobileEdit.setHintTextColor(Color.parseColor("#fa3636"));
                    filled = false;
                }
                if (email.equals("")){
                    emailEdit.setHint("必填");
                    emailEdit.setHintTextColor(Color.parseColor("#fa3636"));
                    filled = false;
                }
                if (!filled){
                    return;
                }
                //verify password
                if (!verification.equals(password)) {
                    Toast.makeText(AddAccountActivity.this,"密码不一致",Toast.LENGTH_SHORT).show();
                    return;
                }
                info = "username,"+username+",password,"+password+",name,"+name+",mobilePhone,"+mobilePhone+",email,"+email+",roleNo,"+roleNo;
                if (!telephone.equals("")) {
                    info += ",telephone,"+telephone;
                }
                Log.e("info",info);
                url = "";
                //add image url
                if ((imagePath != null) && (imagePath.get(0) != null)) {
                    url = imagePath.get(0);
                    int length = url.length();
                    path = username+url.substring(length-4);
                    info += ",url,"+path;
                }
                //get current time
                DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String date = dateFormat.format(Calendar.getInstance().getTime());
                info += ",createTime,"+date;
                Log.e("info",info);
                //submit all the information
                okHttpClient = new OkHttpClient();
                Request.Builder builder = new Request.Builder();
                Request request = builder.get().url(baseUrl+"submitUserInfo?userInfo="+info).build();
                Call call = okHttpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Looper.prepare();
                        Toast.makeText(AddAccountActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final String result = response.body().string();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (result.equals("existed")) {
                                    Toast.makeText(AddAccountActivity.this,"用户名已存在",Toast.LENGTH_SHORT).show();
                                } else if (result.equals("success")) {
                                    if (!url.equals("")){
                                        File file = new File(url);
                                        RequestBody requestBody = RequestBody.create(MediaType.parse("application/octet-stream"),file);
                                        Request.Builder build = new Request.Builder();
                                        Request re = build.url(baseUrl + "postImage?imageUrl="+path).post(requestBody).build();
                                        okHttpClient.newCall(re).enqueue(new Callback() {
                                            @Override
                                            public void onFailure(Call call, IOException e) {
                                                Looper.prepare();
                                                Toast.makeText(AddAccountActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
                                                Looper.loop();
                                            }

                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {

                                            }
                                        });
                                    }
                                    finish();
                                    Toast.makeText(AddAccountActivity.this,"提交成功",Toast.LENGTH_SHORT).show();

                                }  else {
                                    Toast.makeText(AddAccountActivity.this,"提交失败",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });

            }
        });
    }
    private void selectImage(){
        imagePath.clear();
        final String[] items={"拍照","图库","取消"};
        AlertDialog.Builder builder=new AlertDialog.Builder(AddAccountActivity.this);
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
                            .pickPhoto(AddAccountActivity.this);
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
                    Picasso.with(this).load(Uri.fromFile(new File(imagePath.get(0)))).placeholder(R.drawable.man).into(profileImage);
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
                    profileImage.setImageBitmap(bmp);
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
