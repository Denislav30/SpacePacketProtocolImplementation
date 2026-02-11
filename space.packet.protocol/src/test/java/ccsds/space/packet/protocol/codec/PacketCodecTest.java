package ccsds.space.packet.protocol.codec;

import static org.junit.jupiter.api.Assertions.*;

import ccsds.space.packet.protocol.core.SpacePacket;
import ccsds.space.packet.protocol.core.SpacePacketHeader;
import ccsds.space.packet.protocol.types.CommandType;
import ccsds.space.packet.protocol.types.SequenceFlags;
import org.junit.jupiter.api.Test;

class PacketCodecTest {

  @Test
  void encodePacket_shouldHaveLengthSixPlusCPlusOne_correctSpacePacketGiven() {
    byte[] dataField = {1, 2, 3, 4, 5};
    int c = dataField.length - 1;

    SpacePacketHeader spacePacketHeader = new SpacePacketHeader(0, CommandType.TM, false, 100, SequenceFlags.UNSEGMENTED, 7, c);
    SpacePacket spacePacket = new SpacePacket(spacePacketHeader, dataField);
    PacketCodec packetCodec = new PacketCodec();
    byte[] encoded = packetCodec.encodePacket(spacePacket);

    assertEquals(6 + (c + 1), encoded.length);
  }

  @Test
  void decodePacket_shouldPreserveAllData_spacePacketGiven() {
    byte[] dataField = {1, 2, 3, 4, 5};
    int c = dataField.length - 1;

    SpacePacketHeader spacePacketHeader = new SpacePacketHeader(0, CommandType.TM, false, 100, SequenceFlags.UNSEGMENTED, 7, c);
    SpacePacket spacePacket = new SpacePacket(spacePacketHeader, dataField);
    PacketCodec packetCodec = new PacketCodec();

    byte[] encoded = packetCodec.encodePacket(spacePacket);
    SpacePacket decoded = packetCodec.decodePacket(encoded);

    assertEquals(0, decoded.getHeader().getPacketVersionNumber());
    assertEquals(CommandType.TM, decoded.getHeader().getPacketType());
    assertFalse(decoded.getHeader().isSecondaryHeaderFlag());
    assertEquals(100, decoded.getHeader().getApid());
    assertEquals(SequenceFlags.UNSEGMENTED, decoded.getHeader().getSequenceFlags());
    assertEquals(7, decoded.getHeader().getPacketSequenceCount());
    assertEquals(c, decoded.getHeader().getPacketDataLength());
    assertArrayEquals(dataField, decoded.getPacketDataField());
  }

  @Test
  void decodePacket_shouldThrowIllegalArgumentException_inputShorterThanSixBytesGiven() {
    PacketCodec packetCodec = new PacketCodec();
    byte[] bytes = {0, 1, 2, 3, 4};
    assertThrows(IllegalArgumentException.class, () -> packetCodec.decodePacket(bytes));
  }
}