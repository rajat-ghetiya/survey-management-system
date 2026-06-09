package com.example.survey.enums;

public enum Role {
    SUPER_ADMIN,    // Full system access - global owner
    ADMIN,          // Organization-level admin
    MANAGER,        // Can create/manage surveys in their dept
    TEAM_LEAD,      // Can view team responses, assign surveys
    USER            // Can only participate in assigned surveys
}
