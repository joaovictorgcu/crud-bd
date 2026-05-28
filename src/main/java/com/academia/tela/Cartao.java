package com.academia.tela;

import javax.swing.*;
import java.awt.*;

/**
 * Cartao com cantos arredondados e sombra suave (desenhado em Java2D).
 * Usado para dar uma aparencia mais moderna aos paineis (KPIs, graficos).
 *
 * O espaco da sombra fica reservado pela borda interna, entao os filhos
 * nunca invadem a area de sombra. Filhos devem ser nao-opacos para o fundo
 * arredondado aparecer.
 */
public class Cartao extends JPanel {

    private static final int ARCO = 18;
    private static final int SOMBRA = 7;

    private Color fundo = Tema.CARD;

    public Cartao() {
        this(new BorderLayout());
    }

    public Cartao(LayoutManager layout) {
        setOpaque(false);
        setLayout(layout);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14 + SOMBRA, 16 + SOMBRA));
    }

    public void setFundo(Color c) { this.fundo = c; repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        int cw = w - SOMBRA, ch = h - SOMBRA;

        // sombra suave (duas camadas translucidas)
        g2.setColor(new Color(17, 24, 39, 18));
        g2.fillRoundRect(SOMBRA - 2, SOMBRA, cw, ch, ARCO, ARCO);
        g2.setColor(new Color(17, 24, 39, 12));
        g2.fillRoundRect(SOMBRA - 4, SOMBRA - 2, cw, ch, ARCO, ARCO);

        // cartao
        g2.setColor(fundo);
        g2.fillRoundRect(0, 0, cw, ch, ARCO, ARCO);
        g2.setColor(Tema.BORDA);
        g2.drawRoundRect(0, 0, cw - 1, ch - 1, ARCO, ARCO);

        g2.dispose();
        super.paintComponent(g);
    }
}
