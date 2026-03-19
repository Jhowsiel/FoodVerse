from datetime import datetime, timedelta
from decimal import Decimal
import base64
import io
import logging
import random
import re
from urllib import request

import requests
from django.contrib import messages
from django.contrib.auth.hashers import check_password, make_password
from django.db import transaction
from django.db.models import Q
from django.http import JsonResponse
from django.shortcuts import get_object_or_404, redirect, render
from django.templatetags.static import static
from django.utils import timezone

from .models import (
    TbAvaliacoes,
    TbAvaliacoesProdutos,
    TbClientes,
    TbEstoque,
    TbNutricao,
    TbPagamentos,
    TbPedidos,
    TbPedidosProdutos,
    TbProdutos,
    TbReservas,
    TbRestaurantes,
    TbCupons,
    TbStatusPedido,
)

RESERVA_HORARIOS = ['12:00', '13:00', '19:00', '20:00', '21:00']
MESAS_DISPONIVEIS = [f'M{numero}' for numero in range(1, 13)]
RESERVA_TAXA = Decimal('20.00')
DIAS_SEMANA_PT = ['Segunda-feira', 'Terça-feira', 'Quarta-feira', 'Quinta-feira', 'Sexta-feira', 'Sábado', 'Domingo']


def _gerar_codigo_pix(valor: Decimal, pedido_ref: str) -> str:
    return f"00020126580014BR.GOV.BCB.PIX0136foodverse-demo-chave5204000053039865405{valor:.2f}5802BR5913FOODVERSE DEMO6009SAOPAULO62070503***6304{pedido_ref[:4].upper()}"


def _gerar_qr_pix_base64(payload: str) -> str:
    return static('img/pix_qr_padrao.svg')


def _avaliar_cupom(restaurante, subtotal: Decimal, cupom_codigo: str):
    cupom = (cupom_codigo or '').strip()
    if not cupom:
        return Decimal('0.00'), '', None

    cupom_global = TbCupons.objects.filter(codigo__iexact=cupom).first()
    if cupom_global:
        if cupom_global.validade and cupom_global.validade < timezone.localdate():
            return Decimal('0.00'), '', 'Cupom expirado.'
        percentual = cupom_global.desconto or Decimal('0.00')
        desconto = (subtotal * percentual / Decimal('100')).quantize(Decimal('0.01'))
        return desconto, cupom_global.codigo, None

    cupom_restaurante = (restaurante.cupom or '').strip()
    if cupom_restaurante and cupom_restaurante.lower() == cupom.lower():
        desconto = (subtotal * Decimal('0.10')).quantize(Decimal('0.01'))
        return desconto, cupom_restaurante, None

    return Decimal('0.00'), '', 'Cupom inválido para este restaurante.'


def _obter_status_por_id_ou_nome(id_status: int, nome_status: str):
    status = TbStatusPedido.objects.filter(id_status=id_status).first()
    if status:
        return status

    status = TbStatusPedido.objects.filter(nome_status__iexact=nome_status).first()
    if status:
        return status

    try:
        status, _ = TbStatusPedido.objects.get_or_create(
            id_status=id_status,
            defaults={'nome_status': nome_status},
        )
        return status
    except Exception:
        return TbStatusPedido.objects.order_by('id_status').first()


def _validar_dados_cartao(numero: str, validade: str, cvv: str, nome: str) -> bool:
    numero_limpo = re.sub(r'\D', '', numero or '')
    validade_ok = bool(re.match(r'^(0[1-9]|1[0-2])/\d{2}$', (validade or '').strip()))
    cvv_ok = bool(re.match(r'^\d{3,4}$', (cvv or '').strip()))
    nome_ok = len((nome or '').strip()) >= 3
    return len(numero_limpo) in (15, 16) and validade_ok and cvv_ok and nome_ok


def _criar_pedido_completo(cliente, restaurante, dados_venda, total_final, endereco, metodo_pagamento):
    status_pendente = _obter_status_por_id_ou_nome(1, 'Pendente')
    if not status_pendente:
        raise ValueError('Status de pedido não configurado')

    pedido = TbPedidos.objects.create(
        cliente=cliente,
        restaurante=restaurante,
        status=status_pendente,
        valor_total=total_final,
        data_pedido=timezone.now(),
        endereco_entrega=endereco,
    )

    metodo_pagamento_limpo = (metodo_pagamento or '').strip()
    if metodo_pagamento_limpo.lower() in {'pix', 'crédito', 'credito', 'débito', 'debito'}:
        pagamento_status = f'{metodo_pagamento_limpo} aprovado'
    else:
        pagamento_status = f'{metodo_pagamento_limpo} na entrega'

    TbPagamentos.objects.create(
        pedido=pedido,
        metodo_pagamento=pagamento_status,
        valor=total_final,
        data_pagamento=timezone.now(),
    )

    for item in dados_venda['itens']:
        produto_instancia = get_object_or_404(TbProdutos, id_produto=item['id'])
        TbPedidosProdutos.objects.create(
            pedido=pedido,
            produto=produto_instancia,
            quantidade=item['quantidade'],
        )

        estoque = TbEstoque.objects.filter(produto=produto_instancia).first()
        if estoque:
            if estoque.quantidade >= item['quantidade']:
                estoque.quantidade -= item['quantidade']
                estoque.save(update_fields=['quantidade'])
            else:
                raise ValueError(f'Estoque insuficiente: {produto_instancia.nome_produto}')

    return pedido


