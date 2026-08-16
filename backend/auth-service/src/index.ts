import express from 'express';
import cors from 'cors';
import 'dotenv/config';
import authRoutes from './routes/auth';

const app = express();
app.use(cors());
app.use(express.json());

app.get('/health', (_req, res) => {
  res.status(200).json({ status: 'ok' });
});

app.use(authRoutes);

const PORT = process.env.PORT || 3001;
app.listen(PORT, () => {
  console.log(`auth-service escuchando en el puerto ${PORT}`);
});