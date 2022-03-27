package com.internaltest.sarahchatbotmvp.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.internaltest.sarahchatbotmvp.R;
import com.internaltest.sarahchatbotmvp.models.Message;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {

  private final List<Message> messageList;
  private final Activity activity;

  public ChatAdapter(List<Message> messageList, Activity activity) {
    this.messageList = messageList;
    this.activity = activity;
  }

  @NonNull @Override
  public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(activity).inflate(R.layout.adapter_message_one, parent, false);

    return new MyViewHolder(view);
  }

  @Override public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
    final String message = messageList.get(position).getMessage();
    boolean isReceived = messageList.get(position).getIsReceived();

    //arrastar
    holder.messageReceive.setTextIsSelectable(false);
    holder.messageReceive.measure(-1, -1);
    holder.messageReceive.setTextIsSelectable(true);

     if(isReceived){
       holder.messageReceive.setVisibility(View.VISIBLE);
       holder.messageSend.setVisibility(View.GONE);
       holder.imageReceive.setVisibility(View.GONE);
       holder.imageReceiveScroll.setVisibility(View.GONE);

       holder.messageReceive.setText(message);
       holder.messageReceive.setClickable(true);


     }else {
       holder.messageSend.setVisibility(View.VISIBLE);
       holder.messageReceive.setVisibility(View.GONE);
       holder.imageReceive.setVisibility(View.GONE);
       holder.imageReceiveScroll.setVisibility(View.GONE);

       holder.messageSend.setText(message);
     }
  }

  @Override public int getItemCount() {
    return messageList.size();
  }

  static class MyViewHolder extends RecyclerView.ViewHolder{

    TextView messageSend;
    TextView messageReceive;
    ImageView imageReceive;

    HorizontalScrollView imageReceiveScroll;
    ImageView imageReceiveScroll1;
    ImageView imageReceiveScroll2;
    ImageView imageReceiveScroll3;
    ImageView imageReceiveScroll4;
    ImageView imageReceiveScroll5;
    ImageView imageReceiveScroll6;
    ImageView imageReceiveScroll7;
    ImageView imageReceiveScroll8;



    MyViewHolder(@NonNull View itemView) {
      super(itemView);
      messageSend = itemView.findViewById(R.id.message_send);
      messageReceive = itemView.findViewById(R.id.message_receive);
      imageReceive = itemView.findViewById(R.id.image_receive);

      imageReceiveScroll = itemView.findViewById(R.id.image_receive_scroll);
      imageReceiveScroll1 = itemView.findViewById(R.id.scrollimage1);
      imageReceiveScroll2 = itemView.findViewById(R.id.scrollimage2);
      imageReceiveScroll3 = itemView.findViewById(R.id.scrollimage3);
      imageReceiveScroll4 = itemView.findViewById(R.id.scrollimage4);
      imageReceiveScroll5 = itemView.findViewById(R.id.scrollimage5);
      imageReceiveScroll6 = itemView.findViewById(R.id.scrollimage6);
      imageReceiveScroll7 = itemView.findViewById(R.id.scrollimage7);
      imageReceiveScroll8 = itemView.findViewById(R.id.scrollimage8);
    }

  }
  
}
