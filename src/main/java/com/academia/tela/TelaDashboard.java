package com.academia.tela;

import com.academia.conexao.ConexaoBD;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TelaDashboard extends JPanel {

    private JLabel[] valoresLabels;
    private JLabel lblUltimaAtualizacao;

    private final String[] titulos = {
            "Total de Alunos", "Alunos Ativos", "Total de Instrutores", "Assinaturas Ativas",
            "Receita Total", "Pagamentos Pendentes", "Total de Atividades", "Total de Equipamentos"
    };

    private final Color[] acentos = {
            Tema.PRIMARIA, Tema.PRIMARIA, Tema.INFO, Tema.INFO,
            Tema.SUCESSO, Tema.PERIGO, Tema.INFO, Tema.INFO
    };

    private final String[] consultas = {
            "SELECT COUNT(*) FROM aluno",
            "SELECT COUNT(*) FROM aluno WHERE status = 'ATIVO'",
            "SELECT COUNT(*) FROM instrutor",
            "SELECT COUNT(*) FROM assinatura WHERE status = 'ATIVA'",
            "SELECT COALESCE(SUM(valor), 0) FROM pagamento WHERE status = 'PAGO'",
            "SELECT COUNT(*) FROM pagamento WHERE status = 'PENDENTE'",
            "SELECT COUNT(*) FROM atividade",
            "SELECT COUNT(*) FROM equipamento"
    };

    public TelaDashboard() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Tema.FUNDO);

        // Header
        JPanel painelHeader = new JPanel();
        painelHeader.setBackground(Tema.PRIMARIA);
        painelHeader.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel lblTitulo = new JLabel("Dashboard");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);
        painelHeader.add(lblTitulo);
        add(painelHeader, BorderLayout.NORTH);

        // Cards
        JPanel painelCards = new JPanel(new GridLayout(2, 4, 15, 15));
        painelCards.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        painelCards.setBackground(Tema.FUNDO);

        valoresLabels = new JLabel[titulos.length];

        for (int i = 0; i < titulos.length; i++) {
            JPanel card = criarCard(titulos[i], acentos[i]);
            painelCards.add(card);
        }

        add(painelCards, BorderLayout.CENTER);

        // Bottom
        JPanel painelBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        painelBottom.setBackground(Tema.FUNDO);

        JButton btnAtualizar = Tema.botaoPrimario("Atualizar");
        btnAtualizar.addActionListener(e -> carregarDados());
        painelBottom.add(btnAtualizar);

        lblUltimaAtualizacao = new JLabel("");
        lblUltimaAtualizacao.setForeground(Tema.TEXTO_SECUNDARIO);
        lblUltimaAtualizacao.setFont(Tema.PEQUENA);
        painelBottom.add(lblUltimaAtualizacao);

        add(painelBottom, BorderLayout.SOUTH);

        carregarDados();
    }

    private JPanel criarCard(String titulo, Color acento) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Tema.CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Tema.BORDA, 1),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        // Faixa colorida a esquerda
        JPanel faixa = new JPanel();
        faixa.setPreferredSize(new Dimension(4, 0));
        faixa.setBackground(acento);
        card.add(faixa, BorderLayout.WEST);

        JPanel conteudo = new JPanel();
        conteudo.setLayout(new BoxLayout(conteudo, BoxLayout.Y_AXIS));
        conteudo.setBackground(Tema.CARD);
        conteudo.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(Tema.PEQUENA);
        lblTitulo.setForeground(Tema.TEXTO_SECUNDARIO);

        JLabel lblValor = new JLabel("...");
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblValor.setForeground(Tema.TEXTO);

        // Armazena a referencia no array pelo indice baseado no titulo
        for (int i = 0; i < titulos.length; i++) {
            if (titulos[i].equals(titulo)) {
                valoresLabels[i] = lblValor;
                break;
            }
        }

        conteudo.add(lblTitulo);
        conteudo.add(Box.createVerticalStrut(4));
        conteudo.add(lblValor);

        card.add(conteudo, BorderLayout.CENTER);
        return card;
    }

    private void carregarDados() {
        try (Connection conn = ConexaoBD.getConexao()) {
            for (int i = 0; i < consultas.length; i++) {
                try (PreparedStatement ps = conn.prepareStatement(consultas[i]);
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        if (i == 4) {
                            double valor = rs.getDouble(1);
                            valoresLabels[i].setText(String.format("R$ %.2f", valor));
                        } else {
                            valoresLabels[i].setText(String.valueOf(rs.getInt(1)));
                        }
                    }
                }
            }
        } catch (Exception e) {
            for (JLabel lbl : valoresLabels) {
                lbl.setText("?");
            }
            e.printStackTrace();
        }

        String agora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        lblUltimaAtualizacao.setText("Ultima atualizacao: " + agora);
    }
}
