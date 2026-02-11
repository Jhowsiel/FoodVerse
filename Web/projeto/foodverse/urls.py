from django.urls import path
from . import views

from django.urls import path
from . import views
 
urlpatterns = [
    path('login/', views.login_view, name='login'),
    path('cadastro/', views.cadastro_view, name='cadastro'),
    path('', views.home, name='home'),
    path('logout/', views.logout_view, name='logout'),
    ]