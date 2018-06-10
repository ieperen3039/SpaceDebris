package Results;

/**
 * @author Geert van Ieperen created on 21-4-2018.
 */
public class ProgressBar {

    private final long t0;
    private final int total;
    private int i = 0;

    public ProgressBar(int total) {
        this.total = total;
        t0 = System.currentTimeMillis();
    }

    /** call once after every update */
    public synchronized void printUpdate() {
        long ti = System.currentTimeMillis();
        double dt = ti - t0;
        double tps = dt / (++i);
        int msLeft = (int) (tps * (total - i));

        System.out.printf("\rRunning time: %3d sec | Avg time per update: %6.02f ms | Progress: %4.1f%% | Time remaining: %3d sec",
                (int) dt / 1000, tps, (i * 100.0) / total, msLeft / 1000
        );
        System.out.flush();
    }
}
