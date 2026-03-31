package com.academia;

import com.academia.tela.TelaPrincipal;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JWindow splash = new JWindow();
        splash.setSize(450, 280);
        splash.setLocationRelativeTo(null);
        JPanel splashPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(255, 140, 0), 0, getHeight(), new Color(200, 80, 0));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 22));
                FontMetrics fm = g2.getFontMetrics();
                String t1 = "Sistema de Gerenciamento";
                g2.drawString(t1, (getWidth() - fm.stringWidth(t1)) / 2, 100);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 40));
                fm = g2.getFontMetrics();
                String t2 = "Academia";
                g2.drawString(t2, (getWidth() - fm.stringWidth(t2)) / 2, 160);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                fm = g2.getFontMetrics();
                String t3 = "Carregando...";
                g2.drawString(t3, (getWidth() - fm.stringWidth(t3)) / 2, 240);
            }
        };
        splash.setContentPane(splashPanel);
        splash.setVisible(true);

        Timer timer = new Timer(2000, e -> {
            splash.dispose();
            SwingUtilities.invokeLater(() -> {
                TelaPrincipal tela = new TelaPrincipal();
                tela.setVisible(true);
            });
        });
        timer.setRepeats(false);
        timer.start();
    }
}
