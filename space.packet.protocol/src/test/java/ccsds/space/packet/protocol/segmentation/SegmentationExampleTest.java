package ccsds.space.packet.protocol.segmentation;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ccsds.space.packet.protocol.types.SequenceFlags;
import java.util.List;
import org.junit.jupiter.api.Test;

class SegmentationExampleTest {

  @Test
  void segmentExample() {
    byte[] userData = new byte[] {1,2,3,4,5,6,7,8,9,10};

    byte[] expectedFirst = new byte[] {1,2,3,4};
    byte[] expectedSecond = new byte[] {5,6,7,8};
    byte[] expectedThird = new byte[] {9,10};

    List<Segment> segments = SegmentationExample.segmentation(userData, 4, 100);

    assertEquals(3, segments.size());

    assertEquals(SequenceFlags.FIRST, segments.getFirst().sequenceFlags());
    assertEquals(100, segments.get(0).sequenceCount());
    assertArrayEquals(expectedFirst, segments.get(0).data());

    assertEquals(SequenceFlags.CONTINUATION, segments.get(1).sequenceFlags());
    assertEquals(101, segments.get(1).sequenceCount());
    assertArrayEquals(expectedSecond, segments.get(1).data());

    assertEquals(SequenceFlags.LAST, segments.get(2).sequenceFlags());
    assertEquals(102, segments.get(2).sequenceCount());
    assertArrayEquals(expectedThird, segments.get(2).data());
  }
}