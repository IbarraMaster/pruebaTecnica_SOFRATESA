import { Router } from 'express';
import bcrypt from 'bcrypt';
import jwt from 'jsonwebtoken';
import { z } from 'zod';
import { prisma } from '../db';

const router = Router();

const loginSchema = z.object({
  usuario: z.string().min(1),
  password: z.string().min(1),
});

router.post('/auth/login', async (req, res) => {
  const parsed = loginSchema.safeParse(req.body);
  if (!parsed.success) {
    return res.status(401).json({ error: 'credenciales inválidas' });
  }

  const { usuario, password } = parsed.data;
  const user = await prisma.usuario.findUnique({ where: { usuario } });

  // Si el usuario no existe, comparamos igual contra un hash "falso"
  // para que el tiempo de respuesta sea similar y no revele si el usuario existe.
  const hashParaComparar = user?.passwordHash ?? '$2b$12$invalidinvalidinvalidinvalidinvalidinva';
  const passwordCorrecta = await bcrypt.compare(password, hashParaComparar);

  if (!user || !passwordCorrecta) {
    return res.status(401).json({ error: 'credenciales inválidas' });
  }

  const expiresInMin = parseInt(process.env.TOKEN_EXPIRES_MIN || '60', 10);
  const token = jwt.sign(
    { sub: user.id, usuario: user.usuario },
    process.env.JWT_SECRET as string,
    { expiresIn: `${expiresInMin}m` }
  );

  const expiraEn = new Date(Date.now() + expiresInMin * 60_000).toISOString();

  res.status(200).json({ token, expira_en: expiraEn });
});

export default router;