def _int_param(valor, padrao):
    try:
        return int(valor)
    except (TypeError, ValueError):
        return padrao


def get_cliente_logado(request):
    cliente_id = request.session.get('cliente_id')
    if cliente_id:
        return TbClientes.objects.filter(id_cliente=cliente_id).first()
    return None


def restaurantes_operacionais():
    return TbRestaurantes.objects.filter(ativo=True, aberto=True)


def _datas_reserva_validas():
    hoje = timezone.localdate()
    return [hoje + timedelta(days=offset) for offset in range(7)]


def _opcoes_data_reserva():
    opcoes = []
    for data in _datas_reserva_validas():
        opcoes.append({
            'valor': data.strftime('%Y-%m-%d'),
            'label': f"{data.strftime('%d/%m/%Y')} · {DIAS_SEMANA_PT[data.weekday()]}",
        })
    return opcoes


def home(request):
    endereco = None
    cep = None

    if request.method == 'POST':
        cep = request.POST.get('cep', '').strip()
        if cep:
            url = f'https://viacep.com.br/ws/{cep}/json/'
            try:
                response = requests.get(url, timeout=5)
                data = response.json()
                if 'erro' not in data:
                    endereco = (
                        f"{data.get('logradouro', '')}, {data.get('bairro', '')}, "
                        f"{data.get('localidade', '')} - {data.get('uf', '')}"
                    )
                    cliente = get_cliente_logado(request)
                    if cliente and endereco:
                        cliente.endereco = endereco
                        cliente.save(update_fields=['endereco'])
            except requests.RequestException:
                endereco = None

    restaurantes = restaurantes_operacionais()
    categorias = restaurantes.values_list('categoria', flat=True).distinct()

    return render(request, 'index.html', {
        'endereco': endereco,
        'cep': cep,
        'restaurantes': restaurantes,
        'categorias': sorted(filter(None, categorias)),
        'cliente_logado': get_cliente_logado(request),
    })

def salvar_endereco(request):
    if request.method == 'POST':
        endereco = request.POST.get('endereco', '').strip()
        cliente = get_cliente_logado(request)
        if not cliente:
            return redirect('login')
        cliente.endereco = endereco
        cliente.save(update_fields=['endereco'])
        return redirect('home')
    return redirect('home')

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
            # return redirect('home')
        else:
            messages.error(request, 'Usuário ou senha incorretos.')

    return render(request, 'pages/Autentificacao/login.html')


import random
from django.core.mail import send_mail

def recuperar_senha(request):
    template = 'pages/Autentificacao/recuperar_senha.html'

    if request.method == 'POST':
        etapa = request.POST.get('etapa')

        if etapa == 'email':
            email = request.POST.get('email')
            cliente = TbClientes.objects.filter(email=email).first()

            if not cliente:
                messages.error(request, 'E-mail não encontrado.')
                return render(request, template, {'etapa': 1})

            codigo = str(random.randint(100000, 999999))
            request.session['reset_email'] = email
            request.session['reset_codigo'] = codigo

            send_mail(
                subject='Código de recuperação — FoodVerse',
                message=f'Seu código de verificação é: {codigo}',
                from_email='noreply@foodverse.com',
                recipient_list=[email],
                fail_silently=False,
            )

            return render(request, template, {'etapa': 2, 'email_preenchido': email})

        elif etapa == 'codigo':
            codigo_digitado = request.POST.get('codigo_completo')
            codigo_correto  = request.session.get('reset_codigo')

            if codigo_digitado != codigo_correto:
                messages.error(request, 'Código incorreto. Tente novamente.')
                return render(request, template, {'etapa': 2})

            return render(request, template, {'etapa': 3})

        elif etapa == 'senha':
            nova_senha = request.POST.get('nova_senha')
            confirmar  = request.POST.get('confirmar_senha')
            email      = request.session.get('reset_email')

            if nova_senha != confirmar:
                messages.error(request, 'As senhas não coincidem.')
                return render(request, template, {'etapa': 3})

            cliente = TbClientes.objects.filter(email=email).first()
            if cliente:
                from django.contrib.auth.hashers import make_password
                cliente.senha = make_password(nova_senha)
                cliente.save()
                request.session.pop('reset_email', None)
                request.session.pop('reset_codigo', None)

            return render(request, template, {'etapa': 4})

    return render(request, template, {'etapa': 1})

def logout_view(request):
    if 'cliente_id' in request.session:
        del request.session['cliente_id']
    list(messages.get_messages(request))  # limpa todas as mensagens pendentes
    return redirect('login')

