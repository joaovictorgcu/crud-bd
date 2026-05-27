-- =====================================================================
--  Mod 02 - Etapa 04 - Entregável 3: ÍNDICES
--  Sistema de Gerenciamento de Academia (academia_db / PostgreSQL)
-- =====================================================================

CREATE INDEX IF NOT EXISTS idx_pagamento_status_plano
    ON pagamento (status, cod_plano);

CREATE INDEX IF NOT EXISTS idx_assinatura_nro_matric
    ON assinatura (nro_matric);

ANALYZE;
