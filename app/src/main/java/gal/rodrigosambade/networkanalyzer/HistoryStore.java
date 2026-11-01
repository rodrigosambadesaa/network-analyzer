package gal.rodrigosambade.networkanalyzer;
import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public final class HistoryStore {
    private final SharedPreferences prefs;
    public HistoryStore(Context context){prefs=context.getSharedPreferences("speed_history",Context.MODE_PRIVATE);}
    public void add(SpeedTestEngine.Result result){
        try {
            JSONArray saved=records(),next=new JSONArray();
            JSONObject row=new JSONObject();
            row.put("time",System.currentTimeMillis());
            row.put("down",result.downloadMbps);row.put("up",result.uploadMbps);
            row.put("latency",result.latencyMs);row.put("variation",result.variationMs);
            row.put("failures",result.failedConnections);row.put("probes",result.attempts);
            next.put(row);
            for(int i=0;i<Math.min(saved.length(),99);i++)next.put(saved.getJSONObject(i));
            prefs.edit().putString("runs",next.toString()).apply();
        }catch(Exception ignored){}
    }
    public JSONArray records(){
        try{return new JSONArray(prefs.getString("runs","[]"));}catch(Exception e){return new JSONArray();}
    }
    public String csv(){
        StringBuilder out=new StringBuilder("timestamp_utc,download_mbps,upload_mbps,tcp_connect_ms,variation_ms,tcp_connect_failures,probes\n");
        JSONArray arr=records();
        for(int i=0;i<arr.length();i++){
            JSONObject r=arr.optJSONObject(i);if(r==null)continue;
            out.append(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.ROOT){{setTimeZone(java.util.TimeZone.getTimeZone("UTC"));}}.format(new Date(r.optLong("time")))).append(',');
            out.append(r.optDouble("down")).append(',').append(r.optDouble("up")).append(',');
            out.append(r.optDouble("latency")).append(',').append(r.optDouble("variation")).append(',');
            out.append(r.optInt("failures")).append(',').append(r.optInt("probes")).append('\n');
        }
        return out.toString();
    }
}
