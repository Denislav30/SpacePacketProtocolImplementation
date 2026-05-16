package ccsds.space.packet.protocol.core;

import ccsds.space.packet.protocol.types.CommandType;
import ccsds.space.packet.protocol.types.SequenceFieldType;
import ccsds.space.packet.protocol.types.SequenceFlags;

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
  private static final int SEQUENCE_FIELD_VALUE_MIN = 0;
  private static final int SEQUENCE_FIELD_VALUE_MAX = 16383;

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
  private int sequenceFieldValue;
  private SequenceFieldType sequenceFieldType;

  // packet length
  private int packetDataLength;

  public SpacePacketHeader(int packetVersionNumber, CommandType packetType, boolean secondaryHeaderFlag, int apid,
      SequenceFlags sequenceFlags, int sequenceFieldValue, SequenceFieldType sequenceFieldType, int packetDataLength) {
    this.packetVersionNumber = packetVersionNumber;
    this.packetType = packetType;
    this.secondaryHeaderFlag = secondaryHeaderFlag;
    this.apid = apid;
    this.sequenceFlags = sequenceFlags;
    this.sequenceFieldValue = sequenceFieldValue;
    this.sequenceFieldType = sequenceFieldType;
    this.packetDataLength = packetDataLength;
  }

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
    if (sequenceFieldValue < SEQUENCE_FIELD_VALUE_MIN || sequenceFieldValue > SEQUENCE_FIELD_VALUE_MAX) {
      throw new IllegalArgumentException("Sequence field value must be between 0 and 16383!");
    }

    if (sequenceFieldType == null) {
      throw new IllegalArgumentException("Sequence field type cannot be null!");
    }

    if (packetType == CommandType.TM && sequenceFieldType != SequenceFieldType.PACKET_SEQUENCE_COUNT) {
      throw new IllegalArgumentException("Telemetry packet must use PACKET_SEQUENCE_COUNT!");
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
    return packetDataFieldOctets - 1;
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

  public static SpacePacketHeader parsePacketPrimaryHeader(byte[] packetPrimaryHeader) {
    return parsePacketPrimaryHeader(packetPrimaryHeader, SequenceFieldType.PACKET_SEQUENCE_COUNT);
  }

  public static SpacePacketHeader parsePacketPrimaryHeader(byte[] packetPrimaryHeader,
      SequenceFieldType tcSequenceFieldType) {
    if (packetPrimaryHeader == null) {
      throw new IllegalArgumentException("Packet primary header cannot be null!");
    }
    if (packetPrimaryHeader.length != 6) {
      throw new IllegalArgumentException("Packet primary header length must be exactly 6 bytes!");
    }

    if (tcSequenceFieldType == null) {
      throw new IllegalArgumentException("tcSequenceFieldType cannot be null!");
    }

    int packetIdentification = read16BitBigEndianFormat(packetPrimaryHeader, 0);
    int packetSequenceControl = read16BitBigEndianFormat(packetPrimaryHeader, 2);
    int c = read16BitBigEndianFormat(packetPrimaryHeader, 4);

    // temporary header
    SpacePacketHeader temporaryHeader = new SpacePacketHeader(0, CommandType.TM,
        false, 0, SequenceFlags.UNSEGMENTED, 0,
        SequenceFieldType.PACKET_SEQUENCE_COUNT, 0);

    temporaryHeader.parsePacketIdentification(packetIdentification);
    temporaryHeader.parsePacketSequenceControl(packetSequenceControl);
    temporaryHeader.parsePacketDataLength(c);

    if (temporaryHeader.packetType == CommandType.TM) {
      temporaryHeader.sequenceFieldType = SequenceFieldType.PACKET_SEQUENCE_COUNT;
    } else {
      temporaryHeader.sequenceFieldType = tcSequenceFieldType;
    }

    temporaryHeader.validateHeaderFields();
    return temporaryHeader;
  }

  private int buildPacketIdentification() {
    // (3 bits) => 2^3 - 1 MAX
    int packetVersionNumber = this.packetVersionNumber & 7;

    // (1 bit) => 2^1 - 1 = 1
    int packetType = this.packetType.getValue() & 1;

    // (1 bit)
    int secondaryHeaderFlag = (this.secondaryHeaderFlag ? 1 : 0) & 1;

    // (11 bits) => 2^11 - 1 = 2047
    int apid = this.apid & 2047;

    // packetVersionNumber is before (packetType(1) + secondary header flag(1) + application process identifier(11))
    // => packetVersionNumber << 13
    // packetType is before (secondaryHeaderFlag(1) + application process identifier(11)) => packetType << 12
    // secondaryHeaderFlag is before (apid(11)) => secondaryHeaderFlag << 11
    // apid should not be moved
    return (packetVersionNumber << 13) | (packetType << 12) | (secondaryHeaderFlag << 11) | apid;
  }

  private void parsePacketIdentification(int word) {
    // packet version number is top 3 bits => shift down by 13
    // (3 bits) => 2^3 - 1 = 7
    this.packetVersionNumber = (word >>> 13) & 7;

    // packet type is at position 12 => shift down by 12
    // (1 bit) => (2^1 - 1) = 1
    int packetTypeBit = (word >>> 12) & 1;
    this.packetType = (packetTypeBit == 0) ? CommandType.TM : CommandType.TC;

    // secondary header flag is at position 11 => shift down by 11
    // (1 bit) => (2^1 - 1) = 1
    int secondaryHeaderFlagBit = (word >>> 11) & 1;
    this.secondaryHeaderFlag = (secondaryHeaderFlagBit == 1);

    // APID is the lower 11 bits => no shift needed
    // (11 bits) => 2^11 - 1 = 2047
    this.apid = word & 2047;
  }

  private int buildPacketSequenceControl() {
    // (2 bits) => 2^2 - 1 = 3
    int sequenceFlags = this.sequenceFlags.getValue() & 3;

    // (14 bits) => 2^14 - 1 = 16383
    int sequenceFieldValue = this.sequenceFieldValue & 16383;

    // sequenceFlags occupy the upper 2 bits -> they are shifted left by 14 positions
    // sequenceFieldValue occupies the lower 14 bits
    return (sequenceFlags << 14) | sequenceFieldValue;
  }

  private void parsePacketSequenceControl(int word) {
    // sequence flags are top 2 bits => shift down by 14
    // (2 bits) => 2^2 - 1 = 3
    int sequenceFlagsBits = (word >>> 14) & 3;
    this.sequenceFlags = SequenceFlags.fromValue(sequenceFlagsBits);

    // packet sequence count is lower 14 bits => no shift needed
    // (14 bits) => 2^14 - 1 = 16383
    this.sequenceFieldValue = word & 16383;
  }

  private int buildPacketDataLength() {
    // C (16 bits) => 2^16 - 1 = 65535
    return this.packetDataLength & 65535;
  }

  private void parsePacketDataLength(int word) {
    // packet data length => no shift needed
    // (16 bits) => 2^16 - 1 = 65535
    this.packetDataLength = word & 65535;
  }

  // (highByte * 2^8 + lowByte) => [0, 100] => 0 * 2^8 + 100 = 100
  private static int read16BitBigEndianFormat(byte[] bytes, int offSet) {
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

  public void setSequenceFieldValue(int sequenceFieldValue) {
    this.sequenceFieldValue = sequenceFieldValue;
  }

  public void setSequenceFieldType(SequenceFieldType sequenceFieldType) {
    this.sequenceFieldType = sequenceFieldType;
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

  public int getSequenceFieldValue() {
    return sequenceFieldValue;
  }

  public SequenceFieldType getSequenceFieldType() {
    return sequenceFieldType;
  }

  public int getPacketDataLength() {
    return packetDataLength;
  }
}