import re
from django.contrib import messages
from django.contrib.auth.hashers import make_password
from django.utils import timezone
from django.shortcuts import render, redirect

def cadastro_view(request):
    if get_cliente_logado(request):
        return redirect('home')

    if request.method == 'POST':
        nome             = request.POST.get('name', '').strip()
        username         = request.POST.get('username', '').strip()
        cpf              = re.sub(r'\D', '', request.POST.get('cpf', ''))
        telefone         = re.sub(r'\D', '', request.POST.get('telefone', ''))
        email            = request.POST.get('email', '').strip().lower()
        password         = request.POST.get('password', '')
        confirm_password = request.POST.get('confirm_password', '')

        erros = []

        # ── Campos obrigatórios ──────────────────────────────────────────
        if not all([nome, username, cpf, telefone, email, password, confirm_password]):
            erros.append("Todos os campos são obrigatórios.")

        # ── Nome ─────────────────────────────────────────────────────────
        if nome and len(nome) < 3:
            erros.append("O nome deve ter pelo menos 3 caracteres.")
        if nome and not re.match(r'^[A-Za-zÀ-ÿ\s]+$', nome):
            erros.append("O nome deve conter apenas letras.")

        # ── Username ─────────────────────────────────────────────────────
        if username and len(username) < 3:
            erros.append("O nome de usuário deve ter pelo menos 3 caracteres.")
        if username and not re.match(r'^[a-zA-Z0-9_]+$', username):
            erros.append("O usuário só pode conter letras, números e underscore (_).")
        if username and TbClientes.objects.filter(username=username).exists():
            erros.append("Este nome de usuário já está em uso.")

        # ── CPF ───────────────────────────────────────────────────────────
        if cpf and len(cpf) != 11:
            erros.append("CPF inválido. Informe os 11 dígitos.")
        elif cpf and not _cpf_valido(cpf):
            erros.append("CPF inválido (dígitos verificadores incorretos).")
        elif cpf and TbClientes.objects.filter(cpf=cpf).exists():
            erros.append("Este CPF já está cadastrado.")

        # ── Telefone ──────────────────────────────────────────────────────
        if telefone and len(telefone) not in (10, 11):
            erros.append("Telefone inválido. Informe DDD + número (10 ou 11 dígitos).")
        elif telefone and TbClientes.objects.filter(telefone=telefone).exists():
            erros.append("Este telefone já está cadastrado.")

        # ── E-mail ────────────────────────────────────────────────────────
        if email and not re.match(r'^[\w\.\+\-]+@[\w\-]+\.[a-z]{2,}$', email):
            erros.append("E-mail inválido.")
        elif email and TbClientes.objects.filter(email=email).exists():
            erros.append("Este e-mail já está cadastrado.")

        # ── Senha ─────────────────────────────────────────────────────────
        if password:
            if len(password) < 8:
                erros.append("A senha deve ter pelo menos 8 caracteres.")
            if not re.search(r'[A-Z]', password):
                erros.append("A senha deve conter pelo menos uma letra maiúscula.")
            if not re.search(r'[a-z]', password):
                erros.append("A senha deve conter pelo menos uma letra minúscula.")
            if not re.search(r'\d', password):
                erros.append("A senha deve conter pelo menos um número.")
            if not re.search(r'[!@#$%^&*(),.?\":{}|<>]', password):
                erros.append("A senha deve conter pelo menos um caractere especial.")
        if password and confirm_password and password != confirm_password:
            erros.append("As senhas não coincidem.")

        # ── Retorna erros ─────────────────────────────────────────────────
        if erros:
            for erro in erros:
                messages.error(request, erro)
            return render(request, 'pages/Autentificacao/cadastro.html')

        # ── Criação ───────────────────────────────────────────────────────
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
            
        except Exception:
            messages.error(request, 'Ocorreu um erro ao criar a conta.')

    return render(request, 'pages/Autentificacao/cadastro.html')


# ── Validação de CPF (algoritmo oficial) ──────────────────────────────────────
def _cpf_valido(cpf: str) -> bool:
    if len(set(cpf)) == 1:         
        return False
    for i in range(9, 11):
        soma = sum(int(cpf[j]) * (i + 1 - j) for j in range(i))
        digito = (soma * 10 % 11) % 10
        if digito != int(cpf[i]):
            return False
    return True


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
        username = request.POST.get('username', '').strip()
        email = request.POST.get('email', '').strip().lower()
        telefone = request.POST.get('telefone', '').strip()
        endereco = request.POST.get('endereco', '').strip()

        outros = TbClientes.objects.exclude(pk=cliente.pk)
        erros = []
        if username and outros.filter(username=username).exists():
            erros.append('Este nome de usuário já está cadastrado.')
        if email and outros.filter(email=email).exists():
            erros.append('Este e-mail já está cadastrado.')
        if telefone and outros.filter(telefone=telefone).exists():
            erros.append('Este telefone já está cadastrado.')

        if erros:
            return render(request, 'pages/perfil/editar/editar.html', {
                'perfil': cliente,
                'erros': erros,
                'form_data': {
                    'username': username,
                    'email': email,
                    'telefone': telefone,
                    'endereco': endereco,
                },
            })

        cliente.username = username
        cliente.email = email
        cliente.telefone = telefone
        cliente.endereco = endereco
        cliente.save()
        messages.success(request, 'Perfil atualizado!')
        return redirect('perfil')

    return render(request, 'pages/perfil/editar/editar.html', {'perfil': cliente})


