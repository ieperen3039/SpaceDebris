package Distributions;

/**
 * @author Geert van Ieperen created on 25-3-2018.
 */
public class ConstantValue extends Distribution {

    private final double returnValue;

    public ConstantValue(double returnValue) {
        this.returnValue = returnValue;
    }

    @Override
    public double expectation() {
        return returnValue;
    }

    @Override
    public double variance() {
        return 0;
    }

    @Override
    public double nextRandom() {
        return returnValue;
    }
}
