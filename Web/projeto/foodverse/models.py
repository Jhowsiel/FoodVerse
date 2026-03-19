from django.db import models

class TbClientes(models.Model):
    id_cliente = models.AutoField(primary_key=True)
    username = models.CharField(max_length=50, unique=True, null=True, blank=True)
    nome = models.CharField(max_length=100, null=True, blank=True)
    email = models.CharField(max_length=100, unique=True, null=True, blank=True)
    telefone = models.CharField(max_length=20, null=True, blank=True)
    cpf = models.CharField(max_length=14, unique=True, null=True, blank=True)
    senha = models.CharField(max_length=255, null=True, blank=True)
    endereco = models.CharField(max_length=255, null=True, blank=True)
    data_cadastro = models.DateTimeField(null=True, blank=True)

    class Meta:
        db_table = "tb_clientes"

class TbRestaurantes(models.Model):
    id_restaurante = models.AutoField(primary_key=True)
    nome = models.CharField(max_length=100, null=True, blank=True)
    categoria = models.CharField(max_length=50, null=True, blank=True)
    descricao = models.CharField(max_length=255, null=True, blank=True)
    avaliacao = models.DecimalField(max_digits=3, decimal_places=2, null=True, blank=True)
    tempo_entrega = models.CharField(max_length=20, null=True, blank=True)
    taxa_entrega = models.DecimalField(max_digits=10, decimal_places=2, null=True, blank=True)
    cupom = models.CharField(max_length=50, null=True, blank=True)
    imagem = models.CharField(max_length=255, null=True, blank=True)
    banner = models.CharField(max_length=255, null=True, blank=True)
    ativo = models.BooleanField(default=True)    # Admin: controla se existe na plataforma
    aberto = models.BooleanField(default=True)   # Gerente: controla se aceita pedidos agora

    class Meta:
        db_table = "tb_restaurantes"

class TbProdutos(models.Model):
    id_produto = models.AutoField(primary_key=True)
    restaurante = models.ForeignKey(
        TbRestaurantes,
        on_delete=models.CASCADE,
        db_column="ID_restaurante",
        null=True,
        blank=True
    )
    nome_produto = models.CharField(max_length=100, null=True, blank=True)
    descricao = models.CharField(max_length=255, null=True, blank=True)
    preco = models.DecimalField(max_digits=10, decimal_places=2, null=True, blank=True)
    categoria = models.CharField(max_length=50, null=True, blank=True)
    imagem = models.CharField(max_length=255, null=True, blank=True)
    tempo_preparo = models.IntegerField(null=True, blank=True)
    disponivel = models.BooleanField(null=True, blank=True)
    destaque = models.BooleanField(null=True, blank=True)
    restricoes = models.CharField(max_length=255, null=True, blank=True)
    data_criacao = models.DateTimeField(null=True, blank=True)
    tipo_produto = models.CharField(max_length=20, default='VENDA', null=True, blank=True)

    class Meta:
        db_table = "tb_produtos"

class TbEstoque(models.Model):
    id_estoque = models.AutoField(primary_key=True)
    produto = models.ForeignKey(
        TbProdutos,
        on_delete=models.CASCADE,
        db_column="ID_produto"
    )
    quantidade = models.IntegerField(null=True, blank=True)
    estoque_minimo = models.IntegerField(null=True, blank=True)
    ultima_atualizacao = models.DateTimeField(null=True, blank=True)

    class Meta:
        db_table = "tb_estoque"

class TbNutricao(models.Model):
    id_nutricao = models.AutoField(primary_key=True)
    produto = models.ForeignKey(
        TbProdutos,
        on_delete=models.CASCADE,
        db_column="ID_produto"
    )
    kcal = models.IntegerField(null=True, blank=True)
    proteina = models.CharField(max_length=20, null=True, blank=True)
    carbo = models.CharField(max_length=20, null=True, blank=True)
    gordura = models.CharField(max_length=20, null=True, blank=True)

    class Meta:
        db_table = "tb_nutricao"

class TbStatusPedido(models.Model):
    id_status = models.IntegerField(primary_key=True)
    nome_status = models.CharField(max_length=50, null=True, blank=True)

    class Meta:
        db_table = "tb_status_pedido"

class TbPedidos(models.Model):
    id_pedido = models.AutoField(primary_key=True)
    cliente = models.ForeignKey(
        TbClientes,
        on_delete=models.CASCADE,
        db_column="ID_cliente"
    )
    restaurante = models.ForeignKey(
        TbRestaurantes,
        on_delete=models.CASCADE,
        db_column="ID_restaurante"
    )
    status = models.ForeignKey(
        TbStatusPedido,
        on_delete=models.SET_NULL,
        db_column="status_id",
        null=True
    )
    data_pedido = models.DateTimeField(null=True, blank=True)
    endereco_entrega = models.CharField(max_length=255, null=True, blank=True)
    valor_total = models.DecimalField(max_digits=10, decimal_places=2, null=True, blank=True)

    class Meta:
        db_table = "tb_pedidos"

