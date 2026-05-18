package com.indiene.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Curtida {
    private Long id;
    private String tipo;
    private Long comentarioId;
    private Long postagemId;
}
