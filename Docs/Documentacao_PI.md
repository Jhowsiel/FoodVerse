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
| 1 | F | Solicitação de Cadastro de Funcionário | Permite registrar novos funcionários no sistema, com status inicial "pendente" até aprovação. | Campos: nome, username, e-mail, senha (mín. 6 caracteres), cargo e ID do restaurante. Status inicial: "pendente". | Alta |
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
| senha | VARCHAR(255) | NULL | Senha armazenada com hash SHA-256 (prefixo `{SHA256}`) |
| status | VARCHAR(20) | NULL | Status do cadastro (`pendente`, `ativo`, `bloqueado`) |
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

---

## 6. Critérios de Aceitação

---

### Funcionalidade: Solicitação de Cadastro de Funcionário

**1. Cadastro Bem-Sucedido:**
- **Dado:** Que o usuário preencheu todos os campos obrigatórios: nome, usuário, e-mail e senha; e selecionou um cargo (Atendente, Cozinheiro ou Entregador) nos chips de seleção.
- **Quando:** Clicar no botão "Cadastrar".
- **Então:** O sistema deve salvar os dados com status `pendente`, exibir a mensagem **"Solicitação validada e enviada!"** e redirecionar para a tela de login.

**2. Validação de Campos Obrigatórios Vazios:**
- **Dado:** Que um ou mais campos obrigatórios (nome, usuário, e-mail ou senha) estão vazios.
- **Quando:** O usuário tentar se cadastrar.
- **Então:** O sistema deve exibir a mensagem de erro **"Preencha todos os campos obrigatórios (*)"** e não realizar o cadastro.

**3. Validação de Senha Fraca:**
- **Dado:** Que o campo senha contém menos de 6 caracteres.
- **Quando:** O usuário tentar se cadastrar.
- **Então:** O sistema deve exibir a mensagem **"A senha deve ter no mínimo 6 caracteres."** e não realizar o cadastro.

**4. Validação de E-mail ou Usuário Duplicado:**
- **Dado:** Que o e-mail ou nome de usuário informado já está cadastrado.
- **Quando:** O usuário tentar se cadastrar.
- **Então:** O sistema deve impedir o cadastro e mostrar a mensagem **"Usuário ou E-mail já cadastrado."**

**5. Persistência de Dados no Banco:**
- **Dado:** Que todos os dados são válidos.
- **Quando:** O cadastro for realizado.
- **Então:** Os dados devem ser persistidos em `tb_funcionarios` com `status = 'pendente'` e aguardar aprovação de Admin ou Gerente.

---

### Funcionalidade: Aprovação de Cadastro de Funcionário

**1. Aprovar ou Rejeitar Solicitações:**
- **Dado:** Que um Admin ou Gerente esteja autenticado e na tela de gestão de usuários (lista de cadastros pendentes).
- **Quando:** Selecionar pelo menos uma solicitação de cadastro e clicar em "Aprovar" ou "Rejeitar".
- **Então:** O sistema deve atualizar o `status` das solicitações selecionadas para `ativo` (aprovado) ou `bloqueado` (rejeitado), registrar o nome do responsável e a data/hora da ação.

**2. Validação de Seleção de Solicitações:**
- **Dado:** Que o Admin ou Gerente esteja na tela de gestão de usuários.
- **Quando:** Clicar em "Aprovar" ou "Rejeitar" sem nenhuma solicitação selecionada.
- **Então:** O sistema deve exibir uma mensagem informando que é necessário selecionar ao menos uma solicitação e não realizar nenhuma ação de atualização.

**3. Exibição de Solicitações Pendentes:**
- **Dado:** Que um Admin ou Gerente acessa a tela de solicitações.
- **Quando:** A lista de cadastros for carregada.
- **Então:** O sistema deve exibir cadastros com status `pendente` em destaque. Administrador global visualiza apenas solicitações de Gerentes; Gerente visualiza apenas Atendentes, Cozinheiros e Entregadores do próprio restaurante.

