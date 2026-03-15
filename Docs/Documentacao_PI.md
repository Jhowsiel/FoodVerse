# FoodVerse — Documentação do Projeto Integrador

---

## 1. Definição do Projeto de Sistema

### Análise da estrutura do projeto (escopo)

---

### O que é o projeto? Para que ele foi criado.

O FoodVerse é um sistema para gerenciamento de restaurantes parceiros. Ele é composto por duas partes que funcionam juntas: um aplicativo desktop em Java, voltado para os funcionários do restaurante, e um portal web em Python/Django, voltado para os clientes.

Ele foi criado para atender pequenos e médios restaurantes que ainda não têm uma solução digital no dia a dia. A ideia é centralizar as operações em um único sistema: controle de pedidos, cardápio, estoque, entregas, reservas de mesa e atendimento ao cliente — tudo sem depender de planilhas ou processos manuais.

---

### Para quem ele é criado.

**Restaurantes parceiros:** utilizam o aplicativo desktop para gerenciar as operações internas. Os funcionários se dividem em cargos com permissões diferentes (Admin, Gerente, Atendente, Cozinheiro e Entregador).

**Clientes dos restaurantes:** acessam o portal web para navegar pelos restaurantes disponíveis, montar o carrinho, fazer pedidos, reservar mesas e acompanhar o histórico de compras.

---

### Qual a necessidade que ele supre.

Restaurantes que trabalham sem sistema digital têm dificuldade para controlar pedidos, manter o cardápio atualizado, saber o que tem no estoque e organizar a equipe. O FoodVerse resolve isso com um sistema que integra todas essas áreas.

Para o cliente, o sistema oferece uma forma prática de pedir comida pela internet, com informações reais do cardápio, aplicação de cupons e opção de reservar mesa com antecedência.

---

## 2. Análise de Requisitos do Sistema

### Requisitos Funcionais × Requisitos Não Funcionais

