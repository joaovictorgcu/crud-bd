package com.academia.tela;

import com.academia.dao.AlunoDAO;
import com.academia.modelo.Aluno;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TelaAluno extends JPanel {

    private JTable tabela;
    private DefaultTableModel modeloTabela;
    private JTextField txtNome, txtCpf, txtEmail, txtTelefone, txtNascimento, txtRua, txtBairro, txtCep, txtObsSaude;
    private JTextField txtBusca;
    private JComboBox<String> cmbStatus;
    private AlunoDAO dao = new AlunoDAO();
    private DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private TableRowSorter<DefaultTableModel> sorter;

    public TelaAluno() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        modeloTabela = new DefaultTableModel(
                new String[]{"Matrícula", "Nome", "CPF", "Email", "Telefone", "Nascimento", "Rua", "Bairro", "CEP", "Status", "Obs. Saúde", "Cadastro"}, 0) {
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

        // Filtro / busca
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
        painelForm.setBorder(BorderFactory.createTitledBorder("Dados do Aluno"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        painelForm.add(new JLabel("Nome:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtNome = new JTextField(20);
        painelForm.add(txtNome, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        painelForm.add(new JLabel("CPF:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        txtCpf = new JTextField(14);
        Mascara.cpf(txtCpf);
        painelForm.add(txtCpf, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        painelForm.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtEmail = new JTextField(20);
        painelForm.add(txtEmail, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        painelForm.add(new JLabel("Telefone:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        txtTelefone = new JTextField(15);
        Mascara.telefone(txtTelefone);
        painelForm.add(txtTelefone, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        painelForm.add(new JLabel("Nascimento:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtNascimento = new JTextField(10);
        Mascara.data(txtNascimento);
        painelForm.add(txtNascimento, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        painelForm.add(new JLabel("Status:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        cmbStatus = new JComboBox<>(new String[]{"ATIVO", "INATIVO", "SUSPENSO"});
        painelForm.add(cmbStatus, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        painelForm.add(new JLabel("Rua:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtRua = new JTextField(20);
        painelForm.add(txtRua, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        painelForm.add(new JLabel("Bairro:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        txtBairro = new JTextField(15);
        painelForm.add(txtBairro, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        painelForm.add(new JLabel("CEP:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtCep = new JTextField(10);
        Mascara.cep(txtCep);
        painelForm.add(txtCep, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        painelForm.add(new JLabel("Obs. Saúde:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        txtObsSaude = new JTextField(20);
        painelForm.add(txtObsSaude, gbc);

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
            fc.setSelectedFile(new java.io.File("alunos.csv"));
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
            List<Aluno> lista = dao.listarTodos();
            DateTimeFormatter fmtDt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (Aluno a : lista) {
                modeloTabela.addRow(new Object[]{
                    a.getNroMatric(),
                    a.getNome(),
                    a.getCpf(),
                    a.getEmail(),
                    a.getTelefone(),
                    a.getDtNasc() != null ? a.getDtNasc().format(fmt) : "",
                    a.getRua(),
                    a.getBairro(),
                    a.getCep(),
                    a.getStatus(),
                    a.getObsSaude(),
                    a.getDtCadastro() != null ? a.getDtCadastro().format(fmtDt) : ""
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar alunos: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void preencherCampos() {
        int viewRow = tabela.getSelectedRow();
        if (viewRow >= 0) {
            int row = tabela.convertRowIndexToModel(viewRow);
            txtNome.setText(String.valueOf(modeloTabela.getValueAt(row, 1)));
            txtCpf.setText(String.valueOf(modeloTabela.getValueAt(row, 2)));
            txtEmail.setText(String.valueOf(modeloTabela.getValueAt(row, 3)));
            txtTelefone.setText(String.valueOf(modeloTabela.getValueAt(row, 4)));
            txtNascimento.setText(String.valueOf(modeloTabela.getValueAt(row, 5)));
            txtRua.setText(String.valueOf(modeloTabela.getValueAt(row, 6)));
            txtBairro.setText(String.valueOf(modeloTabela.getValueAt(row, 7)));
            txtCep.setText(String.valueOf(modeloTabela.getValueAt(row, 8)));
            cmbStatus.setSelectedItem(modeloTabela.getValueAt(row, 9));
            txtObsSaude.setText(String.valueOf(modeloTabela.getValueAt(row, 10)));
        }
    }

    private boolean validarCampos() {
        if (txtNome.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nome é obrigatório!", "Validação", JOptionPane.WARNING_MESSAGE);
            txtNome.requestFocus();
            return false;
        }
        return true;
    }

    private void inserir() {
        if (!validarCampos()) return;
        try {
            Aluno a = new Aluno();
            a.setNome(txtNome.getText().trim());
            a.setCpf(txtCpf.getText().trim());
            a.setEmail(txtEmail.getText().trim());
            a.setTelefone(txtTelefone.getText().trim());
            if (!txtNascimento.getText().trim().isEmpty())
                a.setDtNasc(LocalDate.parse(txtNascimento.getText().trim(), fmt));
            a.setRua(txtRua.getText().trim());
            a.setBairro(txtBairro.getText().trim());
            a.setCep(txtCep.getText().trim());
            a.setStatus((String) cmbStatus.getSelectedItem());
            a.setObsSaude(txtObsSaude.getText().trim());
            dao.inserir(a);
            JOptionPane.showMessageDialog(this, "Aluno inserido com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            carregarDados();
            limparCampos();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao inserir: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void alterar() {
        int viewRow = tabela.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um aluno na tabela!");
            return;
        }
        if (!validarCampos()) return;
        int row = tabela.convertRowIndexToModel(viewRow);
        try {
            Aluno a = new Aluno();
            a.setNroMatric((int) modeloTabela.getValueAt(row, 0));
            a.setNome(txtNome.getText().trim());
            a.setCpf(String.valueOf(modeloTabela.getValueAt(row, 2)));
            a.setEmail(txtEmail.getText().trim());
            a.setTelefone(txtTelefone.getText().trim());
            if (!txtNascimento.getText().trim().isEmpty())
                a.setDtNasc(LocalDate.parse(txtNascimento.getText().trim(), fmt));
            a.setRua(txtRua.getText().trim());
            a.setBairro(txtBairro.getText().trim());
            a.setCep(txtCep.getText().trim());
            a.setStatus((String) cmbStatus.getSelectedItem());
            a.setObsSaude(txtObsSaude.getText().trim());
            dao.atualizar(a);
            JOptionPane.showMessageDialog(this, "Aluno alterado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            carregarDados();
            limparCampos();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao alterar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletar() {
        int viewRow = tabela.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um aluno na tabela!");
            return;
        }
        int row = tabela.convertRowIndexToModel(viewRow);
        int nroMatric = (int) modeloTabela.getValueAt(row, 0);
        int conf = JOptionPane.showConfirmDialog(this, "Deseja realmente deletar o aluno matrícula " + nroMatric + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            try {
                dao.deletar(nroMatric);
                JOptionPane.showMessageDialog(this, "Aluno deletado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                carregarDados();
                limparCampos();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao deletar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limparCampos() {
        txtNome.setText("");
        txtCpf.setText("");
        txtEmail.setText("");
        txtTelefone.setText("");
        txtNascimento.setText("");
        txtRua.setText("");
        txtBairro.setText("");
        txtCep.setText("");
        cmbStatus.setSelectedIndex(0);
        txtObsSaude.setText("");
        tabela.clearSelection();
    }
}
