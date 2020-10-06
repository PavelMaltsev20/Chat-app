package com.example.pavel.chatapp.Adapter_Modul;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.pavel.chatapp.Adapter_Modul.Items.MyChat;
import com.example.pavel.chatapp.Adapter_Modul.Items.MyUser;
import com.example.pavel.chatapp.Chat.ChatWithUserActivity;
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
import androidx.recyclerview.widget.RecyclerView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context context;
    private String theLastMessage;
    private boolean isChat;
    private List<MyUser> myUserList;

    public UserAdapter(Context context, List<MyUser> myUserList, boolean isChat) {
        this.context = context;
        this.isChat = isChat;
        this.myUserList = myUserList;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserAdapter.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final MyUser myUser = myUserList.get(position);
        myUser.setId(myUserList.get(position).getId());
        holder.username.setText(myUser.getUsername());

        if (myUser.getImageURL().equals("default")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(context).load(myUser.getImageURL()).into(holder.profile_image);
        }


        //Display last message if user not seen it yet
        if (isChat) {
            lastMessage(myUser.getId(), holder.last_message);
        } else {
            holder.last_message.setVisibility(View.GONE);
        }


        //Check if current user online
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

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatWithUserActivity.class);
                intent.putExtra("userId", myUser.getId());
                context.startActivity(intent);
            }
        });
    }


    private void lastMessage(final String userid, final TextView last_msg) {
        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MyChat chat = snapshot.getValue(MyChat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())) {
                        theLastMessage = chat.getMessage();
                    }
                }

                switch (theLastMessage) {
                    case "default":
                        last_msg.setText("No message");
                        break;

                    default:
                        last_msg.setText(theLastMessage);
                        break;
                }
                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return myUserList.size();
    }


    //Holder of user item
    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView username;
        private ImageView profile_image;
        private ImageView img_on;
        private ImageView img_off;
        private TextView last_message;

        public ViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.userItemTVUserName);
            last_message = itemView.findViewById(R.id.last_msg);
            profile_image = itemView.findViewById(R.id.userItemImage);

            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);

        }
    }
}














