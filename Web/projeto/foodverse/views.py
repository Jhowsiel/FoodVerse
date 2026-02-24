from django.shortcuts import render, redirect
from django.contrib.auth import authenticate, login, logout
from django.contrib.auth.models import User
from django.contrib import messages
import requests
from .models import Perfil
import re


RESTAURANTES = [
    {
        "id": 1,
        "nome": "Sabor da Casa",
        "categoria": "Brasileira",
        "descricao": "Pratos executivos, opções saudáveis e comida caseira.",
        "avaliacao": 4.8,
        "tempo": "25-35 min",
        "taxa": "R$ 6,90",
        "cupom": "FOOD10",
        "imagem": "img/cardapio/brasileira.avif",
        "pratos": [
            {
                "id": 101,
                "nome": "Frango grelhado com legumes",
                "descricao": "Alta proteína · sem fritura",
                "preco": "R$ 34,90",
                "nutri": {"kcal": 430, "proteina": "38g", "carbo": "25g", "gordura": "16g"},
            },
            {
                "id": 102,
                "nome": "Risoto de cogumelos",
                "descricao": "Vegetariano · cremoso",
                "preco": "R$ 37,90",
                "nutri": {"kcal": 470, "proteina": "13g", "carbo": "58g", "gordura": "18g"},
            },
        ],
    },
    {
        "id": 2,
        "nome": "Fritello",
        "categoria": "Fast Food",
        "descricao": "Hambúrguer artesanal, combos e porções para compartilhar.",
        "avaliacao": 4.6,
        "tempo": "20-30 min",
        "taxa": "Grátis",
        "cupom": "FRITA5",
        "imagem": "img/cardapio/fastfood.jpg",
        "pratos": [
            {
                "id": 201,
                "nome": "Burger duplo especial",
                "descricao": "Pão brioche · queijo cheddar · molho da casa",
                "preco": "R$ 29,90",
                "nutri": {"kcal": 690, "proteina": "34g", "carbo": "52g", "gordura": "39g"},
            }
        ],
    },
    {
        "id": 3,
        "nome": "Yami Oriental",
        "categoria": "Asiática",
        "descricao": "Sushi, poke e pratos orientais leves.",
        "avaliacao": 4.9,
        "tempo": "30-45 min",
        "taxa": "R$ 8,90",
        "cupom": "YAMI15",
        "imagem": "img/cardapio/japonesa.jpg",
        "pratos": [
            {
                "id": 301,
                "nome": "Poke de salmão",
                "descricao": "Arroz gohan · salmão fresco · mix de vegetais",
                "preco": "R$ 42,90",
                "nutri": {"kcal": 520, "proteina": "30g", "carbo": "48g", "gordura": "20g"},
            }
        ],
    },
]


def _restaurante_por_id(restaurante_id):
    return next((r for r in RESTAURANTES if r["id"] == restaurante_id), RESTAURANTES[0])


def _prato_por_id(restaurante, prato_id):
    return next((p for p in restaurante["pratos"] if p["id"] == prato_id), restaurante["pratos"][0])

def _int_param(valor, padrao):
    try:
        return int(valor)
    except (TypeError, ValueError):
        return padrao


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
                else:
                    endereco = None
            except requests.RequestException:
                endereco = None

    context = {
        'endereco': endereco,
        'cep': cep,
        'restaurantes': RESTAURANTES,
        'categorias': sorted({r['categoria'] for r in RESTAURANTES}),
    }
    return render(request, 'index.html', context)


def login_view(request):
    if request.user.is_authenticated:
        return redirect('home')

    if request.method == 'POST':
        username = request.POST.get('username')
        password = request.POST.get('password')
        remember_me = request.POST.get('remember_me')

        user = authenticate(request, username=username, password=password)

        if user is not None:
            login(request, user)
            request.session.set_expiry(1209600 if remember_me else 0)
            messages.success(request, 'Login realizado com sucesso!')
            return render(request, 'pages/Autentificacao/login.html')

        messages.error(request, 'Usuário ou senha incorretos.')

    return render(request, 'pages/Autentificacao/login.html')


