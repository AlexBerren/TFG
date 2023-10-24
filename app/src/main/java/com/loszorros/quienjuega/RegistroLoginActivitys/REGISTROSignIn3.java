package com.loszorros.quienjuega.RegistroLoginActivitys;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.loszorros.quienjuega.RegistroLoginActivitys.Complementario.CircleTransform;
import com.loszorros.quienjuega.R;
import com.squareup.picasso.Picasso;

import java.util.Comparator;
import java.util.HashMap;


public class REGISTROSignIn3 extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private FirebaseFirestore db;
    ImageView imagenPerfil;
    EditText descripcion;
    Spinner spinner1;
    Spinner spinner2;
    Spinner spinner3;
    Spinner spinner4;

    StorageReference storageReference;  //Referencia a nuestro Storage de Firebase
    String storage_path = "profile_images/*";   //ruta del Storage donde se guardaran las imagenes

    private static final int COD_SEL_IMAGE = 300;

    private Uri image_url;
    String photo = "photo";
    private FirebaseAuth mAuth; //Id del usuario logeado

    ProgressDialog progressDialog; //Cuadro de dialogo para indicar que se esta subiendo la foto

    Button botonDisfruta;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in3);

        db = FirebaseFirestore.getInstance();
        botonDisfruta = findViewById(R.id.botonDisfruta);
        descripcion = findViewById(R.id.TxtDescripcion);
        spinner1 = findViewById(R.id.gamesList1);
        spinner2 = findViewById(R.id.gamesList2);
        spinner3 = findViewById(R.id.gamesList3);
        spinner4 = findViewById(R.id.gamesList4);
        imagenPerfil = findViewById(R.id.imagen);
        //getSupportActionBar().hide();

        botonDisfruta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogIn(v);
            }
        });

        InputFilter filtro = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                StringBuilder stringBuilder = new StringBuilder();

                for (int i = start; i < end; i++) {
                    char character = source.charAt(i);
                    if (Character.isLetterOrDigit(character) || Character.isSpaceChar(character) || character == ',' || character == '.' || character == '-' || character == '\n') {
                        stringBuilder.append(character);
                    }
                }

                return stringBuilder.toString();
            }
        };

        descripcion.setFilters(new InputFilter[] { filtro });



        //rellenar el spinner (lista de juegos)
        addGamesToSpinner();

        storageReference = FirebaseStorage.getInstance().getReference();    //Instanciar el Storage de Firebase
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        //se le pone foto por defecto a todos los usuarios
        photoDefault();
        //seleccionar foto de perfil del usuario
        searchProfileImage();




    }

    //Te lleva a la pagina del login
    public void LogIn(View v) {
        SharedPreferences prefe = getSharedPreferences(getString(R.string.pref_datos), Context.MODE_PRIVATE);
        String email = prefe.getString("email", null);
        HashMap<String, String> hasMap;
        Intent intent = getIntent();
        hasMap = (HashMap<String, String>) intent.getSerializableExtra("hash");

        String description = descripcion.getText().toString().trim();
        String game1 = spinner1.getSelectedItem().toString();
        String game2 = spinner2.getSelectedItem().toString();
        String game3 = spinner3.getSelectedItem().toString();
        String game4 = spinner4.getSelectedItem().toString();

        if (game1.equals(game2) || game1.equals(game3) || game1.equals(game4)
                || game2.equals(game3) || game2.equals(game4)
                || game3.equals(game4) || description.isEmpty()) {

            showAlert();

        } else {
            hasMap.put("description", description);
            hasMap.put("game1", game1);
            hasMap.put("game2", game2);
            hasMap.put("game3", game3);
            hasMap.put("game4", game4);

            db.collection("users").document(email).set(hasMap);

            Intent i = new Intent(this, INICIOLoginActivity.class);
            startActivity(i);
            finish();
        }
    }

    public void Sign2(View v){
        Intent i = new Intent(this, REGISTROSignIn2.class);
        startActivity(i);
        finish();
    }

    /**
    *Metodo para el "ImagePicker", se llama cuando una actividad que fue iniciada para obtener un resultado,
    *finaliza su ejecucion
    */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Comprobamos que el resultado devuelto sea correcto y que no la imagen no sea vacia (null). Si esto se cumple, se actualiza el ImageView con la imagen seleccionada por el usuario
        if (resultCode == RESULT_OK) {
            if (requestCode == COD_SEL_IMAGE) {
                image_url = data.getData(); //Obtenemos nombre de la imagen
                subirPhoto(image_url);  //la subimos
            }
        }
    }

    private void subirPhoto(Uri image_url) {
        SharedPreferences prefe = getSharedPreferences(getString(R.string.pref_datos), Context.MODE_PRIVATE);
        String email = prefe.getString("email",null);
        progressDialog.setMessage("Actualizando foto");
        progressDialog.show();
        String rute_storage_photo = storage_path + "" + photo + "" + mAuth.getUid() + "" + email; //Este es el nombre con el que se va a guardar la imagen en el Storage
        StorageReference reference = storageReference.child(rute_storage_photo); //Para enviar imagen al Storage
        reference.putFile(image_url).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {       //GUARDAR LA IMAGEN EN EL CAMPO "photo" DEL USUARIO EN LA DATABASE
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl(); //Obtener URL que se le va a asignar a la imagen
                while(!uriTask.isSuccessful()); //Preguntar si la uri es exitosa
                if(uriTask.isSuccessful()) {
                    uriTask.addOnSuccessListener(new OnSuccessListener<Uri>() {

                        /**
                         *
                         * Pasamos el Uri a string y lo metemos en un campo
                         * "photo" del usuario para guardarlo en la base de datos
                         */

                        @Override
                        public void onSuccess(Uri uri) {

                                System.out.println("EL CAMPO NO ES NULO");
                                String download_uri = uri.toString();
                                HashMap<String, String> map;
                                Intent intent = getIntent();
                                map = (HashMap<String, String>) intent.getSerializableExtra("hash");
                                map.put("photo",download_uri);

                                db.collection("users").document(email).set(map);

                                progressDialog.dismiss();
                                getPhoto(email);   //Se muestra la foto


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

    private void getPhoto(String id){      //Obtener la foto del usuario y mostrarla
        db.collection("users").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String photoUser = documentSnapshot.getString("photo");
                try {
                    if(!photoUser.equals("")){
                        Toast toast = Toast.makeText(getApplicationContext(), "Cargando foto", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP,0,200);
                        toast.show();
                        Picasso.with(REGISTROSignIn3.this)
                                .load(photoUser)
                                .resize(400, 400) // ajusta el tamaño máximo de la imagen a 300 x 300
                                .transform(new CircleTransform())
                                .into(imagenPerfil);
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


    /**
     * al darle al boton de selecionar imagen te manda a la galeria a buscar
     * una imagen
     */
    private void searchProfileImage (){
        imagenPerfil = findViewById(R.id.imagen);
        imagenPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, COD_SEL_IMAGE);
            }
        });
    }

    //agregar lista TopPlayedGames al spinner
    private void addGamesToSpinner (){

        /** Colocamos en la lista despleagable los juegos guardados en Firebase **/
        CollectionReference gamesCollectionRef = db.collection("games");
        Query query = gamesCollectionRef.whereGreaterThanOrEqualTo(FieldPath.documentId(), "game").whereLessThan(FieldPath.documentId(), "gamel" + '\uf8ff');

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item);

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
                    spinner1.setAdapter(adapter);
                    spinner2.setAdapter(adapter);
                    spinner3.setAdapter(adapter);
                    spinner4.setAdapter(adapter);
                }
            }
        });




    }

    private void photoDefault(){
        SharedPreferences prefe = getSharedPreferences(getString(R.string.pref_datos), Context.MODE_PRIVATE);
        String email = prefe.getString("email",null);
        System.out.println("EL CAMPO NO ES NULO");
        HashMap<String, String> map;
        Intent intent = getIntent();
        map = (HashMap<String, String>) intent.getSerializableExtra("hash");
        map.put("photo","https://firebasestorage.googleapis.com/v0/b/quienjuega-loszorros.appspot.com/o/profile_images%2FfotoNPC.png?alt=media&token=791aab55-e252-470b-bc3b-fbdcbb917d7a");

        db.collection("users").document(email).set(map);

        progressDialog.dismiss();
        getPhoto(email);   //Se muestra la foto
    }

    private void showAlert(){
        //Crear un cuadro de error
        AlertDialog.Builder builder = new AlertDialog.Builder(REGISTROSignIn3.this);
        builder.setTitle("Error en el Registro");
        builder.setMessage("Has seleccionado algún juego igual o la descripción esta vacía");
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