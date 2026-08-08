package com.deltahomes.backend.dto.company;

import com.deltahomes.backend.entity.enums.CompanyType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request/response payloads for the companies module.
 */
public final class CompanyDtos {

    private CompanyDtos() {
    }

    public record CreateCompanyRequest(
            @NotBlank(message = "Company name is required")
            @Size(max = 200, message = "Company name must be at most 200 characters")
            String name,

            @NotNull(message = "Company type is required")
            CompanyType type,

            @Size(max = 2000, message = "Description must be at most 2000 characters")
            String description,

            @Size(max = 255, message = "Logo URL must be at most 255 characters")
            String logoUrl,

            @Size(max = 255, message = "Cover URL must be at most 255 characters")
            String coverUrl,

            @Size(max = 20, message = "Phone must be at most 20 characters")
            String phone,

            @Size(max = 20, message = "WhatsApp must be at most 20 characters")
            String whatsapp,

            @Email(message = "Invalid email address")
            @Size(max = 150, message = "Email must be at most 150 characters")
            String email,

            @Size(max = 150, message = "Website must be at most 150 characters")
            String website,

            @Size(max = 2000, message = "Coverage area must be at most 2000 characters")
            String coverageArea
    ) {
    }
}
