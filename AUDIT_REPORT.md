# FoodVerse — Relatório de Auditoria Completa

**Data:** 2026-03-12  
**Escopo:** Java Swing + Django + Banco de Dados (`FoodVerseDB`)

---

## A) Resumo Executivo — Top 10 Problemas Críticos

| # | Severidade | Problema | Impacto |
|---|-----------|----------|---------|
| 1 | **P0** | `Migration_Sprint1.sql` ausente — testes Java falham | **CORRIGIDO** nesta auditoria |
| 2 | **P0** | Django model `TbRestaurantes` não possuía campo `banner` — Java grava/lê `banner` mas Django ignora | **CORRIGIDO** nesta auditoria |
| 3 | **P0** | `MEDIA_ROOT`/`MEDIA_URL` não definidos em `settings.py` — `urls.py` referencia mas dá erro | **CORRIGIDO** nesta auditoria |
| 4 | **P0** | Django exibe restaurantes inativos (`ativo=False`) aos clientes | **CORRIGIDO** nesta auditoria |
| 5 | **P1** | Login Django não redireciona após sucesso (redirect comentado) | **CORRIGIDO** nesta auditoria |
| 6 | **P1** | Cadastro Django não redireciona após sucesso (redirect comentado) | **CORRIGIDO** nesta auditoria |
| 7 | **P1** | `tb_cupons` e `tb_fidelidade` sem escopo por restaurante — são tabelas globais sem vínculo multi-restaurante | Pendente |
| 8 | **P1** | Java não usa `tb_cupons`, `tb_fidelidade` — funcionalidades inexistentes no lado operacional | Pendente |
| 9 | **P2** | Debug `print()` residual em `views.py` linha 200 | Pendente (baixo risco) |
| 10 | **P2** | Imagens usam caminho relativo `imagens/restaurantes/` no Java vs. padrão `media/` indefinido no Django | Proposta documentada |

---

## B) Inventário do Sistema

### B.1 Java (Swing) — Telas, DAOs, Fluxos

| Módulo | Arquivo(s) Principal(is) | Função |
|--------|------------------------|--------|
| Login | `TelaLogin.java` | Autenticação de funcionários via `tb_funcionarios` |
| Dashboard | `TelaInicial.java`, `HomePanel.java` | Painel principal com cards de navegação |
| Gestão de Restaurantes | `AdminRestaurantesPanel.java` | Admin: CRUD de restaurantes (criar, editar, ativar/inativar) |
| Meu Restaurante | `MeuRestaurantePanel.java` | Gerente: editar dados, abrir/fechar, upload imagens |
| Cardápio | `CardapioPanel.java` | CRUD de produtos/pratos |
| Estoque | `EstoquePanel.java`, `EstoqueDAO.java` | Gestão de estoque com movimentações |
| Pedidos | `PedidosPanel.java`, `PedidoDAO.java` | Listar/gerenciar pedidos recebidos |
| KDS (Cozinha) | `KitchenDashboardPanel.java`, `KitchenOrderDAO.java` | Painel de cozinha com fila de preparo |
| Entregas | `EntregasPainel.java` | Gestão de entregas e motoboys |
| Mesas/Reservas | `GestaoMesasPanel.java`, `ReservaDAO.java` | Mapa de mesas e reservas |
| Usuários | `CadastroFuncionario.java`, `AprovacaoCadastrosPanel.java` | CRUD e aprovação de funcionários |
| Sessão | `SessionContext.java` | Contexto global: cargo, restaurante efetivo, Admin vs Gerente |
| Permissões | `PermissionChecker.java` | Controle de acesso por cargo e contexto |
| Conexão | `ConexaoBanco.java` | Pool JDBC para SQL Server |

### B.2 Django — Views, Models, Fluxos

