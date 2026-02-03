package spp.services.packetService;

import spp.core.SpacePacket;

public interface PacketEncoderService {
  byte[] encodePacket(SpacePacket spacePacket);
}