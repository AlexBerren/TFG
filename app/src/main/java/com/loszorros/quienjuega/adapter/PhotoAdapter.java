package com.loszorros.quienjuega.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.loszorros.quienjuega.RegistroLoginActivitys.Complementario.CircleTransform;
import com.loszorros.quienjuega.MenuFragments.MENUFirstFragment;
import com.loszorros.quienjuega.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder>{
    private List<QueryDocumentSnapshot> photosUsuario;
    private FirebaseFirestore db;
    //public ImageView usuarioImageView;
    private PhotoAdapter.OnItemClickListener clickListener;
    private FragmentManager fragmentManager;
    private static String email;
    private Context context;


    public PhotoAdapter(List<QueryDocumentSnapshot> listaPhotosUsuario, Context context) {
        this.photosUsuario = listaPhotosUsuario;
        this.context = context;

    }

    public interface OnItemClickListener {
        void onItemClick(String idDocumento);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo, parent, false);

        db = FirebaseFirestore.getInstance();
        //usuarioImageView = itemView.findViewById(R.id.usersImageView);
        SharedPreferences prefe = itemView.getContext().getSharedPreferences("datos", Context.MODE_PRIVATE);
        email = prefe.getString("email" ,null);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoAdapter.ViewHolder holder, int position) {
        QueryDocumentSnapshot documentoPhoto = photosUsuario.get(position);

        setNombreUsuarioLogeado(holder.nombreTextView);
        setProfilePhoto(holder.usuarioImageView);


        getUploadedPhoto(documentoPhoto, holder.uploadPhotoImageView);
        getDescriptionPhoto(documentoPhoto, holder.descriptionPhoto);
        getFechaSubidaPhoto(documentoPhoto,holder.fechaSubida);

        /** Borrar foto subida **/
        holder.borrarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    deleteDocumentAndReorderNames(documentoPhoto, context);
                    /** arreglo **/
                    FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.frame_container, new MENUFirstFragment());
                    transaction.addToBackStack(null); // Opcional: agregar la transacción a la pila de retroceso
                    transaction.commit();
                }
            }
        });
    }



    @Override
    public int getItemCount() {
        return photosUsuario.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView uploadPhotoImageView;
        public TextView descriptionPhoto;
        public TextView nombreTextView;
        public ImageView usuarioImageView;
        public Button borrarButton;
        public TextView fechaSubida;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            uploadPhotoImageView = itemView.findViewById(R.id.photoGame);
            descriptionPhoto = itemView.findViewById(R.id.tvDescriptionUser);
            nombreTextView = itemView.findViewById(R.id.nameUser);
            usuarioImageView = itemView.findViewById(R.id.gameImageView);
            borrarButton = itemView.findViewById(R.id.btnBorrarFoto);
            fechaSubida = itemView.findViewById(R.id.tvFechaSubida2);



        }
    }

    public void setOnItemClickListener(PhotoAdapter.OnItemClickListener listener) {
        this.clickListener = listener;
    }


    //recupera foto subida
    private void getUploadedPhoto(QueryDocumentSnapshot documentPhoto, ImageView imageView) {
        db.collection("users").document(email).collection("photos").document(documentPhoto.getId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String photoUser = documentSnapshot.getString("photo");
                    if (photoUser != null && !photoUser.isEmpty()) {
                        Picasso.with(imageView.getContext())
                                .load(photoUser)
                                .resize(450, 400)
                                .centerCrop()
                                .into(imageView);
                    }
                });

    }


    private void getDescriptionPhoto(QueryDocumentSnapshot documentPhoto, TextView descripcion) {
        db.collection("users").document(email).collection("photos").document(documentPhoto.getId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String descripcionPhoto = documentSnapshot.getString("descripcion");
                    if (descripcionPhoto != null && !descripcionPhoto.isEmpty()) {
                        descripcion.setText(descripcionPhoto);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(descripcion.getContext(), "Error al obtener los datos de la descripción", Toast.LENGTH_SHORT).show());
    }

    private void setNombreUsuarioLogeado(TextView nombreTextView) {
        db.collection("users").document(email).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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

    private void setProfilePhoto(ImageView usuarioImageView) {
        db.collection("users").document(email).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String photoUser = documentSnapshot.getString("photo");
                try {
                    if (!photoUser.equals("")) {
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

    private void getFechaSubidaPhoto(QueryDocumentSnapshot documentPhoto, TextView fechaSubida) {
        db.collection("users").document(email).collection("photos").document(documentPhoto.getId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    Timestamp fechaSubidaTimeStamp = documentSnapshot.getTimestamp("fecha_subida");
                    if (fechaSubidaTimeStamp != null) {

                        Date date = fechaSubidaTimeStamp.toDate();

                        // Crea un objeto SimpleDateFormat para el formato deseado
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

                        // Formatea la fecha a String en el formato especificado
                        String fechaFormateada = dateFormat.format(date);


                        fechaSubida.setText(fechaFormateada);
                    }

                });
    }


    private void deleteDocumentAndReorderNames(DocumentSnapshot documentToDelete, Context context) {
        String collectionPath = "users/" + email + "/photos";
        String deletedDocumentId = documentToDelete.getId();

        DocumentReference deletedDocumentRef = db.collection(collectionPath).document(deletedDocumentId);

        deletedDocumentRef.delete().addOnSuccessListener(aVoid -> {
            // Document successfully deleted


            // Retrieve the remaining documents in the collection
            db.collection(collectionPath).orderBy("fecha_subida", Query.Direction.DESCENDING).get().addOnSuccessListener(querySnapshot -> {
                int newDocumentCount = querySnapshot.size();

                // Update the names of the remaining documents
                for (int i = 0; i < newDocumentCount; i++) {
                    DocumentSnapshot currentDocument = querySnapshot.getDocuments().get(i);
                    String currentDocumentId = currentDocument.getId();
                    String newDocumentId = "photo" + (i + 1);

                    // Retrieve the data of the current document
                    Map<String, Object> documentData = currentDocument.getData();

                    // Delete the current document
                    db.collection(collectionPath).document(currentDocumentId).delete().addOnSuccessListener(aVoid1 -> {
                        // Document successfully deleted

                        // Create a new document with the updated name and the same data
                        db.collection(collectionPath).document(newDocumentId).set(documentData).addOnSuccessListener(aVoid2 -> {
                            // Document successfully created with the updated name

                        }).addOnFailureListener(e -> {
                            // Error creating the new document

                        });
                    }).addOnFailureListener(e -> {
                        // Error deleting the current document

                    });
                }
            }).addOnFailureListener(e -> {
                // Error retrieving the remaining documents

            });
        }).addOnFailureListener(e -> {
            // Error deleting the document

        });
    }





}
