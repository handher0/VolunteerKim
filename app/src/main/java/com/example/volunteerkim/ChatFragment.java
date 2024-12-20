package com.example.volunteerkim;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private static final String TAG = "ChatFragment";
    private static final String ARG_USER1ID = "user1id";
    private String user1id; // 현재 로그인한 사용자 닉네임

    private RecyclerView recyclerViewChatList;
    private ChatListAdapter chatListAdapter;

    public ChatFragment() {
        // Required empty public constructor
    }

    /**
     * ChatFragment의 newInstance 메서드. MainActivity에서 닉네임 전달 시 사용.
     */
    public static ChatFragment newInstance(String user1id) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER1ID, user1id);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.chat_activity_main, container, false); // XML 연결
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user1id = getArguments().getString(ARG_USER1ID);
            if (user1id != null) {
                Log.d(TAG, "Received user1id in ChatFragment: " + user1id);
            } else {
                Log.e(TAG, "user1id is null in ChatFragment");
            }
        } else {
            Log.e(TAG, "getArguments() is null in ChatFragment");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button buttonAddFriends = view.findViewById(R.id.buttonAddFriends);
        if (buttonAddFriends != null) {
            // 친구 추가 버튼 클릭 시
            buttonAddFriends.setOnClickListener(v -> {
                ChatAddFriendFragment addFriendFragment = ChatAddFriendFragment.newInstance(user1id);
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(
                                R.anim.slide_in_right,  // 새 화면 들어올 때
                                R.anim.slide_out_left,  // 현재 화면 나갈 때
                                R.anim.slide_in_left,   // 뒤로 가기 시 들어올 때
                                R.anim.slide_out_right  // 뒤로 가기 시 나갈 때
                        )
                        .replace(R.id.fragment_container, addFriendFragment)
                        .addToBackStack(null)
                        .commit();

            });
        }

        // RecyclerView 설정
        recyclerViewChatList = view.findViewById(R.id.recyclerViewChatList);
        recyclerViewChatList.setLayoutManager(new LinearLayoutManager(getContext()));
        chatListAdapter = new ChatListAdapter(new ArrayList<>(), user1id); // Adapter 생성
        recyclerViewChatList.setAdapter(chatListAdapter);

        // 채팅방 항목 클릭 시
        chatListAdapter.setOnItemClickListener(chatRoomId -> {
            openChatRoom(chatRoomId);
        });

        loadChatRooms(); // Firebase에서 채팅방 데이터 로드
    }


    /**
     * 채팅방을 열고 ChatRoomFragment로 이동
     */
    private void openChatRoom(String user2id) {
        String chatRoomId = ChatHelper.generateChatRoomId(user1id, user2id);
        Log.d(TAG, "Opening ChatRoomFragment with ID: " + chatRoomId + " for user: " + user1id);
        ChatRoomFragment chatRoomFragment = ChatRoomFragment.newInstance(chatRoomId, user1id);

        // 프래그먼트 전환 시 애니메이션 적용
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,  // 뒤로가기 시 들어올 때
                        R.anim.slide_out_left   // 뒤로가기 시 나갈 때
                )
                .replace(R.id.fragment_container, chatRoomFragment) // 새 프래그먼트로 교체
                .addToBackStack(null) // 뒤로 가기 스택에 추가
                .commit();
    }

    /**
     * Firebase에서 채팅방 목록을 가져오고 RecyclerView를 업데이트
     */
    private void loadChatRooms() {
        DatabaseReference userChatRef = FirebaseDatabase.getInstance().getReference("users").child(user1id).child("chats");
        Log.d(TAG, "Loading chat rooms for user1id: " + user1id);

        userChatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> chatPartners = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                        String chatRoomId = chatSnapshot.getKey(); // 채팅방 ID 가져오기
                        if (chatRoomId != null) {
                            String[] ids = chatRoomId.split("_");
                            String user2id = ids[0].equals(user1id) ? ids[1] : ids[0]; // 상대방 ID 추출
                            chatPartners.add(user2id);
                            Log.d(TAG, "Chat room ID: " + chatRoomId + ", Partner ID: " + user2id);
                        }
                    }
                    Log.d(TAG, "Loaded chat partners: " + chatPartners);
                } else {
                    Log.e(TAG, "No chat rooms exist for user1id: " + user1id);
                }
                chatListAdapter.updateData(chatPartners); // RecyclerView 업데이트
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load chat rooms", error.toException());
            }
        });
    }

}
