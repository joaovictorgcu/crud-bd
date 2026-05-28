package com.academia.tela.grafico;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * Grafico de linha (tendencia temporal) - Java2D puro.
 */
public class GraficoLinha extends JPanel {

    private String titulo = "";
    private String[] rotulos = new String[0];
    private double[] valores = new double[0];
    private boolean moeda = false;

    public GraficoLinha() {
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

        int margemEsq = 55, margemDir = 20, margemTopo = 35, margemBaixo = 40;
        int areaX = margemEsq, areaY = margemTopo;
        int areaW = w - margemEsq - margemDir;
        int areaH = h - margemTopo - margemBaixo;

        double max = 0;
        for (double v : valores) max = Math.max(max, v);
        if (max <= 0) max = 1;

        // grade + escala
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        int linhas = 4;
        for (int i = 0; i <= linhas; i++) {
            int y = areaY + areaH - (areaH * i / linhas);
            g2.setColor(new Color(230, 232, 235));
            g2.drawLine(areaX, y, areaX + areaW, y);
            g2.setColor(new Color(140, 140, 140));
            g2.drawString(fmt(max * i / linhas), 6, y + 4);
        }

        int n = valores.length;
        double passo = (n > 1) ? (double) areaW / (n - 1) : areaW;

        int[] px = new int[n];
        int[] py = new int[n];
        for (int i = 0; i < n; i++) {
            px[i] = (int) (n > 1 ? areaX + passo * i : areaX + areaW / 2.0);
            py[i] = (int) (areaY + areaH - areaH * (valores[i] / max));
        }

        // area sob a linha
        g2.setColor(new Color(255, 140, 0, 40));
        Polygon area = new Polygon();
        area.addPoint(px[0], areaY + areaH);
        for (int i = 0; i < n; i++) area.addPoint(px[i], py[i]);
        area.addPoint(px[n - 1], areaY + areaH);
        g2.fillPolygon(area);

        // linha
        g2.setColor(new Color(255, 140, 0));
        g2.setStroke(new BasicStroke(2.2f));
        for (int i = 0; i < n - 1; i++) {
            g2.drawLine(px[i], py[i], px[i + 1], py[i + 1]);
        }

        // pontos + rotulos x
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        int salto = Math.max(1, n / 8); // evita poluir o eixo x
        for (int i = 0; i < n; i++) {
            g2.setColor(new Color(255, 140, 0));
            g2.fill(new Ellipse2D.Double(px[i] - 3, py[i] - 3, 6, 6));
            g2.setColor(Color.WHITE);
            g2.draw(new Ellipse2D.Double(px[i] - 3, py[i] - 3, 6, 6));

            if (i % salto == 0 || i == n - 1) {
                g2.setColor(new Color(110, 110, 110));
                String r = rotulos[i];
                int rw = g2.getFontMetrics().stringWidth(r);
                g2.drawString(r, px[i] - rw / 2, areaY + areaH + 15);
            }
        }

        // eixo base
        g2.setColor(new Color(180, 180, 180));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(areaX, areaY + areaH, areaX + areaW, areaY + areaH);
    }
}
