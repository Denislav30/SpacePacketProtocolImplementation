package spp.core;

import spp.types.CommandType;
import spp.types.SequenceFlags;

/*
SpacePacket Header
  * Packet version number (3 bits)
  * Packet identification (13 bits)
    ** packet type (1 bit)
    ** secondary header flag (1 bit)
    ** application process identifier (11 bits)
  * Packet Sequence Control (16 bits)
    ** sequenceFlags (2 bits)
    ** packet sequence count / packet name (14 bits)
  * Packet data length (16 bits)

  Packet version number + Packet identification = 16 bits = 2 octets
  Packet sequence control = 16 bits = 2 octets
  Packet data length = 16 bits = 2 octets

  Bits 0 - 2   Packet version number (3)
  Bits 3 - 15  Packet identification (13) = Packet Type (1) + Secondary header flag (1) + APID (11)
  Bits 16 - 31 Packet sequence control (16) = SequenceFlags (2) + Packet Sequence Count / Packet name (14)
  Bits 32 - 47 Packet data length (16) = C = (Packet data field length in octets) - 1
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

  public byte[] convertToPacketPrimaryHeaderBytes() {
    validateHeaderFields();

    byte[] packetPrimaryHeader = new byte[6];

    int packetIdentification = buildPacketIdentification();
    int packetSequenceControl = buildPacketSequenceControl();
    int c = buildPacketDataLength();

    write16BitBigEndianFormat(packetPrimaryHeader, 0, packetIdentification);
    write16BitBigEndianFormat(packetPrimaryHeader, 2, packetSequenceControl);
    write16BitBigEndianFormat(packetPrimaryHeader, 4, c);

    return packetPrimaryHeader;
  }

//  public SpacePacketHeader parsePacketPrimaryHeader(byte[] packetPrimaryHeader) {
//    if (packetPrimaryHeader == null) {
//      throw new IllegalArgumentException("Packet primary header cannot be null!");
//    }
//    if (packetPrimaryHeader.length != 6) {
//      throw new IllegalArgumentException("Packet primary header length must be exactly 6 bytes!");
//    }
//
//    int packetIdentification = read16BitBigEndianFormat(packetPrimaryHeader, 0);
//    int packetSequenceControl = read16BitBigEndianFormat(packetPrimaryHeader, 2);
//    int c = read16BitBigEndianFormat(packetPrimaryHeader, 4);
//
//    // temporary header
//    SpacePacketHeader temporaryHeader = new SpacePacketHeader(0, CommandType.TM, false, 0, SequenceFlags.UNSEGMENTED, 0, 0);
//
//
//  }

  private int buildPacketIdentification() {
    // (3 bits) => 2^3 - 1 MAX
    int packetVersionNumber = this.packetVersionNumber & 7;

    // (1 bit) => 2^1 - 1 = 1
    int packetType = this.packetType.getValue() & 1;

    // (1 bit)
    int secondaryHeaderFlag = (this.secondaryHeaderFlag ? 1 : 0) & 1;

    // (11 bits) => 2^11 - 1 = 2047
    int apid = this.apid & 2047;

    // packetVersionNumber is before (packetType(1) + secondary header flag(1) + application process identifier(11)) => packetVersionNumber << 13
    // packetType is before (secondaryHeaderFlag(1) + application process identifier(11)) => packetType << 12
    // secondaryHeaderFlag is before (apid(11)) => secondaryHeaderFlag << 11
    // apid should not be moved
    return (packetVersionNumber << 13) | (packetType << 12) | (secondaryHeaderFlag << 11) | apid;
  }

  private int buildPacketSequenceControl() {
    // (2 bits) => 2^2 - 1 = 3
    int sequenceFlags = this.sequenceFlags.getValue() & 3;

    // (14 bits) => 2^14 - 1 = 16383
    int packetSequenceCountOrPacketName = this.packetSequenceCount & 16383;

    // sequence flags is before (packetSequenceCountOrPacketName(14)) => packetSequenceCountOrPacketName << 14
    return (sequenceFlags << 14) | packetSequenceCountOrPacketName;
  }

  private int buildPacketDataLength() {
    // C (16 bits) => 2^16 - 1 = 65535
    return this.packetDataLength & 65535;
  }

  // (highByte * 2^8 + lowByte) => [0, 100] => 0 * 2^8 + 100 = 100
  private int read16BitBigEndianFormat(byte[] bytes, int offSet) {
    int highByte = Byte.toUnsignedInt(bytes[offSet]);
    int lowByte = Byte.toUnsignedInt(bytes[offSet + 1]);
    return (highByte << 8) | lowByte;
  }

  // 255 in binary = 11111111
  // value >>> 8 => value / 2^8 => 2^8 = (256)
  private void write16BitBigEndianFormat(byte[] bytes, int offSet, int value) {
    bytes[offSet] = (byte) ((value >>> 8) & 255); // High byte
    bytes[offSet + 1] = (byte) (value & 255); // Low byte
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