| Nº | Tipo | Nome | Descrição | Observação | Prioridade |
|----|------|------|-----------|------------|------------|
| 1 | F | Solicitação de Cadastro de Funcionário | Permite registrar novos funcionários no sistema, com status inicial "pendente" até aprovação. | Campos: nome, username, e-mail, senha, cargo e ID do restaurante. Status inicial: "pendente". | Alta |
| 1.2 | F | Aprovação de Cadastro de Funcionário | Permite que Admin ou Gerente aprove ou rejeite solicitações de cadastro de funcionários. | Lista de pendentes em destaque, alteração de status para "ativo" ou "bloqueado", registro de quem aprovou e quando. | Alta |
| 2 | F | Login de Funcionário | Permite que funcionários aprovados acessem o sistema com e-mail e senha. | Verifica se o status é "ativo" antes de autenticar. Se pendente, bloqueado ou desligado, exibe mensagem explicativa. | Alta |
| 2.1 | F | Controle de Acesso por Cargo | Libera ou restringe funcionalidades de acordo com o cargo do funcionário logado. | Cargos: Admin (acesso global ou por restaurante), Gerente, Atendente, Cozinheiro e Entregador. Cada cargo tem acesso a painéis específicos. | Alta |
| 2.2 | F | Gestão de Restaurantes (Admin) | Permite ao Admin criar, editar, ativar e desativar restaurantes na plataforma. | Painel exclusivo para Admin sem vínculo de restaurante. Campos: nome, categoria, descrição, taxa e tempo de entrega, imagem, banner e cupom. | Alta |
| 3 | F | Gestão de Perfil de Funcionário | Permite visualizar e atualizar dados cadastrais do próprio perfil. | Alteração de nome, e-mail, telefone, cargo e senha. Disponível para todos os cargos logados. | Média |
| 4 | F | Gestão de Pedidos | Controle completo dos pedidos recebidos, com filtro por status e atualização manual do andamento. | Estados: pendente, em preparo, pronto, em rota, concluído e cancelado. Ao mover para "em preparo", o estoque é baixado automaticamente pela receita do prato. | Alta |
| 4.1 | F | Painel de Cozinha (KDS) | Exibe em tempo real os pedidos em fila para a cozinha, com destaque para pedidos por tempo de espera. | Filtra apenas pedidos com status "em preparo" e "pronto". Atualização automática a cada intervalo configurado. | Alta |
| 5 | F | Gestão de Estoque | Controle de insumos e matérias-primas usados nos pratos. | Cadastro de itens com quantidade atual e estoque mínimo. Baixa automática via receita quando pedido entra em preparo. Alerta visual quando item atinge estoque mínimo. | Alta |
| 5.1 | F | Cadastro de Receitas (BOM) | Vincula produtos de venda aos seus insumos com as quantidades necessárias para preparo. | Cada prato de venda pode ter uma lista de ingredientes (receita). Usada para baixa automática do estoque no pedido. | Alta |
| 6 | F | Gestão de Entregas | Controle dos pedidos em entrega, com visualização dos entregadores em rota. | Exibe pedidos com status "em rota", mostra entregador responsável, endereço de entrega e hora de saída. Permite marcar entrega como concluída. | Média |
| 7 | F | Cadastro e Gestão de Cardápio | Permite cadastrar, editar e desativar itens do cardápio, com categorias, preços e disponibilidade. | Inclui imagem, descrição, preço, categoria e tempo de preparo. Produtos podem ser do tipo "VENDA" (itens do cardápio) ou "INSUMO" (ingredientes de estoque). | Alta |
| 8 | F | Cupons de Desconto | Aplicação de códigos de desconto no fechamento do pedido pelo portal web. | Cada restaurante pode ter um cupom cadastrado. O portal web também aplica desconto automático de 10% para pedidos acima de R$ 50,00. | Média |
| 9 | F | Registro de Pagamentos | Registra o método de pagamento e o valor pago ao finalizar um pedido. | Métodos disponíveis: PIX, Cartão de Crédito e Dinheiro. O registro é salvo em `tb_pagamentos` vinculado ao pedido. | Alta |
| 10 | F | Reservas de Mesa | Gerenciamento de reservas feitas pelos clientes via portal web. | Disponível para os próximos 7 dias, com 5 horários disponíveis. O sistema atribui automaticamente uma mesa livre (M1 a M12). Taxa fixa de R$ 20,00. Gerente visualiza e gerencia as reservas no desktop. | Média |
| 11 | F | Cadastro e Login de Clientes (Web) | Permite que clientes criem conta e façam login no portal web para realizar pedidos. | Campos: nome, username, e-mail, telefone, CPF, endereço e senha. Validação de CPF com algoritmo de dígito verificador. Sessão com opção "lembrar por 14 dias". | Alta |
| 11.1 | F | Gestão de Perfil do Cliente (Web) | Permite ao cliente visualizar e atualizar seus dados no portal web. | Campos editáveis: username, e-mail, telefone e endereço. Verifica unicidade de e-mail e telefone antes de salvar. | Média |
| 12 | F | Avaliações de Restaurantes | Clientes podem avaliar restaurantes com nota (1 a 5) e comentário após realizar pedidos. | Registro salvo em `tb_avaliacoes`. Exibido na página do restaurante no portal web. | Média |
| 13 | F | Programa de Fidelidade | Acúmulo de pontos e cashback para clientes frequentes. | Estrutura de dados implementada (`tb_fidelidade` com pontos e cashback). Integração com pedidos em desenvolvimento. | Baixa |
| 14 | NF | Performance | O sistema deve carregar e salvar dados com agilidade, mesmo com múltiplos registros. | Consultas SQL otimizadas com filtros por restaurante. Escopo de dados por sessão para evitar carregamento desnecessário. | Alta |
| 15 | NF | Usabilidade | Interface intuitiva, com elementos claros e organizados para uso no dia a dia do restaurante. | Desktop: design padronizado via `UIConstants`, notificações via `Toast` (sem popups), menus organizados por cargo. Web: navegação simples, mensagens de feedback claras. | Alta |
| 16 | NF | Compatibilidade | O sistema deve funcionar em Windows e Linux. | Desktop: Java Swing, empacotado via Maven. Web: Django, testado em ambiente Linux. Banco de dados: SQL Server. | Média |
| 17 | NF | Segurança | Dados de usuários e senhas armazenados de forma segura, com controle de acesso por sessão. | Senhas armazenadas com hash. Controle de permissões por cargo. Consultas com `PreparedStatement` para evitar SQL Injection. Escopo de dados por restaurante para evitar acesso cruzado. | Alta |
| 18 | NF | Backup | O banco de dados deve ter suporte a backup para garantir a integridade dos dados. | Script de migração idempotente (`Migration_Sprint1.sql`) para recriar a estrutura e dados iniciais. Backup manual via SQL Server Management Studio. | Alta |
| 19 | NF | Portabilidade | O sistema deve ser leve e não exigir configurações complexas para rodar. | Desktop: executável Java sem instalador pesado. Web: dependências listadas em `requirements.txt`, fácil de subir com `manage.py`. | Média |

