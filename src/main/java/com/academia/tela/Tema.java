package com.academia.tela;

import java.awt.*;
import javax.swing.*;

public class Tema {
    // Cores principais
    public static final Color PRIMARIA = new Color(247, 127, 0);
    public static final Color PRIMARIA_DARK = new Color(204, 92, 0);
    public static final Color PRIMARIA_HOVER = new Color(224, 110, 0);
    public static final Color PRIMARIA_LIGHT = new Color(255, 224, 178);

    // Cores semanticas
    public static final Color SUCESSO = new Color(34, 152, 88);
    public static final Color PERIGO = new Color(214, 48, 49);
    public static final Color PERIGO_HOVER = new Color(184, 32, 40);
    public static final Color INFO = new Color(41, 121, 226);

    // Cores neutras
    public static final Color FUNDO = new Color(243, 244, 247);
    public static final Color CARD = Color.WHITE;
    public static final Color BORDA = new Color(228, 231, 236);
    public static final Color TEXTO = new Color(30, 37, 46);
    public static final Color TEXTO_SECUNDARIO = new Color(120, 128, 138);
    public static final Color CINZA_BTN = new Color(237, 239, 242);
    public static final Color CINZA_BTN_HOVER = new Color(223, 226, 231);
    public static final Color INFO_HOVER = new Color(31, 102, 196);

    // Fontes
    public static final Font TITULO = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font SUBTITULO = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font CORPO = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font LABEL = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font PEQUENA = new Font("Segoe UI", Font.PLAIN, 11);

    public static JButton botaoPrimario(String texto) {
        return montarBotao(texto, PRIMARIA, PRIMARIA_HOVER, Color.WHITE, SUBTITULO, 20);
    }

    public static JButton botaoSecundario(String texto) {
        return montarBotao(texto, CINZA_BTN, CINZA_BTN_HOVER, TEXTO, CORPO, 16);
    }

    public static JButton botaoPerigo(String texto) {
        return montarBotao(texto, PERIGO, PERIGO_HOVER, Color.WHITE, CORPO, 16);
    }

    public static JButton botaoExportar(String texto) {
        return montarBotao(texto, INFO, INFO_HOVER, Color.WHITE, CORPO, 16);
    }

    /** Botao "flat" com cantos arredondados, hover suave e sem foco pintado. */
    private static JButton montarBotao(String texto, Color base, Color hover, Color fg, Font fonte, int padX) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? hover : base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setBackground(base);
        btn.setForeground(fg);
        btn.setFont(fonte);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, padX, 8, padX));
        return btn;
    }

    /** Cabecalho de tela com gradiente laranja. Adicione controles no EAST. */
    public static JPanel cabecalho(String titulo) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, PRIMARIA, getWidth(), 0, PRIMARIA_DARK));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        JLabel t = new JLabel(titulo);
        t.setFont(new Font("Segoe UI", Font.BOLD, 19));
        t.setForeground(Color.WHITE);
        p.add(t, BorderLayout.WEST);
        return p;
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
