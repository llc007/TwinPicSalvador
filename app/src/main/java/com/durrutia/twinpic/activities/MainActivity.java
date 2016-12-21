package com.durrutia.twinpic.activities;

import android.annotation.TargetApi;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.durrutia.twinpic.R;
import com.durrutia.twinpic.domain.FotosPareadas;
import com.durrutia.twinpic.domain.Pic;
import com.durrutia.twinpic.domain.Twin;
import com.durrutia.twinpic.logic.Adaptador;
import com.durrutia.twinpic.services.WebService;
import com.durrutia.twinpic.util.DeviceUtils;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION_CODES.M;


/**
 * Actividad Principal, saca la foto y llama al metodo que guarda en la BD
 * @author  Luis Lopez
 * @version 20162112
 */
@Slf4j
public class MainActivity extends ListActivity  {
    /**
     * Variables de sistema
     */
    private static String APP_DIRECTORY = "MyPictureApp/";
    private static String MEDIA_DIRECTORY = APP_DIRECTORY + "PictureApp";
    private final int MY_PERMISSIONS = 100;
    private final int PHOTO_CODE = 200;
    private final int SELECT_PICTURE = 300;
    /**
     * Inicializacion de variables utiles
     */
    private ImageView mSetImage;
    private RelativeLayout mRlView;
    private String mPath;
    private String mUri;
    private String deviceID = "";
    Adaptador adaptador;
    private FotosPareadas ultimaFoto;
    FotosPareadas twin;

    /**
     * Inicializacion del boton que toma la foto y del listView principal
     * desde ButterKnife
     */
    @BindView(R.id.fab) FloatingActionButton mOptionButton;
    @BindView(R.id.lvPrincipal) ListView lvPrincipal;

    /**
     * Al Iniciar la App
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        //log.debug("Mensaje de debug probando lombok","OnCreate");
        //Detectando el id del Device
        deviceID = DeviceUtils.getDeviceId(this);

        // Destroy db
        /*
        super.getApplicationContext().deleteDatabase(Database.NAME + ".db");
        FlowManager.init(new FlowConfig.Builder(this).build());
        FlowManager.getDatabase(Database.class).getWritableDatabase();
        /*/


        //Actualizo el ListView cuando inicio la aplicacion
        actualizarListView(twin);

        //Si tengo permisos activo el boton
        if(mayRequestStoragePermission()) {
            mOptionButton.setEnabled(true);
        }else{
            mOptionButton.setEnabled(false);
        }

