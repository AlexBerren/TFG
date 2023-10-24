package com.loszorros.quienjuega;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.loszorros.quienjuega.MenuFragments.MENUFifthFragment;
import com.loszorros.quienjuega.MenuFragments.MENUFirstFragment;
import com.loszorros.quienjuega.MenuFragments.MENUSecondFragmentProfiles;
import com.loszorros.quienjuega.adapter.GamerAdapter;
import com.loszorros.quienjuega.adapter.UsuarioAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class GamersListFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private RecyclerView recyclerJugadores;
    private static String email;
    private FirebaseFirestore db;
    String fragmentProvider="";
    private TextView tvNombreJuego;
    private ImageView btnAtras;

    public GamersListFragment() {
    }

    public static GamersListFragment newInstance(String param1, String param2) {
        GamersListFragment fragment = new GamersListFragment();
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
        View view = inflater.inflate(R.layout.fragment_gamers_list, container, false);
        SharedPreferences prefe = getActivity().getSharedPreferences(getString(R.string.pref_datos), Context.MODE_PRIVATE);
        email = prefe.getString("email" ,null);
        db = FirebaseFirestore.getInstance();
        tvNombreJuego = view.findViewById(R.id.title_toolbar);
        btnAtras = view.findViewById(R.id.chatTooolbarIcon);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerJugadores = view.findViewById(R.id.recyclerJugadores);
        recyclerJugadores.setLayoutManager(layoutManager);

        if (email != null){
            OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
                @Override
                public void handleOnBackPressed() {
                    getParentFragmentManager().popBackStack();
                }
            };
            requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
        }

        Bundle bundle = getArguments();

        String nombreJuego = bundle.getString("nombreJuego");

        tvNombreJuego.setText(nombreJuego);




        List<String> nombresUsuarios = new ArrayList<>();
        /** Obtener lista de usuarios que juegan al juego **/
        db.collection("users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    if (documentSnapshot.contains("game1") && documentSnapshot.contains("game2")
                            && documentSnapshot.contains("game3") && documentSnapshot.contains("game4")) {
                        String game1 = documentSnapshot.getString("game1");
                        String game2 = documentSnapshot.getString("game2");
                        String game3 = documentSnapshot.getString("game3");
                        String game4 = documentSnapshot.getString("game4");

                        if(game1.equals(nombreJuego) || game2.equals(nombreJuego)
                                || game3.equals(nombreJuego) || game4.equals(nombreJuego)) {
                            String nombreUsuario = documentSnapshot.getString("username");
                            nombresUsuarios.add(nombreUsuario);
                        }
                    }
                }


                GamerAdapter adapter = new GamerAdapter(nombresUsuarios);
                adapter.setOnItemClickListener(new GamerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(String idDocumento) {
                        buscarUsername(idDocumento);
                    }
                });
                recyclerJugadores.setAdapter(adapter);

            }
        });

        btnAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crear una instancia del fragmento
                MENUFirstFragment fragment = new MENUFirstFragment();

                // Pasar el ID del documento como argumento al fragmento
                Bundle bundle = new Bundle();
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