package com.foo.fuckyou;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerViewChatList;
    private Button buttonCreateChatRoom;
    private ArrayList<String> chatRoomList;
    private ChatListAdapter chatListAdapter;

    // 테스트용 사용자 ID
    private String currentUserId = "test_user_1";
    private String friendId = "test_user_2";

    private DatabaseReference userChatsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerViewChatList = findViewById(R.id.recyclerViewChatList);
        recyclerViewChatList.setLayoutManager(new LinearLayoutManager(this));

        buttonCreateChatRoom = findViewById(R.id.buttonCreateChatRoom);
        chatRoomList = new ArrayList<>();
        chatListAdapter = new ChatListAdapter(chatRoomList);
        recyclerViewChatList.setAdapter(chatListAdapter);

        // Firebase 참조 초기화
        userChatsRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId).child("chats");

        // 기존 채팅방 불러오기
        loadChatRooms();

        // 채팅방 생성 버튼 클릭 이벤트
        buttonCreateChatRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createChatRoom(friendId);  // friendId는 고정된 테스트용 값입니다.
            }
        });
    }

    private void loadChatRooms() {
        userChatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatRoomList.clear();
                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    String chatRoomId = chatSnapshot.getKey();
                    chatRoomList.add(chatRoomId);
                }
                chatListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 오류 처리
            }
        });
    }

    private void createChatRoom(String friendId) {
        String chatRoomId = ChatHelper.generateChatRoomId(currentUserId, friendId);
        userChatsRef.child(chatRoomId).setValue(true); // 현재 사용자 채팅방 추가
        FirebaseDatabase.getInstance().getReference("users").child(friendId).child("chats").child(chatRoomId).setValue(true); // 친구의 채팅방 추가

        Intent intent = new Intent(this, ChatRoomActivity.class);
        intent.putExtra("chatRoomId", chatRoomId);
        startActivity(intent);
    }
}
