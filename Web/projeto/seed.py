import os
import django
import random
from decimal import Decimal
from datetime import timedelta

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'projeto.settings')
django.setup()

from django.utils import timezone
from foodverse.models import (
    TbClientes, TbRestaurantes, TbProdutos, TbPedidos,
    TbPedidosProdutos, TbStatusPedido, TbPagamentos,
    TbAvaliacoes, TbReservas, TbNutricao, TbEstoque,
    TbCupons, TbFidelidade, TbFuncionarios
)

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

    print("📦 Criando Status Pedido...")

    status_nomes = ["Pendente", "Preparando", "A caminho", "Entregue", "Cancelado"]
    status_lista = []

    for i, nome in enumerate(status_nomes, 1):
        s = TbStatusPedido.objects.create(
            id_status=i,
            nome_status=nome
        )
        status_lista.append(s)

    print("👥 Criando Clientes...")

    clientes = []

    for i in range(1, 11):

        cliente = TbClientes.objects.create(
            username=f"user{i}",
            nome=f"Cliente {i}",
            email=f"cliente{i}@foodverse.com",
            telefone=f"119999900{i:02}",
            cpf=f"111.222.333-{i:02}",
            senha="123456",
            endereco=f"Rua Teste {i}",
            data_cadastro=timezone.now()
        )

        clientes.append(cliente)

        TbFidelidade.objects.create(
            cliente=cliente,
            pontos=random.randint(0, 500),
            cashback=Decimal(random.uniform(5, 50)).quantize(Decimal("0.00"))
        )

    print("🍕 Criando Restaurantes...")

    restaurantes_dados = [
        ("Burger King", "Fast Food"),
        ("Sushiloko", "Japonesa"),
        ("Cantina Italiana", "Massas"),
        ("Churrasco Prime", "Churrasco"),
        ("Pizza Master", "Pizzaria")
    ]

    restaurantes = []

    for nome, categoria in restaurantes_dados:

        r = TbRestaurantes.objects.create(
            nome=nome,
            categoria=categoria,
            descricao=f"O melhor de {categoria}",
            avaliacao=Decimal(random.uniform(3.5,5)).quantize(Decimal("0.00")),
            tempo_entrega=f"{random.randint(25,50)} min",
            taxa_entrega=Decimal(random.uniform(5,10)).quantize(Decimal("0.00")),
            cupom="FOOD10",
            imagem="restaurante.jpg",

            # controle de disponibilidade
            ativo=True,
            aberto=random.choice([True, True, True, False]) 
        )

        restaurantes.append(r)

        TbFuncionarios.objects.create(
            restaurante=r,
            nome=f"Gerente {nome}",
            username=f"gerente_{nome.lower().replace(' ','')}",
            email=f"{nome.lower().replace(' ','')}@foodverse.com",
            cargo="Gerente",
            telefone="11988887777",
            senha="123456",
            status="Ativo",
            data_cadastro=timezone.now()
        )

    print("🍔 Criando Produtos...")

    produtos = []

    for restaurante in restaurantes:

        for i in range(1,6):

            p = TbProdutos.objects.create(
                restaurante=restaurante,
                nome_produto=f"{restaurante.categoria} Especial {i}",
                descricao="Prato preparado com ingredientes frescos",
                preco=Decimal(random.uniform(20,80)).quantize(Decimal("0.00")),
                categoria=restaurante.categoria,
                imagem="produto.jpg",
                tempo_preparo=random.randint(10,30),
                disponivel=True,
                destaque=random.choice([True, False]),
                data_criacao=timezone.now()
            )

            produtos.append(p)

            TbEstoque.objects.create(
                produto=p,
                quantidade=random.randint(30,100),
                estoque_minimo=10,
                ultima_atualizacao=timezone.now()
            )

            TbNutricao.objects.create(
                produto=p,
                kcal=random.randint(300,900),
                proteina="25g",
                carbo="60g",
                gordura="20g"
            )

    print("🎟 Criando Cupons...")

    for i in range(1,5):

        TbCupons.objects.create(
            codigo=f"FOOD{i*10}",
            desconto=Decimal(i*5),
            validade=timezone.now().date() + timedelta(days=30)
        )

    print("📦 Criando Pedidos...")

    for cliente in clientes:

        for i in range(3):

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

            itens = random.sample(produtos_restaurante, min(3, len(produtos_restaurante)))

            for prod in itens:

                qtd = random.randint(1,3)

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

    print("⭐ Criando Avaliações...")

    for i in range(20):

        TbAvaliacoes.objects.create(
            cliente=random.choice(clientes),
            restaurante=random.choice(restaurantes),
            comentario=random.choice([
                "Muito bom!",
                "Entrega rápida",
                "Comida deliciosa",
                "Gostei bastante",
                "Voltarei a pedir"
            ]),
            nota=random.randint(3,5),
            data_avaliacao=timezone.now()
        )

    print("📅 Criando Reservas...")

    for cliente in clientes:

        TbReservas.objects.create(
            cliente=cliente,
            restaurante=random.choice(restaurantes),
            data_reserva=timezone.now() + timedelta(days=random.randint(1,5)),
            numero_pessoas=random.randint(2,6),
            mesa=f"Mesa {random.randint(1,20)}"
        )

    print("\n✅ Banco populado com sucesso!")

if __name__ == "__main__":
    popular_banco()