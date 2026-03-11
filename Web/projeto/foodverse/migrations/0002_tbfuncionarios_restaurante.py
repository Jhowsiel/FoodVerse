import django.db.models.deletion
from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('foodverse', '0001_initial'),
    ]

    operations = [
        migrations.AddField(
            model_name='tbfuncionarios',
            name='restaurante',
            field=models.ForeignKey(
                blank=True,
                db_column='ID_restaurante',
                null=True,
                on_delete=django.db.models.deletion.SET_NULL,
                to='foodverse.tbrestaurantes',
            ),
        ),
    ]
