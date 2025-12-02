package dev.dong4j.ai.spring.controller;

import jakarta.annotation.Resource;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * PromptEngineering 控制器
 *
 * <p>该控制器演示 Spring AI 中的提示词工程技术，包括： 1. Zero-shot 学习技术 2. Few-shot 学习技术
 *
 * @author ogromwang
 * @version 1.0.0
 * @email mailto:ogromwang@gmail.com
 * @date 2025.11.29
 * @since 1.0.0
 */
@Controller
@ResponseBody
@RequestMapping("/engineering")
public class PromptEngineeringController {

    /** OpenAI 聊天客户端实例 */
    @Resource
    private ChatClient openAiChatClient;

    /**
     * 演示 Zero-shot 学习技术
     *
     * <p>Zero-shot 学习是指让 AI 模型在没有先前示例的情况下完成特定任务。 这种技术依赖于模型的预训练知识和明确的任务描述。
     *
     * @param task  任务描述
     * @param input 输入内容
     * @return AI 模型的处理结果
     */
    @GetMapping("/zero-shot")
    public String zeroShotPrompt(
        @RequestParam(defaultValue = "情感分类") String task,
        @RequestParam(defaultValue = "这个产品的质量真的很棒！") String input) {

        String zeroShotTemplate =
            """
                你是一个专业的{task}专家。
                请直接对以下内容进行{task}，无需额外解释。

                输入内容：{input}

                请以 JSON 格式输出结果：
                {{"result": "你的分类结果", "confidence": "置信度"}}
                """;

        PromptTemplate promptTemplate = new PromptTemplate(zeroShotTemplate);
        Map<String, Object> variables = Map.of("task", task, "input", input);

        String renderedPrompt = promptTemplate.render(variables);
        String response = openAiChatClient.prompt().user(renderedPrompt).call().content();

        return String.format("""
                                 === Zero-shot 学习技术演示 ===
                                 任务类型: %s
                                 输入内容: %s
                                 技术说明: 模型在没有先前示例的情况下完成特定任务

                                 AI 处理结果:
                                 %s
                                 """, task, input, response);
    }

    /**
     * 演示 Few-shot 学习技术
     *
     * <p>Few-shot 学习通过在提示词中提供少量示例来帮助 AI 模型理解任务模式。 这种技术对于复杂或特定的任务格式特别有效。
     *
     * @param task  任务描述
     * @param input 需要处理的输入
     * @return AI 模型的处理结果
     */
    @GetMapping("/few-shot")
    public String fewShotPrompt(
        @RequestParam(defaultValue = "文本风格转换") String task,
        @RequestParam(defaultValue = "请把这句话改为正式商务风格：我们明天开会讨论") String input) {

        String fewShotTemplate =
            """
                你是一个专业的{task}专家。

                以下是一些{task}的示例：

                示例1:
                输入: "这个想法不错，我们试试看"
                输出: "此建议具有可行性，建议予以采纳并试行"

                示例2:
                输入: "我搞不懂这个怎么回事"
                输出: "对该问题存在理解困难，需要进一步说明"

                示例3:
                输入: "弄快点，没时间了"
                输出: "请加快处理进度，时间较为紧迫"

                现在请对以下内容进行{task}：
                输入: "{input}"

                输出:
                """;

        PromptTemplate promptTemplate = new PromptTemplate(fewShotTemplate);
        Map<String, Object> variables =
            Map.of(
                "task", task,
                "input", input);

        String renderedPrompt = promptTemplate.render(variables);
        String response = openAiChatClient.prompt().user(renderedPrompt).call().content();


        return String.format("""
                                 === Few-shot 学习技术演示 ===
                                 任务类型: %s
                                 输入内容: %s
                                 技术说明: 通过提供少量示例帮助模型理解任务模式
                                 提供的示例数: 3个

                                 AI 处理结果:
                                 %s
                                 """, task, input, response);
    }
}
