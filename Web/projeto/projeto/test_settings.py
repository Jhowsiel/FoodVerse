from .settings import *  # noqa: F401,F403


DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.sqlite3',
        'NAME': BASE_DIR / 'test_db.sqlite3',
    }
}

PASSWORD_HASHERS = [
    # Apenas para acelerar a suíte de testes local.
    'django.contrib.auth.hashers.MD5PasswordHasher',
]
