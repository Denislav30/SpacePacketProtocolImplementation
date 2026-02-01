package spp.services.octetStringService;

public interface OctetStringService {
  // OCTET_STRING.request
  void request(byte[] data);
  void setListener(OctetStringListener octetStringListener);
}
