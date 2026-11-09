package com.torion.model;

public record ParkingTicket(
        int ticket_id,
        int slot_id,
        String plate_number,
        String entry_time,
        String exit_time,
        double total_fee,
        String payment_status
) {
    @Override
    public String toString() {
        return String.format("[Ticket #%d] Slot ID: %d | Plate: %s | Entry: %s | Status: %s",
                ticket_id, slot_id, plate_number, entry_time, payment_status);
    }
}