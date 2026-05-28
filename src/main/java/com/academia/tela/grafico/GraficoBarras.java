package com.academia.tela.grafico;

import javax.swing.*;
import java.awt.*;

/**
 * Grafico de barras verticais desenhado em Java2D puro (sem bibliotecas).
 */
public class GraficoBarras extends JPanel {

    private String titulo = "";
    private String[] rotulos = new String[0];
    private double[] valores = new double[0];
    private boolean moeda = false;
    private boolean corPorBarra = false;

    public GraficoBarras() {
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
    public void setCorPorBarra(boolean v) { this.corPorBarra = v; }

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

        // titulo
        g2.setColor(new Color(33, 37, 41));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2.drawString(titulo, 12, 20);

        if (valores.length == 0) {
            desenharSemDados(g2, w, h);
            return;
        }

        int margemEsq = 50, margemDir = 15, margemTopo = 35, margemBaixo = 45;
        int areaX = margemEsq, areaY = margemTopo;
        int areaW = w - margemEsq - margemDir;
        int areaH = h - margemTopo - margemBaixo;

        double max = 0;
        for (double v : valores) max = Math.max(max, v);
        if (max <= 0) max = 1;

        // linhas de grade horizontais + escala
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        int linhas = 4;
        for (int i = 0; i <= linhas; i++) {
            int y = areaY + areaH - (areaH * i / linhas);
            g2.setColor(new Color(230, 232, 235));
            g2.drawLine(areaX, y, areaX + areaW, y);
            g2.setColor(new Color(140, 140, 140));
            String escala = fmt(max * i / linhas);
            g2.drawString(escala, 6, y + 4);
        }

        int n = valores.length;
        double passo = (double) areaW / n;
        int larguraBarra = (int) Math.max(4, passo * 0.6);

        // evita poluicao quando ha muitas barras: so mostra o valor se a barra
        // for larga o suficiente, e espaca os rotulos do eixo X
        boolean mostrarValores = larguraBarra >= 16;
        int saltoRot = Math.max(1, (int) Math.ceil(28.0 / passo));

        for (int i = 0; i < n; i++) {
            int alturaBarra = (int) (areaH * (valores[i] / max));
            int x = (int) (areaX + passo * i + (passo - larguraBarra) / 2);
            int y = areaY + areaH - alturaBarra;

            g2.setColor(corPorBarra ? PaletaGrafico.cor(i) : PaletaGrafico.cor(0));
            g2.fillRoundRect(x, y, larguraBarra, alturaBarra, 6, 6);

            // valor acima da barra (apenas quando cabe)
            if (mostrarValores) {
                g2.setColor(new Color(60, 60, 60));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                String val = fmt(valores[i]);
                int vw = g2.getFontMetrics().stringWidth(val);
                g2.drawString(val, x + (larguraBarra - vw) / 2, y - 3);
            }

            // rotulo abaixo (espacado para nao sobrepor)
            if (i % saltoRot == 0 || i == n - 1) {
                g2.setColor(new Color(90, 90, 90));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                String r = encurtar(g2, rotulos[i], (int) (passo * saltoRot) + 6);
                int rw = g2.getFontMetrics().stringWidth(r);
                g2.drawString(r, (int) (areaX + passo * i + (passo - rw) / 2), areaY + areaH + 15);
            }
        }

        // eixo base
        g2.setColor(new Color(180, 180, 180));
        g2.drawLine(areaX, areaY + areaH, areaX + areaW, areaY + areaH);
    }

    static String encurtar(Graphics2D g2, String txt, int larguraMax) {
        if (txt == null) txt = "";
        if (g2.getFontMetrics().stringWidth(txt) <= larguraMax) return txt;
        String s = txt;
        while (s.length() > 1 && g2.getFontMetrics().stringWidth(s + "...") > larguraMax) {
            s = s.substring(0, s.length() - 1);
        }
        return s + "...";
    }

    static void desenharSemDados(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(150, 150, 150));
        g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        String msg = "Sem dados para exibir";
        int mw = g2.getFontMetrics().stringWidth(msg);
        g2.drawString(msg, (w - mw) / 2, h / 2);
    }
}
