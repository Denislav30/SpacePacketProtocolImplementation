package spp.services.packetService;

import spp.core.SpacePacket;

/*
  PacketEncoderService class contains method encodePacket which is used to encode space packet
 */
public interface PacketEncoderService {
  byte[] encodePacket(SpacePacket spacePacket);
}