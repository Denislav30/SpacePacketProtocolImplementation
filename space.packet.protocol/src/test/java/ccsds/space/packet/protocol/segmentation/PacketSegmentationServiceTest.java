package ccsds.space.packet.protocol.segmentation;

import static org.junit.jupiter.api.Assertions.*;

import ccsds.space.packet.protocol.core.SpacePacket;
import ccsds.space.packet.protocol.core.SpacePacketHeader;
import ccsds.space.packet.protocol.types.CommandType;
import ccsds.space.packet.protocol.types.SequenceFieldType;
import ccsds.space.packet.protocol.types.SequenceFlags;
import java.util.List;
import org.junit.jupiter.api.Test;

class PacketSegmentationServiceTest {

  private final PacketSegmentationService packetSegmentationService;

  public PacketSegmentationServiceTest() {
    this.packetSegmentationService = new PacketSegmentationService();
  }

  @Test
  void shouldSegmentPacketDataFieldIntoThreePackets() {
    byte[] packetSecondaryHeader = new byte[] {11, 12};
    byte[] userDataField = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    byte[] expectedFirstUserDataChunk = new byte[] {1, 2, 3, 4};
    byte[] expectedSecondUserDataChunk = new byte[] {5, 6, 7, 8};
    byte[] expectedThirdUserDataChunk = new byte[] {9, 10};
    byte[] expectedEmptySecondaryHeader = new byte[0];

    SpacePacketHeader header = new SpacePacketHeader(0, CommandType.TC, true,
        100, SequenceFlags.UNSEGMENTED, 7, SequenceFieldType.PACKET_SEQUENCE_COUNT,
        SpacePacketHeader.getPacketLengthFromOctets(packetSecondaryHeader.length
            + userDataField.length));

    SpacePacket originalPacket = new SpacePacket(header, packetSecondaryHeader, userDataField);
    List<SpacePacket> segments = packetSegmentationService.segmentPacket(originalPacket, 4);
    assertEquals(3, segments.size());

    // First segment
    assertEquals(SequenceFlags.FIRST, segments.getFirst().getHeader().getSequenceFlags());
    assertEquals(7, segments.getFirst().getHeader().getSequenceFieldValue());
    assertArrayEquals(packetSecondaryHeader, segments.get(0).getPacketSecondaryHeader());
    assertArrayEquals(expectedFirstUserDataChunk, segments.get(0).getUserDataField());

    // Second segment
    assertEquals(SequenceFlags.CONTINUATION, segments.get(1).getHeader().getSequenceFlags());
    assertEquals(8, segments.get(1).getHeader().getSequenceFieldValue());
    assertArrayEquals(expectedEmptySecondaryHeader, segments.get(1).getPacketSecondaryHeader());
    assertArrayEquals(expectedSecondUserDataChunk, segments.get(1).getUserDataField());

    // Third segment
    assertEquals(SequenceFlags.LAST, segments.get(2).getHeader().getSequenceFlags());
    assertEquals(9, segments.get(2).getHeader().getSequenceFieldValue());
    assertArrayEquals(expectedEmptySecondaryHeader, segments.get(2).getPacketSecondaryHeader());
    assertArrayEquals(expectedThirdUserDataChunk, segments.get(2).getUserDataField());
  }

  @Test
  void shouldDesegmentThreePacketsIntoOne() {
    byte[] packetSecondaryHeader = new byte[] {11, 12};
    byte[] userDataField = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    SpacePacketHeader header = new SpacePacketHeader(0, CommandType.TC, true,
        100, SequenceFlags.UNSEGMENTED, 7, SequenceFieldType.PACKET_SEQUENCE_COUNT,
        SpacePacketHeader.getPacketLengthFromOctets(packetSecondaryHeader.length
            + userDataField.length));

    SpacePacket originalPacket = new SpacePacket(header, packetSecondaryHeader, userDataField);

    List<SpacePacket> segments = packetSegmentationService.segmentPacket(originalPacket, 4);
    SpacePacket reassembledPacket = packetSegmentationService.desegmentPacket(segments);

    assertEquals(SequenceFlags.UNSEGMENTED, reassembledPacket.getHeader().getSequenceFlags());
    assertEquals(7, reassembledPacket.getHeader().getSequenceFieldValue());
    assertArrayEquals(packetSecondaryHeader, reassembledPacket.getPacketSecondaryHeader());
    assertArrayEquals(userDataField, reassembledPacket.getUserDataField());
  }
}