package dev.dong4j.ai.spring.controller;

import jakarta.annotation.Resource;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 基础提示词控制器
 *
 * <p>该控制器演示 Spring AI 中基础提示词的创建和使用方法，包括： 1. 简单字符串提示词 2. 多消息提示词 3. 带选项的提示词
 *
 * @author ogromwang
 * @version 1.0.0
 * @email mailto:ogromwang@gmail.com
 * @date 2025.11.29
 * @since 1.0.0
 */
@Controller
@ResponseBody
@RequestMapping("/prompts")
public class PromptController {

    /** OpenAI 聊天客户端实例, 用于演示基础提示词功能 */
    @Resource
    private ChatClient openAiChatClient;

    /**
     * 演示最简单的字符串提示词
     *
     * <p>该方法展示了如何创建一个基本的字符串提示词，这是最简单和最直接的提示词方式。 这种方式适合快速原型开发和简单的问答场景。
     *
     * @param input 用户输入的问题，使用 prompt(String content) 方法
     * @return AI 模型的回复内容
     */
    @GetMapping("/simple")
    public String simplePrompt(@RequestParam(defaultValue = "给我讲个笑话吧") String input) {
        return openAiChatClient.prompt(input).call().content();
    }

    /**
     * 演显式创建用户消息的提示词
     *
     * <p>该方法展示了如何显式地创建 UserMessage 对象，这提供了更细粒度的控制。 这种方式适合需要精确控制消息内容和格式的场景。
     *
     * @return AI 模型的回复内容
     */
    @GetMapping("/user-message")
    public String promptWithUserMessage() {
        String userText =
            """
                告诉我两种著名的编程语言，并分别为每种语言提供简要概述。
                """;
        UserMessage userMessage = new UserMessage(userText);

        return openAiChatClient.prompt().messages(userMessage).call().content();
    }

    /**
     * 演示多消息提示词
     *
     * <p>该方法展示了如何创建包含多个消息的复杂提示词，这在构建上下文丰富的对话时非常有用。 通过组合不同类型的消息，可以创建更精确和上下文相关的提示词。
     *
     * @return AI 模型的回复内容
     */
    @GetMapping("/multi-message")
    public String promptWithMultipleMessages() {
        // 创建系统消息
        var systemMessage =
            new SystemMessage(
                """
                    你是一个专业的技术导师。
                    你的回答应该简洁明了，重点突出核心概念。
                    每个回答不要超过200字。
                    """);

        // 创建用户消息
        var userMessage = new UserMessage("请解释什么是微服务架构？");

        // 创建提示词
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        // call
        return openAiChatClient.prompt(prompt).call().content();
    }

    /**
     * 演示带选项的提示词
     *
     * <p>该方法展示了如何为提示词添加配置选项，如温度、模型参数等。 这些选项可以控制 AI 模型的行为和输出特征。
     *
     * @param creative 是否启用创意模式（高温度）
     * @return AI 模型的回复内容
     */
    @GetMapping("/with-options")
    public String promptWithOptions(@RequestParam(defaultValue = "false") boolean creative) {
        String promptText = "请用创意的方式解释什么是人工智能";

        // 根据参数设置不同的选项
        var options =
            creative
            ? OpenAiChatOptions.builder().temperature(0.9).build()
            : OpenAiChatOptions.builder().temperature(0.3).build();

        Prompt prompt = new Prompt(promptText, options);
        var content = openAiChatClient.prompt(prompt).call().content();

        return String.format(
            "创意模式: %s%n温度参数: %s%n%n%s", creative, options.getTemperature(), content);
    }
}
