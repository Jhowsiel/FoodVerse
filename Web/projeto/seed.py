import os
import django
import random
from decimal import Decimal
from datetime import timedelta

# Configuração do Django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'projeto.settings')
django.setup()

from django.utils import timezone
from foodverse.models import *

def popular_banco():
    print("🧹 Limpando banco de dados...")
    
    # Ordem de deleção para evitar erros de FK
    TbMovimentacaoEstoque.objects.all().delete()
    TbReceitas.objects.all().delete()
    TbAvaliacoesProdutos.objects.all().delete()
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

    # --- LISTAS DE APOIO ---
    categorias_rest = ["Fast Food", "Japonesa", "Italiana", "Churrasco", "Pizzaria", "Saudável", "Vegano"]
    nomes_clientes = ["Ana Silva", "Bruno Souza", "Carla Dias", "Diego Lima", "Elena Torres", "Fabio Melo", "Gisele Bündchen", "Hugo Gloss"]
    comentarios = ["Excelente comida!", "Chegou muito rápido.", "O tempero é único.", "Preço justo e qualidade alta.", "Melhor da região!"]
    
    # URLs de imagens reais (Unsplash)
    img_restaurantes = [
        "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4",
        "https://images.unsplash.com/photo-1552566626-52f8b828add9",
        "https://images.unsplash.com/photo-1555396273-367ea4eb4db5"
    ]
    img_produtos = [
        "https://images.unsplash.com/photo-1504674900247-0877df9cc836",
        "https://images.unsplash.com/photo-1567620905732-2d1ec7bb7445",
        "https://images.unsplash.com/photo-1473093226795-af9932fe5856"
    ]

    print("📦 Criando Status...")
    status_objs = []
    for i, nome in enumerate(["Pendente", "Preparando", "A caminho", "Entregue", "Cancelado"], 1):
        status_objs.append(TbStatusPedido.objects.create(id_status=i, nome_status=nome))

    print("👥 Criando Clientes e Fidelidade...")
    clientes = []
    for i in range(1, 51):
        c = TbClientes.objects.create(
            username=f"user_{i}",
            nome=f"{random.choice(nomes_clientes)} {i}",
            email=f"cliente{i}@email.com",
            telefone=f"119{random.randint(10000000, 99999999)}",
            cpf=f"{random.randint(100, 999)}{random.randint(100, 999)}{random.randint(100, 999)}{random.randint(10, 99)}",
            senha="pbkdf2_sha256$...", # Ideal usar make_password
            endereco=f"Avenida Paulista, {1000 + i}, SP",
            data_cadastro=timezone.now()
        )
        clientes.append(c)
        TbFidelidade.objects.create(
            cliente=c,
            pontos=random.randint(0, 500),
            cashback=Decimal(random.uniform(0, 50)).quantize(Decimal("0.00"))
        )

    print("🍕 Criando Restaurantes e Funcionários...")
    restaurantes = []
    for i in range(1, 11):
        r = TbRestaurantes.objects.create(
            nome=f"Gastronomia {random.choice(categorias_rest)} {i}",
            categoria=random.choice(categorias_rest),
            descricao=f"O melhor de {random.choice(categorias_rest)} na sua mesa.",
            avaliacao=Decimal(random.uniform(3.5, 5.0)).quantize(Decimal("0.0")),
            tempo_entrega=f"{random.randint(20, 50)} min",
            taxa_entrega=Decimal(random.uniform(5.0, 15.0)).quantize(Decimal("0.00")),
            cupom=f"PROMO{i}",
            imagem=f"{random.choice(img_restaurantes)}?auto=format&fit=crop&w=400&q=80",
            banner=f"{random.choice(img_restaurantes)}?auto=format&fit=crop&w=1200&q=80",
            ativo=True,
            aberto=True
        )
        restaurantes.append(r)
        
        # Funcionários
        for j in range(2):
            TbFuncionarios.objects.create(
                restaurante=r,
                nome=f"Colaborador {i}-{j}",
                username=f"staff_{i}_{j}",
                email=f"staff{i}_{j}@restaurante.com",
                cargo="Gerente" if j == 0 else "Atendente",
                status="Ativo",
                data_cadastro=timezone.now()
            )

    print("🍔 Criando Produtos, Estoque, Nutrição e Receitas...")
    todos_produtos = []
    for rest in restaurantes:
        # Criar Insumos primeiro (tipo_produto='INSUMO')
        insumos_locais = []
        for k in range(1, 6):
            insumo = TbProdutos.objects.create(
                restaurante=rest,
                nome_produto=f"Insumo {k} ({rest.nome})",
                tipo_produto='INSUMO',
                preco=Decimal(random.uniform(1, 10)).quantize(Decimal("0.00")),
                disponivel=True
            )
            insumos_locais.append(insumo)
            # Estoque do Insumo
            est = TbEstoque.objects.create(
                produto=insumo,
                quantidade=Decimal(random.randint(50, 100)),
                unidade='kg' if k % 2 == 0 else 'un',
                ultima_atualizacao=timezone.now()
            )
            # Movimentação Inicial
            TbMovimentacaoEstoque.objects.create(
                estoque=est, tipo="ENTRADA", quantidade=100, observacao="Carga inicial"
            )

        # Criar Produtos de Venda
        for i in range(1, 8):
            p = TbProdutos.objects.create(
                restaurante=rest,
                nome_produto=f"Prato Especial {i}",
                descricao="Ingredientes frescos e selecionados.",
                preco=Decimal(random.uniform(25, 90)).quantize(Decimal("0.00")),
                categoria=rest.categoria,
                imagem=f"{random.choice(img_produtos)}?auto=format&fit=crop&w=400&q=80",
                tempo_preparo=random.randint(15, 45),
                disponivel=True,
                destaque=random.choice([True, False]),
                tipo_produto='VENDA',
                data_criacao=timezone.now()
            )
            todos_produtos.append(p)
            
            # Nutrição
            TbNutricao.objects.create(
                produto=p, kcal=random.randint(300, 900),
                proteina=f"{random.randint(10, 40)}g",
                carbo=f"{random.randint(30, 80)}g",
                gordura=f"{random.randint(5, 30)}g"
            )

            # Receita (Ficha técnica ligando Venda -> Insumo)
            TbReceitas.objects.create(
                produto_venda=p,
                insumo=random.choice(insumos_locais),
                quantidade=Decimal("0.250"),
                unidades="kg"
            )

    print("🛒 Criando Pedidos, Itens e Pagamentos...")
    for _ in range(150):
        cliente_sort = random.choice(clientes)
        rest_sort = random.choice(restaurantes)
        prods_rest = [p for p in todos_produtos if p.restaurante == rest_sort]
        
        if prods_rest:
            pedido = TbPedidos.objects.create(
                cliente=cliente_sort,
                restaurante=rest_sort,
                status=random.choice(status_objs),
                data_pedido=timezone.now() - timedelta(days=random.randint(0, 30)),
                endereco_entrega=cliente_sort.endereco,
                valor_total=0
            )
            
            total_pedido = Decimal("0.00")
            for item in random.sample(prods_rest, k=random.randint(1, 3)):
                qtd = random.randint(1, 3)
                TbPedidosProdutos.objects.create(pedido=pedido, produto=item, quantidade=qtd)
                total_pedido += (item.preco * qtd)
            
            pedido.valor_total = total_pedido + rest_sort.taxa_entrega
            pedido.save()

            TbPagamentos.objects.create(
                pedido=pedido,
                metodo_pagamento=random.choice(["Cartão", "Pix", "Vale Refeição"]),
                valor=pedido.valor_total,
                data_pagamento=timezone.now()
            )

    print("⭐ Finalizando com Avaliações, Reservas e Cupons...")
    # Avaliações de Produtos
    for _ in range(100):
        TbAvaliacoesProdutos.objects.create(
            cliente=random.choice(clientes),
            produto=random.choice(todos_produtos),
            nota=random.randint(4, 5),
            comentario=random.choice(comentarios),
            data_avaliacao=timezone.now()
        )

    # Reservas
    for i in range(20):
        TbReservas.objects.create(
            cliente=random.choice(clientes),
            restaurante=random.choice(restaurantes),
            data_reserva=timezone.now() + timedelta(days=i),
            numero_pessoas=random.randint(2, 6),
            mesa=f"M{i+1}"
        )

    # Cupons
    for i in range(5):
        TbCupons.objects.create(
            codigo=f"BEMVINDO{i}",
            desconto=Decimal("15.00"),
            validade=timezone.now().date() + timedelta(days=30)
        )

    print("\n✅ SUCESSO! O ecossistema FoodVerse está populado e pronto para testes.")

if __name__ == "__main__":
    popular_banco()