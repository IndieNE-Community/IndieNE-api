package com.indiene.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Plataforma {
    private Long id;
    private String plataforma;
    private Long jogoId;
}
