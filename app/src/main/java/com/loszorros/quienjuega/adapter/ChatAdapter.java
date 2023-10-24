package com.loszorros.quienjuega.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.loszorros.quienjuega.RegistroLoginActivitys.Complementario.CircleTransform;
import com.loszorros.quienjuega.R;
import com.loszorros.quienjuega.models.Chat;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<Chat> chats = new ArrayList<>();
    private final OnChatClickListener chatClick;

    TextView chatNameText;
    TextView usersTextView;

    private FirebaseFirestore db;
    private static String email;
   // private ImageView imagenPerfil;
    List<Chat> listChats = new ArrayList<>();

    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }

    public ChatAdapter(OnChatClickListener chatClick) {
        this.chatClick = chatClick;
    }

    public void setData(List<Chat> list) {
        chats = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_chat,
                parent,
                false
        );
        chatNameText = itemView.findViewById(R.id.nameUser);
        usersTextView = itemView.findViewById(R.id.usersTextView);
        db = FirebaseFirestore.getInstance();
        SharedPreferences prefe = itemView.getContext().getSharedPreferences("datos", Context.MODE_PRIVATE);
        email = prefe.getString("email" ,null);

        return new ChatViewHolder(itemView,chatNameText,usersTextView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chats.get(position);
        String otherEmail="";

        /**verificamos en que cuenta estamos es decir si nnn fue el que creo el chat y estamos en nnn que nos muestre a la otra persona y sino,
         * que nos muestre a la persona que creo el chat*/
        if (email.equals(chat.getUsers().get(0))) {
             otherEmail = chat.getUsers().get(1);
        }else {
             otherEmail = chat.getUsers().get(0);
        }

        db.collection("users").document(otherEmail).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                holder.chatNameText.setText(documentSnapshot.getString("username"));
            }
        });

        db.collection("chats")
                .document(chat.getId())
                .collection("messages")
                .orderBy("dob", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                            String message = documentSnapshot.getString("message");

                            if (message.length() > 25) {
                                String shortenedMessage = message.substring(0, 25) + "...";
                                holder.usersTextView.setText(shortenedMessage);
                            } else {
                                holder.usersTextView.setText(message);
                            }


                        }
                    }
                });

        db.collection("chats").document(chat.getId()).collection("messages")
                .orderBy("dob", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error == null && queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        String message = documentSnapshot.getString("message");

                        if (message.length() > 25) {
                            String shortenedMessage = message.substring(0, 25) + "...";
                            holder.usersTextView.setText(shortenedMessage);
                        } else {
                            holder.usersTextView.setText(message);
                        }
                    }
                });

        getPhoto(otherEmail,holder.imagenPerfil);
        holder.itemView.setOnClickListener(view -> chatClick.onChatClick(chat));
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView chatNameText;
        TextView usersTextView;
        ImageView imagenPerfil;
        public ChatViewHolder(@NonNull View itemView, TextView chatNameText, TextView usersTextView) {
            super(itemView);
            this.chatNameText = chatNameText;
            this.usersTextView = usersTextView;
            this.imagenPerfil = itemView.findViewById(R.id.gameImageView);

        }
    }

    private void getPhoto(String id, ImageView imageView) {
        db.collection("users").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String photoUser = documentSnapshot.getString("photo");
                try {
                    if (!photoUser.equals("")) {
                        Picasso.with(imageView.getContext())
                                .load(photoUser)
                                .resize(400, 400) // ajusta el tamaño máximo de la imagen a 400 x 400
                                .transform(new CircleTransform())
                                .into(imageView);
                    }
                } catch (Exception e) {
                    Log.v("Error", "e: " + e);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

}
