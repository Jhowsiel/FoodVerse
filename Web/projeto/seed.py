import os
import django
import random
from decimal import Decimal
from datetime import datetime, time, timedelta

# CONFIGURAÇÃO DO AMBIENTE
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'projeto.settings')
django.setup()

from django.utils import timezone
from foodverse.models import (
    TbClientes, TbRestaurantes, TbProdutos, TbPedidos, 
    TbPedidosProdutos, TbStatusPedido, TbPagamentos, 
    TbAvaliacoes, TbReservas, TbNutricao, TbEstoque
)

def popular_banco():
    print("🧹 Limpando dados antigos...")
    # Ordem reversa de dependência para evitar erro de Foreign Key
    TbPagamentos.objects.all().delete()
    TbPedidosProdutos.objects.all().delete()
    TbPedidos.objects.all().delete()
    TbStatusPedido.objects.all().delete()
    TbNutricao.objects.all().delete()
    TbEstoque.objects.all().delete()
    TbProdutos.objects.all().delete()
    TbReservas.objects.all().delete()
    TbAvaliacoes.objects.all().delete()
    TbRestaurantes.objects.all().delete()
    TbClientes.objects.all().delete()

    print("🚀 Criando Status de Pedido...")
    status_nomes = ['Pendente', 'Preparando', 'A caminho', 'Entregue', 'Cancelado']
    status_objetos = []
    for i, nome in enumerate(status_nomes, 1):
        s = TbStatusPedido.objects.create(id_status=i, nome_status=nome)
        status_objetos.append(s)

    print("👥 Criando Clientes...")
    clientes = []
    for i in range(1, 6):
        c = TbClientes.objects.create(
            username=f"user_{i}",
            nome=f"Cliente Teste {i}",
            email=f"cliente{i}@foodverse.com",
            telefone=f"1199999000{i}",
            cpf=f"123.456.789-0{i}",
            endereco=f"Rua das Flores, {10 * i}",
            data_cadastro=timezone.now()
        )
        clientes.append(c)

    print("🍕 Criando Restaurantes e Produtos...")
    restaurantes_dados = [
        {"nome": "Burger King", "cat": "Fast Food"},
        {"nome": "Sushiloko", "cat": "Japonesa"},
        {"nome": "Cantina Italiana", "cat": "Massas"}
    ]
    
    for dado in restaurantes_dados:
        res = TbRestaurantes.objects.create(
            nome=dado["nome"],
            categoria=dado["cat"],
            descricao=f"O melhor de {dado['cat']} da região.",
            avaliacao=Decimal(random.uniform(3.5, 5.0)).quantize(Decimal('0.00')),
            tempo_entrega="30-45 min",
            taxa_entrega=Decimal('7.50'),
            ativo=True,
            aberto=True
        )

        # Criar 3 produtos para cada restaurante
        for j in range(1, 4):
            prod = TbProdutos.objects.create(
                restaurante=res,
                nome_produto=f"Prato {dado['nome']} {j}",
                descricao="Ingredientes frescos e selecionados.",
                preco=Decimal(random.uniform(20.0, 80.0)).quantize(Decimal('0.00')),
                categoria=dado["cat"],
                disponivel=True,
                data_criacao=timezone.now()
            )

            # Criar Estoque para o produto
            TbEstoque.objects.create(
                produto=prod,
                quantidade=random.randint(50, 100),
                estoque_minimo=10,
                ultima_atualizacao=timezone.now()
            )

            # Criar Nutrição para o produto
            TbNutricao.objects.create(
                produto=prod,
                kcal=random.randint(200, 800),
                proteina="20g",
                carbo="50g",
                gordura="15g"
            )

    print("📦 Criando Pedidos e Itens...")
    todos_produtos = list(TbProdutos.objects.all())
    
    for cliente in clientes:
        # Criar 2 pedidos para cada cliente
        for k in range(2):
            res_aleatorio = random.choice(TbRestaurantes.objects.all())
            pedido = TbPedidos.objects.create(
                cliente=cliente,
                restaurante=res_aleatorio,
                status=random.choice(status_objetos),
                data_pedido=timezone.now() - timedelta(days=random.randint(0, 5)),
                endereco_entrega=cliente.endereco,
                valor_total=Decimal('0.00')
            )

            # Adicionar 2 produtos ao pedido
            produtos_restaurante = [p for p in todos_produtos if p.restaurante == res_aleatorio]
            total_pedido = Decimal('0.00')
            
            for p_pedido in random.sample(produtos_restaurante, 2):
                qtd = random.randint(1, 3)
                TbPedidosProdutos.objects.create(
                    pedido=pedido,
                    produto=p_pedido,
                    quantidade=qtd
                )
                total_pedido += (p_pedido.preco * qtd)

            # Atualiza o valor total do pedido e cria pagamento
            pedido.valor_total = total_pedido + res_aleatorio.taxa_entrega
            pedido.save()

            TbPagamentos.objects.create(
                pedido=pedido,
                metodo_pagamento=random.choice(['Cartão de Crédito', 'Pix', 'Dinheiro']),
                valor=pedido.valor_total,
                data_pagamento=timezone.now()
            )

    print("⭐ Criando Avaliações...")
    for _ in range(10):
        TbAvaliacoes.objects.create(
            cliente=random.choice(clientes),
            restaurante=random.choice(TbRestaurantes.objects.all()),
            comentario="Excelente comida e entrega rápida!",
            nota=random.randint(4, 5),
            data_avaliacao=timezone.now()
        )

    print("📅 Criando Reservas...")
    for cliente in clientes:
        TbReservas.objects.create(
            cliente=cliente,
            restaurante=random.choice(TbRestaurantes.objects.all()),
            data_reserva=timezone.now() + timedelta(days=2),
            numero_pessoas=random.randint(2, 6),
            mesa=f"Mesa {random.randint(1, 20)}"
        )

    print("\n✅ SUCESSO! Banco de dados populado com dados de teste.")

if __name__ == "__main__":
    popular_banco()