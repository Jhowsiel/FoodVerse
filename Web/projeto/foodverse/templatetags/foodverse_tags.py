from django import template
from django.conf import settings

register = template.Library()


@register.simple_tag
def img_url(path):
    """Resolve image path stored in DB to a usable URL.

    - External URLs (http/https) are returned as-is.
    - Local paths are prefixed with MEDIA_URL so Django can serve them.
    - Paths that already start with MEDIA_URL are not double-prefixed.
    """
    if not path:
        return ''
    path = str(path).strip()
    if path.startswith(('http://', 'https://')):
        return path
    media_url = getattr(settings, 'MEDIA_URL', '/media/')
    clean = path.lstrip('/')
    if clean.startswith(media_url.lstrip('/')):
        return '/' + clean
    return media_url + clean
