package com.michicorp.sobel;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SobelTest {

    @Test
    void imagenUniformeNoTieneBordes() {
        int[] gris = new int[10 * 8];
        java.util.Arrays.fill(gris, 120);
        int[] out = DetectorBordes.secuencial(gris, 10, 8);
        for (int v : out) {
            assertEquals(0, v);
        }
    }

    @Test
    void escalonVerticalDaElValorEsperado() {
        // Mitad izquierda 0, derecha 100: en la columna del salto gx = 4 * 100 y gy = 0.
        int ancho = 6;
        int alto = 5;
        int[] gris = new int[ancho * alto];
        for (int y = 0; y < alto; y++) {
            for (int x = 3; x < ancho; x++) {
                gris[y * ancho + x] = 100;
            }
        }
        int[] out = DetectorBordes.secuencial(gris, ancho, alto);
        assertEquals(255, out[2 * ancho + 2]); // 400 recortado a 255
        assertEquals(255, out[2 * ancho + 3]);
        assertEquals(0, out[2 * ancho + 1]);
        assertEquals(0, out[2 * ancho]); // borde exterior
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 7, 16, 64})
    void paraleloDaLoMismoQueSecuencial(int hilos) throws InterruptedException {
        int ancho = 37;
        int alto = 23; // no divisible por la mayoría de los hilos
        int[] gris = new Random(7).ints(ancho * alto, 0, 256).toArray();
        assertArrayEquals(DetectorBordes.secuencial(gris, ancho, alto),
                DetectorBordes.paralelo(gris, ancho, alto, hilos));
    }

    @Test
    void masHilosQueFilasNoRompe() throws InterruptedException {
        int[] gris = new Random(1).ints(5 * 3, 0, 256).toArray();
        assertArrayEquals(DetectorBordes.secuencial(gris, 5, 3),
                DetectorBordes.paralelo(gris, 5, 3, 50));
    }

    @Test
    void ceroHilosEsUnError() {
        assertThrows(IllegalArgumentException.class,
                () -> DetectorBordes.paralelo(new int[9], 3, 3, 0));
    }
}
