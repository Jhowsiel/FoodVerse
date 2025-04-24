package com.senac.food.verse.gui;

import com.senac.food.verse.ConexaoBanco;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;


public class AprovacaoCadastrosPanel extends javax.swing.JPanel {

    private List<Integer> funcionariosIds;

    /**
     * Creates new form AprovacaoCadastrosPanel
     */
    public AprovacaoCadastrosPanel() {
        initComponents();
        String[] colunas = {"Selecionar", "Nome", "Email", "Cargo", "Data de Registro", "Status", "Ações"};
        DefaultTableModel modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class;
                }
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 6; // Apenas colunas de seleção e ações são editáveis
            }
        };

        // Aplicar modelo à tabela
        tabelaCadastros.setModel(modeloTabela);

        // Configurar largura das colunas
        tabelaCadastros.getColumnModel().getColumn(0).setMaxWidth(80); // Coluna de seleção
        tabelaCadastros.getColumnModel().getColumn(6).setMaxWidth(120); // Coluna de ações

        // Centralizar texto em colunas específicas
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tabelaCadastros.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tabelaCadastros.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        tabelaCadastros.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        // Renderizador personalizado para a coluna de status
        tabelaCadastros.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer());

        // Renderizador e editor para a coluna de ações
        TableColumn acaoColumn = tabelaCadastros.getColumnModel().getColumn(6);
        acaoColumn.setCellRenderer(new ButtonRenderer());
        acaoColumn.setCellEditor(new ButtonEditor(new JCheckBox()));

        // Inicializar lista de IDs
        funcionariosIds = new ArrayList<>();

        // Carregar dados
        carregarCadastrosPendentes();
        
        // atualizar dados
        iniciarAtualizacaoAutomatica();

        // Configurar ações dos botões
        configurarAcoesBotoes();
    }

    // Renderizador para a coluna de status
    class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value != null) {
                String status = value.toString();
                if ("pendente".equals(status)) {
                    c.setForeground(new Color(255, 165, 0)); // Laranja
                } else if ("aprovado".equals(status)) {
                    c.setForeground(new Color(0, 128, 0)); // Verde
                } else if ("rejeitado".equals(status)) {
                    c.setForeground(new Color(255, 0, 0)); // Vermelho
                }
            }
            
            return c;
        }
    }

    // Renderizador para os botões de ação
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText("Opções");
            
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }
            
            return this;
        }
    }

    // Editor para os botões de ação
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow;
        
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (isSelected) {
                button.setForeground(table.getSelectionForeground());
                button.setBackground(table.getSelectionBackground());
            } else {
                button.setForeground(table.getForeground());
                button.setBackground(table.getBackground());
            }
            
            label = (value == null) ? "Opções" : value.toString();
            button.setText(label);
            isPushed = true;
            currentRow = row;
            
            return button;
        }
        
        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                // Obter o ID do funcionário correspondente à linha
                int funcionarioId = funcionariosIds.get(currentRow);
                
                // Criar o menu de opções
                String[] opcoes = {"Aprovar", "Excluir", "Cancelar"};
                int escolha = JOptionPane.showOptionDialog(button, 
                        "Escolha uma ação para este cadastro:", 
                        "Opções", 
                        JOptionPane.DEFAULT_OPTION, 
                        JOptionPane.INFORMATION_MESSAGE, 
                        null, 
                        opcoes, 
                        opcoes[0]);
                
                if (escolha == 0) { // Aprovar
                    boolean sucesso = aprovarCadastro(funcionarioId);
                    if (sucesso) {
                        JOptionPane.showMessageDialog(button, "Cadastro aprovado com sucesso!");
                        DefaultTableModel modelo = (DefaultTableModel) tabelaCadastros.getModel();
                        modelo.removeRow(currentRow);
                        funcionariosIds.remove(currentRow);
                    } else {
                        JOptionPane.showMessageDialog(button, "Erro ao aprovar cadastro.", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                } else if (escolha == 1) { // Excluir
                    int confirmacao = JOptionPane.showConfirmDialog(button, 
                            "Tem certeza que deseja excluir este cadastro?", 
                            "Confirmação de Exclusão", 
                            JOptionPane.YES_NO_OPTION);
                    
                    if (confirmacao == JOptionPane.YES_OPTION) {
                        boolean sucesso = excluirCadastro(funcionarioId);
                        if (sucesso) {
                            JOptionPane.showMessageDialog(button, "Cadastro excluído com sucesso!");
                            DefaultTableModel modelo = (DefaultTableModel) tabelaCadastros.getModel();
                            modelo.removeRow(currentRow);
                            funcionariosIds.remove(currentRow);
                        } else {
                            JOptionPane.showMessageDialog(button, "Erro ao excluir cadastro.", "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
            isPushed = false;
            return label;
        }
        
        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }

    private void configurarAcoesBotoes() {
        // Evento para selecionar todos os itens
        btnSelecionarTodos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultTableModel modelo = (DefaultTableModel) tabelaCadastros.getModel();
                for (int i = 0; i < modelo.getRowCount(); i++) {
                    modelo.setValueAt(true, i, 0);
                }
            }
        });
        
        // Evento para limpar a seleção
        btnLimparSelecao.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultTableModel modelo = (DefaultTableModel) tabelaCadastros.getModel();
                for (int i = 0; i < modelo.getRowCount(); i++) {
                    modelo.setValueAt(false, i, 0);
                }
            }
        });
        
        // Evento para aprovar os cadastros selecionados
        btnAprovarSelecionados.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultTableModel modelo = (DefaultTableModel) tabelaCadastros.getModel();
                List<Integer> rowsToRemove = new ArrayList<>();
                List<Integer> idsToApprove = new ArrayList<>();
                
                // Coletar linhas selecionadas
                for (int i = 0; i < modelo.getRowCount(); i++) {
                    Boolean isSelected = (Boolean) modelo.getValueAt(i, 0);
                    if (isSelected) {
                        rowsToRemove.add(i);
                        idsToApprove.add(funcionariosIds.get(i));
                    }
                }
                
                if (idsToApprove.isEmpty()) {
                    JOptionPane.showMessageDialog(null, 
                            "Nenhum cadastro selecionado para aprovação.", 
                            "Atenção", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Aprovar cadastros selecionados
                boolean aprovacaoRealizada = aprovarCadastros(idsToApprove);
                
                if (aprovacaoRealizada) {
                    JOptionPane.showMessageDialog(null, 
                            "Cadastros aprovados com sucesso!", 
                            "Confirmação", JOptionPane.INFORMATION_MESSAGE);
                    
                    // Remover linhas da tabela (de trás para frente para não afetar os índices)
                    for (int i = rowsToRemove.size() - 1; i >= 0; i--) {
                        int row = rowsToRemove.get(i);
                        modelo.removeRow(row);
                        funcionariosIds.remove(row);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, 
                            "Erro ao aprovar cadastros. Tente novamente.", 
                            "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        // Evento para excluir os cadastros selecionados
        btnExcluirSelecionados.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultTableModel modelo = (DefaultTableModel) tabelaCadastros.getModel();
                List<Integer> rowsToRemove = new ArrayList<>();
                List<Integer> idsToDelete = new ArrayList<>();
                
                // Coletar linhas selecionadas
                for (int i = 0; i < modelo.getRowCount(); i++) {
                    Boolean isSelected = (Boolean) modelo.getValueAt(i, 0);
                    if (isSelected) {
                        rowsToRemove.add(i);
                        idsToDelete.add(funcionariosIds.get(i));
                    }
                }
                
                if (idsToDelete.isEmpty()) {
                    JOptionPane.showMessageDialog(null, 
                            "Nenhum cadastro selecionado para exclusão.", 
                            "Atenção", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Confirmar exclusão
                int confirmacao = JOptionPane.showConfirmDialog(null,
                        "Tem certeza que deseja excluir os cadastros selecionados?",
                        "Confirmação de Exclusão", JOptionPane.YES_NO_OPTION);
                
                if (confirmacao == JOptionPane.YES_OPTION) {
                    // Excluir cadastros selecionados
                    boolean exclusaoRealizada = excluirCadastros(idsToDelete);
                    
                    if (exclusaoRealizada) {
                        JOptionPane.showMessageDialog(null, 
                                "Cadastros excluídos com sucesso!", 
                                "Confirmação", JOptionPane.INFORMATION_MESSAGE);
                        
                        // Remover linhas da tabela (de trás para frente para não afetar os índices)
                        for (int i = rowsToRemove.size() - 1; i >= 0; i--) {
                            int row = rowsToRemove.get(i);
                            modelo.removeRow(row);
                            funcionariosIds.remove(row);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, 
                                "Erro ao excluir cadastros. Tente novamente.", 
                                "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }

    private void carregarCadastrosPendentes() {
        limparTabela();
        
        ConexaoBanco banco = new ConexaoBanco();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = banco.abrirConexao();
            String query = "SELECT userID, name, email, role, registrationDate, status FROM tb_funcionarios WHERE status = 'pendente'";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            
            DefaultTableModel modelo = (DefaultTableModel) tabelaCadastros.getModel();
            
            while (rs.next()) {
                int id = rs.getInt("userID");
                String nome = rs.getString("name");
                String email = rs.getString("email");
                String cargo = rs.getString("role");
                String dataRegistro = rs.getString("registrationDate");
                String status = rs.getString("status");
                
                // Adicionar ID à lista de IDs
                funcionariosIds.add(id);
                
                // Adicionar linha na tabela (com checkbox desmarcado por padrão)
                modelo.addRow(new Object[]{false, nome, email, cargo, dataRegistro, status, "Ações"});
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Erro ao carregar cadastros pendentes", ex);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) banco.fecharConexao();
            } catch (SQLException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Erro ao fechar recursos", ex);
            }
        }
    }
    
    private void iniciarAtualizacaoAutomatica() {
    Timer timer = new Timer(60000, e -> carregarCadastrosPendentes()); // A cada 60s
    timer.start();
}

    private void limparTabela() {
        DefaultTableModel modelo = (DefaultTableModel) tabelaCadastros.getModel();
        modelo.setRowCount(0);
        funcionariosIds.clear();
    }

    // Método para aprovar cadastros no banco de dados
    private boolean aprovarCadastros(List<Integer> ids) {
        this.carregarCadastrosPendentes();
                
        ConexaoBanco banco = new ConexaoBanco();
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean sucesso = true;

        try {
            conn = banco.abrirConexao();
            conn.setAutoCommit(false); // Iniciar transação

            String query = "UPDATE tb_funcionarios SET status = 'aprovado' WHERE userID = ?";
            stmt = conn.prepareStatement(query);

            for (Integer id : ids) {
                stmt.setInt(1, id);
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            conn.commit();

            // Verificar se todas as atualizações foram bem-sucedidas
            for (int result : results) {
                if (result != 1) {
                    sucesso = false;
                    break;
                }
            }

        } catch (SQLException ex) {
            sucesso = false;
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Erro ao aprovar cadastros", ex);
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Erro ao fazer rollback", e);
            }
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    banco.fecharConexao();
                }
            } catch (SQLException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Erro ao fechar recursos", ex);
            }
        }

        return sucesso;
    }

    // Método para excluir cadastros no banco de dados
    private boolean excluirCadastros(List<Integer> ids) {
        ConexaoBanco banco = new ConexaoBanco();
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean sucesso = true;

        try {
            conn = banco.abrirConexao();
            conn.setAutoCommit(false); // Iniciar transação

            String query = "DELETE FROM tb_funcionarios WHERE userID = ?";
            stmt = conn.prepareStatement(query);

            for (Integer id : ids) {
                stmt.setInt(1, id);
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            conn.commit();

            // Verificar se todas as exclusões foram bem-sucedidas
            for (int result : results) {
                if (result != 1) {
                    sucesso = false;
                    break;
                }
            }

        } catch (SQLException ex) {
            sucesso = false;
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Erro ao excluir cadastros", ex);
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Erro ao fazer rollback", e);
            }
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    banco.fecharConexao();
                }
            } catch (SQLException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Erro ao fechar recursos", ex);
            }
        }

        return sucesso;
    }

    // Método para aprovar um único cadastro (usado pelo botão de ação)
    private boolean aprovarCadastro(int id) {
        ConexaoBanco banco = new ConexaoBanco();
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean sucesso = false;

        try {
            conn = banco.abrirConexao();

            String query = "UPDATE tb_funcionarios SET status = 'aprovado' WHERE userID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();
            sucesso = (rowsAffected > 0);

        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Erro ao aprovar cadastro", ex);
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) banco.fecharConexao();
            } catch (SQLException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Erro ao fechar recursos", ex);
            }
        }

        return sucesso;
    }

    // Método para excluir um único cadastro (usado pelo botão de ação)
    private boolean excluirCadastro(int id) {
        ConexaoBanco banco = new ConexaoBanco();
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean sucesso = false;

        try {
            conn = banco.abrirConexao();

            String query = "DELETE FROM tb_funcionarios WHERE userID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();
            sucesso = (rowsAffected > 0);

        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Erro ao excluir cadastro", ex);
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) banco.fecharConexao();
            } catch (SQLException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Erro ao fechar recursos", ex);
            }
        }

        return sucesso;
    }

    // Método para recarregar a tabela (útil para ser chamado externamente)
    public void recarregarTabela() {
        carregarCadastrosPendentes();
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelTitulo = new javax.swing.JLabel();
        scrollPaneTabela = new javax.swing.JScrollPane();
        tabelaCadastros = new javax.swing.JTable();
        btnSelecionarTodos = new javax.swing.JButton();
        btnLimparSelecao = new javax.swing.JButton();
        btnExcluirSelecionados = new javax.swing.JButton();
        btnAprovarSelecionados = new javax.swing.JButton();

        labelTitulo.setText("Aprovação de Cadastros");

        tabelaCadastros.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        scrollPaneTabela.setViewportView(tabelaCadastros);

        btnSelecionarTodos.setText("Selecionar Todos");
        btnSelecionarTodos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelecionarTodosActionPerformed(evt);
            }
        });

        btnLimparSelecao.setText("Limpar Seleção");
        btnLimparSelecao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimparSelecaoActionPerformed(evt);
            }
        });

        btnExcluirSelecionados.setText("Excluir Selecionados");
        btnExcluirSelecionados.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExcluirSelecionadosActionPerformed(evt);
            }
        });

        btnAprovarSelecionados.setText("Aprovar Selecionados");
        btnAprovarSelecionados.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAprovarSelecionadosActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(scrollPaneTabela, javax.swing.GroupLayout.PREFERRED_SIZE, 725, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addComponent(btnSelecionarTodos)
                                .addGap(55, 55, 55)
                                .addComponent(btnLimparSelecao)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnAprovarSelecionados)
                                .addGap(70, 70, 70)
                                .addComponent(btnExcluirSelecionados)
                                .addGap(9, 9, 9))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(293, 293, 293)
                        .addComponent(labelTitulo)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelTitulo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPaneTabela, javax.swing.GroupLayout.PREFERRED_SIZE, 353, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSelecionarTodos)
                    .addComponent(btnLimparSelecao)
                    .addComponent(btnExcluirSelecionados)
                    .addComponent(btnAprovarSelecionados))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnSelecionarTodosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelecionarTodosActionPerformed
        // Selecionar todos os itens na tabela
    DefaultTableModel modelo = (DefaultTableModel) tabelaCadastros.getModel();
    for (int i = 0; i < modelo.getRowCount(); i++) {
        modelo.setValueAt(true, i, 0);
        }
    }//GEN-LAST:event_btnSelecionarTodosActionPerformed

    private void btnLimparSelecaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparSelecaoActionPerformed
        // Limpar todas as seleções na tabela
    DefaultTableModel modelo = (DefaultTableModel) tabelaCadastros.getModel();
    for (int i = 0; i < modelo.getRowCount(); i++) {
        modelo.setValueAt(false, i, 0);
        }
    }//GEN-LAST:event_btnLimparSelecaoActionPerformed

    private void btnAprovarSelecionadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAprovarSelecionadosActionPerformed
        // Aprovar os cadastros selecionados
    }//GEN-LAST:event_btnAprovarSelecionadosActionPerformed

    private void btnExcluirSelecionadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcluirSelecionadosActionPerformed
    
    }//GEN-LAST:event_btnExcluirSelecionadosActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAprovarSelecionados;
    private javax.swing.JButton btnExcluirSelecionados;
    private javax.swing.JButton btnLimparSelecao;
    private javax.swing.JButton btnSelecionarTodos;
    private javax.swing.JLabel labelTitulo;
    private javax.swing.JScrollPane scrollPaneTabela;
    private javax.swing.JTable tabelaCadastros;
    // End of variables declaration//GEN-END:variables
}
