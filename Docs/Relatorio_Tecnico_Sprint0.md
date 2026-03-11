# Relatório Técnico — Sprint 0

## 1. Situação atual mapeada

### Login Java Swing
- **Tela de login:** `/home/runner/work/FoodVerse/FoodVerse/FoodVerse/src/main/java/com/senac/food/verse/gui/TelaInicial.java`
- **Ação de login:** método `logar()`
- **Leitura de cargo/status/restaurante:** `/home/runner/work/FoodVerse/FoodVerse/FoodVerse/src/main/java/com/senac/food/verse/Funcionario.java`, método `loginComContexto(...)`
- **Contexto em memória:** `/home/runner/work/FoodVerse/FoodVerse/FoodVerse/src/main/java/com/senac/food/verse/SessionContext.java`

### Regras já implementadas no Java
- `cargo = "Admin"` entra como **Admin global**
- `status != "ativo"` bloqueia login
- não-admin sem `ID_restaurante` é bloqueado
- não-admin com restaurante `ativo = false` é bloqueado
- o contexto de sessão já guarda:
  - `funcionarioId`
  - `nome`
  - `cargo`
  - `status`
  - `restauranteId`
  - `restauranteSelecionadoId` para Admin

## 2. Telas/painéis Java e navegação

### Tela principal
- `/home/runner/work/FoodVerse/FoodVerse/FoodVerse/src/main/java/com/senac/food/verse/gui/TelaInicial.java`

### Painéis carregados no dashboard
- `HomePanel`
- `AdminRestaurantesPanel`
- `MeuRestaurantePanel`
- `CardapioPainel`
- `EstoquePainel`
- `PedidosPanel`
- `GestaoMesasPanel`
- `GestaoCozinhaPanel`
- `EntregasPainel`
- `AprovacaoCadastrosPanel`

### Regra de navegação atual
- o menu lateral é montado por `construirMenuPorCargo(String cargo)`
- **Admin** recebe a seção **Admin Global**
- **Admin/Gerente** recebem a seção **Meu Restaurante**
- módulos operacionais usam `SessionContext.getRestauranteEfetivo()`

## 3. DAOs e tabelas utilizadas

### Pedidos
- **DAO:** `/home/runner/work/FoodVerse/FoodVerse/FoodVerse/src/main/java/com/senac/food/verse/PedidoDAO.java`
- **Tabelas:** `tb_pedidos`, `tb_clientes`, `tb_status_pedido`, `tb_pagamentos`, `tb_pedidos_produtos`
- **Escopo por restaurante:** já usa `p.ID_restaurante = ?` quando existe contexto

### Estoque
- **DAO:** `/home/runner/work/FoodVerse/FoodVerse/FoodVerse/src/main/java/com/senac/food/verse/EstoqueDAO.java`
- **Tabelas:** `tb_estoque`, `tb_produtos`
- **Escopo por restaurante:** já usa `p.ID_restaurante = ?`

### Cardápio
- **DAO:** `/home/runner/work/FoodVerse/FoodVerse/FoodVerse/src/main/java/com/senac/food/verse/CardapioDAO.java`
- **Tabelas:** `tb_produtos`, `tb_nutricao`, `tb_estoque`
- **Escopo por restaurante:** já usa `ID_restaurante = ?`

### Reservas
- **DAO:** `/home/runner/work/FoodVerse/FoodVerse/FoodVerse/src/main/java/com/senac/food/verse/ReservaDAO.java`
- **Tabelas:** `tb_reservas`, `tb_clientes`
- **Escopo por restaurante:** já usa `r.ID_restaurante = ?`

### Funcionários
- **Classe/consultas principais:** `/home/runner/work/FoodVerse/FoodVerse/FoodVerse/src/main/java/com/senac/food/verse/Funcionario.java`
- **Tela de gestão:** `/home/runner/work/FoodVerse/FoodVerse/FoodVerse/src/main/java/com/senac/food/verse/gui/AprovacaoCadastrosPanel.java`
- **Tabela:** `tb_funcionarios`

### Restaurantes
- **Tela global:** `/home/runner/work/FoodVerse/FoodVerse/FoodVerse/src/main/java/com/senac/food/verse/gui/AdminRestaurantesPanel.java`
- **Tela do restaurante:** `/home/runner/work/FoodVerse/FoodVerse/FoodVerse/src/main/java/com/senac/food/verse/gui/MeuRestaurantePanel.java`
- **Tabela:** `tb_restaurantes`

