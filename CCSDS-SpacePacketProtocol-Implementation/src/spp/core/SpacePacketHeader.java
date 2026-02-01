package spp.core;

import spp.types.CommandType;
import spp.types.SequenceFlags;

/*
SpacePacket Header
  * Packet version number
  * Packet identification
    ** packet type
    ** secondary header flag
    ** application process identifier
  * Packet Sequence Control
    ** sequenceFlags
    ** packet sequence count / packet name
  * Packet data length
 */
public class SpacePacketHeader {

  // Packet Version Number
  private static final int CCSDS_VERSION_NUMBER_000 = 0;

  // APID (Idle Packet)
  private static final int APID_IDLE_PACKET_11111111111 = 2047;

  // Sequence Count
  private static final int PACKET_SEQUENCE_COUNT_MIN = 0;
  private static final int PACKET_SEQUENCE_COUNT_MAX = 16383;

  // Packet data length
  private static final int PACKET_DATA_LENGTH_MIN = 0;
  private static final int PACKET_DATA_LENGTH_MAX = 65535;

  private int packetVersionNumber;

  // packet identification
  private CommandType packetType;
  private boolean secondaryHeaderFlag;
  private int apid;

  // packet sequence control
  private SequenceFlags sequenceFlags;
  private int packetSequenceCount;

  // packet length
  private int packetDataLength;

  public SpacePacketHeader(int packetVersionNumber, CommandType packetType, boolean secondaryHeaderFlag,  int apid, SequenceFlags sequenceFlags, int packetSequenceCount, int packetDataLength) {
    this.packetVersionNumber = packetVersionNumber;
    this.packetType = packetType;
    this.secondaryHeaderFlag = secondaryHeaderFlag;
    this.apid = apid;
    this.sequenceFlags = sequenceFlags;
    this.packetSequenceCount = packetSequenceCount;
    this.packetDataLength = packetDataLength;
  }

  //TODO - Ask Hristo if i can use decimal format or it would be inappropriate
  public void validateHeaderFields() {
    // Packet version number - Specified from CCSDS (should always be 000)
    if (packetVersionNumber != CCSDS_VERSION_NUMBER_000) {
      throw new IllegalArgumentException("Packet version number must be 0!");
    }

    // Packet Type (0 - TM, 1 - TC)
    if (packetType == null) {
      throw new IllegalArgumentException("Packet type cannot be null!");
    }

    // Apid - 2047 (idle packet in decimal format - binary: 11111111111)
    if (apid < 0 || apid > APID_IDLE_PACKET_11111111111) {
      throw new IllegalArgumentException("APID must be between 0 and 2047!");
    }

    // Sequence flags: 00, 01, 10, 11
    if (sequenceFlags == null) {
      throw new IllegalArgumentException("Sequence flags cannot be null!");
    }

    // Sequence count
    if (packetSequenceCount < PACKET_SEQUENCE_COUNT_MIN || packetSequenceCount > PACKET_SEQUENCE_COUNT_MAX) {
      throw new IllegalArgumentException("Sequence count must be between 0 and 16383!");
    }

    // Packet Length
    if (packetDataLength < PACKET_DATA_LENGTH_MIN || packetDataLength > PACKET_DATA_LENGTH_MAX) {
      throw new IllegalArgumentException("Packet length must be between 0 and 65535!");
    }
  }

  public int getPacketDataFieldOctets() {
    return packetDataLength + 1;
  }

  public static int getPacketLengthFromOctets(int packetDataFieldOctets) {
    if (packetDataFieldOctets < 1) {
      throw new IllegalArgumentException("packetDataFieldOctets must be at least 1 octet!");
    }
    return  packetDataFieldOctets - 1;
  }

  public void setPacketVersionNumber(int packetVersionNumber) {
    this.packetVersionNumber = packetVersionNumber;
  }

  public void setPacketType(CommandType packetType) {
    this.packetType = packetType;
  }

  public void setSecondaryHeaderFlag(boolean secondaryHeaderFlag) {
    this.secondaryHeaderFlag = secondaryHeaderFlag;
  }

  public void setApid(int apid) {
    this.apid = apid;
  }

  public void setSequenceFlags(SequenceFlags sequenceFlags) {
    this.sequenceFlags = sequenceFlags;
  }

  public void setPacketSequenceCount(int packetSequenceCount) {
    this.packetSequenceCount = packetSequenceCount;
  }

  public void setPacketDataLength(int packetDataLength) {
    this.packetDataLength = packetDataLength;
  }

  public int getPacketVersionNumber() {
    return packetVersionNumber;
  }

  public CommandType getPacketType() {
    return packetType;
  }

  public boolean isSecondaryHeaderFlag() {
    return secondaryHeaderFlag;
  }

  public int getApid() {
    return apid;
  }

  public SequenceFlags getSequenceFlags() {
    return sequenceFlags;
  }

  public int getPacketSequenceCount() {
    return packetSequenceCount;
  }

  public int getPacketDataLength() {
    return packetDataLength;
  }
}