def restaurante_view(request):
    categoria = request.GET.get('categoria')
    restaurantes = restaurantes_operacionais()
    if categoria:
        restaurantes = restaurantes.filter(categoria=categoria)

    categorias = restaurantes_operacionais().values_list('categoria', flat=True).distinct()
    return render(request, 'pages/catalogo/restaurante.html', {
        'restaurantes': restaurantes,
        'categorias': sorted(filter(None, categorias)),
        'categoria_ativa': categoria,
        'restaurante_destaque': restaurantes.first(),
    })


def restaurante_detalhe_view(request, id):
    restaurante = get_object_or_404(TbRestaurantes, id_restaurante=id, ativo=True)
    pratos = TbProdutos.objects.filter(restaurante=restaurante, disponivel=True).exclude(tipo_produto='INSUMO')

    categoria_ativa = request.GET.get('categoria', '')
    if categoria_ativa:
        pratos = pratos.filter(categoria=categoria_ativa)

    categorias = (
        TbProdutos.objects.filter(restaurante=restaurante, disponivel=True, categoria__isnull=False)
        .exclude(categoria='')
        .exclude(tipo_produto='INSUMO')
        .values_list('categoria', flat=True)
        .distinct()
        .order_by('categoria')
    )

    sugestoes = TbProdutos.objects.filter(
        restaurante=restaurante,
        disponivel=True,
    ).filter(
        Q(categoria__icontains='bebida') | Q(categoria__icontains='sobremesa')
    ).exclude(tipo_produto='INSUMO')[:6]

    avaliacoes = TbAvaliacoes.objects.filter(
        restaurante=restaurante
    ).order_by('-data_avaliacao')

    return render(request, 'pages/catalogo/restaurante_detalhe.html', {
        'restaurante': restaurante,
        'pratos': pratos,
        'categorias': list(categorias),
        'categoria_ativa': categoria_ativa,
        'sugestoes': sugestoes,
        'avaliacoes': avaliacoes,
    })


def prato_view(request):
    rest_id = _int_param(request.GET.get('restaurante_id'), 1)
    p_id = _int_param(request.GET.get('produto_id'), 1)

    restaurante = get_object_or_404(TbRestaurantes, id_restaurante=rest_id, ativo=True)
    prato = get_object_or_404(TbProdutos, id_produto=p_id, restaurante=restaurante)
    nutricao = TbNutricao.objects.filter(produto=prato).first()
    cliente = get_cliente_logado(request)
    restricoes_list = []
    if prato.restricoes:
        restricoes_list = [t.strip() for t in prato.restricoes.split(',') if t.strip()]

    acompanhamentos = TbProdutos.objects.filter(
        restaurante=restaurante,
        disponivel=True,
    ).filter(
        Q(categoria__icontains='bebida') | Q(categoria__icontains='sobremesa')
    ).exclude(id_produto=prato.id_produto).exclude(tipo_produto='INSUMO')[:6]

    ja_pediu = False
    ja_avaliou = False

    if cliente:
        ja_pediu = TbPedidos.objects.filter(
            cliente=cliente,
            restaurante=restaurante,
            status__nome_status__iexact='Entregue'
        ).exists()
        ja_avaliou = TbAvaliacoesProdutos.objects.filter(
            cliente=cliente,
            produto=prato
        ).exists()

        # POST — salvar avaliação
    if request.method == 'POST':
        if cliente and ja_pediu and not ja_avaliou:
            nota = _int_param(request.POST.get('avaliacao'), 5)
            comentario = request.POST.get('comentario', '')
            TbAvaliacoesProdutos.objects.create(
                cliente=cliente,
                produto=prato,
                nota=nota,
                comentario=comentario,
                data_avaliacao=timezone.now(),
            )
        return redirect(f"{request.path}?restaurante_id={rest_id}&produto_id={p_id}")

    avaliacoes_produto = TbAvaliacoesProdutos.objects.filter(
        produto=prato
    ).order_by('-data_avaliacao')

    return render(request, 'pages/catalogo/prato.html', {
        'restaurante': restaurante,
        'prato': prato,
        'nutricao': nutricao,
        'restricoes_list': restricoes_list,
        'acompanhamentos': acompanhamentos,
        'cliente': cliente,
        'ja_pediu': ja_pediu,
        'ja_avaliou': ja_avaliou,
        'avaliacoes_produto': avaliacoes_produto,
    })


