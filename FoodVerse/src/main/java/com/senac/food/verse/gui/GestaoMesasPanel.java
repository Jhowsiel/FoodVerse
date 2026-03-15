package com.senac.food.verse.gui;

import com.senac.food.verse.Reserva;
import com.senac.food.verse.ReservaDAO;
import com.senac.food.verse.SessionContext;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class GestaoMesasPanel extends JPanel {

    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATA_HORA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int CHECK_IN_WINDOW_MINUTES = 15;
    private static final int TOLERANCIA_RESERVA_PASSADA_MINUTES = 10;
    private static final ZoneId APP_ZONE_ID = ZoneId.of("America/Sao_Paulo");

    private final ReservaDAO dao = new ReservaDAO();
    private final SessionContext sessionContext = SessionContext.getInstance();
    private final boolean podeCriarReserva = PermissionChecker.canCreateReservation(sessionContext);
    private final boolean podeCancelarReserva = PermissionChecker.canCancelReservation(sessionContext);

    private JPanel containerMesas;
    private JPanel listaReservasPanel;
    private JLabel lblResumo;
    private final List<Reserva> reservasHoje = new ArrayList<>();

    public GestaoMesasPanel() {
        setLayout(new BorderLayout(0, 15));
        UIConstants.stylePanel(this);

        add(criarHeader(), BorderLayout.NORTH);
        add(criarConteudo(), BorderLayout.CENTER);
        add(criarLegenda(), BorderLayout.SOUTH);

        carregarMesas();
    }

    private JPanel criarHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(10, 10, 0, 10));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Mapa de Mesas & Reservas");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.FG_LIGHT);
        title.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.EVENT_SEAT, 28, UIConstants.FG_LIGHT));
        title.setIconTextGap(12);
        textos.add(title);

        lblResumo = new JLabel("Carregando reservas...");
        lblResumo.setFont(UIConstants.FONT_REGULAR);
        lblResumo.setForeground(UIConstants.FG_MUTED);
        lblResumo.setBorder(new EmptyBorder(6, 0, 0, 0));
        textos.add(lblResumo);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botoes.setOpaque(false);

        JButton btnNova = createButton("Nova Reserva", GoogleMaterialDesignIcons.ADD, UIConstants.PRIMARY_RED);
        btnNova.addActionListener(e -> abrirModalNovaReserva());
        btnNova.setVisible(podeCriarReserva);

        JButton btnGerenciarMesas = createButton("Gerenciar Mesas", GoogleMaterialDesignIcons.TUNE, UIConstants.BG_DARK_ALT);
        btnGerenciarMesas.addActionListener(e -> abrirModalGerenciarMesas());

        JButton btnRefresh = createButton("Atualizar", GoogleMaterialDesignIcons.REFRESH, UIConstants.BG_DARK_ALT);
        btnRefresh.addActionListener(e -> carregarMesas());

        botoes.add(btnNova);
        botoes.add(btnGerenciarMesas);
        botoes.add(btnRefresh);

        header.add(textos, BorderLayout.WEST);
        header.add(botoes, BorderLayout.EAST);
        return header;
    }

    private JComponent criarConteudo() {
        containerMesas = new JPanel(new GridLayout(0, 4, 15, 15));
        containerMesas.setBackground(UIConstants.BG_DARK);
        containerMesas.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollMesas = new JScrollPane(containerMesas);
        UIConstants.styleScrollPane(scrollMesas);
        scrollMesas.getVerticalScrollBar().setUnitIncrement(16);

        listaReservasPanel = new JPanel();
        listaReservasPanel.setLayout(new BoxLayout(listaReservasPanel, BoxLayout.Y_AXIS));
        listaReservasPanel.setBackground(UIConstants.BG_DARK);
        listaReservasPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollReservas = new JScrollPane(listaReservasPanel);
        UIConstants.styleScrollPane(scrollReservas);
        scrollReservas.setPreferredSize(new Dimension(320, 0));
        scrollReservas.getVerticalScrollBar().setUnitIncrement(16);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollMesas, scrollReservas);
        split.setResizeWeight(0.72);
        split.setBorder(null);
        split.setDividerSize(6);
        split.setOpaque(false);
        split.setBackground(UIConstants.BG_DARK);
        return split;
    }

    private JPanel criarLegenda() {
        JPanel legenda = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        legenda.setBackground(UIConstants.BG_DARK_ALT);
        legenda.add(criarItemLegenda("Livre", UIConstants.SUCCESS_GREEN));
        legenda.add(criarItemLegenda("Próxima", UIConstants.WARNING_ORANGE));
        legenda.add(criarItemLegenda("Check-in agora", UIConstants.PRIMARY_RED));
        legenda.add(criarItemLegenda("Atrasada", UIConstants.DANGER_RED));
        return legenda;
    }

    private void carregarMesas() {
        reservasHoje.clear();
        reservasHoje.addAll(dao.listarReservasDoDia());
        reservasHoje.sort(Comparator.comparing(Reserva::getDataReserva, Comparator.nullsLast(LocalDateTime::compareTo)));

        containerMesas.removeAll();
        for (String nomeMesa : dao.getListaMesas()) {
            Reserva reservaDaMesa = reservasHoje.stream()
                    .filter(r -> nomeMesa.equalsIgnoreCase(r.getMesa()))
                    .findFirst()
                    .orElse(null);
            containerMesas.add(criarCardMesa(nomeMesa, reservaDaMesa));
        }

        atualizarListaReservas();
        atualizarResumo();

        containerMesas.revalidate();
        containerMesas.repaint();
    }

    private void atualizarResumo() {
        int reservadas = reservasHoje.size();
        int ocupadasAgora = (int) reservasHoje.stream()
                .filter(r -> {
                    String status = classifyReservationStatus(r, nowInAppZone());
                    return "CHECK_IN".equals(status) || "ATRASADA".equals(status);
                })
                .map(Reserva::getMesa)
                .distinct()
                .count();
        int prontas = (int) reservasHoje.stream()
                .filter(r -> "CHECK_IN".equals(classifyReservationStatus(r, nowInAppZone())))
                .count();
        int atrasadas = (int) reservasHoje.stream()
                .filter(r -> "ATRASADA".equals(classifyReservationStatus(r, nowInAppZone())))
                .count();
        int livresAgora = Math.max(0, dao.getListaMesas().size() - ocupadasAgora);
        lblResumo.setText(String.format("Agora: %d livres • %d reservas hoje • %d em check-in • %d atrasadas",
                livresAgora, reservadas, prontas, atrasadas));
    }

    private void atualizarListaReservas() {
        listaReservasPanel.removeAll();

        JLabel titulo = new JLabel("Reservas de Hoje");
        titulo.setFont(UIConstants.FONT_BOLD);
        titulo.setForeground(UIConstants.FG_LIGHT);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        listaReservasPanel.add(titulo);
        listaReservasPanel.add(Box.createVerticalStrut(12));

        if (reservasHoje.isEmpty()) {
            JLabel vazio = new JLabel("Nenhuma reserva encontrada para o restaurante em contexto.");
            vazio.setFont(UIConstants.FONT_REGULAR);
            vazio.setForeground(UIConstants.FG_MUTED);
            vazio.setAlignmentX(Component.LEFT_ALIGNMENT);
            listaReservasPanel.add(vazio);
        } else {
            for (Reserva reserva : reservasHoje) {
                listaReservasPanel.add(criarCardReserva(reserva));
                listaReservasPanel.add(Box.createVerticalStrut(10));
            }
        }

        listaReservasPanel.revalidate();
        listaReservasPanel.repaint();
    }

    private JPanel criarCardMesa(String nomeMesa, Reserva reserva) {
        String status = classifyReservationStatus(reserva, nowInAppZone());
        Color corStatus = resolveStatusColor(status);

        UIConstants.RoundedPanel card = new UIConstants.RoundedPanel(18, UIConstants.CARD_DARK);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblNome = new JLabel(nomeMesa);
        lblNome.setFont(UIConstants.FONT_BOLD);
        lblNome.setForeground(UIConstants.FG_LIGHT);

        JLabel lblIcon = new JLabel(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.EVENT_SEAT, 36, corStatus));
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblInfo = new JLabel(buildReservationSummary(reserva, status));
        lblInfo.setFont(UIConstants.FONT_REGULAR);
        lblInfo.setForeground("LIVRE".equals(status) ? UIConstants.FG_MUTED : corStatus);
        lblInfo.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel badge = new JLabel(buildStatusLabel(status));
        badge.setFont(UIConstants.ARIAL_12_B);
        badge.setForeground(corStatus);

        card.add(lblNome, BorderLayout.NORTH);
        card.add(lblIcon, BorderLayout.CENTER);

        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setOpaque(false);
        rodape.add(lblInfo, BorderLayout.CENTER);
        rodape.add(badge, BorderLayout.SOUTH);
        card.add(rodape, BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (reserva == null) {
                    if (!podeCriarReserva) {
                        Toast.show(GestaoMesasPanel.this, "Seu perfil não pode criar reservas.", Toast.Type.WARNING);
                        return;
                    }
                    abrirModalNovaReserva(nomeMesa);
                } else {
                    abrirDetalhesReserva(reserva);
                }
            }
        });

        return card;
    }

    private JPanel criarCardReserva(Reserva reserva) {
        String status = classifyReservationStatus(reserva, nowInAppZone());
        Color cor = resolveStatusColor(status);

        UIConstants.RoundedPanel card = new UIConstants.RoundedPanel(16, UIConstants.BG_DARK_ALT);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        JLabel lblCliente = new JLabel(reserva.getNomeCliente());
        lblCliente.setFont(UIConstants.FONT_BOLD);
        lblCliente.setForeground(UIConstants.FG_LIGHT);

        JLabel lblHorario = new JLabel(
                reserva.getMesa() + " • " + reserva.getDataReserva().format(DATA_HORA_FORMATTER) + " • "
                        + reserva.getNumPessoas() + " pessoas");
        lblHorario.setFont(UIConstants.FONT_REGULAR);
        lblHorario.setForeground(UIConstants.FG_MUTED);

        JLabel lblStatus = new JLabel(buildStatusLabel(status));
        lblStatus.setFont(UIConstants.ARIAL_12_B);
        lblStatus.setForeground(cor);

        JPanel centro = new JPanel();
        centro.setOpaque(false);
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        centro.add(lblCliente);
        centro.add(Box.createVerticalStrut(6));
        centro.add(lblHorario);
        centro.add(Box.createVerticalStrut(6));
        centro.add(lblStatus);

        JButton btnDetalhes = createButton("Detalhes", GoogleMaterialDesignIcons.VISIBILITY, UIConstants.BG_DARK);
        btnDetalhes.addActionListener(e -> abrirDetalhesReserva(reserva));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(btnDetalhes);

        if (podeCancelarReserva) {
            JButton btnCancelar = createButton("Cancelar", GoogleMaterialDesignIcons.CANCEL, UIConstants.DANGER_RED);
            btnCancelar.addActionListener(e -> cancelarReserva(reserva));
            actions.add(btnCancelar);
        }

        card.add(centro, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    private void abrirDetalhesReserva(Reserva reserva) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Detalhes da Reserva", true);
        dialog.setLayout(new BorderLayout(0, 15));
        dialog.getContentPane().setBackground(UIConstants.BG_DARK);

        JPanel content = new JPanel();
        content.setBackground(UIConstants.BG_DARK);
        content.setBorder(new EmptyBorder(20, 20, 10, 20));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        String status = classifyReservationStatus(reserva, nowInAppZone());

        content.add(infoLabel("Cliente", reserva.getNomeCliente()));
        content.add(infoLabel("Mesa", reserva.getMesa()));
        content.add(infoLabel("Horário", reserva.getDataReserva().format(DATA_HORA_FORMATTER)));
        content.add(infoLabel("Pessoas", String.valueOf(reserva.getNumPessoas())));
        content.add(infoLabel("Status operacional", buildStatusLabel(status)));

        if ("CHECK_IN".equals(status)) {
            JLabel dica = new JLabel("Janela ideal para check-in: confirme a chegada e siga para o pedido da mesa.");
            dica.setForeground(UIConstants.SUCCESS_GREEN);
            dica.setFont(UIConstants.FONT_REGULAR);
            dica.setBorder(new EmptyBorder(12, 0, 0, 0));
            content.add(dica);
        } else if ("ATRASADA".equals(status)) {
            JLabel dica = new JLabel("Reserva atrasada: avalie contato com o cliente ou cancelamento.");
            dica.setForeground(UIConstants.DANGER_RED);
            dica.setFont(UIConstants.FONT_REGULAR);
            dica.setBorder(new EmptyBorder(12, 0, 0, 0));
            content.add(dica);
        }

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(UIConstants.BG_DARK);

        JButton btnFechar = createButton("Fechar", GoogleMaterialDesignIcons.CLOSE, UIConstants.BG_DARK_ALT);
        btnFechar.addActionListener(e -> dialog.dispose());
        footer.add(btnFechar);

        if (podeCancelarReserva) {
            JButton btnCancelar = createButton("Cancelar Reserva", GoogleMaterialDesignIcons.CANCEL, UIConstants.DANGER_RED);
            btnCancelar.addActionListener(e -> {
                dialog.dispose();
                cancelarReserva(reserva);
            });
            footer.add(btnCancelar);
        }

        dialog.add(content, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        configurarDialogResponsivo(dialog, 420, 300);
        dialog.setVisible(true);
    }

    private JPanel infoLabel(String titulo, String valor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel lblTitulo = new JLabel(titulo + ":");
        lblTitulo.setFont(UIConstants.ARIAL_12_B);
        lblTitulo.setForeground(UIConstants.FG_MUTED);

        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(UIConstants.FONT_BOLD);
        lblValor.setForeground(UIConstants.FG_LIGHT);

        panel.add(lblTitulo, BorderLayout.NORTH);
        panel.add(lblValor, BorderLayout.CENTER);
        return panel;
    }

    private void abrirModalGerenciarMesas() {
        JDialog modal = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Gerenciar Mesas", true);
        modal.setLayout(new BorderLayout(0, 10));
        modal.getContentPane().setBackground(UIConstants.BG_DARK);

        String[] cols = {"ID", "Mesa", "Capacidade", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tabelaMesas = new JTable(model);
        UIConstants.styleTable(tabelaMesas);

        Runnable recarregar = () -> {
            model.setRowCount(0);
            for (ReservaDAO.MesaConfig m : dao.listarMesasConfig()) {
                model.addRow(new Object[]{m.getId(), m.getNome(), m.getCapacidade(), m.isAtiva() ? "Ativa" : "Inativa"});
            }
        };
        recarregar.run();

        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        topo.setOpaque(false);
        JTextField txtMesa = new JTextField(14);
        UIConstants.styleField(txtMesa);
        txtMesa.putClientProperty("JTextField.placeholderText", "Mesa 01");
        JSpinner spCapacidade = new JSpinner(new SpinnerNumberModel(4, 1, 30, 1));
        UIConstants.styleSpinner(spCapacidade);
        JButton btnAdicionar = createButton("Adicionar", GoogleMaterialDesignIcons.ADD, UIConstants.SUCCESS_GREEN);
        btnAdicionar.addActionListener(e -> {
            String nome = txtMesa.getText() != null ? txtMesa.getText().trim() : "";
            if (nome.isBlank()) {
                Toast.show(modal, "Informe o nome da mesa.", Toast.Type.WARNING);
                return;
            }
            if (dao.salvarMesa(nome, ((Number) spCapacidade.getValue()).intValue())) {
                Toast.show(modal, "Mesa adicionada.", Toast.Type.SUCCESS);
                txtMesa.setText("");
                spCapacidade.setValue(4);
                recarregar.run();
                carregarMesas();
            } else {
                Toast.show(modal, "Não foi possível adicionar a mesa.", Toast.Type.ERROR);
            }
        });
        topo.add(new JLabel("Mesa:"));
        topo.add(txtMesa);
        topo.add(new JLabel("Capacidade:"));
        topo.add(spCapacidade);
        topo.add(btnAdicionar);

        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        acoes.setOpaque(false);
        JButton btnAtivar = createButton("Ativar/Inativar", GoogleMaterialDesignIcons.SWAP_HORIZ, UIConstants.BG_DARK_ALT);
        btnAtivar.addActionListener(e -> {
            int row = tabelaMesas.getSelectedRow();
            if (row < 0) {
                Toast.show(modal, "Selecione uma mesa.", Toast.Type.WARNING);
                return;
            }
            int id = (int) model.getValueAt(row, 0);
            String nome = String.valueOf(model.getValueAt(row, 1));
            int capacidade = Integer.parseInt(String.valueOf(model.getValueAt(row, 2)));
            boolean ativaAtual = "Ativa".equals(String.valueOf(model.getValueAt(row, 3)));
            if (dao.atualizarMesa(id, nome, capacidade, !ativaAtual)) {
                Toast.show(modal, "Status da mesa atualizado.", Toast.Type.SUCCESS);
                recarregar.run();
                carregarMesas();
            } else {
                Toast.show(modal, "Não foi possível atualizar a mesa.", Toast.Type.ERROR);
            }
        });
        JButton btnExcluir = createButton("Excluir", GoogleMaterialDesignIcons.DELETE, UIConstants.DANGER_RED);
        btnExcluir.addActionListener(e -> {
            int row = tabelaMesas.getSelectedRow();
            if (row < 0) {
                Toast.show(modal, "Selecione uma mesa.", Toast.Type.WARNING);
                return;
            }
            int id = (int) model.getValueAt(row, 0);
            if (dao.excluirMesa(id)) {
                Toast.show(modal, "Mesa removida.", Toast.Type.SUCCESS);
                recarregar.run();
                carregarMesas();
            } else {
                Toast.show(modal, "Não foi possível remover a mesa.", Toast.Type.ERROR);
            }
        });
        acoes.add(btnAtivar);
        acoes.add(btnExcluir);

        modal.add(topo, BorderLayout.NORTH);
        modal.add(new JScrollPane(tabelaMesas), BorderLayout.CENTER);
        modal.add(acoes, BorderLayout.SOUTH);
        configurarDialogResponsivo(modal, 640, 420);
        modal.setVisible(true);
    }

    private void abrirModalNovaReserva() {
        abrirModalNovaReserva(null);
    }

    private void abrirModalNovaReserva(String mesaPreSelecionada) {
        if (!podeCriarReserva) {
            Toast.show(this, "Seu perfil não pode criar reservas.", Toast.Type.WARNING);
            return;
        }

        JDialog modal = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Nova Reserva", true);
        modal.setLayout(new BorderLayout(0, 15));
        modal.getContentPane().setBackground(UIConstants.BG_DARK);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 20, 10, 20));
        form.setBackground(UIConstants.BG_DARK);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.insets = new Insets(6, 0, 6, 0);

        JTextField txtCliente = new JTextField();
        UIConstants.styleField(txtCliente);
        txtCliente.putClientProperty("JTextField.placeholderText", "Nome do cliente presente no salão");

        SpinnerDateModel dateModel = new SpinnerDateModel(
                Date.from(nowInAppZone().plusHours(1).atZone(APP_ZONE_ID).toInstant()),
                null,
                null,
                java.util.Calendar.MINUTE);
        JSpinner spDataHora = new JSpinner(dateModel);
        spDataHora.setEditor(new JSpinner.DateEditor(spDataHora, "dd/MM/yyyy HH:mm"));
        UIConstants.styleSpinner(spDataHora);

        JSpinner spPessoas = new JSpinner(new SpinnerNumberModel(2, 1, 20, 1));
        UIConstants.styleSpinner(spPessoas);

        JComboBox<String> comboMesas = new JComboBox<>(dao.getListaMesas().toArray(new String[0]));
        UIConstants.styleCombo(comboMesas);
        if (mesaPreSelecionada != null) {
            comboMesas.setSelectedItem(mesaPreSelecionada);
        }

        form.add(label("Cliente no salão"), gc);
        gc.gridy++;
        form.add(txtCliente, gc);
        gc.gridy++;
        form.add(label("Data e hora"), gc);
        gc.gridy++;
        form.add(spDataHora, gc);
        gc.gridy++;
        form.add(label("Número de pessoas"), gc);
        gc.gridy++;
        form.add(spPessoas, gc);
        gc.gridy++;
        form.add(label("Mesa"), gc);
        gc.gridy++;
        form.add(comboMesas, gc);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(UIConstants.BG_DARK);

        JButton btnCancelar = createButton("Cancelar", GoogleMaterialDesignIcons.CLOSE, UIConstants.BG_DARK_ALT);
        btnCancelar.addActionListener(e -> modal.dispose());

        JButton btnSalvar = createButton("Confirmar Reserva", GoogleMaterialDesignIcons.CHECK, UIConstants.SUCCESS_GREEN);
        btnSalvar.addActionListener(e -> {
            try {
                Reserva r = new Reserva();
                String nomeCliente = txtCliente.getText() != null ? txtCliente.getText().trim() : "";
                if (nomeCliente.isBlank()) {
                    Toast.show(modal, "Informe o nome do cliente para registrar a reserva.", Toast.Type.WARNING);
                    return;
                }
                int idCliente = dao.obterOuCriarClienteLocal(nomeCliente);
                if (idCliente <= 0) {
                    Toast.show(modal, "Não foi possível vincular o cliente ao restaurante atual.", Toast.Type.ERROR);
                    return;
                }

                r.setIdCliente(idCliente);
                r.setNomeCliente(nomeCliente);
                r.setIdRestaurante(sessionContext.getRestauranteEfetivo());
                r.setDataReserva(LocalDateTime.ofInstant(((Date) spDataHora.getValue()).toInstant(), APP_ZONE_ID));
                r.setNumPessoas(((Number) spPessoas.getValue()).intValue());
                r.setMesa((String) comboMesas.getSelectedItem());

                if (r.getDataReserva().isBefore(nowInAppZone().minusMinutes(TOLERANCIA_RESERVA_PASSADA_MINUTES))) {
                    Toast.show(modal, "Escolha um horário atual ou futuro para a reserva.", Toast.Type.WARNING);
                    return;
                }

                if (dao.criarReserva(r)) {
                    Toast.show(this, "Reserva registrada com sucesso.", Toast.Type.SUCCESS);
                    modal.dispose();
                    carregarMesas();
                } else {
                    Toast.show(modal, "Não foi possível criar a reserva. Verifique conflito de mesa e contexto.", Toast.Type.ERROR);
                }
            } catch (Exception ex) {
                Toast.show(modal, "Erro ao validar os dados da reserva.", Toast.Type.ERROR);
            }
        });

        footer.add(btnCancelar);
        footer.add(btnSalvar);

        modal.add(form, BorderLayout.CENTER);
        modal.add(footer, BorderLayout.SOUTH);
        configurarDialogResponsivo(modal, 460, 400);
        modal.setVisible(true);
    }

    private void configurarDialogResponsivo(JDialog dialog, int minWidth, int minHeight) {
        dialog.pack();
        dialog.setMinimumSize(new Dimension(minWidth, minHeight));
        Dimension base = getSize();
        int width = Math.max(dialog.getWidth(), minWidth);
        int height = Math.max(dialog.getHeight(), minHeight);
        if (base.width > 0 && base.height > 0) {
            width = Math.min(width, (int) (base.width * 0.95));
            height = Math.min(height, (int) (base.height * 0.95));
        }
        dialog.setSize(width, height);
        dialog.setLocationRelativeTo(this);
    }

    private void cancelarReserva(Reserva reserva) {
        UIConstants.showConfirmDialog(this,
                "Cancelar reserva",
                "Deseja cancelar a reserva da " + reserva.getMesa() + " para " + reserva.getNomeCliente() + "?",
                () -> {
                    if (dao.cancelarReserva(reserva.getIdReserva())) {
                        Toast.show(this, "Reserva cancelada.", Toast.Type.SUCCESS);
                        carregarMesas();
                    } else {
                        Toast.show(this, "Não foi possível cancelar a reserva.", Toast.Type.ERROR);
                    }
                });
    }

    private JLabel label(String txt) {
        JLabel l = new JLabel(txt);
        l.setForeground(UIConstants.FG_LIGHT);
        l.setFont(UIConstants.FONT_BOLD);
        l.setBorder(new EmptyBorder(4, 0, 0, 0));
        return l;
    }

    private JLabel criarItemLegenda(String txt, Color cor) {
        JLabel l = new JLabel("  " + txt);
        l.setForeground(UIConstants.FG_LIGHT);
        l.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.FIBER_MANUAL_RECORD, 12, cor));
        return l;
    }

    private JButton createButton(String text, GoogleMaterialDesignIcons icon, Color bg) {
        JButton btn = new JButton(text);
        btn.setIcon(IconFontSwing.buildIcon(icon, 18, UIConstants.SEL_FG));
        if (UIConstants.SUCCESS_GREEN.equals(bg)) {
            UIConstants.styleSuccess(btn);
        } else if (UIConstants.DANGER_RED.equals(bg)) {
            UIConstants.styleDanger(btn);
        } else if (UIConstants.PRIMARY_RED.equals(bg)) {
            UIConstants.stylePrimary(btn);
        } else {
            UIConstants.styleSecondary(btn);
        }
        return btn;
    }

    static String classifyReservationStatus(Reserva reserva, LocalDateTime now) {
        if (reserva == null || reserva.getDataReserva() == null) {
            return "LIVRE";
        }
        LocalDateTime dataReserva = reserva.getDataReserva();
        if (dataReserva.isBefore(now.minusMinutes(CHECK_IN_WINDOW_MINUTES))) {
            return "ATRASADA";
        }
        if (!dataReserva.isAfter(now.plusMinutes(CHECK_IN_WINDOW_MINUTES))) {
            return "CHECK_IN";
        }
        return "PROXIMA";
    }

    static Color resolveStatusColor(String status) {
        return switch (status) {
            case "CHECK_IN" -> UIConstants.PRIMARY_RED;
            case "ATRASADA" -> UIConstants.DANGER_RED;
            case "PROXIMA" -> UIConstants.WARNING_ORANGE;
            default -> UIConstants.SUCCESS_GREEN;
        };
    }

    private static String buildStatusLabel(String status) {
        return switch (status) {
            case "CHECK_IN" -> "Check-in agora";
            case "ATRASADA" -> "Atrasada";
            case "PROXIMA" -> "Próxima";
            default -> "Livre";
        };
    }

    private static String buildReservationSummary(Reserva reserva, String status) {
        if (reserva == null) {
            return "Livre";
        }
        return switch (status) {
            case "CHECK_IN" -> "Chegada prevista às " + reserva.getDataReserva().format(HORA_FORMATTER);
            case "ATRASADA" -> "Reserva das " + reserva.getDataReserva().format(HORA_FORMATTER);
            default -> "Reservada às " + reserva.getDataReserva().format(HORA_FORMATTER);
        };
    }

    private static LocalDateTime nowInAppZone() {
        return LocalDateTime.now(APP_ZONE_ID);
    }
}
