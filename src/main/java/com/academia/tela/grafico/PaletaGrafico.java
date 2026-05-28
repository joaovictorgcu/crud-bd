package com.academia.tela.grafico;

import java.awt.Color;

/** Paleta de cores compartilhada pelos graficos (combina com o tema laranja). */
public final class PaletaGrafico {

    private PaletaGrafico() {}

    public static final Color[] CORES = {
        new Color(255, 140, 0),   // laranja (primaria)
        new Color(47, 128, 237),  // azul
        new Color(46, 160, 67),   // verde
        new Color(207, 34, 46),   // vermelho
        new Color(155, 89, 182),  // roxo
        new Color(241, 196, 15),  // amarelo
        new Color(26, 188, 156),  // turquesa
        new Color(149, 165, 166), // cinza
        new Color(230, 126, 34),  // laranja escuro
        new Color(52, 73, 94)     // azul petroleo
    };

    public static Color cor(int indice) {
        return CORES[Math.abs(indice) % CORES.length];
    }
}
