package gal.rodrigosambade.networkanalyzer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
/** Explicit private-LAN /24 scan only; an absent service does not imply an absent host. */
public final class LanScanner {
    private LanScanner(){}
    static boolean isPrivateIpv4(int a,int b) {
        return a==10 || a==192&&b==168 || a==172&&b>=16&&b<=31;
    }
    public static String prefixFromWifiAddress(int encoded) {
        int a=encoded&255,b=(encoded>>>8)&255,c=(encoded>>>16)&255;
        if(!isPrivateIpv4(a,b))throw new IllegalArgumentException("Only private Wi-Fi IPv4 networks are supported");
        return a+"."+b+"."+c+".";
    }
    /** Validate a dotted IPv4 address and derive the single /24 block containing it. */
    public static String prefixFromAddress(String address) {
        if (address == null || !address.matches("(?:[0-9]{1,3}\\.){3}[0-9]{1,3}"))
            throw new IllegalArgumentException("Invalid Wi-Fi IPv4 address");
        String[] octets = address.split("\\.");
        int[] numbers = new int[4];
        for (int i = 0; i < 4; i++) {
            numbers[i] = Integer.parseInt(octets[i]);
            if (numbers[i] < 0 || numbers[i] > 255)
                throw new IllegalArgumentException("Invalid IPv4 octet");
        }
        if (!isPrivateIpv4(numbers[0], numbers[1]))
            throw new IllegalArgumentException("Only private Wi-Fi IPv4 networks are supported");
        return numbers[0] + "." + numbers[1] + "." + numbers[2] + ".";
    }
    public static List<String> scan(String prefix) throws Exception {
        if(prefix==null||!prefix.matches("(?:\\d{1,3}\\.){3}"))
            throw new IllegalArgumentException("Invalid subnet");
        String[] parts=prefix.substring(0,prefix.length()-1).split("\\.");
        if(parts.length!=3)throw new IllegalArgumentException("Invalid subnet");
        int a=Integer.parseInt(parts[0]),b=Integer.parseInt(parts[1]),c=Integer.parseInt(parts[2]);
        if(a>255||b>255||c>255||!isPrivateIpv4(a,b))
            throw new IllegalArgumentException("Private IPv4 only");
        ExecutorService pool=Executors.newFixedThreadPool(12);
        try {
            List<Future<String>> pending=new ArrayList<>();
            for(int i=1;i<=254;i++){
                final String address=prefix+i;
                pending.add(pool.submit(()->visible(address)?address:null));
            }
            List<String> found=new ArrayList<>();
            for(Future<String> item:pending){
                if(Thread.currentThread().isInterrupted())throw new InterruptedException("Cancelled");
                try {String result=item.get(15,TimeUnit.SECONDS);if(result!=null)found.add(result);}
                catch(ExecutionException|TimeoutException ignored){}
            }
            return found;
        } finally {pool.shutdownNow();}
    }
    private static boolean visible(String address) {
        try(Socket s=new Socket()) {
            s.connect(new InetSocketAddress(InetAddress.getByName(address),80),220);
            return true;
        }catch(Exception ignored){}
        try(Socket s=new Socket()) {
            s.connect(new InetSocketAddress(InetAddress.getByName(address),443),220);
            return true;
        }catch(Exception ignored){}
        return false;
    }
}
