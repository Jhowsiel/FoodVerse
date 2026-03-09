from decimal import Decimal

from django.http import JsonResponse
from django.shortcuts import render, redirect, get_object_or_404
from django.contrib import messages
from django.utils import timezone
from django.db.models import Q
from django.contrib.auth.hashers import make_password, check_password
import requests
import re

# Importando as suas models
from .models import (
    TbClientes, TbRestaurantes, TbProdutos, TbNutricao, TbAvaliacoes
)

# -------------------------------------------------------------------------
# FUNÇÕES AUXILIARES
# -------------------------------------------------------------------------
def _int_param(valor, padrao):
    """Converte parâmetro para inteiro com segurança."""
    try:
        return int(valor)
    except (TypeError, ValueError):
        return padrao

def get_cliente_logado(request):
    """Retorna a instância do cliente logado ou None."""
    cliente_id = request.session.get('cliente_id')
    if cliente_id:
        return TbClientes.objects.filter(id_cliente=cliente_id).first()
    return None

# -------------------------------------------------------------------------
# PÁGINA INICIAL
# -------------------------------------------------------------------------
def home(request):
    endereco = None
    cep = None

    if request.method == 'POST':
        cep = request.POST.get('cep', '').strip()
        if cep:
            url = f"https://viacep.com.br/ws/{cep}/json/"
            try:
                response = requests.get(url, timeout=5)
                data = response.json()
                if "erro" not in data:
                    endereco = f"{data.get('logradouro', '')}, {data.get('bairro', '')}, {data.get('localidade', '')} - {data.get('uf', '')}"
            except requests.RequestException:
                endereco = None

    restaurantes = TbRestaurantes.objects.all()
    categorias = TbRestaurantes.objects.values_list('categoria', flat=True).distinct()

    context = {
        'endereco': endereco,
        'cep': cep,
        'restaurantes': restaurantes,
        'categorias': sorted(filter(None, categorias)),
        'cliente_logado': get_cliente_logado(request),
    }
    return render(request, 'index.html', context)

# -------------------------------------------------------------------------
# AUTENTICAÇÃO E CADASTRO
# -------------------------------------------------------------------------
def login_view(request):
    if get_cliente_logado(request):
        return redirect('home')

    if request.method == 'POST':
        username = request.POST.get('username')
        password = request.POST.get('password')
        remember_me = request.POST.get('remember_me')

        cliente = TbClientes.objects.filter(username=username).first()

        if cliente and check_password(password, cliente.senha):
            request.session['cliente_id'] = cliente.id_cliente
            request.session.set_expiry(1209600 if remember_me else 0)
            messages.success(request, 'Login realizado com sucesso!')
            # return render(request, 'index.html')
        else:
            messages.error(request, 'Usuário ou senha incorretos.')

    return render(request, 'pages/Autentificacao/login.html')

def logout_view(request):
    if 'cliente_id' in request.session:
        del request.session['cliente_id']
    list(messages.get_messages(request))  # limpa todas as mensagens pendentes
    return redirect('login')

def cadastro_view(request):
    if get_cliente_logado(request):
        return redirect('home')

    if request.method == 'POST':
        nome = request.POST.get('name')
        username = request.POST.get('username')
        cpf = request.POST.get('cpf')
        telefone = request.POST.get('telefone')
        email = request.POST.get('email')
        password = request.POST.get('password')
        confirm_password = request.POST.get('confirm_password')

        erros = []

        if not all([nome, username, cpf, telefone, email, password, confirm_password]):
            erros.append("• Todos os campos são obrigatórios.")
        
        if TbClientes.objects.filter(username=username).exists():
            erros.append("• Este nome de usuário já está em uso.")
        if TbClientes.objects.filter(cpf=cpf).exists():
            erros.append("• Este CPF já está cadastrado.")

        if erros:
            for erro in erros:
                messages.error(request, erro)
            return render(request, 'pages/Autentificacao/cadastro.html')

        try:
            TbClientes.objects.create(
                nome=nome,
                username=username,
                email=email,
                cpf=cpf,
                telefone=telefone,
                senha=make_password(password),
                data_cadastro=timezone.now()
            )
            messages.success(request, 'Cadastro realizado com sucesso! Faça login.')
            return redirect('login')
        except Exception:
            messages.error(request, 'Ocorreu um erro ao criar a conta.')

    return render(request, 'pages/Autentificacao/cadastro.html')

