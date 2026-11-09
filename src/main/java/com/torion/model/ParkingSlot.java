package com.torion.model;

public record ParkingSlot(
        int slot_id,
        String slot_code,
        int floor_level,
        String slot_type,
        String status
) {
    @Override
    public String toString() {
        return String.format("[%s] Floor: %d | Type: %s | Status: %s", slot_code, floor_level, slot_type, status);
    }
}