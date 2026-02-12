from django.shortcuts import render, redirect
from django.contrib.auth import authenticate, login, logout
from django.contrib.auth.models import User
from django.contrib import messages
from django.contrib.auth.decorators import login_required

def home(request):
    return render(request, 'index.html')

def login_view(request):
    # Se o usuário já estiver logado, redireciona para a home
    if request.user.is_authenticated:
        return redirect('home')

    if request.method == 'POST':
        username = request.POST.get('username')
        password = request.POST.get('password')
        remember_me = request.POST.get('remember_me')  #
        
        # Tenta autenticar
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

import re

import re
from django.shortcuts import render, redirect
from django.contrib import messages
from django.contrib.auth.models import User

def cadastro_view(request):
    # Se o usuário já estiver logado, redireciona para a home
    if request.user.is_authenticated:
        return redirect('home')

    if request.method == 'POST':
        username = request.POST.get('username')
        email = request.POST.get('email')
        password = request.POST.get('password')
        confirm_password = request.POST.get('confirm_password')

        erros = []

        if not username or not email or not password or not confirm_password:
            erros.append("• Todos os campos são obrigatórios.")


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
            user.save()

            messages.success(request, 'Cadastro realizado com sucesso! Faça login.')
            return render(request, 'pages/Autentificacao/cadastro.html')

        except Exception:
            messages.error(request, 'Ocorreu um erro ao criar a conta. Tente novamente.')
            return render(request, 'pages/Autentificacao/cadastro.html')

    return render(request, 'pages/Autentificacao/cadastro.html')


def logout_view(request):
    logout(request)
    return redirect('login')