# -------------------------------------------------------------------------
# PERFIL DO CLIENTE
# -------------------------------------------------------------------------
def perfil_view(request):
    cliente = get_cliente_logado(request)
    
    if not cliente:
        return redirect('login')
    return render(request, 'pages/perfil/perfil.html', {'perfil': cliente})


def editar_perfil_view(request):
    cliente = get_cliente_logado(request)
    if not cliente:
        return redirect('login')

    if request.method == 'POST':
        cliente.username = request.POST.get('username')
        cliente.email = request.POST.get('email')
        cliente.telefone = request.POST.get('telefone')
        cliente.endereco = request.POST.get('endereco')
        cliente.save()
        messages.success(request, 'Perfil atualizado!')
        return redirect('perfil')

    return render(request, 'pages/perfil/editar/editar.html', {'perfil': cliente})

# -------------------------------------------------------------------------
# CATÁLOGO: RESTAURANTES E PRATOS
# -------------------------------------------------------------------------
def restaurante_view(request):
    categoria = request.GET.get('categoria')
    restaurantes = TbRestaurantes.objects.all()
    
    if categoria:
        restaurantes = restaurantes.filter(categoria=categoria)

    categorias = TbRestaurantes.objects.values_list('categoria', flat=True).distinct()

    return render(request, 'pages/catalogo/restaurante.html', {
        'restaurantes': restaurantes,
        'categorias': sorted(filter(None, categorias)),
        'categoria_ativa': categoria,
        'restaurante_destaque': restaurantes.first(),
    })

def restaurante_detalhe_view(request, id):
    restaurante = get_object_or_404(TbRestaurantes, id_restaurante=id)
    # Filtra produtos usando a instância do restaurante
    pratos = TbProdutos.objects.filter(restaurante=restaurante, disponivel=True)

    return render(request, 'pages/catalogo/restaurante_detalhe.html', {
        'restaurante': restaurante,
        'pratos': pratos
    })

def prato_view(request):
    rest_id = _int_param(request.GET.get('restaurante_id'), 1)  
    p_id = _int_param(request.GET.get('produto_id'), 1)

    print(f"Recebendo restaurante_id={rest_id} e produto_id={p_id}")  # Debug
    
    restaurante = get_object_or_404(TbRestaurantes, id_restaurante=rest_id)
    prato = get_object_or_404(TbProdutos, id_produto=p_id, restaurante=restaurante)
    # O campo na model TbNutricao chama-se 'produto'
    nutricao = TbNutricao.objects.filter(produto=prato).first()

    return render(request, 'pages/catalogo/prato.html', {
        'restaurante': restaurante,
        'prato': prato,
        'nutricao': nutricao
    })

def buscar_prato_restaurante(request):
    query = request.GET.get('q', '').strip()
    if query:
        resultados = TbRestaurantes.objects.filter(
            Q(nome__icontains=query) | 
            Q(tbprodutos__nome_produto__icontains=query)
        ).distinct()
    else:
        resultados = TbRestaurantes.objects.all()

    categorias = TbRestaurantes.objects.values_list('categoria', flat=True).distinct()

    return render(request, 'pages/catalogo/restaurante.html', {
        'restaurantes': resultados,
        'categorias': sorted(filter(None, categorias)),
    })

# -------------------------------------------------------------------------
# PEDIDOS E RESERVAS
# -------------------------------------------------------------------------
def reserva_view(request):
    r_id = _int_param(request.GET.get('restaurante_id'), 1)
    restaurante = get_object_or_404(TbRestaurantes, id_restaurante=r_id)

    return render(request, 'pages/pedido/reserva.html', {
        'restaurante': restaurante,
        'horarios': ['19:00', '20:00', '21:00'],
        'datas': ['2026-03-07', '2026-03-08'],
    })

def reserva_pagamento(request):
    """Processa a visualização de pagamento da reserva."""
    if request.method == 'POST':
        r_id = _int_param(request.POST.get('restaurante_id'), 1)
        restaurante = get_object_or_404(TbRestaurantes, id_restaurante=r_id)

        return render(request, 'pages/pedido/reserva_pagamento.html', {
            'restaurante': restaurante,
            'horario': request.POST.get('horario'),
            'data': request.POST.get('data'),
            'pessoas': request.POST.get('pessoas'),
            'total': 'R$ 38,31', 
        })
    return redirect('reserva')

def pedido_view(request):
    r_id = _int_param(request.GET.get('restaurante_id'), 1)
    restaurante = get_object_or_404(TbRestaurantes, id_restaurante=r_id)
    item = TbProdutos.objects.filter(restaurante=restaurante).first()

    return render(request, 'pages/pedido/pedido.html', {
        'restaurante': restaurante,
        'item': item,
        'subtotal': item.preco if item else 0,
        'entrega': restaurante.taxa_entrega,
    })

