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
public class Comentario {
    private Long id;
    private String texto;
    private LocalDateTime data;
    private Long userId;
    private Long postagemId;
}
