package com.senac.food.verse;

/**
 * Contexto de sessão do usuário logado.
 * Singleton — preenchido no login e limpo no logout.
 *
 * Regras:
 *  - isAdmin == true  → acesso global (sem restaurante obrigatório)
 *  - isAdmin == false → restauranteId é obrigatório
 *  - restauranteSelecionadoId: usado pelo Admin quando entra no contexto de um restaurante específico
 */
public final class SessionContext {

    private static SessionContext instance;

    private int    funcionarioId;
    private String nome;
    private String cargo;
    private String status;
    private int    restauranteId;          // ID do restaurante vinculado ao funcionário
    private String nomeRestaurante;        // Nome do restaurante efetivo (para exibição)
    private boolean isAdmin;
    private int    restauranteSelecionadoId; // Admin escolheu entrar no contexto deste restaurante

    private SessionContext() {}

    /** Retorna (ou cria) a instância única. */
    public static SessionContext getInstance() {
        if (instance == null) {
            instance = new SessionContext();
        }
        return instance;
    }

    /** Preenche o contexto após login bem-sucedido. */
    public void inicializar(int funcionarioId, String nome, String cargo, String status, int restauranteId) {
        this.funcionarioId              = funcionarioId;
        this.nome                       = nome;
        this.cargo                      = cargo;
        this.status                     = status;
        this.restauranteId              = restauranteId;
        this.isAdmin                    = "Admin".equalsIgnoreCase(cargo);
        this.restauranteSelecionadoId   = 0;
    }

    /** Limpa o contexto no logout. */
    public void limpar() {
        funcionarioId            = 0;
        nome                     = null;
        cargo                    = null;
        status                   = null;
        restauranteId            = 0;
        nomeRestaurante          = null;
        isAdmin                  = false;
        restauranteSelecionadoId = 0;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public int getFuncionarioId() { return funcionarioId; }
    public String getNome()       { return nome; }
    public String getCargo()      { return cargo; }
    public String getStatus()     { return status; }
    public int getRestauranteId() { return restauranteId; }
    public boolean isAdmin()      { return isAdmin; }

    public int getRestauranteSelecionadoId() { return restauranteSelecionadoId; }
    public void setRestauranteSelecionadoId(int id) { this.restauranteSelecionadoId = id; }

    public String getNomeRestaurante() { return nomeRestaurante; }
    public void setNomeRestaurante(String nomeRestaurante) { this.nomeRestaurante = nomeRestaurante; }

    /**
     * Retorna o ID do restaurante efetivo para filtrar dados:
     *  - Se não-Admin: usa restauranteId do login.
     *  - Se Admin com restaurante selecionado: usa restauranteSelecionadoId.
     *  - Se Admin sem restaurante selecionado: retorna 0 (sem filtro / global).
     */
    public int getRestauranteEfetivo() {
        if (!isAdmin) return restauranteId;
        return restauranteSelecionadoId;
    }

    /** Admin está operando dentro do contexto de um restaurante específico? */
    public boolean adminTemContextoRestaurante() {
        return isAdmin && restauranteSelecionadoId > 0;
    }

    /**
     * Retorna o nome do restaurante em contexto para exibição na UI.
     * Evita exibir identificadores técnicos quando o nome não estiver disponível.
     */
    public String getRestauranteLabel() {
        if (nomeRestaurante != null && !nomeRestaurante.isBlank()) {
            return nomeRestaurante.trim();
        }
        return "restaurante sem nome cadastrado";
    }
}
