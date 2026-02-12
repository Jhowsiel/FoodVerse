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

            return redirect('home')
        else:
            messages.error(request, 'Usuário ou senha incorretos.')
            
    return render(request, 'pages/Autentificacao/login.html')

def cadastro_view(request):
    # Se o usuário já estiver logado, redireciona para a home
    if request.user.is_authenticated:
        return redirect('home')

    if request.method == 'POST':
        username = request.POST.get('username')
        email = request.POST.get('email')
        password = request.POST.get('password')
        confirm_password = request.POST.get('confirm_password')

        # Validações
        if password != confirm_password:
            messages.error(request, 'As senhas não conferem.')
            return render(request, 'pages/Autentificacao/cadastro.html')
        
        if User.objects.filter(username=username).exists():
            messages.error(request, 'Este nome de usuário já está em uso.')
            return render(request, 'pages/Autentificacao/cadastro.html')

        if User.objects.filter(email=email).exists():
            messages.error(request, 'Este e-mail já está cadastrado.')
            return render(request, 'pages/Autentificacao/cadastro.html')

        try:
            # Cria o usuário no banco de dados (db.sqlite3)
            user = User.objects.create_user(username=username, email=email, password=password)
            user.save()
            
            messages.success(request, 'Cadastro realizado com sucesso! Faça login.')
            return redirect('login')
            
        except Exception as e:
            messages.error(request, 'Ocorreu um erro ao criar a conta. Tente novamente.')
            return render(request, 'pages/Autentificacao/cadastro.html')

    return render(request, 'pages/Autentificacao/cadastro.html')

def logout_view(request):
    logout(request)
    return redirect('login')