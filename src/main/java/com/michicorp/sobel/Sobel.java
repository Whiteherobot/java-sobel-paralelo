package com.michicorp.sobel;

import java.awt.image.BufferedImage;

/**
 * Operador de Sobel sobre una imagen en escala de grises guardada como arreglo plano.
 *
 * La imagen se convierte a gris una sola vez ({@link #aGris}); antes cada píxel se convertía
 * nueve veces, una por cada vecino de la máscara.
 */
public final class Sobel {

    private Sobel() {
    }

    /** Luminancia (Rec. 601) de cada píxel, fila por fila. */
    public static int[] aGris(BufferedImage imagen) {
        int ancho = imagen.getWidth();
        int alto = imagen.getHeight();
        int[] rgb = imagen.getRGB(0, 0, ancho, alto, null, 0, ancho);
        int[] gris = new int[rgb.length];
        for (int i = 0; i < rgb.length; i++) {
            int r = (rgb[i] >> 16) & 0xFF;
            int g = (rgb[i] >> 8) & 0xFF;
            int b = rgb[i] & 0xFF;
            gris[i] = (int) (0.299 * r + 0.587 * g + 0.114 * b);
        }
        return gris;
    }

    /**
     * Calcula la magnitud del gradiente en las filas [desde, hasta) y la escribe en {@code salida}.
     * El borde exterior queda en 0 porque la máscara de 3x3 no cabe ahí. Cada hilo recibe un rango
     * de filas distinto y solo escribe en ese rango, así que no hace falta sincronizar.
     */
    public static void filas(int[] gris, int ancho, int alto, int[] salida, int desde, int hasta) {
        for (int y = Math.max(desde, 1); y < Math.min(hasta, alto - 1); y++) {
            int fila = y * ancho;
            for (int x = 1; x < ancho - 1; x++) {
                int i = fila + x;
                int arriba = i - ancho;
                int abajo = i + ancho;
                int gx = -gris[arriba - 1] + gris[arriba + 1]
                        - 2 * gris[i - 1] + 2 * gris[i + 1]
                        - gris[abajo - 1] + gris[abajo + 1];
                int gy = -gris[arriba - 1] - 2 * gris[arriba] - gris[arriba + 1]
                        + gris[abajo - 1] + 2 * gris[abajo] + gris[abajo + 1];
                salida[i] = Math.min(255, (int) Math.sqrt(gx * gx + gy * gy));
            }
        }
    }

    /** Arma una imagen en grises a partir de las magnitudes. */
    public static BufferedImage aImagen(int[] magnitud, int ancho, int alto) {
        BufferedImage img = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        int[] rgb = new int[magnitud.length];
        for (int i = 0; i < magnitud.length; i++) {
            int m = magnitud[i];
            rgb[i] = (m << 16) | (m << 8) | m;
        }
        img.setRGB(0, 0, ancho, alto, rgb, 0, ancho);
        return img;
    }
}
