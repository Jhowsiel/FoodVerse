from django.db import models
from django.contrib.auth.models import User

# ========================================================================
# 1. CLIENTES WEB (DJANGO AUTH + PERFIL)
# ========================================================================
# Os clientes são geridos nativamente pelo User do Django.
# O Perfil estende os dados que o Django não tem por padrão.
class Perfil(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)
    cpf = models.CharField(max_length=14, unique=True)
    telefone = models.CharField(max_length=15, blank=True)

    class Meta:
        db_table = 'tb_perfil_usuario'
        
    def __str__(self):
        return self.user.username

# ========================================================================
# 2. FUNCIONÁRIOS (GERIDOS PELO JAVA DESKTOP)
# ========================================================================
class Funcionario(models.Model):
    user_id = models.AutoField(primary_key=True, db_column='userID')
    name = models.CharField(max_length=100, db_column='name')
    user_name = models.CharField(max_length=50, unique=True, db_column='userName')
    email = models.EmailField(max_length=100, db_column='email')
    role = models.CharField(max_length=50, db_column='role')
    phone = models.CharField(max_length=20, db_column='phone')
    password = models.CharField(max_length=255, db_column='password')
    # CharField para evitar conflito de formato de data com o "Formatador" do Java
    registration_date = models.CharField(max_length=20, db_column='registrationDate') 
    status = models.CharField(max_length=20, default='pendente', db_column='status')

    class Meta:
        db_table = 'tb_funcionarios'

    def __str__(self):
        return f"{self.name} ({self.role})"

# ========================================================================
# 3. SISTEMA MULTI-RESTAURANTE (DJANGO WEB)
# ========================================================================
class Restaurante(models.Model):
    id_restaurante = models.AutoField(primary_key=True, db_column='ID_restaurante')
    nome = models.CharField(max_length=100)
    categoria = models.CharField(max_length=50)
    descricao = models.TextField(null=True, blank=True)
    avaliacao = models.DecimalField(max_digits=3, decimal_places=1, default=5.0)
    tempo_entrega = models.CharField(max_length=20, null=True, blank=True)
    taxa_entrega = models.DecimalField(max_digits=10, decimal_places=2, default=0.0)
    cupom_ativo = models.CharField(max_length=20, null=True, blank=True)
    imagem = models.ImageField(upload_to='restaurantes/', null=True, blank=True)

    class Meta:
        db_table = 'tb_restaurantes'

    def __str__(self):
        return self.nome

# ========================================================================
# 4. ESTOQUE E INSUMOS (JAVA DESKTOP)
# ========================================================================
class UnidadeMedida(models.Model):
    codigo = models.CharField(max_length=10, primary_key=True, db_column='codigo_unidade')
    base = models.CharField(max_length=10, db_column='base')
    fator_base = models.DecimalField(max_digits=10, decimal_places=4, db_column='fator_base')

    class Meta:
        db_table = 'tb_unidades_medida'

class ItemEstoque(models.Model):
    id_item = models.AutoField(primary_key=True, db_column='ID_item_estoque')
    nome = models.CharField(max_length=100, db_column='nome')
    categoria = models.CharField(max_length=50, db_column='categoria')
    unidade_padrao = models.CharField(max_length=10, db_column='unidade_padrao') 
    estoque_atual = models.DecimalField(max_digits=10, decimal_places=3, default=0.0, db_column='estoque_atual')
    estoque_minimo = models.DecimalField(max_digits=10, decimal_places=3, default=0.0, db_column='estoque_minimo')
    custo_medio = models.DecimalField(max_digits=10, decimal_places=2, default=0.0, db_column='custo_medio')
    ativo = models.BooleanField(default=True, db_column='ativo')

    class Meta:
        db_table = 'tb_itens_estoque'

