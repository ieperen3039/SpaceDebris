/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Distributions;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author mboon
 */
public class GammaDistribution extends Distribution {

    private final ThreadLocalRandom rand = ThreadLocalRandom.current();
    protected double alpha, beta;

    public GammaDistribution(double alpha, double beta) {
        this.alpha = alpha;
        this.beta = beta;
    }

    @Override
    public double expectation() {
        return alpha / beta;
    }

    @Override
    public double variance() {
        return alpha / beta / beta;
    }

    public double nextRandom() {
        double shape = alpha;
        double scale = 1.0 / beta;
        if (shape < 1) {
            // [1]: p. 228, Algorithm GS

            while (true) {
                // Step 1:
                final double u = rand.nextDouble();
                final double bGS = 1 + shape / Math.E;
                final double p = bGS * u;

                if (p <= 1) {
                    // Step 2:

                    final double x = Math.pow(p, 1 / shape);
                    final double u2 = rand.nextDouble();

                    if (u2 <= Math.exp(-x)) {
                        return scale * x;
                    }  // else Reject

                } else {
                    // Step 3:

                    final double x = -1 * Math.log((bGS - p) / shape);
                    final double u2 = rand.nextDouble();

                    if (u2 <= Math.pow(x, shape - 1)) {
                        return scale * x;
                    } // else Reject
                }
            }
        }

        // Now shape >= 1

        final double d = shape - 0.333333333333333333;
        final double c = 1 / (3 * Math.sqrt(d));

        while (true) {
            final double x = rand.nextGaussian();
            final double v = (1 + c * x) * (1 + c * x) * (1 + c * x);

            if (v <= 0) {
                continue;
            }

            final double x2 = x * x;
            final double u = rand.nextDouble();

            // Squeeze
            if (u < 1 - 0.0331 * x2 * x2) {
                return scale * d * v;
            }

            if (Math.log(u) < 0.5 * x2 + d * (1 - v + Math.log(v))) {
                return scale * d * v;
            }
        }
    }


    public static void main(String[] arg) {
        double sumX = 0;
        double sumX2 = 0;
        GammaDistribution dist = new GammaDistribution(0.5, 0.25);
        int n = 100000;
        for (int i = 0; i < n; i++) {
            double r = dist.nextRandom();
            sumX += r;
            sumX2 += r * r;
        }
        double EX = sumX / n;
        double EX2 = sumX2 / n;
        System.out.println("E[X] = " + EX);
        System.out.println("Var[X] = " + (EX2 - EX * EX));
    }

}