---

## 3. Banco de Dados

### Tecnologias utilizadas

| Componente | Tecnologia | Versão |
|---|---|---|
| SGBD | Microsoft SQL Server | 2022 |
| Dialeto SQL | T-SQL (Transact-SQL) | — |
| Driver Java | Microsoft JDBC Driver for SQL Server (`mssql-jdbc`) | 12.x |
| Driver Python/Django | `mssql-django` + ODBC Driver 17 for SQL Server | — |
| Nome do banco | `FoodVerseDB` | — |

---

### Dados de conexão

| Parâmetro | Valor |
|---|---|
| Servidor | `127.0.0.1` |
| Porta | `1433` |
| Banco | `FoodVerseDB` |
| Usuário | `sa` |
| Autenticação Java | `jdbc:sqlserver://127.0.0.1:1433;databaseName=FoodVerseDB;encrypt=false;trustServerCertificate=true` |
| Autenticação Django | `ENGINE: mssql`, `DRIVER: ODBC Driver 17 for SQL Server` |

---

### Rotinas de Backup e Restore

#### 1. Backup do Banco de Dados

**Passo a passo:**

1. Faça login no servidor onde o SQL Server está instalado.
2. Abra o SQL Server Management Studio (SSMS) e conecte-se ao banco `FoodVerseDB`.
3. Execute o comando abaixo para realizar o backup completo:

```sql
BACKUP DATABASE FoodVerseDB
TO DISK = 'C:\backups\FoodVerseDB.bak'
WITH FORMAT, INIT, NAME = 'Backup Completo FoodVerseDB';
```

4. Verifique que o arquivo `FoodVerseDB.bak` foi gerado no diretório especificado.
5. Para automatizar, utilize o **Agendador de Tarefas do Windows** ou um SQL Server Agent Job.

#### 2. Restore do Banco de Dados

**Passo a passo:**

1. Faça login no servidor onde deseja restaurar o banco.
2. Abra o SSMS e conecte-se ao SQL Server (sem selecionar um banco específico).
3. Execute o comando:

```sql
RESTORE DATABASE FoodVerseDB
FROM DISK = 'C:\backups\FoodVerseDB.bak'
WITH REPLACE;
```

4. Confirme que os dados foram restaurados e que o banco está operacional.

> **Alternativa incremental:** utilize o script `Banco de Dados/Migration_Sprint1.sql` para recriar apenas a estrutura e os dados de referência em um banco vazio, sem necessidade de arquivo `.bak`.

---

### Dicionário de Dados

#### tb_clientes — Clientes do portal web

| Coluna | Tipo | Restrição | Descrição |
|---|---|---|---|
| id_cliente | INT IDENTITY | PK | Identificador único do cliente |
| username | VARCHAR(50) | NULL | Nome de usuário (login) |
| nome | VARCHAR(100) | NULL | Nome completo |
| email | VARCHAR(100) | NULL | Endereço de e-mail |
| telefone | VARCHAR(20) | NULL | Número de telefone |
| cpf | VARCHAR(14) | NULL | CPF (formato `000.000.000-00`) |
| senha | VARCHAR(255) | NULL | Senha armazenada com hash |
| endereco | VARCHAR(255) | NULL | Endereço de entrega padrão |
| data_cadastro | DATETIME | NULL | Data e hora do cadastro |

#### tb_restaurantes — Restaurantes cadastrados na plataforma