| Módulo | Arquivo(s) | Função |
|--------|-----------|--------|
| Home | `views.py:home` | Página inicial do portal do cliente |
| Login/Cadastro | `views.py:login_view`, `cadastro_view` | Autenticação de clientes via `tb_clientes` |
| Perfil | `views.py:perfil_view`, `editar_perfil_view` | Visualizar/editar perfil do cliente |
| Catálogo | `views.py:restaurante_view`, `restaurante_detalhe_view`, `prato_view` | Listar restaurantes, pratos, detalhes |
| Busca | `views.py:buscar_prato_restaurante` | Busca por nome de restaurante ou prato |
| Carrinho | `views.py:carrinho_view`, `adicionar_carrinho`, `aumentar_item`, `diminuir_item` | Carrinho de compras em sessão |
| Pedidos | `views.py:pedido_view`, `finalizacao_view` | Finalizar pedidos, criar pagamento, baixar estoque |
| Reservas | `views.py:reserva_view`, `reserva_pagamento` | Criar reservas de mesas |
| Avaliações | `views.py:feedback_view` | Avaliar restaurante |
| Models | `models.py` | 13 models mapeando todas as tabelas |
| Context | `context_processors.py` | Cliente logado + carrinho no template |
| Seed | `seed.py` | Dados de teste |

### B.3 Banco de Dados — Tabelas

| Tabela | Campos principais | Java usa? | Django usa? |
|--------|------------------|-----------|-------------|
| `tb_clientes` | id_cliente, username, nome, email, cpf, senha, endereco | ❌ (só lê em ReservaDAO) | ✅ Login, perfil |
| `tb_restaurantes` | id_restaurante, nome, categoria, descricao, avaliacao, tempo_entrega, taxa_entrega, cupom, imagem, **banner**, ativo, aberto | ✅ CRUD completo | ✅ Catálogo (agora com banner) |
| `tb_funcionarios` | id_funcionario, id_restaurante, nome, username, cargo, senha, status | ✅ Login, CRUD | ❌ |
| `tb_produtos` | id_produto, id_restaurante, nome_produto, descricao, preco, categoria, imagem, disponivel | ✅ Cardápio + Estoque | ✅ Catálogo |
| `tb_estoque` | id_estoque, id_produto, quantidade, estoque_minimo | ✅ EstoqueDAO | ✅ Baixa em pedido |
| `tb_nutricao` | id_nutricao, id_produto, kcal, proteina, carbo, gordura | ❌ | ✅ Detalhe prato |
| `tb_status_pedido` | id_status, nome_status | ✅ KDS/Pedidos | ✅ Pedidos |
| `tb_pedidos` | id_pedido, id_cliente, id_restaurante, status_id, valor_total, data_pedido, endereco_entrega | ✅ PedidoDAO | ✅ Finalização |
| `tb_pedidos_produtos` | id_pedido, id_produto, quantidade | ✅ PedidoDAO | ✅ Finalização |
| `tb_reservas` | id_reserva, id_cliente, id_restaurante, data_reserva, numero_pessoas, mesa | ✅ ReservaDAO | ✅ Reservas |
| `tb_pagamentos` | id_pagamento, id_pedido, metodo_pagamento, valor, data_pagamento | ✅ PedidoDAO (leitura) | ✅ Finalização (escrita) |
| `tb_avaliacoes` | id_avaliacao, id_cliente, id_restaurante, comentario, nota, data_avaliacao | ❌ | ✅ Feedback |
| `tb_cupons` | id_cupom, codigo, desconto, validade | ❌ | ✅ Seed/Model |
| `tb_fidelidade` | id_fidelidade, id_cliente, pontos, cashback | ❌ | ✅ Seed/Model |

---

## C) Checklist Multi-Restaurante (OK/NOK)

