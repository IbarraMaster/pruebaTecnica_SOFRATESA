import { PrismaClient } from '@prisma/client';
import bcrypt from 'bcrypt';

const prisma = new PrismaClient();

async function main() {
  const passwordPlano = process.env.SEED_PASSWORD || 'Tecnico123!';
  const hash = await bcrypt.hash(passwordPlano, 12);

  await prisma.usuario.upsert({
    where: { usuario: 'tecnico1' },
    update: {},
    create: {
      usuario: 'tecnico1',
      passwordHash: hash,
    },
  });

  console.log('Usuario de prueba creado: tecnico1');
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });