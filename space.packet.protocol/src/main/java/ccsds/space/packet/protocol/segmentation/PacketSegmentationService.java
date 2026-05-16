package ccsds.space.packet.protocol.segmentation;

import ccsds.space.packet.protocol.core.SpacePacket;
import ccsds.space.packet.protocol.core.SpacePacketHeader;
import ccsds.space.packet.protocol.types.CommandType;
import ccsds.space.packet.protocol.types.SequenceFieldType;
import ccsds.space.packet.protocol.types.SequenceFlags;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PacketSegmentationService {

  public List<SpacePacket> segmentPacket(SpacePacket originalPacket, int maxPacketDataFieldPerSegment) {
    if (originalPacket == null) {
      throw new IllegalArgumentException("Original packet cannot be null!");
    }
    if (maxPacketDataFieldPerSegment <= 0) {
      throw new IllegalArgumentException("MaxPacketDataFieldPerSegment must be > 0!");
    }

    originalPacket.validateSpacePacket();

    SpacePacketHeader originalHeader = originalPacket.getHeader();
    byte[] fullPacketDataField = originalPacket.getPacketDataField();

    List<SpacePacket> result = new ArrayList<>();

    if (fullPacketDataField.length <= maxPacketDataFieldPerSegment) {
      SpacePacketHeader singleHeader = new SpacePacketHeader(
          originalHeader.getPacketVersionNumber(),
          originalHeader.getPacketType(),
          originalHeader.isSecondaryHeaderFlag(),
          originalHeader.getApid(),
          SequenceFlags.UNSEGMENTED,
          originalHeader.getSequenceFieldValue(),
          originalHeader.getSequenceFieldType(),
          SpacePacketHeader.getPacketLengthFromOctets(fullPacketDataField.length)
      );

      result.add(new SpacePacket(singleHeader, fullPacketDataField));
      return result;
    }

    int offset = 0;
    int index = 0;
    int startSequenceCount = originalHeader.getSequenceFieldValue();

    while (offset < fullPacketDataField.length) {
      int remaining = fullPacketDataField.length - offset;
      int currentLength = Math.min(maxPacketDataFieldPerSegment, remaining);

      byte[] chunk = Arrays.copyOfRange(fullPacketDataField, offset, offset + currentLength);

      SequenceFlags sequenceFlags;
      if (offset == 0) {
        sequenceFlags = SequenceFlags.FIRST;
      } else if (offset + currentLength >= fullPacketDataField.length) {
        sequenceFlags = SequenceFlags.LAST;
      } else {
        sequenceFlags = SequenceFlags.CONTINUATION;
      }

      int segmentSequenceCount = (startSequenceCount + index) & 16383;

      SpacePacketHeader segmentHeader = new SpacePacketHeader(
          originalHeader.getPacketVersionNumber(),
          originalHeader.getPacketType(),
          originalHeader.isSecondaryHeaderFlag(),
          originalHeader.getApid(),
          sequenceFlags,
          segmentSequenceCount,
          originalHeader.getSequenceFieldType(),
          SpacePacketHeader.getPacketLengthFromOctets(chunk.length)
      );

      result.add(new SpacePacket(segmentHeader, chunk));
      offset += currentLength;
      index++;
    }

    return result;
  }

  public SpacePacket desegmentPacket(List<SpacePacket> segments) {
    if (segments == null || segments.isEmpty()) {
      throw new IllegalArgumentException("Segments cannot be null or empty!");
    }

    if (segments.size() == 1) {
      SpacePacket onlyPacket = segments.getFirst();
      onlyPacket.validateSpacePacket();

      if (onlyPacket.getHeader().getSequenceFlags() != SequenceFlags.UNSEGMENTED) {
        throw new IllegalArgumentException("Single segment must use UNSEGMENTED flag!");
      }

      return onlyPacket;
    }

    SpacePacket firstPacket = segments.getFirst();
    SpacePacket lastPacket = segments.getLast();

    firstPacket.validateSpacePacket();
    lastPacket.validateSpacePacket();

    if (firstPacket.getHeader().getSequenceFlags() != SequenceFlags.FIRST) {
      throw new IllegalArgumentException("First segment must have FIRST flag!");
    }

    if (lastPacket.getHeader().getSequenceFlags() != SequenceFlags.LAST) {
      throw new IllegalArgumentException("Last segment must have LAST flag!");
    }

    SpacePacketHeader firstHeader = firstPacket.getHeader();
    int apid = firstHeader.getApid();
    int packetVersionNumber = firstHeader.getPacketVersionNumber();
    CommandType packetType = firstHeader.getPacketType();
    SequenceFieldType sequenceFieldType = firstHeader.getSequenceFieldType();
    boolean secondaryHeaderFlag = firstHeader.isSecondaryHeaderFlag();
    int expectedSequenceValue = firstHeader.getSequenceFieldValue();

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    for (int i = 0; i < segments.size(); i++) {
      SpacePacket currentPacket = segments.get(i);
      currentPacket.validateSpacePacket();

      SpacePacketHeader currentHeader = currentPacket.getHeader();

      if (currentHeader.getApid() != apid) {
        throw new IllegalArgumentException("All segments must have the same APID!");
      }

      if (currentHeader.getPacketVersionNumber() != packetVersionNumber) {
        throw new IllegalArgumentException("All segments must have the same packetVersionNumber!");
      }

      if (currentHeader.getPacketType() != packetType) {
        throw new IllegalArgumentException("All segments must have the same packetType!");
      }

      if (currentHeader.getSequenceFieldType() != sequenceFieldType) {
        throw new IllegalArgumentException("All segments must have the same sequenceFieldType!");
      }

      if (currentHeader.getSequenceFieldValue() != expectedSequenceValue) {
        throw new IllegalArgumentException("Segments are out of order or missing! Expected sequence value: "
                + expectedSequenceValue + ", actual: " + currentHeader.getSequenceFieldValue());
      }

      if (i == 0) {
        if (currentHeader.getSequenceFlags() != SequenceFlags.FIRST) {
          throw new IllegalArgumentException("First segment must have FIRST flag!");
        }
      } else if (i == segments.size() - 1) {
        if (currentHeader.getSequenceFlags() != SequenceFlags.LAST) {
          throw new IllegalArgumentException("Last segment must have LAST flag!");
        }
      } else {
        if (currentHeader.getSequenceFlags() != SequenceFlags.CONTINUATION) {
          throw new IllegalArgumentException("Middle segments must have CONTINUATION flag!");
        }
      }
      byteArrayOutputStream.writeBytes(currentPacket.getPacketDataField());
      expectedSequenceValue = (expectedSequenceValue + 1) & 16383;
    }

    byte[] fullPacketDataField = byteArrayOutputStream.toByteArray();

    SpacePacketHeader reassembledHeader = new SpacePacketHeader(packetVersionNumber, packetType, secondaryHeaderFlag,
        apid, SequenceFlags.UNSEGMENTED, firstHeader.getSequenceFieldValue(), sequenceFieldType,
        SpacePacketHeader.getPacketLengthFromOctets(fullPacketDataField.length));

    return new SpacePacket(reassembledHeader, fullPacketDataField);
  }
}