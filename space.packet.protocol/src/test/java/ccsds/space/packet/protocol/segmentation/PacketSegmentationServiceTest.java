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
    byte[] packetDataField = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    SpacePacketHeader header = new SpacePacketHeader(0, CommandType.TC, false,
        100, SequenceFlags.UNSEGMENTED, 7, SequenceFieldType.PACKET_SEQUENCE_COUNT,
        SpacePacketHeader.getPacketLengthFromOctets(packetDataField.length));

    SpacePacket originalPacket = new SpacePacket(header, packetDataField);
    List<SpacePacket> segments = packetSegmentationService.segmentPacket(originalPacket, 4);

    byte[] expectedFirstChunk = new byte[] {1, 2, 3, 4};
    byte[] expectedSecondChunk = new byte[] {5, 6, 7, 8};
    byte[] expectedThirdChunk = new byte[] {9, 10};

    assertEquals(3, segments.size());

    // First segment
    assertEquals(SequenceFlags.FIRST, segments.getFirst().getHeader().getSequenceFlags());
    assertEquals(7, segments.get(0).getHeader().getSequenceFieldValue());
    assertArrayEquals(expectedFirstChunk, segments.get(0).getPacketDataField());

    // Second segment
    assertEquals(SequenceFlags.CONTINUATION, segments.get(1).getHeader().getSequenceFlags());
    assertEquals(8, segments.get(1).getHeader().getSequenceFieldValue());
    assertArrayEquals(expectedSecondChunk, segments.get(1).getPacketDataField());

    // Third segment
    assertEquals(SequenceFlags.LAST, segments.get(2).getHeader().getSequenceFlags());
    assertEquals(9, segments.get(2).getHeader().getSequenceFieldValue());
    assertArrayEquals(expectedThirdChunk, segments.get(2).getPacketDataField());
  }

  @Test
  void shouldDesegmentThreePacketsIntoOne() {
    byte[] packetDataField = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    SpacePacketHeader header = new SpacePacketHeader(0, CommandType.TC, false,
        100, SequenceFlags.UNSEGMENTED, 7, SequenceFieldType.PACKET_SEQUENCE_COUNT,
        SpacePacketHeader.getPacketLengthFromOctets(packetDataField.length));

    SpacePacket originalPacket = new SpacePacket(header, packetDataField);

    List<SpacePacket> segments = packetSegmentationService.segmentPacket(originalPacket, 4);
    SpacePacket reassembledPacket = packetSegmentationService.desegmentPacket(segments);

    assertEquals(SequenceFlags.UNSEGMENTED, reassembledPacket.getHeader().getSequenceFlags());
    assertEquals(7, reassembledPacket.getHeader().getSequenceFieldValue());
    assertArrayEquals(packetDataField, reassembledPacket.getPacketDataField());
  }
}