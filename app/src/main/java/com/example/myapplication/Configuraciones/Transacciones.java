package com.example.myapplication.Configuraciones;

public class Transacciones
{
    //nombre DB
    public static final String DBNAME = "PM01UCENM";

    //Nombre de la tabla personas
    public static  final String TablePersonas = "personas";

    //campos de la tabla personas
    public static  final String id = "id";
    public static  final String nombres = "nombres";
    public static  final String apellidos = "apellidos";
    public static  final String edad = "edad";
    public static  final String correo = "correo";
    public static  final String foto = "foto";

    //DDL

    public static final String CREATETABLEPERSONAS =
            "CREATE TABLE " + TablePersonas + " (" +
                    id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    nombres + " TEXT NOT NULL, " +
                    apellidos + " TEXT NOT NULL, " +
                    edad + " INTEGER NOT NULL, " +
                    correo + " UNIQUE, " +
                    foto + " TEXT)";


    public static final String DROPTABLEPERSONA ="DROP TABLE IF EXISTS "+ TablePersonas;
    public static final String SELECTTABLEPERSONAS = "SELECT * FROM " + TablePersonas;

}
