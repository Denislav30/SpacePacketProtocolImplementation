package spp.services.packetService;

import spp.core.SpacePacket;

@FunctionalInterface
public interface PacketListener {
  // PACKET.indication
  void indication(SpacePacket spacePacket);
}
