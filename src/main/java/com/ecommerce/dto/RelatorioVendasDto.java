package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class RelatorioVendasDto {
    private final long totalPedidos;
    private final BigDecimal valorMedio;
    private final BigDecimal valorTotal;
}
