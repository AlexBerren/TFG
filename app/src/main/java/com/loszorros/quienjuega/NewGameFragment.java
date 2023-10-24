package com.loszorros.quienjuega;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.Manifest;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.loszorros.quienjuega.MenuFragments.MENUFifthFragment;
import com.loszorros.quienjuega.MenuFragments.MENUThirdFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

public class NewGameFragment extends Fragment {


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private FirebaseFirestore db;


    private String mParam1;
    private String mParam2;
    ScrollView scrollView;

    private Button btnGenerar;
    private Spinner spSeleccionJuego;
    private EditText etCantidadJugadores;
    private CalendarView cvFechaPartida;
    private EditText horaPartida;
    Map<String, Object> datosDocumento = new HashMap<>();
    private Calendar selectedCalendar;
    Context context;
    private TextView titulo;
    private ImageView atras;


    //localizacion
    private static final int PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    double longitud;
    double latitud;
    private String email;


    public NewGameFragment() {

    }


    public static NewGameFragment newInstance(String param1, String param2) {
        NewGameFragment fragment = new NewGameFragment();
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
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Guardar el contexto para usarlo posteriormente
        this.context = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_game, container, false);
        btnGenerar = view.findViewById(R.id.btnGenerar);
        spSeleccionJuego = view.findViewById(R.id.SpnJuegos);
        etCantidadJugadores = view.findViewById(R.id.EdtCantidadJugadores);
        cvFechaPartida= view.findViewById(R.id.cvFecha);
        scrollView = view.findViewById(R.id.scrollView);
        titulo = view.findViewById(R.id.title_toolbar);
        atras = view.findViewById(R.id.chatTooolbarIcon);
        db = FirebaseFirestore.getInstance();

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
                MENUThirdFragment fragment = new MENUThirdFragment();

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

        // Verificar permisos de ubicación
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
        } else {
            // Permissions are already granted, proceed with location updates
            startLocationUpdates();
        }

        cvFechaPartida.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                selectedCalendar = Calendar.getInstance();
                selectedCalendar.set(year, month, dayOfMonth, 0, 0, 0);
                selectedCalendar.set(Calendar.MILLISECOND, 0);
            }
        });


        horaPartida = view.findViewById(R.id.EdtHora);
        db = FirebaseFirestore.getInstance();

        /** Colocamos en la lista despleagable los juegos guardados en Firebase **/
        CollectionReference gamesCollectionRef = db.collection("games");
        Query query = gamesCollectionRef.whereGreaterThanOrEqualTo(FieldPath.documentId(), "game").whereLessThan(FieldPath.documentId(), "gamel" + '\uf8ff');

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item);

                    for (QueryDocumentSnapshot gameDocument : task.getResult()) {
                        String name = gameDocument.getString("name");
                        if (name != null) {
                            adapter.add(name);
                        }
                    }