def buscar_prato_restaurante(request):
    query = request.GET.get('q', '').strip()
    if query:
        resultados = restaurantes_operacionais().filter(
            Q(nome__icontains=query) | Q(tbprodutos__nome_produto__icontains=query)
        ).distinct()
    else:
        resultados = TbRestaurantes.objects.none()

    categorias = restaurantes_operacionais().values_list('categoria', flat=True).distinct()
    return render(request, 'pages/catalogo/restaurante.html', {
        'restaurantes': resultados,
        'categorias': sorted(filter(None, categorias)),
        'categoria_ativa': None,
        'query': query,
    })


def reserva_view(request):
    r_id = _int_param(request.GET.get('restaurante_id'), 1)

    client = get_cliente_logado(request)
    if not client:
        messages.error(request, 'Faça login para fazer uma reserva.')
        return redirect('login')

    restaurante = get_object_or_404(TbRestaurantes, id_restaurante=r_id, ativo=True)
    if not restaurante.aberto:
        messages.error(request, 'Este restaurante está fechado no momento.')
        return redirect('restaurante_detalhe', id=r_id)

    return render(request, 'pages/pedido/reserva.html', {
        'restaurante': restaurante,
        'datas': _opcoes_data_reserva(),
        'taxa_reserva': RESERVA_TAXA,
        'horarios': RESERVA_HORARIOS,
    })


def reserva_pagamento(request):
    if request.method != 'POST':
        return redirect('reserva')

    client = get_cliente_logado(request)
    if not client:
        messages.error(request, 'Faça login para concluir uma reserva.')
        return redirect('login')

    r_id = _int_param(request.POST.get('restaurante_id'), 1)
    restaurante = get_object_or_404(TbRestaurantes, id_restaurante=r_id, ativo=True)

    if not restaurante.aberto:
        messages.error(request, 'Este restaurante está fechado no momento.')
        return redirect('restaurante_detalhe', id=r_id)

    data_str = request.POST.get('data', '')
    horario_str = request.POST.get('horario', '')
    pessoas = _int_param(request.POST.get('pessoas'), 2)
    metodo_pagamento = request.POST.get('metodo_pagamento', 'Pix').title()

    datas_validas = [d.strftime('%Y-%m-%d') for d in _datas_reserva_validas()]
    if data_str not in datas_validas or horario_str not in RESERVA_HORARIOS:
        messages.error(request, 'Selecione uma data e horário válidos para os próximos 7 dias.')
        return redirect(f"{redirect('reserva').url}?restaurante_id={r_id}")

    try:
        reserva_data = timezone.make_aware(datetime.strptime(f'{data_str} {horario_str}', '%Y-%m-%d %H:%M'))
    except ValueError:
        messages.error(request, 'Data ou horário inválidos.')
        return redirect(f"{redirect('reserva').url}?restaurante_id={r_id}")

    mesas_ocupadas = set(
        TbReservas.objects.filter(restaurante=restaurante, data_reserva=reserva_data)
        .values_list('mesa', flat=True)
    )
    mesas_livres = [mesa for mesa in MESAS_DISPONIVEIS if mesa not in mesas_ocupadas]
    if not mesas_livres:
        messages.error(request, 'Não há mesas disponíveis para este horário. Tente outro horário.')
        return redirect(f"{redirect('reserva').url}?restaurante_id={r_id}")

    mesa_sorteada = random.choice(mesas_livres)
    payload_pix = _gerar_codigo_pix(RESERVA_TAXA, f"RV{client.id_cliente}{r_id}")

    request.session['reserva_checkout'] = {
        'restaurante_id': restaurante.id_restaurante,
        'data': data_str,
        'horario': horario_str,
        'pessoas': pessoas,
        'metodo_pagamento': metodo_pagamento,
        'mesa': mesa_sorteada,
        'payload_pix': payload_pix,
    }
    request.session.modified = True

    return render(request, 'pages/pedido/reserva_pagamento.html', {
        'restaurante': restaurante,
        'horario': horario_str,
        'data': data_str,
        'pessoas': pessoas,
        'metodo_pagamento': metodo_pagamento,
        'mesa': mesa_sorteada,
        'total': RESERVA_TAXA,
        'pix_payload': payload_pix,
        'pix_qr_code': _gerar_qr_pix_base64(payload_pix),
        'pix_timeout': 18,
    })


