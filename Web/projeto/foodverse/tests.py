from django.test import TestCase
from django.urls import reverse

from .models import TbProdutos, TbRestaurantes


class MarketplaceRestaurantesTests(TestCase):
    def setUp(self):
        self.restaurante_ativo = TbRestaurantes.objects.create(
            nome='Restaurante Ativo',
            categoria='Lanches',
            ativo=True,
            aberto=False,
        )
        self.restaurante_inativo = TbRestaurantes.objects.create(
            nome='Restaurante Inativo',
            categoria='Lanches',
            ativo=False,
            aberto=True,
        )

        TbProdutos.objects.create(
            restaurante=self.restaurante_ativo,
            nome_produto='Hambúrguer',
            preco='25.90',
            disponivel=True,
        )
        TbProdutos.objects.create(
            restaurante=self.restaurante_inativo,
            nome_produto='Pizza Oculta',
            preco='49.90',
            disponivel=True,
        )

    def test_home_lista_apenas_restaurantes_ativos(self):
        response = self.client.get(reverse('home'))

        restaurantes = list(response.context['restaurantes'])

        self.assertEqual(response.status_code, 200)
        self.assertIn(self.restaurante_ativo, restaurantes)
        self.assertNotIn(self.restaurante_inativo, restaurantes)

    def test_restaurante_detalhe_retorna_404_para_restaurante_inativo(self):
        response = self.client.get(
            reverse('restaurante_detalhe', args=[self.restaurante_inativo.id_restaurante])
        )

        self.assertEqual(response.status_code, 404)

    def test_busca_ignora_restaurantes_inativos_mesmo_com_produto_correspondente(self):
        response = self.client.get(
            reverse('buscar_prato_restaurante'),
            {'q': 'Pizza Oculta'},
        )

        restaurantes = list(response.context['restaurantes'])

        self.assertEqual(response.status_code, 200)
        self.assertNotIn(self.restaurante_inativo, restaurantes)

    def test_pedido_sem_parametro_usa_primeiro_restaurante_ativo(self):
        response = self.client.get(reverse('pedido'))

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.context['restaurante'], self.restaurante_ativo)
