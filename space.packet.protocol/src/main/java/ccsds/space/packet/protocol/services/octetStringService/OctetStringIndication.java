package ccsds.space.packet.protocol.services.octetStringService;

public record OctetStringIndication(byte[] octetString, int apid, boolean secondaryHeaderIndicator, Boolean dataLossIndicator) {}