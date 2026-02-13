from django.db import models


class Cliente(models.Model):
    user_name = models.CharField(max_length=100)
    name = models.CharField(max_length=100)
    email = models.EmailField(max_length=100)
    phone = models.CharField(max_length=100)
    password = models.CharField(max_length=255)
    cpf = models.CharField(max_length=255)
    registration_date = models.DateField(auto_now_add=True)
    is_login = models.BooleanField(default=False)

    def __str__(self):
        return self.name


class Produto(models.Model):
    nome_produto = models.CharField(max_length=100)
    des_produto = models.CharField(max_length=255)
    preco_produto = models.DecimalField(max_digits=10, decimal_places=2)

    def __str__(self):
        return self.nome_produto


class Reserva(models.Model):
    cliente = models.ForeignKey(Cliente, on_delete=models.CASCADE)
    data_reserva = models.DateTimeField()
    num_pessoas = models.IntegerField()
    mesa = models.CharField(max_length=10)

    def __str__(self):
        return f"Reserva {self.id} - {self.cliente.name}"


class StatusPedido(models.Model):
    status_nome = models.CharField(max_length=50)

    def __str__(self):
        return self.status_nome


class TipoPedido(models.Model):
    tipo_nome = models.CharField(max_length=50)

    def __str__(self):
        return self.tipo_nome


class Pedido(models.Model):
    id_pedido = models.CharField(primary_key=True, max_length=10)
    data_pedido = models.DateTimeField()
    hora_entrega = models.DateTimeField(null=True, blank=True)
    codigo_localizador = models.CharField(max_length=50, null=True, blank=True)

    cliente = models.ForeignKey(Cliente, on_delete=models.CASCADE)
    status = models.ForeignKey(StatusPedido, on_delete=models.CASCADE)
    tipo = models.ForeignKey(TipoPedido, on_delete=models.CASCADE)

    modo_consumo = models.CharField(max_length=20)
    endereco_completo = models.CharField(max_length=255, null=True, blank=True)

    nome_entregador = models.CharField(max_length=100, null=True, blank=True)
    telefone_entregador = models.CharField(max_length=20, null=True, blank=True)
    observacoes = models.CharField(max_length=255, null=True, blank=True)

    reserva = models.ForeignKey(Reserva, on_delete=models.SET_NULL, null=True, blank=True)

    def __str__(self):
        return self.id_pedido


class PedidoProduto(models.Model):
    pedido = models.ForeignKey(Pedido, on_delete=models.CASCADE)
    produto = models.ForeignKey(Produto, on_delete=models.CASCADE)
    quantidade = models.IntegerField()

    class Meta:
        unique_together = ('pedido', 'produto')


class Cupom(models.Model):
    codigo_cupom = models.CharField(max_length=50)
    desconto = models.DecimalField(max_digits=5, decimal_places=2)
    validade = models.DateField()

    def __str__(self):
        return self.codigo_cupom


class Avaliacao(models.Model):
    cliente = models.ForeignKey(Cliente, on_delete=models.CASCADE)
    produto = models.ForeignKey(Produto, on_delete=models.CASCADE)
    comentario = models.CharField(max_length=255)
    nota = models.IntegerField()
    data_avaliacao = models.DateField()

    def __str__(self):
        return f"{self.cliente.name} - {self.produto.nome_produto}"


class Pagamento(models.Model):
    pedido = models.ForeignKey(Pedido, on_delete=models.CASCADE)
    metodo_pagamento = models.CharField(max_length=50)
    valor_total = models.DecimalField(max_digits=10, decimal_places=2)
    data_pagamento = models.DateField()

    def __str__(self):
        return f"Pagamento {self.id}"
    

from django.contrib.auth.models import User
from django.db import models

class Perfil(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)
    cpf = models.CharField(max_length=14, unique=True)
    telefone = models.CharField(max_length=15, blank=True)

    def __str__(self):
        return self.user.username