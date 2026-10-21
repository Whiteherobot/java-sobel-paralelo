package com.michicorp.sobel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Aplica Sobel de forma secuencial o repartiendo franjas de filas entre varios hilos. */
public final class DetectorBordes {

    private DetectorBordes() {
    }

    public static int[] secuencial(int[] gris, int ancho, int alto) {
        int[] salida = new int[gris.length];
        Sobel.filas(gris, ancho, alto, salida, 0, alto);
        return salida;
    }

    public static int[] paralelo(int[] gris, int ancho, int alto, int hilos)
            throws InterruptedException {
        if (hilos < 1) {
            throw new IllegalArgumentException("Hace falta al menos un hilo");
        }
        int franjas = Math.min(hilos, alto);
        int[] salida = new int[gris.length];
        ExecutorService pool = Executors.newFixedThreadPool(franjas);
        try {
            List<Callable<Void>> tareas = new ArrayList<>();
            for (int k = 0; k < franjas; k++) {
                // Reparto parejo: las primeras (alto % franjas) franjas llevan una fila más.
                int desde = (int) ((long) alto * k / franjas);
                int hasta = (int) ((long) alto * (k + 1) / franjas);
                tareas.add(() -> {
                    Sobel.filas(gris, ancho, alto, salida, desde, hasta);
                    return null;
                });
            }
            for (Future<Void> f : pool.invokeAll(tareas)) {
                f.get(); // propaga cualquier excepción de un hilo en vez de perderla
            }
        } catch (ExecutionException e) {
            throw new IllegalStateException("Falló una franja", e.getCause());
        } finally {
            pool.shutdown();
        }
        return salida;
    }
}
