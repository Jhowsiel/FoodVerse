from django.urls import path
from . import views

urlpatterns = [
    path('', views.home, name='home'),
    path('login/', views.login_view, name='login'),
    path('cadastro/', views.cadastro_view, name='cadastro'),
    path('restaurantes/', views.restaurante_view, name='restaurantes'),
    path('restaurantes/<int:id>/', views.restaurante_detalhe_view, name='restaurante_detalhe'),
    path('restaurantes/buscar/', views.buscar_prato_restaurante, name='buscar_prato_restaurante'),
    path('prato/', views.prato_view, name='prato'),
    path('pedido/', views.pedido_view, name='pedido'),
    path('finalizacao/', views.finalizacao_view, name='finalizacao'),
    path('logout/', views.logout_view, name='logout'),
]
