package com.loszorros.quienjuega.MenuFragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.loszorros.quienjuega.MenuFragments.Chat.ChatFragment;
import com.loszorros.quienjuega.R;
import com.loszorros.quienjuega.adapter.ChatAdapter;
import com.loszorros.quienjuega.models.Chat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MENUFourthFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private FirebaseFirestore db;
    private static String email;
    private static View view;
    private static String otherUserEmail;
    private static String otherUsername;
    private static String matchingChatID;

    private List<String> nombresDeUsuariosAutocompletar;
    ArrayAdapter<String> sugerenciasAdapter;

    private AutoCompleteTextView txtUsername;


    public MENUFourthFragment() {
    }

    public static MENUFourthFragment newInstance(String param1, String param2) {
        MENUFourthFragment fragment = new MENUFourthFragment();
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
        view =  inflater.inflate(R.layout.fragment_fourth, container, false);

        //conexion base de dato + obtener email
        db = FirebaseFirestore.getInstance();
        SharedPreferences prefe = getActivity().getSharedPreferences(getString(R.string.pref_datos), Context.MODE_PRIVATE);
        email = prefe.getString("email" ,null);
        txtUsername = view.findViewById(R.id.newChatText);
        nombresDeUsuariosAutocompletar = new ArrayList<>();
        sugerenciasAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, nombresDeUsuariosAutocompletar);



        if (email != null){
            initViews();
            // Crear una instancia de OnBackPressedCallback y sobrescribir su método handleOnBackPressed()
            OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
                @Override
                public void handleOnBackPressed() {
                    getParentFragmentManager().popBackStack();
                }
            };
            requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
        }

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

        return view;
    }

    private void initViews(){
        view.findViewById(R.id.newChatButton).setOnClickListener(viewC -> checkMatchingChatIDs());

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        ChatAdapter chatAdapter = new ChatAdapter(this::chatSelected);
        RecyclerView listChatsRecyclerView = view.findViewById(R.id.listChatsRecyclerView);
        listChatsRecyclerView.setLayoutManager(layoutManager);
        listChatsRecyclerView.setAdapter(chatAdapter);

        DocumentReference userRef = db.collection("users").document(email);

        userRef.collection("chats")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Chat> listChats = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Chat chat = document.toObject(Chat.class);
                        listChats.add(chat);
                    }
                    chatAdapter.setData(listChats);
                });

        userRef.collection("chats")
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error == null && queryDocumentSnapshots != null) {
                        List<Chat> listChats = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Chat chat = document.toObject(Chat.class);
                            listChats.add(chat);
                        }
                        chatAdapter.setData(listChats);
                    }
                });
    }

    private void chatSelected(Chat chat){

        String otherEmails="";
        if (email.equals(chat.getUsers().get(0))) {
            otherEmails = chat.getUsers().get(1);
        }else {
            otherEmails = chat.getUsers().get(0);
        }
        ChatFragment chatFragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString("otherEmail", otherEmails);

        db.collection("users").document(otherEmails).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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
                FragmentTransaction transaction = fragmentManager.beginTransaction();

                transaction.setCustomAnimations(
                        R.anim.slide_in_right,  // animación de entrada hacia la derecha
                        R.anim.slide_out_left,  // animación de salida hacia la izquierda
                        R.anim.slide_in_left,   // animación de entrada hacia la izquierda al retroceder
                        R.anim.slide_out_right  // animación de salida hacia la derecha al retroceder
                );

                transaction.replace(R.id.frame_container, chatFragment);
                transaction.addToBackStack(null);
                transaction.commit();

            }
        });
    }

    private void newChat(){


        String chatId = UUID.randomUUID().toString();

        List<String> users = Arrays.asList(email, otherUserEmail);


        Chat chat = new Chat(chatId, "Chat con " + otherUserEmail, users);

        db.collection("chats").document(chatId).set(chat);
        db.collection("users").document(email).collection("chats").document(chatId).set(chat);
        db.collection("users").document(otherUserEmail).collection("chats").document(chatId).set(chat);


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
                FragmentTransaction transaction = fragmentManager.beginTransaction();

                transaction.setCustomAnimations(
                        R.anim.slide_in_right,  // animación de entrada hacia la derecha
                        R.anim.slide_out_left,  // animación de salida hacia la izquierda
                        R.anim.slide_in_left,   // animación de entrada hacia la izquierda al retroceder
                        R.anim.slide_out_right  // animación de salida hacia la derecha al retroceder
                );

                transaction.replace(R.id.frame_container, chatFragment);
                transaction.addToBackStack(null);
                transaction.commit();

            }
        });

    }

    public interface OnChatIDsCompleteListener {
        void onChatIDsComplete(ArrayList<String> chatIDs);
        void onChatIDsError(Exception e);
    }

    public interface OnUsernameExistsListener {
        void onUsernameExists(String email);
        void onUsernameNotExists();
    }


    private void getChatIDs(OnChatIDsCompleteListener listener) {
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

    private void getOtherChatIDs(OnChatIDsCompleteListener listener) {
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

    /**Metodo el cual llama a on usernameExists si es asi es decir existe ese
     * recoje la lista de chats de la persona autenticada luego recoje la lista de chats
     * de la persona a la que estamos buscando si algun id de las dos listas coincide entonces es que anteriormente habian hablado
     * y les manda directamente a su conversacion por el contrario si ningun id coincide quiere decir que nunca habían hablado y les crea el chat*/
    private void checkMatchingChatIDs() {

        TextView newChatText = view.findViewById(R.id.newChatText);
        otherUsername = newChatText.getText().toString();

        db.collection("users").document(email).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Object nombreUsuarioLogeado = documentSnapshot.get("username");

                    if(otherUsername.equals(nombreUsuarioLogeado)){

                    } else{
                        checkUsernameExists(otherUsername, new OnUsernameExistsListener() {
                            @Override
                            public void onUsernameExists(String email) {
                                // El username existe, se ha obtenido el email
                                otherUserEmail=email;
                                getChatIDs(new OnChatIDsCompleteListener() {
                                    @Override
                                    public void onChatIDsComplete(ArrayList<String> chatIDs) {
                                        getOtherChatIDs(new OnChatIDsCompleteListener() {
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
                                                    args.putString("Fragment_Provider" , "fourth");

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

                                if (otherUsername.isEmpty()) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setTitle("Error al buscar usuario");
                                    builder.setMessage("No has buscado ningun nombre de usuario.");
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
                                    // El username no existe
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setTitle("Error al buscar usuario");
                                    builder.setMessage("El usuario '" + otherUsername + "' no existe.");
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
                        });
                    }

                } else {

                }
            }
        });



    }

    /**Metodo para verificar si existe el username introducido y recuperar su email*/
    private void checkUsernameExists(String username, OnUsernameExistsListener listener) {
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
}