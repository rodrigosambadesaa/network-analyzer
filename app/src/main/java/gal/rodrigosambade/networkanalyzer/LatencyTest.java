package gal.rodrigosambade.networkanalyzer;
import android.os.SystemClock;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
public final class LatencyTest {
    public static final class Result {
        public final double averageMs,variationMs;
        public final int failed,attempts;
        Result(double mean,double variation,int failed,int attempts){
            this.averageMs=mean;this.variationMs=variation;this.failed=failed;this.attempts=attempts;
        }
    }
    private LatencyTest(){}
    /** TCP connect samples: failures are NOT equivalent to network packet-loss measurement. */
    public static Result run(String host,int port,int probes) {
        if(probes<1||probes>20)throw new IllegalArgumentException("1..20 probes");
        ArrayList<Double> times=new ArrayList<>();
        int failures=0;
        for(int i=0;i<probes;i++){
            if(Thread.currentThread().isInterrupted())break;
            long start=SystemClock.elapsedRealtimeNanos();
            try(Socket socket=new Socket()) {
                socket.connect(new InetSocketAddress(host,port),2500);
                times.add((SystemClock.elapsedRealtimeNanos()-start)/1_000_000d);
            } catch(Exception e){failures++;}
        }
        double[] arr=new double[times.size()];for(int i=0;i<times.size();i++)arr[i]=times.get(i);
        return new Result(SpeedMath.average(arr),SpeedMath.variation(arr),failures,times.size()+failures);
    }
}
