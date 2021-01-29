package com.example.digibarter;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.digibarter.adapter.ChatAdapter;
import com.example.digibarter.model.ChatDetails;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    EditText editText;
    Button send;
    private DatabaseReference databaseReference1;
    private DatabaseReference databaseReference2;
    private DatabaseReference userReference1;
    private DatabaseReference userReference2;
    private ListView listView;
    private List<ChatDetails> chatDetails = new ArrayList<>();

    String firstUser;
    String secondUser;
    public ChatActivity() {
         firstUser = "111";
         secondUser = "110";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        editText = findViewById(R.id.edittext_chatbox);
        send = findViewById(R.id.button_chatbox_send);
        listView = findViewById(R.id.messageList);

        firstUser = String.valueOf(getIntent().getIntExtra("firstPartyUserId", -1));
        secondUser = String.valueOf(getIntent().getIntExtra("secondPartUserId", -1));

        this.getSupportActionBar().setTitle(getIntent().getStringExtra("targetUserName"));

        databaseReference1 = FirebaseDatabase.getInstance().getReference(firstUser + "_" + secondUser);
        databaseReference2 = FirebaseDatabase.getInstance().getReference(secondUser + "_" + firstUser);
        userReference1 = FirebaseDatabase.getInstance().getReference(firstUser+"/"+secondUser);
        userReference2 = FirebaseDatabase.getInstance().getReference(secondUser+"/"+firstUser);

        ChatAdapter chatAdapter = new ChatAdapter(this, chatDetails, firstUser, secondUser);
        listView.setAdapter(chatAdapter);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatDetails chatDetails = new ChatDetails();
                chatDetails.setMessage(editText.getText().toString());
                chatDetails.setUser(firstUser);

                Calendar calendar = Calendar.getInstance();
                String time  = calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
                chatDetails.setTime(time);

                chatDetails.setDate(calendar.get(Calendar.DATE) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR));

                databaseReference1.push().setValue(chatDetails);
                databaseReference2.push().setValue(chatDetails);
                userReference1.child("lastMessage").setValue(chatDetails.getMessage());
                userReference2.child("lastMessage").setValue(chatDetails.getMessage());
                editText.setText("");
            }
        });


        databaseReference1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ChatDetails chatDetail = snapshot.getValue(ChatDetails.class);
                chatDetails.add(chatDetail);
                listView.invalidateViews();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}