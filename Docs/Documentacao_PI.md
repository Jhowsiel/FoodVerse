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

*Documentação elaborada com base no código da Sprint 1 do Projeto Integrador — Senac.*
