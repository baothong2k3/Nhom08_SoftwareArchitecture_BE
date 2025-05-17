import { askGemini } from "../services/geminiService.js";

export const sendMessage = async (req, res) => {
    const { message } = req.body;
    try {
        const response = await askGemini(message);
        res.json({ response });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
};
