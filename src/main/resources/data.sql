-- Datos de ejemplo compatibles con el modelo actual.
-- Los totales de las facturas reales se calculan en FacturaService a partir de sus líneas.

INSERT INTO facturas (
    num_factura,
    fecha_factura,
    empresa_nombre,
    empresa_cif,
    empresa_direccion,
    empresa_email,
    cliente_nombre,
    cliente_cif,
    cliente_direccion,
    subtotal,
    iva_total,
    total,
    estado
) VALUES
    ('F-2026-001', '2026-08-10', 'Mi Empresa', 'B00000000', 'Calle Principal 1, Madrid', 'facturacion@example.com', 'Acme S.L.', 'B11111111', 'Calle Cliente 1, Madrid', 1000.00, 200.00, 1200.00, 'Pagada'),
    ('F-2026-002', '2026-08-12', 'Mi Empresa', 'B00000000', 'Calle Principal 1, Madrid', 'facturacion@example.com', 'Beta Corp', 'B22222222', 'Calle Cliente 2, Madrid', 700.00, 150.00, 850.00, 'Pendiente')
ON CONFLICT (num_factura) DO NOTHING;

INSERT INTO factura_items (description, quantity, unit_price, tax, total, factura_id)
SELECT 'Servicio de consultoría', 2.00, 500.00, 20.00, 1200.00, f.id
FROM facturas f
WHERE f.num_factura = 'F-2026-001'
  AND NOT EXISTS (
      SELECT 1 FROM factura_items i
      WHERE i.factura_id = f.id AND i.description = 'Servicio de consultoría'
  );

INSERT INTO factura_items (description, quantity, unit_price, tax, total, factura_id)
SELECT 'Desarrollo de software', 1.00, 708.33, 20.00, 850.00, f.id
FROM facturas f
WHERE f.num_factura = 'F-2026-002'
  AND NOT EXISTS (
      SELECT 1 FROM factura_items i
      WHERE i.factura_id = f.id AND i.description = 'Desarrollo de software'
  );
