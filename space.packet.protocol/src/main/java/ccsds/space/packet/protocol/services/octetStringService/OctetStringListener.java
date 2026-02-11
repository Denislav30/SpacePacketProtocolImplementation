package ccsds.space.packet.protocol.services.octetStringService;

/*
  OctetStringListener class contains method indication which represents the primitive PACKET.indication
 */
@FunctionalInterface
public interface OctetStringListener {
  void indication(OctetStringIndication octetStringIndication);
}
