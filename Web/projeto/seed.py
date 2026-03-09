import os
import django
from django.utils import timezone

# 1. CONFIGURAÇÃO DO DJANGO
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'projeto.settings') # <--- Ajuste se o nome da pasta do projeto for outro
django.setup()

from foodverse.models import (
    TbRestaurantes, TbProdutos, TbNutricao, TbStatusPedido, TbCupons
)

def popular_banco():
    print("🚀 Iniciando a inserção de dados...")

    # 2. STATUS DE PEDIDO
    print("Inserindo status...")
    status_list = [
        (1, 'Pendente'),
        (2, 'Preparando'),
        (3, 'Em Rota'),
        (4, 'Entregue'),
        (5, 'Cancelado')
    ]
    for id_s, nome in status_list:
        TbStatusPedido.objects.get_or_create(id_status=id_s, nome_status=nome)

    # 3. CUPONS
    print("Inserindo cupons...")
    TbCupons.objects.get_or_create(codigo="FOOD10", desconto=10.00, validade="2026-12-31")
    TbCupons.objects.get_or_create(codigo="FRETEGRATIS", desconto=0.00, validade="2026-12-31")

    # 4. RESTAURANTES
    print("Inserindo restaurantes...")
    res1, _ = TbRestaurantes.objects.get_or_create(
        nome="Sabor da Casa",
        categoria="Brasileira",
        descricao="Comida caseira feita com amor.",
        avaliacao=4.8,
        tempo_entrega="30-45 min",
        taxa_entrega=5.00,
        cupom="SABOR10",
        imagem="https://images.unsplash.com/photo-1547592166-23ac45744acd?auto=format&fit=crop&w=800&q=80"
    )

    res2, _ = TbRestaurantes.objects.get_or_create(
        nome="Burger Mania",
        categoria="Lanches",
        descricao="Hambúrgueres artesanais suculentos.",
        avaliacao=4.9,
        tempo_entrega="20-30 min",
        taxa_entrega=0.00,
        cupom="BURGER5",
        imagem="https://images.unsplash.com/photo-1571091718767-18b5b1457add?auto=format&fit=crop&w=800&q=80"
    )

    # 5. PRODUTOS (Ligados aos Restaurantes)
    print("Inserindo produtos...")
    # Prato para Sabor da Casa
    prod1, _ = TbProdutos.objects.get_or_create(
        restaurante=res1,
        nome_produto="Feijoada Completa",
        descricao="Acompanha arroz, couve e farofa.",
        preco=45.90,
        categoria="Prato Principal",
        disponivel=True,
        destaque=True,
        data_criacao=timezone.now(),
        imagem="https://images.unsplash.com/photo-1512058560366-cd242d458730?auto=format&fit=crop&w=800&q=80"
    )

    # Burger para Burger Mania
    prod2, _ = TbProdutos.objects.get_or_create(
        restaurante=res2,
        nome_produto="Cheese Bacon Deluxe",
        descricao="Pão brioche, 180g de carne e muito bacon.",
        preco=32.00,
        categoria="Hambúrguer",
        disponivel=True,
        destaque=True,
        data_criacao=timezone.now(),
        imagem="https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=800&q=80"
    )

    # 6. NUTRIÇÃO (Ligado aos Produtos)
    print("Inserindo dados nutricionais...")
    TbNutricao.objects.get_or_create(
        produto=prod1,
        kcal=850,
        proteina="45g",
        carbo="90g",
        gordura="30g"
    )
    
    TbNutricao.objects.get_or_create(
        produto=prod2,
        kcal=650,
        proteina="35g",
        carbo="40g",
        gordura="38g"
    )

    print("✅ Banco de dados populado com sucesso!")

if __name__ == "__main__":
    popular_banco()