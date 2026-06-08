-- =============================================================================
-- Seed: producto_serie (series de inventario para ítems con maneja_serie=true)
-- Base: PostgreSQL · BackEndEasy
--
-- Uso:
--   psql -U postgres -d factapp -f docs/seed_producto_serie.sql
--
-- Inserta 20 números de serie DISPONIBLE para "Producto Series" y otros serializados.
-- =============================================================================

BEGIN;

-- Opcional: datos fijos de prueba (RUC demo de tests / emulador)
INSERT INTO producto_serie (
    id, company_ruc, catalog_item_id, numero_serie, almacen_id, estado, created_at, updated_at
) VALUES
    (
        'e1000001-0001-4000-8000-000000000001',
        '20100000001',
        'd1000001-0001-4000-8000-000000000001',
        'SN-DEMO-LAP-001',
        'b1000001-0001-4000-8000-000000000001',
        'DISPONIBLE',
        now(),
        now()
    ),
    (
        'e1000001-0001-4000-8000-000000000002',
        '20100000001',
        'd1000001-0001-4000-8000-000000000001',
        'SN-DEMO-LAP-002',
        'b1000001-0001-4000-8000-000000000001',
        'DISPONIBLE',
        now(),
        now()
    ),
    (
        'e1000001-0001-4000-8000-000000000003',
        '20100000001',
        'd1000001-0001-4000-8000-000000000001',
        'SN-DEMO-LAP-003',
        'b1000001-0001-4000-8000-000000000001',
        'DISPONIBLE',
        now(),
        now()
    )
ON CONFLICT DO NOTHING;

-- "Producto Series": 20 series en el primer almacén activo
INSERT INTO producto_serie (
    id, company_ruc, catalog_item_id, numero_serie, almacen_id, estado, created_at, updated_at
)
SELECT
    gen_random_uuid(),
    ci.company_ruc,
    ci.id,
    'PS-SERIES-' || lpad(s.n::text, 3, '0'),
    alm.id,
    'DISPONIBLE',
    now(),
    now()
FROM catalog_item ci
INNER JOIN LATERAL (
    SELECT a.id
    FROM almacen a
    WHERE a.company_ruc = ci.company_ruc
      AND COALESCE(a.activo, true) = true
    ORDER BY a.codigo
    LIMIT 1
) alm ON true
CROSS JOIN generate_series(1, 20) AS s(n)
WHERE COALESCE(ci.activo, true) = true
  AND (
      ci.maneja_serie = true
      OR lower(ci.nombre) LIKE '%producto%series%'
      OR lower(ci.nombre) LIKE '%producto%serie%'
  )
  AND NOT EXISTS (
      SELECT 1
      FROM producto_serie ps
      WHERE ps.company_ruc = ci.company_ruc
        AND ps.catalog_item_id = ci.id
        AND ps.numero_serie = 'PS-SERIES-' || lpad(s.n::text, 3, '0')
  );

-- Otros productos serializados: 20 series cada uno
INSERT INTO producto_serie (
    id, company_ruc, catalog_item_id, numero_serie, almacen_id, estado, created_at, updated_at
)
SELECT
    gen_random_uuid(),
    ci.company_ruc,
    ci.id,
    'SN-' || upper(substr(replace(ci.id::text, '-', ''), 1, 8)) || '-' || lpad(s.n::text, 3, '0'),
    alm.id,
    'DISPONIBLE',
    now(),
    now()
FROM catalog_item ci
INNER JOIN LATERAL (
    SELECT a.id
    FROM almacen a
    WHERE a.company_ruc = ci.company_ruc
      AND COALESCE(a.activo, true) = true
    ORDER BY a.codigo
    LIMIT 1
) alm ON true
CROSS JOIN generate_series(1, 20) AS s(n)
WHERE ci.maneja_serie = true
  AND COALESCE(ci.activo, true) = true
  AND lower(ci.nombre) NOT LIKE '%producto%series%'
  AND lower(ci.nombre) NOT LIKE '%producto%serie%'
  AND NOT EXISTS (
      SELECT 1
      FROM producto_serie ps
      WHERE ps.company_ruc = ci.company_ruc
        AND ps.catalog_item_id = ci.id
        AND ps.numero_serie = 'SN-' || upper(substr(replace(ci.id::text, '-', ''), 1, 8)) || '-' || lpad(s.n::text, 3, '0')
  );

COMMIT;

-- Verificar
SELECT
    ci.nombre AS producto,
    ps.numero_serie,
    ps.estado,
    a.nombre AS almacen
FROM producto_serie ps
JOIN catalog_item ci ON ci.id = ps.catalog_item_id
LEFT JOIN almacen a ON a.id = ps.almacen_id
ORDER BY ci.nombre, ps.numero_serie;
