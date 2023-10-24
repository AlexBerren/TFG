package com.loszorros.quienjuega.MenuFragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.loszorros.quienjuega.MenuFragments.SubirFoto.UploadPhotoFragment;
import com.loszorros.quienjuega.RegistroLoginActivitys.Complementario.CircleTransform;
import com.loszorros.quienjuega.FollowersFollowsFragments.FollowersListFragment;
import com.loszorros.quienjuega.FollowersFollowsFragments.FollowsListFragment;
import com.loszorros.quienjuega.InformationGameFragment;
import com.loszorros.quienjuega.R;
import com.loszorros.quienjuega.RegistroLoginActivitys.INICIOLoginActivity;
import com.loszorros.quienjuega.adapter.HistorialPartidasAdapter;
import com.loszorros.quienjuega.adapter.PhotoAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class MENUFifthFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;
    private ImageView imagenPerfil;
    private FirebaseFirestore db;
    private static String email;
    ImageButton subir;
    ImageButton partidas;
    ImageButton publicaciones;
    ImageButton btnLogout;

    private TextView juego1;
    private TextView juego2;
    private TextView juego3;
    private TextView juego4;
    private TextView descripcion;
    private TextView toolbarTitulo;

    private TextView numeroSeguidores;
    private TextView numeroSeguidos;
    private TextView textoNumeroSeguidores;
    private TextView textoNumeroSeguidos;
    private View view;

    private List<QueryDocumentSnapshot> photosUsuario = new ArrayList<>();
    private List<DocumentSnapshot> partidasUsuario = new ArrayList<>();
    private RecyclerView uploadedPhotos;

    public MENUFifthFragment() {
    }

    public static MENUFifthFragment newInstance(String param1, String param2) {
        MENUFifthFragment fragment = new MENUFifthFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        //email = primary key para referirnos al usuario
        SharedPreferences prefe = getActivity().getSharedPreferences(getString(R.string.pref_datos), Context.MODE_PRIVATE);
        email = prefe.getString("email" ,null);

        //recuperar instancia de la base de datos
        db = FirebaseFirestore.getInstance();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

         view = inflater.inflate(R.layout.fragment_fifth, container, false);

        //recuperamos la imagen de perfil por usuarios
        imagenPerfil = view.findViewById(R.id.profilePhoto1);
        getPhoto(email);

        partidas = view.findViewById(R.id.btnPartidas);
        publicaciones = view.findViewById(R.id.btnFotos);

        //Obtener el email por preferencias
        SharedPreferences prefe = getActivity().getSharedPreferences(getString(R.string.pref_datos), Context.MODE_PRIVATE);
        email = prefe.getString("email" ,null);

        //recuperar informacion del peril
        numeroSeguidores = view.findViewById(R.id.TxtSeguidores);
        numeroSeguidos = view.findViewById(R.id.TxtSeguidos);

        subir=view.findViewById(R.id.btnSubir);
        juego1 = view.findViewById(R.id.txtJuego1);
        juego2 = view.findViewById(R.id.txtJuego2);
        juego3 = view.findViewById(R.id.txtJuego3);
        juego4 = view.findViewById(R.id.txtJuego4);
        toolbarTitulo = view.findViewById(R.id.title_toolbar);
        descripcion = view.findViewById(R.id.txtDescripcion);
        textoNumeroSeguidores = view.findViewById(R.id.txtTextoSeguidores);
        textoNumeroSeguidos = view.findViewById(R.id.txtTextoSeguidos);
        uploadedPhotos = view.findViewById(R.id.Rpublicaciones);
        uploadedPhotos.setLayoutManager(new LinearLayoutManager(getActivity()));
        uploadedPhotos.setHasFixedSize(true);
        btnLogout = view.findViewById(R.id.logout);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefe = getActivity().getSharedPreferences(getString(R.string.pref_datos), Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = prefe.edit();
                editor.putString("email", null);
                editor.apply();

                Intent i = new Intent(getActivity(), INICIOLoginActivity.class);
                startActivity(i);
                getActivity().finish();

            }
        });

        DocumentReference usuarioNombre = db.collection("users").document(email);
        usuarioNombre.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String username = document.getString("username");
                    toolbarTitulo.setText("@" + username);
                }
            }
        });



        db.collection("users").document(email).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                juego1.setText(documentSnapshot.getString("game1"));
                juego2.setText(documentSnapshot.getString("game2"));
                juego3.setText(documentSnapshot.getString("game3"));
                juego4.setText(documentSnapshot.getString("game4"));

                descripcion.setText(documentSnapshot.getString("description"));
            }
        });
        mostrarCantidadSeguidores();
        mostrarCantidadSeguidos();

        /** Al hacer click en Seguidos, mostrar lista de usuarios Seguidos**/
        textoNumeroSeguidos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crear una instancia del fragmento
                FollowsListFragment fragment = new FollowsListFragment();
                Bundle bundle = new Bundle();
                bundle.putString("Fragment_Provider", "fifth");
                fragment.setArguments(bundle);

                // Obtener el FragmentManager del fragmento actual
                FragmentManager fragmentManager = getParentFragmentManager();

                // Reemplazar el contenido principal del contenedor del fragmento actual con el nuevo fragmento
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.frame_container, fragment);
                transaction.addToBackStack(null); // Agregar la transacción a la pila retroceder (opcional)
                transaction.commit();
            }
        });

        /** Al hacer click en Seguidores, mostrar lista de usuarios Seguidores**/
        textoNumeroSeguidores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crear una instancia del fragmento
                FollowersListFragment fragment = new FollowersListFragment();

                Bundle bundle = new Bundle();
                bundle.putString("Fragment_Provider", "fifth");
                fragment.setArguments(bundle);

                // Obtener el FragmentManager del fragmento actual
                FragmentManager fragmentManager = getParentFragmentManager();

                // Reemplazar el contenido principal del contenedor del fragmento actual con el nuevo fragmento
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.frame_container, fragment);
                transaction.addToBackStack(null); // Agregar la transacción a la pila retroceder (opcional)
                transaction.commit();
            }
        });

        /** Al hacer click en SUBIR, te lleva a la actividad de subir una foto**/
        subir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                Fragment fragment = new UploadPhotoFragment();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.frame_container, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        /** MOSTRAR PUBLICACIONES **/
        CollectionReference usuariosRef = db.collection("users").document(email).collection("photos");

        usuariosRef.orderBy("fecha_subida", Query.Direction.DESCENDING).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                PhotoAdapter adapterPhotos = new PhotoAdapter(photosUsuario, getContext());
                CollectionReference photosRef = usuariosRef;

                photosRef.get().addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot photoDocument : querySnapshot) {
                        photosUsuario.add(photoDocument);
                    }

                    Collections.sort(photosUsuario, (doc1, doc2) -> {
                        Timestamp timestamp1 = doc1.getTimestamp("fecha_subida");
                        Timestamp timestamp2 = doc2.getTimestamp("fecha_subida");
                        return timestamp2.compareTo(timestamp1);
                    });

                    if (photosUsuario.isEmpty()) {

                    }

                    uploadedPhotos.setAdapter(adapterPhotos);
                }).addOnFailureListener(e -> {

                });
            } else {

            }
        });
        photosUsuario.clear();

        /** MOSTRAR HISTORIAL DE PARTIDAS **/
        partidasUsuario.clear();
        partidas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                partidasUsuario.clear();
                CollectionReference usersCollectionRef = db.collection("users");
                usersCollectionRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        HistorialPartidasAdapter adaptadorPartidas = new HistorialPartidasAdapter(partidasUsuario, getContext());


                        int numUsers = task.getResult().size();
                        AtomicInteger counter = new AtomicInteger(0);

                        for (DocumentSnapshot userDoc : task.getResult()) {
                            CollectionReference gamesCollectionRef = userDoc.getReference().collection("games");

                            gamesCollectionRef.orderBy("Date", Query.Direction.ASCENDING).get().addOnCompleteListener(gamesTask -> {
                                if (gamesTask.isSuccessful()) {
                                    for (DocumentSnapshot gameDoc : gamesTask.getResult()) {
                                        Map<String, Object> gameData = gameDoc.getData();
                                        for (String field : gameData.keySet()) {
                                            if (field.startsWith("participante")) {
                                                db.collection("users").document(email).get().addOnSuccessListener(userSnapshot -> {
                                                    if (userSnapshot.exists()) {
                                                        String username = userSnapshot.getString("username");
                                                        Object value = gameData.get(field);
                                                        if (value != null && value.equals(username)) {

                                                            partidasUsuario.add(gameDoc);
                                                        }
                                                    }
                                                    adaptadorPartidas.setOnItemClickListener(new HistorialPartidasAdapter.OnItemClickListener() {

                                                        @Override
                                                        public void onItemClick(DocumentSnapshot gameDoc) {

                                                            loadFragment(gameDoc);
                                                        }
                                                    });
                                                        uploadedPhotos.setAdapter(adaptadorPartidas);
                                                        adaptadorPartidas.notifyDataSetChanged();

                                                });
                                            }
                                        }
                                    }
                                }
                                int currentCount = counter.incrementAndGet();
                                if (currentCount == numUsers) {
                                    uploadedPhotos.setAdapter(adaptadorPartidas);
                                    adaptadorPartidas.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                });
            }
        });


        publicaciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /** MOSTRAR PUBLICACIONES AL HACER CLICK **/
                photosUsuario.clear();
                CollectionReference usuariosRef = db.collection("users").document(email).collection("photos");

                usuariosRef.orderBy("fecha_subida", Query.Direction.DESCENDING).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        PhotoAdapter adapterPhotos = new PhotoAdapter(photosUsuario, getContext());
                        CollectionReference photosRef = usuariosRef;

                        photosRef.get().addOnSuccessListener(querySnapshot -> {
                            for (QueryDocumentSnapshot photoDocument : querySnapshot) {
                                photosUsuario.add(photoDocument);
                            }

                            Collections.sort(photosUsuario, (doc1, doc2) -> {
                                Timestamp timestamp1 = doc1.getTimestamp("fecha_subida");
                                Timestamp timestamp2 = doc2.getTimestamp("fecha_subida");
                                return timestamp2.compareTo(timestamp1);
                            });

                            if (photosUsuario.isEmpty()) {

                            }

                            uploadedPhotos.setAdapter(adapterPhotos);
                        }).addOnFailureListener(e -> {

                        });
                    } else {

                    }
                });
            }
        });



        return view;
    }

    public void mostrarCantidadSeguidores(){
        // Obtener una referencia al documento en la colección de Firebase Firestore
        DocumentReference docRef = db.collection("users").document(email).collection("followers").document("usernames");

        // Obtener el documento actual y contar el número de campos
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    int cantidadSeguidores = documentSnapshot.getData().size();
                    numeroSeguidores.setText(cantidadSeguidores+"");
                } else {
                    int cantidadSeguidores = 0;
                    numeroSeguidores.setText(cantidadSeguidores+"");
                }
            }
        });
    }

    public void mostrarCantidadSeguidos(){
        // Obtener una referencia al documento en la colección de Firebase Firestore
        DocumentReference docRef = db.collection("users").document(email).collection("follows").document("usernames");

        // Obtener el documento actual y contar el número de campos
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    int cantidadSeguidos = documentSnapshot.getData().size();
                    numeroSeguidos.setText(cantidadSeguidos+"");
                } else {
                    int cantidadSeguidos = 0;
                    numeroSeguidos.setText(cantidadSeguidos+"");
                }
                // Obtener el número de campos del documento

            }
        });

    }


        /**
        * Obtener la foto de perfil del usuario correspondiente
         * */
        private void getPhoto(String id){      //Obtener la foto del usuario y mostrarla
            db.collection("users").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    String photoUser = documentSnapshot.getString("photo");
                    try {
                        if(!photoUser.equals("")){
                            Picasso.with(getActivity())
                                    .load(photoUser)
                                    .resize(400, 400) // ajusta el tamaño máximo de la imagen a 300 x 300
                                    .transform(new CircleTransform())
                                    .into(imagenPerfil);
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

    public void loadFragment(DocumentSnapshot gameDoc) {
            InformationGameFragment fragment = new InformationGameFragment();

            Bundle bundle = new Bundle();
            bundle.putString("participante1", gameDoc.getString("participante1"));
            bundle.putString("idDocumento", gameDoc.getId());
            fragment.setArguments(bundle);

            // Obtener el FragmentManager del fragmento actual
            FragmentManager fragmentManager = getParentFragmentManager();

            // Reemplazar el contenido principal del contenedor del fragmento actual con el nuevo fragmento
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.frame_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();

    }


    }
