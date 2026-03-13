# clientes/context_processors.py
from .models import TbClientes

def cliente_logado(request):
    cliente_id = request.session.get('cliente_id')
    if cliente_id:
        return {'cliente_logado': TbClientes.objects.filter(id_cliente=cliente_id).first()}
    return {'cliente_logado': None}
    
def carrinho_context(request):
    carrinho = request.session.get('carrinho', {})
    
    total_itens = sum(
        i['quantidade']
        for r in carrinho.values()
        for i in r.get('itens', [])
    )
    
    subtotal = sum(
        (i.get('preco') or 0) * i.get('quantidade', 0)
        for r in carrinho.values()
        for i in r.get('itens', [])
    )
    
    return {
        'carrinho': carrinho,
        'carrinho_total_itens': total_itens,
        'carrinho_subtotal': subtotal,
    }