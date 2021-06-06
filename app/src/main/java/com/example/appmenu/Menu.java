package com.example.appmenu;

public class Menu implements Comparable<Menu> {

    private final String fecha;
    private final String num;
    private final String dia;
    private final String comida;
    private final String cena;

    public Menu(String fecha, String num, String dia, String comida, String cena) {
        this.fecha = fecha;
        this.num = num;
        this.dia = dia;
        this.comida = comida;
        this.cena = cena;
    }

    @Override
    public int compareTo(Menu o) {
        int num = converter(fecha);
        int num2 = converter(o.fecha);

        //Se comparan las dos fechas obtenidas para ordenarlas

        if (num < num2) {
            if (num + 7 < num2){
                return 1;
            }
            return -1;
        }
        if (num > num2) {
            if (num > num2+7){
                return -1;
            }
            return 1;
        }
        return 0;
    }

    public int converter(String fecha){
        String[] array = fecha.split("-");
        return Integer.parseInt(array[0]);
    }

    public String getFecha() {
        return fecha;
    }

    public String getNum() {
        return num;
    }

    public String getDia() {
        return dia;
    }

    public String getComida() {
        return comida;
    }

    public String getCena() {
        return cena;
    }

    @Override
    public String toString() {
        return "Menu{" +
                "fecha='" + fecha + '\'' +
                ", num='" + num + '\'' +
                ", dia='" + dia + '\'' +
                ", comida='" + comida + '\'' +
                ", cena='" + cena + '\'' +
                '}';
    }
}