def reserva_confirmar_pagamento(request):
    if request.method != 'POST':
        return redirect('pedido')

    cliente = get_cliente_logado(request)
    if not cliente:
        return redirect('login')

    checkout = request.session.get('reserva_checkout') or {}
    if not checkout:
        messages.error(request, 'Dados da reserva expiraram. Refaça a reserva.')
        return redirect('restaurantes')

    restaurante = get_object_or_404(TbRestaurantes, id_restaurante=checkout.get('restaurante_id'), ativo=True)
    metodo = (request.POST.get('metodo_pagamento') or checkout.get('metodo_pagamento') or 'Pix').title()

    if metodo == 'Pix' and request.POST.get('pix_confirmado') != '1':
        messages.error(request, 'Aguardando confirmação do Pix.')
        return redirect(f"{redirect('reserva').url}?restaurante_id={restaurante.id_restaurante}")

    if metodo in {'Crédito', 'Debito', 'Débito'}:
        if request.POST.get('cartao_confirmado') != '1':
            messages.error(request, 'Pagamento com cartão não confirmado.')
            return redirect(f"{redirect('reserva').url}?restaurante_id={restaurante.id_restaurante}")
        if not _validar_dados_cartao(
            request.POST.get('cartao_numero'),
            request.POST.get('cartao_validade'),
            request.POST.get('cartao_cvv'),
            request.POST.get('cartao_nome'),
        ):
            messages.error(request, 'Dados do cartão inválidos.')
            return redirect(f"{redirect('reserva').url}?restaurante_id={restaurante.id_restaurante}")

    try:
        reserva_data = timezone.make_aware(datetime.strptime(f"{checkout['data']} {checkout['horario']}", '%Y-%m-%d %H:%M'))
    except ValueError:
        messages.error(request, 'Data da reserva inválida.')
        return redirect('restaurantes')

    with transaction.atomic():
        TbReservas.objects.create(
            cliente=cliente,
            restaurante=restaurante,
            data_reserva=reserva_data,
            numero_pessoas=checkout['pessoas'],
            mesa=checkout['mesa'],
        )

    request.session.pop('reserva_checkout', None)
    request.session.modified = True
    messages.success(request, 'Reserva confirmada com sucesso.')
    return redirect('pedido')


def pedido_view(request):
    cliente = get_cliente_logado(request)
    if not cliente:
        return redirect('login')

    pedidos = (
        TbPedidos.objects.filter(cliente=cliente)
        .select_related('status', 'restaurante')
        .order_by('-data_pedido')
        .prefetch_related('tbpedidosprodutos_set__produto', 'tbpagamentos_set')
    )

    pedidos_pendentes = pedidos.filter(status_id=1)
    pedidos_andamento = pedidos.filter(status_id__in=[2, 3])
    pedidos_concluidos = pedidos.filter(status_id=4)
    pedidos_cancelados = pedidos.filter(status_id=5)

    reservas = (
        TbReservas.objects.filter(cliente=cliente)
        .select_related('restaurante')
        .order_by('-data_reserva')
    )

    r_id = request.GET.get('restaurante_id')
    restaurante = None
    if r_id:
        restaurante = get_object_or_404(TbRestaurantes, id_restaurante=r_id, ativo=True, aberto=True)

    pedido_para_avaliar = TbPedidos.objects.filter(
    cliente=cliente,
    status__nome_status__iexact='Entregue'
    ).exclude(
    restaurante__in=TbAvaliacoes.objects.filter(cliente=cliente).values('restaurante')
    ).order_by('-data_pedido').first()

    return render(request, 'pages/pedido/pedido.html', {
        'pedidos': pedidos,
        'pedidos_pendentes': pedidos_pendentes,
        'pedidos_andamento': pedidos_andamento,
        'pedidos_concluidos': pedidos_concluidos,
        'pedidos_cancelados': pedidos_cancelados,
        'reservas': reservas,
        'restaurante_atual': restaurante,
        'pedido_para_avaliar': pedido_para_avaliar
    })


def adicionar_carrinho(request):
    r_id = _int_param(request.GET.get('restaurante_id'), 1)
    p_id = _int_param(request.GET.get('produto_id'), 1)

    carrinho = request.session.get('carrinho', {})
    if carrinho and str(r_id) not in carrinho:
        messages.error(request, 'Você já tem itens de outro restaurante. Finalize ou limpe o carrinho primeiro.')
        return redirect('carrinho')

    restaurante = get_object_or_404(TbRestaurantes, id_restaurante=r_id, ativo=True, aberto=True)
    produto = get_object_or_404(TbProdutos, id_produto=p_id, restaurante=restaurante)

    if str(r_id) not in carrinho:
        carrinho[str(r_id)] = {'restaurante': restaurante.nome, 'itens': []}

    itens = carrinho[str(r_id)]['itens']
    existente = next((i for i in itens if i['id'] == p_id), None)

    if existente:
        if existente['quantidade'] + 1 > 10:
            return JsonResponse({'quantidade': existente['quantidade'], 'erro': 'Limite de 10 unidades'})
        existente['quantidade'] += 1
    else:
        itens.append({
            'id': produto.id_produto,
            'nome': produto.nome_produto,
            'preco': float(produto.preco),
            'imagem': produto.imagem or '',
            'quantidade': 1,
        })

    request.session['carrinho'] = carrinho
    request.session.modified = True
    return redirect('carrinho')