**4. Erro na Atualização de Status:**
- **Dado:** Que uma ou mais solicitações estejam selecionadas.
- **Quando:** Houver erro na atualização do status no banco de dados.
- **Então:** O sistema deve manter os dados inalterados na tabela e exibir uma mensagem de erro ao responsável.

---

### Funcionalidade: Login de Funcionário

**1. Autenticação Bem-Sucedida:**
- **Dado:** Que o funcionário tenha cadastro com status `ativo` no banco de dados.
- **Quando:** Informar e-mail (ou nome de usuário) e senha válidos e clicar em "Entrar".
- **Então:** O sistema deve autenticar o usuário e direcioná-lo à dashboard do sistema do restaurante com as opções correspondentes ao seu cargo.

**2. Cadastro Pendente:**
- **Dado:** Que o funcionário tenha cadastro com status `pendente`.
- **Quando:** Informar e-mail e senha válidos e clicar em "Entrar".
- **Então:** O sistema deve exibir a mensagem de erro **"Seu cadastro ainda está em análise."** e não realizar o login.

**3. Senha/E-mail Inválidos:**
- **Dado:** Que o funcionário não tenha cadastro no banco ou tenha inserido senha incorreta.
- **Quando:** Clicar em "Entrar".
- **Então:** O sistema deve exibir a mensagem de erro **"Credenciais inválidas."**

**4. Campos Vazios:**
- **Dado:** Que os campos de e-mail e/ou senha estejam vazios.
- **Quando:** Clicar em "Entrar".
- **Então:** O sistema deve exibir a mensagem de erro **"Preencha e-mail e senha!"**

---

### Funcionalidade: Acesso de Administrador

**1. Permitir acesso administrativo:**
- **Dado:** Que um funcionário está cadastrado com `cargo = 'Admin'` e `status = 'ativo'`.
- **Quando:** O funcionário informar e-mail e senha válidos e acionar o login.
- **Então:** O sistema deve autenticar o usuário, verificar o cargo `Admin` e conceder acesso às funcionalidades administrativas globais (gestão de restaurantes, gestão de gerentes).

**2. Negar acesso a módulos restritos:**
- **Dado:** Que um funcionário autenticado não possui o cargo necessário para acessar um módulo.
- **Quando:** O funcionário tentar navegar para um módulo restrito ao seu cargo.
- **Então:** O sistema deve negar o acesso e exibir a mensagem **"Seu perfil não possui permissão para acessar este módulo."** Se o usuário for Admin sem restaurante selecionado, exibe **"Selecione um restaurante antes de acessar este módulo."**

**3. Exibir opções de cada perfil de acordo com o cargo:**
- **Dado:** Que um funcionário autenticado acessa o menu principal.
- **Quando:** O menu for carregado.
- **Então:** O sistema deve exibir apenas as opções de menu compatíveis com o cargo: Admin (global) → Restaurantes e Gerentes; Admin (em contexto de restaurante) e Gerente → todos os painéis operacionais; Atendente → Pedidos, Entregas, Mesas; Cozinheiro → Cardápio, Estoque, KDS; Entregador → Entregas.

**4. Confirmação para ações sensíveis (planejado):**
- **Dado:** Que um Admin acessa uma funcionalidade sensível (como exclusão definitiva de dados).
- **Quando:** O usuário acionar essa funcionalidade sensível.
- **Então:** O sistema deverá solicitar confirmação adicional antes de executar a ação. *(Funcionalidade prevista no modelo — `Admin.AcessCode` — pendente de implementação na interface.)*

---

### Funcionalidade: Gestão de Perfil de Funcionário

**1. Visualizar e editar dados de colaboradores:**
- **Dado:** Que um Admin ou Gerente está autenticado e acessa o painel de Gestão de Usuários.
- **Quando:** Selecionar um funcionário e acionar a edição.
- **Então:** O sistema deve exibir e permitir a atualização dos dados do funcionário (nome, e-mail, telefone, cargo, status e senha), registrando a ação.

**2. Validação na edição:**
- **Dado:** Que um responsável está editando os dados de um funcionário.
- **Quando:** Informar uma nova senha com menos de 6 caracteres.
- **Então:** O sistema deve exibir **"A nova senha deve ter no mínimo 6 caracteres."** e não salvar as alterações.

