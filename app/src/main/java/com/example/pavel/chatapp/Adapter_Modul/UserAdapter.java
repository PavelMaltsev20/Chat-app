package com.example.pavel.chatapp.Adapter_Modul;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.pavel.chatapp.Adapter_Modul.Items.Message;
import com.example.pavel.chatapp.Adapter_Modul.Items.MyUser;
import com.example.pavel.chatapp.MainActivities.ChatWithUserActivity;
import com.example.pavel.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<MyUser> myUserList;
    private String lastMessage;
    private Context context;
    private boolean isChat;
    private ViewHolder holder;
    private MyUser myUser;

    public UserAdapter(Context context, List<MyUser> myUserList, boolean isChat) {
        this.context = context;
        this.isChat = isChat;
        this.myUserList = myUserList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        setBackgroundUserTheme(view);
        return new ViewHolder(view);
    }

    private void setBackgroundUserTheme(View view) {
        SharedPref sharedPref = new SharedPref(context);
        if (sharedPref.loadNightModeState() == true) {
            view.setBackground(AppCompatResources.getDrawable(context, R.drawable.item_user_theme_blue));
        } else {
            view.setBackground(AppCompatResources.getDrawable(context, R.drawable.item_user_theme_green));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        this.holder = holder;
        myUser = myUserList.get(position);

        setIdToMyUserClass(position);
        setUserName();
        setUserProfileImage();
        displayLastMessageOfSecondUser();
        checkIfCurrentUserOnline();
        setOnItemClickListener();
    }

    private void setIdToMyUserClass(int position) {
        myUser.setId(myUserList.get(position).getId());
    }

    private void setUserName() {
        holder.username_tv.setText(myUser.getUsername());
    }

    private void setUserProfileImage() {
        if (myUser.getImageURL().equals("default")) {
            holder.profile_image.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_user_profile));
        } else {
            Glide.with(context)
                    .load(myUser.getImageURL())
                    .into(holder.profile_image);
        }
    }

    private void displayLastMessageOfSecondUser() {
        if (isChat) {
            lastMessage(myUser.getId(), holder.lastMessage_tv);
        } else {
            holder.lastMessage_tv.setVisibility(View.GONE);
        }
    }

    private void lastMessage(final String userid, final TextView last_msg) {
        lastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message chat = snapshot.getValue(Message.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())) {
                        lastMessage = chat.getMessage();
                    }
                }

                switch (lastMessage) {
                    case "default":
                        last_msg.setText("No message");
                        break;

                    default:
                        last_msg.setText(lastMessage);
                        break;
                }
                lastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void checkIfCurrentUserOnline() {
        if (isChat) {
            if (myUser.getStatus().equals("online")) {
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            } else {
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }
        } else {
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }
    }

    private void setOnItemClickListener() {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatWithUserActivity.class);
                intent.putExtra("userId", myUser.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return myUserList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView username_tv, lastMessage_tv;
        private ImageView profile_image, img_on, img_off;

        public ViewHolder(View itemView) {
            super(itemView);
            username_tv = itemView.findViewById(R.id.userItem_TV_userName);
            lastMessage_tv = itemView.findViewById(R.id.userItem_TV_last_msg);
            profile_image = itemView.findViewById(R.id.userItem_IV_profileImage);

            img_on = itemView.findViewById(R.id.userItem_IV_img_on);
            img_off = itemView.findViewById(R.id.userItem_IV_img_off);

        }
    }
}