// Ordenar el adapter
                    adapter.sort(new Comparator<String>() {
                        @Override
                        public int compare(String s1, String s2) {
                            if (s1 == null && s2 == null) {
                                return 0; // Ambas cadenas son nulas, se consideran iguales
                            } else if (s1 == null) {
                                return -1; // s1 es nula, s2 se considera mayor
                            } else if (s2 == null) {
                                return 1; // s2 es nula, s1 se considera mayor
                            } else {
                                // Comparar las cadenas de nombres
                                return s1.compareToIgnoreCase(s2);
                            }
                        }
                    });

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spSeleccionJuego.setAdapter(adapter);
                }
            }
        });

        InputFilter filtroEmail = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                StringBuilder stringBuilder = new StringBuilder();

                for (int i = start; i < end; i++) {
                    char character = source.charAt(i);
                    if (Character.isDigit(character)) {
                        stringBuilder.append(character);
                    }
                }

                return stringBuilder.toString();
            }
        };

        etCantidadJugadores.setFilters(new InputFilter[] { filtroEmail });

        btnGenerar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spSeleccionJuego.equals("") || etCantidadJugadores.getText().toString().isEmpty() || horaPartida.getText().toString().isEmpty() || selectedCalendar == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Error al crear partida");
                    builder.setMessage("Hay algún campo vacio o no seleccionado.");
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
                    if (isValidTimeFormat(horaPartida.getText().toString())) {
                        saveGame(latitud, longitud);
                    }else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Error al crear partida");
                        builder.setMessage("El formato de la hora no es válido. (HH:mm)");
                        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Acción a realizar cuando se hace clic en el botón "Aceptar"
                                dialog.dismiss(); //Cerrar el cuadro
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
            }
        });

        return view;
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

    public void saveGame(double latitud, double longitud) {
        SharedPreferences prefs = getContext().getSharedPreferences(getString(R.string.pref_datos), Context.MODE_PRIVATE);
        String email = prefs.getString("email", null);

        CollectionReference docRef = db.collection("users").document(email).collection("games");

        // Verificar si la colección está vacía
        docRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                /** Obtener nombre de usuario para subirlo**/
                DocumentReference userDocRef = db.collection("users").document(email);

                userDocRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot userDoc = task.getResult();
                        if (userDoc.exists()) {
                            // La colección está vacía, crear el primer documento con nombre "game1"
                            DocumentReference nuevoDocumento = docRef.document("game1");
                            // Crear un objeto de datos para el nuevo documento
                            Map<String, Object> datosDocumento = new HashMap<>();
                            datosDocumento.put("Game", spSeleccionJuego.getSelectedItem().toString());
                            datosDocumento.put("NumberPlayers", etCantidadJugadores.getText().toString());
                            datosDocumento.put("Date", guardarFecha());
                            datosDocumento.put("Hour", horaPartida.getText().toString());
                            datosDocumento.put("Notas", "");
                            datosDocumento.put("Latitud", latitud + "");
                            datosDocumento.put("Longitud", longitud + "");
                            String username = userDoc.getString("username");
                            datosDocumento.put("participante1", username);

                            nuevoDocumento.set(datosDocumento);

                            loadFragment();




                        }
                    }
                });




            } else {

                /** Obtener nombre de usuario para subirlo**/
                DocumentReference userDocRef = db.collection("users").document(email);

                userDocRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot userDoc = task.getResult();
                        if (userDoc.exists()) {
                            // Obtener el número de campos en la colección
                            int numCampos = queryDocumentSnapshots.size();

                            // Usar el número de campos como nombre para el siguiente campo
                            String nuevoNombreCampo = "game" + (numCampos + 1);

                            // Crear un nuevo documento con el nombre generado
                            DocumentReference nuevoDocumento = docRef.document(nuevoNombreCampo);

                            // Crear un objeto de datos para el nuevo documento

                            Map<String, Object> nuevoCampo = new HashMap<>();

                            nuevoCampo.put("Game", spSeleccionJuego.getSelectedItem().toString());
                            nuevoCampo.put("NumberPlayers", etCantidadJugadores.getText().toString());
                            nuevoCampo.put("Date", guardarFecha());
                            nuevoCampo.put("Hour", horaPartida.getText().toString());
                            nuevoCampo.put("Notas", "");
                            nuevoCampo.put("Latitud", latitud + "");
                            nuevoCampo.put("Longitud", longitud + "");
                            String username = userDoc.getString("username");
                            nuevoCampo.put("participante1", username);


                            /** Comprobar que no hay una partida igual a esta ya creada **/


                            CollectionReference gamesCollectionRef = db.collection("users").document(email).collection("games");
                            gamesCollectionRef.get().addOnCompleteListener(task1 -> {
                                boolean partidaYaCreada = false;
                                if (task1.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task1.getResult()) {
                                        String game = document.getString("Game");
                                        String date = document.getString("Date");
                                        String hour = document.getString("Hour");
                                        String numeroJugadores = document.getString("NumberPlayers");

                                        String juegoCreado = spSeleccionJuego.getSelectedItem().toString() + " - " + guardarFecha()
                                                + " - " +  horaPartida.getText().toString() + " - " + etCantidadJugadores.getText().toString();
                                        String concatenatedValue = game + " - " + date + " - " + hour + " - " + numeroJugadores;


                                        if (juegoCreado.equals(concatenatedValue)) {
                                            partidaYaCreada = true;
                                            break;
                                        }
                                    }

                                    if (partidaYaCreada == false) {
                                        // Actualizar el documento con el nuevo campo
                                        docRef.document(nuevoNombreCampo).update(nuevoCampo);
                                        // Guardar el nuevo documento en la colección
                                        nuevoDocumento.set(nuevoCampo);
                                        loadFragment();
                                    } else {

                                    }
                                }
                            });


                        }
                    }
                });


            }
        });
    }


    public String guardarFecha(){
        // Obtener la fecha seleccionada en milisegundos
        long selectedDateInMillis = selectedCalendar.getTimeInMillis();

        // Crear un objeto Date a partir de los milisegundos
        Date selectedDate = new Date(selectedDateInMillis);

        // Formatear la fecha como una cadena (string)
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateString = sdf.format(selectedDate);
        return dateString;
    }

    public void loadFragment() {
        MENUThirdFragment fragment = new MENUThirdFragment();


        // Obtener el FragmentManager del fragmento actual
        FragmentManager fragmentManager = getParentFragmentManager();

        // Reemplazar el contenido principal del contenedor del fragmento actual con el nuevo fragmento
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

    }

    private boolean isValidTimeFormat(String timeText) {
        String pattern = "^([01]\\d|2[0-3]):[0-5]\\d$";
        return timeText.matches(pattern);
    }



}