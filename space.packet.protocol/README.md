# CCSDS Space Packet Protocol – Java Implementation

## Package / Folder Structure (Detailed Explanation)

### `ccsds.space.packet.protocol.codec`
This package contains the **encoding/decoding logic** for the SpacePacket format:
- **`PacketCodec`** – converts between:
  - `SpacePacket` → `byte[]` (encoding)
  - `byte[]` → `SpacePacket` (decoding)

---

### `ccsds.space.packet.protocol.core`
This package contains the **core data model** of the Space Packet:
- **`SpacePacketHeader`** – represents the primary header fields (packet version number, packet type, secondary header flag, APID, sequence flags, packet sequence count / packet name, packet data length). It contains validation logic for field ranges.
- **`SpacePacket`** – represents a full space packet:
    - `SpacePacketHeader`
    - `byte[] packetDataField` (secondary header + user data)

---

### `ccsds.space.packet.protocol.demo`
- **`SppImplementation`** – the main class used to demonstrate space packet operations:
    - creating a `SpacePacket` from header + data
    - encoding it to `byte[]`
    - decoding it back to a `SpacePacket`
    - printing/logging all decoded fields to verify correctness

---

### `ccsds.space.packet.protocol.services`
This package contains implementation for CCSDS SPP service primitives.
It demonstrates the request/indication flow described in the protocol.

#### `ccsds.space.packet.protocol.services.octetStringService`
Used to transfer raw octet strings over a channel:
- **`OctetStringService`** – contains:
  - `OCTET_STRING.request`
  - `setOctetStringListener` for receiving `OCTET_STRING.indication`
- **`OctetStringListener`** – interface for `OCTET_STRING.indication`
- **`OctetStringRequest` / `OctetStringIndication`** – record classes that represent the primitive parameters
- **`OctetStringChannel`** – channel used as simple simulated transport layer

#### `ccsds.space.packet.protocol.services.packetService`
Used to transfer already-built `SpacePacket` objects:
- **`PacketService`** – contains:
  - `PACKET.request`
  - `setPacketListener` for receiving `PACKET.indication`
- **`PacketListener`** – interface for `PACKET.indication`
- **`PacketRequest` / `PacketIndication`** – record classes that represent the primitive parameters
- **`PacketEncoderService` / `PacketDecoderService`** – interfaces which define encoding/decoding operations
- **`PacketServiceProvider`** – provider implementation that:
  - uses `PacketCodec` to encode/decode packets
  - uses `OctetStringChannel` as the underlying transport
  - delivers received packets to the listener via `PACKET.indication`

---

### `ccsds.space.packet.protocol.types`
This package contains **enums** for SPP field values:
- **`CommandType`** – represents the CCSDS packet type bit:
  - `TM` (value `0`)
  - `TC` (value `1`)
- **`SequenceFlags`** – represents the CCSDS flags (2-bit field):
  - continuation segment
  - first segment
  - last segment
  - unsegmented

---

## Test Structure

### `src/test/java/ccsds.space.packet.protocol.codec`
Contains **unit tests for packet encoding/decoding**:
- **`PacketCodecTest`** – verifies:
  - encode → decode which preserves header fields and data field
  - correct error handling for invalid inputs

---

### `src/test/java/ccsds.space.packet.protocol.core`
Contains **unit tests for Space Packet Header**:
- **`SpacePacketHeaderTest`** – verifies:
  - field validation rules and ranges
  - correct primary header encoding
  - correct parsing of primary header bytes back into header fields
  - correctness of bit packing (shift logic) through expected values