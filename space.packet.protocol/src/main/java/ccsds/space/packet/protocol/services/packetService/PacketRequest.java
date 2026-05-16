package ccsds.space.packet.protocol.services.packetService;

import ccsds.space.packet.protocol.core.SpacePacket;

public record PacketRequest(SpacePacket spacePacket, Integer qosRequirement) {}