# View 1 — só ADICIONA e redireciona
def adicionar_carrinho(request):
    r_id = _int_param(request.GET.get('restaurante_id'), 1)
    p_id = _int_param(request.GET.get('produto_id'), 1)

    restaurante = get_object_or_404(TbRestaurantes, id_restaurante=r_id)
    produto = get_object_or_404(TbProdutos, id_produto=p_id, restaurante=restaurante)

    carrinho = request.session.get('carrinho', {})

    if str(r_id) not in carrinho:
        carrinho[str(r_id)] = {'restaurante': restaurante.nome, 'itens': []}

    itens = carrinho[str(r_id)]['itens']
    existente = next((i for i in itens if i['id'] == p_id), None)

    if existente:
        if (existente['quantidade'] + 1) * Decimal(existente['preco']) > produto.preco * 10:
            messages.error(request, 'Limite de 10 unidades por produto.')
        else:
            existente['quantidade'] += 1
    else:
        itens.append({
            'id': produto.id_produto,
            'nome': produto.nome_produto,
            'preco': float(produto.preco),
            'quantidade': 1
        })

    request.session['carrinho'] = carrinho
    request.session.modified = True

    return redirect('carrinho')  
        
# View 2 — só EXIBE o carrinho, nunca modifica
def carrinho_view(request):
    carrinho = request.session.get('carrinho', {})
    subtotal = sum(
        i['preco'] * i['quantidade']
        for r in carrinho.values()
        for i in r['itens']
    )

    return render(request, 'pages/pedido/carrinho.html', {
        'carrinho': carrinho,
        'subtotal': f"R$ {subtotal:.2f}",
        'entrega': 'Grátis',
        'desconto': 'R$ 0,00',
        'total': f"R$ {subtotal:.2f}",
    })

def aumentar_item(request, r_id, produto_id):
    carrinho = request.session.get('carrinho', {})
    itens = carrinho.get(str(r_id), {}).get('itens', [])
    item = next((i for i in itens if i['id'] == produto_id), None)
    if item:
        if (item['quantidade'] + 1) * Decimal(item['preco']) > Decimal(item['preco']) * 10:
            return JsonResponse({'error': 'Limite de 10 unidades por produto.'}, status=400)
        item['quantidade'] += 1
    request.session['carrinho'] = carrinho
    request.session.modified = True
    return JsonResponse({'quantidade': item['quantidade'] if item else 0})


def diminuir_item(request, r_id, produto_id):
    carrinho = request.session.get('carrinho', {})
    itens = carrinho.get(str(r_id), {}).get('itens', [])
    item = next((i for i in itens if i['id'] == produto_id), None)
    if item:
        if item['quantidade'] > 1:
            item['quantidade'] -= 1
        else:
            itens.remove(item)  # remove se chegar a 0
    request.session['carrinho'] = carrinho
    request.session.modified = True
    return JsonResponse({'quantidade': item['quantidade'] if item in itens else 0})


def remover_restaurante(request, r_id):
    carrinho = request.session.get('carrinho', {})

    if str(r_id) in carrinho:
        del carrinho[str(r_id)]

    request.session['carrinho'] = carrinho
    request.session.modified = True

    return JsonResponse({"status":"ok"})



def finalizacao_view(request):
    return render(request, 'pages/pedido/finalizacao.html')

# -------------------------------------------------------------------------
# FEEDBACK
# -------------------------------------------------------------------------
def feedback_view(request):
    r_id = _int_param(request.GET.get('restaurante_id'), 1)
    restaurante = get_object_or_404(TbRestaurantes, id_restaurante=r_id)
    cliente = get_cliente_logado(request)

    if request.method == 'POST':
        nota = _int_param(request.POST.get('avaliacao'), 5)
        comentario = request.POST.get('comentario', '')

        if cliente:
            TbAvaliacoes.objects.create(
                cliente=cliente,
                restaurante=restaurante,
                nota=nota,
                comentario=comentario,
                data_avaliacao=timezone.now()
            )

        return render(request, 'pages/pedido/feedback_sucesso.html', {
            'restaurante': restaurante
        })
    
    return render(request, 'pages/pedido/feedback.html', {'restaurante': restaurante})

def feedback_sucesso_view(request):
    return render(request, 'pages/pedido/feedback_sucesso.html')