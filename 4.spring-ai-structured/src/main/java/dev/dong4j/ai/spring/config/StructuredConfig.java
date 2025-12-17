package dev.dong4j.ai.spring.config;

import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI Structured Output 配置类
 *
 * <p>用于配置和初始化聊天客户端实例，支持 OpenAI 模型的集成，以及结构化输出的相关配置。提供了不同的聊天客户端配置： 1. 默认配置的 openAiChatClient 2.
 * 支持原生结构化输出的 openAiChatClientWithNativeSupport 3. 全局启用原生结构化输出的 openAiChatClientGlobalNative
 *
 * @author ogromwang
 * @version 1.0.0
 * @email mailto:ogromwang@gmail.com
 * @date 2025.12.17
 * @since 1.0.0
 */
@Configuration
public class StructuredConfig {

    /**
     * 创建并返回一个配置好的 OpenAI 聊天客户端实例
     *
     * <p>该方法使用默认提供的 OpenAiChatModel 对象进行配置，可设置默认的模型和温度参数，然后创建并返回一个 ChatClient 实例。
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

    /**
     * 创建并返回一个支持原生结构化输出的 OpenAI 聊天客户端实例
     *
     * <p>该客户端通过 ChatClient.builder 创建，便于后续添加 Advisor 来启用原生结构化输出功能。 在实际使用中，可以通过调用时添加 Advisor
     * 来启用原生结构化输出。
     *
     * @param chatModel 用于构建聊天客户端的模型配置对象
     * @return 配置好的支持原生结构化输出的 ChatClient 实例
     */
    @Bean
    public ChatClient openAiChatClientWithNativeSupport(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultOptions(
                        OpenAiChatOptions.builder().model("Qwen/Qwen3-8B").temperature(0.7).build())
                // 注意，AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT 在 stable 1.1.2 版本中提供
                .defaultAdvisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
                .build();
    }
}