        /**
         * Al apretar el boton tomar foto
         */
        mOptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });
    }



    /**
     * Metodo que activa la camara, saca la foto y la envia a la actividad resultande.
     */
    private void openCamera() {
        /**
         * Variables internas
         */
        File file = new File(Environment.getExternalStorageDirectory(), MEDIA_DIRECTORY);
        boolean isDirectoryCreated = file.exists();

        if(!isDirectoryCreated) {
            isDirectoryCreated = file.mkdirs();
        }

        if(isDirectoryCreated){
            Long timestamp = System.currentTimeMillis() / 1000;
            String imageName = timestamp.toString() + ".jpg";
            //Path en donde se guardara la foto tomada
            mPath = Environment.getExternalStorageDirectory() + File.separator + MEDIA_DIRECTORY
                    + File.separator + imageName;

            File newFile = new File(mPath);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(newFile));
            startActivityForResult(intent, PHOTO_CODE);
        }
    }

    /**
     * Actividad que recibe la foto tomada, la envia al servidor y recibe una foto de vuelta.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case PHOTO_CODE:
                    MediaScannerConnection.scanFile(this,
                            new String[]{mPath}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    mUri = uri.toString();
                                }
                            });

                    /**
                     * Se codifica la imagen tomada para enviarla al servidor
                     */
                    Bitmap bm = BitmapFactory.decodeFile(mPath);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.JPEG, 90, baos);
                    byte[] b = baos.toByteArray();
                    String encodedImage = Base64.encodeToString(b,Base64.DEFAULT);

                    /**
                     * Creando la PIC local, tomada recientemente
                     */
                    Pic pic = new Pic();
                    pic.setDate("0");
                    pic.setUrl(mPath);
                    pic.setDeviceId(deviceID);
                    pic.setLatitude(0.0);
                    pic.setLongitude(0.0);
                    pic.setFoto(encodedImage);

                    /**
                     * Comunicacion con la API retrofit, por medio del metodo POST se envia una PIC,
                     * y recibe un TWIN.
                     */
                     WebService.Factory.getInstance().enviarPic(pic).enqueue(new Callback<Twin>() {
                        @Override
                        public void onResponse(Call<Twin> call, Response<Twin> response) {
                            //log.debug("APIRETURNresponse",String.valueOf(response.body()));
                            //Se crean las PIC's local y remota y se asignan a las que retorno el servidor.
                            final Pic local = response.body().getLocal();
                            local.save();
                            final Pic remota = response.body().getRemota();
                            remota.save();
                            //Se crea el TWIN con las dos PIC's
                            final Twin twin=Twin.builder()
                                    .local(local)
                                    .remota(remota)
                                    .build();
                                    twin.save();
                            //Se envian las fotos al adaptador para que sean agregadas a la APP
                            ultimaFoto = new FotosPareadas(twin.getLocal(),twin.getRemota());
                            actualizarListView(ultimaFoto);
                        }
                        @Override
                        public void onFailure(Call<Twin> call, Throwable t) {
                            Log.d("APIRETURN",String.valueOf(t));
                        }
                    });
                    break;

            }
        }
    }


    /**
     *  Metodo que actualiza el listview con las imagenes guardadas en la base de datos.
     *  Este mismo se encarga de llenar el listView con las fotos cuando se abre la aplicacion.
     */
    private void actualizarListView(FotosPareadas twin) {
        //final ListView listView = (ListView) findViewById(R.id.listView1);
        //creo un arreglo de FotosPareadas
        ArrayList<FotosPareadas> pictures = new ArrayList<FotosPareadas>();
        //Recorro la tabla twins y envio todos los PARES al adaptador.
        List<Twin> twins = SQLite.select().from(Twin.class).queryList();
        for (int i = twins.size()-1;i >= 0; i--) {
            if (i<10) {
                //Se omiten algunos pares
            }else{
                Twin twinAdapter = twins.get(i);
                final FotosPareadas arreglo = new FotosPareadas(twinAdapter.getLocal(), twinAdapter.getRemota());
                pictures.add(arreglo);
            }
        }
        Adaptador adaptador = new Adaptador(this, pictures);
        lvPrincipal.setAdapter(adaptador);

    }

    /**
     * PERMISOS!
     *Verifica los permisos para la camara y el internal Storage
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("file_path", mPath);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mPath = savedInstanceState.getString("file_path");
    }

    /**
     * Permisos aceptados!
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == MY_PERMISSIONS){
            if(grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(MainActivity.this, "Permisos aceptados", Toast.LENGTH_SHORT).show();
                mOptionButton.setEnabled(true);
            }
        }else{
            showExplanation();
        }
    }

    /**
     * Metodo que controla cuando los permisos fueron denegados
     */
    private void showExplanation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Permisos denegados");
        builder.setMessage("Para usar las funciones de la app necesitas aceptar los permisos");
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        builder.show();
    }

    /**
     * Metodo que pide permisos de Storage
     * @return
     */
    private boolean mayRequestStoragePermission() {
        if(Build.VERSION.SDK_INT < M)
            return true;

        if((checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                (checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED))
            return true;

        if((shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) || (shouldShowRequestPermissionRationale(CAMERA))){
            Snackbar.make(mRlView, "Los permisos son necesarios para poder usar la aplicación",
                    Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener() {
                @TargetApi(M)
                @Override
                public void onClick(View v) {
                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MY_PERMISSIONS);
                }
            });
        }else{
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MY_PERMISSIONS);
        }

        return false;
    }
}