| Entidade | Vinculada a Restaurante? | Status | Evidência |
|----------|------------------------|--------|-----------|
| **tb_restaurantes** | — (é a entidade raiz) | ✅ OK | Possui `ativo`+`aberto` |
| **tb_funcionarios** | `ID_restaurante` FK | ✅ OK | Admin tem `ID_restaurante=NULL`, FK com ON DELETE SET NULL |
| **tb_produtos** | `ID_restaurante` FK | ✅ OK | Sempre vinculado |
| **tb_estoque** | Via `tb_produtos.ID_restaurante` | ✅ OK | JOIN garante escopo no EstoqueDAO |
| **tb_pedidos** | `ID_restaurante` FK | ✅ OK | Django cria com restaurante, Java lê com escopo |
| **tb_pedidos_produtos** | Via `tb_pedidos` + `tb_produtos` | ✅ OK | Consistência implícita |
| **tb_reservas** | `ID_restaurante` FK | ✅ OK | ReservaDAO filtra por restauranteEfetivo |
| **tb_avaliacoes** | `ID_restaurante` FK | ✅ OK | Django cria com restaurante |
| **tb_pagamentos** | Via `tb_pedidos.ID_pedido` | ✅ OK | Vínculo indireto via pedido |
| **tb_cupons** | ❌ **SEM vínculo** | ⚠️ NOK | Tabela global sem `ID_restaurante`. Impacto: cupom se aplica a qualquer restaurante sem controle |
| **tb_fidelidade** | ❌ **SEM vínculo** | ⚠️ NOK | Tabela global por cliente, sem escopo restaurante. Pode ser intencional (programa global) |
| **tb_nutricao** | Via `tb_produtos.ID_restaurante` | ✅ OK | Vínculo indireto via produto |

### Recomendações para NOKs:
- **tb_cupons**: Adicionar `ID_restaurante INT NULL` (FK) para cupons por restaurante. Cupons com `NULL` seriam globais.
- **tb_fidelidade**: Definir se programa é global (manter como está) ou por restaurante (adicionar FK). Para MVP, manter global é aceitável.

---

## D) Bugs e Erros — Lista Completa

### Bugs Corrigidos nesta Auditoria

| ID | Sev. | Onde | Evidência | Causa | Correção |
|----|------|------|-----------|-------|----------|
| BUG-001 | P0 | Banco | `Banco de Dados/Migration_Sprint1.sql` ausente | Arquivo nunca foi criado; `SchemaMigrationScriptTest` falha | Criado script idempotente com colunas `ativo`, `aberto`, `banner`, `imagem`, FK `ID_restaurante` |
| BUG-002 | P0 | Django | `models.py:TbRestaurantes` sem campo `banner` | Java lê/grava `banner` via SQL, Django ignora — dados perdidos no Django | Adicionado `banner = models.CharField(max_length=255, null=True, blank=True)` |
| BUG-003 | P0 | Django | `settings.py` sem `MEDIA_ROOT`/`MEDIA_URL` | `urls.py:42` referencia `settings.MEDIA_URL` — erro em runtime | Adicionado `MEDIA_URL = '/media/'` e `MEDIA_ROOT = BASE_DIR / 'media'` |
| BUG-004 | P0 | Django | `views.py:restaurante_view`, `buscar_prato_restaurante` | `TbRestaurantes.objects.all()` exibe restaurantes inativos | Filtrado por `ativo=True` em todas as queries públicas |
| BUG-005 | P1 | Django | `views.py:login_view` linha ~83 | `redirect('home')` comentado — login bem-sucedido não redireciona | Descomentado redirect |
| BUG-006 | P1 | Django | `views.py:cadastro_view` linha ~134 | `redirect('login')` comentado — cadastro não redireciona | Descomentado redirect |

### Bugs Encontrados (Pendentes)

