package com.example.pavel.chatapp.Adapter_Modul;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.pavel.chatapp.Adapter_Modul.Items.Message;
import com.example.pavel.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_SIDE_LEFT = 0;
    public static final int MSG_SIDE_RIGHT = 1;
    private FirebaseUser firebaseUser;
    private List<Message> messageList;
    private Context context;
    private String imageUrl;

    public MessageAdapter(Context context, List<Message> messageList, String imageUrl) {
        this.context = context;
        this.imageUrl = imageUrl;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_SIDE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_side_right, parent, false);
            return new MessageAdapter.ViewHolder(view);

        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_side_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messageList.get(position);
        setTextOfMessage(holder, message.getMessage());
        notifyUserAboutStatusOfMessage(position, holder, message);
    }

    private void notifyUserAboutStatusOfMessage(int position, ViewHolder holder, Message message) {
        if (position == messageList.size() - 1) {
            if (message.isSeen()) {
                holder.notifyIfMessageIsSeen.setText("Seen");
            } else {
                holder.notifyIfMessageIsSeen.setText("Delivered");
            }
        } else {
            holder.notifyIfMessageIsSeen.setVisibility(View.GONE);
        }
    }

    private void setTextOfMessage(ViewHolder holder, String text) {
        holder.textOfMessage.setText(text);
    }

    //Checking which side message should be displayed
    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (messageList.get(position).getSender().equals(firebaseUser.getUid())) {
            return MSG_SIDE_RIGHT;
        } else {
            return MSG_SIDE_LEFT;
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textOfMessage, notifyIfMessageIsSeen;

        public ViewHolder(View itemView) {
            super(itemView);
            textOfMessage = itemView.findViewById(R.id.textOfMessage);
            notifyIfMessageIsSeen = itemView.findViewById(R.id.notifyIfMessageIsSeen);
        }
    }

}