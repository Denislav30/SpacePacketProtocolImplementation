package ccsds.space.packet.protocol.core;

/*
SpacePacket
  * Packet Primary Header (SpacePacketHeader class)
  * Packet Data Field (Packet Secondary Header + User Data Field)
 */
public class SpacePacket {

  private SpacePacketHeader header;
  private byte[] packetDataField;

  public SpacePacket(SpacePacketHeader header, byte[] packetDataField) {
    this.header = header;
    this.packetDataField = packetDataField;
  }

  public void validateSpacePacket() {
    if (header == null) {
      throw new IllegalArgumentException("Header cannot be null!");
    }

    header.validateHeaderFields();

    if (packetDataField == null) {
      throw new IllegalArgumentException("Packet Data Field cannot be null!");
    }

    // Packet data field length (octets) must be C + 1
    int expected = header.getPacketDataFieldOctets();
    if (packetDataField.length != expected) {
      throw new IllegalArgumentException("Packet Data Field length must be exactly C + 1");
    }
  }

  public SpacePacketHeader getHeader() {
    return header;
  }

  public byte[] getPacketDataField() {
    return packetDataField;
  }

  public void setHeader(SpacePacketHeader header) {
    this.header = header;
  }

  public void setPacketDataField(byte[] packetDataField) {
    this.packetDataField = packetDataField;
  }
}