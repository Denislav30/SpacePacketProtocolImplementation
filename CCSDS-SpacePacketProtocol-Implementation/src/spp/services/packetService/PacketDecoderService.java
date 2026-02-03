package spp.services.packetService;

import spp.core.SpacePacket;

public interface PacketDecoderService {
  SpacePacket decodePacket(byte[] packetBytes);
}