package spp;

/*
Packet Primary Header

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

  // Packet Type
  private static final int TYPE_TM = 0;
  private static final int TYPE_TC = 1;

  // APID (Idle Packet)
  private static final int APID_IDLE_PACKET_11111111111 = 2047;

  // Sequence Flags
  private static final int CONTINUATION_SEGMENT_FLAG = 0;
  private static final int FIRST_SEGMENT_FLAG = 1;
  private static final int LAST_SEGMENT_FLAG = 2;
  private static final int UNSEGMENTED_FLAG = 3;

  // Sequence Count
  private static final int PACKET_SEQUENCE_COUNT_MIN = 0;
  private static final int PACKET_SEQUENCE_COUNT_MAX = 16383;

  // Packet data length
  private static final int PACKET_DATA_LENGTH_MIN = 0;
  private static final int PACKET_DATA_LENGTH_MAX = 65535;

  private int packetVersionNumber;

  // packet identification
  private int packetType;
  private boolean secondaryHeaderFlag;
  private int apid;

  // packet sequence control
  private int sequenceFlags;
  private int packetSequenceCount;

  // packet length
  private int packetDataLength;

  public SpacePacketHeader(int packetVersionNumber, int packetType, boolean secondaryHeaderFlag,  int apid, int sequenceFlags, int packetSequenceCount, int packetDataLength) {
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
    if (packetType != TYPE_TM &&  packetType != TYPE_TC) {
      throw new IllegalArgumentException("Packet type must be 0 for (TM) or 1 (TC)!");
    }

    // Apid - 2047 (idle packet in decimal format - binary: 11111111111)
    if (apid < 0 || apid > APID_IDLE_PACKET_11111111111) {
      throw new IllegalArgumentException("APID must be between 0 and 2047!");
    }

    // Sequence flags: 00, 01, 10, 11
    if (sequenceFlags < CONTINUATION_SEGMENT_FLAG || sequenceFlags > UNSEGMENTED_FLAG) {
      throw new IllegalArgumentException("Sequence flags must be between 0 and 3!");
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

  public void setPacketType(int packetType) {
    this.packetType = packetType;
  }

  public void setSecondaryHeaderFlag(boolean secondaryHeaderFlag) {
    this.secondaryHeaderFlag = secondaryHeaderFlag;
  }

  public void setApid(int apid) {
    this.apid = apid;
  }

  public void setSequenceFlags(int sequenceFlags) {
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

  public int getPacketType() {
    return packetType;
  }

  public boolean isSecondaryHeaderFlag() {
    return secondaryHeaderFlag;
  }

  public int getApid() {
    return apid;
  }

  public int getSequenceFlags() {
    return sequenceFlags;
  }

  public int getPacketSequenceCount() {
    return packetSequenceCount;
  }

  public int getPacketDataLength() {
    return packetDataLength;
  }
}