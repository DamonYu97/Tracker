package com.example.damon.tracker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;


public class MessageActivity extends AppCompatActivity {
    private ImageButton backButton;
    private List<String> messages;
    private RecyclerView messagesRView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        backButton = (ImageButton)findViewById(R.id.message_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        messages = new ArrayList<String>();
        String message1 = R.drawable.newspaper + ",资讯" + ",最新设备已上线," + "5月2日";
        String message2 = R.drawable.system + ",系统消息！" + ",5台设备升级完成," + "5月5日";
        messages.add(message1);
        messages.add(message2);
        MessageRecycleAdapter messageRecycleAdapter = new MessageRecycleAdapter(messages);
        messagesRView = (RecyclerView)findViewById(R.id.message_recycle);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        messagesRView.setLayoutManager(linearLayoutManager);
        messagesRView.setAdapter(messageRecycleAdapter);
    }
}
