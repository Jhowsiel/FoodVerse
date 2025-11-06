package com.senac.food.verse.gui;

import com.senac.food.verse.CardapioDAO;
import com.senac.food.verse.CardapioDAO.Prato;
import com.senac.food.verse.CardapioDAO.ProdutoVenda;
import com.senac.food.verse.CardapioDAO.ReceitaItem;
import com.senac.food.verse.EstoqueDAO;
import com.senac.food.verse.EstoqueDAO.ItemEstoque;
import com.senac.food.verse.EstoqueDAO.Unidade;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Locale;


public class CardapioPainel extends JPanel {

    private final CardapioDAO dao = new CardapioDAO();
    private final EstoqueDAO estoqueDAO = new EstoqueDAO();

    private static final Locale LOCALE_BR = new Locale("pt", "BR");
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(LOCALE_BR);

    private JTabbedPane tabs;

    // PRATOS
    private JTextField txtBuscaPratos;
    private JComboBox<String> cbCategoriaPratos;
    private JComboBox<String> cbStatusPratos;
    private JTable tblPratos;
    private DefaultTableModel pratosModel;
    private TableRowSorter<DefaultTableModel> pratosSorter;

    // PRODUTOS
    private JTextField txtBuscaProdutos;
    private JComboBox<String> cbCategoriaProdutos;
    private JComboBox<String> cbStatusProdutos;
    private JTable tblProdutos;
    private DefaultTableModel produtosModel;
    private TableRowSorter<DefaultTableModel> produtosSorter;

    // Categorias padrão
    private static final String[] CATEGORIAS_PRATOS_PADRAO = {
            "Lanches","Pratos","Bebidas","Sobremesas","Entradas",
            "Acompanhamentos","Combos","Kids","Veganos",
            "Vegetarianos","Sem Glúten","Sem Lactose"
    };
    private static final String[] CATEGORIAS_PRODUTOS_PADRAO = {
            "Bebidas","Sobremesas","Outros","Mercearia","Descartáveis","Higiene"
    };

    public CardapioPainel() {
        UIConstants.applyDarkDefaults();
        setOpaque(true);
        setBackground(UIConstants.BG_DARK);
        UIManager.put("Component.arc",16);
        UIManager.put("Button.arc",18);
        UIManager.put("TextComponent.arc",16);
        UIManager.put("ScrollBar.showButtons", true);
        initComponents();
        carregarCombos();
        configurarAtalhosGlobais();
        atualizarTabelas();
    }

    private void initComponents(){
        JLabel lblTitulo = new JLabel("Cardápio", IconLoader.load("/icons/dish.png",24,24), SwingConstants.LEADING);
        lblTitulo.setFont(UIConstants.ARIAL_16_B);
        lblTitulo.setForeground(UIConstants.FG_LIGHT);

        tabs = new JTabbedPane();
        tabs.setFont(UIConstants.ARIAL_12_B);
        styleTabbedPane(tabs);

        JPanel abaPratos   = criarAbaPratos();
        JPanel abaProdutos = criarAbaProdutos();

        tabs.addTab("Pratos", abaPratos);
        tabs.addTab("Produtos", abaProdutos);

        GroupLayout gl = new GroupLayout(this);
        setLayout(gl);
        gl.setAutoCreateGaps(true);
        gl.setAutoCreateContainerGaps(true);

        gl.setHorizontalGroup(gl.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(lblTitulo)
                .addComponent(tabs)
        );
        gl.setVerticalGroup(gl.createSequentialGroup()
                .addComponent(lblTitulo)
                .addComponent(tabs)
        );
    }

    /* ----------------------------- ABA PRATOS -------------------------------- */

    private JPanel criarAbaPratos(){
        JPanel p = new JPanel();
        p.setBackground(UIConstants.BG_DARK);

        JLabel lBusca = label("Buscar:", true);
        txtBuscaPratos = new JTextField();
        UIConstants.styleField(txtBuscaPratos);
        txtBuscaPratos.addActionListener(e -> atualizarTabelaPratos());

        JLabel lCat = label("Categoria:", true);
        cbCategoriaPratos = new JComboBox<>();
        UIConstants.styleCombo(cbCategoriaPratos);

        JLabel lStatus = label("Status:", true);
        cbStatusPratos = new JComboBox<>(new String[]{"Todos","Ativos","Inativos"});
        UIConstants.styleCombo(cbStatusPratos);

        JButton btnBuscar = buttonPrimary("Buscar","/icons/search.png", e -> atualizarTabelaPratos());
        JButton btnNovo   = buttonPrimary("+ Novo Prato","/icons/add.png", e -> novoPrato());
        JButton btnEditar = buttonSecondary("Editar","/icons/edit.png", e -> editarPrato());
        JButton btnDel    = buttonSecondary("Excluir","/icons/delete.png", e -> excluirPrato());

        pratosModel = new DefaultTableModel(new Object[]{"ID","Nome","Categoria","Preço","Status","Ingredientes"},0){
            @Override public boolean isCellEditable(int r,int c){ return false; }
            @Override public Class<?> getColumnClass(int c){ return c==0?Long.class:String.class; }
        };
        tblPratos = criarTabelaBase(pratosModel);
        aplicarRenderersPratos();
        configurarSorterPratos();
        configurarPopupPratos();
        adicionarDuploClique(tblPratos,this::editarPrato);
        configurarAtalhosTabela(tblPratos,this::editarPrato,this::excluirPrato);

        JScrollPane sp = new JScrollPane(tblPratos);
        styleScroll(sp);

        JSeparator sep = separator();

        GroupLayout gl = new GroupLayout(p);
        p.setLayout(gl);
        gl.setAutoCreateGaps(true);
        gl.setAutoCreateContainerGaps(true);

        gl.setHorizontalGroup(gl.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(gl.createSequentialGroup()
                .addComponent(lBusca).addComponent(txtBuscaPratos)
                .addComponent(lCat).addComponent(cbCategoriaPratos, GroupLayout.PREFERRED_SIZE,160,GroupLayout.PREFERRED_SIZE)
                .addComponent(lStatus).addComponent(cbStatusPratos, GroupLayout.PREFERRED_SIZE,130,GroupLayout.PREFERRED_SIZE)
                .addComponent(btnBuscar))
            .addGroup(gl.createSequentialGroup()
                .addComponent(btnNovo)
                .addComponent(btnEditar)
                .addComponent(btnDel))
            .addComponent(sep)
            .addComponent(sp)
        );

        gl.setVerticalGroup(gl.createSequentialGroup()
            .addGroup(gl.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(lBusca)
                .addComponent(txtBuscaPratos,GroupLayout.PREFERRED_SIZE,32,GroupLayout.PREFERRED_SIZE)
                .addComponent(lCat)
                .addComponent(cbCategoriaPratos,GroupLayout.PREFERRED_SIZE,30,GroupLayout.PREFERRED_SIZE)
                .addComponent(lStatus)
                .addComponent(cbStatusPratos,GroupLayout.PREFERRED_SIZE,30,GroupLayout.PREFERRED_SIZE)
                .addComponent(btnBuscar))
            .addGroup(gl.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(btnNovo)
                .addComponent(btnEditar)
                .addComponent(btnDel))
            .addComponent(sep)
            .addComponent(sp)
        );

        ajustarLarguras(tblPratos, new int[]{60,260,200,120,90,130});
        return p;
    }

