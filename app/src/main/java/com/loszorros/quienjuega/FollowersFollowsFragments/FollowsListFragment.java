package com.loszorros.quienjuega.FollowersFollowsFragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.loszorros.quienjuega.MenuFragments.MENUFifthFragment;
import com.loszorros.quienjuega.MenuFragments.MENUSecondFragmentProfiles;
import com.loszorros.quienjuega.R;
import com.loszorros.quienjuega.adapter.UsuarioAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FollowsListFragment extends Fragment {


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;

    private RecyclerView recyclerSeguidos;
    private ImageButton botonAtras;
    private static String email;
    private FirebaseFirestore db;
    private String fragmentProvider="";
    private TextView toolbarTitulo;


    public FollowsListFragment() {
        // Required empty public constructor
    }


    public static FollowsListFragment newInstance(String param1, String param2) {
        FollowsListFragment fragment = new FollowsListFragment();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_follows_list, container, false);

        SharedPreferences prefe = getActivity().getSharedPreferences(getString(R.string.pref_datos), Context.MODE_PRIVATE);
        email = prefe.getString("email" ,null);
        db = FirebaseFirestore.getInstance();
        recyclerSeguidos = view.findViewById(R.id.recyclerSeguidosLayout);
        botonAtras = view.findViewById(R.id.chatTooolbarIcon);
        recyclerSeguidos.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerSeguidos.setHasFixedSize(true);
        toolbarTitulo = view.findViewById(R.id.title_toolbar);

        Bundle bundle = getArguments();
        if (bundle != null) {

            fragmentProvider = bundle.getString("Fragment_Provider");
            // Hacer algo con el valor del ID del documento recuperado
        }

        DocumentReference usuariosRef = db.collection("users").document(email).collection("follows").document("usernames");

        // Obtiene los datos del documento
        usuariosRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Obtiene los datos del documento como un mapa
                Map<String, Object> datosUsuario = task.getResult().getData();
                if (datosUsuario == null){

                } else {

                    // Crea una lista para almacenar los nombres de usuario
                    List<String> nombresUsuarios = new ArrayList<>();

                    // Recorre las claves del mapa para buscar los campos que empiezan por "username"
                    for (String clave : datosUsuario.keySet()) {


                        if (clave.startsWith("username")) {
                            // Obtiene el valor del campo y lo agrega a la lista
                            String valor = datosUsuario.get(clave).toString();
                            nombresUsuarios.add(valor);
                        }
                    }


                    // Crea el adaptador personalizado y asigna la lista de nombres de usuario
                    // ...
                    UsuarioAdapter adapter = new UsuarioAdapter(nombresUsuarios);
                    adapter.setOnItemClickListener(new UsuarioAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(String idDocumento) {
                            buscarUsername(idDocumento);
                        }
                    });
                    recyclerSeguidos.setAdapter(adapter);


                }
            } else {

            }
        });

        MENUFifthFragment fragment = new MENUFifthFragment();
        botonAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.popBackStack();
            }

        });

        DocumentReference usuarioNombre = db.collection("users").document(email);
        usuarioNombre.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String username = document.getString("username");
                    toolbarTitulo.setText("Seguidos de @" + username);
                }
            }
        });


        return view;

    }

    private void buscarUsername(String username){
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                String idDocumento = document.getId();
                                loadFragment(idDocumento);

                            }
                        } else {

                        }
                    }
                });
    }


    public void loadFragment(String idDocumento) {
        if (email.equals(idDocumento)) {
            // Crear una instancia del fragmento
            MENUFifthFragment fragment = new MENUFifthFragment();

            // Pasar el ID del documento como argumento al fragmento
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
        } else {
            // Crear una instancia del fragmento
            MENUSecondFragmentProfiles fragment = new MENUSecondFragmentProfiles();

            // Pasar el ID del documento como argumento al fragmento
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
    }

    }