class TbPedidosProdutos(models.Model):
    id_produto_pedido = models.AutoField(primary_key=True)
    pedido = models.ForeignKey(TbPedidos, models.DO_NOTHING, db_column='ID_pedido')
    produto = models.ForeignKey(TbProdutos, models.DO_NOTHING, db_column='ID_produto')
    quantidade = models.IntegerField(db_column='quantidade')

    class Meta:
        db_table = 'tb_pedidos_produtos'
        unique_together = (('pedido', 'produto'),)

class TbReservas(models.Model):
    id_reserva = models.AutoField(primary_key=True)
    cliente = models.ForeignKey(
        TbClientes,
        on_delete=models.CASCADE,
        db_column="ID_cliente"
    )
    restaurante = models.ForeignKey(
        TbRestaurantes,
        on_delete=models.CASCADE,
        db_column="ID_restaurante"
    )
    data_reserva = models.DateTimeField(null=True, blank=True)
    numero_pessoas = models.IntegerField(null=True, blank=True)
    mesa = models.CharField(max_length=10, null=True, blank=True)

    class Meta:
        db_table = "tb_reservas"

class TbPagamentos(models.Model):
    id_pagamento = models.AutoField(primary_key=True)
    pedido = models.ForeignKey(
        TbPedidos,
        on_delete=models.CASCADE,
        db_column="ID_pedido"
    )
    metodo_pagamento = models.CharField(max_length=50, null=True, blank=True)
    valor = models.DecimalField(max_digits=10, decimal_places=2, null=True, blank=True)
    data_pagamento = models.DateTimeField(null=True, blank=True)

    class Meta:
        db_table = "tb_pagamentos"

class TbAvaliacoes(models.Model):
    id_avaliacao = models.AutoField(primary_key=True)
    cliente = models.ForeignKey(
        TbClientes,
        on_delete=models.CASCADE,
        db_column="ID_cliente"
    )
    restaurante = models.ForeignKey(
        TbRestaurantes,
        on_delete=models.CASCADE,
        db_column="ID_restaurante"
    )
    comentario = models.CharField(max_length=255, null=True, blank=True)
    nota = models.IntegerField(null=True, blank=True)
    data_avaliacao = models.DateTimeField(null=True, blank=True)

    class Meta:
        db_table = "tb_avaliacoes"

class TbCupons(models.Model):
    id_cupom = models.AutoField(primary_key=True)
    codigo = models.CharField(max_length=50, null=True, blank=True)
    desconto = models.DecimalField(max_digits=5, decimal_places=2, null=True, blank=True)
    validade = models.DateField(null=True, blank=True)

    class Meta:
        db_table = "tb_cupons"

class TbFidelidade(models.Model):
    id_fidelidade = models.AutoField(primary_key=True)
    cliente = models.ForeignKey(
        TbClientes,
        on_delete=models.CASCADE,
        db_column="ID_cliente"
    )
    pontos = models.IntegerField(null=True, blank=True)
    cashback = models.DecimalField(max_digits=10, decimal_places=2, null=True, blank=True)

    class Meta:
        db_table = "tb_fidelidade"

class TbFuncionarios(models.Model):
    id_funcionario = models.AutoField(primary_key=True)
    restaurante = models.ForeignKey(
        TbRestaurantes,
        on_delete=models.SET_NULL,
        db_column="ID_restaurante",
        null=True,
        blank=True
    )
    nome = models.CharField(max_length=100, null=True, blank=True)
    username = models.CharField(max_length=50, null=True, blank=True)
    email = models.CharField(max_length=100, null=True, blank=True)
    cargo = models.CharField(max_length=50, null=True, blank=True)
    telefone = models.CharField(max_length=20, null=True, blank=True)
    senha = models.CharField(max_length=255, null=True, blank=True)
    status = models.CharField(max_length=20, null=True, blank=True)
    data_cadastro = models.DateTimeField(null=True, blank=True)

    class Meta:
        db_table = "tb_funcionarios"
        
class TbAvaliacoesProdutos(models.Model):
    id_avaliacao_produto = models.AutoField(primary_key=True)
    cliente = models.ForeignKey(
        TbClientes,
        on_delete=models.CASCADE,
        db_column="ID_cliente"
    )
    produto = models.ForeignKey(
        TbProdutos,
        on_delete=models.CASCADE,
        db_column="ID_produto"
    )
    nota = models.IntegerField(null=True, blank=True)
    comentario = models.CharField(max_length=255, null=True, blank=True)
    data_avaliacao = models.DateTimeField(null=True, blank=True)

    class Meta:
        db_table = "tb_avaliacoes_produtos"