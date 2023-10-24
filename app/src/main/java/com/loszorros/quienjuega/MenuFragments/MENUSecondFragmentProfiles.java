package com.loszorros.quienjuega.MenuFragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.loszorros.quienjuega.RegistroLoginActivitys.Complementario.CircleTransform;
import com.loszorros.quienjuega.FollowersFollowsFragments.OtherFollowersListFragment;
import com.loszorros.quienjuega.FollowersFollowsFragments.OtherFollowsListFragment;
import com.loszorros.quienjuega.R;
import com.loszorros.quienjuega.adapter.PhotoAdapterSecond;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MENUSecondFragmentProfiles extends Fragment {


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;
    private ImageView imagenPerfil;
    private FirebaseFirestore db;
    private static String email;
    Button seguir;
    boolean encontrado;

    String usuarioDejarDeSeguir;
    String numeroCampoDejarDeSeguir;

    String usuarioDejarDeSeguido;
    String numeroCampoDejarDeSeguido;

    String nombreUsuarioLogeado;
    private TextView juego1;
    private TextView juego2;
    private TextView juego3;
    private TextView juego4;
    private TextView userName;
    private TextView descripcion;
    private TextView numeroSeguidores;
    private TextView numeroSeguidos;
    private TextView txtSeguidores;
    private TextView txtSeguidos;

    private ImageButton atras;
    private List<QueryDocumentSnapshot> fotosList = new ArrayList<>();

    private RecyclerView recyclerFotos;

    public MENUSecondFragmentProfiles() {
        // Required empty public constructor
    }

    public static MENUSecondFragmentProfiles newInstance(String param1, String param2) {
        MENUSecondFragmentProfiles fragment = new MENUSecondFragmentProfiles();
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



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_m_e_n_u_second_profiles, container, false);

        //Instancia FireBase
        db = FirebaseFirestore.getInstance();

        //Obtener el email por preferencias
        SharedPreferences prefe = getActivity().getSharedPreferences(getString(R.string.pref_datos), Context.MODE_PRIVATE);
        email = prefe.getString("email" ,null);

        // Obtener el valor del idDocumento usando el Bundle
        Bundle bundle = getArguments();
        String idDocumento = bundle.getString("ID_DOCUMENTO");
        String fragmentProvider = bundle.getString("Fragment_Provider");

        seguir=view.findViewById(R.id.btnSubir);
        juego1 = view.findViewById(R.id.txtJuego1);
        juego2 = view.findViewById(R.id.txtJuego2);
        juego3 = view.findViewById(R.id.txtJuego3);
        juego4 = view.findViewById(R.id.txtJuego4);
        userName = view.findViewById(R.id.title_toolbar);
        descripcion = view.findViewById(R.id.txtDescripcion);
        numeroSeguidores = view.findViewById(R.id.txtSeguidores);
        numeroSeguidos = view.findViewById(R.id.txtSeguidos);
        txtSeguidores = view.findViewById(R.id.txtTextoSeguidores);
        txtSeguidos = view.findViewById(R.id.txtTextoSeguidos2);
        atras = view.findViewById(R.id.chatTooolbarIcon);

        atras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.popBackStack();
            }

        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerFotos = view.findViewById(R.id.Rpublicaciones2);
        recyclerFotos.setLayoutManager(layoutManager);

        /** Obtener las fotos del usuario **/
        CollectionReference photosRef = db.collection("users").document(idDocumento).collection("photos");
        photosRef.orderBy("fecha_subida", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                for (QueryDocumentSnapshot photoDocument : querySnapshot) {
                    fotosList.add(photoDocument);


                }
                Collections.sort(fotosList, new Comparator<QueryDocumentSnapshot>() {
                    @Override
                    public int compare(QueryDocumentSnapshot doc1, QueryDocumentSnapshot doc2) {
                        Timestamp timestamp1 = doc1.getTimestamp("fecha_subida");
                        Timestamp timestamp2 = doc2.getTimestamp("fecha_subida");
                        return timestamp2.compareTo(timestamp1);
                    }
                });


                //TreeMap<String, QueryDocumentSnapshot> fotosOrdenadas = new TreeMap<>(fotosMap);
                PhotoAdapterSecond adapterPhotos = new PhotoAdapterSecond(fotosList, getContext());
                recyclerFotos.setAdapter(adapterPhotos);
            }
        });


        txtSeguidores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crear una instancia del fragmento
                OtherFollowersListFragment fragment = new OtherFollowersListFragment();
                Bundle bundle = new Bundle();
                bundle.putString("ID_DOCUMENTO", idDocumento);
                bundle.putString("Fragment_Provider", fragmentProvider);
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





        txtSeguidos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crear una instancia del fragmento
                OtherFollowsListFragment fragment = new OtherFollowsListFragment();
                Bundle bundle = new Bundle();
                bundle.putString("ID_DOCUMENTO", idDocumento);
                bundle.putString("Fragment_Provider", fragmentProvider);
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






        //Recuperar informacion del peril
        db.collection("users").document(idDocumento).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                juego1.setText(documentSnapshot.getString("game1"));
                juego2.setText(documentSnapshot.getString("game2"));
                juego3.setText(documentSnapshot.getString("game3"));
                juego4.setText(documentSnapshot.getString("game4"));
                userName.setText("@" + documentSnapshot.getString("username"));
                descripcion.setText(documentSnapshot.getString("description"));
                buscarStringEnMiDocumentoSeguidos();        //Comprueba si el usuario logeado sigue ya al usaurio buscado
                buscarStringEnMiDocumentoSeguidores(idDocumento);

                DocumentReference documentoUsuarioLogeadoNombre = db.collection("users").document(email);
                documentoUsuarioLogeadoNombre.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                 @Override
                 public void onSuccess(DocumentSnapshot documentSnapshot2) {
                     if (documentSnapshot2.exists()) {
                         nombreUsuarioLogeado = documentSnapshot2.getString("username");
                     }
                 }
             });

                seguir.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (encontrado) {

                            dejarDeSeguir(usuarioDejarDeSeguir, numeroCampoDejarDeSeguir, idDocumento, usuarioDejarDeSeguido, numeroCampoDejarDeSeguido);
                        } else {

                            sistemaSeguimiento(idDocumento);
                            encontrado = true;
                        }
                    }
                });

            }
        });


        //Recuperamos la imagen de perfil del usuario buscado
        imagenPerfil = view.findViewById(R.id.profilePhoto1);
        getPhoto(idDocumento);

        //Mostramos los SEGUIDOS y SEGUIDORES que tiene el usuario buscado al cargar el fragmento
        mostrarCantidadSeguidores(idDocumento);
        mostrarCantidadSeguidos(idDocumento);

        return view;
    }

    /** Metodo que se ejecuta nada mas cargar este fragmento.
     * Comprueba si el usuario buscado ya esta siendo seguido por el usuario logeado
     * si es asi muestra el boton de "Dejar de Seguir"
     * en caso contrario muestra el boton "Seguir"**/

    public void buscarStringEnMiDocumentoSeguidos() {
        DocumentReference documentoUsuarioLogeado = db.collection("users").document(email).collection("follows").document("usernames");
        documentoUsuarioLogeado.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> campos = document.getData();

                        //Recorremos con un for la lista de los usuarios SEGUIDOS por el usuario logeado
                        for (String campo : campos.keySet()) {
                            if (campo.startsWith("username")) {     //Mirar los campos en "follows" que empiecen por "username" (todos)
                                String usernameDocumento = document.getString(campo);   //Coge el valor del campo que empiece por username
                                String usernameBuscado = userName.getText().toString().substring(1);    //Coge el nombre del @ que has buscado

                                if (usernameBuscado.equals(usernameDocumento)) {    //Si el @ que has buscado se encuentra en tu lista de Seguidos = true
                                    seguir.setText("DEJAR DE SEGUIR");
                                    seguir.setBackgroundColor(Color.GRAY);

                                    // Obtén los parámetros de diseño actuales del botón
                                    ViewGroup.LayoutParams layoutParams = seguir.getLayoutParams();
                                    layoutParams.width = seguir.getWidth(); // Pone a 600 el ancho del boton
                                    seguir.setLayoutParams(layoutParams);

                                    usuarioDejarDeSeguir = usernameDocumento;
                                    numeroCampoDejarDeSeguir = campo;
                                    encontrado = true;
                                    break;
                                } else {

                                }
                            }
                        }
                    }
                }
            }
        });
    }

    /** Metodo que se ejecuta nada mas cargar este fragmento.
     * Comprueba si el username del usuario logeado se encuentra
     * en la lista de seguidores del usuario buscado
     * Si es asi guarda el nombre del campo del username (usernameX)
     * y el valor que tiene ese campo.
     * Este metodo se usa a la hora de dejar de seguir.**/

    public void buscarStringEnMiDocumentoSeguidores(String idDocumento) {
        DocumentReference documentoUsuarioLogeado = db.collection("users").document(idDocumento).collection("followers").document("usernames");
        documentoUsuarioLogeado.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> campos = document.getData();

                        for (String campo : campos.keySet()) {
                            if (campo.startsWith("username")) {
                                String usernameDocumento = document.getString(campo);

                                /** Obtener el username del user logeado **/
                                DocumentReference myUser = db.collection("users").document(email);

                                myUser.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if (documentSnapshot.exists()) {
                                            String username = documentSnapshot.getString("username");
                                            String usernameBuscado = username;
                                            if (usernameBuscado.equals(usernameDocumento)) {
                                                usuarioDejarDeSeguido = usernameDocumento;
                                                numeroCampoDejarDeSeguido = campo;

                                            }
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            }
        });
    }

    /** Metodo que se ejecuta al pulsar el boton de "Seguir".
     * Añade el username del usuario buscado a la coleccion de SEGUIDOS del usuario logeado.
     * Añade el username del usuario logeado a la coleccion de SEGUIDORES del usuario buscado.**/

    public void sistemaSeguimiento(String idDocumento) {

            /** Añadir SEGUIDO a lista de seguidos del usuario que ha iniciado sesion **/
            DocumentReference searchedUser = db.collection("users").document(idDocumento);
            searchedUser.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        annadirSeguido(username);
                        encontrado = true;
                        seguir.setText("DEJAR DE SEGUIR");
                        seguir.setBackgroundColor(Color.GRAY);

                        // Obtén los parámetros de diseño actuales del botón
                        ViewGroup.LayoutParams layoutParams = seguir.getLayoutParams();
                        layoutParams.width = seguir.getWidth(); // Reemplaza 400 con el ancho deseado
                        seguir.setLayoutParams(layoutParams);
                    }
                }
            });

            /** Añadir SEGUIDOR a la lista de seguidores del usuario buscado **/
            DocumentReference myUser = db.collection("users").document(email);

            // Obtener el valor del campo "username" del usuario que ha iniciado sesion
            myUser.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        annadirSeguidor(username, idDocumento);
                        encontrado = true;
                    }
                }
            });
    }



    /** Metodo que se ejecuta al pulsar el boton de "Dejar de Seguir".
     * Elimina el username del usuario buscado en la coleccion de SEGUIDOS del usuario logeado.
     * Elimina el username del usuario logeado en la coleccion de SEGUIDORES del usuario buscado.**/

    public void dejarDeSeguir(String campoNombreEncontrado, String campo, String idDocumento, String usuarioDejarDeSeguido, String numeroCampoDejarDeSeguido) {

        /** Eliminar SEGUIDO de lista de seguidos del usuario que ha iniciado sesion **/
        DocumentReference documentoUserLogeado = db.collection("users").document(email).collection("follows").document("usernames");
        documentoUserLogeado.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Map<String, Object> mapUserLogeado = documentSnapshot.getData();
                            String valorCampo = documentSnapshot.getString(campo);

                            if (valorCampo.equals(campoNombreEncontrado)) {
                                mapUserLogeado.remove(campo);
                                seguir.setText("SEGUIR");
                                seguir.setBackgroundColor(Color.parseColor("#42055A"));

                                // Obtén los parámetros de diseño actuales del botón
                                ViewGroup.LayoutParams layoutParams = seguir.getLayoutParams();
                                layoutParams.width = seguir.getWidth(); // Reemplaza 400 con el ancho deseado
                                seguir.setLayoutParams(layoutParams);
                                encontrado = false;

                                // Iterar sobre los campos restantes para actualizar sus nombres
                                Map<String, Object> newData = new HashMap<>();
                                for (Map.Entry<String, Object> entry : mapUserLogeado.entrySet()) {
                                    String key = entry.getKey();
                                    if (key.startsWith("username")) {
                                        int index = Integer.parseInt(key.substring(8));
                                        int numeroDeCampo = Integer.parseInt(campo.substring(8));
                                        if (index > numeroDeCampo) {
                                            // Restar 1 al índice del campo y actualizar su nombre
                                            String newKey = "username" + (index - 1);
                                            newData.put(newKey, entry.getValue());
                                        } else {
                                            newData.put(key, entry.getValue());
                                        }
                                    }
                                }
                                // Actualizar los datos en Firebase
                                documentoUserLogeado.set(newData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }

        });



        /** Eliminar SEGUIDOR de la lista de seguidores del usuario buscado **/
        DocumentReference documentoUserBuscado = db.collection("users").document(idDocumento).collection("followers").document("usernames");
        documentoUserBuscado.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot1) {
                if (documentSnapshot1.exists()) {
                    Map<String, Object> mapUserBuscado = documentSnapshot1.getData();
                    String valorCampo2 = documentSnapshot1.getString(numeroCampoDejarDeSeguido);

                    if (valorCampo2.equals(usuarioDejarDeSeguido)) {
                        mapUserBuscado.remove(numeroCampoDejarDeSeguido);

                        // Iterar sobre los campos restantes para actualizar sus nombres
                        Map<String, Object> newData = new HashMap<>();
                        for (Map.Entry<String, Object> entry : mapUserBuscado.entrySet()) {
                            String key = entry.getKey();
                            if (key.startsWith("username")) {
                                int index = Integer.parseInt(key.substring(8));
                                int numeroDeCampo = Integer.parseInt(numeroCampoDejarDeSeguido.substring(8));
                                if (index > numeroDeCampo) {
                                    // Restar 1 al índice del campo y actualizar su nombre
                                    String newKey = "username" + (index - 1);
                                    newData.put(newKey, entry.getValue());
                                } else {
                                    newData.put(key, entry.getValue());
                                }
                            }
                        }
                        // Actualizar los datos en Firebase
                        documentoUserBuscado.set(newData).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                mostrarCantidadSeguidores(idDocumento);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                    }

                            }
                        }
                    });


    }


    /** Añade el valor del parametro username a la lista de
     * SEGUIDOS del usuario logeado.
     * Lo añade con nombre de campo "username + tamaño de la coleccion SEGUIDOS"**/

    public void annadirSeguido(String username){
        DocumentReference docRef = db.collection("users").document(email).collection("follows").document("usernames");
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // Obtener el número de campos del documento
                    int numCampos = documentSnapshot.getData().size();

                    // Usar el número de campos como nombre para el siguiente campo
                    String nuevoNombreCampo = "username" + (numCampos + 1);

                    // Crear un nuevo campo con el nombre generado
                    Map<String, Object> nuevoCampo = new HashMap<>();
                    nuevoCampo.put(nuevoNombreCampo, username);

                    // Actualizar el documento con el nuevo campo
                    docRef.update(nuevoCampo);
                    usuarioDejarDeSeguir = username;
                    numeroCampoDejarDeSeguir = nuevoNombreCampo;
                }else{
                    String nuevoNombreCampo = "username1";
                    Map<String, Object> nuevoCampo = new HashMap<>();
                    nuevoCampo.put(nuevoNombreCampo, username);

                    // Actualizar el documento con el nuevo campo
                    docRef.set(nuevoCampo);
                    usuarioDejarDeSeguir = username;
                    numeroCampoDejarDeSeguir = nuevoNombreCampo;
                }
            }
        });
    }

    /** Añade el username del usuario logeado a la lista de
     * SEGUIDORES del usuario buscado.
     * Lo añade con nombre de campo "username + tamaño de la coleccion SEGUIDORES"**/

    public void annadirSeguidor(String username, String idDocumento){
        DocumentReference docRef = db.collection("users").document(idDocumento).collection("followers").document("usernames");
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // Obtener el número de campos del documento
                    int numCampos = documentSnapshot.getData().size();

                    // Usar el número de campos como nombre para el siguiente campo
                    String nuevoNombreCampo = "username" + (numCampos + 1);

                    // Crear un nuevo campo con el nombre generado
                    Map<String, Object> nuevoCampo = new HashMap<>();
                    nuevoCampo.put(nuevoNombreCampo, username);

                    // Actualizar el documento con el nuevo campo
                    docRef.update(nuevoCampo);
                    usuarioDejarDeSeguido = username;
                    numeroCampoDejarDeSeguido = nuevoNombreCampo;
                    mostrarCantidadSeguidores(idDocumento);
                } else {
                    String nuevoNombreCampo = "username1";

                    // Crear un nuevo campo con el nombre generado
                    Map<String, Object> nuevoCampo = new HashMap<>();
                    nuevoCampo.put(nuevoNombreCampo, username);

                    // Actualizar el documento con el nuevo campo
                    docRef.set(nuevoCampo);
                    usuarioDejarDeSeguido = username;
                    numeroCampoDejarDeSeguido = nuevoNombreCampo;
                    mostrarCantidadSeguidores(idDocumento);
                }
            }
        });

    }

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

    public void mostrarCantidadSeguidores(String idDocumento){
        // Obtener una referencia al documento en la colección de Firebase Firestore
        DocumentReference docRef = db.collection("users").document(idDocumento).collection("followers").document("usernames");

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

    public void mostrarCantidadSeguidos(String idDocumento){
        // Obtener una referencia al documento en la colección de Firebase Firestore
        DocumentReference docRef = db.collection("users").document(idDocumento).collection("follows").document("usernames");

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


        }