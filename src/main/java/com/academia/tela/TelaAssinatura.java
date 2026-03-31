package com.academia.tela;

import com.academia.dao.AssinaturaDAO;
import com.academia.dao.AlunoDAO;
import com.academia.dao.PlanoDAO;
import com.academia.modelo.Assinatura;
import com.academia.modelo.Aluno;
import com.academia.modelo.Plano;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class TelaAssinatura extends JPanel {

    private JTable tabela;
    private DefaultTableModel modeloTabela;
    private JTextField txtDtAssinatura, txtDtInicio, txtDtFim;
    private JTextField txtBusca;
    private JComboBox<Aluno> cmbAluno;
    private JComboBox<Plano> cmbPlano;
    private JComboBox<String> cmbStatus;
    private AssinaturaDAO dao = new AssinaturaDAO();
    private AlunoDAO alunoDAO = new AlunoDAO();
    private PlanoDAO planoDAO = new PlanoDAO();
    private DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private List<Assinatura> listaAssinaturas;
    private TableRowSorter<DefaultTableModel> sorter;

    public TelaAssinatura() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        modeloTabela = new DefaultTableModel(
                new String[]{"Dt. Assinatura", "Aluno", "Plano", "Dt. Início", "Dt. Fim", "Status"}, 0) {
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
        painelForm.setBorder(BorderFactory.createTitledBorder("Dados da Assinatura"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        painelForm.add(new JLabel("Dt. Assinatura:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtDtAssinatura = new JTextField(10);
        Mascara.data(txtDtAssinatura);
        painelForm.add(txtDtAssinatura, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        painelForm.add(new JLabel("Aluno:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        cmbAluno = new JComboBox<>();
        painelForm.add(cmbAluno, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        painelForm.add(new JLabel("Plano:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        cmbPlano = new JComboBox<>();
        painelForm.add(cmbPlano, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        painelForm.add(new JLabel("Status:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        cmbStatus = new JComboBox<>(new String[]{"ATIVA", "CANCELADA", "VENCIDA"});
        painelForm.add(cmbStatus, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        painelForm.add(new JLabel("Dt. Início:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtDtInicio = new JTextField(10);
        Mascara.data(txtDtInicio);
        painelForm.add(txtDtInicio, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        painelForm.add(new JLabel("Dt. Fim:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        txtDtFim = new JTextField(10);
        Mascara.data(txtDtFim);
        painelForm.add(txtDtFim, gbc);

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
            fc.setSelectedFile(new java.io.File("assinaturas.csv"));
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
            cmbAluno.removeAllItems();
            List<Aluno> alunos = alunoDAO.listarTodos();
            for (Aluno a : alunos) {
                cmbAluno.addItem(a);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar alunos: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
        try {
            cmbPlano.removeAllItems();
            List<Plano> planos = planoDAO.listarTodos();
            for (Plano p : planos) {
                cmbPlano.addItem(p);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar planos: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarDados() {
        modeloTabela.setRowCount(0);
        try {
            listaAssinaturas = dao.listarTodas();
            for (Assinatura a : listaAssinaturas) {
                modeloTabela.addRow(new Object[]{
                    a.getDtAssinatura() != null ? a.getDtAssinatura().format(fmt) : "",
                    a.getAlunoNome(),
                    a.getPlanoNome(),
                    a.getDtInicio() != null ? a.getDtInicio().format(fmt) : "",
                    a.getDtFim() != null ? a.getDtFim().format(fmt) : "",
                    a.getStatus()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar assinaturas: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void preencherCampos() {
        int viewRow = tabela.getSelectedRow();
        if (viewRow >= 0) {
            int row = tabela.convertRowIndexToModel(viewRow);
            if (listaAssinaturas != null && row < listaAssinaturas.size()) {
                Assinatura a = listaAssinaturas.get(row);
                txtDtAssinatura.setText(a.getDtAssinatura() != null ? a.getDtAssinatura().format(fmt) : "");
                txtDtInicio.setText(a.getDtInicio() != null ? a.getDtInicio().format(fmt) : "");
                txtDtFim.setText(a.getDtFim() != null ? a.getDtFim().format(fmt) : "");
                cmbStatus.setSelectedItem(a.getStatus());

                for (int i = 0; i < cmbAluno.getItemCount(); i++) {
                    if (cmbAluno.getItemAt(i).getNroMatric() == a.getNroMatric()) {
                        cmbAluno.setSelectedIndex(i);
                        break;
                    }
                }
                for (int i = 0; i < cmbPlano.getItemCount(); i++) {
                    if (cmbPlano.getItemAt(i).getCodPlano() == a.getCodPlano()) {
                        cmbPlano.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
    }

    private boolean validarCampos() {
        String dtAss = txtDtAssinatura.getText().trim();
        if (!dtAss.isEmpty()) {
            try {
                LocalDate.parse(dtAss, fmt);
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Data da assinatura deve estar no formato dd/MM/yyyy!", "Validação", JOptionPane.WARNING_MESSAGE);
                txtDtAssinatura.requestFocus();
                return false;
            }
        } else {
            JOptionPane.showMessageDialog(this, "Data da assinatura é obrigatória!", "Validação", JOptionPane.WARNING_MESSAGE);
            txtDtAssinatura.requestFocus();
            return false;
        }
        if (cmbAluno.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Selecione um aluno!", "Validação", JOptionPane.WARNING_MESSAGE);
            cmbAluno.requestFocus();
            return false;
        }
        if (cmbPlano.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Selecione um plano!", "Validação", JOptionPane.WARNING_MESSAGE);
            cmbPlano.requestFocus();
            return false;
        }
        String dtInicio = txtDtInicio.getText().trim();
        if (!dtInicio.isEmpty()) {
            try {
                LocalDate.parse(dtInicio, fmt);
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Data de início deve estar no formato dd/MM/yyyy!", "Validação", JOptionPane.WARNING_MESSAGE);
                txtDtInicio.requestFocus();
                return false;
            }
        }
        String dtFim = txtDtFim.getText().trim();
        if (!dtFim.isEmpty()) {
            try {
                LocalDate.parse(dtFim, fmt);
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Data de fim deve estar no formato dd/MM/yyyy!", "Validação", JOptionPane.WARNING_MESSAGE);
                txtDtFim.requestFocus();
                return false;
            }
        }
        return true;
    }

    private void inserir() {
        if (!validarCampos()) return;
        try {
            Assinatura a = new Assinatura();
            a.setDtAssinatura(LocalDate.parse(txtDtAssinatura.getText().trim(), fmt));
            Aluno alunoSel = (Aluno) cmbAluno.getSelectedItem();
            Plano planoSel = (Plano) cmbPlano.getSelectedItem();
            a.setNroMatric(alunoSel.getNroMatric());
            a.setCodPlano(planoSel.getCodPlano());
            if (!txtDtInicio.getText().trim().isEmpty())
                a.setDtInicio(LocalDate.parse(txtDtInicio.getText().trim(), fmt));
            if (!txtDtFim.getText().trim().isEmpty())
                a.setDtFim(LocalDate.parse(txtDtFim.getText().trim(), fmt));
            a.setStatus((String) cmbStatus.getSelectedItem());
            dao.inserir(a);
            JOptionPane.showMessageDialog(this, "Assinatura inserida com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            carregarDados();
            limparCampos();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao inserir: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void alterar() {
        int viewRow = tabela.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma assinatura na tabela!");
            return;
        }
        if (!validarCampos()) return;
        int row = tabela.convertRowIndexToModel(viewRow);
        if (listaAssinaturas == null || row >= listaAssinaturas.size()) {
            JOptionPane.showMessageDialog(this, "Selecione uma assinatura na tabela!");
            return;
        }
        try {
            Assinatura original = listaAssinaturas.get(row);
            Assinatura a = new Assinatura();
            a.setDtAssinatura(original.getDtAssinatura());
            a.setNroMatric(original.getNroMatric());
            a.setCodPlano(original.getCodPlano());
            if (!txtDtInicio.getText().trim().isEmpty())
                a.setDtInicio(LocalDate.parse(txtDtInicio.getText().trim(), fmt));
            if (!txtDtFim.getText().trim().isEmpty())
                a.setDtFim(LocalDate.parse(txtDtFim.getText().trim(), fmt));
            a.setStatus((String) cmbStatus.getSelectedItem());
            dao.atualizar(a);
            JOptionPane.showMessageDialog(this, "Assinatura alterada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            carregarDados();
            limparCampos();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao alterar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletar() {
        int viewRow = tabela.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma assinatura na tabela!");
            return;
        }
        int row = tabela.convertRowIndexToModel(viewRow);
        if (listaAssinaturas == null || row >= listaAssinaturas.size()) {
            JOptionPane.showMessageDialog(this, "Selecione uma assinatura na tabela!");
            return;
        }
        Assinatura a = listaAssinaturas.get(row);
        int conf = JOptionPane.showConfirmDialog(this, "Deseja realmente deletar esta assinatura?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            try {
                dao.deletar(a.getDtAssinatura(), a.getNroMatric(), a.getCodPlano());
                JOptionPane.showMessageDialog(this, "Assinatura deletada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                carregarDados();
                limparCampos();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao deletar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limparCampos() {
        txtDtAssinatura.setText("");
        txtDtInicio.setText("");
        txtDtFim.setText("");
        cmbStatus.setSelectedIndex(0);
        if (cmbAluno.getItemCount() > 0) cmbAluno.setSelectedIndex(0);
        if (cmbPlano.getItemCount() > 0) cmbPlano.setSelectedIndex(0);
        tabela.clearSelection();
    }
}
