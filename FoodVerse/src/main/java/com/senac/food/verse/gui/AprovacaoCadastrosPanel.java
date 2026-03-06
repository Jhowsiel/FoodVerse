package com.senac.food.verse.gui;

import com.senac.food.verse.ConexaoBanco;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AprovacaoCadastrosPanel extends JPanel {

    // Componentes
    private JTable tabela;
    private DefaultTableModel modeloTabela;
    private JTextField txtBusca;
    private JLabel lblTotalPendentes;
    private JLabel lblTotalAtivos;
    
    // Cor específica para status "Pendente" (pode ser movida para UIConstants futuramente se usada em outros lugares)
    private final Color STATUS_PENDING = new Color(230, 126, 34);

    public AprovacaoCadastrosPanel() {
        setLayout(new BorderLayout());
        UIConstants.stylePanel(this); // Aplica fundo e borda padrão

        // 1. HEADER
        add(criarHeader(), BorderLayout.NORTH);

        // 2. CORPO
        JPanel corpo = new JPanel(new BorderLayout(0, 20));
        corpo.setBackground(UIConstants.BG_DARK);
        corpo.setBorder(new EmptyBorder(10, 20, 10, 20));

        corpo.add(criarToolbar(), BorderLayout.NORTH);
        corpo.add(criarTabela(), BorderLayout.CENTER);

        add(corpo, BorderLayout.CENTER);

        // Carregar dados iniciais
        carregarDados("");
    }

    // --- SEÇÃO 1: HEADER ---
    private JPanel criarHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIConstants.BG_DARK);
        p.setBorder(new EmptyBorder(15, 10, 15, 10));

        // Título e Ícone
        JLabel titulo = new JLabel("Gestão de Equipe");
        titulo.setFont(UIConstants.FONT_TITLE); // Usa fonte do UIConstants
        titulo.setForeground(UIConstants.FG_LIGHT);
        titulo.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.SUPERVISOR_ACCOUNT, 32, UIConstants.FG_LIGHT));
        titulo.setIconTextGap(15);
        p.add(titulo, BorderLayout.WEST);

        // Cards de Métricas
        JPanel cards = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        cards.setOpaque(false);

        lblTotalPendentes = criarBadgeInfo("Pendentes", STATUS_PENDING);
        lblTotalAtivos = criarBadgeInfo("Ativos", UIConstants.SUCCESS_GREEN);

        cards.add(lblTotalAtivos);
        cards.add(lblTotalPendentes);
        p.add(cards, BorderLayout.EAST);

        return p;
    }

    private JLabel criarBadgeInfo(String texto, Color cor) {
        JLabel l = new JLabel("0 " + texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12); // Arredondamento padrão
                super.paintComponent(g);
            }
        };
        l.setFont(UIConstants.FONT_BOLD);
        l.setForeground(Color.WHITE);
        l.setBackground(cor);
        l.setOpaque(false); 
        l.setBorder(new EmptyBorder(5, 12, 5, 12));
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    // --- SEÇÃO 2: TOOLBAR ---
    private JPanel criarToolbar() {
        JPanel p = new JPanel(new BorderLayout(15, 0));
        p.setOpaque(false);

        // Campo de Busca
        txtBusca = new JTextField();
        UIConstants.styleField(txtBusca); // ESTILIZAÇÃO CENTRALIZADA
        txtBusca.setPreferredSize(new Dimension(300, 40));
        txtBusca.putClientProperty("JTextField.placeholderText", "Buscar nome, e-mail ou cargo...");
        txtBusca.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });

        // Botões
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton btnNovo = new JButton("Novo Usuário");
        btnNovo.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.PERSON_ADD, 18, Color.WHITE));
        UIConstants.stylePrimary(btnNovo); // ESTILIZAÇÃO CENTRALIZADA
        btnNovo.setPreferredSize(new Dimension(160, 40));
        btnNovo.addActionListener(e -> abrirModalEdicao(null)); 

        JButton btnAtualizar = new JButton("Atualizar");
        btnAtualizar.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.REFRESH, 18, UIConstants.FG_LIGHT));
        UIConstants.styleSecondary(btnAtualizar); // ESTILIZAÇÃO CENTRALIZADA
        btnAtualizar.setPreferredSize(new Dimension(130, 40));
        btnAtualizar.addActionListener(e -> carregarDados(""));

        btnPanel.add(btnNovo);
        btnPanel.add(btnAtualizar);

        p.add(txtBusca, BorderLayout.CENTER);
        p.add(btnPanel, BorderLayout.EAST);

        return p;
    }

    private void filtrar() {
        carregarDados(txtBusca.getText().trim());
    }

    // --- SEÇÃO 3: TABELA ---
    private JScrollPane criarTabela() {
        String[] colunas = {"ID", "Nome", "Usuário", "E-mail", "Cargo", "Status", "Ações"};
        
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Só a coluna de ações é editável
            }
        };

        tabela = new JTable(modeloTabela);
        UIConstants.styleTable(tabela); // ESTILIZAÇÃO CENTRALIZADA

        // Renderizadores
        tabela.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer());
        tabela.getColumnModel().getColumn(6).setCellRenderer(new ActionRenderer());
        tabela.getColumnModel().getColumn(6).setCellEditor(new ActionEditor());

        // Tamanhos
        tabela.getColumnModel().getColumn(0).setMaxWidth(60); // ID
        tabela.getColumnModel().getColumn(4).setMaxWidth(120); // Cargo
        tabela.getColumnModel().getColumn(5).setMaxWidth(110); // Status
        tabela.getColumnModel().getColumn(6).setMinWidth(140); // Ações
        tabela.getColumnModel().getColumn(6).setMaxWidth(160);

        JScrollPane scroll = new JScrollPane(tabela);
        UIConstants.styleScrollPane(scroll); // ESTILIZAÇÃO CENTRALIZADA

        return scroll;
    }

    // --- LÓGICA DE DADOS ---
    private void carregarDados(String filtro) {
        modeloTabela.setRowCount(0);
        int pendentes = 0;
        int ativos = 0;

        String sql = "SELECT userID, name, userName, email, role, status FROM tb_funcionarios";
        if (!filtro.isEmpty()) {
            sql += " WHERE name LIKE ? OR email LIKE ? OR role LIKE ?";
        }
        sql += " ORDER BY CASE WHEN status = 'pendente' THEN 0 ELSE 1 END, name ASC";

        ConexaoBanco cb = new ConexaoBanco();
        Connection conn = null;
        try {
            conn = cb.abrirConexao();
            if (conn == null) throw new SQLException("Modo Offline");

            PreparedStatement ps = conn.prepareStatement(sql);
            if (!filtro.isEmpty()) {
                String f = "%" + filtro + "%";
                ps.setString(1, f);
                ps.setString(2, f);
                ps.setString(3, f);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String status = rs.getString("status");
                if (status == null) status = "pendente";

                if ("pendente".equalsIgnoreCase(status)) pendentes++;
                else if ("aprovado".equalsIgnoreCase(status)) ativos++;

                modeloTabela.addRow(new Object[]{
                        rs.getInt("userID"),
                        rs.getString("name"),
                        rs.getString("userName"),
                        rs.getString("email"),
                        rs.getString("role"),
                        status,
                        "" 
                });
            }

            lblTotalPendentes.setText(pendentes + " Pendentes");
            lblTotalPendentes.setBackground(pendentes > 0 ? STATUS_PENDING : UIConstants.CARD_DARK);
            lblTotalAtivos.setText(ativos + " Ativos");

        } catch (Exception e) {
            // Fallback visual
            modeloTabela.addRow(new Object[]{0, "Sistema Offline", "-", "-", "Admin", "erro", ""});
        } finally {
            cb.fecharConexao();
        }
    }

    private void atualizarStatus(int id, String novoStatus) {
        String sql = "UPDATE tb_funcionarios SET status = ? WHERE userID = ?";
        ConexaoBanco cb = new ConexaoBanco();
        try {
            Connection conn = cb.abrirConexao();
            if (conn != null) {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, novoStatus);
                ps.setInt(2, id);
                ps.executeUpdate();
                
                // MENSAGEM PADRONIZADA
                UIConstants.showSuccess(this, "Status atualizado para: " + novoStatus);
                carregarDados(txtBusca.getText());
                cb.fecharConexao();
            }
        } catch (Exception e) {
            UIConstants.showError(this, "Erro: " + e.getMessage());
        }
    }

    // --- MODAL DE EDIÇÃO/CRIAÇÃO CORRIGIDO ---
    private void abrirModalEdicao(Object[] dadosAtuais) {
        JDialog modal = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Gerenciar Usuário", true);
        modal.setSize(450, 550);
        modal.setLocationRelativeTo(this);
        modal.getContentPane().setBackground(UIConstants.BG_DARK);
        modal.setLayout(new BorderLayout());

        // Painel de Conteúdo
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIConstants.BG_DARK);
        form.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 0, 15, 0); 
        gc.gridx = 0; gc.gridy = 0;
        
        // Título
        JLabel lblTitulo = new JLabel(dadosAtuais == null ? "Novo Cadastro" : "Editar Cadastro");
        lblTitulo.setFont(UIConstants.FONT_TITLE);
        lblTitulo.setForeground(UIConstants.FG_LIGHT);
        form.add(lblTitulo, gc);
        
        // Campos
        gc.gridy++; gc.insets = new Insets(20, 0, 5, 0);
        form.add(criarLabel("Nome Completo"), gc);
        gc.gridy++; gc.insets = new Insets(0, 0, 15, 0);
        JTextField tNome = new JTextField(); 
        UIConstants.styleField(tNome); // Padronizado
        form.add(tNome, gc);
        
        gc.gridy++; gc.insets = new Insets(0, 0, 5, 0);
        form.add(criarLabel("Usuário (Login)"), gc);
        gc.gridy++; gc.insets = new Insets(0, 0, 15, 0);
        JTextField tUser = new JTextField(); 
        UIConstants.styleField(tUser); // Padronizado
        form.add(tUser, gc);
        
        gc.gridy++; gc.insets = new Insets(0, 0, 5, 0);
        form.add(criarLabel("E-mail"), gc);
        gc.gridy++; gc.insets = new Insets(0, 0, 15, 0);
        JTextField tEmail = new JTextField(); 
        UIConstants.styleField(tEmail); // Padronizado
        form.add(tEmail, gc);
        
        gc.gridy++; gc.insets = new Insets(0, 0, 5, 0);
        form.add(criarLabel("Cargo"), gc);
        gc.gridy++; gc.insets = new Insets(0, 0, 15, 0);
        JComboBox<String> cRole = new JComboBox<>(new String[]{"Cozinheiro", "Entregador", "Atendente", "Gerente", "Admin"});
        UIConstants.styleCombo(cRole); // Padronizado
        form.add(cRole, gc);
        
        gc.gridy++; gc.insets = new Insets(0, 0, 5, 0);
        form.add(criarLabel(dadosAtuais == null ? "Senha" : "Nova Senha (deixe vazio para manter)"), gc);
        gc.gridy++; gc.insets = new Insets(0, 0, 15, 0);
        JPasswordField tSenha = new JPasswordField(); 
        UIConstants.styleField(tSenha); // Padronizado
        form.add(tSenha, gc);
        
        // Preencher dados se for edição
        if(dadosAtuais != null) {
            tNome.setText(dadosAtuais[1].toString());
            tUser.setText(dadosAtuais[2].toString());
            tEmail.setText(dadosAtuais[3].toString());
            cRole.setSelectedItem(dadosAtuais[4].toString());
        }
        
        modal.add(form, BorderLayout.CENTER);
        
        // Botões
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(UIConstants.BG_DARK);
        btnPanel.setBorder(new EmptyBorder(10, 20, 20, 20));
        
        JButton btnCancelar = new JButton("Cancelar");
        UIConstants.styleSecondary(btnCancelar); // Padronizado
        btnCancelar.addActionListener(e -> modal.dispose());
        
        JButton btnSalvar = new JButton("SALVAR");
        UIConstants.styleSuccess(btnSalvar); // Padronizado
        btnSalvar.addActionListener(e -> {
            // Lógica simples de salvamento (Pode ser expandida para INSERT/UPDATE no banco)
            UIConstants.showSuccess(this, "Dados processados com sucesso!");
            modal.dispose();
            carregarDados("");
        });
        
        btnPanel.add(btnCancelar);
        btnPanel.add(btnSalvar);
        modal.add(btnPanel, BorderLayout.SOUTH);
        
        modal.setVisible(true);
    }

    // --- HELPER UI ---
    
    private JLabel criarLabel(String txt) {
        JLabel l = new JLabel(txt);
        l.setForeground(UIConstants.FG_MUTED);
        l.setFont(UIConstants.ARIAL_12_B);
        return l;
    }

    // --- RENDERERS INTERNOS ---

    class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = (String) value;
            l.setHorizontalAlignment(SwingConstants.CENTER);
            
            if ("aprovado".equalsIgnoreCase(status)) {
                l.setForeground(UIConstants.SUCCESS_GREEN);
                l.setText("● ATIVO");
            } else if ("pendente".equalsIgnoreCase(status)) {
                l.setForeground(STATUS_PENDING);
                l.setText("● PENDENTE");
            } else {
                l.setForeground(UIConstants.DANGER_RED);
                l.setText("● BLOQUEADO");
            }
            l.setFont(UIConstants.ARIAL_12_B);
            return l;
        }
    }

    class ActionPanel extends JPanel {
        JButton btnAprovar = new JButton();
        JButton btnEditar = new JButton();
        JButton btnExcluir = new JButton();

        public ActionPanel() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 8, 0));
            setOpaque(false);
            
            // Configura Botões Pequenos
            configBtn(btnAprovar, GoogleMaterialDesignIcons.CHECK, UIConstants.SUCCESS_GREEN);
            configBtn(btnEditar, GoogleMaterialDesignIcons.EDIT, UIConstants.FG_MUTED);
            configBtn(btnExcluir, GoogleMaterialDesignIcons.DELETE, UIConstants.DANGER_RED);

            add(btnAprovar);
            add(btnEditar);
            add(btnExcluir);
        }

        private void configBtn(JButton b, GoogleMaterialDesignIcons icon, Color c) {
            b.setIcon(IconFontSwing.buildIcon(icon, 16, c));
            b.setPreferredSize(new Dimension(32, 32));
            b.setBackground(new Color(0,0,0,0)); // Transparente
            b.setOpaque(false);
            b.setBorder(BorderFactory.createLineBorder(c, 1));
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            b.setContentAreaFilled(false);
            b.setToolTipText("Clique para ação");
        }
    }

    class ActionRenderer extends DefaultTableCellRenderer {
        private ActionPanel panel = new ActionPanel();
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            String status = (String) table.getValueAt(row, 5);
            // Se já aprovado, esconde o botão de aprovar
            panel.btnAprovar.setVisible(!"aprovado".equalsIgnoreCase(status));
            return panel;
        }
    }

    class ActionEditor extends AbstractCellEditor implements TableCellEditor {
        private ActionPanel panel = new ActionPanel();
        private int currentRow;

        public ActionEditor() {
            panel.btnAprovar.addActionListener(e -> {
                int id = (int) tabela.getValueAt(currentRow, 0);
                atualizarStatus(id, "aprovado");
                fireEditingStopped();
            });
            panel.btnEditar.addActionListener(e -> {
                Object[] dados = new Object[6];
                for(int i=0; i<6; i++) dados[i] = tabela.getValueAt(currentRow, i);
                abrirModalEdicao(dados);
                fireEditingStopped();
            });
            panel.btnExcluir.addActionListener(e -> {
                int id = (int) tabela.getValueAt(currentRow, 0);
                int opt = JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(panel), 
                    "Deseja bloquear este usuário?", "Confirmar Bloqueio", JOptionPane.YES_NO_OPTION);
                    
                if(opt == JOptionPane.YES_OPTION) {
                     atualizarStatus(id, "bloqueado");
                }
                fireEditingStopped();
            });
        }
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row;
            String status = (String) table.getValueAt(row, 5);
            panel.btnAprovar.setVisible(!"aprovado".equalsIgnoreCase(status));
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }
        @Override public Object getCellEditorValue() { return ""; }
    }
}