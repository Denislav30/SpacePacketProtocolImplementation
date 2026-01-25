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

  private int versionNumber;

  // packet identification
  private int packetType;
  private boolean secondaryHeaderFlag;
  private int apid;

  // packet sequence control
  private int sequenceFlags;
  private int sequenceCount;

  // packet length
  private int packetLength;

  public SpacePacketHeader(int versionNumber, int packetType, boolean secondaryHeaderFlag,  int apid, int sequenceFlags, int sequenceCount, int packetLength) {
    this.versionNumber = versionNumber;
    this.packetType = packetType;
    this.secondaryHeaderFlag = secondaryHeaderFlag;
    this.apid = apid;
    this.sequenceFlags = sequenceFlags;
    this.sequenceCount = sequenceCount;
    this.packetLength = packetLength;
  }

  public void validateHeaderFields() {
    // Packet version number - Specified from CCSDS (should always be 000)
    if (versionNumber != 0) {
      throw new IllegalArgumentException("Packet version number must be 0!");
    }

    // Packet Type (0 - TM, 1 - TC)
    if (packetType != 0 &&  packetType != 1) {
      throw new IllegalArgumentException("Packet type must be 0 for (TM) or 1 (TC)!");
    }


  }

  public void setVersionNumber(int versionNumber) {
    this.versionNumber = versionNumber;
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

  public void setSequenceCount(int sequenceCount) {
    this.sequenceCount = sequenceCount;
  }

  public void setPacketLength(int packetLength) {
    this.packetLength = packetLength;
  }

  public int getVersionNumber() {
    return versionNumber;
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

  public int getSequenceCount() {
    return sequenceCount;
  }

  public int getPacketLength() {
    return packetLength;
  }
}