| Coluna | Tipo | Restrição | Descrição |
|---|---|---|---|
| ID_restaurante | INT IDENTITY | PK | Identificador único do restaurante |
| nome | VARCHAR(100) | NULL | Nome do restaurante |
| categoria | VARCHAR(50) | NULL | Categoria (ex.: Pizzaria, Fast Food) |
| descricao | VARCHAR(255) | NULL | Descrição resumida |
| avaliacao | DECIMAL(3,2) | NULL | Média de avaliações (0,00 a 5,00) |
| tempo_entrega | VARCHAR(20) | NULL | Tempo estimado de entrega |
| taxa_entrega | DECIMAL(10,2) | NULL | Taxa de entrega em reais |
| cupom | VARCHAR(50) | NULL | Código de cupom promocional |
| imagem | VARCHAR(255) | NULL | Caminho da imagem do logo |
| banner | VARCHAR(255) | NULL | Caminho do banner do restaurante |
| ativo | BIT NOT NULL | DEFAULT 1 | Controla se o restaurante existe na plataforma (Admin) |
| aberto | BIT NOT NULL | DEFAULT 1 | Controla se aceita pedidos no momento (Gerente) |

#### tb_funcionarios — Funcionários dos restaurantes

| Coluna | Tipo | Restrição | Descrição |
|---|---|---|---|
| ID_funcionario | INT IDENTITY | PK | Identificador único |
| ID_restaurante | INT | NULL, FK → tb_restaurantes | Restaurante ao qual pertence |
| nome | VARCHAR(100) | NULL | Nome completo |
| username | VARCHAR(50) | NULL | Nome de usuário (login) |
| email | VARCHAR(100) | NULL | E-mail |
| cargo | VARCHAR(50) | NULL | Cargo (Admin, Gerente, Atendente, Cozinheiro, Entregador) |
| telefone | VARCHAR(20) | NULL | Telefone |
| senha | VARCHAR(255) | NULL | Senha armazenada com hash |
| status | VARCHAR(20) | NULL | Status do cadastro (pendente, aprovado, bloqueado) |
| data_cadastro | DATETIME | NULL | Data do cadastro |

#### tb_status_pedido — Tabela mestre de status de pedido

| Coluna | Tipo | Restrição | Descrição |
|---|---|---|---|
| ID_status | INT | PK (manual) | Identificador do status |
| nome_status | VARCHAR(50) | NULL | Nome do status |

**Dados de referência:**

| ID_status | nome_status |
|---|---|
| 1 | pendente |
| 2 | em preparo |
| 3 | pronto |
| 4 | em rota |
| 5 | concluido |
| 6 | cancelado |

#### tb_produtos — Produtos (itens de venda e insumos)

| Coluna | Tipo | Restrição | Descrição |
|---|---|---|---|
| ID_produto | INT IDENTITY | PK | Identificador único |
| ID_restaurante | INT | NULL, FK → tb_restaurantes | Restaurante dono do produto |
| nome_produto | VARCHAR(100) | NULL | Nome do produto |
| descricao | VARCHAR(255) | NULL | Descrição |
| preco | DECIMAL(10,2) | NULL | Preço de venda |
| categoria | VARCHAR(50) | NULL | Categoria do produto |
| imagem | VARCHAR(255) | NULL | Caminho da imagem |
| tempo_preparo | INT | NULL | Tempo de preparo em minutos |
| disponivel | BIT | NULL | Se o produto está disponível para venda |
| destaque | BIT | NULL | Se é destaque no cardápio |
| restricoes | VARCHAR(255) | NULL | Restrições alimentares |
| data_criacao | DATETIME | NULL | Data de criação |
| tipo_produto | VARCHAR(20) NOT NULL | DEFAULT 'VENDA' | `'VENDA'` = item do cardápio; `'INSUMO'` = ingrediente de estoque |

#### tb_estoque — Estoque de insumos

| Coluna | Tipo | Restrição | Descrição |
|---|---|---|---|
| ID_estoque | INT IDENTITY | PK | Identificador único |
| ID_produto | INT | NOT NULL, FK → tb_produtos | Produto (tipo INSUMO) |
| quantidade | INT | NULL | Quantidade disponível em estoque |
| estoque_minimo | INT | NULL | Quantidade mínima (gatilho de alerta) |
| ultima_atualizacao | DATETIME | NULL | Última movimentação |

#### tb_nutricao — Informações nutricionais dos produtos

