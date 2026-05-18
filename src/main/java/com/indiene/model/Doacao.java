package com.indiene.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Doacao {
    private Long id;
    private Double valor;
    private LocalDateTime data;
    private Long userId;
    private Long jogoId;
}
