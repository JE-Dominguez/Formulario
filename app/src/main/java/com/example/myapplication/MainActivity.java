package com.example.myapplication;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.myapplication.Configuraciones.SQLiteConexion;
import com.example.myapplication.Configuraciones.Transacciones;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private EditText nombres, apellidos, edad, correo;
    private ImageView fotoView;

    private static final int PERMISO_CAMARA = 101;
    private File fotoFile;
    private String fotoBase64 = null;

    private ActivityResultLauncher<Intent> tomarFotoLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Inicializar views
        nombres = findViewById(R.id.nombres);
        apellidos = findViewById(R.id.apellidos);
        edad = findViewById(R.id.edad);
        correo = findViewById(R.id.correo);
        fotoView = findViewById(R.id.foto);

        Button btnGuardar = findViewById(R.id.guardar);
        Button btnFoto = findViewById(R.id.btnfoto);

        // Inicializar ActivityResultLauncher para cámara
        tomarFotoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (fotoFile != null && fotoFile.exists()) {
                            try {
                                // Cargar bitmap desde archivo
                                Bitmap bitmapFoto = BitmapFactory.decodeFile(fotoFile.getAbsolutePath());

                                // Leer orientación EXIF
                                ExifInterface exif = new ExifInterface(fotoFile.getAbsolutePath());
                                int orientation = exif.getAttributeInt(
                                        ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                                int rotation = exifToDegrees(orientation);

                                // Rotar bitmap si es necesario
                                if (rotation != 0) {
                                    Matrix matrix = new Matrix();
                                    matrix.preRotate(rotation);
                                    bitmapFoto = Bitmap.createBitmap(bitmapFoto, 0, 0,
                                            bitmapFoto.getWidth(), bitmapFoto.getHeight(), matrix, true);
                                }

                                // Mostrar en ImageView
                                fotoView.setImageBitmap(bitmapFoto);

                                // Convertir a Base64
                                fotoBase64 = bitmapToBase64(bitmapFoto);

                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(MainActivity.this, "Error al procesar la foto", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "No se pudo obtener la foto", Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );

        // Click en guardar
        btnGuardar.setOnClickListener(v -> AddPersona());

        // Click en tomar foto
        btnFoto.setOnClickListener(v -> checkPermisos());
    }

    // Convierte orientación EXIF a grados
    private int exifToDegrees(int exifOrientation) {
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_ROTATE_90: return 90;
            case ExifInterface.ORIENTATION_ROTATE_180: return 180;
            case ExifInterface.ORIENTATION_ROTATE_270: return 270;
            default: return 0;
        }
    }

    // Pedir permisos de cámara
    private void checkPermisos() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, PERMISO_CAMARA);
        } else {
            openCamara();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISO_CAMARA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamara();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Abrir cámara y crear archivo temporal
    private void openCamara() {
        try {
            fotoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "foto_" + System.currentTimeMillis() + ".jpg");
            Uri fotoUri = FileProvider.getUriForFile(this,
                    "com.example.myapplication.provider", fotoFile);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri);
            tomarFotoLauncher.launch(intent);

        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, "Error al abrir cámara: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Guardar persona en SQLite
    private void AddPersona() {
        // Validar campos
        String nombre = nombres.getText().toString().trim();
        String apellido = apellidos.getText().toString().trim();
        String correoStr = correo.getText().toString().trim();
        String edadStr = edad.getText().toString().trim();

        if (nombre.isEmpty() || apellido.isEmpty() || correoStr.isEmpty() || edadStr.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int edadInt;
        try {
            edadInt = Integer.parseInt(edadStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Edad inválida", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteConexion con = new SQLiteConexion(this, Transacciones.DBNAME, null, 1);
        SQLiteDatabase db = con.getWritableDatabase();

        try {
            ContentValues valores = new ContentValues();
            valores.put(Transacciones.nombres, nombre);
            valores.put(Transacciones.apellidos, apellido);
            valores.put(Transacciones.edad, edadInt);
            valores.put(Transacciones.correo, correoStr);
            valores.put(Transacciones.foto, fotoBase64 != null ? fotoBase64 : "");

            long resultado = db.insert(Transacciones.TablePersonas, null, valores);

            if (resultado > 0) {
                Toast.makeText(this, "Registro ingresado correctamente", Toast.LENGTH_SHORT).show();
                clearFields();
            } else {
                Toast.makeText(this, "Error, registro no ingresado", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            db.close();
            con.close();
        }
    }

    // Limpiar campos
    private void clearFields() {
        nombres.setText("");
        apellidos.setText("");
        edad.setText("");
        correo.setText("");
        fotoView.setImageBitmap(null);
        fotoBase64 = null;
    }

    private String bitmapToBase64(Bitmap bitmap) {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] byteArray = baos.toByteArray();
        return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT);
    }
}
