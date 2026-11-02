package gal.rodrigosambade.networkanalyzer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import gal.rodrigosambade.networkanalyzer.connectivity.ConnectivityAndInternetAccess;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public final class MainActivity extends Activity {
    private final ExecutorService tasks=Executors.newSingleThreadExecutor();
    private final Handler main=new Handler(Looper.getMainLooper());
    private HistoryStore history;
    private TextView output;
    private Button speedButton;
    private boolean busy;
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        history=new HistoryStore(this);
        ScrollView scroll=new ScrollView(this);
        LinearLayout stack=new LinearLayout(this);stack.setOrientation(LinearLayout.VERTICAL);stack.setPadding(dp(16),dp(14),dp(16),dp(16));
        scroll.addView(stack);
        TextView title=new TextView(this);title.setText("Network Analyzer");title.setTextSize(26);stack.addView(title);
        TextView note=new TextView(this);note.setText("Diagnóstico local y pruebas iniciadas por el usuario. Sin análisis permanente ni publicidad.");
        stack.addView(note);
        addButton(stack,"Estado y diagnóstico de conectividad",this::showConnectivity);
        addButton(stack,"Inspeccionar Wi-Fi y capacidades del enlace",this::showWifi);
        speedButton=addButton(stack,"Prueba de descarga, subida y latencia",this::confirmSpeed);
        addButton(stack,"Descubrir hosts de mi Wi-Fi (/24)",this::confirmLan);
        addButton(stack,"Historial de mediciones",this::showHistory);
        addButton(stack,"Exportar historial CSV",this::shareHistory);
        output=new TextView(this);output.setTextSize(16);output.setTextIsSelectable(true);
        output.setPadding(dp(4),dp(16),dp(4),dp(12));
        output.setText("Selecciona una prueba. Las pruebas de velocidad transfieren hasta 6 MB y comunican tu IP al servidor de Cloudflare.");
        stack.addView(output);
        setContentView(scroll);
    }
    private int dp(int x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
    private Button addButton(LinearLayout parent,String title,Runnable task){
        Button b=new Button(this);b.setText(title);b.setAllCaps(false);
        parent.addView(b,new LinearLayout.LayoutParams(-1,dp(60)));
        b.setOnClickListener(v->task.run());return b;
    }
    private void show(String s){main.post(()->{if(!isFinishing())output.setText(s);});}
    @FunctionalInterface private interface CheckedRunnable { void run() throws Exception; }
    private void execute(CheckedRunnable r){
        if(busy){Toast.makeText(this,"Hay una prueba en curso",Toast.LENGTH_SHORT).show();return;}
        busy=true;
        tasks.execute(()->{try{r.run();}catch(Exception e){show("Error: "+e.getClass().getSimpleName()+" — "+e.getMessage());}
            finally{main.post(()->busy=false);}});
    }
    private void showConnectivity(){
        execute(()->{
            boolean connected=ConnectivityAndInternetAccess.isConnected(this);
            boolean physical=ConnectivityAndInternetAccess.hasPhysicalNetwork(this);
            boolean validated=ConnectivityAndInternetAccess.isInternetValidated(this);
            boolean captive=ConnectivityAndInternetAccess.isCaptivePortalDetected(this);
            boolean vpn=ConnectivityAndInternetAccess.vpnActive(this);
            show("Red activa: "+connected+"\nTransporte físico: "+physical+"\nValidada por Android: "+validated+
                    "\nPortal cautivo: "+captive+"\nVPN activa: "+vpn+"\n\nEjecutando diagnóstico activo…");
            ConnectivityAndInternetAccess.InternetResult response=ConnectivityAndInternetAccess.checkInternetBlockingDefault(this);
            show("Red activa: "+connected+"\nTransporte físico: "+physical+"\nValidación Android: "+validated+
                    "\nPortal cautivo: "+captive+"\nVPN: "+vpn+"\n\nResultado del Gist:\nAlcanzable: "+response.isReachable()+"\nDestino: "+response.getReachedHost()+"\nDuración: "+response.getElapsedMilliseconds()+" ms\nIntentos: "+response.getAttemptedHosts());
        });
    }
    private void showWifi(){
        try {
            ConnectivityManager cm=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
            Network network=cm.getActiveNetwork();
            NetworkCapabilities cap=network==null?null:cm.getNetworkCapabilities(network);
            WifiManager wifi=(WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo info=wifi==null?null:wifi.getConnectionInfo();
            StringBuilder s=new StringBuilder();
            if(cap==null){show("Sin red activa");return;}
            s.append("Wi-Fi: ").append(cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)).append('\n');
            s.append("Móvil: ").append(cap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)).append('\n');
            s.append("Ethernet: ").append(cap.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)).append('\n');
            s.append("VPN: ").append(cap.hasTransport(NetworkCapabilities.TRANSPORT_VPN)).append('\n');
            s.append("Ancho de banda estimado ↓: ").append(cap.getLinkDownstreamBandwidthKbps()/1000d).append(" Mbps\n");
            s.append("Ancho de banda estimado ↑: ").append(cap.getLinkUpstreamBandwidthKbps()/1000d).append(" Mbps\n");
            if(cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)&&info!=null){
                s.append("RSSI: ").append(info.getRssi()).append(" dBm\n");
                s.append("Frecuencia: ").append(info.getFrequency()).append(" MHz\n");
                s.append("Velocidad del enlace: ").append(info.getLinkSpeed()).append(" Mbps\n");
            }
            s.append("\nIMPORTANTE: valores estimados del enlace, NO una prueba de velocidad real.");
            show(s.toString());
        }catch(Exception e){show("No se puede inspeccionar Wi-Fi: "+e.getMessage());}
    }
    private void confirmSpeed(){
        new AlertDialog.Builder(this).setTitle("Consentimiento para la prueba")
          .setMessage("Se transferirán hasta 5 MB de descarga y 1 MB de subida, con posibles datos adicionales de protocolo. Tu dirección IP y métricas de conexión llegarán a Cloudflare. Puede consumir datos móviles. ¿Ejecutar?")
          .setPositiveButton("Iniciar",(d,w)->execute(()->{
              show("Midiendo latencia, descarga y subida…");
              SpeedTestEngine.Result r=SpeedTestEngine.run();
              history.add(r);
              show(String.format(Locale.ROOT,"Descarga: %.2f Mbps\nSubida: %.2f Mbps\nConexión TCP: %.1f ms\nVariación entre conexiones: %.1f ms\nIntentos fallidos: %d de %d\n\nNota: latencia TCP, no RTT ICMP. Los fallos TCP no equivalen a pérdida de paquetes. Prueba corta y orientativa.",r.downloadMbps,r.uploadMbps,r.latencyMs,r.variationMs,r.failedConnections,r.attempts));
          })).setNegativeButton("Cancelar",null).show();
    }
    private void confirmLan(){
        new AlertDialog.Builder(this).setTitle("Escaneo local")
          .setMessage("¿Comprobar hasta 254 direcciones de tu subred Wi-Fi privada, únicamente en puertos TCP 80 y 443? Utilízalo solo en redes propias o autorizadas. No garantiza descubrir todos los equipos.")
          .setPositiveButton("Escanear",(d,w)->execute(()->{
              WifiSubnet.Subnet subnet=WifiSubnet.findPrivate24(this);
              String prefix=subnet.prefix;
              show("Explorando "+prefix+"0/24…");
              List<String> hosts=LanScanner.scan(prefix,subnet.network.getSocketFactory());
              show("Hosts con TCP 80/443 accesible en "+prefix+"0/24: "+hosts.size()+
                "\n"+String.join("\n",hosts)+"\n\nNota: no detecta dispositivos con esos puertos cerrados.");
          })).setNegativeButton("Cancelar",null).show();
    }
    private void showHistory(){
        org.json.JSONArray arr=history.records();StringBuilder b=new StringBuilder("Mediciones recientes: "+arr.length()+"\n\n");
        for(int i=0;i<Math.min(25,arr.length());i++){
            org.json.JSONObject row=arr.optJSONObject(i);if(row==null)continue;
            b.append(new java.util.Date(row.optLong("time"))).append("\n↓ ")
             .append(String.format(Locale.ROOT,"%.2f",row.optDouble("down")))
             .append(" Mbps  ↑ ").append(String.format(Locale.ROOT,"%.2f",row.optDouble("up")))
             .append(" Mbps\n\n");
        }show(b.toString());
    }
    private void shareHistory(){
        Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/csv");
        i.putExtra(Intent.EXTRA_SUBJECT,"Network Analyzer - speed test history");
        i.putExtra(Intent.EXTRA_TEXT,history.csv());
        startActivity(Intent.createChooser(i,"Compartir informe CSV"));
    }
    @Override public void onDestroy(){tasks.shutdownNow();super.onDestroy();}
}