class MovimentacaoEstoque(models.Model):
    id_movimentacao = models.AutoField(primary_key=True, db_column='ID_movimentacao')
    item = models.ForeignKey(ItemEstoque, on_delete=models.CASCADE, db_column='ID_item_estoque')
    nome_item_snapshot = models.CharField(max_length=100, db_column='nome_item_snapshot')
    tipo = models.CharField(max_length=20, db_column='tipo') # ENTRADA ou SAIDA
    quantidade = models.DecimalField(max_digits=10, decimal_places=3, db_column='quantidade')
    observacao = models.CharField(max_length=255, null=True, blank=True, db_column='observacao')
    data_movimento = models.DateTimeField(auto_now_add=True, db_column='data_movimento')

    class Meta:
        db_table = 'tb_movimentacoes_estoque'

# ========================================================================
# 5. CARDÁPIO UNIFICADO (PRODUTOS, PRATOS E RECEITAS)
# ========================================================================
class Produto(models.Model):
    id_produto = models.AutoField(primary_key=True, db_column='ID_produto')
    restaurante = models.ForeignKey(Restaurante, on_delete=models.CASCADE, null=True, blank=True, db_column='ID_restaurante')
    nome_produto = models.CharField(max_length=100, db_column='nome_produto')
    des_produto = models.CharField(max_length=255, null=True, blank=True, db_column='des_produto')
    categoria = models.CharField(max_length=50, null=True, blank=True, db_column='categoria')
    preco_produto = models.DecimalField(max_digits=10, decimal_places=2, db_column='preco_produto')
    ativo = models.BooleanField(default=True, db_column='ativo')
    is_prato = models.BooleanField(default=False, db_column='is_prato') # Diferencia Prato vs Bebida

    # Informação Nutricional para a Web
    kcal = models.IntegerField(null=True, blank=True, db_column='kcal')
    proteina = models.CharField(max_length=20, null=True, blank=True, db_column='proteina')
    carbo = models.CharField(max_length=20, null=True, blank=True, db_column='carbo')
    gordura = models.CharField(max_length=20, null=True, blank=True, db_column='gordura')

    class Meta:
        db_table = 'tb_produtos'

class ReceitaItem(models.Model):
    id_receita = models.AutoField(primary_key=True, db_column='ID_receita')
    produto = models.ForeignKey(Produto, on_delete=models.CASCADE, db_column='ID_produto')
    item_estoque = models.ForeignKey(ItemEstoque, on_delete=models.RESTRICT, db_column='ID_item_estoque')
    unidade = models.CharField(max_length=10, db_column='unidade')
    quantidade = models.DecimalField(max_digits=10, decimal_places=3, db_column='quantidade')

    class Meta:
        db_table = 'tb_receitas_prato'

# ========================================================================
# 6. PEDIDOS E RESERVAS
# ========================================================================
class Reserva(models.Model):
    id_reserva = models.AutoField(primary_key=True, db_column='ID_reserva')
    # Mudança: O cliente agora é o User do Django Auth
    cliente = models.ForeignKey(User, on_delete=models.CASCADE, db_column='ID_cliente')
    data_reserva = models.DateTimeField(db_column='data_reserva')
    num_pessoas = models.IntegerField(db_column='num_pessoas')
    mesa = models.CharField(max_length=10, db_column='mesa')

    class Meta:
        db_table = 'tb_reservas'

class StatusPedido(models.Model):
    status_id = models.IntegerField(primary_key=True, db_column='status_id')
    status_nome = models.CharField(max_length=50, db_column='status_nome')

    class Meta:
        db_table = 'tb_status_pedido'

class TipoPedido(models.Model):
    tipo_id = models.AutoField(primary_key=True, db_column='tipo_id')
    tipo_nome = models.CharField(max_length=50, db_column='tipo_nome')

    class Meta:
        db_table = 'tb_tipo_pedido'

