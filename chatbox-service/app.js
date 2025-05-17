import express from "express";
import cors from "cors";
import dotenv from "dotenv";
import chatRoutes from "./routes/chatRoutes.js";

dotenv.config();

const app = express();

console.log("GEMINI_API_KEY: ", process.env.GEMINI_API_KEY);  // Kiểm tra API Key

const PORT = 8990;

app.use(cors());
app.use(express.json());

app.use("/api/chat", chatRoutes);

app.listen(PORT, () => {
    console.log(`Server running at http://localhost:${PORT}`);
});
