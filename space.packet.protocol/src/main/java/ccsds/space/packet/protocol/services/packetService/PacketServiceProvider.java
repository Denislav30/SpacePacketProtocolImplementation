package ccsds.space.packet.protocol.services.packetService;

import ccsds.space.packet.protocol.codec.PacketCodec;
import ccsds.space.packet.protocol.core.SpacePacket;
import ccsds.space.packet.protocol.services.octetStringService.OctetStringRequest;
import ccsds.space.packet.protocol.services.octetStringService.OctetStringService;

public class PacketServiceProvider implements PacketService{
  private final PacketCodec packetCodec;
  private final OctetStringService octetStringService;
  private PacketListener packetListener;

  public PacketServiceProvider(PacketCodec packetCodec, OctetStringService octetStringService) {
    this.packetCodec = packetCodec;
    this.octetStringService = octetStringService;

    this.octetStringService.setOctetStringListener(octetStringIndication -> {
      SpacePacket spacePacket = packetCodec.decodePacket(octetStringIndication.octetString());
      if (packetListener != null) {
        packetListener.indication(new PacketIndication(spacePacket, spacePacket.getHeader().getApid(), null));
      }
    });
  }

  @Override
  public void request(PacketRequest packetRequest) {
    SpacePacket spacePacket = packetRequest.spacePacket();
    byte[] bytes = packetCodec.encodePacket(spacePacket);

    octetStringService.request(new OctetStringRequest(
        bytes,
        packetRequest.apid(),
        spacePacket.getHeader().isSecondaryHeaderFlag(),
        spacePacket.getHeader().getPacketType(),
        spacePacket.getHeader().getPacketSequenceCount()
    ));
  }

  @Override
  public void setPacketListener(PacketListener packetListener) {
    this.packetListener = packetListener;
  }
}