package gal.rodrigosambade.networkanalyzer;
import android.os.SystemClock;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/** Opt-in, bounded sequential speed sample against Cloudflare's public test endpoints. */
public final class SpeedTestEngine {
    public static final int DOWNLOAD_BYTES=5_000_000;
    public static final int UPLOAD_BYTES=1_000_000;
    public static final long TOTAL_TRANSFER_LIMIT=DOWNLOAD_BYTES+UPLOAD_BYTES;
    public static final String HOST="speed.cloudflare.com";
    public static final class Result {
        public final double downloadMbps,uploadMbps,latencyMs,variationMs;
        public final int failedConnections,attempts;
        public Result(double d,double u,double latency,double variation,int failed,int attempts){
            this.downloadMbps=d;this.uploadMbps=u;this.latencyMs=latency;
            this.variationMs=variation;this.failedConnections=failed;this.attempts=attempts;
        }
    }
    private SpeedTestEngine(){}
    public static Result run() throws IOException {
        LatencyTest.Result latency=LatencyTest.run(HOST,443,8);
        return new Result(download(),upload(),latency.averageMs,latency.variationMs,latency.failed,latency.attempts);
    }
    static double download() throws IOException {
        HttpURLConnection conn=(HttpURLConnection)new URL("https://speed.cloudflare.com/__down?bytes="+DOWNLOAD_BYTES).openConnection();
        conn.setConnectTimeout(7000);conn.setReadTimeout(13000);
        conn.setUseCaches(false);conn.setRequestProperty("Accept-Encoding","identity");
        long count=0,start=0,end=0;
        try {
            if(conn.getResponseCode()!=200)throw new IOException("Download HTTP "+conn.getResponseCode());
            start=SystemClock.elapsedRealtimeNanos();
            try(InputStream stream=conn.getInputStream()) {
                byte[] b=new byte[32*1024];int n;
                while((n=stream.read(b))!=-1) {
                    count+=n;
                    if(count>DOWNLOAD_BYTES)throw new IOException("Download exceeded size limit");
                    if(Thread.currentThread().isInterrupted())throw new IOException("Cancelled");
                }
            }
            end=SystemClock.elapsedRealtimeNanos();
            if(count!=DOWNLOAD_BYTES)throw new IOException("Incomplete download: "+count+" bytes");
            return SpeedMath.megabitsPerSecond(count,end-start);
        } finally {conn.disconnect();}
    }
    static double upload() throws IOException {
        HttpURLConnection conn=(HttpURLConnection)new URL("https://speed.cloudflare.com/__up").openConnection();
        conn.setRequestMethod("POST");conn.setDoOutput(true);
        conn.setConnectTimeout(7000);conn.setReadTimeout(13000);
        conn.setFixedLengthStreamingMode(UPLOAD_BYTES);
        conn.setRequestProperty("Content-Type","application/octet-stream");
        conn.setUseCaches(false);
        long start=SystemClock.elapsedRealtimeNanos(),end;
        try{
            byte[] b=new byte[16*1024];
            try(OutputStream stream=conn.getOutputStream()) {
                int left=UPLOAD_BYTES;
                while(left>0){
                    if(Thread.currentThread().isInterrupted())throw new IOException("Cancelled");
                    int n=Math.min(left,b.length);stream.write(b,0,n);left-=n;
                }
                stream.flush();
            }
            int status=conn.getResponseCode();
            if(status<200||status>=300)throw new IOException("Upload HTTP "+status);
            end=SystemClock.elapsedRealtimeNanos();
            return SpeedMath.megabitsPerSecond(UPLOAD_BYTES,end-start);
        } finally {conn.disconnect();}
    }
}
