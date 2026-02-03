# CCSDS Space Packet Protocol – Java Implementation

## Package / Folder Structure (Detailed Explanation)

### `spp.core`
This package contains the **core data model** of the Space Packet:
- **`SpacePacketHeader`** – represents the primary header fields (packet version number, packet type, secondary header flag, APID, sequence flags, packet sequence count / packet name, packet data length). It contains validation logic for field ranges.
- **`SpacePacket`** – represents a full space packet:
    - `SpacePacketHeader`
    - `byte[] packetDataField` (secondary header + user data)

---

### `spp.demo`
- **`SppImplementation`** – the main class used to demonstrate space packet operations:
    - creating a `SpacePacket` from header + data
    - encoding it to `byte[]`
    - decoding it back to a `SpacePacket`
    - printing/logging all decoded fields to verify correctness