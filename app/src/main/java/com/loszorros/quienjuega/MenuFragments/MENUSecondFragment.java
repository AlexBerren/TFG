package com.loszorros.quienjuega.MenuFragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.loszorros.quienjuega.R;

import java.util.ArrayList;
import java.util.List;

public class MENUSecondFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String mParam1;
    private String mParam2;
    private static String email;

    Button btnBuscar;
    AutoCompleteTextView txtUsername;

    private List<String> nombresDeUsuariosAutocompletar;
    ArrayAdapter<String> sugerenciasAdapter;


    public MENUSecondFragment() {
    }

    public static MENUSecondFragment newInstance(String param1, String param2) {
        MENUSecondFragment fragment = new MENUSecondFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_second, container, false);

        btnBuscar=view.findViewById(R.id.btnBuscar);
        txtUsername=view.findViewById(R.id.txtBuscarUsuario);

        nombresDeUsuariosAutocompletar = new ArrayList<>();
        sugerenciasAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, nombresDeUsuariosAutocompletar);
        SharedPreferences prefe = getActivity().getSharedPreferences(getString(R.string.pref_datos), Context.MODE_PRIVATE);
        email = prefe.getString("email" ,null);

        //Nos introducimos en users para obtener todos los nombres de usuarios
        CollectionReference usersRef = db.collection("users");

        usersRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        if (document.contains("username")) {
                            String username = document.getString("username");
                            nombresDeUsuariosAutocompletar.add(username); //Obtenemos todos los nombres de usuario y los añadimos al List

                            Context context = getContext();
                            if (context != null) {
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                                        android.R.layout.simple_dropdown_item_1line, nombresDeUsuariosAutocompletar);

                                txtUsername.setAdapter(adapter);
                                txtUsername.setThreshold(0);

                            }

                            txtUsername.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                }

                                @Override
                                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                }

                                @Override
                                public void afterTextChanged(Editable editable) {   //Comprobamos si se ha cambiado el texto
                                    sugerenciasAdapter.notifyDataSetChanged();
                                    String input = editable.toString();
                                    List<String> sugerencias = new ArrayList<>();

                                    //Si se ha cambiado el texto guardamos en "sugerencias" los nombres, y se lo ponemos al EditText
                                    for (String nombre : nombresDeUsuariosAutocompletar) {
                                        if (nombre.toLowerCase().startsWith(input.toLowerCase())) {
                                            sugerencias.add(nombre);
                                        }
                                    }

                                    sugerenciasAdapter = new ArrayAdapter<>(getContext(),
                                            android.R.layout.simple_dropdown_item_1line, sugerencias);


                                    txtUsername.setAdapter(sugerenciasAdapter);
                                    sugerenciasAdapter.notifyDataSetChanged();
                                }
                            });

                        }
                    }

                } else {

                }
            }
        });




        buscarUsername();
        return view;
    }


    public void buscarUsername(){
        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombreBuscado = txtUsername.getText().toString();
                db.collection("users")
                        .whereEqualTo("username", nombreBuscado)
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
        });
    }

    public void loadFragment(String idDocumento) {
        DocumentReference docRef = db.collection("users").document(email);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    String username = documentSnapshot.getString("username");
                    if(txtUsername.getText().toString().equals(username)){
                        FragmentManager fragmentManager = getFragmentManager();
                        Fragment fragment = new MENUFifthFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("ID_DOCUMENTO", idDocumento);
                        bundle.putString("Fragment_Provider","second");

                        // Establecer el Bundle como argumento en el Fragmento 2
                        fragment.setArguments(bundle);
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.frame_container, fragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                    } else{
                        FragmentManager fragmentManager = getFragmentManager();
                        Fragment fragment = new MENUSecondFragmentProfiles();
                        Bundle bundle = new Bundle();
                        bundle.putString("ID_DOCUMENTO", idDocumento);
                        bundle.putString("Fragment_Provider","second");

                        // Establecer el Bundle como argumento en el Fragmento 2
                        fragment.setArguments(bundle);
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.frame_container, fragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Ocurrió un error al acceder al documento
            }
        });




    }
}