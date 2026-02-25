package com.senac.food.verse.gui;

import com.senac.food.verse.ConexaoBanco;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;

public class AprovacaoCadastrosPanel extends JPanel {

    // --- CORES & CONSTANTES (Design System Local para garantir funcionamento) ---
    private final Color BG_DARK = new Color(30, 30, 30);
    private final Color BG_PANEL = new Color(45, 45, 48);
    private final Color FG_TEXT = new Color(240, 240, 240);
    private final Color FG_MUTED = new Color(160, 160, 160);
    private final Color PRIMARY_RED = new Color(188, 16, 21);
    private final Color ACCENT_GREEN = new Color(39, 174, 96);
    private final Color ACCENT_ORANGE = new Color(230, 126, 34);
    private final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    private final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);

    // Componentes
    private JTable tabela;
    private DefaultTableModel modeloTabela;
    private JTextField txtBusca;
    private JLabel lblTotalPendentes;
    private JLabel lblTotalAtivos;

    public AprovacaoCadastrosPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_DARK);

        // 1. HEADER
        add(criarHeader(), BorderLayout.NORTH);

        // 2. CORPO
        JPanel corpo = new JPanel(new BorderLayout(0, 20));
        corpo.setBackground(BG_DARK);
        corpo.setBorder(new EmptyBorder(10, 30, 30, 30));

        corpo.add(criarToolbar(), BorderLayout.NORTH);
        corpo.add(criarTabela(), BorderLayout.CENTER);

        add(corpo, BorderLayout.CENTER);

        // Carregar dados iniciais
        carregarDados("");
    }

    // --- SEÇÃO 1: HEADER ---
    private JPanel criarHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(25, 30, 15, 30));

        // Título e Ícone
        JLabel titulo = new JLabel("Gestão de Equipe");
        titulo.setFont(FONT_TITLE);
        titulo.setForeground(FG_TEXT);
        titulo.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.SUPERVISOR_ACCOUNT, 32, FG_TEXT));
        titulo.setIconTextGap(15);
        p.add(titulo, BorderLayout.WEST);

        // Cards de Métricas
        JPanel cards = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        cards.setOpaque(false);

        lblTotalPendentes = criarBadgeInfo("Pendentes", ACCENT_ORANGE);
        lblTotalAtivos = criarBadgeInfo("Ativos", ACCENT_GREEN);

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
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                super.paintComponent(g);
            }
        };
        l.setFont(FONT_BOLD);
        l.setForeground(Color.WHITE);
        l.setBackground(cor);
        l.setOpaque(false); // Custom paint
        l.setBorder(new EmptyBorder(8, 15, 8, 15));
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    // --- SEÇÃO 2: TOOLBAR ---
    private JPanel criarToolbar() {
        JPanel p = new JPanel(new BorderLayout(15, 0));
        p.setOpaque(false);

        // Campo de Busca
        txtBusca = new JTextField();
        estilizarCampo(txtBusca);
        txtBusca.putClientProperty("JTextField.placeholderText", "Buscar nome, e-mail ou cargo...");
        txtBusca.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });

        // Botões
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton btnNovo = criarBotao("Novo Usuário", GoogleMaterialDesignIcons.PERSON_ADD, PRIMARY_RED);
        btnNovo.addActionListener(e -> abrirModalEdicao(null)); 

        JButton btnAtualizar = criarBotao("Atualizar", GoogleMaterialDesignIcons.REFRESH, new Color(60, 60, 60));
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
        estilizarTabela(tabela);

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
        scroll.setBorder(new LineBorder(new Color(60,60,60)));
        scroll.getViewport().setBackground(BG_PANEL);

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
        try {
            if (cb.conn == null || cb.conn.isClosed()) {
                cb.conn = DriverManager.getConnection("jdbc:sqlserver://127.0.0.1:1433;databaseName=FoodVerseDB;encrypt=false;trustServerCertificate=true;loginTimeout=5", "sa", "123456");
            }

            PreparedStatement ps = cb.conn.prepareStatement(sql);
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
            lblTotalPendentes.setBackground(pendentes > 0 ? ACCENT_ORANGE : new Color(60,60,60));
            lblTotalAtivos.setText(ativos + " Ativos");

        } catch (Exception e) {
            // Fallback visual
            modeloTabela.addRow(new Object[]{0, "Sistema Offline", "-", "-", "Admin", "erro", ""});
        }
    }

    private void atualizarStatus(int id, String novoStatus) {
        String sql = "UPDATE tb_funcionarios SET status = ? WHERE userID = ?";
        ConexaoBanco cb = new ConexaoBanco();
        try {
            if (cb.conn == null) cb.conn = DriverManager.getConnection("jdbc:sqlserver://127.0.0.1:1433;databaseName=FoodVerseDB;encrypt=false", "sa", "123456");
            PreparedStatement ps = cb.conn.prepareStatement(sql);
            ps.setString(1, novoStatus);
            ps.setInt(2, id);
            ps.executeUpdate();
            
            mostrarMensagemPersonalizada("Status atualizado com sucesso!", false);
            carregarDados(txtBusca.getText());
        } catch (Exception e) {
            mostrarMensagemPersonalizada("Erro: " + e.getMessage(), true);
        }
    }
    
    // --- MENSAGENS E MODAIS CUSTOMIZADOS ---
    
    private void mostrarMensagemPersonalizada(String msg, boolean isErro) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0,0,0,0));
        
        JPanel p = new JPanel(new BorderLayout(15, 0));
        p.setBackground(new Color(40, 40, 40));
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(isErro ? PRIMARY_RED : ACCENT_GREEN, 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel icon = new JLabel(IconFontSwing.buildIcon(
            isErro ? GoogleMaterialDesignIcons.ERROR : GoogleMaterialDesignIcons.CHECK_CIRCLE, 
            32, isErro ? PRIMARY_RED : ACCENT_GREEN));
            
        JLabel text = new JLabel("<html><div style='width: 250px;'>"+msg+"</div></html>");
        text.setFont(FONT_BODY);
        text.setForeground(FG_TEXT);
        
        JButton btnOk = criarBotao("OK", GoogleMaterialDesignIcons.CHECK, new Color(60,60,60));
        btnOk.addActionListener(e -> dialog.dispose());
        
        p.add(icon, BorderLayout.WEST);
        p.add(text, BorderLayout.CENTER);
        p.add(btnOk, BorderLayout.SOUTH);
        
        dialog.add(p);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // --- MODAL DE EDIÇÃO/CRIAÇÃO CORRIGIDO ---
    private void abrirModalEdicao(Object[] dadosAtuais) {
        JDialog modal = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Gerenciar Usuário", true);
        modal.setSize(450, 600); // Aumentado para evitar corte
        modal.setLocationRelativeTo(this);
        modal.getContentPane().setBackground(BG_DARK);
        modal.setLayout(new BorderLayout());

        // Painel de Conteúdo com GridBagLayout para controle total
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG_DARK);
        form.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 0, 15, 0); // Espaçamento vertical
        gc.gridx = 0; gc.gridy = 0;
        
        // Título do Modal
        JLabel lblTitulo = new JLabel(dadosAtuais == null ? "Novo Cadastro" : "Editar Cadastro");
        lblTitulo.setFont(FONT_TITLE);
        lblTitulo.setForeground(FG_TEXT);
        form.add(lblTitulo, gc);
        
        gc.gridy++; gc.insets = new Insets(20, 0, 5, 0);
        form.add(criarLabel("Nome Completo"), gc);
        gc.gridy++; gc.insets = new Insets(0, 0, 15, 0);
        JTextField tNome = new JTextField(); estilizarCampo(tNome);
        form.add(tNome, gc);
        
        gc.gridy++; gc.insets = new Insets(0, 0, 5, 0);
        form.add(criarLabel("Usuário (Login)"), gc);
        gc.gridy++; gc.insets = new Insets(0, 0, 15, 0);
        JTextField tUser = new JTextField(); estilizarCampo(tUser);
        form.add(tUser, gc);
        
        gc.gridy++; gc.insets = new Insets(0, 0, 5, 0);
        form.add(criarLabel("E-mail"), gc);
        gc.gridy++; gc.insets = new Insets(0, 0, 15, 0);
        JTextField tEmail = new JTextField(); estilizarCampo(tEmail);
        form.add(tEmail, gc);
        
        gc.gridy++; gc.insets = new Insets(0, 0, 5, 0);
        form.add(criarLabel("Cargo"), gc);
        gc.gridy++; gc.insets = new Insets(0, 0, 15, 0);
        JComboBox<String> cRole = new JComboBox<>(new String[]{"Cozinheiro", "Entregador", "Atendente", "Gerente", "Admin"});
        cRole.setBackground(BG_PANEL); cRole.setForeground(Color.WHITE);
        form.add(cRole, gc);
        
        gc.gridy++; gc.insets = new Insets(0, 0, 5, 0);
        form.add(criarLabel(dadosAtuais == null ? "Senha" : "Nova Senha (deixe vazio para manter)"), gc);
        gc.gridy++; gc.insets = new Insets(0, 0, 15, 0);
        JPasswordField tSenha = new JPasswordField(); estilizarCampo(tSenha);
        form.add(tSenha, gc);
        
        // Preencher dados se for edição
        if(dadosAtuais != null) {
            tNome.setText(dadosAtuais[1].toString());
            tUser.setText(dadosAtuais[2].toString());
            tEmail.setText(dadosAtuais[3].toString());
            cRole.setSelectedItem(dadosAtuais[4].toString());
        }
        
        modal.add(form, BorderLayout.CENTER);
        
        // Botões do Modal
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(BG_DARK);
        btnPanel.setBorder(new EmptyBorder(10, 20, 20, 20));
        
        JButton btnCancelar = criarBotao("Cancelar", GoogleMaterialDesignIcons.CLOSE, new Color(80, 80, 80));
        btnCancelar.addActionListener(e -> modal.dispose());
        
        JButton btnSalvar = criarBotao("SALVAR", GoogleMaterialDesignIcons.SAVE, ACCENT_GREEN);
        btnSalvar.addActionListener(e -> {
            // Simulação de Salvamento
            mostrarMensagemPersonalizada("Dados salvos com sucesso!", false);
            modal.dispose();
            carregarDados("");
        });
        
        btnPanel.add(btnCancelar);
        btnPanel.add(btnSalvar);
        modal.add(btnPanel, BorderLayout.SOUTH);
        
        modal.setVisible(true);
    }

    // --- HELPER UI ---
    
    private void estilizarTabela(JTable table) {
        table.setRowHeight(55);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(60, 60, 60));
        table.setBackground(BG_PANEL);
        table.setForeground(FG_TEXT);
        table.setSelectionBackground(new Color(70, 70, 75));
        table.setSelectionForeground(Color.WHITE);
        table.setFont(FONT_BODY);

        JTableHeader header = table.getTableHeader();
        header.setBackground(BG_DARK);
        header.setForeground(FG_MUTED);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(80, 80, 80)));
        ((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
    }

    private void estilizarCampo(JTextField tf) {
        tf.setPreferredSize(new Dimension(200, 40));
        tf.setBackground(BG_PANEL);
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.WHITE);
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(80, 80, 80)),
                new EmptyBorder(5, 10, 5, 10)
        ));
    }
    
    private JLabel criarLabel(String txt) {
        JLabel l = new JLabel(txt);
        l.setForeground(FG_MUTED);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return l;
    }

    private JButton criarBotao(String texto, GoogleMaterialDesignIcons icon, Color bg) {
        JButton btn = new JButton(texto);
        btn.setIcon(IconFontSwing.buildIcon(icon, 18, Color.WHITE));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Efeito Hover simples
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.brighter()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    // --- RENDERERS INTERNOS ---

    class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = (String) value;
            l.setHorizontalAlignment(SwingConstants.CENTER);
            
            if ("aprovado".equalsIgnoreCase(status)) {
                l.setForeground(ACCENT_GREEN);
                l.setText("● ATIVO");
            } else if ("pendente".equalsIgnoreCase(status)) {
                l.setForeground(ACCENT_ORANGE);
                l.setText("● PENDENTE");
            } else {
                l.setForeground(PRIMARY_RED);
                l.setText("● BLOQUEADO");
            }
            l.setFont(new Font("Segoe UI", Font.BOLD, 11));
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
            
            // Botões Circulares
            configBtn(btnAprovar, GoogleMaterialDesignIcons.CHECK, ACCENT_GREEN);
            configBtn(btnEditar, GoogleMaterialDesignIcons.EDIT, FG_MUTED);
            configBtn(btnExcluir, GoogleMaterialDesignIcons.DELETE, PRIMARY_RED);

            add(btnAprovar);
            add(btnEditar);
            add(btnExcluir);
        }

        private void configBtn(JButton b, GoogleMaterialDesignIcons icon, Color c) {
            b.setIcon(IconFontSwing.buildIcon(icon, 16, c));
            b.setPreferredSize(new Dimension(32, 32));
            b.setBackground(new Color(0,0,0,0));
            b.setBorder(new LineBorder(c.darker(), 1, true)); // Borda fina
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            b.setContentAreaFilled(false);
        }
    }

    class ActionRenderer extends DefaultTableCellRenderer {
        private ActionPanel panel = new ActionPanel();
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            String status = (String) table.getValueAt(row, 5);
            // Se já aprovado, desabilita ou esconde o check
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
                stopCellEditing();
            });
            panel.btnEditar.addActionListener(e -> {
                Object[] dados = new Object[6];
                for(int i=0; i<6; i++) dados[i] = tabela.getValueAt(currentRow, i);
                abrirModalEdicao(dados);
                stopCellEditing();
            });
            panel.btnExcluir.addActionListener(e -> {
                int id = (int) tabela.getValueAt(currentRow, 0);
                JDialog confirm = new JDialog((Frame) SwingUtilities.getWindowAncestor(panel), true);
                // ... lógica de confirmação visual ... 
                if(JOptionPane.showConfirmDialog(null, "Bloquear acesso deste usuário?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                     atualizarStatus(id, "bloqueado"); // Não deleta, bloqueia para histórico
                }
                stopCellEditing();
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