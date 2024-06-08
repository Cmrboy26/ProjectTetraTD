package net.cmr.rtd;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.xml.parsers.ParserConfigurationException;

import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.xml.sax.SAXException;

import com.esotericsoftware.kryonet.Server;

public class UPNPTest {

    public static void main(String[] args) throws SocketException, UnknownHostException, IOException, SAXException, ParserConfigurationException, InterruptedException {
        long start = System.nanoTime();
        GatewayDiscover discover = new GatewayDiscover();
        discover.discover();
        long end = System.nanoTime();
        System.out.println("Time to discover gateway: " + (end - start) / 1000000 + "ms");
        GatewayDevice d = discover.getValidGateway();
        System.out.println(d.getFriendlyName());
        InetAddress localAddress = d.getLocalAddress();
        System.out.println("Local address: " + localAddress);
        String externalIPAddress = d.getExternalIPAddress();
        System.out.println("External address: " + externalIPAddress);
        boolean portMappingSuccessful = d.addPortMapping(11266, 11266, localAddress.getHostAddress(), "TCP", "Test port mapping");
        System.out.println("Port mapping successful: " + portMappingSuccessful);

        Server server = new Server();
        server.start();
        server.bind(11266);

        Thread.sleep(10000);
        d.deletePortMapping(11266, "TCP");
        server.stop();
    }
    
}
