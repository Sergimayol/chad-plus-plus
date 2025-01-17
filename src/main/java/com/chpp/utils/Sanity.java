package com.chpp.utils;

import com.chpp.errors.ErrorHandler;

/**
 * Clase encargada de checkear que un elemento cumpla con las restricciones
 * impuestas
 *
 * Assignatura 21742 - Compiladors
 * Estudis: Grau en Informàtica
 * Itinerari: Computació
 * Curs: 2022 - 2023
 */
public class Sanity {

    /**
     * Comprueba los datos de entrada del programa. Los datos de entrada deben ser:
     * Ejemplo del input completo seria: chadpp test.chpp target/output
     * Esto deberia compilar test.chpp a target/ a un fichero llamado output
     *
     * - El nombre del fichero a compilar (Obligatorio)
     * - El path (y nombre) donde queremos que se genere el fichero de salida
     * (Opcional)
     *
     * @param args
     * @return int - Error o Ok
     */
    public static int checkInput(String[] args) {

        if (args.length == 0) {
            System.out.println(Env.welcomeString);
            System.exit(0);
        }

        switch (args.length) {
            case 1:
                Env.FILE_DATA.setMultipleFileData(args[0], null);
                break;
            case 2:
                Env.FILE_DATA.setMultipleFileData(args[0], args[1]);
                break;
        }

        if (Env.DEBUG_MODE) {
            System.out.println(Env.FILE_DATA);
        }

        if (ErrorHandler.hasErrors()) {
            return Env.Error;
        }

        return Env.FILE_DATA.checkFileData() ? Env.Ok : Env.Error;
    }

    public static String sanitizePath(String s) {
        return s.replace("\\", Env.SLASH).replace("/", Env.SLASH);
    }

}
