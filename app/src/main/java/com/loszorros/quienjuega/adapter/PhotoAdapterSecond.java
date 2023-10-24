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
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.loszorros.quienjuega.RegistroLoginActivitys.Complementario.CircleTransform;
import com.loszorros.quienjuega.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PhotoAdapterSecond extends RecyclerView.Adapter<PhotoAdapterSecond.ViewHolder>{
    private List<QueryDocumentSnapshot> fotosList;
    private FirebaseFirestore db;
    private static String email;
    private Context context;
    private OnItemClickListener itemClickListener;

    public PhotoAdapterSecond(List<QueryDocumentSnapshot> fotosList, Context context) {
        this.fotosList = fotosList;
        this.context = context;

    }

    public interface OnItemClickListener {
        void onItemClick(String idDocumento);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo_first, parent, false);
        db = FirebaseFirestore.getInstance();
        //usuarioImageView = itemView.findViewById(R.id.usersImageView);
        SharedPreferences prefe = itemView.getContext().getSharedPreferences("datos", Context.MODE_PRIVATE);
        email = prefe.getString("email" ,null);
        return new ViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull PhotoAdapterSecond.ViewHolder holder, int position) {
        QueryDocumentSnapshot fotoDocument = fotosList.get(position);
        cargarFotoSubida(fotoDocument, holder.uploadPhotoImageView);
        cargarDescripcion(fotoDocument, holder.descriptionText);
        cargarNombreUsuario(fotoDocument, holder.nombreTextView);
        cargarFotoPerfil(fotoDocument, holder.usuarioImageView);
        cargarFechaSubida(fotoDocument,holder.fechaSubida);


    }

    @Override
    public int getItemCount() {
        return fotosList.size();
    }



    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView uploadPhotoImageView;
        public TextView descriptionText;
        public TextView nombreTextView;
        public ImageView usuarioImageView;
        public TextView fechaSubida;



        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            uploadPhotoImageView = itemView.findViewById(R.id.photoGame);
            descriptionText = itemView.findViewById(R.id.tvDescriptionUser);
            nombreTextView = itemView.findViewById(R.id.nameUser);
            usuarioImageView = itemView.findViewById(R.id.gameImageView);
            fechaSubida = itemView.findViewById(R.id.tvFechaSubida);


        }

    }


    private void cargarFotoSubida(QueryDocumentSnapshot fotoDocument, ImageView imageView) {
        String photoUser = fotoDocument.getString("photo");
        if (photoUser != null && !photoUser.isEmpty()) {
            Picasso.with(imageView.getContext())
                    .load(photoUser)
                    .resize(450, 400)
                    .centerCrop()
                    .into(imageView);
        }
    }

    private void cargarDescripcion(QueryDocumentSnapshot fotoDocument, TextView descriptionText) {
        String descriptionUser = fotoDocument.getString("descripcion");
        if (descriptionUser != null && !descriptionUser.isEmpty()) {
            descriptionText.setText(descriptionUser);
        }
    }

    private void cargarFechaSubida(QueryDocumentSnapshot fotoDocument, TextView fechaText) {
        Timestamp fechaSubida = fotoDocument.getTimestamp("fecha_subida");
        if (fechaSubida != null) {

            Date date = fechaSubida.toDate();

            // Crea un objeto SimpleDateFormat para el formato deseado
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            // Formatea la fecha a String en el formato especificado
            String fechaFormateada = dateFormat.format(date);

            fechaText.setText(fechaFormateada);
        }
    }

    private void cargarNombreUsuario(QueryDocumentSnapshot fotoDocument, TextView nombreTextView) {
        DocumentReference userRef = fotoDocument.getReference().getParent().getParent();
        String IDusuarioSeguido = userRef.getId();

        db.collection("users").document(IDusuarioSeguido).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String username = documentSnapshot.getString("username");


                try {
                    if (!username.equals("")) {
                        nombreTextView.setText("@" + username);
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

    private void cargarFotoPerfil(QueryDocumentSnapshot fotoDocument, ImageView usuarioImageView) {
        DocumentReference userRef = fotoDocument.getReference().getParent().getParent();
        String IDusuarioSeguido = userRef.getId();
        db.collection("users").document(IDusuarioSeguido).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String photoUser = documentSnapshot.getString("photo");
                try {
                    if (photoUser != null && !photoUser.isEmpty()) {
                        Picasso.with(usuarioImageView.getContext())
                                .load(photoUser)
                                .resize(400, 400) // ajusta el tamaño máximo de la imagen a 400 x 400
                                .transform(new CircleTransform())
                                .into(usuarioImageView);
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
