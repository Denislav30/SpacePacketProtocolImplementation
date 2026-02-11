package ccsds.space.packet.protocol.codec;

import ccsds.space.packet.protocol.core.SpacePacket;
import ccsds.space.packet.protocol.core.SpacePacketHeader;
import ccsds.space.packet.protocol.services.packetService.PacketDecoderService;
import ccsds.space.packet.protocol.services.packetService.PacketEncoderService;

public class PacketCodec implements PacketEncoderService, PacketDecoderService {

  @Override
  public SpacePacket decodePacket(byte[] packetBytes) {
    if (packetBytes == null) {
      throw new IllegalArgumentException("PacketBytes are null!");
    }
    if (packetBytes.length < 6) {
      throw new IllegalArgumentException("PacketBytes need at least 6 bytes for primary header!");
    }

    byte[] primaryHeader = new byte[6];
    System.arraycopy(packetBytes, 0, primaryHeader, 0, 6);

    SpacePacketHeader spacePacketHeader = SpacePacketHeader.parsePacketPrimaryHeader(primaryHeader);

    int dataFieldLength = spacePacketHeader.getPacketDataFieldOctets();

    int totalLength = 6 + dataFieldLength;
    if (packetBytes.length < totalLength) {
      throw new IllegalArgumentException("PacketBytes are incomplete!");
    }

    byte[] dataField = new byte[dataFieldLength];
    System.arraycopy(packetBytes, 6, dataField, 0, dataFieldLength);

    SpacePacket spacePacket = new SpacePacket(spacePacketHeader, dataField);
    spacePacket.validateSpacePacket();
    return spacePacket;
  }

  @Override
  public byte[] encodePacket(SpacePacket spacePacket) {
    if (spacePacket == null) {
      throw new IllegalArgumentException("SpacePacket is null!");
    }

    spacePacket.validateSpacePacket();

    SpacePacketHeader spacePacketHeader = spacePacket.getHeader();
    byte[] packetDataField = spacePacket.getPacketDataField();

    byte[] primaryHeader = spacePacketHeader.convertToPacketPrimaryHeaderBytes();

    byte[] bytes = new byte[6 + packetDataField.length];
    System.arraycopy(primaryHeader, 0, bytes, 0, 6);
    System.arraycopy(packetDataField, 0, bytes, 6, packetDataField.length);

    return bytes;
  }
}