import os
import django
import random
from decimal import Decimal
from datetime import timedelta

# Configuração do Django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'projeto.settings')
django.setup()

from django.utils import timezone
from django.contrib.auth.hashers import make_password
from django.contrib.auth.models import User
from foodverse.models import *

def popular_banco():
    print("🧹 [1/9] Limpando banco de dados (Estratégia Cascata)...")
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

    senha_padrao = make_password('123456')
    agora = timezone.now()

    print("🔐 [2/9] Criando Credenciais (Django, E-commerce e Java)...")
    if not User.objects.filter(username='admin').exists():
        User.objects.create_superuser('admin', 'admin@foodverse.com', '123456')

    cliente_site = TbClientes.objects.create(
        username="cliente", nome="Carlos Avaliador", email="cliente@email.com", telefone="11999990000",
        cpf="00011122233", senha=senha_padrao, endereco="Av. Paulista, 1000 - Bela Vista, SP", data_cadastro=agora
    )
    TbFidelidade.objects.create(cliente=cliente_site, pontos=500, cashback=Decimal("50.00"))

    outros_clientes = []
    for i in range(1, 6):
        cli = TbClientes.objects.create(username=f"user_{i}", nome=f"Usuário {i}", email=f"user{i}@email.com", cpf=f"1231231230{i}", senha=senha_padrao, endereco=f"Rua Genérica, {i}00, SP", data_cadastro=agora)
        outros_clientes.append(cli)

    TbFuncionarios.objects.create(restaurante=None, nome="Administrador Master", username="admin", senha=123456, cargo="Admin", status="Ativo")

    print("📦 [3/9] Criando Domínio de Status de Pedido...")
    status_dict = {}
    for i, nome in enumerate(["Pendente", "Preparando", "A caminho", "Entregue", "Cancelado"], 1):
        status_dict[nome] = TbStatusPedido.objects.create(id_status=i, nome_status=nome)

    print("🏢 [4/9] Criando 12 Restaurantes (Foco em São Paulo)...")
    rest_bra = TbRestaurantes.objects.create(nome="O Braseiro Paulista", categoria="Brasileira", descricao="A verdadeira experiência do churrasco e culinária paulista.", avaliacao=Decimal("4.8"), tempo_entrega="40-55 min", taxa_entrega=Decimal("8.50"), cupom="BRASEIRO10", imagem="https://images.unsplash.com/photo-1544025162-d76694265947?w=400&q=80", ativo=True, aberto=True)
    rest_jap = TbRestaurantes.objects.create(nome="Tokyo Sushi SP", categoria="Japonesa", descricao="Peixes frescos diariamente do Mercadão. Tradição e sabor.", avaliacao=Decimal("4.9"), tempo_entrega="30-50 min", taxa_entrega=Decimal("12.00"), imagem="https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=400&q=80", ativo=True, aberto=True)
    
    TbFuncionarios.objects.create(restaurante=rest_bra, nome="Gerente Braseiro", username="gerente_braseiro", senha=senha_padrao, cargo="Gerente", status="Ativo")
    TbFuncionarios.objects.create(restaurante=rest_jap, nome="Gerente Sushi", username="gerente_sushi", senha=senha_padrao, cargo="Gerente", status="Ativo")

    nomes_fig = ["Cantina da Nonna", "Smash Burger Berrini", "Vegano da Vila", "Nordeste Arretado", "Pizzaria Mooca", "Tacos Mexicanos", "Cozinha Árabe", "Doceria Gourmet", "Bar dos Espetos", "Pastelaria da Praça"]
    cats_fig = ["Italiana", "Fast Food", "Vegano", "Regional", "Pizzaria", "Mexicana", "Árabe", "Sobremesas", "Churrasco", "Lanches"]
    imgs_fig = [
        "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=400&q=80", "https://images.unsplash.com/photo-1550547660-d9450f859349?w=400&q=80",
        "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=400&q=80", "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=400&q=80",
        "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=400&q=80", "https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=400&q=80",
        "https://images.unsplash.com/photo-1528736235302-52922df5c122?w=400&q=80", "https://images.unsplash.com/photo-1557142046-c704a3adf364?w=400&q=80",
        "https://images.unsplash.com/photo-1585238341210-940cb0dcbc41?w=400&q=80", "https://images.unsplash.com/photo-1628840042765-356cda07504e?w=400&q=80"
    ]
    for i in range(10):
        TbRestaurantes.objects.create(nome=nomes_fig[i], categoria=cats_fig[i], descricao=f"Especialidade em {cats_fig[i]} na sua região.", avaliacao=Decimal(random.uniform(4.0, 4.9)).quantize(Decimal("0.0")), tempo_entrega="30-45 min", taxa_entrega=Decimal("6.00"), imagem=imgs_fig[i], ativo=True, aberto=True)

    print("🥩 [5/9] Gerando Pratos e Bebidas (O Braseiro Paulista)...")
    insumo_carne = TbProdutos.objects.create(restaurante=rest_bra, nome_produto="Corte Bovino Premium", tipo_produto='INSUMO', preco=Decimal("45.00"), disponivel=True)
    TbEstoque.objects.create(produto=insumo_carne, quantidade=Decimal("150"), estoque_minimo=Decimal("30"), unidade="kg")
    
    pratos_bra = [
        {"nome": "Picanha na Brasa (500g)", "preco": 129.90, "img": "https://images.unsplash.com/photo-1720052875430-eecbd1caef16?q=400&q=80", "rest": "Sem restrições", "kcal": 1200},
        {"nome": "Feijoada Completa", "preco": 89.50, "img": "https://wallpapers.com/images/high/brazilian-feijoada-in-a-clay-pot-l952r9oo0w9fl2a3.webp", "rest": "Contém Glúten, Porco", "kcal": 1500},
        {"nome": "Bife Ancho Suculento", "preco": 95.00, "img": "https://onthelist.com.br/uploads/2014/07/MG_9712_ok.jpg?q=90&w=825&h=382", "rest": "Sem restrições", "kcal": 950},
        {"nome": "Costela no Bafo", "preco": 110.00, "img": "https://folhago.com.br/blogs/receitas-faceis/wp-content/uploads/2022/07/costela-no-bafo.jpg", "rest": "Sem restrições", "kcal": 1300},
        {"nome": "Frango Grelhado Light", "preco": 39.90, "img": "https://radio93fm.com.br/wp-content/uploads/2020/12/frango-grelhado.jpg", "rest": "Fitness, Baixo Carbo", "kcal": 350},
        {"nome": "Baião de Dois Tradicional", "preco": 55.00, "img": "https://folhago.com.br/blogs/receitas-faceis/wp-content/uploads/2021/12/Baiao-de-dois-canva.jpg", "rest": "Contém Lactose", "kcal": 850},
        {"nome": "Linguiça Artesanal Apimentada", "preco": 45.00, "img": "https://images.tcdn.com.br/img/img_prod/1027475/linguica_apimentada_fina_400g_233_2_493730b377c1d74433023902631ba45e.jpg", "rest": "Levemente apimentado", "kcal": 600},
        {"nome": "Cupim Casqueirado", "preco": 85.90, "img": "https://static.itdg.com.br/images/1200-630/ceea13491df7edcf1ce68614539dee2c/39216-original.jpg", "rest": "Sem restrições", "kcal": 1100},
        {"nome": "Maminha Assada na Manteiga", "preco": 92.00, "img": "https://folhago.com.br/blogs/receitas-faceis/wp-content/uploads/2022/08/Maminha-assada-na-manteiga-com-legumes-1.jpg", "rest": "Contém Lactose", "kcal": 1050},
        {"nome": "Salada de Maionese (Acompanhamento)", "preco": 25.00, "img": "https://folhago.com.br/blogs/receitas-faceis/wp-content/uploads/2021/03/receitadesaladademaionesecomlegumes-1130x580.jpg", "rest": "Vegetariano, Contém Ovo", "kcal": 450},
    ]

    prods_bra = []
    for pb in pratos_bra:
        p = TbProdutos.objects.create(restaurante=rest_bra, nome_produto=pb["nome"], preco=Decimal(str(pb["preco"])), categoria="Carnes e Acompanhamentos", tipo_produto='PRATO', disponivel=True, imagem=pb["img"], tempo_preparo=random.randint(20, 45), restricoes=pb["rest"], descricao=f"Autêntico prato preparado com excelência. ({pb['rest']})")
        TbNutricao.objects.create(produto=p, kcal=pb["kcal"], proteina=f"{random.randint(20, 70)}g", carbo=f"{random.randint(10, 50)}g", gordura=f"{random.randint(15, 40)}g")
        TbReceitas.objects.create(produto_venda=p, insumo=insumo_carne, quantidade=Decimal("0.500"), unidades="kg")
        prods_bra.append(p)

    bebidas_bra = [
        {"nome": "Guaraná Antarctica (Lata)", "preco": 8.00, "img": "https://assets.propmark.com.br/uploads/2020/01/guarana-antarctica.jpg"},
        {"nome": "Caipirinha de Limão", "preco": 12.00, "img": "https://guiadacozinha.com.br/wp-content/uploads/2023/09/Caipirinha-de-limao.jpg"},
        {"nome": "Coca-Cola (Vidro)", "preco": 10.90, "img": "https://images.unsplash.com/photo-1618914059174-40767c46f838?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0"}
    ]
    for b in bebidas_bra:
        prods_bra.append(TbProdutos.objects.create(restaurante=rest_bra, nome_produto=b["nome"], preco=Decimal(str(b["preco"])), categoria="Bebidas", tipo_produto='PRODUTO', disponivel=True, imagem=b["img"]))

    print("🍣 [6/9] Gerando Pratos e Bebidas (Tokyo Sushi SP)...")
    insumo_peixe = TbProdutos.objects.create(restaurante=rest_jap, nome_produto="Salmão Chileno", tipo_produto='INSUMO', preco=Decimal("85.00"), disponivel=True)
    TbEstoque.objects.create(produto=insumo_peixe, quantidade=Decimal("40"), estoque_minimo=Decimal("10"), unidade="kg")
    
    pratos_jap = [
        {"nome": "Combinado Salmão (30 peças)", "preco": 149.90, "img": "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=400&q=80", "rest": "Alérgicos: Peixe, Soja", "kcal": 750},
        {"nome": "Temaki de Salmão Completo", "preco": 35.00, "img": "https://images.unsplash.com/photo-1553621042-f6e147245754?w=400&q=80", "rest": "Contém Glúten (Cream Cheese)", "kcal": 320},
        {"nome": "Yakisoba Tradicional", "preco": 58.00, "img": "https://images.unsplash.com/photo-1585032226651-759b368d7246?w=400&q=80", "rest": "Contém Glúten, Soja", "kcal": 650},
        {"nome": "Hot Roll (10 peças)", "preco": 42.00, "img": "https://images.unsplash.com/photo-1611143669185-af224c5e3252?w=400&q=80", "rest": "Contém Glúten, Fritura", "kcal": 550},
        {"nome": "Sashimi Variado (15 fatias)", "preco": 75.00, "img": "https://images.unsplash.com/photo-1615361200141-f45040f367be?w=400&q=80", "rest": "Zero Carbo, Low Carb", "kcal": 250},
        {"nome": "Niguiri de Atum (Dupla)", "preco": 18.00, "img": "https://images.unsplash.com/photo-1583623025817-d180a2221d0a?w=400&q=80", "rest": "Contém Frutos do Mar", "kcal": 120},
        {"nome": "Uramaki California (8 peças)", "preco": 28.00, "img": "https://images.unsplash.com/photo-1617196034738-26c5f7c977ce?w=400&q=80", "rest": "Contém Frutos do Mar", "kcal": 280},
        {"nome": "Teppanyaki de Frango", "preco": 48.90, "img": "https://images.unsplash.com/photo-1529042410759-befb1204b468?w=400&q=80", "rest": "Sem Lactose", "kcal": 450},
        {"nome": "Shimeji na Manteiga", "preco": 32.00, "img": "https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=400&q=80", "rest": "Vegetariano, Contém Lactose", "kcal": 220},
        {"nome": "Ceviche de Peixe Branco", "preco": 45.00, "img": "https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?w=400&q=80", "rest": "Pimenta, Cítrico", "kcal": 180},
    ]

    prods_jap = []
    for pj in pratos_jap:
        p = TbProdutos.objects.create(restaurante=rest_jap, nome_produto=pj["nome"], preco=Decimal(str(pj["preco"])), categoria="Japonesa", tipo_produto='PRATO', disponivel=True, imagem=pj["img"], tempo_preparo=random.randint(15, 30), restricoes=pj["rest"], descricao=f"Corte fresco e saboroso. ({pj['rest']})")
        TbNutricao.objects.create(produto=p, kcal=pj["kcal"], proteina=f"{random.randint(10, 40)}g", carbo=f"{random.randint(5, 60)}g", gordura=f"{random.randint(2, 20)}g")
        TbReceitas.objects.create(produto_venda=p, insumo=insumo_peixe, quantidade=Decimal("0.250"), unidades="kg")
        prods_jap.append(p)

    bebidas_jap = [
        {"nome": "Sake Nacional 175ml", "preco": 25.00, "img": "https://lovefoodfeed.com/wp-content/uploads/2024/02/what-is-sake-px-1200-03-1024x1024.jpg"},
        {"nome": "Chá Verde Gelado (Matcha)", "preco": 12.00, "img": "https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=400&q=80"},
        {"nome": "Coca-Cola Zero (Lata)", "preco": 7.00, "img": "https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=400&q=80"}
    ]
    for b in bebidas_jap:
        prods_jap.append(TbProdutos.objects.create(restaurante=rest_jap, nome_produto=b["nome"], preco=Decimal(str(b["preco"])), categoria="Bebidas", tipo_produto='PRODUTO', disponivel=True, imagem=b["img"]))

    print("📈 [7/9] Injetando Pedidos para Testar todos os 5 Status...")
    todos_clientes = [cliente_site] + outros_clientes
    status_lista_nome = ["Pendente", "Preparando", "A caminho", "Entregue", "Cancelado"]
    
    for i, nome_status in enumerate(status_lista_nome):
        rest_alvo = rest_bra if i % 2 == 0 else rest_jap
        prods_alvo = prods_bra if rest_alvo == rest_bra else prods_jap
        
        pedido = TbPedidos.objects.create(cliente=cliente_site, restaurante=rest_alvo, status=status_dict[nome_status], data_pedido=agora - timedelta(hours=i), endereco_entrega=cliente_site.endereco, valor_total=Decimal("0.00"))
        
        total = Decimal("0.00")
        produtos_sorteados = random.sample(prods_alvo, k=random.randint(2, 3))
        for prod in produtos_sorteados:
            qtd = random.randint(1, 2)
            TbPedidosProdutos.objects.create(pedido=pedido, produto=prod, quantidade=qtd)
            total += (prod.preco * qtd)
        
        pedido.valor_total = total + rest_alvo.taxa_entrega
        pedido.save()
        if nome_status in ["Entregue", "Cancelado"]:
            TbPagamentos.objects.create(pedido=pedido, metodo_pagamento="Pix", valor=pedido.valor_total, data_pagamento=agora)

    print("📊 [8/9] Criando Histórico Pesado para Popular Relatórios Java...")
    for _ in range(30):
        c = random.choice(outros_clientes)
        rest_alvo = random.choice([rest_bra, rest_jap])
        prods_alvo = prods_bra if rest_alvo == rest_bra else prods_jap
        
        ped = TbPedidos.objects.create(cliente=c, restaurante=rest_alvo, status=status_dict["Entregue"], data_pedido=agora - timedelta(days=random.randint(1, 30)), endereco_entrega=c.endereco, valor_total=Decimal("0.00"))
        total = Decimal("0.00")
        for prod in random.sample(prods_alvo, k=2):
            TbPedidosProdutos.objects.create(pedido=ped, produto=prod, quantidade=1)
            total += prod.preco
        ped.valor_total = total + rest_alvo.taxa_entrega
        ped.save()

    print("⭐ [9/9] Gerando Avaliações, Reservas e Cupons...")
    
    comentarios = ["Comida maravilhosa, nota 10!", "Chegou super rápido e quente.", "O melhor prato que já pedi.", "Atendimento excelente do restaurante.", "Sabor impecável e porção generosa."]
    
    # Avaliações dos Pratos (Para popular o Django no detalhe do prato)
    todos_produtos = prods_bra + prods_jap
    for prod in todos_produtos:
        # Cria de 2 a 4 avaliações por produto
        for _ in range(random.randint(2, 4)):
            TbAvaliacoesProdutos.objects.create(
                cliente=random.choice(outros_clientes),
                produto=prod,
                nota=random.randint(4, 5),
                comentario=random.choice(comentarios),
                data_avaliacao=agora - timedelta(days=random.randint(1, 15))
            )

    # Avaliações Gerais dos Restaurantes
    for rest in [rest_bra, rest_jap]:
        for _ in range(5):
            TbAvaliacoes.objects.create(
                cliente=random.choice(outros_clientes),
                restaurante=rest,
                nota=random.randint(4, 5),
                comentario="Um dos melhores restaurantes de São Paulo, recomendo demais!",
                data_avaliacao=agora - timedelta(days=random.randint(1, 15))
            )

    # Reservas de Mesas (Para aparecer no Java e no Perfil do Cliente Django)
    TbReservas.objects.create(cliente=cliente_site, restaurante=rest_bra, data_reserva=agora + timedelta(days=2), numero_pessoas=4, mesa="A12")
    TbReservas.objects.create(cliente=cliente_site, restaurante=rest_jap, data_reserva=agora + timedelta(days=5), numero_pessoas=2, mesa="M05")

    # Cupons de Desconto (Para uso no carrinho)
    TbCupons.objects.create(codigo="BEMVINDO10", desconto=Decimal("10.00"), validade=agora.date() + timedelta(days=30))
    TbCupons.objects.create(codigo="MASTER20", desconto=Decimal("20.00"), validade=agora.date() + timedelta(days=30))

    print("\n✅ MEGA SEED FINALIZADA COM SUCESSO! Sistema pronto e realista para a Banca.")
    print("\n📋 DADOS PARA LOGIN - DJANGO (E-Commerce):")
    print("   Usuário (Cliente): cliente | Senha: 123456")
    print("   Administrador do Django (/admin): admin | Senha: 123456")
    print("\n📋 DADOS PARA LOGIN - JAVA (Desktop Backoffice):")
    print("   Administrador Geral: admin | Senha: 123456")
    print("   Gerente O Braseiro: gerente_braseiro | Senha: 123456")
    print("   Gerente Tokyo Sushi: gerente_sushi | Senha: 123456")

if __name__ == "__main__":
    popular_banco()
