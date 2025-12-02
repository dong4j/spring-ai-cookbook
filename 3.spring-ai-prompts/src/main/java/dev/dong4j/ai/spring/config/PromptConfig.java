package dev.dong4j.ai.spring.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI Prompts 配置类
 *
 * <p>用于配置和初始化不同的聊天客户端实例, 支持 OpenAI 和 Anthropic 等聊天模型的集成. 提供了默认配置的 chatModel.
 *
 * @author ogromwang
 * @version 1.0.0
 * @email mailto:ogromwang@gmail.com
 * @date 2025.11.29
 * @since 1.0.0
 */
@Configuration
public class PromptConfig {

    /**
     * 创建并返回一个配置好的 OpenAI 聊天客户端实例
     *
     * <p>该方法使用默认提供的 OpenAiChatModel 对象进行配置, 可设置默认的模型和温度参数, 然后创建并返回一个 ChatClient 实例.
     *
     * @param chatModel 用于构建聊天客户端的模型配置对象
     * @return 配置好的 ChatClient 实例
     */
    @Bean
    public ChatClient openAiChatClient(OpenAiChatModel chatModel) {
        return ChatClient.create(
            chatModel
                .mutate()
                .defaultOptions(
                    OpenAiChatOptions.builder()
                        .model("Qwen/Qwen3-8B")
                        .temperature(0.7)
                        .build())
                .build());
    }
}
