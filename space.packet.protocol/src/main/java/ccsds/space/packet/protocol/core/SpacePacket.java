package ccsds.space.packet.protocol.core;

/*
SpacePacket
  * Packet Primary Header (SpacePacketHeader class)
  * Packet Data Field (Packet Secondary Header + User Data Field)
 */
public class SpacePacket {

  private SpacePacketHeader header;
  private byte[] packetSecondaryHeader;
  private byte[] userDataField;

  public SpacePacket(SpacePacketHeader header, byte[] packetSecondaryHeader, byte[] userDataField) {
    this.header = header;
    this.packetSecondaryHeader = packetSecondaryHeader;
    this.userDataField = userDataField;
  }

  public void validateSpacePacket() {
    if (header == null) {
      throw new IllegalArgumentException("Header cannot be null!");
    }

    header.validateHeaderFields();

    if (packetSecondaryHeader == null) {
      throw new IllegalArgumentException("Packet secondary header cannot be null!");
    }

    if (userDataField == null) {
      throw new IllegalArgumentException("User data field cannot be null!");
    }

    if (header.isSecondaryHeaderFlag() && packetSecondaryHeader.length == 0) {
      throw new IllegalArgumentException("Secondary header flag is true, but packet secondary header is empty!");
    }

    if (!header.isSecondaryHeaderFlag() && packetSecondaryHeader.length > 0) {
      throw new IllegalArgumentException("Secondary header flag is false, but packet secondary header is present!");
    }

    if (!header.isSecondaryHeaderFlag() && userDataField.length == 0) {
      throw new IllegalArgumentException("User data field is mandatory when secondary header is not present!");
    }

    if (getPacketDataField().length != header.getPacketDataFieldOctets()) {
      throw new IllegalArgumentException("Packet Data Field length must match packetDataLength + 1!");
    }
  }

  public byte[] getPacketDataField() {
    byte[] result = new byte[packetSecondaryHeader.length + userDataField.length];
    System.arraycopy(packetSecondaryHeader, 0, result, 0, packetSecondaryHeader.length);
    System.arraycopy(userDataField, 0, result, packetSecondaryHeader.length, userDataField.length);
    return result;
  }

  public SpacePacketHeader getHeader() {
    return header;
  }

  public byte[] getPacketSecondaryHeader() {
    return packetSecondaryHeader;
  }

  public byte[] getUserDataField() {
    return userDataField;
  }

  public void setHeader(SpacePacketHeader header) {
    this.header = header;
  }

  public void setPacketSecondaryHeader(byte[] packetSecondaryHeader) {
    this.packetSecondaryHeader = packetSecondaryHeader;
  }

  public void setUserDataField(byte[] userDataField) {
    this.userDataField = userDataField;
  }
}