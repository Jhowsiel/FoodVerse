from django.db import migrations, models


def _colunas_tabela(schema_editor, tabela):
    with schema_editor.connection.cursor() as cursor:
        descricao = schema_editor.connection.introspection.get_table_description(cursor, tabela)
    return {coluna.name for coluna in descricao}


def adicionar_campos_status_restaurante(apps, schema_editor):
    tabela = apps.get_model('foodverse', 'TbRestaurantes')._meta.db_table
    colunas = _colunas_tabela(schema_editor, tabela)
    vendor = schema_editor.connection.vendor

    with schema_editor.connection.cursor() as cursor:
        if 'ativo' not in colunas:
            if vendor == 'sqlite':
                cursor.execute(f'ALTER TABLE {tabela} ADD COLUMN ativo bool NOT NULL DEFAULT 1')
            else:
                cursor.execute(
                    f'ALTER TABLE {tabela} ADD ativo BIT NOT NULL CONSTRAINT DF_{tabela}_ativo DEFAULT 1'
                )

        if 'aberto' not in colunas:
            if vendor == 'sqlite':
                cursor.execute(f'ALTER TABLE {tabela} ADD COLUMN aberto bool NOT NULL DEFAULT 1')
            else:
                cursor.execute(
                    f'ALTER TABLE {tabela} ADD aberto BIT NOT NULL CONSTRAINT DF_{tabela}_aberto DEFAULT 1'
                )


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
