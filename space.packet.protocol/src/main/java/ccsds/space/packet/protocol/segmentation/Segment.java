package ccsds.space.packet.protocol.segmentation;

import ccsds.space.packet.protocol.types.SequenceFlags;

public record Segment(SequenceFlags sequenceFlags, int sequenceCount, byte[] data) {}