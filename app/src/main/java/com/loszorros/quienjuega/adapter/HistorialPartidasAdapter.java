package com.loszorros.quienjuega.adapter;



import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.loszorros.quienjuega.RegistroLoginActivitys.Complementario.CircleTransform;
import com.loszorros.quienjuega.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class HistorialPartidasAdapter extends RecyclerView.Adapter<HistorialPartidasAdapter.ViewHolder> {
    private List<DocumentSnapshot> nombresPartidas;
    private FirebaseFirestore db;
    //public ImageView usuarioImageView;
    private OnItemClickListener clickListener;
    private FragmentManager fragmentManager;
    private Context context;

    public HistorialPartidasAdapter(List<DocumentSnapshot> nombresPartidas, Context context) {
        this.nombresPartidas = nombresPartidas;
        this.context = context;

    }




    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot idDocumento);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historial, parent, false);

        db = FirebaseFirestore.getInstance();
        //usuarioImageView = itemView.findViewById(R.id.usersImageView);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentSnapshot nombrePartida = nombresPartidas.get(position);
        holder.nombreTextView.setText(nombrePartida.getString("Game"));
        getFecha(nombrePartida, holder.fecha);
        getHora(nombrePartida, holder.hora);
        getFotoJuego(nombrePartida, holder.usuarioImageView);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    DocumentSnapshot idDocumento = nombrePartida;

                    clickListener.onItemClick(idDocumento);
                }
            }
        });


    }


    @Override
    public int getItemCount() {
        return nombresPartidas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nombreTextView;
        public ImageView usuarioImageView;
        public TextView fecha;
        public TextView hora;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreTextView = itemView.findViewById(R.id.nameUser);
            usuarioImageView = itemView.findViewById(R.id.gameImageView);
            fecha = itemView.findViewById(R.id.txtNumeroSeguidores);
            hora = itemView.findViewById(R.id.txtNumeroSeguidos);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
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

    private void getFecha(DocumentSnapshot documento, TextView textview){
            if (documento.exists()) {
                String fecha = documento.getString("Date");
                if (fecha != null) {
                    textview.setText(fecha);
                }
        }
    }

    private void getHora(DocumentSnapshot documento, TextView textview){
        if (documento.exists()) {
            String hora = documento.getString("Hour");
            if (hora != null) {
                textview.setText(hora);
            }
        }
    }

    private void getFotoJuego(DocumentSnapshot documento, ImageView imageview){
        if (documento.exists()) {
            String nombreJuego = documento.getString("Game");

            CollectionReference gamesCollectionRef = db.collection("games");

            gamesCollectionRef.whereEqualTo("name", nombreJuego).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot gameDoc : task.getResult()) {
                        String url = gameDoc.getString("url");
                        Picasso.with(imageview.getContext())
                                .load(url)
                                .resize(400, 400) // ajusta el tamaño máximo de la imagen a 400 x 400
                                .transform(new CircleTransform())
                                .into(imageview);
                    }
                } else {
                    // Error al obtener los documentos
                }
            });

        }
    }



}
