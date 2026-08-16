import { Router } from "express";
import { PrismaClient, Prisma } from "@prisma/client";
import { requireAuth, AuthRequest } from "../middleware/auth";

const router = Router();
const prisma = new PrismaClient();

const UUID_V4 = /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
const CODIGO = /^[A-Za-z0-9_-]{3,30}$/;
const TIPOS = ["PREVENTIVO", "CORRECTIVO", "INSPECCION"];

function validar(body: any): string | null {
  if (!UUID_V4.test(body.id_registro)) return "id_registro inválido";
  if (!CODIGO.test(body.codigo_activo)) return "codigo_activo inválido";
  if (!TIPOS.includes(body.tipo_actividad)) return "tipo_actividad inválido";
  if (typeof body.observacion !== "string" || body.observacion.length < 1 || body.observacion.length > 500)
    return "observacion inválida";
  if (isNaN(Date.parse(body.capturado_en))) return "capturado_en inválido";
  return null;
}

router.post("/", requireAuth, async (req: AuthRequest, res) => {
  const error = validar(req.body);
  if (error) return res.status(400).json({ error });

  const { id_registro, codigo_activo, tipo_actividad, observacion, capturado_en } = req.body;

  try {
    await prisma.registro.create({
      data: {
        idRegistro: id_registro,
        codigoActivo: codigo_activo,
        tipoActividad: tipo_actividad,
        observacion,
        capturadoEn: new Date(capturado_en),
        usuarioId: req.usuarioId!,
      },
    });
    return res.status(201).json({ mensaje: "registro almacenado" });
  } catch (e) {
    if (e instanceof Prisma.PrismaClientKnownRequestError && e.code === "P2002") {
      return res.status(200).json({ mensaje: "registro ya existía" });
    }
    console.error("error al almacenar registro");
    return res.status(500).json({ error: "error interno" });
  }
});

export default router;