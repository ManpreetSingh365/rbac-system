package com.fleetmanagement.dto.request;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @Size(max = 15, message = "Phone number cannot exceed 15 characters")
    private String phoneNumber;

    private UUID tenantId;

    private Set<UUID> roleIds;

    private Set<UUID> deviceIds;

    private Set<UUID> vehicleIds;

}