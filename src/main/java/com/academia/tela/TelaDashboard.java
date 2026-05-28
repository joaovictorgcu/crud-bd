package com.academia.tela;

import com.academia.dao.RelatorioDAO;
import com.academia.dao.ResultadoTabela;
import com.academia.tela.grafico.GraficoBarras;
import com.academia.tela.grafico.GraficoBarrasHorizontal;
import com.academia.tela.grafico.GraficoLinha;
import com.academia.tela.grafico.GraficoPizza;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Dashboard Estatistico Integrado - todos os numeros vem do banco (RelatorioDAO).
 *
 * Indicadores resumidos (totais, media, percentual), bloco de estatisticas
 * (media/mediana/moda/variancia/desvio padrao do salario) e 5 graficos dinamicos
 * com filtro de periodo (ano).
 */
public class TelaDashboard extends JPanel {

    private final RelatorioDAO dao = new RelatorioDAO();

    // indicadores
    private final String[] titulos = {
            "Total de Alunos", "Total de Instrutores", "Total de Assinaturas", "Receita Total (PAGO)",
            "Pagamentos Pendentes", "Total de Pagamentos", "Ticket Medio (PAGO)", "% Pagamentos Pagos"
    };
    private final Color[] acentos = {
            Tema.PRIMARIA, Tema.INFO, Tema.INFO, Tema.SUCESSO,
            Tema.PERIGO, Tema.PRIMARIA, Tema.PRIMARIA, Tema.INFO
    };
    private final JLabel[] valores = new JLabel[titulos.length];
    private final JLabel[] titulosLabels = new JLabel[titulos.length];
    private boolean montando = false;

    // estatisticas salario
    private final String[] statTitulos = {"Media", "Mediana", "Moda", "Variancia", "Desvio Padrao", "Min", "Max"};
    private final JLabel[] statValores = new JLabel[statTitulos.length];

    // graficos
    private final GraficoPizza gPagStatus = new GraficoPizza();
    private final GraficoBarrasHorizontal gReceitaPlano = new GraficoBarrasHorizontal();
    private final GraficoLinha gReceitaMes = new GraficoLinha();
    private final GraficoBarrasHorizontal gInadimplencia = new GraficoBarrasHorizontal();
    private final GraficoBarras gAssinStatus = new GraficoBarras();
    private final GraficoPizza gAlunosStatus = new GraficoPizza();

    private final JComboBox<String> cmbAno = new JComboBox<>();
    private final JLabel lblAtualizacao = new JLabel("");

    public TelaDashboard() {
        setLayout(new BorderLayout());
        setBackground(Tema.FUNDO);

        add(criarHeader(), BorderLayout.NORTH);

        JPanel conteudo = new JPanel();
        conteudo.setLayout(new BoxLayout(conteudo, BoxLayout.Y_AXIS));
        conteudo.setBackground(Tema.FUNDO);
        conteudo.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        conteudo.add(criarCards());
        conteudo.add(Box.createVerticalStrut(12));
        conteudo.add(criarEstatisticas());
        conteudo.add(Box.createVerticalStrut(12));
        conteudo.add(criarGraficos());

        JScrollPane scroll = new JScrollPane(conteudo);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        popularAnos();
        carregar();
    }

    private JPanel criarHeader() {
        JPanel header = Tema.cabecalho("Dashboard Estatistico");

        JPanel dir = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        dir.setOpaque(false);
        JLabel lblPeriodo = new JLabel("Periodo (ano):");
        lblPeriodo.setForeground(Color.WHITE);
        lblPeriodo.setFont(Tema.CORPO);
        dir.add(lblPeriodo);
        cmbAno.setFont(Tema.CORPO);
        cmbAno.addActionListener(e -> { if (!montando && cmbAno.getSelectedIndex() >= 0) carregar(); });
        dir.add(cmbAno);
        JButton btn = Tema.botaoSecundario("Atualizar");
        btn.addActionListener(e -> carregar());
        dir.add(btn);
        lblAtualizacao.setForeground(new Color(255, 235, 210));
        lblAtualizacao.setFont(Tema.PEQUENA);
        dir.add(lblAtualizacao);
        header.add(dir, BorderLayout.EAST);

        return header;
    }

    private JPanel criarCards() {
        JPanel painel = new JPanel(new GridLayout(2, 4, 12, 12));
        painel.setBackground(Tema.FUNDO);
        painel.setAlignmentX(LEFT_ALIGNMENT);
        for (int i = 0; i < titulos.length; i++) {
            painel.add(criarCard(i));
        }
        return painel;
    }

    private JPanel criarCard(int i) {
        Cartao card = new Cartao(new BorderLayout());

        // topo: chip colorido + titulo
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        topo.setOpaque(false);
        topo.add(new Bolinha(acentos[i]));
        JLabel t = new JLabel(titulos[i]);
        t.setFont(Tema.PEQUENA);
        t.setForeground(Tema.TEXTO_SECUNDARIO);
        titulosLabels[i] = t;
        topo.add(t);

        valores[i] = new JLabel("...");
        valores[i].setFont(new Font("Segoe UI", Font.BOLD, 27));
        valores[i].setForeground(acentos[i]);

        card.add(topo, BorderLayout.NORTH);
        card.add(valores[i], BorderLayout.CENTER);
        return card;
    }