| Coluna | Tipo | Restrição | Descrição |
|---|---|---|---|
| ID_nutricao | INT IDENTITY | PK | Identificador único |
| ID_produto | INT | NOT NULL, FK → tb_produtos | Produto relacionado |
| kcal | INT | NULL | Calorias (kcal) |
| proteina | VARCHAR(20) | NULL | Proteínas (ex.: `25g`) |
| carbo | VARCHAR(20) | NULL | Carboidratos (ex.: `60g`) |
| gordura | VARCHAR(20) | NULL | Gorduras (ex.: `20g`) |

#### tb_pedidos — Pedidos realizados

| Coluna | Tipo | Restrição | Descrição |
|---|---|---|---|
| ID_pedido | INT IDENTITY | PK | Identificador único |
| ID_cliente | INT | NULL, FK → tb_clientes | Cliente que realizou o pedido |
| ID_restaurante | INT | NULL, FK → tb_restaurantes | Restaurante do pedido |
| status_id | INT | NULL, FK → tb_status_pedido | Status atual do pedido |
| data_pedido | DATETIME | NULL | Data e hora do pedido |
| endereco_entrega | VARCHAR(255) | NULL | Endereço de entrega (NULL = pedido no salão) |
| valor_total | DECIMAL(10,2) | NULL | Valor total do pedido |
| mesa | VARCHAR(50) | NULL | Mesa (para pedidos no salão/retirada) |

#### tb_pedidos_produtos — Itens dos pedidos

| Coluna | Tipo | Restrição | Descrição |
|---|---|---|---|
| ID_pedido | INT | PK + FK → tb_pedidos | Pedido ao qual o item pertence |
| ID_produto | INT | PK + FK → tb_produtos | Produto pedido |
| quantidade | INT | NULL | Quantidade pedida |
| preco_unitario | DECIMAL(10,2) | NULL | Preço unitário no momento da compra |

#### tb_reservas — Reservas de mesa

| Coluna | Tipo | Restrição | Descrição |
|---|---|---|---|
| ID_reserva | INT IDENTITY | PK | Identificador único |
| ID_cliente | INT | NULL, FK → tb_clientes | Cliente que fez a reserva |
| ID_restaurante | INT | NULL, FK → tb_restaurantes | Restaurante da reserva |
| data_reserva | DATETIME | NULL | Data e horário da reserva |
| numero_pessoas | INT | NULL | Número de pessoas |
| mesa | VARCHAR(10) | NULL | Mesa reservada |

#### tb_pagamentos — Pagamentos dos pedidos

| Coluna | Tipo | Restrição | Descrição |
|---|---|---|---|
| ID_pagamento | INT IDENTITY | PK | Identificador único |
| ID_pedido | INT | NULL, FK → tb_pedidos | Pedido relacionado |
| metodo_pagamento | VARCHAR(50) | NULL | Método (PIX, Cartão, Dinheiro) |
| valor | DECIMAL(10,2) | NULL | Valor pago |
| data_pagamento | DATETIME | NULL | Data e hora do pagamento |

#### tb_avaliacoes — Avaliações de restaurantes

| Coluna | Tipo | Restrição | Descrição |
|---|---|---|---|
| ID_avaliacao | INT IDENTITY | PK | Identificador único |
| ID_cliente | INT | NULL, FK → tb_clientes | Cliente que avaliou |
| ID_restaurante | INT | NULL, FK → tb_restaurantes | Restaurante avaliado |
| comentario | VARCHAR(255) | NULL | Comentário livre |
| nota | INT | NULL | Nota de 1 a 5 |
| data_avaliacao | DATETIME | NULL | Data da avaliação |

#### tb_cupons — Cupons de desconto

| Coluna | Tipo | Restrição | Descrição |
|---|---|---|---|
| ID_cupom | INT IDENTITY | PK | Identificador único |
| codigo | VARCHAR(50) | NULL | Código do cupom |
| desconto | DECIMAL(5,2) | NULL | Percentual de desconto |
| validade | DATE | NULL | Data de validade |

#### tb_fidelidade — Programa de fidelidade

| Coluna | Tipo | Restrição | Descrição |
|---|---|---|---|
| ID_fidelidade | INT IDENTITY | PK | Identificador único |
| ID_cliente | INT | NULL, FK → tb_clientes | Cliente participante |
| pontos | INT | NULL | Pontos acumulados |
| cashback | DECIMAL(10,2) | NULL | Cashback disponível em reais |

