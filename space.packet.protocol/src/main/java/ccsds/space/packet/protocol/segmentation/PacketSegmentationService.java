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

  public List<SpacePacket> segmentPacket(SpacePacket originalPacket, int maxUserDataFieldPerSegment) {
    if (originalPacket == null) {
      throw new IllegalArgumentException("Original packet cannot be null!");
    }
    if (maxUserDataFieldPerSegment <= 0) {
      throw new IllegalArgumentException("MaxUserDataFieldPerSegment must be > 0!");
    }

    originalPacket.validateSpacePacket();

    SpacePacketHeader originalHeader = originalPacket.getHeader();
    byte[] packetSecondaryHeader = originalPacket.getPacketSecondaryHeader();
    byte[] fullUserDataField = originalPacket.getUserDataField();

    List<SpacePacket> result = new ArrayList<>();

    if (fullUserDataField.length <= maxUserDataFieldPerSegment) {
      SpacePacketHeader singleHeader = new SpacePacketHeader(
          originalHeader.getPacketVersionNumber(),
          originalHeader.getPacketType(),
          originalHeader.isSecondaryHeaderFlag(),
          originalHeader.getApid(),
          SequenceFlags.UNSEGMENTED,
          originalHeader.getSequenceFieldValue(),
          originalHeader.getSequenceFieldType(),
          SpacePacketHeader.getPacketLengthFromOctets(packetSecondaryHeader.length
              + fullUserDataField.length));

      result.add(new SpacePacket(singleHeader, packetSecondaryHeader, fullUserDataField));
      return result;
    }

    int offset = 0;
    int index = 0;
    int startSequenceCount = originalHeader.getSequenceFieldValue();

    while (offset < fullUserDataField.length) {
      int remaining = fullUserDataField.length - offset;
      int currentLength = Math.min(maxUserDataFieldPerSegment, remaining);

      byte[] chunk = Arrays.copyOfRange(fullUserDataField, offset, offset + currentLength);

      SequenceFlags sequenceFlags;
      if (offset == 0) {
        sequenceFlags = SequenceFlags.FIRST;
      } else if (offset + currentLength >= fullUserDataField.length) {
        sequenceFlags = SequenceFlags.LAST;
      } else {
        sequenceFlags = SequenceFlags.CONTINUATION;
      }

      int segmentSequenceCount = (startSequenceCount + index) & 16383;

      byte[] currentPacketSecondaryHeader;
      boolean currentSecondaryHeaderFlag;

      if (offset == 0) {
        currentPacketSecondaryHeader = packetSecondaryHeader;
        currentSecondaryHeaderFlag = originalHeader.isSecondaryHeaderFlag();
      } else {
        currentPacketSecondaryHeader = new byte[0];
        currentSecondaryHeaderFlag = false;
      }

      SpacePacketHeader segmentHeader = new SpacePacketHeader(originalHeader.getPacketVersionNumber(),
          originalHeader.getPacketType(), currentSecondaryHeaderFlag, originalHeader.getApid(),
          sequenceFlags, segmentSequenceCount, originalHeader.getSequenceFieldType(),
          SpacePacketHeader.getPacketLengthFromOctets(currentPacketSecondaryHeader.length
              + chunk.length));

      result.add(new SpacePacket(segmentHeader, currentPacketSecondaryHeader, chunk));
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
    int expectedSequenceValue = firstHeader.getSequenceFieldValue();

    byte[] packetSecondaryHeader = firstPacket.getPacketSecondaryHeader();
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
      byteArrayOutputStream.writeBytes(currentPacket.getUserDataField());
      expectedSequenceValue = (expectedSequenceValue + 1) & 16383;
    }

    byte[] fullUserDataField = byteArrayOutputStream.toByteArray();

    SpacePacketHeader reassembledHeader = new SpacePacketHeader(packetVersionNumber, packetType,
        packetSecondaryHeader.length > 0, apid, SequenceFlags.UNSEGMENTED,
        firstHeader.getSequenceFieldValue(), sequenceFieldType,
        SpacePacketHeader.getPacketLengthFromOctets(packetSecondaryHeader.length
            + fullUserDataField.length));

    return new SpacePacket(reassembledHeader, packetSecondaryHeader, fullUserDataField);
  }
}