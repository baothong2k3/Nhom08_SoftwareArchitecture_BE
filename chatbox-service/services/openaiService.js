import { OpenAI } from 'openai';  // Nhập đúng thư viện OpenAI

const openai = new OpenAI({
    apiKey: "YOUR_OPENAI"
});

export const askOpenAI = async (prompt) => {
    try {
        const chatCompletion = await openai.chat.completions.create({
            model: "gpt-3.5-turbo", // Đảm bảo sử dụng đúng mô hình GPT-3.5
            messages: [
                { role: "user", content: prompt }  // Tin nhắn của người dùng
            ]
        });

        return chatCompletion.choices[0].message.content; // Trả về nội dung tin nhắn phản hồi
    } catch (error) {
        console.error('Error with OpenAI API:', error);  // Log lỗi nếu có
        throw error;
    }
};