**3. Confirmação de alteração bem-sucedida:**
- **Dado:** Que os dados editados são válidos.
- **Quando:** O responsável confirmar as alterações.
- **Então:** O sistema deve exibir a mensagem **"Cadastro atualizado com sucesso!"** e refletir os dados atualizados na listagem.

---

### Funcionalidade: Gestão de Pedidos

**1. Encaminhamento de Pedidos:**
- **Dado:** Que um cliente realiza um pedido via plataforma web (delivery) ou o atendente registra um pedido no sistema (local).
- **Quando:** O pedido é salvo no sistema com o status inicial `pendente`.
- **Então:** O sistema deve encaminhar automaticamente o pedido para a interface de pedidos com status `pendente`, visível no painel de pedidos e no KDS da cozinha.

**2. Atualização de Status para "em preparo":**
- **Dado:** Que o pedido está com o status `pendente` no painel de pedidos.
- **Quando:** O atendente ou cozinheiro acionar "Aceitar" o pedido.
- **Então:** O sistema deve atualizar o status do pedido para `em preparo` e descontar automaticamente os insumos do estoque conforme a receita (BOM) do prato.

**3. Atualização de Status para "pronto":**
- **Dado:** Que o pedido está com o status `em preparo`.
- **Quando:** A equipe da cozinha finalizar o preparo.
- **Então:** O sistema deve permitir atualizar o status do pedido para `pronto`.

**4. Despacho de Pedido Delivery:**
- **Dado:** Que o pedido está com o status `pronto` e é do tipo Delivery.
- **Quando:** O atendente clicar em "Despachar (Entregar ao Motoboy)" e selecionar um entregador disponível.
- **Então:** O sistema deve atualizar o status do pedido para `em rota` e registrar o nome do entregador no pedido.

**5. Conclusão de Pedido:**
- **Dado:** Que o pedido de delivery está com status `em rota` ou o pedido local está `pronto`.
- **Quando:** A entrega for confirmada ou o item for entregue na mesa.
- **Então:** O sistema deve atualizar o status do pedido para `concluido`.

---

### Funcionalidade: Gestão de Estoque

**1. Cadastro de Ingredientes (Insumos):**
- **Dado:** Que o Admin, Gerente ou Cozinheiro acessa o painel de estoque.
- **Quando:** Cadastrar um novo insumo com nome, unidade de medida, quantidade e estoque mínimo.
- **Então:** O sistema deve salvar o item com `tipo_produto = 'INSUMO'` e exibi-lo corretamente na lista de estoque.

**2. Entrada de Insumos no Estoque:**
- **Dado:** Que o restaurante adquire novos insumos.
- **Quando:** O responsável registrar a entrada informando o insumo e a quantidade.
- **Então:** O estoque deve ser atualizado com a nova quantidade do insumo.

**3. Desconto Automático em Pedido:**
- **Dado:** Que um pedido foi registrado com itens vinculados a receitas (BOM).
- **Quando:** O status do pedido for atualizado de `pendente` para `em preparo`.
- **Então:** O sistema deve descontar automaticamente os insumos do estoque conforme a receita de cada produto pedido (operação idempotente).

**4. Alerta de Baixo Estoque:**
- **Dado:** Que um insumo tem um estoque mínimo configurado.
- **Quando:** A quantidade disponível atingir ou ficar abaixo do limite mínimo.
- **Então:** O sistema deve exibir destaque visual (indicador de alerta) na interface para alertar o responsável.

---

### Funcionalidade: Gestão de Entregadores

**1. Controle de disponibilidade:**
- **Dado:** Que o entregador está cadastrado no sistema com cargo `Entregador`.
- **Quando:** A gestão precisar despachar um pedido de delivery.
- **Então:** O sistema deve listar os entregadores disponíveis para seleção na tela de despacho do pedido.

**2. Consulta de histórico de entregas:**
- **Dado:** Que existem entregas registradas no sistema para o restaurante.
- **Quando:** O Atendente, Gerente ou Admin acessa o painel de Entregas.
- **Então:** O sistema deve exibir a lista de pedidos com status `em rota` e `concluido`, incluindo dados do entregador, endereço e horários.