def carrinho_view(request):
    client = get_cliente_logado(request)
    if not client:
        messages.error(request, 'Faça login para fazer um pedido.')
        return redirect('login')

    carrinho = request.session.get('carrinho', {})
    subtotal = sum(i['preco'] * i['quantidade'] for r in carrinho.values() for i in r['itens'])
    total_itens = sum(i['quantidade'] for r in carrinho.values() for i in r['itens'])

    return render(request, 'pages/pedido/carrinho.html', {
        'carrinho': carrinho,
        'subtotal_num': subtotal,
        'subtotal': f'R$ {subtotal:.2f}',
        'entrega': 'Grátis',
        'desconto': 'R$ 0,00',
        'total': f'R$ {subtotal:.2f}',
        'total_itens': total_itens,
    })


def aumentar_item(request, r_id, produto_id):
    carrinho = request.session.get('carrinho', {})
    itens = carrinho.get(str(r_id), {}).get('itens', [])
    item = next((i for i in itens if i['id'] == produto_id), None)
    if item:
        nova_quantidade = item['quantidade'] + 1
        if nova_quantidade > 10:
            return JsonResponse({
                'status': 'erro',
                'mensagem': 'Limite máximo de 10 unidades por produto atingido.',
                'quantidade_atual': item['quantidade'],
            }, status=400)
        item['quantidade'] = nova_quantidade

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
            itens.remove(item)

    request.session['carrinho'] = carrinho
    request.session.modified = True
    return JsonResponse({'quantidade': item['quantidade'] if item in itens else 0})


def remover_restaurante(request, r_id):
    carrinho = request.session.get('carrinho', {})
    if str(r_id) in carrinho:
        del carrinho[str(r_id)]

    request.session['carrinho'] = carrinho
    request.session.modified = True
    return JsonResponse({'status': 'ok'})


def finalizacao_view(request):
    r_id = request.GET.get('restaurante_id') or request.POST.get('restaurante_id')
    carrinho = request.session.get('carrinho', {})

    if not r_id or r_id not in carrinho:
        return redirect('carrinho')

    cliente = get_cliente_logado(request)
    if not cliente:
        return redirect('login')

    restaurante = get_object_or_404(TbRestaurantes, id_restaurante=r_id, ativo=True, aberto=True)
    dados_venda = carrinho[r_id]
    subtotal = sum(Decimal(str(i['preco'])) * i['quantidade'] for i in dados_venda['itens'])
    entrega = restaurante.taxa_entrega if restaurante.taxa_entrega else Decimal('0.00')

    cupom_fonte = request.POST if request.method == 'POST' else request.GET
    cupom_codigo = (cupom_fonte.get('cupom_codigo') or '').strip()
    desconto, cupom_aplicado, cupom_erro = _avaliar_cupom(restaurante, subtotal, cupom_codigo)
    total_final = max(subtotal + entrega - desconto, Decimal('0.00'))

    payload_pix = _gerar_codigo_pix(total_final, f"PD{cliente.id_cliente}{r_id}")
    contexto = {
        'carrinho': dados_venda,
        'restaurante': restaurante,
        'cliente': cliente,
        'subtotal': subtotal,
        'entrega': entrega,
        'desconto': desconto,
        'total': total_final,
        'cupom_codigo': cupom_codigo,
        'cupom_aplicado': cupom_aplicado,
        'cupom_erro': cupom_erro,
        'pix_timeout': 18,
        'pix_payload': payload_pix,
        'pix_qr_code': _gerar_qr_pix_base64(payload_pix),
    }

    if request.method == 'GET':
        return render(request, 'pages/pedido/finalizacao.html', contexto)

    if cupom_erro and cupom_codigo:
        return render(request, 'pages/pedido/finalizacao.html', contexto)

    endereco = (request.POST.get('endereco') or '').strip()
    metodo_pagamento = (request.POST.get('pagamento') or 'Pix').strip().title()

    if not endereco:
        messages.error(request, 'Informe o endereço de entrega.')
        return render(request, 'pages/pedido/finalizacao.html', contexto)

    if metodo_pagamento == 'Pix' and request.POST.get('pix_confirmado') != '1':
        messages.error(request, 'Aguardando confirmação do Pix.')
        return render(request, 'pages/pedido/finalizacao.html', contexto)

    if metodo_pagamento in {'Crédito', 'Debito', 'Débito'}:
        if request.POST.get('cartao_confirmado') != '1':
            messages.error(request, 'Pagamento com cartão não confirmado.')
            return render(request, 'pages/pedido/finalizacao.html', contexto)
        if not _validar_dados_cartao(
            request.POST.get('cartao_numero'),
            request.POST.get('cartao_validade'),
            request.POST.get('cartao_cvv'),
            request.POST.get('cartao_nome'),
        ):
            messages.error(request, 'Dados do cartão inválidos.')
            return render(request, 'pages/pedido/finalizacao.html', contexto)

    try:
        with transaction.atomic():
            _criar_pedido_completo(
                cliente=cliente,
                restaurante=restaurante,
                dados_venda=dados_venda,
                total_final=total_final,
                endereco=endereco,
                metodo_pagamento=metodo_pagamento,
            )
            del request.session['carrinho'][r_id]
            request.session.modified = True
    except Exception as exc:
        logging.getLogger(__name__).error('Erro ao finalizar pedido: %s', exc)
        messages.error(request, 'Não foi possível concluir o pedido. Tente novamente.')
        return redirect('carrinho')

    messages.success(request, 'Pedido confirmado com sucesso!')
    return redirect('pedido')


