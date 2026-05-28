package com.academia.tela.grafico;

import javax.swing.*;
import java.awt.*;

/**
 * Grafico de barras horizontais (bom para rotulos longos e rankings) - Java2D puro.
 */
public class GraficoBarrasHorizontal extends JPanel {

    private String titulo = "";
    private String[] rotulos = new String[0];
    private double[] valores = new double[0];
    private boolean moeda = false;

    public GraficoBarrasHorizontal() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(360, 240));
    }

    public void setDados(String titulo, String[] rotulos, double[] valores) {
        this.titulo = titulo;
        this.rotulos = rotulos;
        this.valores = valores;
        repaint();
    }

    public void setMoeda(boolean moeda) { this.moeda = moeda; }

    private String fmt(double v) {
        if (moeda) return String.format("R$ %,.0f", v);
        return (v == Math.floor(v)) ? String.format("%.0f", v) : String.format("%.1f", v);
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

        if (valores.length == 0) {
            GraficoBarras.desenharSemDados(g2, w, h);
            return;
        }

        int margemEsq = 110, margemDir = 55, margemTopo = 35, margemBaixo = 15;
        int areaX = margemEsq, areaY = margemTopo;
        int areaW = w - margemEsq - margemDir;
        int areaH = h - margemTopo - margemBaixo;

        double max = 0;
        for (double v : valores) max = Math.max(max, v);
        if (max <= 0) max = 1;

        int n = valores.length;
        double passo = (double) areaH / n;
        int alturaBarra = (int) Math.max(8, passo * 0.6);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        for (int i = 0; i < n; i++) {
            int comprimento = (int) (areaW * (valores[i] / max));
            int y = (int) (areaY + passo * i + (passo - alturaBarra) / 2);

            // rotulo a esquerda
            g2.setColor(new Color(80, 80, 80));
            String r = GraficoBarras.encurtar(g2, rotulos[i], margemEsq - 12);
            g2.drawString(r, 8, y + alturaBarra / 2 + 4);

            // barra
            g2.setColor(PaletaGrafico.cor(i));
            g2.fillRoundRect(areaX, y, Math.max(1, comprimento), alturaBarra, 6, 6);

            // valor a direita
            g2.setColor(new Color(60, 60, 60));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.drawString(fmt(valores[i]), areaX + comprimento + 5, y + alturaBarra / 2 + 4);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        }
    }
}
