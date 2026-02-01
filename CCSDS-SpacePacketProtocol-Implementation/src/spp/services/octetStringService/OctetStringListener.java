package spp.services.octetStringService;

@FunctionalInterface
public interface OctetStringListener {
  // OCTET_STRING.indication
  void indication(byte[] data);
}
