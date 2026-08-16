import express from "express";
import dotenv from "dotenv";
import registrosRouter from "./routes/registros";

dotenv.config();
const app = express();
app.use(express.json());

app.get("/health", (_req, res) => res.status(200).json({ status: "ok" }));
app.use("/registros", registrosRouter);

const port = process.env.PORT || 3002;
app.listen(port, () => console.log(`records-service escuchando en ${port}`));