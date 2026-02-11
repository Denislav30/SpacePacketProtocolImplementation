package ccsds.space.packet.protocol.services.packetService;

import ccsds.space.packet.protocol.core.SpacePacket;

/*
  PacketDecoderService class contains method decodePacket which is used to decode space packet
 */
public interface PacketDecoderService {
  SpacePacket decodePacket(byte[] packetBytes);
}