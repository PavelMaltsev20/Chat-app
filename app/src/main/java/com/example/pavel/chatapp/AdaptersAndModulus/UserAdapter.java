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

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
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

        setUserName();
        setUserProfileImage();
        checkIfThoseUsersHaveConversation();
        checkIfCurrentUserOnline();
        setOnItemClickListener(myUserList.get(position));
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

    private void checkIfThoseUsersHaveConversation() {
        if (isChat) {
            displayLastMessage(myUser.getId(), holder.lastMessage_tv);
        } else {
            holder.lastMessage_tv.setVisibility(View.GONE);
        }
    }

    private void displayLastMessage(final String userid, final TextView last_msg) {
        lastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");

        if (firebaseUser != null) {
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Message message = snapshot.getValue(Message.class);
                        //Check if this message belongs to our user and to whoever he is talking
                        if (message.getReceiver().equals(firebaseUser.getUid()) && message.getSender().equals(userid) ||
                                message.getReceiver().equals(userid) && message.getSender().equals(firebaseUser.getUid())) {
                            lastMessage = message.getMessage();
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

    private void setOnItemClickListener(final MyUser chatWithThisUser) {
        holder.parent.setOnClickListener(new View.OnClickListener() {
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

        private ConstraintLayout parent;
        private TextView username_tv, lastMessage_tv;
        private ImageView profile_image, img_on, img_off;

        public ViewHolder(View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.itemUser);
            username_tv = itemView.findViewById(R.id.userItem_TV_userName);
            lastMessage_tv = itemView.findViewById(R.id.userItem_TV_last_msg);
            profile_image = itemView.findViewById(R.id.userItem_IV_profileImage);

            img_on = itemView.findViewById(R.id.userItem_IV_img_on);
            img_off = itemView.findViewById(R.id.userItem_IV_img_off);
        }
    }
}














