package com.academia.tela;

import com.academia.dao.InstrutorDAO;
import com.academia.modelo.Instrutor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TelaInstrutor extends JPanel {

    private JTable tabela;
    private DefaultTableModel modeloTabela;
    private JTextField txtCref, txtNome, txtCpf, txtEmail, txtSalario, txtAdmissao, txtCrefSupervisor, txtRua, txtBairro, txtCep;
    private JTextField txtBusca;
    private InstrutorDAO dao = new InstrutorDAO();
    private DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private TableRowSorter<DefaultTableModel> sorter;

    public TelaInstrutor() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        modeloTabela = new DefaultTableModel(
                new String[]{"CREF", "Nome", "CPF", "Email", "Salário", "Admissão", "Supervisor", "Rua", "Bairro", "CEP"}, 0) {
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
        painelForm.setBorder(BorderFactory.createTitledBorder("Dados do Instrutor"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        painelForm.add(new JLabel("CREF:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtCref = new JTextField(15);
        painelForm.add(txtCref, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        painelForm.add(new JLabel("Nome:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        txtNome = new JTextField(20);
        painelForm.add(txtNome, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        painelForm.add(new JLabel("CPF:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtCpf = new JTextField(14);
        Mascara.cpf(txtCpf);
        painelForm.add(txtCpf, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        painelForm.add(new JLabel("Email:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        txtEmail = new JTextField(20);
        painelForm.add(txtEmail, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        painelForm.add(new JLabel("Salário:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtSalario = new JTextField(10);
        painelForm.add(txtSalario, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        painelForm.add(new JLabel("Admissão:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        txtAdmissao = new JTextField(10);
        Mascara.data(txtAdmissao);
        painelForm.add(txtAdmissao, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        painelForm.add(new JLabel("CREF Supervisor:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtCrefSupervisor = new JTextField(15);
        painelForm.add(txtCrefSupervisor, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        painelForm.add(new JLabel("Rua:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        txtRua = new JTextField(20);
        painelForm.add(txtRua, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        painelForm.add(new JLabel("Bairro:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtBairro = new JTextField(15);
        painelForm.add(txtBairro, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        painelForm.add(new JLabel("CEP:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        txtCep = new JTextField(10);
        Mascara.cep(txtCep);
        painelForm.add(txtCep, gbc);

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
            fc.setSelectedFile(new java.io.File("instrutores.csv"));
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
            List<Instrutor> lista = dao.listarTodos();
            for (Instrutor i : lista) {
                modeloTabela.addRow(new Object[]{
                    i.getCref(),
                    i.getNome(),
                    i.getCpf(),
                    i.getEmail(),
                    i.getSalario() != null ? "R$ " + String.format("%.2f", i.getSalario()) : "",
                    i.getDtAdmissao() != null ? i.getDtAdmissao().format(fmt) : "",
                    i.getCrefSupervisor() != null ? i.getCrefSupervisor() : "",
                    i.getRua(),
                    i.getBairro(),
                    i.getCep()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar instrutores: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void preencherCampos() {
        int viewRow = tabela.getSelectedRow();
        if (viewRow >= 0) {
            int row = tabela.convertRowIndexToModel(viewRow);
            txtCref.setText(String.valueOf(modeloTabela.getValueAt(row, 0)));
            txtNome.setText(String.valueOf(modeloTabela.getValueAt(row, 1)));
            txtCpf.setText(String.valueOf(modeloTabela.getValueAt(row, 2)));
            txtEmail.setText(String.valueOf(modeloTabela.getValueAt(row, 3)));
            String salStr = String.valueOf(modeloTabela.getValueAt(row, 4));
            txtSalario.setText(salStr.replace("R$ ", ""));
            txtAdmissao.setText(String.valueOf(modeloTabela.getValueAt(row, 5)));
            txtCrefSupervisor.setText(String.valueOf(modeloTabela.getValueAt(row, 6)));
            txtRua.setText(String.valueOf(modeloTabela.getValueAt(row, 7)));
            txtBairro.setText(String.valueOf(modeloTabela.getValueAt(row, 8)));
            txtCep.setText(String.valueOf(modeloTabela.getValueAt(row, 9)));
        }
    }

    private boolean validarCampos() {
        if (txtCref.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "CREF é obrigatório!", "Validação", JOptionPane.WARNING_MESSAGE);
            txtCref.requestFocus();
            return false;
        }
        if (txtNome.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nome é obrigatório!", "Validação", JOptionPane.WARNING_MESSAGE);
            txtNome.requestFocus();
            return false;
        }
        String salario = txtSalario.getText().trim().replace(",", ".");
        if (!salario.isEmpty()) {
            try {
                BigDecimal val = new BigDecimal(salario);
                if (val.compareTo(BigDecimal.ZERO) <= 0) {
                    JOptionPane.showMessageDialog(this, "Salário deve ser um valor positivo!", "Validação", JOptionPane.WARNING_MESSAGE);
                    txtSalario.requestFocus();
                    return false;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Salário deve ser um número válido!", "Validação", JOptionPane.WARNING_MESSAGE);
                txtSalario.requestFocus();
                return false;
            }
        }
        return true;
    }

    private void inserir() {
        if (!validarCampos()) return;
        try {
            Instrutor i = new Instrutor();
            i.setCref(txtCref.getText().trim());
            i.setNome(txtNome.getText().trim());
            i.setCpf(txtCpf.getText().trim());
            i.setEmail(txtEmail.getText().trim());
            if (!txtSalario.getText().trim().isEmpty())
                i.setSalario(new BigDecimal(txtSalario.getText().trim().replace(",", ".")));
            if (!txtAdmissao.getText().trim().isEmpty())
                i.setDtAdmissao(LocalDate.parse(txtAdmissao.getText().trim(), fmt));
            i.setCrefSupervisor(txtCrefSupervisor.getText().trim().isEmpty() ? null : txtCrefSupervisor.getText().trim());
            i.setRua(txtRua.getText().trim());
            i.setBairro(txtBairro.getText().trim());
            i.setCep(txtCep.getText().trim());
            dao.inserir(i);
            JOptionPane.showMessageDialog(this, "Instrutor inserido com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            carregarDados();
            limparCampos();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao inserir: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void alterar() {
        int viewRow = tabela.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um instrutor na tabela!");
            return;
        }
        if (!validarCampos()) return;
        int row = tabela.convertRowIndexToModel(viewRow);
        try {
            Instrutor i = new Instrutor();
            i.setCref(String.valueOf(modeloTabela.getValueAt(row, 0)));
            i.setNome(txtNome.getText().trim());
            i.setCpf(txtCpf.getText().trim());
            i.setEmail(txtEmail.getText().trim());
            if (!txtSalario.getText().trim().isEmpty())
                i.setSalario(new BigDecimal(txtSalario.getText().trim().replace(",", ".")));
            if (!txtAdmissao.getText().trim().isEmpty())
                i.setDtAdmissao(LocalDate.parse(txtAdmissao.getText().trim(), fmt));
            i.setCrefSupervisor(txtCrefSupervisor.getText().trim().isEmpty() ? null : txtCrefSupervisor.getText().trim());
            i.setRua(txtRua.getText().trim());
            i.setBairro(txtBairro.getText().trim());
            i.setCep(txtCep.getText().trim());
            dao.atualizar(i);
            JOptionPane.showMessageDialog(this, "Instrutor alterado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            carregarDados();
            limparCampos();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao alterar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletar() {
        int viewRow = tabela.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um instrutor na tabela!");
            return;
        }
        int row = tabela.convertRowIndexToModel(viewRow);
        String cref = String.valueOf(modeloTabela.getValueAt(row, 0));
        int conf = JOptionPane.showConfirmDialog(this, "Deseja realmente deletar o instrutor CREF " + cref + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            try {
                dao.deletar(cref);
                JOptionPane.showMessageDialog(this, "Instrutor deletado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                carregarDados();
                limparCampos();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao deletar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limparCampos() {
        txtCref.setText("");
        txtNome.setText("");
        txtCpf.setText("");
        txtEmail.setText("");
        txtSalario.setText("");
        txtAdmissao.setText("");
        txtCrefSupervisor.setText("");
        txtRua.setText("");
        txtBairro.setText("");
        txtCep.setText("");
        tabela.clearSelection();
    }
}
