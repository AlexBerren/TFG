package com.loszorros.quienjuega.MenuFragments.SubirFoto;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.loszorros.quienjuega.MainActivity;
import com.loszorros.quienjuega.MenuFragments.MENUFifthFragment;
import com.loszorros.quienjuega.MenuFragments.MENUFirstFragment;
import com.loszorros.quienjuega.R;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;

public class UploadPhotoFragment extends Fragment {


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private EditText editTextTextDescripcion;
    private ImageView imageSubirFoto;
    private Button btnSubirFoto;
    private TextView titulo;
    private static String email;
    private ImageView atras;

    private FirebaseFirestore db;

    StorageReference storageReference;  //Referencia a nuestro Storage de Firebase
    String storage_path = "upload_images/*";   //ruta del Storage donde se guardaran las imagenes

    private static final int COD_SEL_IMAGE = 300;

    private Uri image_url;
    String photo = "photo";
    private FirebaseAuth mAuth; //Id del usuario logeado

    ProgressDialog progressDialog; //Cuadro de dialogo para indicar que se esta subiendo la foto

    private View view;

    public UploadPhotoFragment() {
        // Required empty public constructor
    }

    public static UploadPhotoFragment newInstance(String param1, String param2) {
        UploadPhotoFragment fragment = new UploadPhotoFragment();
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
        view = inflater.inflate(R.layout.fragment_upload_photo, container, false);
        SharedPreferences prefe = getActivity().getSharedPreferences(getString(R.string.pref_datos), Context.MODE_PRIVATE);
        email = prefe.getString("email" ,null);

        editTextTextDescripcion = view.findViewById(R.id.editTextTextDescripcion);
        imageSubirFoto = view.findViewById(R.id.imageSubirFoto);
        btnSubirFoto = view.findViewById(R.id.btnSubirFoto);
        titulo = view.findViewById(R.id.title_toolbar);
        atras = view.findViewById(R.id.chatTooolbarIcon);

        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();    //Instanciar el Storage de Firebase
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(getActivity());

        //seleccionar foto de perfil del usuario
        searchProfileImage();

        btnSubirFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(image_url == null || editTextTextDescripcion.getText().toString().isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Error al subir foto");
                    builder.setMessage("No has seleccionado ninguna foto o no has rellenado la descripción.");
                    builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Acción a realizar cuando se hace clic en el botón "Aceptar"
                            dialog.dismiss(); //Cerrar el cuadro
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else{
                    subirPhoto(image_url);  //la subimos
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

        DocumentReference usuarioNombre = db.collection("users").document(email);
        usuarioNombre.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String username = document.getString("username");
                    titulo.setText("@" + username);
                }
            }
        });



        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {
            if (requestCode == COD_SEL_IMAGE) {
                image_url = data.getData(); //Obtenemos la URL de la imagen seleccionada
                Picasso.with(imageSubirFoto.getContext())
                        .load(image_url)
                        .resize(450, 400) // ajusta el tamaño máximo de la imagen a 400 x 400
                        .centerCrop()
                        .into(imageSubirFoto);
            }
        }
    }

    private void subirPhoto(Uri image_url) {
        SharedPreferences prefe = getActivity().getSharedPreferences(getString(R.string.pref_datos), Context.MODE_PRIVATE);
        String email = prefe.getString("email", null);
        progressDialog.setMessage("Actualizando foto");
        progressDialog.show();
        String timestamp = String.valueOf(System.currentTimeMillis());
        String rute_storage_photo = storage_path + "" + photo + "" + mAuth.getUid() + "" + email + "" + timestamp;
        StorageReference reference = storageReference.child(rute_storage_photo);
        reference.putFile(image_url).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;
                if (uriTask.isSuccessful()) {
                    uriTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            CollectionReference usersRef = db.collection("users").document(email).collection("photos");

                            usersRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    int tamannoColeccion = task.getResult().size();
                                    if (task.isSuccessful()) {
                                        if(tamannoColeccion == 0) {
                                            String download_uri = uri.toString();
                                            HashMap<String, Object> map = new HashMap<>();
                                            map.put("photo", download_uri);
                                            map.put("descripcion", editTextTextDescripcion.getText().toString());
                                            // SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                            Date fechaSubida = new Date();
                                            map.put("fecha_subida", fechaSubida);
                                            db.collection("users").document(email).collection("photos").document("photo1").set(map)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            progressDialog.dismiss();
                                                            getPhoto(email);
                                                            Intent i = new Intent(getContext(), MainActivity.class);
                                                            startActivity(i);
                                                            getActivity().finish();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {

                                                        }
                                                    });
                                        } else {
                                            String download_uri = uri.toString();
                                            HashMap<String, Object> map = new HashMap<>();
                                            map.put("photo", download_uri);
                                            map.put("descripcion", editTextTextDescripcion.getText().toString());

                                            //SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                            Date fechaSubida = new Date();
                                            map.put("fecha_subida", fechaSubida);
                                            db.collection("users").document(email).collection("photos").document("photo" + (tamannoColeccion + 1)).set(map)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            progressDialog.dismiss();
                                                            getPhoto(email);
                                                            Intent i = new Intent(getContext(), MainActivity.class);
                                                            startActivity(i);
                                                            getActivity().finish();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {

                                                        }
                                                    });
                                        }
                                    } else {
                                        Log.d("Firestore", "Error getting documents: ", task.getException());
                                    }
                                }
                            });
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void getPhoto(String email) {      //Obtener la foto del usuario y mostrarla
        db.collection("users").document(email).collection("photos").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                if (!querySnapshot.isEmpty()) {
                    for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
                        String photoUser = documentSnapshot.getString("photo");
                        try {
                            if (photoUser != null && !photoUser.isEmpty()) {
                                Toast toast = Toast.makeText(getContext(), "Cargando foto", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.TOP, 0, 200);
                                toast.show();

                            }
                        } catch (Exception e) {
                            Log.v("Error", "e: " + e);
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    /**
     * al darle al boton de selecionar imagen te manda a la galeria a buscar
     * una imagen
     */
    private void searchProfileImage() {
        imageSubirFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, COD_SEL_IMAGE);
            }
        });
    }

}