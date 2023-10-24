package com.loszorros.quienjuega.MenuFragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.loszorros.quienjuega.MenuFragments.Chat.ChatFragment;
import com.loszorros.quienjuega.RegistroLoginActivitys.Complementario.RecyclerViewDisableScrollTouchListener;
import com.loszorros.quienjuega.NewGameFragment;
import com.loszorros.quienjuega.R;
import com.loszorros.quienjuega.RegistroLoginActivitys.REGISTROSignIn3;
import com.loszorros.quienjuega.adapter.GameAdapter;
import com.loszorros.quienjuega.models.Chat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

public class MENUThirdFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private Button btnCrearPartida;
    private Button btnSiguientePartida;
    private Button btnBuscar;

    private FirebaseFirestore db;
    private static String email;
    private RecyclerView games;
    private List<QueryDocumentSnapshot> gamesList = new ArrayList<>();
    private RecyclerView recyclerJuegos;
    private static String otherUserEmail;
    private static String otherUsername;
    private static String matchingChatID;
    private static final double EARTH_RADIUS = 6371;
    private EditText kilometros;

    GameAdapter adapterGames;
    static int i = 1;


    //localizacion
    private static final int PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    double longitud;
    double latitud;


    public MENUThirdFragment() {
    }

    public static MENUThirdFragment newInstance(String param1, String param2) {
        MENUThirdFragment fragment = new MENUThirdFragment();
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

        SharedPreferences prefe = getActivity().getSharedPreferences(getString(R.string.pref_datos), Context.MODE_PRIVATE);
        email = prefe.getString("email" ,null);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_third, container, false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerJuegos = view.findViewById(R.id.RecyclerJuegos);
        recyclerJuegos.setLayoutManager(layoutManager);
        btnCrearPartida = view.findViewById(R.id.BtnCrearPartida);
        btnSiguientePartida = view.findViewById(R.id.btnSiguientePartida);
        btnBuscar = view.findViewById(R.id.btnBuscarKilometros);
        kilometros = view.findViewById(R.id.edtIntroducirKm);
        kilometros.setText("");


        view.findViewById(R.id.btnEscribir).setOnClickListener(viewC -> checkMatchingChatIDs());

        // Verificar permisos de ubicación
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
        } else {
            // Permissions are already granted, proceed with location updates
            startLocationUpdates();
        }



        /** Obtenemos todos los subdocumentos que empiecen por "game", menos los del usuario logeado **/
        CollectionReference usersCollectionRef = db.collection("users");
        usersCollectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task1) {
                if (task1.isSuccessful()) {
                    gamesList.clear();
                    for (QueryDocumentSnapshot userDocument : task1.getResult()) {
                        if (!userDocument.getId().equals(email)) {
                            // Obtener la referencia de la subcolección "games" para cada documento
                            CollectionReference gamesCollectionRef = userDocument.getReference().collection("games");

                            // Consultar los subdocumentos que comienzan con "game"
                            Query query = gamesCollectionRef.whereGreaterThan(FieldPath.documentId(), "game").whereLessThan(FieldPath.documentId(), "game" + '\uf8ff');

                            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {

                                        for (QueryDocumentSnapshot gameDocument : task.getResult()) {
                                            String numberPlayersString = gameDocument.getString("NumberPlayers");
                                            int numberPlayers = Integer.parseInt(numberPlayersString);

                                            String fechaPartida = gameDocument.getString("Date");

                                            SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
                                            formatoFecha.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));

                                            Date fechaActual = new Date();
                                            String fechaActualFormateada = formatoFecha.format(fechaActual);
                                            try{
                                                if (numberPlayers != 0 && compararFechas(fechaActualFormateada, fechaPartida) >= 0) {
                                                    gamesList.add(gameDocument);
                                                }
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        adapterGames = new GameAdapter(gamesList, getContext(), latitud, longitud);
                                        recyclerJuegos.setAdapter(adapterGames);
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });


        /** BOTON BUSCAR **/
        btnBuscar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (kilometros.getText().toString().trim().isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Error al buscar partidas");
                    builder.setMessage("No has introducido una distancia para buscar partidas");
                    builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Acción a realizar cuando se hace clic en el botón "Aceptar"
                            dialog.dismiss(); //Cerrar el cuadro
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    gamesList.clear();


                    /** Obtenemos todos los subdocumentos que empiecen por "game", menos los del usuario logeado **/
                    CollectionReference usersCollectionRef = db.collection("users");
                    usersCollectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task1) {
                            if (task1.isSuccessful()) {
                                gamesList.clear();
                                for (QueryDocumentSnapshot userDocument : task1.getResult()) {
                                    if (!userDocument.getId().equals(email)) {
                                        // Obtener la referencia de la subcolección "games" para cada documento
                                        CollectionReference gamesCollectionRef = userDocument.getReference().collection("games");

                                        // Consultar los subdocumentos que comienzan con "game"
                                        Query query = gamesCollectionRef.whereGreaterThan(FieldPath.documentId(), "game").whereLessThan(FieldPath.documentId(), "game" + '\uf8ff');

                                        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot gameDocument : task.getResult()) {

                                                        Double longitudPartida = Double.parseDouble(gameDocument.getString("Longitud"));
                                                        Double latitudPartida = Double.parseDouble(gameDocument.getString("Latitud"));



                                                        double dLat = Math.toRadians(latitudPartida - latitud);
                                                        double dLon = Math.toRadians(longitudPartida - longitud);

                                                        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                                                                Math.cos(Math.toRadians(latitud)) * Math.cos(Math.toRadians(latitudPartida)) *
                                                                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
                                                        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

                                                        double distance = EARTH_RADIUS * c;


                                                        String numberPlayersString = gameDocument.getString("NumberPlayers");
                                                        int numberPlayers = Integer.parseInt(numberPlayersString);

                                                        String fechaPartida = gameDocument.getString("Date");

                                                        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
                                                        formatoFecha.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));

                                                        Date fechaActual = new Date();
                                                        String fechaActualFormateada = formatoFecha.format(fechaActual);
                                                        try {
                                                            if (numberPlayers != 0 && compararFechas(fechaActualFormateada, fechaPartida) >= 0 && distance <= Double.parseDouble(kilometros.getText().toString())) {
                                                                gamesList.add(gameDocument);
                                                            }
                                                        } catch (ParseException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                    adapterGames = new GameAdapter(gamesList, getContext(), latitud, longitud);
                                                    recyclerJuegos.setAdapter(adapterGames);
                                                    i = 1;
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    });

                }
            }
        });


        /** Para que no se pueda deslizar **/
        RecyclerViewDisableScrollTouchListener recycler = new RecyclerViewDisableScrollTouchListener();
        recyclerJuegos.addOnItemTouchListener(recycler);
        i = 1;

        btnCrearPartida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment();
            }
        });

        btnSiguientePartida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (i < adapterGames.getItemCount()){
                    recyclerJuegos.scrollToPosition(i);

                    i++;
                } else {

                }

            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        kilometros.setText("");
    }

    public void loadFragment() {

        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = new NewGameFragment();


        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();



    }

    private void startLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000); // Intervalo de actualización de ubicación en milisegundos

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        latitud = location.getLatitude();
                        longitud = location.getLongitude();

                        // Guardar posición en Firebase
                        //guardarPosicionFirebase(latitud, longitud);

                        // Mostrar mensaje o realizar otras acciones con la posición

                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Detener actualizaciones de ubicación cuando no se necesitan
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    // Manejar resultado de solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                // Permiso denegado. Mostrar mensaje de error u otras acciones.

            }
        }
    }

    public static int compararFechas(String fecha1, String fecha2) throws ParseException {
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
        formatoFecha.setLenient(false);


        Date date1 = formatoFecha.parse(fecha1);
        Date date2 = formatoFecha.parse(fecha2);

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        if (cal1.equals(cal2)) {
            return 0;
        } else if (cal1.after(cal2)) {
            return -1;
        } else {
            return 1;
        }

    }

    /**DESDE AQUI PARA QUE CUANDO LE DES A ESCRIBIR FUNCIONE*/
    private void checkMatchingChatIDs() {

        if (gamesList.isEmpty()){
            System.out.println("LISTA DE JUEGOS VACIA");
        }else{
        QueryDocumentSnapshot gameDocument = gamesList.get(i-1);
        DocumentReference userDocRef = gameDocument.getReference().getParent().getParent();
        otherUsername = "";
        userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    otherUsername = documentSnapshot.getString("username");
                    // Aquí puedes utilizar el valor de "username"
                    // ...
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Ocurrió un error al obtener el documento
                Log.e("TAG", "onFailure: ", e);
            }
        });


        db.collection("users").document(gameDocument.getReference().getParent().getParent().getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {

                        checkUsernameExists(otherUsername, new MENUFourthFragment.OnUsernameExistsListener() {
                            @Override
                            public void onUsernameExists(String email) {
                                // El username existe, se ha obtenido el email
                                otherUserEmail=email;
                                getChatIDs(new MENUFourthFragment.OnChatIDsCompleteListener() {
                                    @Override
                                    public void onChatIDsComplete(ArrayList<String> chatIDs) {
                                        getOtherChatIDs(new MENUFourthFragment.OnChatIDsCompleteListener() {
                                            @Override
                                            public void onChatIDsComplete(ArrayList<String> otherChatIDs) {
                                                // Verificar si hay algún chat ID coincidente
                                                boolean hasMatchingChatID = false;

                                                for (String chatID : chatIDs) {
                                                    if (otherChatIDs.contains(chatID)) {
                                                        hasMatchingChatID = true;
                                                        matchingChatID = chatID;
                                                        break;
                                                    }
                                                }

                                                if (hasMatchingChatID) {
                                                    // Hay chat IDs coincidentes
                                                    // Realiza las acciones necesarias
                                                    ChatFragment chatFragment = new ChatFragment();
                                                    Bundle args = new Bundle();
                                                    args.putString("otherEmail", otherUserEmail);
                                                    args.putString("chatId", matchingChatID);
                                                    args.putString("user", email);
                                                    args.putString("otherUser", otherUsername);
                                                    args.putString("matchingChatID", matchingChatID);
                                                    args.putString("Fragment_Provider" , "third");

                                                    chatFragment.setArguments(args);

                                                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                                    fragmentManager.beginTransaction()
                                                            .replace(R.id.frame_container, chatFragment)
                                                            .addToBackStack(null)
                                                            .commit();
                                                } else {
                                                    // No hay chat IDs coincidentes
                                                    // Realiza las acciones necesarias
                                                    newChat();
                                                }
                                            }

                                            @Override
                                            public void onChatIDsError(Exception e) {
                                                // Maneja el error de obtención de chat IDs del otro usuario
                                                Log.d("TAG", "Error getting other user's chat documents: ", e);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onChatIDsError(Exception e) {
                                        // Maneja el error de obtención de chat IDs del usuario actual
                                        Log.d("TAG", "Error getting user's chat documents: ", e);
                                    }
                                });

                            }

                            @Override
                            public void onUsernameNotExists() {
                                // El username no existe

                            }
                        });
                    }

                }
        });}
    }

    /**Metodo para verificar si existe el username introducido y recuperar su email*/
    private void checkUsernameExists(String username, MENUFourthFragment.OnUsernameExistsListener listener) {
        CollectionReference usersCollectionRef = FirebaseFirestore.getInstance().collection("users");

        usersCollectionRef
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Existe al menos un documento con el username ingresado
                            // Obtener el email correspondiente
                            DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                            String email = documentSnapshot.getId();
                            listener.onUsernameExists(email);
                        } else {
                            // No se encontraron documentos con el username ingresado
                            listener.onUsernameNotExists();
                        }
                    } else {
                        Log.d("TAG", "Error al verificar la existencia del username: ", task.getException());
                        // Manejar el error de consulta
                    }
                });
    }


    private void getChatIDs(MENUFourthFragment.OnChatIDsCompleteListener listener) {
        CollectionReference chatsCollectionRef = db.collection("users").document(email).collection("chats");

        chatsCollectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<String> chatIDs = new ArrayList<>();

                    for (QueryDocumentSnapshot chatDocument : task.getResult()) {
                        String chatId = chatDocument.getId();
                        chatIDs.add(chatId);
                    }

                    listener.onChatIDsComplete(chatIDs);
                } else {
                    listener.onChatIDsError(task.getException());
                }
            }
        });
    }

    private void getOtherChatIDs(MENUFourthFragment.OnChatIDsCompleteListener listener) {
        CollectionReference chatsCollectionRef = db.collection("users").document(otherUserEmail).collection("chats");

        chatsCollectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<String> chatIDs = new ArrayList<>();

                    for (QueryDocumentSnapshot chatDocument : task.getResult()) {
                        String chatId = chatDocument.getId();
                        chatIDs.add(chatId);
                    }

                    listener.onChatIDsComplete(chatIDs);
                } else {
                    listener.onChatIDsError(task.getException());
                }
            }
        });
    }

    private void newChat(){


        String chatId = UUID.randomUUID().toString();

        List<String> users = Arrays.asList(email, otherUserEmail);
        //checkMatchingChatIDs();

        Chat chat = new Chat(chatId, "Chat con " + otherUserEmail, users);

        db.collection("chats").document(chatId).set(chat);
        db.collection("users").document(email).collection("chats").document(chatId).set(chat);
        db.collection("users").document(otherUserEmail).collection("chats").document(chatId).set(chat);

        //aqui quiero que espere para que se pase bien el other username

        ChatFragment chatFragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString("otherEmail", otherUserEmail);

        db.collection("users").document(otherUserEmail).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String otherUser = documentSnapshot.getString("username");

                // Reemplaza el fragmento actual con el fragmento de chat

                args.putString("chatId", chat.getId());
                args.putString("user", email);
                args.putString("otherUser", otherUser);
                args.putString("Fragment_Provider" , "fourth");

                chatFragment.setArguments(args);

                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_container, chatFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }
    /**HASTA AUI EL BOTON DE ESCRIBIR*/

}