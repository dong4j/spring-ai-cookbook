package dev.dong4j.ai.spring.controller;

import jakarta.annotation.Resource;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * PromptRole 控制器
 *
 * <p>该控制器演示 Spring AI 中多角色提示词的使用方法，包括： 1. System + User 角色组合 3. Tool/Function 角色示例 4. 多轮对话管理 5.
 * 角色上下文控制
 *
 * @author ogromwang
 * @version 1.0.0
 * @email mailto:ogromwang@gmail.com
 * @date 2025.11.29
 * @since 1.0.0
 */
@Controller
@ResponseBody
@RequestMapping("/roles")
public class PromptRoleController {

    /** OpenAI 聊天客户端实例 */
    @Resource
    private ChatClient openAiChatClient;

    /**
     * 演示 System + User 角色组合
     *
     * <p>该方法展示了如何组合系统消息和用户消息来创建上下文丰富的提示词。 System 角色用于定义 AI 的行为和回应风格，User 角色提供具体的问题或任务。
     *
     * @param assistantName AI助手名字
     * @param voice         回应风格
     * @param question      用户问题
     * @return AI 模型的回复内容
     */
    @GetMapping("/system-user")
    public String systemUserPrompt(
        @RequestParam(defaultValue = "技术专家") String assistantName,
        @RequestParam(defaultValue = "专业且友好") String voice,
        @RequestParam(defaultValue = "请解释什么是微服务架构") String question) {

        String systemText =
            """
                你是一个有帮助的AI助手，帮助人们找到信息。
                你的名字是：{name}
                你应该用{voice}的语调来回应用户的请求。
                回答应准确、全面且易于理解。
                """;

        PromptTemplate systemPromptTemplate = new PromptTemplate(systemText);
        Message systemMessage =
            systemPromptTemplate.createMessage(
                Map.of(
                    "name", assistantName,
                    "voice", voice));

        Message userMessage = new UserMessage(question);

        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        var response = openAiChatClient.prompt(prompt).call().content();

        return String.format("""
                                 系统角色设置:
                                 %s
                                 助手名字: %s
                                 回应风格: %s

                                 用户问题: %s

                                 AI 回复:
                                 %s
                                 """, systemText, assistantName, voice, question, response);
    }
}
