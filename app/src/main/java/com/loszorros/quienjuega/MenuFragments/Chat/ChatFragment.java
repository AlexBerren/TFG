package com.loszorros.quienjuega.MenuFragments.Chat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.loszorros.quienjuega.RegistroLoginActivitys.Complementario.CircleTransform;
import com.loszorros.quienjuega.MenuFragments.MENUFourthFragment;
import com.loszorros.quienjuega.MenuFragments.MENUSecondFragmentProfiles;
import com.loszorros.quienjuega.R;
import com.loszorros.quienjuega.adapter.MessageAdapter;
import com.loszorros.quienjuega.models.Message;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ChatFragment extends Fragment {


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;


    private FirebaseFirestore db;
    private static String email;
    private static String chatId;
    private static String otherUsername;
    private static String otherUserEmail;
    private TextView toolbarUsername;
    private ImageView btnAtras;
    private ImageView otherProfileImage;
    private ImageButton btnInvitar;
    private String fragmentProvider;


    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
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
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        Bundle args = getArguments();

        //conexion base de dato + obtener email
        db = FirebaseFirestore.getInstance();
        SharedPreferences prefe = getActivity().getSharedPreferences(getString(R.string.pref_datos), Context.MODE_PRIVATE);
        email = prefe.getString("email" ,null);
        chatId = args.getString("chatId");
        otherUsername = args.getString("otherUser");
        otherUserEmail = args.getString("otherEmail");
        fragmentProvider = args.getString("Fragment_Provider");
        toolbarUsername = view.findViewById(R.id.title_toolbar);
        btnAtras = view.findViewById(R.id.chatTooolbarIcon);
        otherProfileImage = view.findViewById(R.id.otherProfileImage);
        btnInvitar = view.findViewById(R.id.btInvitar);

        if (chatId != null && email != null){
            getPhoto(otherUserEmail,otherProfileImage);
            initViews(view);
            toolbarUsername.setText("@" + otherUsername);
            btnAtras.setOnClickListener(views -> volverAtras());
            toolbarUsername.setOnClickListener(views -> loadFragment(otherUserEmail));
        }

        btnInvitar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                invitarPartida();
            }
        });




   return view;
    }

    private void initViews(View view) {
        RecyclerView messagesRecylerView = view.findViewById(R.id.messagesRecylerView);
        Button sendMessageButton = view.findViewById(R.id.sendMessageButton);
        messagesRecylerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        MessageAdapter adapter = new MessageAdapter(email, chatId);
        messagesRecylerView.setAdapter(adapter);

        sendMessageButton.setOnClickListener(views -> sendMessage(view));

        DocumentReference chatRef = db.collection("chats").document(chatId);

        chatRef.collection("messages").orderBy("dob", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Message> listMessages = queryDocumentSnapshots.toObjects(Message.class);
                    List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                    adapter.setData(listMessages,documentSnapshots);

                    // Desplazarse al final del RecyclerView
                    messagesRecylerView.scrollToPosition(adapter.getItemCount() - 1);
                });


        chatRef.collection("messages").orderBy("dob", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error == null && queryDocumentSnapshots != null) {
                        List<Message> listMessages = queryDocumentSnapshots.toObjects(Message.class);
                        List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                        adapter.setData(listMessages,documentSnapshots);

                        // Desplazarse al final del RecyclerView
                        messagesRecylerView.scrollToPosition(adapter.getItemCount() - 1);
                    }
                });

        EditText textField = view.findViewById(R.id.messageTextField);
        textField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        messagesRecylerView.scrollToPosition(adapter.getItemCount() - 1);
                    }
                }, 200); // 200 milisegundos de retraso para permitir que el teclado se abra
            }
        });

    }

    private void sendMessage(View view) {
        TextView messageTextField = view.findViewById(R.id.messageTextField);
        String messageText = messageTextField.getText().toString();

        if (!TextUtils.isEmpty(messageText)) {
            Message message = new Message(messageText, email, false);

            db.collection("chats").document(chatId).collection("messages").document().set(message);

            messageTextField.setText("");
        }
    }

    public void volverAtras() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.popBackStack();
    }

    private void getPhoto(String id, ImageView imageView) {
        db.collection("users").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String photoUser = documentSnapshot.getString("photo");
                try {
                    if (!photoUser.equals("")) {
                        Picasso.with(imageView.getContext())
                                .load(photoUser)
                                .resize(400, 400) // ajusta el tamaño máximo de la imagen a 400 x 400
                                .transform(new CircleTransform())
                                .into(imageView);
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

    public void loadFragment(String idDocumento) {
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

    private void invitarPartida() {

        CollectionReference gamesRef = db.collection("users").document(email).collection("games");

        gamesRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null) {
                        List<String> opciones = new ArrayList<>();

                        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
                        formatoFecha.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));

                        Date fechaActual = new Date();
                        String fechaActualFormateada = formatoFecha.format(fechaActual);

                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            String game = document.getString("Game");
                            String hour = document.getString("Hour");
                            String date = document.getString("Date");
                            String jugadores = document.getString("NumberPlayers");





                            try {
                                if (!jugadores.equals("0") && compararFechas(fechaActualFormateada, date) >= 0) {
                                    String gameInfo = game + " - " + date + " - " + hour + " - ";
                                    opciones.add(gameInfo);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Seleccionar una partida");

                        // Convierte el ArrayList en un array para pasarlo al cuadro de diálogo
                        CharSequence[] opcionesArray = opciones.toArray(new CharSequence[opciones.size()]);

                        builder.setItems(opcionesArray, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Acción a realizar cuando se hace clic en una opción de la lista
                                String opcionSeleccionada = opciones.get(which);

                                // Realizar otras acciones según la opción seleccionada

                                /**esto sirve para comprobar que no haya un mensaje con el mensaje opcionselecionada + aceptada o rechazada
                                 * y que asi solo se pueda enviar una peticion por partida*/
                                Query query = db.collection("chats").document(chatId).collection("messages")
                                        .whereGreaterThanOrEqualTo("message", opcionSeleccionada)
                                        .whereLessThan("message", opcionSeleccionada + "\uf8ff");
                                query.get().addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        QuerySnapshot querySnapshot = task1.getResult();
                                        if (querySnapshot != null && !querySnapshot.isEmpty()) {

                                        } else {

                                            Message message = new Message(opcionSeleccionada, email, true);
                                            db.collection("chats").document(chatId).collection("messages").document().set(message);



                                        }
                                    } else {
                                        // Handle the case if the query task is not successful

                                    }
                                });


                                dialog.dismiss(); // Cerrar el cuadro
                            }
                        });

                        // Agregar el botón "Cerrar" para cerrar el cuadro de diálogo
                        builder.setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Acción a realizar cuando se hace clic en el botón "Cerrar"
                                dialog.dismiss(); // Cerrar el cuadro
                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
            }
        });
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

}