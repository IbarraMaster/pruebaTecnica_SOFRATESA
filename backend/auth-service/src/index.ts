import express from 'express';
import cors from 'cors';
import path from 'path';
import 'dotenv/config';
import authRoutes from './routes/auth';

const app = express();
app.use(cors());
app.use(express.json());

app.get('/health', (_req, res) => {
  res.status(200).json({ status: 'ok' });
});

// OpenAPI como archivo estático (ver sección 7.3 del enunciado).
app.get('/openapi.yaml', (_req, res) => {
  res.type('text/yaml').sendFile(path.join(__dirname, '../openapi.yaml'));
});

app.use(authRoutes);

const PORT = process.env.PORT || 3001;
app.listen(PORT, () => {
  console.log(`auth-service escuchando en el puerto ${PORT}`);
});