def cancelar_pedido_view(request, pedido_id):
    if request.method != 'POST':
        return redirect('pedido')

    cliente = get_cliente_logado(request)
    if not cliente:
        return redirect('login')

    pedido = get_object_or_404(TbPedidos, id_pedido=pedido_id, cliente=cliente)
    if pedido.status_id != 1:
        messages.error(request, 'Este pedido não pode mais ser cancelado.')
        return redirect('pedido')

    status_cancelado = _obter_status_por_id_ou_nome(5, 'Cancelado')
    if not status_cancelado:
        messages.error(request, 'Status de cancelamento não configurado.')
        return redirect('pedido')
    pedido.status = status_cancelado
    pedido.save(update_fields=['status'])
    messages.success(request, f'Pedido #{pedido.id_pedido} cancelado com sucesso.')
    return redirect('pedido')


def cancelar_reserva_view(request, reserva_id):
    if request.method != 'POST':
        return redirect('pedido')

    cliente = get_cliente_logado(request)
    if not cliente:
        return redirect('login')

    reserva = get_object_or_404(TbReservas, id_reserva=reserva_id, cliente=cliente)
    reserva.delete()
    messages.success(request, f'Reserva #R{reserva_id} cancelada com sucesso.')
    return redirect('pedido')


def feedback_view(request):
    r_id = _int_param(request.GET.get('restaurante_id'), 1)
    restaurante = get_object_or_404(TbRestaurantes, id_restaurante=r_id, ativo=True)
    cliente = get_cliente_logado(request)

    ja_pediu = False
    ja_avaliou = False
    if cliente:
        ja_pediu = TbPedidos.objects.filter(
            cliente=cliente,
            restaurante=restaurante,
            status__nome_status__iexact='Entregue'
        ).exists()
        ja_avaliou = TbAvaliacoes.objects.filter(
            cliente=cliente,
            restaurante=restaurante
        ).exists()

    if request.method == 'POST':
        if not cliente or not ja_pediu or ja_avaliou:
            return redirect(f"{request.path}?restaurante_id={r_id}")

        nota = _int_param(request.POST.get('avaliacao'), 5)
        comentario = request.POST.get('comentario', '')

        TbAvaliacoes.objects.create(
            cliente=cliente,
            restaurante=restaurante,
            nota=nota,
            comentario=comentario,
            data_avaliacao=timezone.now(),
        )

        return render(request, 'pages/pedido/feedback_sucesso.html', {'restaurante': restaurante})

    return render(request, 'pages/pedido/feedback.html', {
        'restaurante': restaurante,
        'cliente': cliente,
        'ja_pediu': ja_pediu,
        'ja_avaliou': ja_avaliou,
    })


def feedback_sucesso_view(request):
    return render(request, 'pages/pedido/feedback_sucesso.html')

def avaliar_pedido_view(request):
    if request.method != 'POST':
        return redirect('pedido')

    cliente = get_cliente_logado(request)
    if not cliente:
        return redirect('pedido')

    pedido_id = _int_param(request.POST.get('pedido_id'), 0)
    pedido = get_object_or_404(TbPedidos, id_pedido=pedido_id, cliente=cliente)

    # Salva avaliação do restaurante
    nota_restaurante = _int_param(request.POST.get('nota_restaurante'), 5)
    comentario = request.POST.get('comentario_restaurante', '')

    ja_avaliou_rest = TbAvaliacoes.objects.filter(
        cliente=cliente,
        restaurante=pedido.restaurante
    ).exists()

    if not ja_avaliou_rest:
        TbAvaliacoes.objects.create(
            cliente=cliente,
            restaurante=pedido.restaurante,
            nota=nota_restaurante,
            comentario=comentario,
            data_avaliacao=timezone.now(),
        )

    # Salva avaliação de cada prato
    for item in pedido.tbpedidosprodutos_set.all():
        nota_key = f'nota_produto_{item.produto.id_produto}'
        comentario_key = f'comentario_produto_{item.produto.id_produto}'
        nota = _int_param(request.POST.get(nota_key), 0)
        comentario_prato = request.POST.get(comentario_key, '')
        if nota > 0:
            ja_avaliou_prato = TbAvaliacoesProdutos.objects.filter(
                cliente=cliente,
                produto=item.produto
            ).exists()
            if not ja_avaliou_prato:
                TbAvaliacoesProdutos.objects.create(
                    cliente=cliente,
                    produto=item.produto,
                    nota=nota,
                    comentario=comentario_prato,
                    data_avaliacao=timezone.now(),
                )

    messages.success(request, 'Avaliação enviada com sucesso! Obrigado pelo feedback.')
    return redirect('pedido')