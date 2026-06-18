package ccsds.space.packet.protocol.segmentation;

import ccsds.space.packet.protocol.types.SequenceFlags;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SegmentationExample {

  public static List<Segment> segmentation(byte[] userData, int chunkSize, int startSequenceCount) {
    if (userData == null || userData.length == 0) {
      throw new IllegalArgumentException("UserData cannot be null or empty!");
    }
    if (chunkSize <= 0) {
      throw new IllegalArgumentException("Chunk size must be positive!");
    }

    List<Segment> result = new ArrayList<>();

    // 2^14 - 1 = 16383
    if (userData.length <= chunkSize) {
      result.add(new Segment(SequenceFlags.UNSEGMENTED, startSequenceCount & 16383, userData));
      return result;
    }

    int offset = 0;
    int index = 0;

    while(offset < userData.length) {
     int remaining = userData.length - offset;
     int currentLength = Math.min(chunkSize, remaining);

     byte[] chunk = Arrays.copyOfRange(userData, offset, offset + currentLength);

     SequenceFlags sequenceFlags;
     if (offset == 0) {
       sequenceFlags = SequenceFlags.FIRST;
     } else if (offset + currentLength >= userData.length) {
       sequenceFlags = SequenceFlags.LAST;
     } else {
       sequenceFlags = SequenceFlags.CONTINUATION;
     }

     int segmentSequenceCount = (startSequenceCount + index) & 16383;

     result.add(new Segment(sequenceFlags, segmentSequenceCount, chunk));
     offset += currentLength;
     index++;
    }

    return result;
  }
}