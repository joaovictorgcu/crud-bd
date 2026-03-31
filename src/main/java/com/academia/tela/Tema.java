package com.academia.tela;

import java.awt.*;
import javax.swing.*;

public class Tema {
    // Cores principais
    public static final Color PRIMARIA = new Color(255, 140, 0);
    public static final Color PRIMARIA_HOVER = new Color(230, 120, 0);
    public static final Color PRIMARIA_LIGHT = new Color(255, 200, 120);

    // Cores semanticas
    public static final Color SUCESSO = new Color(46, 160, 67);
    public static final Color PERIGO = new Color(207, 34, 46);
    public static final Color PERIGO_HOVER = new Color(180, 25, 35);
    public static final Color INFO = new Color(47, 128, 237);

    // Cores neutras
    public static final Color FUNDO = new Color(248, 249, 250);
    public static final Color CARD = Color.WHITE;
    public static final Color BORDA = new Color(222, 226, 230);
    public static final Color TEXTO = new Color(33, 37, 41);
    public static final Color TEXTO_SECUNDARIO = new Color(108, 117, 125);

    // Fontes
    public static final Font TITULO = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font SUBTITULO = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font CORPO = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font LABEL = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font PEQUENA = new Font("Segoe UI", Font.PLAIN, 11);

    public static JButton botaoPrimario(String texto) {
        JButton btn = new JButton(texto);
        btn.setBackground(PRIMARIA);
        btn.setForeground(Color.WHITE);
        btn.setFont(SUBTITULO);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        return btn;
    }

    public static JButton botaoSecundario(String texto) {
        JButton btn = new JButton(texto);
        btn.setBackground(new Color(233, 236, 239));
        btn.setForeground(TEXTO);
        btn.setFont(CORPO);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }

    public static JButton botaoPerigo(String texto) {
        JButton btn = new JButton(texto);
        btn.setBackground(PERIGO);
        btn.setForeground(Color.WHITE);
        btn.setFont(CORPO);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }

    public static JButton botaoExportar(String texto) {
        JButton btn = new JButton(texto);
        btn.setBackground(INFO);
        btn.setForeground(Color.WHITE);
        btn.setFont(CORPO);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }

    public static void estilizarTabela(JTable tabela) {
        tabela.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean focus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                l.setBackground(PRIMARIA);
                l.setForeground(Color.WHITE);
                l.setFont(LABEL);
                l.setOpaque(true);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, PRIMARIA_HOVER));
                return l;
            }
        });
        tabela.setRowHeight(28);
        tabela.setSelectionBackground(PRIMARIA_LIGHT);
        tabela.setSelectionForeground(TEXTO);
        tabela.setGridColor(BORDA);
        tabela.setFont(CORPO);
        tabela.setIntercellSpacing(new Dimension(1, 1));
        tabela.setShowGrid(true);
    }
}
