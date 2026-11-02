package gal.rodrigosambade.networkanalyzer;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import java.net.Inet4Address;

/**
 * Discover the active Wi-Fi IPv4 block from LinkProperties, including when a VPN
 * is the default network. Avoids deprecated WifiInfo.getIpAddress() and does not
 * request sensitive location permissions.
 */
public final class WifiSubnet {
    private WifiSubnet() {}
    public static String private24Prefix(Context context) {
        ConnectivityManager manager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) throw new IllegalStateException("El servicio de red no está disponible.");
        for (Network network : manager.getAllNetworks()) {
            NetworkCapabilities capabilities = manager.getNetworkCapabilities(network);
            if (capabilities == null
                    || !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) continue;
            LinkProperties properties = manager.getLinkProperties(network);
            if (properties == null) continue;
            for (LinkAddress link : properties.getLinkAddresses()) {
                if (!(link.getAddress() instanceof Inet4Address)) continue;
                // A /25, /26, etc. cannot be scanned as a /24 without crossing the subnet.
                if (link.getPrefixLength() > 24) {
                    throw new IllegalStateException(
                            "La red Wi-Fi utiliza una subred más pequeña que /24. Escaneo no iniciado.");
                }
                try {
                    return LanScanner.prefixFromAddress(link.getAddress().getHostAddress());
                } catch (IllegalArgumentException ignored) {
                    // Continue looking for a private IPv4 address.
                }
            }
        }
        throw new IllegalStateException(
                "No hay una red Wi-Fi privada IPv4 disponible para escanear.");
    }
}