    /** Pequeno indicador colorido (chip) ao lado do titulo do card. */
    private static class Bolinha extends JComponent {
        private final Color cor;
        Bolinha(Color cor) { this.cor = cor; setPreferredSize(new Dimension(10, 10)); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(cor);
            g2.fillRoundRect(0, 1, 10, 10, 6, 6);
            g2.dispose();
        }
    }

    private JPanel criarEstatisticas() {
        Cartao secao = new Cartao(new BorderLayout(0, 8));
        secao.setAlignmentX(LEFT_ALIGNMENT);
        secao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        JLabel cab = new JLabel("Estatisticas do salario dos instrutores admitidos ate o ano");
        cab.setFont(Tema.SUBTITULO);
        cab.setForeground(Tema.TEXTO);
        secao.add(cab, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, statTitulos.length, 10, 0));
        grid.setOpaque(false);
        for (int i = 0; i < statTitulos.length; i++) {
            JPanel c = new JPanel();
            c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
            c.setBackground(Tema.FUNDO);
            c.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));
            JLabel t = new JLabel(statTitulos[i]);
            t.setFont(Tema.PEQUENA);
            t.setForeground(Tema.TEXTO_SECUNDARIO);
            t.setAlignmentX(CENTER_ALIGNMENT);
            statValores[i] = new JLabel("-");
            statValores[i].setFont(new Font("Segoe UI", Font.BOLD, 15));
            statValores[i].setForeground(i == 0 ? Tema.PRIMARIA : Tema.TEXTO);
            statValores[i].setAlignmentX(CENTER_ALIGNMENT);
            c.add(t);
            c.add(Box.createVerticalStrut(3));
            c.add(statValores[i]);
            grid.add(c);
        }
        secao.add(grid, BorderLayout.CENTER);
        return secao;
    }

    private JPanel criarGraficos() {
        JPanel grid = new JPanel(new GridLayout(0, 2, 12, 12));
        grid.setBackground(Tema.FUNDO);
        grid.setAlignmentX(LEFT_ALIGNMENT);

        gReceitaPlano.setMoeda(true);
        gReceitaMes.setMoeda(true);

        grid.add(emCard(gPagStatus));
        grid.add(emCard(gReceitaPlano));
        grid.add(emCard(gReceitaMes));
        grid.add(emCard(gInadimplencia));
        grid.add(emCard(gAssinStatus));
        grid.add(emCard(gAlunosStatus));

        return grid;
    }

    private JPanel emCard(JComponent grafico) {
        Cartao card = new Cartao(new BorderLayout());
        grafico.setOpaque(false);                 // deixa o fundo arredondado do cartao aparecer
        grafico.setPreferredSize(new Dimension(460, 250));
        card.add(grafico, BorderLayout.CENTER);
        return card;
    }

    // ------------------------------------------------------------- carga
    private void popularAnos() {
        montando = true;
        try {
            cmbAno.removeAllItems();
            cmbAno.addItem("Todos");
            ResultadoTabela rt = dao.listarAnosPagamento();
            for (Object[] l : rt.getLinhas()) {
                cmbAno.addItem(String.valueOf(((Number) l[0]).intValue()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            montando = false;
        }
    }

    private int anoSelecionado() {
        Object sel = cmbAno.getSelectedItem();
        if (sel == null || "Todos".equals(sel)) return 0;
        try { return Integer.parseInt(sel.toString()); } catch (Exception e) { return 0; }
    }

    private void carregar() {
        int ano = anoSelecionado();
        carregarIndicadores(ano);
        carregarEstatisticas(ano);
        carregarGraficos();
        lblAtualizacao.setText("  " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
    }

    private void carregarIndicadores(int ano) {
        boolean f = ano > 0;
        String sufixo = f ? " (" + ano + ")" : "";
        Object[] p = f ? new Object[]{ano} : new Object[0];
        try {
            // filtros por coluna de data de cada tabela (0 = todos)
            String fCad = f ? " WHERE EXTRACT(YEAR FROM dt_cadastro) = ?" : "";
            String fAdm = f ? " WHERE EXTRACT(YEAR FROM dt_admissao) = ?" : "";
            String fIni = f ? " WHERE EXTRACT(YEAR FROM dt_inicio) = ?" : "";
            String fVenc = f ? " AND EXTRACT(YEAR FROM dt_venc) = ?" : "";
            String fVencW = f ? " WHERE EXTRACT(YEAR FROM dt_venc) = ?" : "";

            setCard(0, (f ? "Alunos cadastrados" : "Total de Alunos") + sufixo,
                    intTxt("SELECT COUNT(*) FROM aluno" + fCad, p));
            setCard(1, (f ? "Instrutores admitidos" : "Total de Instrutores") + sufixo,
                    intTxt("SELECT COUNT(*) FROM instrutor" + fAdm, p));
            setCard(2, (f ? "Assinaturas iniciadas" : "Total de Assinaturas") + sufixo,
                    intTxt("SELECT COUNT(*) FROM assinatura" + fIni, p));
            setCard(3, "Receita PAGO" + sufixo,
                    "R$ " + moedaTxt("SELECT COALESCE(SUM(valor),0) FROM pagamento WHERE status = 'PAGO'" + fVenc, p));
            setCard(4, "Pagamentos Pendentes" + sufixo,
                    intTxt("SELECT COUNT(*) FROM pagamento WHERE status = 'PENDENTE'" + fVenc, p));
            setCard(5, "Total de Pagamentos" + sufixo,
                    intTxt("SELECT COUNT(*) FROM pagamento" + fVencW, p));
            setCard(6, "Ticket Medio PAGO" + sufixo,
                    "R$ " + moedaTxt("SELECT COALESCE(AVG(valor),0) FROM pagamento WHERE status = 'PAGO'" + fVenc, p));
            double pct = dao.valorDouble(
                    "SELECT CASE WHEN COUNT(*)=0 THEN 0 ELSE " +
                    "100.0 * COUNT(*) FILTER (WHERE status='PAGO') / COUNT(*) END FROM pagamento" + fVencW, p);
            setCard(7, "% Pagamentos Pagos" + sufixo, String.format("%.1f%%", pct));
        } catch (Exception ex) {
            for (JLabel l : valores) l.setText("?");
            ex.printStackTrace();
        }
    }

    private void setCard(int i, String titulo, String valor) {
        titulosLabels[i].setText(titulo);
        valores[i].setText(valor);
    }

    private void carregarEstatisticas(int ano) {
        try {
            ResultadoTabela rt = dao.estatisticasSalario(ano);
            if (rt.getQtdLinhas() > 0) {
                // ordem: media, mediana, moda, variancia, desvio, min, max
                statValores[0].setText("R$ " + fmt(rt.comoDouble(0, 0)));
                statValores[1].setText("R$ " + fmt(rt.comoDouble(0, 1)));
                statValores[2].setText("R$ " + fmt(rt.comoDouble(0, 2)));
                statValores[3].setText(fmt(rt.comoDouble(0, 3)));
                statValores[4].setText("R$ " + fmt(rt.comoDouble(0, 4)));
                statValores[5].setText("R$ " + fmt(rt.comoDouble(0, 5)));
                statValores[6].setText("R$ " + fmt(rt.comoDouble(0, 6)));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void carregarGraficos() {
        try {
            int ano = anoSelecionado();
            String sufixo = (ano > 0 ? " (" + ano + ")" : " (todos)");
            preencher(gPagStatus, dao.pagamentosPorStatus(ano), "Pagamentos por status" + sufixo);
            preencher(gReceitaPlano, dao.receitaPorPlano(ano, 10), "Top 10 planos por receita" + sufixo);
            preencher(gReceitaMes, dao.receitaPorMes(ano), "Receita por mes" + sufixo);
            preencher(gInadimplencia, dao.alunosPorInadimplencia(), "Alunos por faixa de inadimplencia (atual)");
            preencher(gAssinStatus, dao.assinaturasPorStatus(), "Assinaturas por status (atual)");
            preencher(gAlunosStatus, dao.alunosPorStatus(), "Alunos por status (atual)");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** Converte um resultado de 2 colunas (rotulo, valor) e injeta no grafico. */
    private void preencher(JComponent grafico, ResultadoTabela rt, String titulo) {
        int n = rt.getQtdLinhas();
        String[] rotulos = new String[n];
        double[] vals = new double[n];
        for (int i = 0; i < n; i++) {
            Object r = rt.getLinhas().get(i)[0];
            rotulos[i] = r != null ? r.toString() : "";
            vals[i] = rt.comoDouble(i, 1);
        }
        if (grafico instanceof GraficoPizza) ((GraficoPizza) grafico).setDados(titulo, rotulos, vals);
        else if (grafico instanceof GraficoBarras) ((GraficoBarras) grafico).setDados(titulo, rotulos, vals);
        else if (grafico instanceof GraficoLinha) ((GraficoLinha) grafico).setDados(titulo, rotulos, vals);
        else if (grafico instanceof GraficoBarrasHorizontal) ((GraficoBarrasHorizontal) grafico).setDados(titulo, rotulos, vals);
    }

    // ------------------------------------------------------------- helpers
    private String intTxt(String sql, Object... params) throws Exception {
        Object v = dao.valorUnico(sql, params);
        return v != null ? String.valueOf(((Number) v).longValue()) : "0";
    }

    private String moedaTxt(String sql, Object... params) throws Exception {
        return fmt(dao.valorDouble(sql, params));
    }

    private static String fmt(double v) {
        return String.format("%,.2f", v);
    }
}
