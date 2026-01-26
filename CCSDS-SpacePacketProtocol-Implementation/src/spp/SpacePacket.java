package spp;

public class SpacePacket {

  private SpacePacketHeader header;
  private byte[] packetDataField;

  public SpacePacket(SpacePacketHeader header, byte[] packetDataField) {
    this.header = header;
    this.packetDataField = packetDataField;
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