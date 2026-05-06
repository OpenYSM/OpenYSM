package rip.ysm.algorithms;/* Luis Bodart A01635000 */

/* Mersenne Twister de 64 bits (MT19937-64) */

/**
 * w - Rango de valores generados (bits), 2^w
 * n - Grado de recurrencia (tamaño de la reserva de bytes)
 * m - Desplazamiento utilizado en la relación de recurrencia que define la serie x, 1 ≤ m < n
 * r - Número de bits de la lower bit-mask (twist value/valor de giro), 0 ≤ r ≤ w - 1
 * a - Coeficientes de la matriz de giro de forma normal racional
 * u - Componente 1 de la matriz de bit-scrambling (templado)
 * d - Componente 2 de la matriz de bit-scrambling (templado)
 * s - Componente 3 de la matriz de bit-scrambling (templado)
 * b - Componente 4 de la matriz de bit-scrambling (templado)
 * t - Componente 5 de la matriz de bit-scrambling (templado)
 * c - Componente 6 de la matriz de bit-scrambling (templado)
 * l - Componente 7 de la matriz de bit-scrambling (templado)
 * f - Multiplicador de inicialización
 * lower_mask - 31 bits menos significativos
 * upper_mask - 33 bits más significativos
 * MT - arreglo de estado interno
 * index - Índice actual en la reserva de bytes
 */

/**
 * Referencias:
 *
 * Mersenne Twister
 * https://en.wikipedia.org/wiki/Mersenne_Twister
 *
 * The Mersenne Twister
 * http://www.quadibloc.com/crypto/co4814.htm
 *
 * Mersenne Twister Engine
 * https://en.cppreference.com/w/cpp/numeric/random/mersenne_twister_engine
 *
 * mt19937-64
 * https://cplusplus.com/reference/random/mt19937_64/
 *
 * Mersenne Twister: A 623-Dimensionally Equidistributed Uniform Pseudo-Random Number Generator
 * http://www.math.sci.hiroshima-u.ac.jp/~m-mat/MT/ARTICLES/mt.pdf
 */

public class MT19937 {

    private static final int w = 64, n = 312, m = 156, r = 31;
    private static final long a = 0xB5026F5AA96619E9l;
    private static final int u = 29, s = 17, t = 37;
    private static final long d = 0x5555555555555555l, b = 0x71d67fffeda60000l, c = 0xfff7eee000000000l;
    private static final int l = 43;
    private static final long f = 6364136223846793005l;
    private static final long lower_mask = 0x7FFFFFFFl, upper_mask = 0xFFFFFFFF80000000l;
    public static final int default_seed = 5489;

    private final long[] MT;
    private int index;

    /**
     * Constructor con una semilla aleatoria
     */
    public MT19937() {
        this.MT = new long[n];
        this.setSeed(System.currentTimeMillis());
    }

    /**
     * Cnstructor con una semilla definida
     *
     * @param seed semilla inicial
     */
    public MT19937(long seed) {
        this.MT = new long[n];
        this.setSeed(seed);
    }

    /**
     * Inicializa el generador a partir de una semilla
     *
     * @param seed semilla para la creación de los números aleatorios
     */
    private void setSeed(long seed) {
        this.MT[0] = seed;
        this.index = n;
        for (int i = 1; i < n; i++) {
            this.MT[i] = (f * (this.MT[i - 1] ^ (this.MT[i - 1] >>> (w - 2))) + i);
        }
    }

    /**
     * Genera los siguientes valores n de la serie x_i
     */
    private void twist() {
        for (int i = 0; i < n; i++) {
            long x = (this.MT[i] & upper_mask) | (this.MT[(i + 1) % n] & lower_mask);
            long xA = x >>> 1;
            if ((x & 1L) != 0L) {
                xA ^= a;
            }
            this.MT[i] = this.MT[(i + m) % n] ^ xA;
        }
        this.index = 0;
    }

    /**
     * Genera un valor templado basado en MT[index] llamando a twist() cada n
     */
    public long extract_number() {
        if (this.index >= n) {
            this.twist();
        }
        long y = this.MT[this.index++];

        y ^= (y >>> u) & d;
        y ^= (y << s) & b;
        y ^= (y << t) & c;
        y ^= (y >>> l);

        return y;
    }

    /**
     * Genera un valor double pseudoaleatorio y uniformemente distribuido entre [0.0, 1.0)
     */
    public double random() {
        return (this.extract_number() >> 1) / (double) Long.MAX_VALUE;
    }

    /**
     * Genera un valor entero pseudoaleatorio entre [0, x)
     *
     * @param x Número entero límite, x > 0
     * @return Valor entero entre [0, x)
     * @throws IllegalArgumentException Si x <= 0
     */
    public int randint(int x) {
        if (x <= 0) {
            throw new IllegalArgumentException("n debe ser mayor a 0");
        }
        int bits, val;
        do {
            bits = (int) (extract_number() >> (w - r));
            val = bits % x;
        } while (bits - val + (x - 1) < 0);
        return val;
    }

    /**
     * Genera un valor float pseudoaleatorio entre [0.0, x)
     *
     * @param x Número float límite, x > 0.0
     * @return Valor float entre [0.0, x)
     * @throws IllegalArgumentException Si x <= 0
     */
    public float randfloat(float x) {
        if (x <= 0.0f) {
            throw new IllegalArgumentException("n debe ser mayor a 0.0");
        }
        return (float) this.random() * x;
    }

    /**
     * Genera un valor double pseudoaleatorio entre [0.0, x)
     *
     * @param x Número double límite, x > 0.0
     * @return Valor double entre [0.0, x)
     * @throws IllegalArgumentException Si x <= 0.0
     */
    public double randdouble(double x) {
        if (x <= 0.0d) {
            throw new IllegalArgumentException("n debe ser mayor a 0.0");
        }
        return this.random() * x;
    }

    /**
     * Genera un número entero aleatorio entre [min, max - 1]
     *
     * @param min Número entero incial, min < max
     * @param max Número entero final
     * @return Valor entero entre [min, max - 1]
     * @throws IllegalArgumentException Si min >= max
     */
    public int randrange(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("min debe ser menor que max");
        }
        return this.randint(max - min) + min;
    }

    /**
     * Obtener la semilla inicial
     *
     * @return semilla inicial
     */
    public long getSeed() {
        return this.MT[0];
    }
}