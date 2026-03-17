import os
import django
import random
from decimal import Decimal
from datetime import timedelta

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'projeto.settings')
django.setup()

from django.utils import timezone
from foodverse.models import *

def popular_banco():

    print("🧹 Limpando banco...")

    TbPagamentos.objects.all().delete()
    TbPedidosProdutos.objects.all().delete()
    TbPedidos.objects.all().delete()
    TbStatusPedido.objects.all().delete()
    TbNutricao.objects.all().delete()
    TbEstoque.objects.all().delete()
    TbProdutos.objects.all().delete()
    TbReservas.objects.all().delete()
    TbAvaliacoes.objects.all().delete()
    TbFuncionarios.objects.all().delete()
    TbCupons.objects.all().delete()
    TbFidelidade.objects.all().delete()
    TbRestaurantes.objects.all().delete()
    TbClientes.objects.all().delete()

    print("📦 Criando Status...")

    status_lista = []
    for i, nome in enumerate(["Pendente", "Preparando", "A caminho", "Entregue", "Cancelado"], 1):
        status_lista.append(TbStatusPedido.objects.create(id_status=i, nome_status=nome))

    print("👥 Criando MUITOS Clientes...")

    clientes = []
    for i in range(1, 101):  # 🔥 100 clientes

        cliente = TbClientes.objects.create(
            username=f"user{i}",
            nome=f"Cliente {i}",
            email=f"cliente{i}@foodverse.com",
            telefone=f"1199999{i:04}",
            cpf=f"{i:011}",
            senha="123456",
            endereco=f"Rua {i}",
            data_cadastro=timezone.now()
        )

        clientes.append(cliente)

        TbFidelidade.objects.create(
            cliente=cliente,
            pontos=random.randint(0, 2000),
            cashback=Decimal(random.uniform(0, 100)).quantize(Decimal("0.00"))
        )

    print("🍕 Criando MUITOS Restaurantes...")

    categorias = ["Fast Food", "Japonesa", "Italiana", "Churrasco", "Pizzaria", "Saudável"]

    restaurantes = []
    for i in range(1, 21):  # 🔥 20 restaurantes

        r = TbRestaurantes.objects.create(
            nome=f"Restaurante {i}",
            categoria=random.choice(categorias),
            descricao="Top comida",
            avaliacao=Decimal(random.uniform(3.0,5)).quantize(Decimal("0.00")),
            tempo_entrega=f"{random.randint(20,60)} min",
            taxa_entrega=Decimal(random.uniform(3,15)).quantize(Decimal("0.00")),
            cupom="FOOD10",
            imagem="img.jpg",
            ativo=True,
            aberto=random.choice([True, True, True, False])
        )

        restaurantes.append(r)

        # 🔥 vários funcionários
        for j in range(3):
            TbFuncionarios.objects.create(
                restaurante=r,
                nome=f"Funcionario {i}-{j}",
                username=f"func_{i}_{j}",
                email=f"func{i}{j}@mail.com",
                cargo=random.choice(["Gerente","Atendente"]),
                telefone="11999999999",
                senha="123",
                status="Ativo",
                data_cadastro=timezone.now()
            )

    print("🍔 Criando MUITOS Produtos...")

    produtos = []

    for restaurante in restaurantes:
        for i in range(1, 16):  # 🔥 15 produtos por restaurante

            p = TbProdutos.objects.create(
                restaurante=restaurante,
                nome_produto=f"Produto {i}",
                descricao="Muito bom",
                preco=Decimal(random.uniform(10,120)).quantize(Decimal("0.00")),
                categoria=restaurante.categoria,
                imagem="produto.jpg",
                tempo_preparo=random.randint(5,40),
                disponivel=True,
                destaque=random.choice([True, False]),
                data_criacao=timezone.now()
            )

            produtos.append(p)

            TbEstoque.objects.create(
                produto=p,
                quantidade=random.randint(10,200),
                estoque_minimo=5,
                ultima_atualizacao=timezone.now()
            )

            TbNutricao.objects.create(
                produto=p,
                kcal=random.randint(200,1000),
                proteina=f"{random.randint(10,50)}g",
                carbo=f"{random.randint(20,100)}g",
                gordura=f"{random.randint(5,40)}g"
            )

    print("🎟 Criando Cupons...")

    for i in range(1, 11):  # 🔥 10 cupons
        TbCupons.objects.create(
            codigo=f"CUPOM{i}",
            desconto=Decimal(random.randint(5,30)),
            validade=timezone.now().date() + timedelta(days=60)
        )

    print("📦 Criando MUITOS Pedidos...")

    for cliente in clientes:

        for _ in range(random.randint(3,10)):  # 🔥 até 10 pedidos por cliente

            restaurante = random.choice(restaurantes)

            if not restaurante.aberto:
                continue

            pedido = TbPedidos.objects.create(
                cliente=cliente,
                restaurante=restaurante,
                status=random.choice(status_lista),
                data_pedido=timezone.now(),
                endereco_entrega=cliente.endereco,
                valor_total=Decimal("0.00")
            )

            produtos_restaurante = [p for p in produtos if p.restaurante == restaurante]

            total = Decimal("0.00")

            itens = random.sample(produtos_restaurante, min(5, len(produtos_restaurante)))

            for prod in itens:

                qtd = random.randint(1,5)

                TbPedidosProdutos.objects.create(
                    pedido=pedido,
                    produto=prod,
                    quantidade=qtd
                )

                total += prod.preco * qtd

            pedido.valor_total = total + restaurante.taxa_entrega
            pedido.save()

            TbPagamentos.objects.create(
                pedido=pedido,
                metodo_pagamento=random.choice(["Pix","Cartão","Dinheiro"]),
                valor=pedido.valor_total,
                data_pagamento=timezone.now()
            )

    print("⭐ Criando MUITAS Avaliações...")

    for _ in range(200):  # 🔥 200 avaliações
        TbAvaliacoes.objects.create(
            cliente=random.choice(clientes),
            restaurante=random.choice(restaurantes),
            comentario=random.choice([
                "Muito bom!", "Top", "Entrega rápida", "Excelente", "Perfeito"
            ]),
            nota=random.randint(1,5),
            data_avaliacao=timezone.now()
        )

    print("📅 Criando Reservas...")

    for _ in range(100):  # 🔥 100 reservas
        TbReservas.objects.create(
            cliente=random.choice(clientes),
            restaurante=random.choice(restaurantes),
            data_reserva=timezone.now() + timedelta(days=random.randint(1,10)),
            numero_pessoas=random.randint(1,10),
            mesa=f"Mesa {random.randint(1,50)}"
        )

    print("\n🔥 BANCO LOTADO COM DADOS!")

if __name__ == "__main__":
    popular_banco()