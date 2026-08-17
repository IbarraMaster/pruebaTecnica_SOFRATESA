import express from "express";
import dotenv from "dotenv";
import path from "path";
import registrosRouter from "./routes/registros";

dotenv.config();
const app = express();
app.use(express.json());

app.get("/health", (_req, res) => res.status(200).json({ status: "ok" }));

// OpenAPI como archivo estático (ver sección 7.3 del enunciado).
app.get("/openapi.yaml", (_req, res) => {
  res.type("text/yaml").sendFile(path.join(__dirname, "../openapi.yaml"));
});

app.use("/registros", registrosRouter);

const port = process.env.PORT || 3002;
app.listen(port, () => console.log(`records-service escuchando en ${port}`));