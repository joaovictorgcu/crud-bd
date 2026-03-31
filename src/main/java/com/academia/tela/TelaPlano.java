package com.academia.tela;

import com.academia.dao.PlanoDAO;
import com.academia.modelo.Plano;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class TelaPlano extends JPanel {

    private JTable tabela;
    private DefaultTableModel modeloTabela;
    private JTextField txtNome, txtDuracao, txtValor;
    private JTextField txtBusca;
    private PlanoDAO dao = new PlanoDAO();
    private TableRowSorter<DefaultTableModel> sorter;

    public TelaPlano() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        modeloTabela = new DefaultTableModel(
                new String[]{"Código", "Nome", "Duração (meses)", "Valor Mensal"}, 0) {
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
        painelForm.setBorder(BorderFactory.createTitledBorder("Dados do Plano"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        painelForm.add(new JLabel("Nome:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtNome = new JTextField(20);
        painelForm.add(txtNome, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        painelForm.add(new JLabel("Duração (meses):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtDuracao = new JTextField(5);
        painelForm.add(txtDuracao, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        painelForm.add(new JLabel("Valor Mensal (R$):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtValor = new JTextField(10);
        painelForm.add(txtValor, gbc);

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

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        painelForm.add(painelBotoes, gbc);

        add(painelForm, BorderLayout.SOUTH);

        btnInserir.addActionListener(e -> inserir());
        btnAlterar.addActionListener(e -> alterar());
        btnDeletar.addActionListener(e -> deletar());
        btnLimpar.addActionListener(e -> limparCampos());

        btnExportar.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new java.io.File("planos.csv"));
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

        carregarDados();
    }

    private void carregarDados() {
        modeloTabela.setRowCount(0);
        try {
            List<Plano> lista = dao.listarTodos();
            for (Plano p : lista) {
                modeloTabela.addRow(new Object[]{
                    p.getCodPlano(),
                    p.getNome(),
                    p.getDuracao(),
                    p.getValorMes() != null ? "R$ " + String.format("%.2f", p.getValorMes()) : ""
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar planos: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void preencherCampos() {
        int viewRow = tabela.getSelectedRow();
        if (viewRow >= 0) {
            int row = tabela.convertRowIndexToModel(viewRow);
            txtNome.setText(String.valueOf(modeloTabela.getValueAt(row, 1)));
            txtDuracao.setText(String.valueOf(modeloTabela.getValueAt(row, 2)));
            String valorStr = String.valueOf(modeloTabela.getValueAt(row, 3));
            txtValor.setText(valorStr.replace("R$ ", ""));
        }
    }

    private boolean validarCampos() {
        if (txtNome.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nome é obrigatório!", "Validação", JOptionPane.WARNING_MESSAGE);
            txtNome.requestFocus();
            return false;
        }
        String duracao = txtDuracao.getText().trim();
        try {
            int dur = Integer.parseInt(duracao);
            if (dur <= 0) {
                JOptionPane.showMessageDialog(this, "Duração deve ser um número inteiro positivo!", "Validação", JOptionPane.WARNING_MESSAGE);
                txtDuracao.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Duração deve ser um número inteiro válido!", "Validação", JOptionPane.WARNING_MESSAGE);
            txtDuracao.requestFocus();
            return false;
        }
        String valor = txtValor.getText().trim().replace(",", ".");
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
        return true;
    }

    private void inserir() {
        if (!validarCampos()) return;
        try {
            Plano p = new Plano();
            p.setNome(txtNome.getText().trim());
            p.setDuracao(Integer.parseInt(txtDuracao.getText().trim()));
            p.setValorMes(new BigDecimal(txtValor.getText().trim().replace(",", ".")));
            dao.inserir(p);
            JOptionPane.showMessageDialog(this, "Plano inserido com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            carregarDados();
            limparCampos();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao inserir: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void alterar() {
        int viewRow = tabela.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um plano na tabela!");
            return;
        }
        if (!validarCampos()) return;
        int row = tabela.convertRowIndexToModel(viewRow);
        try {
            Plano p = new Plano();
            p.setCodPlano((int) modeloTabela.getValueAt(row, 0));
            p.setNome(txtNome.getText().trim());
            p.setDuracao(Integer.parseInt(txtDuracao.getText().trim()));
            p.setValorMes(new BigDecimal(txtValor.getText().trim().replace(",", ".")));
            dao.atualizar(p);
            JOptionPane.showMessageDialog(this, "Plano alterado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            carregarDados();
            limparCampos();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao alterar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletar() {
        int viewRow = tabela.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um plano na tabela!");
            return;
        }
        int row = tabela.convertRowIndexToModel(viewRow);
        int codPlano = (int) modeloTabela.getValueAt(row, 0);
        int conf = JOptionPane.showConfirmDialog(this, "Deseja realmente deletar o plano código " + codPlano + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            try {
                dao.deletar(codPlano);
                JOptionPane.showMessageDialog(this, "Plano deletado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                carregarDados();
                limparCampos();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao deletar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limparCampos() {
        txtNome.setText("");
        txtDuracao.setText("");
        txtValor.setText("");
        tabela.clearSelection();
    }
}