| ID | Sev. | Onde | Evidência | Causa | Correção Recomendada |
|----|------|------|-----------|-------|---------------------|
| BUG-007 | P1 | Django | `views.py` não filtra por `aberto=True` para novos pedidos | Cliente pode fazer pedido em restaurante fechado | Adicionar `aberto=True` ao filtro de `restaurante_detalhe_view` ou na finalização |
| BUG-008 | P1 | Java | `tb_cupons`, `tb_fidelidade` sem escopo restaurante | Cupons e fidelidade são globais sem FK restaurante | Adicionar `ID_restaurante` a `tb_cupons` (opcional em `tb_fidelidade`) |
| BUG-009 | P2 | Django | `views.py:200` — `print(f"Recebendo...")` | Debug residual em produção | Remover ou usar `logging.debug()` |
| BUG-010 | P2 | Django | `views.py:home` linha ~53 | `TbRestaurantes.objects.all()` na home também mostra inativos | Filtrar por `ativo=True` |
| BUG-011 | P2 | Java | `MeuRestaurantePanel.IMAGES_DIR = "imagens/restaurantes"` | Caminho relativo ao working dir; não compatível com `media/` do Django | Unificar para `media/restaurantes` |
| BUG-012 | P2 | Java | `EstoqueDAO` movimentações são apenas in-memory (`MOVS_MOCK`) | Histórico de movimentações se perde ao reiniciar; não persiste no banco | Criar tabela `tb_movimentacoes_estoque` |
| BUG-013 | P2 | Java | `ReservaDAO.getListaMesas()` retorna lista fixa 1-20 | Número de mesas é hardcoded, não vem do banco | Tornar dinâmico por restaurante |
| BUG-014 | P3 | Django | `reserva_view` exibe datas/horários que JÁ existem como opção | Confuso: mostra slots ocupados, não disponíveis | Inverter lógica para mostrar slots livres |
| BUG-015 | P3 | Django | `finalizacao_view` usa `entrega=Decimal('5.00')` fixo | Taxa de entrega deveria vir de `tb_restaurantes.taxa_entrega` | Usar `restaurante_instancia.taxa_entrega` |
| BUG-016 | P3 | Django | `finalizacao_view` desconto arbitrário `>50 = R$10` | Deveria aplicar cupons reais de `tb_cupons` | Integrar sistema de cupons |

---

## E) Divergências Java × Django

### E.1 Divergências de Campo

| Tabela | Campo | Java | Django | Ação |
|--------|-------|------|--------|------|
| `tb_restaurantes` | `banner` | ✅ Lê/grava | ✅ **(corrigido)** | Modelo alinhado |
| `tb_restaurantes` | `cupom` | ❌ Não usa | ✅ Model existe | Nenhuma ação imediata |
| `tb_produtos` | `preco` | ❌ Não usa diretamente | ✅ Usa no carrinho | Java foca em estoque, Django em catálogo |
| `tb_produtos` | `destaque` | ❌ Não usa | ✅ Model existe | Sem uso real em views |
| `tb_produtos` | `tempo_preparo` | ❌ Não usa | ✅ Model existe | Poderia ser usado no KDS |
| `tb_nutricao` | — | ❌ Não usa | ✅ Detalhe do prato | Informacional, OK no Django |

### E.2 Divergências de Status

| Entidade | Java | Django | Divergência |
|----------|------|--------|-------------|
| Pedido | Status via KDS: Pendente → Em Preparo → Pronto → Entregue | Status via `tb_status_pedido.id_status` (1 = default) | Java faz transição completa; Django só cria com status=1 |
| Restaurante | `ativo` + `aberto` (separados, botões) | `ativo` + `aberto` (model OK, views não usavam `ativo`) | **Corrigido**: views agora filtram `ativo=True` |
| Funcionário | `status` = Pendente/Ativo/Inativo | Model tem campo `status`, mas Django não usa | Sem conflito (Django não gerencia funcionários) |
| Reserva | `status` = "RESERVADA" (hardcoded no DAO) | Sem campo `status` no model | Sem tabela de status de reserva |

### E.3 Divergências de Regra de Negócio

