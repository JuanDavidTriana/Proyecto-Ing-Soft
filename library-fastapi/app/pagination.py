# =============================================================================
# pagination.py - Helper para calcular metadata de paginación
# =============================================================================
# ¿Por qué paginar? Sin esto, un GET /libros/ con 500.000 registros
# devolvería TODA la tabla en una sola respuesta: lento, pesado para el
# cliente, y un golpe fuerte a la base de datos en cada request. Con
# paginación, el cliente pide "de a pedazos" (por ejemplo, de 10 en 10)
# y navega con `skip`/`limit`.
#
# Esta función centraliza el cálculo de la metadata que le devolvemos al
# cliente junto a los resultados, para no repetir la misma lógica en el
# router de usuarios y en el de libros.
# =============================================================================

import math


def calcular_metadata_paginacion(total: int, skip: int, limit: int) -> dict:
    """A partir de:
      - total: cuántos registros hay en total (sin paginar) en la tabla.
      - skip: cuántos registros nos saltamos (offset).
      - limit: cuántos registros máximo devolvemos en esta página.

    Calcula:
      - total_paginas: en cuántas "páginas" de tamaño `limit` cabría
        todo el total. Ej: 25 libros con limit=10 -> 3 páginas (10+10+5).
      - pagina_actual: en qué número de página estamos ahora mismo,
        asumiendo que siempre se navega en saltos de `limit`.
      - hay_siguiente: true si después de esta "página" quedan más
        registros por leer.
      - hay_anterior: true si skip > 0 (ya avanzamos al menos una vez).
    """
    total_paginas = math.ceil(total / limit) if limit > 0 else 0
    pagina_actual = (skip // limit) + 1 if limit > 0 else 1

    return {
        "total_paginas": total_paginas,
        "pagina_actual": pagina_actual,
        "hay_siguiente": (skip + limit) < total,
        "hay_anterior": skip > 0,
    }