    /* ----------------------------- ABA PRODUTOS ------------------------------ */

    private JPanel criarAbaProdutos(){
        JPanel p = new JPanel();
        p.setBackground(UIConstants.BG_DARK);

        JLabel lBusca = label("Buscar:", true);
        txtBuscaProdutos = new JTextField();
        UIConstants.styleField(txtBuscaProdutos);
        txtBuscaProdutos.addActionListener(e -> atualizarTabelaProdutos());

        JLabel lCat = label("Categoria:", true);
        cbCategoriaProdutos = new JComboBox<>();
        UIConstants.styleCombo(cbCategoriaProdutos);

        JLabel lStatus = label("Status:", true);
        cbStatusProdutos = new JComboBox<>(new String[]{"Todos","Ativos","Inativos"});
        UIConstants.styleCombo(cbStatusProdutos);

        JButton btnBuscar = buttonPrimary("Buscar","/icons/search.png", e -> atualizarTabelaProdutos());
        JButton btnNovo   = buttonPrimary("+ Novo Produto","/icons/add.png", e -> novoProduto());
        JButton btnEditar = buttonSecondary("Editar","/icons/edit.png", e -> editarProduto());
        JButton btnDel    = buttonSecondary("Excluir","/icons/delete.png", e -> excluirProduto());

        produtosModel = new DefaultTableModel(new Object[]{"ID","Nome","Categoria","Preço","Status"},0){
            @Override public boolean isCellEditable(int r,int c){ return false; }
            @Override public Class<?> getColumnClass(int c){ return c==0?Long.class:String.class; }
        };
        tblProdutos = criarTabelaBase(produtosModel);
        aplicarRenderersProdutos();
        configurarSorterProdutos();
        configurarPopupProdutos();
        adicionarDuploClique(tblProdutos,this::editarProduto);
        configurarAtalhosTabela(tblProdutos,this::editarProduto,this::excluirProduto);

        JScrollPane sp = new JScrollPane(tblProdutos);
        styleScroll(sp);

        JSeparator sep = separator();

        GroupLayout gl = new GroupLayout(p);
        p.setLayout(gl);
        gl.setAutoCreateGaps(true);
        gl.setAutoCreateContainerGaps(true);

        gl.setHorizontalGroup(gl.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(gl.createSequentialGroup()
                .addComponent(lBusca).addComponent(txtBuscaProdutos)
                .addComponent(lCat).addComponent(cbCategoriaProdutos, GroupLayout.PREFERRED_SIZE,160,GroupLayout.PREFERRED_SIZE)
                .addComponent(lStatus).addComponent(cbStatusProdutos, GroupLayout.PREFERRED_SIZE,130,GroupLayout.PREFERRED_SIZE)
                .addComponent(btnBuscar))
            .addGroup(gl.createSequentialGroup()
                .addComponent(btnNovo)
                .addComponent(btnEditar)
                .addComponent(btnDel))
            .addComponent(sep)
            .addComponent(sp)
        );
        gl.setVerticalGroup(gl.createSequentialGroup()
            .addGroup(gl.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(lBusca)
                .addComponent(txtBuscaProdutos,GroupLayout.PREFERRED_SIZE,32,GroupLayout.PREFERRED_SIZE)
                .addComponent(lCat)
                .addComponent(cbCategoriaProdutos,GroupLayout.PREFERRED_SIZE,30,GroupLayout.PREFERRED_SIZE)
                .addComponent(lStatus)
                .addComponent(cbStatusProdutos,GroupLayout.PREFERRED_SIZE,30,GroupLayout.PREFERRED_SIZE)
                .addComponent(btnBuscar))
            .addGroup(gl.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(btnNovo)
                .addComponent(btnEditar)
                .addComponent(btnDel))
            .addComponent(sep)
            .addComponent(sp)
        );

        ajustarLarguras(tblProdutos, new int[]{60,280,220,120,90});
        return p;
    }

    /* ----------------------------- UTIL VISUAIS ------------------------------ */

    private JLabel label(String txt, boolean bold){
        JLabel l = new JLabel(txt);
        l.setFont(bold?UIConstants.ARIAL_12_B:UIConstants.ARIAL_12);
        l.setForeground(UIConstants.FG_LIGHT);
        return l;
    }
    private JButton buttonPrimary(String text, String icon, ActionListener al){
        JButton b = new JButton(text, IconLoader.load(icon,16,16));
        UIConstants.stylePrimary(b);
        b.addActionListener(al);
        return b;
    }
    private JButton buttonSecondary(String text, String icon, ActionListener al){
        JButton b = new JButton(text, IconLoader.load(icon,16,16));
        UIConstants.styleSecondary(b);
        b.addActionListener(al);
        return b;
    }
    private void styleScroll(JScrollPane sp){
        sp.setBorder(null);
        sp.getViewport().setBackground(UIConstants.BG_DARK);
    }
    private JSeparator separator(){
        JSeparator s = new JSeparator(SwingConstants.HORIZONTAL);
        s.setForeground(UIConstants.GRID_DARK);
        return s;
    }
    private void styleTabbedPane(JTabbedPane tp){
        tp.setBackground(UIConstants.BG_DARK);
        tp.setForeground(UIConstants.FG_LIGHT);
        tp.setOpaque(true);
        tp.setBorder(null);
    }

    private JTable criarTabelaBase(DefaultTableModel model){
        JTable t = new JTable(model){
            @Override public Component prepareRenderer(TableCellRenderer r,int row,int col){
                Component c = super.prepareRenderer(r,row,col);
                if(!isRowSelected(row)){
                    c.setBackground(row%2==0?UIConstants.BG_DARK:UIConstants.ALT_ROW);
                    c.setForeground(UIConstants.FG_LIGHT);
                }
                return c;
            }
        };
        t.setRowHeight(28);
        t.setSelectionBackground(UIConstants.SEL_BG);
        t.setSelectionForeground(UIConstants.SEL_FG);
        t.setGridColor(UIConstants.GRID_DARK);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setDefaultRenderer(new HeaderRenderer());
        return t;
    }