| Regra | Java | Django | Impacto |
|-------|------|--------|---------|
| Quem faz pedido | — (Java é operação) | Cliente via carrinho+finalização | OK (separação de responsabilidades) |
| Quem gerencia estoque | EstoqueDAO com movimentações | Baixa automática na finalização | Podem conflitar se ambos atualizarem ao mesmo tempo |
| Quem cria reserva | ReservaDAO via GestaoMesasPanel | Cliente via `reserva_view` | OK (ambos gravam em `tb_reservas`) |
| Conflito de reserva | `existeConflitoReserva()` com janela de 90min | ❌ Sem verificação de conflito | Django permite reservas sobrepostas |
| Pagamento | Só leitura (PedidoDAO lê `tb_pagamentos`) | Cria registro na finalização | OK (Django escreve, Java lê) |
| Cupons | ❌ Não implementado | Model + seed existem, mas sem aplicação real | Gap funcional |
| Fidelidade/Cashback | ❌ Não implementado | Model + seed existem, mas sem aplicação real | Gap funcional |
| Avaliações | ❌ Não implementado | Feedback view funciona | Gap: Java poderia exibir avaliações |

### E.4 Divergências de Fluxo

| Fluxo | Java | Django |
|-------|------|--------|
| Imagens | Copia para `imagens/restaurantes/`, grava path relativo no DB | Sem upload funcional (MEDIA não configurada — **corrigido**) |
| Autenticação | `tb_funcionarios` com bcrypt | `tb_clientes` com Django `make_password`/`check_password` |
| Sessão | `SessionContext` singleton | Django session middleware |

---

## F) Imagens — Proposta Final

### F.1 Estado Atual

| Aspecto | Java | Django |
|---------|------|--------|
| Campos DB | `tb_restaurantes.imagem`, `tb_restaurantes.banner`, `tb_produtos.imagem` | Mesmos campos (banner adicionado) |
| Armazenamento | Copia arquivo para `imagens/restaurantes/` (relativo ao working dir) | Sem upload funcional |
| Tipo de valor | Caminho relativo do arquivo (ex: `imagens/restaurantes/foto.jpg`) | CharField (pode ser URL ou path) |
| Configuração | `MeuRestaurantePanel.IMAGES_DIR = "imagens/restaurantes"` | `MEDIA_ROOT` agora definido como `BASE_DIR / 'media'` |

### F.2 Campos de Banco Recomendados (sem alteração de schema)

Os campos atuais (`imagem VARCHAR(255)`, `banner VARCHAR(255)`) são suficientes. O valor armazenado deve seguir convenção:

- **Caminho local**: `media/restaurantes/<id>/logo.jpg` — relativo à raiz do projeto
- **URL externa**: `https://exemplo.com/imagem.jpg` — URL completa

**Regra de interpretação:**
- Se o valor começa com `http://` ou `https://` → é URL
- Caso contrário → é caminho local relativo

Isso dispensa colunas adicionais (`imagem_tipo`, `imagem_url`) e mantém compatibilidade.

### F.3 Estrutura de Pasta `media/`

```
media/
├── restaurantes/
│   ├── 1/
│   │   ├── logo.jpg
│   │   └── banner.jpg
│   ├── 2/
│   │   └── logo.png
│   └── ...
└── produtos/
    ├── 1/
    │   └── foto.jpg
    └── ...
```

### F.4 Como Java Salva

**Mudança recomendada** em `MeuRestaurantePanel.IMAGES_DIR`:
```java
// De:
static final String IMAGES_DIR = "imagens" + File.separator + "restaurantes";
// Para:
static final String IMAGES_DIR = "media" + File.separator + "restaurantes";
```

O Java copia o arquivo selecionado pelo usuário para `media/restaurantes/`, grava o caminho relativo (ex: `media/restaurantes/logo.jpg`) no campo `imagem` ou `banner` do banco.

### F.5 Como Django Serve

Com `MEDIA_ROOT` e `MEDIA_URL` agora configurados:
- Django serve `media/` em desenvolvimento via `urls.py`
- Templates usam `{{ restaurante.imagem }}` diretamente como path ou URL
- Para produção, servir `media/` via nginx/CDN

### F.6 Migração de Dados Legados

