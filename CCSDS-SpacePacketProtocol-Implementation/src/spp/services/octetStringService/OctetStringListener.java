package spp.services.octetStringService;

/*
  OctetStringListener class contains method indication which represents the primitive PACKET.indication
 */
@FunctionalInterface
public interface OctetStringListener {
  void indication(byte[] data);
}
