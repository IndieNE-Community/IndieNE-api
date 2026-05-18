package com.indiene.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Imagem {
    private Long id;
    private String imagem;
    private Long jogoId;
    private Long postagemId;
}
