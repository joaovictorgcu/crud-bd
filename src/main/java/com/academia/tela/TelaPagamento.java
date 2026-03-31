package com.academia.tela;

import com.academia.dao.PagamentoDAO;
import com.academia.dao.AssinaturaDAO;
import com.academia.modelo.Pagamento;
import com.academia.modelo.Assinatura;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class TelaPagamento extends JPanel {

    private JTable tabela;
    private DefaultTableModel modeloTabela;
    private JTextField txtDtVenc, txtValor;
    private JTextField txtBusca;
    private JComboBox<String> cmbStatus;
    private JComboBox<String> cmbAssinatura;
    private PagamentoDAO dao = new PagamentoDAO();
    private AssinaturaDAO assinaturaDAO = new AssinaturaDAO();
    private DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private List<Assinatura> listaAssinaturas;
    private TableRowSorter<DefaultTableModel> sorter;

    public TelaPagamento() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        modeloTabela = new DefaultTableModel(
                new String[]{"Código", "Vencimento", "Status", "Valor", "Aluno"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabela = new JTable(modeloTabela) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                }
                return c;
            }
        };
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        Tema.estilizarTabela(tabela);

        sorter = new TableRowSorter<>(modeloTabela);
        tabela.setRowSorter(sorter);

        JPanel painelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblBusca = new JLabel("Buscar:");
        lblBusca.setFont(Tema.CORPO);
        painelBusca.add(lblBusca);
        txtBusca = new JTextField(20);
        txtBusca.setFont(Tema.CORPO);
        painelBusca.add(txtBusca);
        txtBusca.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
            private void filtrar() {
                String texto = txtBusca.getText().trim();
                if (texto.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(texto)));
                }
            }
        });

        add(painelBusca, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        JPanel painelForm = new JPanel(new GridBagLayout());
        painelForm.setBorder(BorderFactory.createTitledBorder("Dados do Pagamento"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        painelForm.add(new JLabel("Assinatura:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.gridwidth = 3;
        cmbAssinatura = new JComboBox<>();
        painelForm.add(cmbAssinatura, gbc);
        gbc.gridwidth = 1;

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        painelForm.add(new JLabel("Dt. Vencimento:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtDtVenc = new JTextField(10);
        Mascara.data(txtDtVenc);
        painelForm.add(txtDtVenc, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        painelForm.add(new JLabel("Status:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        cmbStatus = new JComboBox<>(new String[]{"PENDENTE", "PAGO", "ATRASADO", "CANCELADO"});
        painelForm.add(cmbStatus, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        painelForm.add(new JLabel("Valor (R$):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtValor = new JTextField(10);
        painelForm.add(txtValor, gbc);

        row++;
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton btnInserir = Tema.botaoPrimario("Inserir");
        JButton btnAlterar = Tema.botaoSecundario("Alterar");
        JButton btnDeletar = Tema.botaoPerigo("Deletar");
        JButton btnLimpar = Tema.botaoSecundario("Limpar");
        JButton btnExportar = Tema.botaoExportar("Exportar CSV");
        painelBotoes.add(btnInserir);
        painelBotoes.add(btnAlterar);
        painelBotoes.add(btnDeletar);
        painelBotoes.add(btnLimpar);
        painelBotoes.add(btnExportar);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 4;
        painelForm.add(painelBotoes, gbc);

        add(painelForm, BorderLayout.SOUTH);

        btnInserir.addActionListener(e -> inserir());
        btnAlterar.addActionListener(e -> alterar());
        btnDeletar.addActionListener(e -> deletar());
        btnLimpar.addActionListener(e -> limparCampos());

        btnExportar.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new java.io.File("pagamentos.csv"));
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(
                        new java.io.FileOutputStream(fc.getSelectedFile()), "UTF-8"))) {
                    pw.print("\uFEFF");
                    for (int i = 0; i < modeloTabela.getColumnCount(); i++) {
                        if (i > 0) pw.print(";");
                        pw.print(modeloTabela.getColumnName(i));
                    }
                    pw.println();
                    for (int r = 0; r < modeloTabela.getRowCount(); r++) {
                        for (int c = 0; c < modeloTabela.getColumnCount(); c++) {
                            if (c > 0) pw.print(";");
                            Object val = modeloTabela.getValueAt(r, c);
                            pw.print(val != null ? val.toString() : "");
                        }
                        pw.println();
                    }
                    JOptionPane.showMessageDialog(this, "Exportado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erro ao exportar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) preencherCampos();
        });

        carregarCombos();
        carregarDados();
    }

    private void carregarCombos() {
        try {
            cmbAssinatura.removeAllItems();
            listaAssinaturas = assinaturaDAO.listarTodas();
            for (Assinatura a : listaAssinaturas) {
                String label = a.getAlunoNome() + " - " + a.getPlanoNome() + " - " +
                        (a.getDtAssinatura() != null ? a.getDtAssinatura().format(fmt) : "");
                cmbAssinatura.addItem(label);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar assinaturas: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarDados() {
        modeloTabela.setRowCount(0);
        try {
            List<Pagamento> lista = dao.listarTodos();
            for (Pagamento p : lista) {
                modeloTabela.addRow(new Object[]{
                    p.getCodPgto(),
                    p.getDtVenc() != null ? p.getDtVenc().format(fmt) : "",
                    p.getStatus(),
                    p.getValor() != null ? "R$ " + String.format("%.2f", p.getValor()) : "",
                    p.getAlunoNome()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar pagamentos: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void preencherCampos() {
        int viewRow = tabela.getSelectedRow();
        if (viewRow >= 0) {
            int row = tabela.convertRowIndexToModel(viewRow);
            txtDtVenc.setText(String.valueOf(modeloTabela.getValueAt(row, 1)));
            cmbStatus.setSelectedItem(modeloTabela.getValueAt(row, 2));
            String valorStr = String.valueOf(modeloTabela.getValueAt(row, 3));
            txtValor.setText(valorStr.replace("R$ ", ""));
        }
    }

    private boolean validarCampos() {
        String dtVenc = txtDtVenc.getText().trim();
        if (!dtVenc.isEmpty()) {
            try {
                LocalDate.parse(dtVenc, fmt);
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Data de vencimento deve estar no formato dd/MM/yyyy!", "Validação", JOptionPane.WARNING_MESSAGE);
                txtDtVenc.requestFocus();
                return false;
            }
        } else {
            JOptionPane.showMessageDialog(this, "Data de vencimento é obrigatória!", "Validação", JOptionPane.WARNING_MESSAGE);
            txtDtVenc.requestFocus();
            return false;
        }
        String valor = txtValor.getText().trim().replace(",", ".");
        if (valor.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Valor é obrigatório!", "Validação", JOptionPane.WARNING_MESSAGE);
            txtValor.requestFocus();
            return false;
        }
        try {
            BigDecimal val = new BigDecimal(valor);
            if (val.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Valor deve ser positivo!", "Validação", JOptionPane.WARNING_MESSAGE);
                txtValor.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Valor deve ser um número válido!", "Validação", JOptionPane.WARNING_MESSAGE);
            txtValor.requestFocus();
            return false;
        }
        if (cmbAssinatura.getSelectedIndex() < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma assinatura!", "Validação", JOptionPane.WARNING_MESSAGE);
            cmbAssinatura.requestFocus();
            return false;
        }
        return true;
    }

    private void inserir() {
        if (!validarCampos()) return;
        try {
            int idx = cmbAssinatura.getSelectedIndex();
            Assinatura ass = listaAssinaturas.get(idx);
            Pagamento p = new Pagamento();
            p.setDtVenc(LocalDate.parse(txtDtVenc.getText().trim(), fmt));
            p.setStatus((String) cmbStatus.getSelectedItem());
            p.setValor(new BigDecimal(txtValor.getText().trim().replace(",", ".")));
            p.setDtAssinatura(ass.getDtAssinatura());
            p.setNroMatric(ass.getNroMatric());
            p.setCodPlano(ass.getCodPlano());
            dao.inserir(p);
            JOptionPane.showMessageDialog(this, "Pagamento inserido com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            carregarDados();
            limparCampos();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao inserir: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void alterar() {
        int viewRow = tabela.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um pagamento na tabela!");
            return;
        }
        if (!validarCampos()) return;
        int row = tabela.convertRowIndexToModel(viewRow);
        try {
            Pagamento p = new Pagamento();
            p.setCodPgto((int) modeloTabela.getValueAt(row, 0));
            p.setDtVenc(LocalDate.parse(txtDtVenc.getText().trim(), fmt));
            p.setStatus((String) cmbStatus.getSelectedItem());
            p.setValor(new BigDecimal(txtValor.getText().trim().replace(",", ".")));

            int idx = cmbAssinatura.getSelectedIndex();
            if (idx >= 0 && listaAssinaturas != null && idx < listaAssinaturas.size()) {
                Assinatura ass = listaAssinaturas.get(idx);
                p.setDtAssinatura(ass.getDtAssinatura());
                p.setNroMatric(ass.getNroMatric());
                p.setCodPlano(ass.getCodPlano());
            }

            dao.atualizar(p);
            JOptionPane.showMessageDialog(this, "Pagamento alterado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            carregarDados();
            limparCampos();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao alterar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletar() {
        int viewRow = tabela.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um pagamento na tabela!");
            return;
        }
        int row = tabela.convertRowIndexToModel(viewRow);
        int codPgto = (int) modeloTabela.getValueAt(row, 0);
        int conf = JOptionPane.showConfirmDialog(this, "Deseja realmente deletar o pagamento código " + codPgto + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            try {
                dao.deletar(codPgto);
                JOptionPane.showMessageDialog(this, "Pagamento deletado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                carregarDados();
                limparCampos();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao deletar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limparCampos() {
        txtDtVenc.setText("");
        txtValor.setText("");
        cmbStatus.setSelectedIndex(0);
        if (cmbAssinatura.getItemCount() > 0) cmbAssinatura.setSelectedIndex(0);
        tabela.clearSelection();
    }
}
