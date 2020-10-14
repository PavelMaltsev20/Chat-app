package com.example.pavel.chatapp.AdaptersAndModulus;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.pavel.chatapp.AdaptersAndModulus.Items.Message;
import com.example.pavel.chatapp.AdaptersAndModulus.Items.MyUser;
import com.example.pavel.chatapp.MainActivities.ChatWithUserActivity;
import com.example.pavel.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private boolean isLastChatsFrag;
    private List<MyUser> myUserList;
    private String lastMessage;
    private ViewHolder holder;
    private Context context;
    private MyUser myUser;

    public UserAdapter(Context context, List<MyUser> myUserList, boolean isLastChatsFrag) {
        this.context = context;
        this.isLastChatsFrag = isLastChatsFrag;
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

        setUserName();
        setUserProfileImage();
        setStatusOfCurrentUser();
        setLasUserMessage();
        setOnUserItemClickListener(myUserList.get(position));
    }

    private void setUserName() {
        holder.username_tv.setText(myUser.getUsername());
    }

    private void setUserProfileImage() {
        if (myUser.getImageURL().equals("default")) {
            holder.profileImage_iv.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_user_profile));
        } else {
            Glide.with(context)
                    .load(myUser.getImageURL())
                    .into(holder.profileImage_iv);
        }
    }

    private void setStatusOfCurrentUser() {
        //Check if it 'Last Chats' fragment
        if (isLastChatsFrag) {
            holder.userStatus_iv.setVisibility(View.VISIBLE);
            setStatusListenerOfCurrentItem(myUser, holder.userStatus_iv);
        } else {//If it not 'Last chats' frag, so it 'Search frag'
            holder.userStatus_iv.setVisibility(View.GONE);
        }
    }

    private void setStatusListenerOfCurrentItem(final MyUser myUser, final ImageView userStatus_iv) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    if (snapshot.child("id").getValue().toString().equals(myUser.getId())) {
                        String tempStatus = snapshot.child("status").getValue().toString();
                        if (!tempStatus.equals(myUser.getStatus())) {
                            myUser.setStatus(tempStatus);
                        }

                        if (myUser.getStatus().equals("online")) {
                            userStatus_iv.setBackground(ContextCompat.getDrawable(context, R.drawable.status_online));
                        } else {
                            userStatus_iv.setBackground(ContextCompat.getDrawable(context, R.drawable.status_offline));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    private void setLasUserMessage() {
        if (isLastChatsFrag) {
            holder.newMessage_iv.setVisibility(View.GONE);
            setMessageListenerOfCurrentItem(myUser.getId(), holder.lastMessage_tv,  holder.newMessage_iv);
        } else {
            holder.lastMessage_tv.setVisibility(View.GONE);
            holder.newMessage_iv.setVisibility(View.GONE);
        }
    }

    private void setMessageListenerOfCurrentItem(final String userid, final TextView last_msg, final ImageView newMessage_iv) {
        lastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {



                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    //Check if this message belongs to our user
                    if (message.getReceiver().equals(firebaseUser.getUid()) && message.getSender().equals(userid) ||
                            message.getReceiver().equals(userid) && message.getSender().equals(firebaseUser.getUid())) {

                        lastMessage = message.getMessage();
                        message.setSeen(Boolean.parseBoolean(snapshot.child("isSeen").getValue().toString()));

                        notifyUseAboutNewMessage(message,newMessage_iv);

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

    private void notifyUseAboutNewMessage(Message message, ImageView newMessage_iv) {
        if(!message.getSeen() && message.getReceiver().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            newMessage_iv.setVisibility(View.VISIBLE);
        }else{
            newMessage_iv.setVisibility(View.GONE);
        }
    }

    private void setOnUserItemClickListener(final MyUser chatWithThisUser) {
        holder.parent_cl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatWithUserActivity.class);
                intent.putExtra("userId", chatWithThisUser.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return myUserList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout parent_cl;
        private TextView username_tv, lastMessage_tv;
        private ImageView profileImage_iv, userStatus_iv, newMessage_iv;

        public ViewHolder(View itemView) {
            super(itemView);
            parent_cl = itemView.findViewById(R.id.itemUser);
            username_tv = itemView.findViewById(R.id.userItem_TV_userName);
            lastMessage_tv = itemView.findViewById(R.id.userItem_TV_last_msg);
            profileImage_iv = itemView.findViewById(R.id.userItem_IV_profileImage);

            userStatus_iv = itemView.findViewById(R.id.userItem_IV_userStatus);
            newMessage_iv = itemView.findViewById(R.id.userItem_IV_newMessage);
        }
    }
}














