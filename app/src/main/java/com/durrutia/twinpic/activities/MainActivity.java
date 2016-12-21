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
import retrofit2.Retrofit;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION_CODES.M;


/**
 * @Author Luis Lopez
 * Actividad Principal, saca la foto y llama al metodo que guarda en la BD
 */
@Slf4j
public class MainActivity extends ListActivity  {

    private static String APP_DIRECTORY = "MyPictureApp/";
    private static String MEDIA_DIRECTORY = APP_DIRECTORY + "PictureApp";
    private final int MY_PERMISSIONS = 100;
    private final int PHOTO_CODE = 200;
    private final int SELECT_PICTURE = 300;
    private ImageView mSetImage;
    private RelativeLayout mRlView;
    private String mPath;
    private String mUri;
    private String deviceID = "";
    Adaptador adaptador;
    private FotosPareadas ultimaFoto;
    FotosPareadas twin;
    //Botoncito
    @BindView(R.id.fab) FloatingActionButton mOptionButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        log.debug("Mensaje de debug probando lombok","OnCreate");
        deviceID = DeviceUtils.getDeviceId(this);

        //mOptionButton = (FloatingActionButton) findViewById(R.id.fab);
        // Destroy db
        /*super.getApplicationContext().deleteDatabase(Database.NAME + ".db");
        FlowManager.init(new FlowConfig.Builder(this).build());
        FlowManager.getDatabase(Database.class).getWritableDatabase();*/
        //log.debug("Borrada base de datos","OnCreate");

        //Actualizo el ListView cuando inicio la aplicacion
        actualizarListView(twin);

        //Si tengo permisos activo el botoncito
        if(mayRequestStoragePermission())
            mOptionButton.setEnabled(true);
        else
            mOptionButton.setEnabled(false);
        mOptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptions();
            }
        });
    } //FIn del onCreate

    //CLick en el boton, y muestro esto
    private void showOptions() {
        openCamera();
    }

    /*
       Metodo que actualiza el listview con las imagenes guardadas en la base de datos
    */
    private void actualizarListView(FotosPareadas twin) {
        final ListView listView = (ListView) findViewById(R.id.listView1);
        ArrayList<FotosPareadas> pictures = new ArrayList<FotosPareadas>();
        //Recorro la tabla twins y envio todos los PARES al adaptador.
        List<Twin> twins = SQLite.select().from(Twin.class).queryList();
        for (int i = twins.size()-1;i >= 0; i--) {
            if (i<10) {

               //log.debug("Contador i",String.valueOf(i));
            } else{
                Twin twinAdapter = twins.get(i);
            FotosPareadas arreglo = new FotosPareadas(twinAdapter.getLocal(), twinAdapter.getRemota());
            pictures.add(arreglo);
        }
        }
        Adaptador adaptador = new Adaptador(this, pictures);
        listView.setAdapter(adaptador);

    }

    /**
     * Metodo que activa la camara, saca la foto y la envia a la actividad resultande.
     */
    private void openCamera() {
        File file = new File(Environment.getExternalStorageDirectory(), MEDIA_DIRECTORY);
        boolean isDirectoryCreated = file.exists();

        if(!isDirectoryCreated)
            isDirectoryCreated = file.mkdirs();

        if(isDirectoryCreated){
            Long timestamp = System.currentTimeMillis() / 1000;
            String imageName = timestamp.toString() + ".jpg";

            mPath = Environment.getExternalStorageDirectory() + File.separator + MEDIA_DIRECTORY
                    + File.separator + imageName;

            File newFile = new File(mPath);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(newFile));
            startActivityForResult(intent, PHOTO_CODE);
        }
    }

    /**
     * Guarda la foto en el directorio
     * ActivityResult
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

                                    Log.i("ExternalStorage", "Scanned " + path + ":");
                                    Log.i("ExternalStorage", "-> Uri = " + uri);
                                    mUri = uri.toString();
                                }
                            });

                    // Codificando imagen para subirla
                    Bitmap bm = BitmapFactory.decodeFile(mPath);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.JPEG, 90, baos); //bm is the bitmap object
                    byte[] b = baos.toByteArray();
                    String encodedImage = Base64.encodeToString(b,Base64.DEFAULT);


                    Pic pic = new Pic();
                    pic.setDate("0");
                    pic.setUrl(mPath);
                    pic.setDeviceId(deviceID);
                    pic.setLatitude(0.0);
                    pic.setLongitude(0.0);
                    pic.setFoto(encodedImage);

                    Retrofit retrofit;
                    //Comunicacion con la API con retrofit - POST
                    WebService.Factory.getInstance().enviarPic(pic).enqueue(new Callback<Twin>() {
                        @Override
                        public void onResponse(Call<Twin> call, Response<Twin> response) {
                            //log.debug("APIRETURNresponse",String.valueOf(response.body()));
                            Pic local = response.body().getLocal();
                            local.save();
                            Pic remota = response.body().getRemota();
                            remota.save();

                            final Twin twin=Twin.builder()
                                    .local(local)
                                    .remota(remota)
                                    .build();
                                    twin.save();

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








    //CLick en la Imagen
    public void cambiarMensaje(View v){

        Log.d("CLICK EN FOTO","Se presiono un boton");


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

    //Mensajes para permisos denegados
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

    //Permisos para Storage
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