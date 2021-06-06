package com.example.appmenu;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class GestorDias {
    private String fecha;
    private String numero;
    private String dia;

    public GestorDias(String fecha, String numero, String dia) {
        this.fecha = fecha;
        this.numero = numero;
        this.dia = dia;
    }
    public GestorDias() {}


    public List<GestorDias> array(){
        List<GestorDias> array = new ArrayList<>();

        //Se carga el array con el día de hoy y los 6 próximos
        for (int i = 0; i<7; i++){
            Calendar cal = Calendar.getInstance();
            int e = cal.get(Calendar.DAY_OF_MONTH) + i;
            cal.set(Calendar.DAY_OF_MONTH, e);
            String dia = cal.getTime().toString();

            String[] datos = dia.split(" ");
            String day = datos[0];
            day = translate(day);
            String month = datos[1];
            String num = datos[2];
            String year = datos[5];
            fecha = num + "-" + month + "-" + year;

            GestorDias elemento = new GestorDias(fecha, num, day);
            array.add(elemento);
        }
        return array;
    }

    public List<GestorDias> arrayOld(){
        List<GestorDias> array = new ArrayList<>();

        //Se carga el array con los 6 días anteriores a hoy
        for (int i = -1; i>-7; i--){
            Calendar cal = Calendar.getInstance();
            int e = cal.get(Calendar.DAY_OF_MONTH) + i;
            cal.set(Calendar.DAY_OF_MONTH, e);
            String dia = cal.getTime().toString();

            String[] datos = dia.split(" ");
            String day = datos[0];
            day = translate(day);
            String month = datos[1];
            String num = datos[2];
            String year = datos[5];
            fecha = num + "-" + month + "-" + year;

            GestorDias elemento = new GestorDias(fecha, num, day);
            array.add(elemento);
        }
        return array;
    }

    public String translate(String dia){
        switch (dia){
            case "Mon":
                dia = "Lun";
                break;
            case "Tue":
                dia = "Mar";
                break;
            case "Wed":
                dia = "Mié";
                break;
            case "Thu":
                dia = "Jue";
                break;
            case "Fri":
                dia = "Vie";
                break;
            case "Sat":
                dia = "Sáb";
                break;
            case "Sun":
                dia = "Dom";
                break;
            default:
        }
        return dia;
    }

    public String getFecha() {
        return fecha;
    }

    public String getNumero() {
        return numero;
    }

    public String getDia() {
        return dia;
    }
}
