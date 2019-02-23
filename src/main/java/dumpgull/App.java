package dumpgull;

import io.pkts.PacketHandler;
import io.pkts.Pcap;
import io.pkts.buffer.Buffer;
import io.pkts.packet.Packet;
import io.pkts.packet.TCPPacket;
import io.pkts.packet.UDPPacket;
import io.pkts.protocol.Protocol;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

public class App {

    private String password;
    private String inet;
    private String[] args;

    public App(String[] _args) {
        args = _args;
        init();

    }

    private void init() {
        String _password = "";
        String _inet = "";
        if (args.length == 0) {
            String envFilePath =  System.getenv("DUMPGULL_CONFIG");
            boolean hasEnv = envFilePath != null && ! envFilePath.equalsIgnoreCase("");
            if (hasEnv) {
                Properties p = new Properties();
                try {
                    p.load(new FileInputStream(Paths.get(envFilePath).toFile()));
                    _password = p.getProperty("password");
                    _inet = p.getProperty("inet");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            _password = args[0];
            _inet = args[1];
        }
        loadPassword(_password);
        loadInterface(_inet);
    }

    private void loadInterface(String value) {
        this.inet = value;
    }

    private void loadPassword(String value) {
        this.password = value;
    }
    Process proc;

    private void start() {
        Runtime rt = Runtime.getRuntime();
        String[] commands = buildCommands();
        try {
            proc = rt.exec(commands);
            printPcap();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printPcap() throws IOException {
        final Pcap pcap = Pcap.openStream(proc.getInputStream());
        pcap.loop(createPacketHandler());
    }

    private PacketHandler createPacketHandler() {
        return new PacketHandler() {
            public boolean nextPacket(Packet packet) throws IOException {
                if (packet.hasProtocol(Protocol.TCP)) {
                    printTcpPacket(packet);
                } else if (packet.hasProtocol(Protocol.UDP)) {

                    printUpdPacket(packet);
                }
                return true;
            }
        };
    }

    private void printUpdPacket(Packet packet) throws IOException {
        UDPPacket udpPacket = (UDPPacket) packet.getPacket(Protocol.UDP);
        Buffer buffer = udpPacket.getPayload();
        if (buffer != null) {
            System.out.println("UDP: " + udpPacket.getSourceIP());
        }
    }

    private void printTcpPacket(Packet packet) throws IOException {
        TCPPacket tcpPacket = (TCPPacket) packet.getPacket(Protocol.TCP);
        Buffer buffer = tcpPacket.getPayload();
        if (buffer != null) {
            System.out.println("TCP: " + tcpPacket.getSourceIP());
        }
    }

    private String[] buildCommands() {
        String tcpdumpCommand ;
        tcpdumpCommand = buildTcpDumpCommand();
        return new String[]{"bash", "-c", tcpdumpCommand};
    }


    private String buildTcpDumpCommand() {
        if (password != null) {
            return String.format("echo %s |sudo -S tcpdump -i %s -w -", password, inet);
        } else {
            return String.format("sudo -S tcpdump -i %s -w -", inet);
        }
    }

    public static void main(String[] args) {
        App app = new App(args) ;
        app.start();
    }
}