## 4. Confirmações de schema no repositório

### `tb_restaurantes`
Arquivo: `/home/runner/work/FoodVerse/FoodVerse/Banco de Dados/Query`

Colunas relevantes já presentes no script-base:
- `ID_restaurante`
- `nome`
- `categoria`
- `descricao`
- `avaliacao`
- `tempo_entrega`
- `taxa_entrega`
- `cupom`
- `imagem`
- `ativo`
- `aberto`

### `tb_funcionarios`
Arquivo: `/home/runner/work/FoodVerse/FoodVerse/Banco de Dados/Query`

Colunas relevantes:
- `ID_funcionario`
- `ID_restaurante`
- `nome`
- `username`
- `email`
- `cargo`
- `telefone`
- `senha`
- `status`
- `data_cadastro`

Observação importante:
- o script-base do repositório **já contém** `ID_restaurante`
- a migração Sprint 1 precisava ser endurecida para casos legados em que a coluna ainda não exista no banco real

## 5. Confirmações Django

### Models
Arquivo: `/home/runner/work/FoodVerse/FoodVerse/Web/projeto/foodverse/models.py`

Confirmações:
- `TbProdutos.restaurante` usa FK `db_column="ID_restaurante"`
- `TbPedidos.restaurante` usa FK `db_column="ID_restaurante"`
- `TbReservas.restaurante` usa FK `db_column="ID_restaurante"`
- `TbFuncionarios.restaurante` usa FK `db_column="ID_restaurante"`
- `TbRestaurantes` já expõe `ativo` e `aberto`

### Ajuste aplicado nesta sprint
- o portal Django agora consulta apenas restaurantes com `ativo=True` para listagem e detalhe público
- isso alinha o marketplace com a regra de **ativo na plataforma**

## 6. Proposta de ALTER TABLE mínimo

Arquivo de referência:
- `/home/runner/work/FoodVerse/FoodVerse/Banco de Dados/Migration_Sprint1.sql`

### Alterações mínimas propostas
1. garantir `tb_restaurantes.ativo BIT NOT NULL DEFAULT 1`
2. garantir `tb_restaurantes.aberto BIT NOT NULL DEFAULT 1`
3. garantir `tb_funcionarios.ID_restaurante INT NULL`
4. garantir FK `tb_funcionarios(ID_restaurante) -> tb_restaurantes(ID_restaurante)`
5. aplicar regra:
   - `cargo = 'Admin'` pode ter `ID_restaurante = NULL`
   - demais cargos devem ter `ID_restaurante IS NOT NULL`

### Estratégia de migração conservadora
- Admin existente: `ID_restaurante = NULL`
- se houver **apenas 1 restaurante**, vincular automaticamente os não-admin sem restaurante
- se houver mais de 1 restaurante, **não vincular automaticamente** para evitar associação errada
- registrar a pendência e exigir correção manual
- usar `CHECK ... WITH NOCHECK` para proteger novos dados sem quebrar legado ambíguo

## 7. Plano de telas

### Admin Global
- tela base atual: `AdminRestaurantesPanel`
- responsabilidades:
  - listar restaurantes
  - criar/editar restaurante
  - ativar/inativar `ativo`
  - visualizar `aberto`
  - entrar no contexto de um restaurante

### Restaurante
- tela base atual: `MeuRestaurantePanel`
- responsabilidades:
  - editar dados do restaurante
  - abrir/fechar operação via `aberto`

### Operacional por restaurante
- painéis que já dependem de contexto ou devem depender:
  - `CardapioPainel`
  - `EstoquePainel`
  - `PedidosPanel`
  - `GestaoMesasPanel`
  - `GestaoCozinhaPanel`
  - `EntregasPainel`
  - `AprovacaoCadastrosPanel`

## 8. Riscos remanescentes

- o Java já filtra vários DAOs por restaurante, mas **Admin sem contexto ainda pode abrir módulos que operam sem filtro**
- a migração Django inicial não refletia `ativo`/`aberto`; foi alinhada com uma nova migration compatível
- os testes Maven existentes têm falhas prévias ligadas a banco real/offline e não foram tratados por não fazerem parte desta sprint

## 9. Próximo passo recomendado

- Sprint 2: bloquear explicitamente telas operacionais quando o Admin não tiver `restauranteSelecionadoId`
- Sprint 3/4: concluir a navegação ao “entrar no contexto” levando o Admin diretamente para a tela inicial do restaurante
