package gal.rodrigosambade.networkanalyzer;
import org.junit.Test;
import static org.junit.Assert.*;
public class SpeedMathTest {
    @Test public void conversion(){assertEquals(8d,SpeedMath.megabitsPerSecond(1_000_000,1_000_000_000),.001);}
    @Test public void emptySamples(){assertEquals(0,SpeedMath.average(new double[0]),0);assertEquals(0,SpeedMath.variation(new double[]{10}),0);}
    @Test public void variation(){assertEquals(5,SpeedMath.variation(new double[]{10,15,10}),.001);}
    @Test public void noNegativeSpeed(){assertEquals(0,SpeedMath.megabitsPerSecond(-1,5),0);}
    @Test public void privateAddresses(){assertTrue(LanScanner.isPrivateIpv4(10,1));assertTrue(LanScanner.isPrivateIpv4(172,20));assertTrue(LanScanner.isPrivateIpv4(192,168));assertFalse(LanScanner.isPrivateIpv4(172,31+1));assertFalse(LanScanner.isPrivateIpv4(8,8));}
    @Test public void addressConversion(){
        assertEquals("192.168.1.",LanScanner.prefixFromWifiAddress((192)|(168<<8)|(1<<16)|(12<<24)));
    }
    @Test(expected=IllegalArgumentException.class)public void publicAddressRejected(){LanScanner.prefixFromWifiAddress((8)|(8<<8)|(8<<16)|(8<<24));}
}
