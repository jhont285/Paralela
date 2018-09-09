package co.edu.unal.paralela;

import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que contiene los métodos para implementar la suma de los recíprocos de un arreglo usando paralelismo.
 */
public final class ReciprocalArraySum {

    /**
     * Constructor.
     */
    private ReciprocalArraySum() {
    }

    /**
     * Calcula secuencialmente la suma de valores recíprocos para un arreglo.
     *
     * @param input Arreglo de entrada
     * @return La suma de los recíprocos del arreglo de entrada
     */
    protected static double seqArraySum(final double[] input) {
        double sum = 0;

        // Calcula la suma de los recíprocos de los elementos del arreglo
        for (int i = 0; i < input.length; i++) {
            sum += 1 / input[i];
        }

        return sum;
    }

    /**
     * calcula el tamaño de cada trozo o sección, de acuerdo con el número de secciones para crear
     * a través de un número dado de elementos.
     *
     * @param nChunks El número de secciones (chunks) para crear
     * @param nElements El número de elementos para dividir
     * @return El tamaño por defecto de la sección (chunk)
     */
    private static int getChunkSize(final int nChunks, final int nElements) {
        // Función techo entera
        return (nElements + nChunks - 1) / nChunks;
    }

    /**
     * Calcula el índice del elemento inclusivo donde la sección/trozo (chunk) inicia,
     * dado que hay cierto número de secciones/trozos (chunks).
     *
     * @param chunk la sección/trozo (chunk) para cacular la posición de inicio
     * @param nChunks Cantidad de seciiones/trozos (chunks) creados
     * @param nElements La cantidad de elementos de la sección/trozo que debe atravesarse
     * @return El indice inclusivo donde esta sección/trozo (chunk) inicia en el conjunto de
     *         nElements
     */
    private static int getChunkStartInclusive(final int chunk,
            final int nChunks, final int nElements) {
        final int chunkSize = getChunkSize(nChunks, nElements);
        return chunk * chunkSize;
    }

    /**
     * Calcula el índice del elemento exclusivo que es proporcionado al final de la sección/trozo (chunk),
     * dado que hay cierto número de secciones/trozos (chunks).
     *
     * @param chunk LA sección para calcular donde termina
     * @param nChunks Cantidad de seciiones/trozos (chunks) creados
     * @param nElements La cantidad de elementos de la sección/trozo que debe atravesarse
     * @return El índice de terminación exclusivo para esta sección/trozo (chunk)
     */
    private static int getChunkEndExclusive(final int chunk, final int nChunks,
            final int nElements) {
        final int chunkSize = getChunkSize(nChunks, nElements);
        final int end = (chunk + 1) * chunkSize;
        if (end > nElements) {
            return nElements;
        } else {
            return end;
        }
    }

    /**
     * Este pedazo de clase puede ser completada para para implementar el cuerpo de cada tarea creada
     * para realizar la suma de los recíprocos del arreglo en paralelo.
     */
    private static class ReciprocalArraySumTask extends RecursiveAction {
        /**
         * Iniciar el índice para el recorrido transversal hecho por esta tarea.
         */
        private final int begin;
        /**
         * Concluir el índice para el recorrido transversal hecho por esta tarea.
         */
        private final int end;
        /**
         * Arreglo de entrada para la suma de recíprocos.
         */
        private final double[] input;
        /*
         * Valor la cantidad de tareas
         */
        private final int tasks;

        /**
         * Valor que guarda el numero de tareas
         */
        private double value;

        /**
         * Constructor.
         * @param begin establece el indice inicial para comenzar
         *        el recorrido trasversal.
         * @param end establece el indice final para el recorrido trasversal.
         * @param input Valores de entrada
         * @param tasks Cantidad de tarea
         */
        ReciprocalArraySumTask(final int begin, final int end, final double[] input, final int tasks) {
            this.begin = begin;
            this.end = end;
            this.input = input;
            this.tasks = tasks;
            this.value = 0;
        }

        /**
         * Adquiere el valor producido por esta tarea.
         * @return El valor producido por esta tarea
         */
        public double getValue() {
            return value;
        }

        @Override
        protected void compute() {
            if (begin < end) {
                final int middle = (begin + end) / 2;
                ReciprocalArraySumTask subTask1 = new ReciprocalArraySumTask(begin, middle, input, tasks);
                ReciprocalArraySumTask subTask2 = new ReciprocalArraySumTask(middle + 1, end, input, tasks);
                invokeAll(subTask1, subTask2);
                this.value = subTask1.getValue() + subTask2.getValue();
            }
            else {
              // Para hacer
              int interval = input.length / this.tasks;
              int left = end * interval;
              int right = end + 1 == tasks ? input.length : interval * (end + 1);
              for (int i = left; i < right; i++)
                this.value += 1 / input[i];
            }
        }
    }

    /**
     * Para hacer: Modificar este método para calcular la misma suma de recíprocos como le realizada en
     * seqArraySum, pero utilizando dos tareas ejecutándose en paralelo dentro del framework ForkJoin de Java
     * Se puede asumir que el largo del arreglo de entrada
     * es igualmente divisible por 2.
     *
     * @param input Arreglo de entrada
     * @return La suma de los recíprocos del arreglo de entrada
     */
    protected static double parArraySum(final double[] input) {
        assert input.length % 2 == 0;
        return parManyTaskArraySum(input, 2);
    }

    /**
     * Para hacer: extender el trabajo hecho para implementar parArraySum que permita utilizar un número establecido
     * de tareas para calcular la suma del arreglo recíproco.
     * getChunkStartInclusive y getChunkEndExclusive pueden ser útiles para cacular
     * el rango de elementos indice que pertenecen a cada sección/trozo (chunk).
     *
     * @param input Arreglo de entrada
     * @param numTasks El número de tareas para crear
     * @return La suma de los recíprocos del arreglo de entrada
     */
    protected static double parManyTaskArraySum(final double[] input, final int numTasks) {
        ReciprocalArraySumTask principal = new ReciprocalArraySumTask(0,  numTasks - 1, input, numTasks);
        ForkJoinPool.commonPool().invoke(principal);
        return principal.getValue();
    }
}