Se existirem paths antigos como `imagens/restaurantes/foto.jpg`:
```sql
UPDATE tb_restaurantes
SET imagem = REPLACE(imagem, 'imagens\restaurantes\', 'media/restaurantes/')
WHERE imagem LIKE 'imagens%';

UPDATE tb_restaurantes
SET banner = REPLACE(banner, 'imagens\restaurantes\', 'media/restaurantes/')
WHERE banner LIKE 'imagens%';
```

---

## G) Backlog / Sprints de Correção

### Sprint Atual (Concluído nesta auditoria)
- [x] Criar `Migration_Sprint1.sql` com colunas `ativo`, `aberto`, `banner`, `imagem`
- [x] Adicionar campo `banner` ao model Django `TbRestaurantes`
- [x] Configurar `MEDIA_ROOT`/`MEDIA_URL` em `settings.py`
- [x] Filtrar restaurantes inativos no catálogo Django
- [x] Corrigir redirect após login
- [x] Corrigir redirect após cadastro

### Sprint 2 — Alinhamento Multi-Restaurante
- [ ] Adicionar filtro `aberto=True` na finalização de pedidos (bloquear pedido se fechado)
- [ ] Adicionar `ID_restaurante` FK em `tb_cupons` (cupons por restaurante)
- [ ] Unificar `IMAGES_DIR` para `media/restaurantes/` no Java
- [ ] Verificação de conflito de reserva no Django (Django permite reservas sobrepostas hoje)

### Sprint 3 — Funcionalidades Faltantes
- [ ] Implementar cupons/fidelidade no Java (pelo menos leitura/exibição)
- [ ] Exibir avaliações no Java (leitura de `tb_avaliacoes`)
- [ ] Criar tabela `tb_movimentacoes_estoque` para persistir histórico de movimentações
- [ ] Tornar lista de mesas dinâmica por restaurante (em vez de 1-20 hardcoded)
- [ ] Usar `tb_restaurantes.taxa_entrega` real na finalização de pedido (Django)
- [ ] Integrar `tb_cupons` na finalização de pedido (Django)

### Sprint 4 — Melhorias e Refinamento
- [ ] Remover `print()` de debug da `views.py`
- [ ] Adicionar campo `status` em `tb_reservas` (Pendente, Confirmada, Cancelada)
- [ ] Unificar estados de pedido entre Java e Django
- [ ] Adicionar `tb_nutricao` no Java para exibição no cardápio
- [ ] Implementar upload real de imagem no Django (formulário com `enctype=multipart`)

---

## H) Riscos e Próximos Passos

### Riscos Identificados

1. **Concorrência Java × Django no estoque**: Ambos atualizam `tb_estoque.quantidade` — se dois operadores (um no Java, um via Django) atualizarem ao mesmo tempo, pode haver race condition. **Mitigação**: usar transações e `SELECT ... FOR UPDATE` ou stored procedures.

2. **Senhas incompatíveis entre Java e Django**: Java usa bcrypt direto; Django usa `make_password` (PBKDF2 por padrão). Funcionários e clientes são tabelas separadas, então não há conflito direto, mas se alguma feature exigir login cruzado, as senhas serão incompatíveis.

3. **Caminho de imagens divergente**: Java salva em `imagens/restaurantes/`, Django espera `media/`. Se não unificado, imagens gravadas por um lado não serão encontradas pelo outro.

4. **Cupons sem escopo de restaurante**: Se o sistema crescer, cupons globais podem gerar problemas de controle financeiro por restaurante.

### Próximos Passos Recomendados

1. Aplicar `Migration_Sprint1.sql` em qualquer ambiente de banco existente
2. Gerar migração Django para o campo `banner` (`python manage.py makemigrations`)
3. Unificar caminho de imagens entre Java e Django
4. Implementar validação de restaurante `aberto` na finalização de pedidos
5. Testar fluxo completo end-to-end: Java cria restaurante → Django exibe → Cliente faz pedido → Java processa no KDS
