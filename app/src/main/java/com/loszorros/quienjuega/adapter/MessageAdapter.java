package com.loszorros.quienjuega.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.loszorros.quienjuega.R;
import com.loszorros.quienjuega.models.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private String user;
    private List<Message> messages;
    private List<DocumentSnapshot> messageSnapshots;
    private FirebaseFirestore db;
    private String chatId;
    private String email;




    public MessageAdapter(String user,  String chatId) {
        this.user = user;
        this.chatId = chatId;
    }

    public void setData(List<Message> list, List<DocumentSnapshot> snapshots) {
        messages = list;
        messageSnapshots = snapshots;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        db = FirebaseFirestore.getInstance();
        SharedPreferences prefe = itemView.getContext().getSharedPreferences("datos", Context.MODE_PRIVATE);
        email = prefe.getString("email" ,null);
        return new MessageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        //Message message = messages.get(position);

        DocumentSnapshot snapshot = messageSnapshots.get(position);
        Message message = snapshot.toObject(Message.class);

        // Obtén el ID del documento
        String messageId = snapshot.getId();

        /**verificarmos cuales son nuetros mensajes y los mostramos*/
        if (user.equals(message.getFrom())) {
            holder.itemView.findViewById(R.id.myMessageLayout).setVisibility(View.VISIBLE);
            holder.itemView.findViewById(R.id.otherMessageLayout).setVisibility(View.GONE);

            TextView myMessageTextView = holder.itemView.findViewById(R.id.myMessageTextView);
            myMessageTextView.setText(message.getMessage());

            TextView dateMyMessageTextView = holder.itemView.findViewById(R.id.dateMyMessageTextView);
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

            if (message.getDob()!=null){
                String formattedDate = dateFormat.format(message.getDob());
                dateMyMessageTextView.setText(formattedDate);
            }

            /** Verificar el valor de typeaproovals, si somos nosotros los que hemos enviado la invitacion , entonces
             * veremos la peticion en el chat pero dehabilitaremos los botones para que no la pueedas acepatr ni rechazar*/
            if (message.getTypeAprooval() && !message.getMessage().contains("ACEPTADA") && !message.getMessage().contains("RECHAZADA")) {
                // Si typeaproovals es true, mostrar los botones
                TextView aproovalsTextView = holder.itemView.findViewById(R.id.aproovalsTextView);
                aproovalsTextView.setText(message.getMessage());
                holder.itemView.findViewById(R.id.myMessageLayout).setVisibility(View.GONE);
                holder.itemView.findViewById(R.id.otherMessageLayout).setVisibility(View.GONE);
                holder.itemView.findViewById(R.id.aproovalsLayOut).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.btnAceptar).setEnabled(false);
                holder.itemView.findViewById(R.id.btnRechazar).setEnabled(false);

            } else {
                /**ocultamos el layout de invitacion si es un mensaje normal*/
                holder.itemView.findViewById(R.id.myMessageLayout).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.aproovalsLayOut).setVisibility(View.GONE);
            }


        } else {
            holder.itemView.findViewById(R.id.myMessageLayout).setVisibility(View.GONE);
            holder.itemView.findViewById(R.id.otherMessageLayout).setVisibility(View.VISIBLE);

            TextView othersMessageTextView = holder.itemView.findViewById(R.id.othersMessageTextView);
            othersMessageTextView.setText(message.getMessage());

            TextView dateMyMessageTextView = holder.itemView.findViewById(R.id.dateOthersMessageTextView);
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

            if (message.getDob()!=null){
                String formattedDate = dateFormat.format(message.getDob());
                dateMyMessageTextView.setText(formattedDate);
            }
            // Verificar el valor de typeaproovals
            if (message.getTypeAprooval() && !message.getMessage().contains("ACEPTADA") && !message.getMessage().contains("RECHAZADA")) {
                // Si typeaproovals es true, mostrar los botones
                TextView aproovalsTextView = holder.itemView.findViewById(R.id.aproovalsTextView);
                aproovalsTextView.setText(message.getMessage());
                holder.itemView.findViewById(R.id.myMessageLayout).setVisibility(View.GONE);
                holder.itemView.findViewById(R.id.otherMessageLayout).setVisibility(View.GONE);
                holder.itemView.findViewById(R.id.aproovalsLayOut).setVisibility(View.VISIBLE);

                /**para cuando le des a aceptar se ponga a false y desaparezcan los botones*/
                holder.itemView.findViewById(R.id.btnAceptar).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        message.setTypeAprooval(false);
                        db.collection("chats").document(chatId).collection("messages").document(messageId).update("typeAprooval", false);
                        db.collection("chats").document(chatId).collection("messages").document(messageId).update("message", message.getMessage() + "\n ACEPTADA");

                        /** Añadir al usuario como participante a la partida **/

                        /** Recupero el "from" y el "message" **/
                        db.collection("chats").document(chatId).collection("messages").document(messageId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if (documentSnapshot.exists()) {
                                            String message = documentSnapshot.getString("message");
                                            String creadorPartida = documentSnapshot.getString("from");

                                            /** Con el "from" recupero todas las partidas de ese jugador **/

                                            CollectionReference gamesRef = db.collection("users").document(creadorPartida).collection("games");

                                            gamesRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                @Override
                                                public void onSuccess(QuerySnapshot snapshot) {
                                                    /** En todas las partidas del jugador junto game - date - hour -jugadores **/
                                                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                                                        String game = document.getString("Game");
                                                        String date = document.getString("Date");
                                                        String hour = document.getString("Hour");

                                                        String entry = game + " - " + date + " - " + hour + " - " + "\n ACEPTADA";


                                                        /** Comparo eso con el "message" recuperado al principio **/
                                                        if (entry.equals(message)) {
                                                            Map<String, Object> campos = document.getData();

                                                            /** Creo un nuevo campo "participanteX" dependiendo de los participantes que haya ya **/
                                                            int contadorParticipantes = 0;
                                                            for (String campo : campos.keySet()) {
                                                                if (campo.startsWith("participante")) {
                                                                    contadorParticipantes++;
                                                                }
                                                            }

                                                            String nuevoCampo = "participante" + (contadorParticipantes + 1);

                                                            /** Recupero el nombre del usuario logeado para añadirlo como participante **/
                                                            db.collection("users").document(email).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onSuccess(DocumentSnapshot userSnapshot) {
                                                                    if (userSnapshot.exists()) {
                                                                        String username = userSnapshot.getString("username");

                                                                        db.collection("users")
                                                                                .document(creadorPartida)
                                                                                .collection("games")
                                                                                .document(document.getId())
                                                                                .get()
                                                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                                    @Override
                                                                                    public void onSuccess(DocumentSnapshot documentSnapshot1) {
                                                                                        if (documentSnapshot1.exists()) {
                                                                                            String currentNumberPlayersString = documentSnapshot1.getString("NumberPlayers");
                                                                                            int currentNumberPlayers = Integer.parseInt(currentNumberPlayersString);
                                                                                                int updatedNumberPlayers = currentNumberPlayers - 1;
                                                                                                if (updatedNumberPlayers >= 0) {


                                                                                                campos.put("NumberPlayers", String.valueOf(updatedNumberPlayers));
                                                                                                campos.put(nuevoCampo, username);

                                                                                                db.collection("users")
                                                                                                        .document(creadorPartida)
                                                                                                        .collection("games")
                                                                                                        .document(document.getId())
                                                                                                        .set(campos)
                                                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onSuccess(Void aVoid) {
                                                                                                            }
                                                                                                        });

                                                                                        } else {
                                                                                        }
                                                                                    }
                                                                                    }
                                                                                })
                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                        System.out.println("Error al obtener el documento: " + e.getMessage());
                                                                                    }
                                                                                });
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // Manejar el error
                                                }
                                            });

                                        }
                                    }
                                });

                    }
                });

                /**para cuando le des a RECHAZAR se ponga a false y desaparezcan los botones*/
                holder.itemView.findViewById(R.id.btnRechazar).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        message.setTypeAprooval(false);
                        db.collection("chats").document(chatId).collection("messages").document(messageId).update("typeAprooval", false);
                        db.collection("chats").document(chatId).collection("messages").document(messageId).update("message", message.getMessage() + "\n RECHAZADA");

                    }
                });


                // Aquí puedes realizar cualquier otra configuración o personalización de los botones si es necesario
            } else {
                // Si typeaproovals es false, ocultar los botones
                holder.itemView.findViewById(R.id.aproovalsLayOut).setVisibility(View.GONE);
            }
        }
    }


    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

}
