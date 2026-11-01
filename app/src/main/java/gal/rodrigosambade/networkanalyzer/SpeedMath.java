package gal.rodrigosambade.networkanalyzer;
/** Unit-tested conversion of transferred bytes and measured elapsed nanoseconds to Mbps. */
public final class SpeedMath {
    private SpeedMath() {}
    public static double megabitsPerSecond(long bytes,long nanos) {
        if(bytes<=0 || nanos<=0)return 0;
        return bytes*8_000d/nanos;
    }
    public static double average(double[] values) {
        if(values.length==0)return 0;
        double sum=0;for(double v:values)sum+=v;return sum/values.length;
    }
    /** Mean absolute consecutive difference, not IPDV/RFC-compliant jitter. */
    public static double variation(double[] values) {
        if(values.length<2)return 0;
        double sum=0;for(int i=1;i<values.length;i++)sum+=Math.abs(values[i]-values[i-1]);
        return sum/(values.length-1);
    }
}