def cadastro_view(request):
    if request.user.is_authenticated:
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

        if not nome or not username or not cpf or not telefone or not email or not password or not confirm_password:
            erros.append("• Todos os campos são obrigatórios.")

        if nome and len(nome) < 2:
            erros.append("• O nome completo deve ter pelo menos 2 caracteres.")

        if username and len(username) < 4:
            erros.append("• O nome de usuário deve ter pelo menos 4 caracteres.")

        if email and not re.match(r'^[\w\.-]+@[\w\.-]+\.\w+$', email):
            erros.append("• E-mail inválido.")

        if cpf and not re.match(r'^\d{3}\.\d{3}\.\d{3}-\d{2}$', cpf):
            erros.append("• CPF deve estar no formato XXX.XXX.XXX-XX.")

        if telefone and not re.match(r'^\(\d{2}\) \d{4,5}-\d{4}$', telefone):
            erros.append("• Telefone deve estar no formato (XX) XXXXX-XXXX ou (XX) XXXX-XXXX.")

        if password != confirm_password:
            erros.append("• As senhas não conferem.")

        if password and len(password) < 8:
            erros.append("• Deve ter pelo menos 8 caracteres.")

        if password:
            if not re.search(r"[a-z]", password):
                erros.append("• Deve conter pelo menos uma letra minúscula.")
            if not re.search(r"[A-Z]", password):
                erros.append("• Deve conter pelo menos uma letra maiúscula.")
            if not re.search(r"[0-9]", password):
                erros.append("• Deve conter pelo menos um número.")
            if not re.search(r"[^A-Za-z0-9]", password):
                erros.append("• Deve conter pelo menos um símbolo (@, #, !, etc).")
            if password.isdigit():
                erros.append("• A senha não pode ser totalmente numérica.")

        if User.objects.filter(username=username).exists():
            erros.append("• Este nome de usuário já está em uso.")
        if Perfil.objects.filter(cpf=cpf).exists():
            erros.append("• Este CPF já está cadastrado.")
        if Perfil.objects.filter(telefone=telefone).exists():
            erros.append("• Este telefone já está cadastrado.")
        if User.objects.filter(email=email).exists():
            erros.append("• Este e-mail já está cadastrado.")

        if erros:
            for erro in erros:
                messages.error(request, erro)
            return render(request, 'pages/Autentificacao/cadastro.html')

        try:
            user = User.objects.create_user(username=username, email=email, password=password)
            Perfil.objects.create(user=user, cpf=cpf, telefone=telefone)
            user.save()
            messages.success(request, 'Cadastro realizado com sucesso! Faça login.')
            return render(request, 'pages/Autentificacao/cadastro.html')
        except Exception:
            messages.error(request, 'Ocorreu um erro ao criar a conta. Tente novamente.')
            return render(request, 'pages/Autentificacao/cadastro.html')

    return render(request, 'pages/Autentificacao/cadastro.html')


def restaurante_view(request):
    restaurante_id = _int_param(request.GET.get('id'), 1)
    categoria = request.GET.get('categoria')

    restaurantes_filtrados = RESTAURANTES
    if categoria:
        restaurantes_filtrados = [r for r in RESTAURANTES if r['categoria'] == categoria]

    restaurante = _restaurante_por_id(restaurante_id)
    return render(request, 'pages/catalogo/restaurante.html', {
        'restaurantes': restaurantes_filtrados,
        'restaurante_destaque': restaurante,
        'categorias': sorted({r['categoria'] for r in RESTAURANTES}),
        'categoria_ativa': categoria,
    })


def prato_view(request):
    restaurante = _restaurante_por_id(_int_param(request.GET.get('restaurante'), 1))
    prato = _prato_por_id(restaurante, _int_param(request.GET.get('id'), restaurante['pratos'][0]['id']))
    return render(request, 'pages/catalogo/prato.html', {
        'restaurante': restaurante,
        'prato': prato,
    })


def pedido_view(request):
    restaurante = _restaurante_por_id(1)
    item = restaurante['pratos'][0]
    return render(request, 'pages/pedido/pedido.html', {
        'restaurante': restaurante,
        'item': item,
        'subtotal': 'R$ 34,90',
        'entrega': 'R$ 6,90',
        'desconto': '-R$ 3,49',
        'total': 'R$ 38,31',
    })


def finalizacao_view(request):
    return render(request, 'pages/pedido/finalizacao.html', {
        'subtotal': 'R$ 34,90',
        'entrega': 'R$ 6,90',
        'desconto': '-R$ 3,49',
        'total': 'R$ 38,31',
    })

def filtrar_restaurantes(query):
    resultados = []

    for r in RESTAURANTES:
        if query in r["nome"].lower():
            resultados.append(r)
            continue

        for prato in r.get("pratos", []):
            if query in prato["nome"].lower():
                resultados.append(r)
                break

    return resultados

def buscar_prato_restaurante(request):
    query = request.GET.get('q', '').strip().lower()

    resultados = filtrar_restaurantes(query)

    return render(request, 'pages/catalogo/restaurante.html', {
        'restaurantes': resultados,
        'categorias': sorted({r['categoria'] for r in RESTAURANTES}),
        'categoria_ativa': None,
    })

def logout_view(request):
    logout(request)
    return redirect('login')
