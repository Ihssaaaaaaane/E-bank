package com.ebank.dtos;

import com.ebank.enums.OperationType;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class OperationDTO {
    private Long id;
    private Date date;
    private BigDecimal amount;
    private OperationType type;
    private String title;
}