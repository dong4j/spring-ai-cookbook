package dev.dong4j.ai.spring.controller;

import jakarta.annotation.Resource;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * PromptTemplate 控制器
 *
 * <p>
 * 该控制器演示 Spring AI 中 PromptTemplate 的使用方法，包括： 1. 基础模板占位符 {variable} 使用 2.
 * 自定义分隔符 <variable> 模板
 * 3. 从资源文件加载模板
 *
 * @author ogromwang
 * @version 1.0.0
 * @email mailto:ogromwang@gmail.com
 * @date 2025.11.29
 * @since 1.0.0
 */
@Controller
@ResponseBody
@RequestMapping("/template")
public class PromptTemplateController {

    /** OpenAI 聊天客户端实例 */
    @Resource
    private ChatClient openAiChatClient;

    /**
     * 演示基础模板占位符
     *
     * <p>
     * 该方法展示了如何使用基础的 {variable} 占位符来创建可重用的提示词模板。 这是最常见的模板使用方式，适用于大多数场景。
     *
     * @param adjective 形容词
     * @param topic     主题
     * @return AI 模型的回复内容
     */
    @GetMapping("/basic")
    public String basicTemplate(
        @RequestParam(defaultValue = "interesting") String adjective,
        @RequestParam(defaultValue = "AI") String topic) {

        String template = "Tell me a {adjective} fact about {topic}";

        PromptTemplate promptTemplate = new PromptTemplate(template);
        Map<String, Object> variables = new HashMap<>();
        variables.put("adjective", adjective);
        variables.put("topic", topic);

        String renderedPrompt = promptTemplate.render(variables);

        return openAiChatClient.prompt().user(renderedPrompt).call().content();
    }

    /**
     * 演示自定义分隔符的模板
     *
     * <p>
     * 该方法展示了如何使用自定义的分隔符来避免与 JSON 等格式冲突。 当模板内容包含复杂格式时，这种技术特别有用。
     *
     * @param composer 作曲家名字
     * @return AI 模型的回复内容
     */
    @GetMapping("/custom-delimiter")
    public String customDelimiterTemplate(
        @RequestParam(defaultValue = "John Williams") String composer) {

        // 使用 StringTemplate 引擎的自定义分隔符
        var promptTemplate = PromptTemplate.builder()
            .renderer(
                StTemplateRenderer.builder()
                    .startDelimiterToken('<')
                    .endDelimiterToken('>')
                    .build())
            .template(
                """
                    告诉我由 <composer> 创作配乐的5部电影的名字。
                    对于每部电影，请注明其上映年份。
                    """)
            .build();

        Map<String, Object> variables = Map.of("composer", composer);
        String renderedPrompt = promptTemplate.render(variables);

        String response = openAiChatClient.prompt().user(renderedPrompt).call().content();

        return String.format("渲染后的提示词:%n%s%n%nAI 回复:%n%s", renderedPrompt, response);
    }

    @GetMapping("/resource")
    public String resourceTemplate(@RequestParam(defaultValue = "Java") String language) {
        try {
            // 加载系统消息模板资源
            ClassPathResource systemResource = new ClassPathResource("templates/system-message.st");
            if (!systemResource.exists()) {
                throw new IllegalStateException("模板文件不存在: templates/system-message.st");
            }

            // 使用 Spring 的 StreamUtils 读取为字符串（自动处理编码，默认 UTF-8）
            String templateContent = StreamUtils.copyToString(
                systemResource.getInputStream(),
                StandardCharsets.UTF_8);

            Map<String, Object> systemVariables = Map.of(
                "language", language,
                "name", "AI编程助手",
                "voice", "专业且友好");

            PromptTemplate systemPromptTemplate = PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder()
                              .startDelimiterToken('<')
                              .endDelimiterToken('>')
                              .build())
                .template(templateContent)
                .build();

            String systemPrompt = systemPromptTemplate.render(systemVariables);
            String userMessage = "请介绍" + language + "的主要特性和应用场景";

            String response = openAiChatClient
                .prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();

            return String.format("系统模板变量: %s%n%nAI 回复:%n%s", systemVariables, response);

        } catch (Exception e) {
            return "模板加载失败: " + e.getMessage();
        }
    }
}
