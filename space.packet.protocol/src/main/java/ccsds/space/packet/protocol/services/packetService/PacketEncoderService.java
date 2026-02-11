package ccsds.space.packet.protocol.services.packetService;

import ccsds.space.packet.protocol.core.SpacePacket;

/*
  PacketEncoderService class contains method encodePacket which is used to encode space packet
 */
public interface PacketEncoderService {
  byte[] encodePacket(SpacePacket spacePacket);
}