#### tb_receitas — Receitas / BOM (Bill of Materials)

Vincula cada produto de venda (`tipo_produto = 'VENDA'`) aos seus insumos (`tipo_produto = 'INSUMO'`) com as quantidades necessárias para preparo. Utilizada para baixa automática do estoque quando um pedido avança para "em preparo".

| Coluna | Tipo | Restrição | Descrição |
|---|---|---|---|
| ID_receita | INT IDENTITY | PK | Identificador único |
| ID_produto_venda | INT | NOT NULL, FK → tb_produtos | Produto final (prato de venda) |
| ID_insumo | INT | NOT NULL, FK → tb_produtos | Ingrediente (insumo) |
| quantidade | DECIMAL(10,4) | NOT NULL | Quantidade do insumo necessária por unidade vendida |
| unidade | VARCHAR(20) | NULL | Unidade de medida (kg, g, L, ml, un) |
| ativo | BIT NOT NULL | DEFAULT 1 | Se a linha da receita está ativa |

#### tb_personalizacao_grupos — Grupos de personalização de produtos

| Coluna | Tipo | Restrição | Descrição |
|---|---|---|---|
| ID_grupo | INT IDENTITY | PK | Identificador único |
| ID_produto | INT | NOT NULL, FK → tb_produtos | Produto que possui as opções |
| nome | VARCHAR(100) | NULL | Nome do grupo (ex.: "Ponto da Carne") |
| obrigatorio | BIT | NULL | Se a escolha é obrigatória |
| max_opcoes | INT | NULL | Máximo de opções selecionáveis |

#### tb_personalizacao_opcoes — Opções de personalização

| Coluna | Tipo | Restrição | Descrição |
|---|---|---|---|
| ID_opcao | INT IDENTITY | PK | Identificador único |
| ID_grupo | INT | NOT NULL, FK → tb_personalizacao_grupos | Grupo ao qual pertence |
| nome | VARCHAR(100) | NULL | Nome da opção (ex.: "Ao ponto") |
| preco_adicional | DECIMAL(10,2) | NULL | Acréscimo no preço (0,00 se sem custo) |

#### tb_mesas — Configuração de mesas do restaurante

| Coluna | Tipo | Restrição | Descrição |
|---|---|---|---|
| ID_mesa | INT IDENTITY | PK | Identificador único |
| ID_restaurante | INT | NOT NULL | Restaurante dono da mesa |
| nome | NVARCHAR(50) | NOT NULL | Nome/número da mesa (ex.: "Mesa 01") |
| capacidade | INT NOT NULL | DEFAULT 4 | Capacidade de pessoas |
| ativa | BIT NOT NULL | DEFAULT 1 | Se a mesa está ativa |

---

### Script de Criação e Migração

O arquivo `Banco de Dados/Migration_Sprint1.sql` contém o script T-SQL completo e **idempotente** para criar todas as tabelas acima do zero, ou aplicar apenas as colunas e tabelas que ainda não existirem em um banco existente. O script está organizado em 4 waves:

| Wave | Conteúdo |
|---|---|
| 0 | Criação de todas as tabelas base (`IF OBJECT_ID ... IS NULL`) |
| 1 | Adição de colunas faltantes em tabelas existentes (imagem, banner, ativo, aberto, restricoes, ID_restaurante, mesa) |
| 2 | `tipo_produto`, tabela `tb_receitas` e tabelas de personalização |
| 3 | Dados de referência: registros da tabela `tb_status_pedido` |
| 4 | Chaves estrangeiras entre todas as tabelas |

**Como executar:**

```sql
-- 1. Conecte ao SQL Server e crie o banco (se não existir):
CREATE DATABASE FoodVerseDB;
GO

-- 2. Execute o script completo no contexto do banco:
USE FoodVerseDB;
GO
-- (colar ou executar o conteúdo de Migration_Sprint1.sql)
```

> O script pode ser re-executado quantas vezes forem necessárias sem causar erros — todas as operações são protegidas por verificações `IF NOT EXISTS` ou `IF OBJECT_ID ... IS NULL`.

---

*Documentação elaborada com base no código da Sprint 1 do Projeto Integrador — Senac.*
