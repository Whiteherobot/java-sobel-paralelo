package com.michicorp.sobel;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
import javax.imageio.ImageIO;

/**
 * Uso: java -jar java-sobel-paralelo.jar [imagen] [salida.png] [hilos]
 * Sin argumentos usa la imagen de ejemplo y todos los procesadores lógicos.
 */
public final class Main {

    private static final int CALENTAMIENTO = 3;
    private static final int REPETICIONES = 10;

    private Main() {
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        BufferedImage entrada = args.length > 0 ? leer(new File(args[0])) : ejemplo();
        File salida = new File(args.length > 1 ? args[1] : "bordes.png");
        int hilos = args.length > 2 ? Integer.parseInt(args[2])
                : Runtime.getRuntime().availableProcessors();

        int ancho = entrada.getWidth();
        int alto = entrada.getHeight();
        int[] gris = Sobel.aGris(entrada);
        System.out.printf("Imagen: %dx%d, hilos: %d%n", ancho, alto, hilos);

        double msSec = medir(() -> DetectorBordes.secuencial(gris, ancho, alto));
        double msPar = medir(() -> DetectorBordes.paralelo(gris, ancho, alto, hilos));

        int[] a = DetectorBordes.secuencial(gris, ancho, alto);
        int[] b = DetectorBordes.paralelo(gris, ancho, alto, hilos);
        ImageIO.write(Sobel.aImagen(b, ancho, alto), "png", salida);

        System.out.printf(Locale.US, "Secuencial: %8.2f ms (promedio de %d)%n", msSec, REPETICIONES);
        System.out.printf(Locale.US, "Paralelo:   %8.2f ms%n", msPar);
        System.out.printf(Locale.US, "Aceleración: %.2fx%n", msSec / msPar);
        System.out.println("Mismo resultado: " + (Arrays.equals(a, b) ? "sí" : "NO"));
        System.out.println("Guardado en " + salida.getAbsolutePath());
    }

    interface Tarea {
        int[] correr() throws InterruptedException;
    }

    /** Promedio en ms, después de unas corridas de calentamiento para el JIT. */
    static double medir(Tarea t) throws InterruptedException {
        for (int i = 0; i < CALENTAMIENTO; i++) {
            t.correr();
        }
        long ini = System.nanoTime();
        for (int i = 0; i < REPETICIONES; i++) {
            t.correr();
        }
        return (System.nanoTime() - ini) / 1e6 / REPETICIONES;
    }

    private static BufferedImage leer(File archivo) throws IOException {
        BufferedImage img = ImageIO.read(archivo);
        if (img == null) {
            throw new IOException("No es una imagen legible: " + archivo);
        }
        return img;
    }

    private static BufferedImage ejemplo() throws IOException {
        try (InputStream in = Main.class.getResourceAsStream("/bikes.jpg")) {
            if (in == null) {
                throw new IOException("Falta la imagen de ejemplo bikes.jpg");
            }
            return ImageIO.read(in);
        }
    }
}
