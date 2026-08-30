INSERT INTO factura (num_factura, cliente, fecha_factura, importe, estado) VALUES
    ('F-2026-001', 'Acme S.L.', '10/08/2026', 1200, 'Pagada'),
    ('F-2026-002', 'Beta Corp', '12/08/2026', 850, 'Pendiente')
ON CONFLICT (num_factura) DO NOTHING;
