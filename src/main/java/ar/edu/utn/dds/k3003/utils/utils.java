package ar.edu.utn.dds.k3003.utils;

import java.util.Random;

public class utils {

    private static final String LETRAS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String NUMEROS = "0123456789";

    public static String generarModeloAleatorio() {
        int longitud = 10;
        Random rand = new Random();
        StringBuilder sb = new StringBuilder(longitud);

        for (int i = 0; i < longitud; i++) {
            int index = rand.nextInt(LETRAS.length());
            sb.append(LETRAS.charAt(index));
        }
        return sb.toString();
    }

    public static String generarDireccionAleatoria() {
        int longitud = 10;
        Random rand = new Random();
        StringBuilder sb = new StringBuilder(longitud);

        for (int i = 0; i < longitud; i++) {
            int index = rand.nextInt(NUMEROS.length());
            sb.append(NUMEROS.charAt(index));
        }
        return sb.toString();
    }

    public static Integer randomID(){
       int randomNumber = 0 ;
       Random random = new Random();
        for (int i = 0; i < 5; i++) {
            int digit = random.nextInt(10); // Genero un numero aleatorio entre 0 y 9
            randomNumber = randomNumber * 10 + digit;
        }
        return randomNumber;
    }

    public static Integer randomNumberBetween(Integer MIN, Integer MAX){
        Random rand = new Random();
        return rand.nextInt((MAX - MIN) + 1) + MIN;
    }
}
