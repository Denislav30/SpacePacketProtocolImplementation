package ccsds.space.packet.protocol.services.packetService;

import ccsds.space.packet.protocol.core.SpacePacket;

public record PacketIndication(SpacePacket spacePacket, int apid, Boolean packetLossIndicator) {}