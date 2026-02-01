package spp.services.packetService;

import spp.core.SpacePacket;

public interface PacketService {
  // PACKET.request
  void request(SpacePacket spacePacket);
  void setListener(PacketListener packetListener);
}