    private void ajustarLarguras(JTable table, int[] widths){
        for(int i=0;i<widths.length;i++){
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    /* ----------------------------- SORTERS / POPUPS / ATALHOS ---------------- */

    private void configurarSorterPratos(){
        pratosSorter = new TableRowSorter<>(pratosModel);
        pratosSorter.setComparator(0, Comparator.comparingLong(v -> (Long)v));
        pratosSorter.setComparator(3, Comparator.comparingDouble(this::parseValorMoeda));
        tblPratos.setRowSorter(pratosSorter);
        pratosSorter.setSortKeys(List.of(new RowSorter.SortKey(1, SortOrder.ASCENDING)));
    }
    private void configurarSorterProdutos(){
        produtosSorter = new TableRowSorter<>(produtosModel);
        produtosSorter.setComparator(0, Comparator.comparingLong(v -> (Long)v));
        produtosSorter.setComparator(3, Comparator.comparingDouble(this::parseValorMoeda));
        tblProdutos.setRowSorter(produtosSorter);
        produtosSorter.setSortKeys(List.of(new RowSorter.SortKey(1, SortOrder.ASCENDING)));
    }

    private void configurarPopupPratos(){
        JPopupMenu m = criarPopup(
                e -> editarPrato(),
                e -> duplicarPrato(),
                e -> toggleStatusPrato(),
                e -> excluirPrato()
        );
        tblPratos.setComponentPopupMenu(m);
    }
    private void configurarPopupProdutos(){
        JPopupMenu m = criarPopup(
                e -> editarProduto(),
                e -> duplicarProduto(),
                e -> toggleStatusProduto(),
                e -> excluirProduto()
        );
        tblProdutos.setComponentPopupMenu(m);
    }

    private JPopupMenu criarPopup(ActionListener editar,
                                  ActionListener duplicar,
                                  ActionListener toggle,
                                  ActionListener excluir){
        JPopupMenu menu = new JPopupMenu();
        stylePopupMenu(menu);
        JMenuItem miEditar   = popupItem("Editar","/icons/edit.png", editar);
        JMenuItem miDuplicar = popupItem("Duplicar","/icons/add.png", duplicar);
        JMenuItem miToggle   = popupItem("Ativar/Inativar","/icons/save.png", toggle);
        JMenuItem miExcluir  = popupItem("Excluir","/icons/delete.png", excluir);
        menu.add(miEditar); menu.add(miDuplicar); menu.add(miToggle);
        menu.addSeparator(); menu.add(miExcluir);
        return menu;
    }

    private JMenuItem popupItem(String text,String icon, ActionListener al){
        JMenuItem mi = new JMenuItem(text, IconLoader.load(icon,16,16));
        mi.addActionListener(al);
        mi.setBackground(UIConstants.CARD_DARK);
        mi.setForeground(UIConstants.FG_LIGHT);
        return mi;
    }

    private void stylePopupMenu(JPopupMenu m){
        m.setBorder(new EmptyBorder(4,4,4,4));
        m.setBackground(UIConstants.CARD_DARK);
        m.setForeground(UIConstants.FG_LIGHT);
    }

    private void configurarAtalhosTabela(JTable t, Runnable edit, Runnable delete){
        InputMap im = t.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = t.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),"edit");
        am.put("edit", new AbstractAction(){ @Override public void actionPerformed(ActionEvent e){ edit.run(); }});
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0),"del");
        am.put("del", new AbstractAction(){ @Override public void actionPerformed(ActionEvent e){ delete.run(); }});
    }

    private void configurarAtalhosGlobais(){
        InputMap im = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "focus");
        am.put("focus", new AbstractAction(){
            @Override public void actionPerformed(ActionEvent e){
                if(tabs.getSelectedIndex()==0) txtBuscaPratos.requestFocusInWindow();
                else txtBuscaProdutos.requestFocusInWindow();
            }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK), "new");
        am.put("new", new AbstractAction(){
            @Override public void actionPerformed(ActionEvent e){
                if(tabs.getSelectedIndex()==0) novoPrato(); else novoProduto();
            }
        });
    }

    private void adicionarDuploClique(JTable t, Runnable action){
        t.addMouseListener(new MouseAdapter(){
            @Override public void mouseClicked(MouseEvent e){
                if(e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) action.run();
            }
        });
    }

    /* ----------------------------- RENDERERS --------------------------------- */

    private void aplicarRenderersPratos(){
        tblPratos.setDefaultRenderer(Object.class, new BodyCellRenderer(tblPratos));
        tblPratos.getColumnModel().getColumn(3).setCellRenderer(new CurrencyRenderer(tblPratos));
        tblPratos.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer(tblPratos));
    }
    private void aplicarRenderersProdutos(){
        tblProdutos.setDefaultRenderer(Object.class, new BodyCellRenderer(tblProdutos));
        tblProdutos.getColumnModel().getColumn(3).setCellRenderer(new CurrencyRenderer(tblProdutos));
        tblProdutos.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer(tblProdutos));
    }

    private static class HeaderRenderer extends DefaultTableCellRenderer {
        HeaderRenderer(){
            setOpaque(true);
            setBackground(UIConstants.HEADER_DARK);
            setForeground(UIConstants.FG_LIGHT);
            setFont(UIConstants.ARIAL_12_B);
            setBorder(new EmptyBorder(6,8,6,8));
        }
        @Override public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column){
            super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
            String name = table.getColumnName(column);
            setHorizontalAlignment(switch(name){
                case "ID","Status","Ingredientes" -> CENTER;
                case "Preço" -> RIGHT;
                default -> LEFT;
            });
            return this;
        }
    }
    private static class BodyCellRenderer extends DefaultTableCellRenderer {
        private final JTable table;
        BodyCellRenderer(JTable t){ this.table=t; setOpaque(true); }
        @Override public Component getTableCellRendererComponent(JTable tbl,Object val,boolean selected,boolean focus,int row,int col){
            super.getTableCellRendererComponent(tbl,val,selected,focus,row,col);
            if(!selected){
                setBackground(row%2==0?UIConstants.BG_DARK:UIConstants.ALT_ROW);
                setForeground(UIConstants.FG_LIGHT);
            }
            setBorder(new EmptyBorder(4,8,4,8));
            String name = table.getColumnName(col);
            setHorizontalAlignment(switch(name){
                case "ID","Status","Ingredientes" -> CENTER;
                case "Preço" -> RIGHT;
                default -> LEFT;
            });
            return this;
        }
    }
    private static class CurrencyRenderer extends BodyCellRenderer {
        CurrencyRenderer(JTable t){ super(t); }
    }
    private static class StatusRenderer extends BodyCellRenderer {
        StatusRenderer(JTable t){ super(t); }
        @Override public Component getTableCellRendererComponent(JTable tbl,Object val,boolean selected,boolean focus,int row,int col){
            JLabel l = (JLabel)super.getTableCellRendererComponent(tbl,val,selected,focus,row,col);
            if(!selected){
                if("Ativo".equalsIgnoreCase(String.valueOf(val))){
                    l.setBackground(UIConstants.SUCCESS_GREEN);
                    l.setForeground(Color.WHITE);
                }else{
                    l.setBackground(UIConstants.STATUS_INACTIVE_BG);
                    l.setForeground(UIConstants.FG_LIGHT);
                }
            }
            l.setHorizontalAlignment(CENTER);
            l.setBorder(new EmptyBorder(4,8,4,8));
            return l;
        }
    }

    /* ----------------------------- CARREGAMENTO / ATUALIZAÇÃO ---------------- */

    private void carregarCombos(){
        // Pratos
        List<String> catsPratos = Stream.concat(
                Arrays.stream(CATEGORIAS_PRATOS_PADRAO),
                dao.categoriasPratos().stream()
        ).filter(s -> s!=null && !s.isBlank())
         .distinct()
         .sorted(String.CASE_INSENSITIVE_ORDER)
         .collect(Collectors.toList());
        cbCategoriaPratos.removeAllItems(); cbCategoriaPratos.addItem("Todas");
        catsPratos.forEach(cbCategoriaPratos::addItem);

        // Produtos
        List<String> catsProdutos = Stream.concat(
                Arrays.stream(CATEGORIAS_PRODUTOS_PADRAO),
                dao.categoriasProdutos().stream()
        ).filter(s -> s!=null && !s.isBlank())
         .distinct()
         .sorted(String.CASE_INSENSITIVE_ORDER)
         .collect(Collectors.toList());
        cbCategoriaProdutos.removeAllItems(); cbCategoriaProdutos.addItem("Todas");
        catsProdutos.forEach(cbCategoriaProdutos::addItem);
    }

    private void atualizarTabelas(){
        atualizarTabelaPratos();
        atualizarTabelaProdutos();
    }

    private void atualizarTabelaPratos(){
        String termo = txtBuscaPratos.getText();
        String categoria = (String) cbCategoriaPratos.getSelectedItem();
        String status = (String) cbStatusPratos.getSelectedItem();

        List<Prato> lista = dao.listarPratos(termo,categoria,status);
        pratosModel.setRowCount(0);
        for(Prato p : lista){
            pratosModel.addRow(new Object[]{
                p.getId(),
                p.getNome(),
                p.getCategoria(),
                CURRENCY.format(p.getPreco()),
                p.isAtivo()?"Ativo":"Inativo",
                p.getIngredientes().size()+" itens"
            });
        }
    }

    private void atualizarTabelaProdutos(){
        String termo = txtBuscaProdutos.getText();
        String categoria = (String) cbCategoriaProdutos.getSelectedItem();
        String status = (String) cbStatusProdutos.getSelectedItem();

        List<ProdutoVenda> lista = dao.listarProdutos(termo,categoria,status);
        produtosModel.setRowCount(0);
        for(ProdutoVenda p : lista){
            produtosModel.addRow(new Object[]{
                p.getId(),
                p.getNome(),
                p.getCategoria(),
                CURRENCY.format(p.getPreco()),
                p.isAtivo()?"Ativo":"Inativo"
            });
        }
    }

    /* ----------------------------- AÇÕES PRATOS ------------------------------ */

    private void novoPrato(){
        PratoDialog d = new PratoDialog(SwingUtilities.getWindowAncestor(this), null);
        d.setVisible(true);
        Prato r = d.getResultado();
        if(r!=null){
            dao.salvarPrato(r);
            carregarCombos();
            atualizarTabelaPratos();
            info("Prato cadastrado com sucesso!");
        }
    }
    private void editarPrato(){
        int vr = tblPratos.getSelectedRow();
        if(vr<0){ warn("Selecione um prato."); return; }
        long id = (Long) tblPratos.getValueAt(vr,0);
        Prato existente = dao.buscarPratoPorId(id);
        if(existente==null) return;
        PratoDialog d = new PratoDialog(SwingUtilities.getWindowAncestor(this), existente);
        d.setVisible(true);
        Prato edit = d.getResultado();
        if(edit!=null){
            edit.setId(existente.getId());
            dao.atualizarPrato(edit);
            carregarCombos();
            atualizarTabelaPratos();
            info("Prato atualizado com sucesso!");
        }
    }
    private void excluirPrato(){
        int vr = tblPratos.getSelectedRow();
        if(vr<0){ warn("Selecione um prato."); return; }
        long id = (Long) tblPratos.getValueAt(vr,0);
        if(confirmar("Confirma exclusão do prato?")){
            dao.excluirPrato(id);
            atualizarTabelaPratos();
            info("Prato excluído.");
        }
    }
    private void duplicarPrato(){
        int vr = tblPratos.getSelectedRow();
        if(vr<0) return;
        long id = (Long) tblPratos.getValueAt(vr,0);
        Prato base = dao.buscarPratoPorId(id);
        if(base==null) return;
        Prato n = new Prato();
        n.setNome(base.getNome()+" (cópia)");
        n.setCategoria(base.getCategoria());
        n.setPreco(base.getPreco());
        n.setAtivo(base.isAtivo());
        for(ReceitaItem ri: base.getIngredientes()){
            n.getIngredientes().add(new ReceitaItem(ri.getItemEstoqueId(), ri.getItemNome(), ri.getUnidade(), ri.getQuantidade()));
        }
        dao.salvarPrato(n);
        atualizarTabelaPratos();
    }
    private void toggleStatusPrato(){
        int vr = tblPratos.getSelectedRow();
        if(vr<0) return;
        long id = (Long) tblPratos.getValueAt(vr,0);
        Prato p = dao.buscarPratoPorId(id);
        if(p==null) return;
        p.setAtivo(!p.isAtivo());
        dao.atualizarPrato(p);
        atualizarTabelaPratos();
    }

    /* ----------------------------- AÇÕES PRODUTOS --------------------------- */

    private void novoProduto(){
        ProdutoDialog d = new ProdutoDialog(SwingUtilities.getWindowAncestor(this), null);
        d.setVisible(true);
        ProdutoVenda r = d.getResultado();
        if(r!=null){
            dao.salvarProduto(r);
            carregarCombos();
            atualizarTabelaProdutos();
            info("Produto cadastrado com sucesso!");
        }
    }
    private void editarProduto(){
        int vr = tblProdutos.getSelectedRow();
        if(vr<0){ warn("Selecione um produto."); return; }
        long id = (Long) tblProdutos.getValueAt(vr,0);
        String nome    = (String) tblProdutos.getValueAt(vr,1);
        String cat     = (String) tblProdutos.getValueAt(vr,2);
        double preco   = parseValorMoeda(tblProdutos.getValueAt(vr,3));
        boolean ativo  = "Ativo".equals(tblProdutos.getValueAt(vr,4));

        ProdutoDialog d = new ProdutoDialog(SwingUtilities.getWindowAncestor(this),
                new ProdutoVenda(id,nome,cat,ativo,preco));
        d.setVisible(true);
        ProdutoVenda edit = d.getResultado();
        if(edit!=null){
            edit.setId(id);
            dao.atualizarProduto(edit);
            carregarCombos();
            atualizarTabelaProdutos();
            info("Produto atualizado com sucesso!");
        }
    }
    private void excluirProduto(){
        int vr = tblProdutos.getSelectedRow();
        if(vr<0){ warn("Selecione um produto."); return; }
        long id = (Long) tblProdutos.getValueAt(vr,0);
        if(confirmar("Confirma exclusão do produto?")){
            dao.excluirProduto(id);
            atualizarTabelaProdutos();
            info("Produto excluído.");
        }
    }
    private void duplicarProduto(){
        int vr = tblProdutos.getSelectedRow();
        if(vr<0) return;
        long id = (Long) tblProdutos.getValueAt(vr,0);
        String nome    = (String) tblProdutos.getValueAt(vr,1);
        String cat     = (String) tblProdutos.getValueAt(vr,2);
        double preco   = parseValorMoeda(tblProdutos.getValueAt(vr,3));
        boolean ativo  = "Ativo".equals(tblProdutos.getValueAt(vr,4));
        ProdutoVenda n = new ProdutoVenda(null, nome+" (cópia)", cat, ativo, preco);
        dao.salvarProduto(n);
        atualizarTabelaProdutos();
    }
    private void toggleStatusProduto(){
        int vr = tblProdutos.getSelectedRow();
        if(vr<0) return;
        long id = (Long) tblProdutos.getValueAt(vr,0);
        String nome    = (String) tblProdutos.getValueAt(vr,1);
        String cat     = (String) tblProdutos.getValueAt(vr,2);
        double preco   = parseValorMoeda(tblProdutos.getValueAt(vr,3));
        boolean ativo  = "Ativo".equals(tblProdutos.getValueAt(vr,4));
        ProdutoVenda edit = new ProdutoVenda(id,nome,cat,!ativo,preco);
        dao.atualizarProduto(edit);
        atualizarTabelaProdutos();
    }

    /* ----------------------------- UTILS ------------------------------------- */
    private double parseValorMoeda(Object o){
        if(o==null) return 0;
        try{
            String s = String.valueOf(o).replace("R$","").replace(".","").trim().replace(",",".");
            return Double.parseDouble(s);
        }catch(Exception e){ return 0; }
    }
    private void info(String msg){ JOptionPane.showMessageDialog(this,msg,"Info",JOptionPane.INFORMATION_MESSAGE); }
    private void warn(String msg){ JOptionPane.showMessageDialog(this,msg,"Atenção",JOptionPane.WARNING_MESSAGE); }
    private boolean confirmar(String msg){ return JOptionPane.showConfirmDialog(this,msg,"Confirmar",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION; }

    /* -------------------- DIALOG PRATO (inclui seletor ingredientes) -------- */

    private class PratoDialog extends JDialog {
        private JTextField txtNome;
        private JComboBox<String> cbCategoria;
        private JSpinner spPreco;
        private JCheckBox chkAtivo;
        private JTable tblIng;
        private DefaultTableModel ingModel;
        private Prato resultado;

        PratoDialog(Window owner, Prato base){
            super(owner,"Cadastro de Prato", ModalityType.APPLICATION_MODAL);
            setSize(860,660);
            setLocationRelativeTo(owner);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            montarUI();
            if(base!=null) carregar(base);
        }
        private void montarUI(){
            JLabel titulo = new JLabel("Cadastro de Prato", IconLoader.load("/icons/dish.png",22,22),SwingConstants.LEADING);
            titulo.setFont(UIConstants.ARIAL_16_B); titulo.setForeground(UIConstants.FG_LIGHT);

            JLabel lNome = label("Nome:", true);
            txtNome = new JTextField(); UIConstants.styleField(txtNome);

            JLabel lCat = label("Categoria:", true);
            cbCategoria = new JComboBox<>(categoriasPratoCombo());
            UIConstants.styleCombo(cbCategoria);

            JLabel lPreco = label("Preço:", true);
            spPreco = new JSpinner(new SpinnerNumberModel(0.0,0.0,99999.99,0.5));
            UIConstants.styleSpinner(spPreco);

            chkAtivo = new JCheckBox("Ativo",true);
            chkAtivo.setOpaque(false); chkAtivo.setForeground(UIConstants.FG_LIGHT);

            JLabel lIng = label("Ingredientes", true);
            lIng.setIcon(IconLoader.load("/icons/ingredients.png",18,18));

            ingModel = new DefaultTableModel(new Object[]{"ID","Item","Unidade","Quantidade"},0){
                @Override public boolean isCellEditable(int r,int c){ return false; }
                @Override public Class<?> getColumnClass(int c){ return switch(c){case 0->Long.class; case 3->Double.class; default->String.class;}; }
            };
            tblIng = criarTabelaBase(ingModel);
            ajustarLarguras(tblIng,new int[]{60,340,120,120});
            JScrollPane sp = new JScrollPane(tblIng);
            styleScroll(sp);

            JButton btnAdd = buttonPrimary("+ Ingrediente","/icons/add.png", e -> abrirSeletorIngrediente());
            JButton btnRem = buttonSecondary("Remover","/icons/delete.png", e -> removerIngrediente());
            JButton btnSalvar = new JButton("Salvar", IconLoader.load("/icons/save.png",16,16));
            UIConstants.styleSuccess(btnSalvar);
            btnSalvar.addActionListener(e -> salvar());
            JButton btnCancelar = buttonSecondary("Cancelar","/icons/cancel.png", e -> { resultado=null; dispose(); });

            getRootPane().setDefaultButton(btnSalvar);
            getRootPane().registerKeyboardAction(e -> { resultado=null; dispose(); },
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0), JComponent.WHEN_IN_FOCUSED_WINDOW);

            JPanel c = new JPanel();
            c.setBackground(UIConstants.BG_DARK);
            GroupLayout gl = new GroupLayout(c);
            c.setLayout(gl);
            gl.setAutoCreateGaps(true);
            gl.setAutoCreateContainerGaps(true);

            gl.setHorizontalGroup(gl.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(titulo)
                .addGroup(gl.createSequentialGroup()
                    .addGroup(gl.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(lNome)
                        .addComponent(lCat)
                        .addComponent(lPreco))
                    .addGroup(gl.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(txtNome)
                        .addComponent(cbCategoria,0,GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE)
                        .addGroup(gl.createSequentialGroup()
                            .addComponent(spPreco,GroupLayout.PREFERRED_SIZE,120,GroupLayout.PREFERRED_SIZE)
                            .addGap(16)
                            .addComponent(chkAtivo))))
                .addComponent(lIng)
                .addComponent(sp)
                .addGroup(GroupLayout.Alignment.TRAILING, gl.createSequentialGroup()
                    .addComponent(btnAdd)
                    .addComponent(btnRem)
                    .addGap(0,0,Short.MAX_VALUE)
                    .addComponent(btnCancelar)
                    .addComponent(btnSalvar))
            );
            gl.setVerticalGroup(gl.createSequentialGroup()
                .addComponent(titulo)
                .addGap(8)
                .addGroup(gl.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(lNome)
                    .addComponent(txtNome,GroupLayout.PREFERRED_SIZE,30,GroupLayout.PREFERRED_SIZE))
                .addGroup(gl.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(lCat)
                    .addComponent(cbCategoria,GroupLayout.PREFERRED_SIZE,30,GroupLayout.PREFERRED_SIZE))
                .addGroup(gl.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(lPreco)
                    .addComponent(spPreco,GroupLayout.PREFERRED_SIZE,30,GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkAtivo))
                .addGap(10)
                .addComponent(lIng)
                .addComponent(sp,GroupLayout.DEFAULT_SIZE,340,Short.MAX_VALUE)
                .addGroup(gl.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(btnAdd)
                    .addComponent(btnRem)
                    .addComponent(btnCancelar)
                    .addComponent(btnSalvar))
            );

            setContentPane(c);
        }
        private String[] categoriasPratoCombo(){
            return Stream.concat(Arrays.stream(CATEGORIAS_PRATOS_PADRAO), dao.categoriasPratos().stream())
                    .filter(s->s!=null && !s.isBlank())
                    .distinct()
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toArray(String[]::new);
        }
        private void abrirSeletorIngrediente(){
            IngredientPickerDialog d = new IngredientPickerDialog(SwingUtilities.getWindowAncestor(this));
            d.setVisible(true);
            IngredientSelection sel = d.getResultado();
            if(sel!=null){
                ingModel.addRow(new Object[]{sel.itemId, sel.itemNome, sel.unidade, sel.quantidade});
            }
        }
        private void removerIngrediente(){
            int r = tblIng.getSelectedRow();
            if(r<0){ warn("Selecione um ingrediente."); return; }
            ingModel.removeRow(r);
        }
        private void salvar(){
            String nome = txtNome.getText().trim();
            if(nome.isBlank()){ warn("Nome é obrigatório."); return; }
            Prato p = new Prato();
            p.setNome(nome);
            p.setCategoria((String) cbCategoria.getSelectedItem());
            p.setPreco(((Number)spPreco.getValue()).doubleValue());
            p.setAtivo(chkAtivo.isSelected());
            for(int i=0;i<ingModel.getRowCount();i++){
                Long id = (Long) ingModel.getValueAt(i,0);
                String item = String.valueOf(ingModel.getValueAt(i,1));
                String un   = String.valueOf(ingModel.getValueAt(i,2));
                double qtd  = ((Number)ingModel.getValueAt(i,3)).doubleValue();
                p.getIngredientes().add(new ReceitaItem(id,item,un,qtd));
            }
            resultado = p;
            dispose();
        }
        private void carregar(Prato base){
            txtNome.setText(base.getNome());
            cbCategoria.setSelectedItem(base.getCategoria());
            spPreco.setValue(base.getPreco());
            chkAtivo.setSelected(base.isAtivo());
            for(ReceitaItem ri: base.getIngredientes()){
                ingModel.addRow(new Object[]{ri.getItemEstoqueId(),ri.getItemNome(),ri.getUnidade(),ri.getQuantidade()});
            }
            resultado = base;
        }
        Prato getResultado(){ return resultado; }

        /* ---------- Seletor de Ingredientes ---------- */
        private class IngredientPickerDialog extends JDialog {
            private JTextField txtBusca;
            private JComboBox<String> cbCat;
            private JTable tbl;
            private DefaultTableModel model;
            private JSpinner spQtd;
            private JComboBox<String> cbUn;
            private JLabel lblConv;
            private IngredientSelection resultado;

            IngredientPickerDialog(Window owner){
                super(owner,"Selecionar Ingrediente", ModalityType.APPLICATION_MODAL);
                setSize(860,600);
                setLocationRelativeTo(owner);
                montar();
                carregarCategorias();
                atualizar();
            }
            private void montar(){
                JLabel titulo = new JLabel("Buscar Item de Estoque", IconLoader.load("/icons/ingredients.png",18,18),SwingConstants.LEADING);
                titulo.setFont(UIConstants.ARIAL_14_B);
                titulo.setForeground(UIConstants.FG_LIGHT);

                txtBusca = new JTextField(); UIConstants.styleField(txtBusca);
                txtBusca.addActionListener(e -> atualizar());

                cbCat = new JComboBox<>(); UIConstants.styleCombo(cbCat);

                JButton btnBuscar = buttonPrimary("Buscar","/icons/search.png", e -> atualizar());

                model = new DefaultTableModel(new Object[]{"ID","Nome","Categoria","Unidade","Estoque","Custo Médio"},0){
                    @Override public boolean isCellEditable(int r,int c){ return false; }
                    @Override public Class<?> getColumnClass(int c){ return c==0?Long.class:(c==4?Double.class:String.class); }
                };
                tbl = criarTabelaBase(model);
                ajustarLarguras(tbl,new int[]{60,320,160,80,100,120});
                tbl.addMouseListener(new MouseAdapter(){
                    @Override public void mouseClicked(MouseEvent e){
                        if(e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)){
                            prepararUnidades();
                            confirmar();
                        }
                    }
                });
                JScrollPane sp = new JScrollPane(tbl);
                styleScroll(sp);

                JLabel lQtd = label("Quantidade:", true);
                spQtd = new JSpinner(new SpinnerNumberModel(1.0,0.001,999999.0,0.5));
                UIConstants.styleSpinner(spQtd);

                JLabel lUn = label("Unidade:", true);
                cbUn = new JComboBox<>();
                UIConstants.styleCombo(cbUn);

                lblConv = new JLabel("Conversão:");
                lblConv.setFont(UIConstants.ARIAL_12);
                lblConv.setForeground(UIConstants.FG_MUTED);

                JButton btnAdd = UIbuttonSuccess("Adicionar","/icons/save.png", e -> { prepararUnidades(); confirmar(); });
                JButton btnCancel = buttonSecondary("Cancelar","/icons/cancel.png", e -> { resultado=null; dispose(); });

                getRootPane().setDefaultButton(btnAdd);
                getRootPane().registerKeyboardAction(e -> { resultado=null; dispose(); },
                        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0), JComponent.WHEN_IN_FOCUSED_WINDOW);

                JSeparator sep = separator();

                JPanel c = new JPanel();
                c.setBackground(UIConstants.BG_DARK);
                GroupLayout gl = new GroupLayout(c);
                c.setLayout(gl);
                gl.setAutoCreateGaps(true);
                gl.setAutoCreateContainerGaps(true);

                gl.setHorizontalGroup(gl.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(titulo)
                    .addGroup(gl.createSequentialGroup()
                        .addComponent(label("Buscar:", false))
                        .addComponent(txtBusca)
                        .addComponent(label("Categoria:", false))
                        .addComponent(cbCat,GroupLayout.PREFERRED_SIZE,220,GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnBuscar))
                    .addComponent(sep)
                    .addComponent(sp)
                    .addGroup(gl.createSequentialGroup()
                        .addComponent(lQtd).addComponent(spQtd,GroupLayout.PREFERRED_SIZE,160,GroupLayout.PREFERRED_SIZE)
                        .addGap(16)
                        .addComponent(lUn).addComponent(cbUn,GroupLayout.PREFERRED_SIZE,160,GroupLayout.PREFERRED_SIZE)
                        .addGap(16)
                        .addComponent(lblConv,GroupLayout.DEFAULT_SIZE,GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE))
                    .addGroup(GroupLayout.Alignment.TRAILING, gl.createSequentialGroup()
                        .addComponent(btnCancel)
                        .addComponent(btnAdd))
                );
                gl.setVerticalGroup(gl.createSequentialGroup()
                    .addComponent(titulo)
                    .addGroup(gl.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(label("Buscar:", false))
                        .addComponent(txtBusca,GroupLayout.PREFERRED_SIZE,30,GroupLayout.PREFERRED_SIZE)
                        .addComponent(label("Categoria:", false))
                        .addComponent(cbCat,GroupLayout.PREFERRED_SIZE,30,GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnBuscar))
                    .addComponent(sep)
                    .addComponent(sp,GroupLayout.DEFAULT_SIZE,340,Short.MAX_VALUE)
                    .addGroup(gl.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(lQtd)
                        .addComponent(spQtd,GroupLayout.PREFERRED_SIZE,30,GroupLayout.PREFERRED_SIZE)
                        .addComponent(lUn)
                        .addComponent(cbUn,GroupLayout.PREFERRED_SIZE,30,GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblConv))
                    .addGroup(gl.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(btnCancel)
                        .addComponent(btnAdd))
                );

                setContentPane(c);
            }

            private JButton UIbuttonSuccess(String text,String icon,ActionListener al){
                JButton b = new JButton(text, IconLoader.load(icon,16,16));
                UIConstants.styleSuccess(b);
                b.addActionListener(al);
                return b;
            }

            private void carregarCategorias(){
                cbCat.removeAllItems();
                cbCat.addItem("Todas");
                estoqueDAO.categoriasNomes().forEach(cbCat::addItem);
            }
            private void atualizar(){
                String termo = txtBusca.getText();
                String cat = (String) cbCat.getSelectedItem();
                List<ItemEstoque> itens = estoqueDAO.listarItens(termo,cat,"Ativos");
                model.setRowCount(0);
                for(ItemEstoque it: itens){
                    model.addRow(new Object[]{
                            it.getId(),
                            it.getNome(),
                            it.getCategoria(),
                            it.getUnidadePadrao(),
                            it.getEstoqueAtual(),
                            CURRENCY.format(it.getCustoMedio())
                    });
                }
                if(model.getRowCount()>0) tbl.setRowSelectionInterval(0,0);
                prepararUnidades();
            }
            private void prepararUnidades(){
                cbUn.removeAllItems();
                lblConv.setText("Conversão:");
                int vr = tbl.getSelectedRow();
                if(vr<0) return;
                int mr = tbl.convertRowIndexToModel(vr);
                Long id = (Long) model.getValueAt(mr,0);
                ItemEstoque item = estoqueDAO.buscarItemPorId(id);
                if(item==null) return;

                Unidade unidadePadrao = estoqueDAO.listarUnidades().stream()
                        .filter(u->u.getCodigo().equals(item.getUnidadePadrao()))
                        .findFirst().orElse(null);

                List<String> possiveis = new ArrayList<>();
                if(unidadePadrao!=null){
                    for(Unidade u : estoqueDAO.listarUnidades()){
                        if(Objects.equals(u.getBase(), unidadePadrao.getBase()) && u.getFatorBase()>0){
                            possiveis.add(u.getCodigo());
                        }
                    }
                }
                if(item.getFatorCxParaUn()!=null){
                    if(!possiveis.contains("un")) possiveis.add("un");
                    if(!possiveis.contains("cx")) possiveis.add("cx");
                    if(!possiveis.contains("pct")) possiveis.add("pct");
                }
                possiveis.remove(item.getUnidadePadrao());
                possiveis.add(0,item.getUnidadePadrao());
                for(String u: possiveis) cbUn.addItem(u);
                cbUn.addActionListener(e -> atualizarInfoConversao(item));
                cbUn.setSelectedIndex(0);
                spQtd.setValue(1.0);
                atualizarInfoConversao(item);
            }
            private void atualizarInfoConversao(ItemEstoque item){
                String unSel = (String) cbUn.getSelectedItem();
                if(unSel==null){ lblConv.setText("Conversão:"); return; }
                String baseGrupo = estoqueDAO.listarUnidades().stream()
                        .filter(u->u.getCodigo().equals(unSel))
                        .map(Unidade::getBase)
                        .findFirst().orElse("-");
                StringBuilder sb = new StringBuilder("Grupo ").append(baseGrupo);
                if("g".equals(baseGrupo)) sb.append(" (1 kg = 1000 g)");
                if("ml".equals(baseGrupo)) sb.append(" (1 L = 1000 ml)");
                if(item.getFatorCxParaUn()!=null) sb.append(" | 1 cx/pct = ").append(String.format("%.0f",item.getFatorCxParaUn())).append(" un");
                lblConv.setText(sb.toString());
            }
            private void confirmar(){
                int vr = tbl.getSelectedRow();
                if(vr<0){ warn("Selecione um item."); return; }
                int mr = tbl.convertRowIndexToModel(vr);
                Long id = (Long) model.getValueAt(mr,0);
                String nome = String.valueOf(model.getValueAt(mr,1));
                String un = (String) cbUn.getSelectedItem();
                double qtd = ((Number)spQtd.getValue()).doubleValue();
                if(qtd<=0){ warn("Quantidade deve ser > 0."); return; }
                resultado = new IngredientSelection(id,nome,un,qtd);
                dispose();
            }
            IngredientSelection getResultado(){ return resultado; }
        }

        private class IngredientSelection {
            final Long itemId; final String itemNome; final String unidade; final double quantidade;
            IngredientSelection(Long itemId,String itemNome,String unidade,double quantidade){
                this.itemId=itemId; this.itemNome=itemNome; this.unidade=unidade; this.quantidade=quantidade;
            }
        }
    }

    /* ----------------------------- DIALOG PRODUTO ---------------------------- */

    private class ProdutoDialog extends JDialog {
        private JTextField txtNome;
        private JComboBox<String> cbCategoria;
        private JSpinner spPreco;
        private JCheckBox chkAtivo;
        private ProdutoVenda resultado;

        ProdutoDialog(Window owner, ProdutoVenda base){
            super(owner,"Cadastro de Produto", ModalityType.APPLICATION_MODAL);
            setSize(580,420);
            setLocationRelativeTo(owner);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            montar();
            if(base!=null) carregar(base);
        }
        private void montar(){
            JLabel titulo = new JLabel("Cadastro de Produto", IconLoader.load("/icons/drink.png",22,22),SwingConstants.LEADING);
            titulo.setFont(UIConstants.ARIAL_16_B); titulo.setForeground(UIConstants.FG_LIGHT);

            JLabel lNome = label("Nome:", true);
            txtNome = new JTextField(); UIConstants.styleField(txtNome);

            JLabel lCat = label("Categoria:", true);
            cbCategoria = new JComboBox<>(categoriasProdutosCombo());
            UIConstants.styleCombo(cbCategoria);

            JLabel lPreco = label("Preço:", true);
            spPreco = new JSpinner(new SpinnerNumberModel(0.0,0.0,9999.99,0.5));
            UIConstants.styleSpinner(spPreco);

            chkAtivo = new JCheckBox("Ativo", true);
            chkAtivo.setOpaque(false); chkAtivo.setForeground(UIConstants.FG_LIGHT);

            JButton btnSalvar = new JButton("Salvar", IconLoader.load("/icons/save.png",16,16));
            UIConstants.styleSuccess(btnSalvar);
            btnSalvar.addActionListener(e -> salvar());

            JButton btnCancelar = buttonSecondary("Cancelar","/icons/cancel.png", e -> { resultado=null; dispose(); });

            getRootPane().setDefaultButton(btnSalvar);
            getRootPane().registerKeyboardAction(e -> { resultado=null; dispose(); },
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0), JComponent.WHEN_IN_FOCUSED_WINDOW);

            JPanel c = new JPanel();
            c.setBackground(UIConstants.BG_DARK);
            GroupLayout gl = new GroupLayout(c);
            c.setLayout(gl);
            gl.setAutoCreateGaps(true);
            gl.setAutoCreateContainerGaps(true);

            gl.setHorizontalGroup(gl.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(titulo)
                .addGroup(gl.createSequentialGroup()
                    .addGroup(gl.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(lNome)
                        .addComponent(lCat)
                        .addComponent(lPreco))
                    .addGroup(gl.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(txtNome)
                        .addComponent(cbCategoria,0,GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE)
                        .addGroup(gl.createSequentialGroup()
                            .addComponent(spPreco,GroupLayout.PREFERRED_SIZE,140,GroupLayout.PREFERRED_SIZE)
                            .addGap(16)
                            .addComponent(chkAtivo))))
                .addGroup(GroupLayout.Alignment.TRAILING, gl.createSequentialGroup()
                    .addComponent(btnCancelar)
                    .addComponent(btnSalvar))
            );
            gl.setVerticalGroup(gl.createSequentialGroup()
                .addComponent(titulo)
                .addGap(8)
                .addGroup(gl.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(lNome)
                    .addComponent(txtNome,GroupLayout.PREFERRED_SIZE,30,GroupLayout.PREFERRED_SIZE))
                .addGroup(gl.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(lCat)
                    .addComponent(cbCategoria,GroupLayout.PREFERRED_SIZE,30,GroupLayout.PREFERRED_SIZE))
                .addGroup(gl.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(lPreco)
                    .addComponent(spPreco,GroupLayout.PREFERRED_SIZE,30,GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkAtivo))
                .addGap(24)
                .addGroup(gl.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(btnCancelar)
                    .addComponent(btnSalvar))
            );

            setContentPane(c);
        }
        private String[] categoriasProdutosCombo(){
            return Stream.concat(Arrays.stream(CATEGORIAS_PRODUTOS_PADRAO), dao.categoriasProdutos().stream())
                    .filter(s->s!=null && !s.isBlank())
                    .distinct()
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toArray(String[]::new);
        }
        private void salvar(){
            String nome = txtNome.getText().trim();
            if(nome.isBlank()){ warn("Nome é obrigatório."); return; }
            ProdutoVenda p = new ProdutoVenda(null,
                    nome,
                    (String)cbCategoria.getSelectedItem(),
                    chkAtivo.isSelected(),
                    ((Number)spPreco.getValue()).doubleValue());
            resultado = p;
            dispose();
        }
        private void carregar(ProdutoVenda base){
            txtNome.setText(base.getNome());
            cbCategoria.setSelectedItem(base.getCategoria());
            spPreco.setValue(base.getPreco());
            chkAtivo.setSelected(base.isAtivo());
            resultado = base;
        }
        ProdutoVenda getResultado(){ return resultado; }
    }

    /* ----------------------------- Buttons util internos --------------------- */
    private JButton UIbuttonSuccess(String text,String icon,ActionListener al){
        JButton b = new JButton(text, IconLoader.load(icon,16,16));
        UIConstants.styleSuccess(b);
        b.addActionListener(al);
        return b;
    }
}