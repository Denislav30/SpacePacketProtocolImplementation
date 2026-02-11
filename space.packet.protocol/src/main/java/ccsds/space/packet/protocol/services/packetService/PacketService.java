package ccsds.space.packet.protocol.services.packetService;

/*
  PacketService class contains method request which represents the primitive PACKET.request
 */
public interface PacketService {
  void request(PacketRequest packetRequest);
  void setPacketListener(PacketListener packetListener);
}
