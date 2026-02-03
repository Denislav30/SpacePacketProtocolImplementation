package spp.services.packetService;

import spp.core.SpacePacket;

/*
  PacketService class contains method request which represents the primitive PACKET.request
 */
public interface PacketService {
  void request(SpacePacket spacePacket);
  void setListener(PacketListener packetListener);
}
