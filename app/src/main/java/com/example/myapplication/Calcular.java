package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;

public class Calcular {

    public static double sumar(double a, double b) {
        return a + b;
    }
    public static double restar(double a, double b) {
        return a - b;
    }
    public static double multiplicar(double a, double b) {
        return a * b;
    }
    public static double dividir(double a, double b) throws ArithmeticException {
        if (b == 0) {
            throw new ArithmeticException("No se puede dividir entre cero");
        }
        return a / b;
    }


    public static void ejecutarOperacion(String tipo, EditText TxtN1, EditText TxtN2, Context mainActivity) {
        if (validarCampos(TxtN1, TxtN2)) return;

        double n1 = Double.parseDouble(TxtN1.getText().toString());
        double n2 = Double.parseDouble(TxtN2.getText().toString());
        double resultado;

        try {
            switch (tipo) {
                case "suma":
                    resultado = sumar(n1, n2);
                    break;
                case "resta":
                    resultado = restar(n1, n2);
                    break;
                case "multiplicacion":
                    resultado = multiplicar(n1, n2);
                    break;
                case "division":
                    resultado = dividir(n1, n2);
                    break;
                default:
                    Toast.makeText(mainActivity, "Operación no válida", Toast.LENGTH_SHORT).show();
                    return;
            }

            Intent intent = new Intent(mainActivity, Resultado.class);
            intent.putExtra("resultado", resultado);
            mainActivity.startActivity(intent);

        } catch (ArithmeticException e) {
            Toast.makeText(mainActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean validarCampos(EditText n1, EditText n2) {
        if (n1.getText().toString().trim().isEmpty()) {
            n1.setError("Campo obligatorio");
            n1.requestFocus();
            return true;
        }
        if (n2.getText().toString().trim().isEmpty()) {
            n2.setError("Campo obligatorio");
            n2.requestFocus();
            return true;
        }
        return false;
    }
}
