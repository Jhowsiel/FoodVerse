from django.db import migrations, models


def _colunas_tabela(schema_editor, tabela):
    with schema_editor.connection.cursor() as cursor:
        descricao = schema_editor.connection.introspection.get_table_description(cursor, tabela)
    return {coluna.name for coluna in descricao}


def adicionar_campos_status_restaurante(apps, schema_editor):
    tb_restaurantes = apps.get_model('foodverse', 'TbRestaurantes')
    tabela = tb_restaurantes._meta.db_table
    colunas = _colunas_tabela(schema_editor, tabela)

    if 'ativo' not in colunas:
        campo_ativo = models.BooleanField(default=True)
        campo_ativo.set_attributes_from_name('ativo')
        schema_editor.add_field(tb_restaurantes, campo_ativo)

    if 'aberto' not in colunas:
        campo_aberto = models.BooleanField(default=True)
        campo_aberto.set_attributes_from_name('aberto')
        schema_editor.add_field(tb_restaurantes, campo_aberto)


class Migration(migrations.Migration):

    dependencies = [
        ('foodverse', '0002_tbfuncionarios_restaurante'),
    ]

    operations = [
        migrations.SeparateDatabaseAndState(
            database_operations=[
                migrations.RunPython(adicionar_campos_status_restaurante, migrations.RunPython.noop),
            ],
            state_operations=[
                migrations.AddField(
                    model_name='tbrestaurantes',
                    name='ativo',
                    field=models.BooleanField(default=True),
                ),
                migrations.AddField(
                    model_name='tbrestaurantes',
                    name='aberto',
                    field=models.BooleanField(default=True),
                ),
            ],
        ),
    ]
