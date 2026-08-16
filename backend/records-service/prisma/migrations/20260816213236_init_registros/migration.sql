-- CreateTable
CREATE TABLE "registros" (
    "id_registro" UUID NOT NULL,
    "codigo_activo" TEXT NOT NULL,
    "tipo_actividad" TEXT NOT NULL,
    "observacion" TEXT NOT NULL,
    "capturado_en" TIMESTAMP(3) NOT NULL,
    "recibido_en" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "usuario_id" TEXT NOT NULL,

    CONSTRAINT "registros_pkey" PRIMARY KEY ("id_registro")
);
