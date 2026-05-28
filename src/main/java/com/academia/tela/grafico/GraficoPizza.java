package com.academia.tela.grafico;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;

/**
 * Grafico de pizza com legenda lateral - Java2D puro.
 */
public class GraficoPizza extends JPanel {

    private String titulo = "";
    private String[] rotulos = new String[0];
    private double[] valores = new double[0];

    public GraficoPizza() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(360, 240));
    }

    public void setDados(String titulo, String[] rotulos, double[] valores) {
        this.titulo = titulo;
        this.rotulos = rotulos;
        this.valores = valores;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        g2.setColor(new Color(33, 37, 41));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2.drawString(titulo, 12, 20);

        double total = 0;
        for (double v : valores) total += v;

        if (valores.length == 0 || total <= 0) {
            GraficoBarras.desenharSemDados(g2, w, h);
            return;
        }

        // area da pizza a esquerda, legenda a direita
        int legendaW = 130;
        int diametro = Math.min(w - legendaW - 30, h - 50);
        if (diametro < 40) diametro = Math.max(40, Math.min(w, h) - 60);
        int cx = 20;
        int cy = 35;

        double anguloInicio = 90; // comeca no topo
        for (int i = 0; i < valores.length; i++) {
            double extensao = -360.0 * (valores[i] / total);
            g2.setColor(PaletaGrafico.cor(i));
            g2.fill(new Arc2D.Double(cx, cy, diametro, diametro, anguloInicio, extensao, Arc2D.PIE));
            anguloInicio += extensao;
        }

        // contorno branco entre fatias
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new java.awt.geom.Ellipse2D.Double(cx, cy, diametro, diametro));

        // legenda
        int lx = cx + diametro + 20;
        int ly = cy + 6;
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        for (int i = 0; i < valores.length; i++) {
            g2.setColor(PaletaGrafico.cor(i));
            g2.fillRoundRect(lx, ly - 9, 11, 11, 3, 3);
            g2.setColor(new Color(70, 70, 70));
            double pct = 100.0 * valores[i] / total;
            String txt = String.format("%s  %.0f (%.1f%%)", rotulos[i], valores[i], pct);
            txt = GraficoBarras.encurtar(g2, txt, w - lx - 14);
            g2.drawString(txt, lx + 16, ly);
            ly += 20;
        }
    }
}
