package com.torion.model;

public record ActivityLog(
        int log_id,
        int userId,
        String action_taken,
        String details,
        String timestamp
) {}