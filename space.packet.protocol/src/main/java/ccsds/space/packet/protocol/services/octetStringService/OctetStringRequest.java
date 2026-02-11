package ccsds.space.packet.protocol.services.octetStringService;

import ccsds.space.packet.protocol.types.CommandType;

public record OctetStringRequest(byte[] octetString, int apid, boolean secondaryHeaderIndicator, CommandType packetType, int packetSequenceCountOrPacketName) {}