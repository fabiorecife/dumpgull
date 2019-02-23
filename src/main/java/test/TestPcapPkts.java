package test;

import io.pkts.PacketHandler;
import io.pkts.Pcap;
import io.pkts.buffer.Buffer;
import io.pkts.packet.Packet;
import io.pkts.packet.TCPPacket;
import io.pkts.packet.UDPPacket;
import io.pkts.protocol.Protocol;

import java.io.IOException;

public class TestPcapPkts {

    public static void main(String[] args) throws IOException {
        final Pcap pcap = Pcap.openStream("/home/fabio/Documents/jfna/tcpdump/test.pcap");

        pcap.loop(new PacketHandler() {
            public boolean nextPacket(Packet packet) throws IOException {

                if (packet.hasProtocol(Protocol.TCP)) {

                    TCPPacket tcpPacket = (TCPPacket) packet.getPacket(Protocol.TCP);
                    Buffer buffer = tcpPacket.getPayload();
                    if (buffer != null) {
                        System.out.println("TCP: " + tcpPacket.getSourceIP());
                    }
                } else if (packet.hasProtocol(Protocol.UDP)) {

                    UDPPacket udpPacket = (UDPPacket) packet.getPacket(Protocol.UDP);
                    Buffer buffer = udpPacket.getPayload();
                    if (buffer != null) {
                        System.out.println("UDP: " + udpPacket.getSourceIP());
                    }
                }
                return true;
            }
        });
    }
}
