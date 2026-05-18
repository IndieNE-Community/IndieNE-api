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
public class Postagem {
    private Long id;
    private String titulo;
    private String descricao;
    private LocalDateTime data;
    private Long jogoId;
}
