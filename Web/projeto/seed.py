import os
import django
import random
from decimal import Decimal
from datetime import timedelta

# 1. CONFIGURAÇÃO DO AMBIENTE DJANGO
# Certifique-se de que 'projeto' é o nome da pasta que contém seu settings.py
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'projeto.settings')
django.setup()

# 2. IMPORTAÇÃO DOS MODELOS
from django.utils import timezone
from foodverse.models import (
    TbClientes, TbRestaurantes, TbProdutos, TbEstoque, TbNutricao,
    TbStatusPedido, TbPedidos, TbPedidosProdutos, TbPagamentos,
    TbAvaliacoes, TbCupons, TbFidelidade, TbFuncionarios
)

def gerar_dados_em_massa():
    print("🧹 Limpando dados antigos para evitar conflitos (opcional)...")
    # Ordem reversa de dependência para evitar erros de Foreign Key
    TbPagamentos.objects.all().delete()
    TbPedidosProdutos.objects.all().delete()
    TbPedidos.objects.all().delete()
    TbNutricao.objects.all().delete()
    TbEstoque.objects.all().delete()
    TbProdutos.objects.all().delete()
    TbRestaurantes.objects.all().delete()
    TbFidelidade.objects.all().delete()
    TbClientes.objects.all().delete()
    TbFuncionarios.objects.all().delete()

    print("⏳ Iniciando inserção massiva de dados...")

    # --- 1. Status de Pedido (Fixos) ---
    status_nomes = ["Pendente", "Preparando", "Saiu para Entrega", "Entregue", "Cancelado"]
    status_objs = []
    for i, nome in enumerate(status_nomes, 1):
        obj, _ = TbStatusPedido.objects.get_or_create(id_status=i, defaults={'nome_status': nome})
        status_objs.append(obj)

    # --- 2. Clientes (50 registros) ---
    clientes = []
    for i in range(50):
        c = TbClientes.objects.create(
            username=f"user_{i}_{random.randint(100, 999)}",
            nome=f"Cliente {i}",
            email=f"cliente{i}@foodverse.com",
            telefone=f"119{random.randint(10000000, 99999999)}",
            cpf=f"{random.randint(100,999)}.{random.randint(100,999)}.{random.randint(100,999)}-{random.randint(10,99)}",
            data_cadastro=timezone.now() - timedelta(days=random.randint(1, 365))
        )
        clientes.append(c)
        # Tabela de Fidelidade vinculada
        TbFidelidade.objects.create(
            cliente=c, 
            pontos=random.randint(0, 500), 
            cashback=Decimal(random.uniform(0, 50)).quantize(Decimal('0.00'))
        )

    # --- 3. Restaurantes (10 registros) ---
    categorias = ["Pizzaria", "Hambúrguer", "Japonesa", "Italiana", "Vegana"]
    restaurantes = []
    for i in range(10):
        cat = random.choice(categorias)
        r = TbRestaurantes.objects.create(
            nome=f"{cat} do Chef {i}",
            categoria=cat,
            avaliacao=Decimal(random.uniform(3.5, 5.0)).quantize(Decimal('0.0')),
            tempo_entrega=f"{random.randint(20, 60)} min",
            taxa_entrega=Decimal(random.uniform(5, 18)).quantize(Decimal('0.00'))
        )
        restaurantes.append(r)

    # --- 4. Produtos e Estoque (5 produtos por restaurante) ---
    produtos = []
    for rest in restaurantes:
        for j in range(5):
            p = TbProdutos.objects.create(
                restaurante=rest,
                nome_produto=f"Prato Especial {rest.id_restaurante}-{j}",
                preco=Decimal(random.uniform(25, 95)).quantize(Decimal('0.00')),
                categoria=rest.categoria,
                disponivel=True,
                data_criacao=timezone.now()
            )
            produtos.append(p)
            
            # Estoque
            TbEstoque.objects.create(
                produto=p, 
                quantidade=random.randint(50, 200), 
                estoque_minimo=10,
                ultima_atualizacao=timezone.now()
            )
            
            # Nutrição
            TbNutricao.objects.create(
                produto=p, 
                kcal=random.randint(300, 800), 
                proteina=f"{random.randint(10, 30)}g", 
                carbo=f"{random.randint(20, 60)}g", 
                gordura=f"{random.randint(5, 25)}g"
            )

    # --- 5. Pedidos e Pagamentos (100 registros) ---
    for _ in range(100):
        cli = random.choice(clientes)
        res = random.choice(restaurantes)
        ped = TbPedidos.objects.create(
            cliente=cli,
            restaurante=res,
            status=random.choice(status_objs),
            data_pedido=timezone.now() - timedelta(days=random.randint(0, 30)),
            endereco_entrega=f"Rua das Palmeiras, {random.randint(10, 999)}",
            valor_total=Decimal('0.00')
        )
        
        # Itens do pedido
        total_acumulado = Decimal('0.00')
        prods_res = [p for p in produtos if p.restaurante == res]
        
        # Seleciona de 1 a 3 itens diferentes do mesmo restaurante
        itens_selecionados = random.sample(prods_res, random.randint(1, 3))
        for prod in itens_selecionados:
            qtd = random.randint(1, 2)
            TbPedidosProdutos.objects.create(pedido=ped, produto=prod, quantidade=qtd)
            total_acumulado += (prod.preco * qtd)
        
        # Atualiza valor total do pedido (produtos + taxa)
        ped.valor_total = total_acumulado + res.taxa_entrega
        ped.save()

        # Pagamento automático para o pedido
        TbPagamentos.objects.create(
            pedido=ped,
            metodo_pagamento=random.choice(["Cartão", "Pix", "Dinheiro"]),
            valor=ped.valor_total,
            data_pagamento=ped.data_pedido
        )

    # --- 6. Funcionários e Cupons ---
    for i in range(5):
        TbFuncionarios.objects.create(
            nome=f"Colaborador {i}",
            cargo=random.choice(["Caixa", "Gerente", "Cozinha"]),
            status="Ativo"
        )
    
    TbCupons.objects.get_or_create(codigo="FOO10", defaults={'desconto': 10.00, 'validade': '2026-12-31'})

    print(f"✅ CONCLUÍDO: O banco de dados foi populado com sucesso!")

if __name__ == "__main__":
    gerar_dados_em_massa()