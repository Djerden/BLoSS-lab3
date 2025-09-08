package com.djeno.lab1.persistence.DTO.app;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilePart {
    private String filename;
    private String base64;
}
