package spp.services.packetService;

import spp.core.SpacePacket;

/*
  PacketDecoderService class contains method decodePacket which is used to decode space packet
 */
public interface PacketDecoderService {
  SpacePacket decodePacket(byte[] packetBytes);
}