package com.djeno.appService.persistence.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateAppRequest {

    private String name;

    private String description;

    private BigDecimal price;

    private List<Long> categoryIds;
}
