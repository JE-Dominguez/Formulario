package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


public class Resultado extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_resultado);

        TextView lblInfo = findViewById(R.id.resp);
        double result = getIntent().getDoubleExtra("resultado", 0);
        lblInfo.setText("El resultado es: " + result);
    }
}