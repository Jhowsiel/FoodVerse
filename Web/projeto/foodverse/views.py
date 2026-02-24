from django.shortcuts import render, redirect
from django.contrib.auth import authenticate, login, logout
from django.contrib.auth.models import User
from django.contrib import messages
import requests
from .models import Perfil
import re


def home(request):
    endereco = None
    cep = None

    if request.method == 'POST':
        cep = request.POST.get('cep', '').strip()
        if cep:
            url = f"https://viacep.com.br/ws/{cep}/json/"
            try:
                response = requests.get(url)
                data = response.json()

                if "erro" not in data:
                    endereco = f"{data.get('logradouro', '')}, {data.get('bairro', '')}, {data.get('localidade', '')} - {data.get('uf', '')}"
                else:
                    endereco = None
            except requests.RequestException:
                endereco = None

    return render(request, 'index.html', {'endereco': endereco, 'cep': cep})


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

            if remember_me:
                request.session.set_expiry(1209600)
            else:
                request.session.set_expiry(0)

            messages.success(request, 'Login realizado com sucesso!')
            return render(request, 'pages/Autentificacao/login.html')
        else:
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
            user = User.objects.create_user(
                username=username,
                email=email,
                password=password
            )
            Perfil.objects.create(user=user, cpf=cpf, telefone=telefone)
            user.save()

            messages.success(request, 'Cadastro realizado com sucesso! Faça login.')
            return render(request, 'pages/Autentificacao/cadastro.html')

        except Exception:
            messages.error(request, 'Ocorreu um erro ao criar a conta. Tente novamente.')
            return render(request, 'pages/Autentificacao/cadastro.html')

    return render(request, 'pages/Autentificacao/cadastro.html')


def restaurante_view(request):
    return render(request, 'pages/catalogo/restaurante.html')


def prato_view(request):
    return render(request, 'pages/catalogo/prato.html')


def pedido_view(request):
    return render(request, 'pages/pedido/pedido.html')


def finalizacao_view(request):
    return render(request, 'pages/pedido/finalizacao.html')


def logout_view(request):
    logout(request)
    return redirect('login')
