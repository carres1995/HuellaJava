package com.huellas;

import com.huellas.view.MainView;

/**
 * Clase lanzadora para evitar errores de componentes de JavaFX ausentes.
 * Esta clase debe ser el punto de entrada al ejecutar el JAR o desde el IDE.
 */
public class Main {
    public static void main(String[] args) {
        MainView.main(args);
    }
}
