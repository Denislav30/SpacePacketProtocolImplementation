package ccsds.space.packet.protocol.core;

import static org.junit.jupiter.api.Assertions.*;

import ccsds.space.packet.protocol.types.CommandType;
import ccsds.space.packet.protocol.types.SequenceFlags;
import org.junit.jupiter.api.Test;

class SpacePacketHeaderTest {

  @Test
  void convertToPacketPrimaryHeaderBytes_shouldReturnSixBytes_spacePacketHeaderGiven() {
    SpacePacketHeader spacePacketHeader = new SpacePacketHeader(0, CommandType.TM, false, 100, SequenceFlags.UNSEGMENTED, 7, 4);
    byte[] bytes = spacePacketHeader.convertToPacketPrimaryHeaderBytes();
    assertEquals(6, bytes.length);
  }

  @Test
  void parsePacketPrimaryHeader_shouldPreserveAllFields_encodedSpacePacketHeaderGiven() {
    SpacePacketHeader spacePacketHeader = new SpacePacketHeader(0, CommandType.TC, true, 10, SequenceFlags.FIRST, 123, 0);
    byte[] bytes = spacePacketHeader.convertToPacketPrimaryHeaderBytes();
    SpacePacketHeader parsedSpacePacketHeader = SpacePacketHeader.parsePacketPrimaryHeader(bytes);

    assertEquals(0, parsedSpacePacketHeader.getPacketVersionNumber());
    assertEquals(CommandType.TC, parsedSpacePacketHeader.getPacketType());
    assertTrue(parsedSpacePacketHeader.isSecondaryHeaderFlag());
    assertEquals(10, parsedSpacePacketHeader.getApid());
    assertEquals(SequenceFlags.FIRST, parsedSpacePacketHeader.getSequenceFlags());
    assertEquals(123, parsedSpacePacketHeader.getPacketSequenceCount());
    assertEquals(0, parsedSpacePacketHeader.getPacketDataLength());
  }

  @Test
  void validateHeaderFields_shouldThrowIllegalArgumentException_invalidPacketVersionNumberGiven() {
    SpacePacketHeader spacePacketHeader = new SpacePacketHeader(1, CommandType.TM, false, 0, SequenceFlags.UNSEGMENTED, 0, 0);
    assertThrows(IllegalArgumentException.class, spacePacketHeader::validateHeaderFields);
  }

  @Test
  void validateHeaderFields_shouldThrowIllegalArgumentException_invalidApidGiven() {
    // 3000 > 2047
    SpacePacketHeader spacePacketHeader = new SpacePacketHeader(0, CommandType.TM, false, 3000, SequenceFlags.UNSEGMENTED, 0, 0);
    assertThrows(IllegalArgumentException.class, spacePacketHeader::validateHeaderFields);
  }

  @Test
  void validateHeaderFields_shouldThrowIllegalArgumentException_invalidPacketSequenceCountGiven() {
    // 20000 > 16383
    SpacePacketHeader spacePacketHeader = new SpacePacketHeader(0, CommandType.TM, false, 0, SequenceFlags.UNSEGMENTED, 20000, 0);
    assertThrows(IllegalArgumentException.class, spacePacketHeader::validateHeaderFields);
  }

  @Test
  void validateHeaderFields_shouldThrowIllegalArgumentException_invalidCGiven() {
    // 70000 > 65535
    SpacePacketHeader spacePacketHeader = new SpacePacketHeader(0, CommandType.TM, false, 0, SequenceFlags.UNSEGMENTED, 0, 70000);
    assertThrows(IllegalArgumentException.class, spacePacketHeader::validateHeaderFields);
  }

  @Test
  void getPacketDataFieldOctets_shouldReturnFive_spacePacketHeaderGiven() {
    // 4 + 1
    SpacePacketHeader spacePacketHeader = new SpacePacketHeader(0, CommandType.TM, false, 0, SequenceFlags.UNSEGMENTED, 0, 4);
    assertEquals(5, spacePacketHeader.getPacketDataFieldOctets());
  }

  @Test
  void convertToPacketPrimaryHeaderBytes_shouldReturnCorrectData_spacePacketHeaderGiven() {
    SpacePacketHeader spacePacketHeader = new SpacePacketHeader(0, CommandType.TM, false, 100, SequenceFlags.UNSEGMENTED, 7, 4);

    byte[] headerBytes = spacePacketHeader.convertToPacketPrimaryHeaderBytes();

    int highByte = Byte.toUnsignedInt(headerBytes[0]);
    int lowByte = Byte.toUnsignedInt(headerBytes[1]);
    int packetIdentificationWord = (highByte << 8) | lowByte;

    // Expected: (0<<13) | (0<<12) | (0<<11) | 100 = 100
    assertEquals(100, packetIdentificationWord);
    assertEquals(0, highByte);
    assertEquals(100, lowByte);

    SpacePacketHeader parsed = SpacePacketHeader.parsePacketPrimaryHeader(headerBytes);
    assertEquals(0, parsed.getPacketVersionNumber());
    assertEquals(CommandType.TM, parsed.getPacketType());
    assertFalse(parsed.isSecondaryHeaderFlag());
    assertEquals(100, parsed.getApid());
  }

  @Test
  void convertToPacketPrimaryHeaderBytes_shouldWriteCInBigEndian_spacePacketHeaderGiven() {
    SpacePacketHeader spacePacketHeader = new SpacePacketHeader(0, CommandType.TM, false, 0, SequenceFlags.UNSEGMENTED, 0, 4);

    byte[] bytes = spacePacketHeader.convertToPacketPrimaryHeaderBytes();

    int highByte = Byte.toUnsignedInt(bytes[4]);
    int lowByte = Byte.toUnsignedInt(bytes[5]);
    int c = (highByte << 8) | lowByte;

    assertEquals(4, c);

    // 4 in big-endian is [0, 4]
    assertEquals(0, highByte);
    assertEquals(4, lowByte);

    SpacePacketHeader parsed = SpacePacketHeader.parsePacketPrimaryHeader(bytes);
    assertEquals(4, parsed.getPacketDataLength());
  }
}