---

### Funcionalidade: Cadastro e Gestão de Cardápio

**1. Cadastro de item no cardápio:**
- **Dado:** Que o Gerente ou Admin (em contexto de restaurante) acessa o painel de cardápio.
- **Quando:** Um novo item é cadastrado com nome, categoria, preço, disponibilidade, imagem, descrição e opções de personalização.
- **Então:** O sistema deve registrar o novo item com `tipo_produto = 'VENDA'` e exibir mensagem de sucesso.

**2. Edição de item do cardápio:**
- **Dado:** Que um item já está cadastrado no cardápio.
- **Quando:** O usuário altera qualquer informação do item.
- **Então:** O sistema deve atualizar as informações do item no banco, garantindo que o escopo de atualização seja restrito ao restaurante do usuário logado (filtro `AND ID_restaurante = ?`).

**3. Desativação de item do cardápio:**
- **Dado:** Que um item está cadastrado no cardápio.
- **Quando:** O usuário opta por desativar o item.
- **Então:** O sistema deve marcar o item como indisponível (sem excluir os dados), removendo-o das listagens públicas no portal web para clientes.

**4. Visualização de itens do cardápio:**
- **Dado:** Que existem itens cadastrados com `tipo_produto = 'VENDA'`.
- **Quando:** Um usuário acessa o cardápio pelo portal web.
- **Então:** O sistema deve exibir apenas os itens de venda disponíveis, com imagem, descrição, preço e opções de personalização, filtrando por `COALESCE(tipo_produto, 'VENDA') = 'VENDA'`.

---

### Funcionalidade: Gestão de Promoções e Descontos

**1. Cadastro de cupom:**
- **Dado:** Que o usuário possui permissão para gerenciar cupons (Admin em contexto de restaurante).
- **Quando:** Um novo cupom é criado com código, valor de desconto e validade.
- **Então:** O sistema deve registrar o cupom em `tb_cupons` e disponibilizá-lo para uso no portal web dos clientes.

**2. Aplicação de cupom no pedido:**
- **Dado:** Que o cliente informa um código de cupom válido no checkout.
- **Quando:** O cliente confirmar o pedido.
- **Então:** O sistema deve aplicar o desconto ao valor total, registrar o cupom utilizado e persistir o pedido com o valor final correto.

**3. Desativação/expiração de cupom:**
- **Dado:** Que um cupom está cadastrado com data de validade definida.
- **Quando:** A data atual ultrapassar a validade do cupom.
- **Então:** O sistema deve impedir que o cupom seja aplicado a novos pedidos.

---

### Funcionalidade: Processamento de Pagamentos

**1. Cliente registrando pagamento com sucesso (web):**
- **Dado:** Que o cliente está na tela de finalização do pedido no portal web.
- **Quando:** O cliente seleciona a forma de pagamento (**Pix**, **Crédito** ou **Débito**) e confirma o pedido.
- **Então:** O registro de pagamento é salvo em `tb_pagamentos` com o método escolhido e o pedido é criado com status `pendente`.

**2. Funcionário registrando pagamento presencial (desktop):**
- **Dado:** Que o atendente está gerenciando um pedido local no sistema desktop.
- **Quando:** O pedido for concluído e o atendente registrar o recebimento.
- **Então:** O registro de pagamento deve ser salvo no histórico financeiro identificando a forma de pagamento (Dinheiro, PIX, etc.) e o pedido deve ser atualizado para `concluido`.

**3. Erro no processamento:**
- **Dado:** Que o sistema não consegue salvar o pagamento (erro de banco, falha de rede).
- **Quando:** Ocorrer um erro durante a confirmação do pedido.
- **Então:** O sistema deve exibir mensagem de erro clara, manter o pedido sem status final e permitir nova tentativa. O pedido permanece com o status anterior até o pagamento ser confirmado.

---

*Documentação elaborada com base no código da Sprint 1 do Projeto Integrador — Senac.*
