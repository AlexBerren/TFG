package com.loszorros.quienjuega.adapter;


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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.loszorros.quienjuega.RegistroLoginActivitys.Complementario.CircleTransform;
import com.loszorros.quienjuega.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class GamerAdapter extends RecyclerView.Adapter<GamerAdapter.ViewHolder> {
    private List<String> nombresUsuarios;
    private FirebaseFirestore db;
    //public ImageView usuarioImageView;
    private TextView numeroSeguidores;
    private TextView numeroSeguidos;
    private OnItemClickListener clickListener;
    private FragmentManager fragmentManager;

    public GamerAdapter(List<String> nombresUsuarios) {
        this.nombresUsuarios = nombresUsuarios;

    }




    public interface OnItemClickListener {
        void onItemClick(String idDocumento);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);

        db = FirebaseFirestore.getInstance();
        //usuarioImageView = itemView.findViewById(R.id.usersImageView);
        numeroSeguidores  = itemView.findViewById(R.id.txtNumeroSeguidores);
        numeroSeguidos  = itemView.findViewById(R.id.txtNumeroSeguidos);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String nombreUsuario = nombresUsuarios.get(position);
        holder.nombreTextView.setText("@" + nombreUsuario);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    String idDocumento = nombreUsuario;

                    clickListener.onItemClick(idDocumento);
                }
            }
        });


        CollectionReference usuariosRef = db.collection("users");
        Query consulta = usuariosRef.whereEqualTo("username", nombreUsuario);
        consulta.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                    String id = documentSnapshot.getId();
                    mostrarCantidadSeguidores(id, holder.numeroSeguidores);
                    mostrarCantidadSeguidos(id, holder.numeroSeguidos);
                    getPhoto(id, holder.usuarioImageView);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("TAG", "Error al obtener los documentos: ", e);
            }
        });
    }


    @Override
    public int getItemCount() {
        return nombresUsuarios.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nombreTextView;
        public ImageView usuarioImageView;
        public TextView numeroSeguidores;
        public TextView numeroSeguidos;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreTextView = itemView.findViewById(R.id.nameUser);
            usuarioImageView = itemView.findViewById(R.id.gameImageView);
            numeroSeguidores = itemView.findViewById(R.id.txtNumeroSeguidores);
            numeroSeguidos = itemView.findViewById(R.id.txtNumeroSeguidos);
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

    public void mostrarCantidadSeguidores(String idDocumento, TextView textView) {
        DocumentReference docRef = db.collection("users").document(idDocumento).collection("followers").document("usernames");

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    int cantidadSeguidores = documentSnapshot.getData().size();
                    textView.setText(String.valueOf(cantidadSeguidores));
                } else {
                    textView.setText("0");
                }
            }
        });

    }
    public void mostrarCantidadSeguidos(String idDocumento, TextView textView) {
        DocumentReference docRef = db.collection("users").document(idDocumento).collection("follows").document("usernames");

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    int cantidadSeguidos = documentSnapshot.getData().size();
                    textView.setText(String.valueOf(cantidadSeguidos));
                } else {
                    textView.setText("0");
                }
            }
        });


    }


}
