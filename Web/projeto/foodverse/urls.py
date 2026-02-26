from django.urls import path
from django.conf import settings
from django.conf.urls.static import static
from . import views

urlpatterns = [
    path('', views.home, name='home'),
    path('login/', views.login_view, name='login'),
    path('cadastro/', views.cadastro_view, name='cadastro'),
    path('logout/', views.logout_view, name='logout'),
    
    # Restaurantes e Busca
    path('restaurantes/', views.restaurante_view, name='restaurantes'),
    path('restaurantes/<int:id>/', views.restaurante_detalhe_view, name='restaurante_detalhe'),
    path('restaurantes/buscar/', views.buscar_prato_restaurante, name='buscar_prato_restaurante'),
    
    # Detalhe do Prato
    path('prato/', views.prato_view, name='prato'),
    
    # Fluxo de Pedido e Carrinho
    path('carrinho/', views.carrinho_view, name='carrinho'), 
    path('reserva/', views.reserva_view, name='reserva'),
    path('reserva/pagamento', views.reserva_pagamento, name='reserva_pagamento'),
    path('pedido/', views.pedido_view, name='pedido'),
    path('finalizacao/', views.finalizacao_view, name='finalizacao'),
    
    # Sugestão: Rota de confirmação após pagar
    # path('confirmacao/', views.confirmacao_view, name='confirmacao'),
]

# Adicione isso para conseguir visualizar as imagens das comidas durante o desenvolvimento
if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)