class Pedido(models.Model):
    id_pedido = models.CharField(primary_key=True, max_length=10, db_column='ID_pedido')
    restaurante = models.ForeignKey(Restaurante, on_delete=models.CASCADE, null=True, blank=True, db_column='ID_restaurante')
    data_pedido = models.DateTimeField(db_column='data_pedido')
    hora_entrega = models.DateTimeField(null=True, blank=True, db_column='hora_entrega')
    codigo_localizador = models.CharField(max_length=50, null=True, blank=True, db_column='codigo_localizador')
    
    # Mudança: O cliente agora é o User do Django Auth
    cliente = models.ForeignKey(User, on_delete=models.CASCADE, db_column='ID_cliente')
    status = models.ForeignKey(StatusPedido, on_delete=models.CASCADE, db_column='status_id')
    tipo = models.ForeignKey(TipoPedido, on_delete=models.CASCADE, db_column='tipo_id')
    
    modo_consumo = models.CharField(max_length=20, db_column='modo_consumo')
    endereco_completo = models.CharField(max_length=255, null=True, blank=True, db_column='endereco_completo')
    
    nome_entregador = models.CharField(max_length=100, null=True, blank=True, db_column='nome_entregador')
    telefone_entregador = models.CharField(max_length=20, null=True, blank=True, db_column='telefone_entregador')
    observacoes = models.CharField(max_length=255, null=True, blank=True, db_column='observacoes')
    
    reserva = models.ForeignKey(Reserva, on_delete=models.SET_NULL, null=True, blank=True, db_column='ID_reserva')

    class Meta:
        db_table = 'tb_pedidos'

class PedidoProduto(models.Model):
    pedido = models.ForeignKey(Pedido, on_delete=models.CASCADE, db_column='ID_pedido')
    produto = models.ForeignKey(Produto, on_delete=models.CASCADE, db_column='ID_produto')
    quantidade = models.IntegerField(db_column='quantidade')

    class Meta:
        db_table = 'tb_pedidos_produtos'
        unique_together = ('pedido', 'produto')

# ========================================================================
# 7. EXTRAS: PAGAMENTOS, AVALIAÇÕES E FIDELIDADE
# ========================================================================
class Cupom(models.Model):
    id_cupom = models.AutoField(primary_key=True, db_column='ID_cupom')
    restaurante = models.ForeignKey(Restaurante, on_delete=models.CASCADE, null=True, blank=True, db_column='ID_restaurante')
    codigo_cupom = models.CharField(max_length=50, db_column='codigo_cupom')
    desconto = models.DecimalField(max_digits=5, decimal_places=2, db_column='desconto')
    validade = models.DateField(db_column='validade')

    class Meta:
        db_table = 'tb_cupons'

class Avaliacao(models.Model):
    id_avaliacao = models.AutoField(primary_key=True, db_column='ID_avaliacao')
    restaurante = models.ForeignKey(Restaurante, on_delete=models.CASCADE, null=True, blank=True, db_column='ID_restaurante')
    cliente = models.ForeignKey(User, on_delete=models.CASCADE, db_column='ID_cliente')
    produto = models.ForeignKey(Produto, on_delete=models.CASCADE, db_column='ID_produto')
    comentario = models.CharField(max_length=255, db_column='comentario')
    nota = models.IntegerField(db_column='nota')
    data_avaliacao = models.DateField(db_column='data_avaliacao')

    class Meta:
        db_table = 'tb_avaliacoes'

class Pagamento(models.Model):
    id_pagamento = models.AutoField(primary_key=True, db_column='ID_pagamento')
    pedido = models.ForeignKey(Pedido, on_delete=models.CASCADE, db_column='ID_pedido')
    metodo_pagamento = models.CharField(max_length=50, db_column='metodo_pagamento')
    valor_total = models.DecimalField(max_digits=10, decimal_places=2, db_column='valor_total')
    data_pagamento = models.DateField(db_column='data_pagamento')

    class Meta:
        db_table = 'tb_pagamentos'

class Fidelidade(models.Model):
    id_fidelidade = models.AutoField(primary_key=True, db_column='ID_fidelidade')
    cliente = models.ForeignKey(User, on_delete=models.CASCADE, db_column='ID_cliente')
    pontos = models.IntegerField(default=0, db_column='pontos')
    cashback = models.DecimalField(max_digits=10, decimal_places=2, default=0.00, db_column='cashback')

    class Meta:
        db_table = 'tb_fidelidade'
