package com.loszorros.quienjuega.adapter;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.loszorros.quienjuega.RegistroLoginActivitys.Complementario.CircleTransform;
import com.loszorros.quienjuega.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.ViewHolder>{
    private List<QueryDocumentSnapshot> juegosList;
    private FirebaseFirestore db;
    private static String email;
    private Context context;
    private OnItemClickListener itemClickListener;
    private double latitud;
    private double longitud;
    private static final double EARTH_RADIUS = 6371;

    public GameAdapter(List<QueryDocumentSnapshot> juegosList, Context context, double latitud, double longitud) {
        this.juegosList = juegosList;
        this.context = context;
        this.latitud = latitud;
        this.longitud= longitud;
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
                .inflate(R.layout.item_game, parent, false);
        db = FirebaseFirestore.getInstance();
        return new ViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull GameAdapter.ViewHolder holder, int position) {
        QueryDocumentSnapshot gameDocument = juegosList.get(position);
        cargarFotoPartida(gameDocument, holder.photoGame);
        cargarFechaPartida(gameDocument, holder.fechaGame);
        cargarHoraPartida(gameDocument,holder.horaGame);
        cargarNumeroJugadores(gameDocument, holder.numeroJugadores);

        cargarNombreUsuario(gameDocument, holder.nameUser);
        cargarFotoUsuario(gameDocument, holder.photoUser);
        cargarDistanciaUsuario(gameDocument, holder.distanceUser);
    }

    @Override
    public int getItemCount() {
        return juegosList.size();
    }



    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView photoGame;
        public TextView fechaGame;
        public TextView horaGame;
        public TextView numeroJugadores;

        public ImageView photoUser;
        public TextView nameUser;
        public TextView distanceUser;
        


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            photoGame = itemView.findViewById(R.id.photoGame);
            distanceUser = itemView.findViewById(R.id.tvDescriptionUser);
            fechaGame = itemView.findViewById(R.id.tvFechaGame);
            horaGame = itemView.findViewById(R.id.tvHoraGame);
            numeroJugadores = itemView.findViewById(R.id.tvJugadores);

            photoUser = itemView.findViewById(R.id.gameImageView);
            nameUser = itemView.findViewById(R.id.nameUser);

        }


    }


    private void  cargarFechaPartida(QueryDocumentSnapshot partidaDocument, TextView textView){
        String fechaGame = partidaDocument.getString("Date");
        if (fechaGame != null && !fechaGame.isEmpty()) {
            textView.setText(fechaGame);
        }
    }



    private void  cargarHoraPartida(QueryDocumentSnapshot partidaDocument, TextView textView){
        String horaGame = partidaDocument.getString("Hour");
        if (horaGame != null && !horaGame.isEmpty()) {
            textView.setText(horaGame);
        }
    }

    private void  cargarNumeroJugadores(QueryDocumentSnapshot partidaDocument, TextView textView){
        String numberPlayersGame = partidaDocument.getString("NumberPlayers");
        if (numberPlayersGame != null && !numberPlayersGame.isEmpty()) {
            textView.setText(numberPlayersGame);
        }
    }

    private void cargarFotoPartida(QueryDocumentSnapshot partidaDocument, ImageView imageView) {
        /** Obtenemos el documento de la coleccion "games"
         * a traves del nombre del juego seleccionado
         * para obtener asi su imagen **/
        String nombreJuego = partidaDocument.getString("Game");
        CollectionReference coleccionGames = db.collection("games");

        coleccionGames.whereEqualTo("name", nombreJuego).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String nombreDocumento = document.getId();

                    /** Me meto en el documento de la coleccion games para recuperar la url **/
                    DocumentReference gameDocumentRef = FirebaseFirestore.getInstance().collection("games").document(nombreDocumento);

                    gameDocumentRef.get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            DocumentSnapshot document1 = task1.getResult();
                            if (document1.exists()) {
                                String url = document1.getString("url");

                                if (nombreJuego != null && !nombreJuego.isEmpty()) {
                                    Picasso.with(imageView.getContext())
                                            .load(url)
                                            .resize(750, 600)
                                            .centerCrop()
                                            .into(imageView);
                                }

                            }
                        }
                    });

                }
            }
        });

    }

    private void  cargarNombreUsuario(QueryDocumentSnapshot partidaDocument, TextView textView){
        DocumentReference userDocRef = partidaDocument.getReference().getParent().getParent();
        userDocRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            textView.setText("@" + username);

                        }
                    }
                });
    }

    private void  cargarFotoUsuario(QueryDocumentSnapshot partidaDocument, ImageView imageview){
        DocumentReference userDocRef = partidaDocument.getReference().getParent().getParent();
        userDocRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String photoUser = documentSnapshot.getString("photo");
                            if (photoUser != null && !photoUser.isEmpty()) {
                                Picasso.with(imageview.getContext())
                                        .load(photoUser)
                                        .resize(400, 400) // ajusta el tamaño máximo de la imagen a 400 x 400
                                        .transform(new CircleTransform())
                                        .into(imageview);
                            }

                        }
                    }
                });
    }

    private void cargarDistanciaUsuario(QueryDocumentSnapshot partidaDocument, TextView textView) {
        if (partidaDocument.exists()) {

            /** Obtener distancia a la partida **/
            Double longitudPartida = Double.parseDouble(partidaDocument.getString("Longitud"));
            Double latitudPartida = Double.parseDouble(partidaDocument.getString("Latitud"));


            double dLat = Math.toRadians(latitudPartida - latitud);
            double dLon = Math.toRadians(longitudPartida - longitud);

            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(Math.toRadians(latitud)) * Math.cos(Math.toRadians(latitudPartida)) *
                            Math.sin(dLon / 2) * Math.sin(dLon / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

            double distance = EARTH_RADIUS * c;

            /** Obtener Ciudad donde se ha creado la partida **/
            Geocoder geocoder = new Geocoder(context);
            List<Address> addresses = null;

            try {
                addresses = geocoder.getFromLocation(latitudPartida, longitudPartida, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String city = address.getLocality();  // Obtener la ciudad

                textView.setText("Distancia: " + Math.round(distance * 10.0) / 10.0 + " km. " + city);
            }


        }
    }

    public void clearData() {
        juegosList.clear();
        notifyDataSetChanged();
    }

}
