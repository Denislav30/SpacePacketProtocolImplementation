package spp.codec;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import spp.core.SpacePacket;
import spp.core.SpacePacketHeader;
import spp.services.packetService.PacketDecoderService;
import spp.services.packetService.PacketEncoderService;
import spp.types.CommandType;
import spp.types.SequenceFlags;

public class PacketCodec implements PacketEncoderService, PacketDecoderService {

  @Override
  public SpacePacket decodePacket(byte[] packetBytes) {
    if (packetBytes == null){
      throw new IllegalArgumentException("PacketBytes is null!");
    }
    if (packetBytes.length < 1) {
      throw new IllegalArgumentException("PacketBytes is empty!");
    }

    try {
      ByteBuffer byteBuffer = ByteBuffer.wrap(packetBytes);

      int version = Byte.toUnsignedInt(byteBuffer.get());
      int typeBit = Byte.toUnsignedInt(byteBuffer.get());
      int secondaryHeaderBit = Byte.toUnsignedInt(byteBuffer.get());
      int apid = Short.toUnsignedInt(byteBuffer.getShort());
      int sequenceFlagsBits = Byte.toUnsignedInt(byteBuffer.get());
      int sequenceCount = Short.toUnsignedInt(byteBuffer.getShort());
      int c = Short.toUnsignedInt(byteBuffer.getShort());

      int dataLength = byteBuffer.getInt();
      if (dataLength < 1 || dataLength > byteBuffer.remaining()) {
        throw new IllegalArgumentException("Invalid dataLength!" + dataLength);
      }

      byte[] data = new byte[dataLength];
      byteBuffer.get(data);

      CommandType commandType = CommandType.getCommandType(typeBit);
      SequenceFlags sequenceFlags = SequenceFlags.getSequenceFlags(sequenceFlagsBits);

      boolean secondaryHeader = (secondaryHeaderBit == 1);

      SpacePacketHeader spacePacketHeader = new SpacePacketHeader(version, commandType, secondaryHeader, apid, sequenceFlags, sequenceCount, c);
      spacePacketHeader.validateHeaderFields();

      return new SpacePacket(spacePacketHeader, data);

    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalArgumentException("Cannot decode packet!", e);
    }
  }

  @Override
  public byte[] encodePacket(SpacePacket spacePacket) {
    if (spacePacket == null) {
      throw new IllegalArgumentException("SpacePacket is null!");
    }
    if (spacePacket.getHeader() == null) {
      throw new IllegalArgumentException("Header is null!");
    }
    if (spacePacket.getPacketDataField() == null) {
      throw new IllegalArgumentException("PacketDataField is null!");
    }

    SpacePacketHeader spacePacketHeader = spacePacket.getHeader();
    spacePacketHeader.validateHeaderFields();

    byte[] data = spacePacket.getPacketDataField();
    if (data.length < 1) {
      throw new IllegalArgumentException("Packet Data Field must be at least 1 byte!");
    }

    int computedC = data.length - 1;

    if (spacePacketHeader.getPacketDataLength() != computedC) {
      throw new IllegalArgumentException("Header packetDataLength (C) does not match data field length!");
    }

    try {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

      dataOutputStream.writeByte(spacePacketHeader.getPacketVersionNumber());
      dataOutputStream.writeByte(spacePacketHeader.getPacketType().getValue());
      dataOutputStream.writeByte(spacePacketHeader.isSecondaryHeaderFlag() ? 1 : 0);
      dataOutputStream.writeShort(spacePacketHeader.getApid());
      dataOutputStream.writeByte(spacePacketHeader.getSequenceFlags().getValue());
      dataOutputStream.writeShort(spacePacketHeader.getPacketSequenceCount());
      dataOutputStream.writeShort(spacePacketHeader.getPacketDataLength());

      dataOutputStream.writeInt(data.length);
      dataOutputStream.write(data);

      dataOutputStream.flush();
      return byteArrayOutputStream.toByteArray();

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}