package com.loszorros.quienjuega;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.loszorros.quienjuega.MenuFragments.MENUFifthFragment;
import com.loszorros.quienjuega.MenuFragments.MENUThirdFragment;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.PrimitiveIterator;


public class InformationGameFragment extends Fragment {


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private FirebaseFirestore db;
    private TextView nombreCreador;
    private TextView nombreJuego;
    private TextView fechaJuego;
    private TextView horaJuego;
    private TextView participantes;
    private TextView ubicacion;
    private EditText notas;
    private Button guardarNotas;
    private TextView titulo;
    private ImageView atras;

    String idDocumento;
    String primerParticipante;

    private String email;


    public InformationGameFragment() {

    }


    public static InformationGameFragment newInstance(String param1, String param2) {
        InformationGameFragment fragment = new InformationGameFragment();
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
        db = FirebaseFirestore.getInstance();
        Bundle bundle = getArguments();
         idDocumento = bundle.getString("idDocumento");
         primerParticipante = bundle.getString("participante1");


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_information_game, container, false);

        nombreCreador = view.findViewById(R.id.tvNombreCreador2);
        nombreJuego = view.findViewById(R.id.tvNombreJuego1);
        fechaJuego = view.findViewById(R.id.tvFecha2);
        horaJuego = view.findViewById(R.id.tvHora2);
        participantes = view.findViewById(R.id.tvNombreParticipantes);
        notas = view.findViewById(R.id.etNotas);
        guardarNotas = view.findViewById(R.id.btnGuardarNotas);
        ubicacion = view.findViewById(R.id.tvUbicacion2);
        titulo = view.findViewById(R.id.title_toolbar);
        atras = view.findViewById(R.id.chatTooolbarIcon);

        SharedPreferences prefe = getActivity().getSharedPreferences(getString(R.string.pref_datos), Context.MODE_PRIVATE);
        email = prefe.getString("email" ,null);

        DocumentReference userRef = db.collection("users").document(email);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String username = document.getString("username");
                    titulo.setText("@" + username);
                }
            }
        });

        atras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crear una instancia del fragmento
                MENUFifthFragment fragment = new MENUFifthFragment();

                // Pasar el ID del documento como argumento al fragmento
                Bundle bundle = new Bundle();
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

        CollectionReference usersCollectionRef = db.collection("users");
        usersCollectionRef.get().addOnCompleteListener(usersTask -> {
            if (usersTask.isSuccessful()) {
                for (DocumentSnapshot userDoc : usersTask.getResult()) {
                    CollectionReference gamesCollectionRef = userDoc.getReference().collection("games");
                    Query query = gamesCollectionRef.whereEqualTo(FieldPath.documentId(), idDocumento)
                            .whereEqualTo("participante1", primerParticipante);

                    query.get().addOnCompleteListener(gamesTask -> {
                        if (gamesTask.isSuccessful()) {
                            ArrayList<String> listaParticipantes = new ArrayList<>();
                            for (DocumentSnapshot gameDoc : gamesTask.getResult()) {
                                for (String field : gameDoc.getData().keySet()) {
                                    if (field.startsWith("participante")) {
                                        String value = gameDoc.getString(field);
                                        if (value != null && !field.equals("participante1")) {
                                            listaParticipantes.add("@" +value);
                                        }
                                    }
                                }



                                String participantesString = TextUtils.join(", ", listaParticipantes);

                                String juego = gameDoc.getString("Game");
                                String fecha = gameDoc.getString("Date");
                                String hora = gameDoc.getString("Hour");
                                String creador = gameDoc.getString("participante1");
                                String notas1 = gameDoc.getString("notas");

                                nombreJuego.setText(juego);
                                nombreCreador.setText("@"+creador);
                                fechaJuego.setText(fecha);
                                horaJuego.setText(hora);
                                participantes.setText(participantesString);
                                notas.setText(notas1);

                                Double longitudPartida = Double.parseDouble(gameDoc.getString("Longitud"));
                                Double latitudPartida = Double.parseDouble(gameDoc.getString("Latitud"));

                                /** Obtener Ciudad donde se ha creado la partida **/
                                Geocoder geocoder = new Geocoder(getContext());
                                List<Address> addresses = null;

                                try {
                                    addresses = geocoder.getFromLocation(latitudPartida, longitudPartida, 1);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                if (addresses != null && !addresses.isEmpty()) {
                                    Address address = addresses.get(0);
                                    String city = address.getLocality();  // Obtener la ciudad

                                    ubicacion.setText(city);
                                }

                                guardarNotas.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String nuevasNotas = notas.getText().toString();
                                        gameDoc.getReference().update("notas", nuevasNotas)
                                                .addOnSuccessListener(aVoid -> {
                                                    loadFragment();
                                                });
                                    }
                                });

                            }
                        }
                    });
                }
            }
        });


        return view;
    }

    public void loadFragment() {
        MENUFifthFragment fragment = new MENUFifthFragment();


        // Obtener el FragmentManager del fragmento actual
        FragmentManager fragmentManager = getParentFragmentManager();

        // Reemplazar el contenido principal del contenedor del fragmento actual con el nuevo fragmento
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

    }
}