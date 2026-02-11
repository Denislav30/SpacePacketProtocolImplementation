package ccsds.space.packet.protocol.services.packetService;

/*
  PacketListener class contains method indication which is representing the primitive PACKET.indication
 */
@FunctionalInterface
public interface PacketListener {
  void indication(PacketIndication packetIndication);
}
