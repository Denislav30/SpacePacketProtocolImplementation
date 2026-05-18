package ccsds.space.packet.protocol.codec;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ccsds.space.packet.protocol.core.SpacePacket;
import ccsds.space.packet.protocol.core.SpacePacketHeader;
import ccsds.space.packet.protocol.types.CommandType;
import ccsds.space.packet.protocol.types.SequenceFieldType;
import ccsds.space.packet.protocol.types.SequenceFlags;
import org.junit.jupiter.api.Test;

class PacketCodecTest {

  @Test
  void encodePacket_shouldReturnPacketWithCorrectTotalLength() {
    byte[] packetSecondaryHeader = new byte[0];
    byte[] userDataField = {1, 2, 3, 4, 5};
    int c = (packetSecondaryHeader.length + userDataField.length) - 1;

    SpacePacketHeader spacePacketHeader = new SpacePacketHeader(0, CommandType.TM,
        false, 100, SequenceFlags.UNSEGMENTED, 7,
        SequenceFieldType.PACKET_SEQUENCE_COUNT, c);

    SpacePacket spacePacket = new SpacePacket(spacePacketHeader, packetSecondaryHeader, userDataField);
    PacketCodec packetCodec = new PacketCodec();
    byte[] encoded = packetCodec.encodePacket(spacePacket);
    assertEquals(6 + (c + 1), encoded.length);
  }

  @Test
  void decodePacket_shouldPreserveAllData_spacePacketGiven() {
    byte[] packetSecondaryHeader = new byte[0];
    byte[] userDataField = {1, 2, 3, 4, 5};
    int c = (packetSecondaryHeader.length + userDataField.length) - 1;
    byte[] expectedEmptySecondaryHeader = new byte[0];

    SpacePacketHeader spacePacketHeader = new SpacePacketHeader(0, CommandType.TM,
        false, 100, SequenceFlags.UNSEGMENTED, 7,
        SequenceFieldType.PACKET_SEQUENCE_COUNT, c);

    SpacePacket spacePacket = new SpacePacket(spacePacketHeader, packetSecondaryHeader, userDataField);
    PacketCodec packetCodec = new PacketCodec();

    byte[] encoded = packetCodec.encodePacket(spacePacket);
    SpacePacket decoded = packetCodec.decodePacket(encoded);

    assertEquals(0, decoded.getHeader().getPacketVersionNumber());
    assertEquals(CommandType.TM, decoded.getHeader().getPacketType());
    assertFalse(decoded.getHeader().isSecondaryHeaderFlag());
    assertEquals(100, decoded.getHeader().getApid());
    assertEquals(SequenceFlags.UNSEGMENTED, decoded.getHeader().getSequenceFlags());
    assertEquals(7, decoded.getHeader().getSequenceFieldValue());
    assertEquals(c, decoded.getHeader().getPacketDataLength());
    assertArrayEquals(expectedEmptySecondaryHeader, decoded.getPacketSecondaryHeader());
    assertArrayEquals(userDataField, decoded.getUserDataField());
    assertArrayEquals(userDataField, decoded.getPacketDataField());
  }

  @Test
  void decodePacket_shouldThrowIllegalArgumentException_inputShorterThanSixBytesGiven() {
    PacketCodec packetCodec = new PacketCodec();
    byte[] bytes = {0, 1, 2, 3, 4};
    assertThrows(IllegalArgumentException.class, () -> packetCodec.decodePacket(bytes));
  }

  @Test
  void decodePacket_shouldPreserveSecondaryHeaderAndUserData_whenSecondaryHeaderIsPresent() {
    byte[] packetSecondaryHeader = {11, 12};
    byte[] userDataField = {1, 2, 3, 4};
    int c = (packetSecondaryHeader.length + userDataField.length) - 1;
    byte[] expectedPacketDataField = {11, 12, 1, 2, 3, 4};

    SpacePacketHeader spacePacketHeader = new SpacePacketHeader(0, CommandType.TC,
        true, 100, SequenceFlags.UNSEGMENTED, 7,
        SequenceFieldType.PACKET_SEQUENCE_COUNT, c);

    SpacePacket spacePacket = new SpacePacket(spacePacketHeader, packetSecondaryHeader, userDataField);
    PacketCodec packetCodec = new PacketCodec();

    byte[] encoded = packetCodec.encodePacket(spacePacket);
    SpacePacket decoded = packetCodec.decodePacket(encoded, 2);

    assertArrayEquals(packetSecondaryHeader, decoded.getPacketSecondaryHeader());
    assertArrayEquals(userDataField, decoded.getUserDataField());
    assertArrayEquals(expectedPacketDataField, decoded.getPacketDataField());
  }
}