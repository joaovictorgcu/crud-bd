package com.academia.tela;

import javax.swing.*;
import java.awt.*;

public class TelaPrincipal extends JFrame {

    private static java.util.List<Image> criarIcones() {
        java.util.List<Image> icones = new java.util.ArrayList<>();
        try {
            java.io.InputStream is = TelaPrincipal.class.getResourceAsStream("/muscle.png");
            if (is == null) {
                is = new java.io.FileInputStream("src/main/resources/muscle.png");
            }
            java.awt.image.BufferedImage original = javax.imageio.ImageIO.read(is);
            is.close();
            for (int s : new int[]{16, 32, 48, 64}) {
                icones.add(recolorir(original, s));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return icones;
    }

    private static Image recolorir(java.awt.image.BufferedImage original, int tamanho) {
        java.awt.image.BufferedImage redim = new java.awt.image.BufferedImage(tamanho, tamanho, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D gr = redim.createGraphics();
        gr.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gr.drawImage(original, 0, 0, tamanho, tamanho, null);
        gr.dispose();

        // transforma pixels escuros em laranja pra combinar com o tema
        int laranja = new Color(255, 140, 0).getRGB();
        java.awt.image.BufferedImage resultado = new java.awt.image.BufferedImage(tamanho, tamanho, java.awt.image.BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < tamanho; y++) {
            for (int x = 0; x < tamanho; x++) {
                int rgba = redim.getRGB(x, y);
                int alpha = (rgba >> 24) & 0xFF;
                int r = (rgba >> 16) & 0xFF;
                int g = (rgba >> 8) & 0xFF;
                int b = rgba & 0xFF;
                float brilho = (r + g + b) / 3f;
                if (alpha > 50 && brilho < 128) {
                    int novoRgba = (alpha << 24) | (laranja & 0x00FFFFFF);
                    resultado.setRGB(x, y, novoRgba);
                }
            }
        }

        return resultado;
    }

    public TelaPrincipal() {
        setTitle("Sistema de Gerenciamento de Academia");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setIconImages(criarIcones());

        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Dashboard", new TelaDashboard());
        abas.addTab("Alunos", new TelaAluno());
        abas.addTab("Instrutores", new TelaInstrutor());
        abas.addTab("Planos", new TelaPlano());
        abas.addTab("Assinaturas", new TelaAssinatura());
        abas.addTab("Pagamentos", new TelaPagamento());
        abas.addTab("Atividades", new TelaAtividade());
        abas.addTab("Equipamentos", new TelaEquipamento());

        abas.setFont(Tema.SUBTITULO);
        getContentPane().setBackground(Tema.FUNDO);

        add(abas, BorderLayout.CENTER);
    }
}
