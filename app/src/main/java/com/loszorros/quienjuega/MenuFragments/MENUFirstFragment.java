package com.loszorros.quienjuega.MenuFragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.loszorros.quienjuega.RegistroLoginActivitys.Complementario.CircleTransform;
import com.loszorros.quienjuega.GamersListFragment;
import com.loszorros.quienjuega.R;
import com.loszorros.quienjuega.adapter.PhotoAdapterFirst;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MENUFirstFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private ImageView chatIcon;
    private FirebaseFirestore db;
    private static String email;
    private TextView titleUsername;
    private RecyclerView uploadedPhotos;
    private List<QueryDocumentSnapshot> fotosList = new ArrayList<>();
    private int j = 0;

    ImageView juego1;
    ImageView juego2;
    ImageView juego3;
    ImageView juego4;
    ImageView juego5;

    TextView tvJuego1;
    TextView tvJuego2;
    TextView tvJuego3;
    TextView tvJuego4;
    TextView tvJuego5;

    private ImageView[] listaFotosJuegos = new ImageView[5];
    private TextView[] listaTextosJuegos = new TextView[5];


    public MENUFirstFragment() {
    }

    public static MENUFirstFragment newInstance(String param1, String param2) {
        MENUFirstFragment fragment = new MENUFirstFragment();
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
        View view = inflater.inflate(R.layout.fragment_first, container, false);
        uploadedPhotos = view.findViewById(R.id.Rpublicaciones2);
        uploadedPhotos.setLayoutManager(new LinearLayoutManager(getActivity()));
        uploadedPhotos.setHasFixedSize(true);

        juego1 = view.findViewById(R.id.imgJuego1);
        juego2 = view.findViewById(R.id.imgJuego2);
        juego3 = view.findViewById(R.id.imgJuego3);
        juego4 = view.findViewById(R.id.imgJuego4);
        juego5 = view.findViewById(R.id.imgJuego5);

        tvJuego1 = view.findViewById(R.id.tvJuego1);
        tvJuego2 = view.findViewById(R.id.tvJuego2);
        tvJuego3 = view.findViewById(R.id.tvJuego3);
        tvJuego4 = view.findViewById(R.id.tvJuego4);
        tvJuego5 = view.findViewById(R.id.tvJuego5);

        listaFotosJuegos[0] = juego1;
        listaFotosJuegos[1] = juego2;
        listaFotosJuegos[2] = juego3;
        listaFotosJuegos[3] = juego4;
        listaFotosJuegos[4] = juego5;

        listaTextosJuegos[0] = tvJuego1;
        listaTextosJuegos[1] = tvJuego2;
        listaTextosJuegos[2] = tvJuego3;
        listaTextosJuegos[3] = tvJuego4;
        listaTextosJuegos[4] = tvJuego5;


        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                if (getActivity().getSupportFragmentManager().getBackStackEntryCount() == 1) {
                    getActivity().finish();
                }else {
                    getParentFragmentManager().popBackStack();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);


        //conexion base de dato + obtener email
        SharedPreferences prefe = getActivity().getSharedPreferences(getString(R.string.pref_datos), Context.MODE_PRIVATE);
        email = prefe.getString("email" ,null);
        db = FirebaseFirestore.getInstance();


        /** OBTENER FOTOS SUBIDAS **/
        fotosList.clear();
        /** Obtener nombre de las personas a las que sigues **/
        DocumentReference usuariosRef = db.collection("users").document(email).collection("follows").document("usernames");
        usuariosRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    List<String> listaNombreUsuariosSeguidos = new ArrayList<>();

                    // Obtener los campos que comienzan con "username"
                    Map<String, Object> fields = documentSnapshot.getData();
                    for (String key : fields.keySet()) {
                        if (key.startsWith("username")) {
                            String nombreUsuarioSeguido = documentSnapshot.getString(key);


                            /** Obtener ID de los usuarios a los que sigues**/
                            CollectionReference usersRef = db.collection("users");
                            usersRef.whereEqualTo("username", nombreUsuarioSeguido)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot querySnapshot) {
                                            for (QueryDocumentSnapshot document : querySnapshot) {
                                                String IDusuarioSeguido = document.getId();



                                                /** Obtener las fotos del usuario **/
                                                CollectionReference photosRef = db.collection("users").document(IDusuarioSeguido).collection("photos");
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
                                                        PhotoAdapterFirst adapterPhotos = new PhotoAdapterFirst(fotosList, getContext());
                                                        adapterPhotos.setOnItemClickListener(new PhotoAdapterFirst.OnItemClickListener() {
                                                            @Override
                                                            public void onItemClick(String idDocumento) {
                                                                buscarUsername(idDocumento);
                                                            }
                                                        });
                                                        uploadedPhotos.setAdapter(adapterPhotos);
                                                    }
                                                });
                                            }
                                        }
                                    });
                        }
                    }

                }
            }

        });


        /** OBTENER 5 JUEGOS MAS JUGADOS **/
        db.collection("users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                HashMap<String, Integer> valueCountMap = new HashMap<>();

                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    if (documentSnapshot.contains("game1") && documentSnapshot.contains("game2")
                            && documentSnapshot.contains("game3") && documentSnapshot.contains("game4")) {
                        String game1 = documentSnapshot.getString("game1");
                        String game2 = documentSnapshot.getString("game2");
                        String game3 = documentSnapshot.getString("game3");
                        String game4 = documentSnapshot.getString("game4");

                        // Incrementar el contador para cada valor
                        incrementValueCount(valueCountMap, game1);
                        incrementValueCount(valueCountMap, game2);
                        incrementValueCount(valueCountMap, game3);
                        incrementValueCount(valueCountMap, game4);
                    }
                }

                // Obtener los 5 valores más frecuentes
                List<Map.Entry<String, Integer>> sortedEntries = getSortedEntries(valueCountMap);
                List<String> topValues = getTopValues(sortedEntries, 5);



                for (String value : topValues) {
                    j = 0;

                    /** Coger la foto de los juegos **/
                    db.collection("games")
                            .whereEqualTo("name", value)
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                        String urlPhoto = documentSnapshot.getString("url");
                                        String nombreJuego = documentSnapshot.getString("name");
                                        listaTextosJuegos[j].setText(nombreJuego);

                                        Picasso.with(listaFotosJuegos[j].getContext())
                                                .load(urlPhoto)
                                                .resize(400, 400) // ajusta el tamaño máximo de la imagen a 400 x 400
                                                .transform(new CircleTransform())
                                                .into(listaFotosJuegos[j]);

                                        listaFotosJuegos[j].setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                GamersListFragment fragment = new GamersListFragment();
                                                Bundle bundle = new Bundle();
                                                bundle.putString("nombreJuego", nombreJuego);
                                                //bundle.putString("Fragment_Provider", fragmentProvider);
                                                fragment.setArguments(bundle);

                                                // Obtener el FragmentManager del fragmento actual
                                                FragmentManager fragmentManager = getParentFragmentManager();

                                                // Reemplazar el contenido principal del contenedor del fragmento actual con el nuevo fragmento
                                                FragmentTransaction transaction = fragmentManager.beginTransaction();
                                                transaction.replace(R.id.frame_container, fragment);
                                                transaction.addToBackStack(null);
                                                //transaction.addToBackStack(null); // Agregar la transacción a la pila retroceder (opcional)
                                                transaction.commit();
                                            }
                                        });
                                        j++;

                                    }
                                }
                            });


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
            bundle.putString("Fragment_Provider","second");         //Poner firstt

            fragment.setArguments(bundle);

            // Obtener el FragmentManager del fragmento actual
            FragmentManager fragmentManager = getParentFragmentManager();

            // Reemplazar el contenido principal del contenedor del fragmento actual con el nuevo fragmento
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.frame_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            // Crear una instancia del fragmento
            MENUSecondFragmentProfiles fragment = new MENUSecondFragmentProfiles();

            // Pasar el ID del documento como argumento al fragmento
            Bundle bundle = new Bundle();
            bundle.putString("ID_DOCUMENTO", idDocumento);
            bundle.putString("Fragment_Provider","second");
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

    /** METODOS PARA JUEGOS MAS JUGADOS **/
    // Método para incrementar el contador de un valor en el HashMap
    private void incrementValueCount(HashMap<String, Integer> valueCountMap, String value) {
        if (valueCountMap.containsKey(value)) {
            int count = valueCountMap.get(value);
            valueCountMap.put(value, count + 1);
        } else {
            valueCountMap.put(value, 1);
        }
    }

    // Método para obtener las entradas del HashMap ordenadas por frecuencia
    private List<Map.Entry<String, Integer>> getSortedEntries(HashMap<String, Integer> valueCountMap) {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(valueCountMap.entrySet());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            entries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        }
        return entries;
    }

    // Método para obtener los N valores más frecuentes
    private List<String> getTopValues(List<Map.Entry<String, Integer>> sortedEntries, int N) {
        List<String> topValues = new ArrayList<>();
        for (int i = 0; i < N && i < sortedEntries.size(); i++) {
            topValues.add(sortedEntries.get(i).getKey());
        }
        return topValues;
    }


}