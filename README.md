# 🍽️ FoodVerse

Sistema de gerenciamento de restaurantes desenvolvido como Projeto Integrador (PI) do curso Tecnico em Informática — Senac Taboão da Serra.

## 📌 Sobre o Projeto

O FoodVerse é composto por duas partes:

- **Aplicativo Desktop (Java/Swing):** voltado para os funcionários do restaurante. Cobre gestão de pedidos, cardápio, estoque, entregas, reservas de mesa, cozinha (KDS) e controle de equipe.
- **Portal Web (Python/Django):** voltado para os clientes. Permite navegar pelos restaurantes, fazer pedidos, reservar mesas, aplicar cupons e acompanhar o histórico de compras.

Os dois sistemas compartilham o mesmo banco de dados SQL Server.

## 🛠️ Tecnologias

- **Desktop:** Java 25, Swing, Maven, JDBC
- **Web:** Python 3, Django
- **Banco de Dados:** SQL Server (T-SQL)
- **Autenticação:** Sessão por cargo (desktop) / sessão HTTP com hash de senha (web)

## 🗂️ Estrutura do Repositório

```
FoodVerse/          # Aplicativo desktop Java (Maven)
Web/                # Portal web Django
Docs/               # Documentação do projeto
Banco de Dados/     # Script de migração SQL (Migration_Sprint1.sql)
```

## 🚀 Como Rodar

### Desktop (Java)
```bash
cd FoodVerse
mvn clean compile
mvn exec:java
```

### Web (Django)
```bash
cd Web/projeto
pip install -r ../requirements.txt
python manage.py runserver
```

