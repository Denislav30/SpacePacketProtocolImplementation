package spp.services.packetService;

import spp.core.SpacePacket;

/*
  PacketListener class contains method indication which is representing the primitive PACKET.indication
 */
@FunctionalInterface
public interface PacketListener {
  void indication(SpacePacket spacePacket);
}
