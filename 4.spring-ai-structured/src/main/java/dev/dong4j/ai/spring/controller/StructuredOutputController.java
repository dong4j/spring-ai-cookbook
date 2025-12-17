package dev.dong4j.ai.spring.controller;

import dev.dong4j.ai.spring.converter.ChineseListOutputConverter;
import dev.dong4j.ai.spring.model.ActorsFilms;

import jakarta.annotation.Resource;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * 结构化输出转换器控制器
 *
 * <p>该控制器演示 Spring AI 中各种结构化输出转换器的使用方法，包括： 1. BeanOutputConverter：映射到 Java 类/记录 2.
 * MapOutputConverter：灵活的键值对 3. ListOutputConverter：纯列表输出 4. 原生结构化输出能力 5. 自定义转换器：中文顿号分隔列表
 *
 * <p>每个示例都展示了不同的使用场景和最佳实践，帮助开发者理解如何将大模型的自由文本输出 转换为可靠的结构化数据。
 *
 * @author ogromwang
 * @version 1.0.0
 * @email mailto:ogromwang@gmail.com
 * @date 2025.12.17
 * @since 1.0.0
 */
@Controller
@ResponseBody
@RequestMapping("/structured")
public class StructuredOutputController {

    /** 默认的 OpenAI 聊天客户端实例 */
    @Resource private ChatClient openAiChatClient;

    /** 支持原生结构化输出的 OpenAI 聊天客户端实例 */
    @Resource private ChatClient openAiChatClientWithNativeSupport;

    /**
     * 演示 BeanOutputConverter - 基本对象转换
     *
     * <p>该方法展示了如何将大模型的自由文本输出直接转换为强类型的 Java 对象。 这是最常用的结构化输出方式，适合需要强类型对象的场景（如 DTO、领域模型）。
     *
     * <p>示例：获取 Tom Hanks 的 5 部电影，并直接得到 ActorsFilms 记录对象。
     *
     * @return 包含演员姓名和电影列表的 ActorsFilms 对象的 JSON 表示
     */
    @GetMapping("/bean")
    public String beanOutputConverter() {
        ActorsFilms result =
                openAiChatClient
                        .prompt()
                        .user("生成汤姆·汉克斯的5部电影作品列表。")
                        .call()
                        .entity(ActorsFilms.class);

        if (result == null) {
            return "未能获取到演员电影信息";
        }

        return String.format("演员: %s\n电影列表: %s", result.actor(), result.movies());
    }

    /**
     * 演示 BeanOutputConverter - 泛型复杂结构
     *
     * <p>该方法展示了如何处理带泛型的复杂结构，如 List<ActorsFilms>。 必须使用 ParameterizedTypeReference 来保留泛型信息，否则 Jackson
     * 无法正确反序列化。
     *
     * <p>注意：某些模型（如 OpenAI）原生不支持"对象数组"，此时应关闭原生模式， 使用默认的结构化输出转换器。
     *
     * @return 包含多个演员及其电影的列表的 JSON 表示
     */
    @GetMapping("/bean-list")
    public String beanListOutputConverter() {
        List<ActorsFilms> films =
                openAiChatClient
                        .prompt()
                        .user("列出汤姆·汉克斯和朱莉娅·罗伯茨各自的3部电影作品。")
                        .call()
                        .entity(new ParameterizedTypeReference<List<ActorsFilms>>() {});

        if (films == null || films.isEmpty()) {
            return "未能获取到演员电影列表";
        }

        StringBuilder result = new StringBuilder("演员电影列表:\n");
        for (ActorsFilms actorsFilms : films) {
            result.append(
                    String.format("演员: %s\n电影: %s\n\n", actorsFilms.actor(), actorsFilms.movies()));
        }
        return result.toString();
    }

    /**
     * 演示 MapOutputConverter - 灵活的键值对
     *
     * <p>该方法展示了如何使用 MapOutputConverter 获取灵活的键值对结构。 适用于输出结构不确定，或只需临时数据容器的场景。
     *
     * <p>底层使用 RFC8259 JSON 格式指令，确保模型返回合法 JSON。
     *
     * @return 用户信息的 Map 对象的 JSON 表示
     */
    @GetMapping("/map")
    public String mapOutputConverter() {
        Map<String, Object> result =
                openAiChatClient
                        .prompt()
                        .user("返回用户信息: name=张三, age=25, active=true," + " hobbies=读书,游泳")
                        .call()
                        .entity(new ParameterizedTypeReference<Map<String, Object>>() {});

        if (result == null || result.isEmpty()) {
            return "未能获取到用户信息";
        }

        StringBuilder output = new StringBuilder("用户信息:\n");
        result.forEach((key, value) -> output.append(String.format("%s: %s\n", key, value)));
        return output.toString();
    }

    /**
     * 演示 ListOutputConverter - 纯列表输出
     *
     * <p>该方法展示了如何获取纯列表输出，适用于模型只需返回一维列表的场景， 如关键词、选项、ID 列表等。
     *
     * <p>格式指令会提示模型："返回逗号分隔的列表，不要编号、不要解释"。
     *
     * @return 冰淇淋口味列表的字符串表示
     */
    @GetMapping("/list")
    public String listOutputConverter() {
        List<String> flavors =
                openAiChatClient
                        .prompt()
                        .user("列出五种冰淇淋口味")
                        .call()
                        .entity(new ListOutputConverter(new DefaultConversionService()));

        if (flavors == null || flavors.isEmpty()) {
            return "未能获取到冰淇淋口味列表";
        }

        return "冰淇淋口味: " + String.join(", ", flavors);
    }

    /**
     * 演示自定义转换器 - 中文顿号分隔列表
     *
     * <p>该方法展示了如何创建和使用自定义转换器，专门处理中文场景下的列表输出。 使用中文顿号（、）作为分隔符，更符合中文书写习惯。
     *
     * <p>这展示了 Spring AI 的扩展性：当内置转换器不满足需求时， 可以通过继承和重写来创建自定义转换器。
     *
     * @return 使用中文顿号分隔的水果列表
     */
    @GetMapping("/chinese-list")
    public String chineseListOutputConverter() {
        ChineseListOutputConverter converter = new ChineseListOutputConverter();
        List<String> fruits = openAiChatClient.prompt().user("列出五种常见的水果").call().entity(converter);

        if (fruits == null || fruits.isEmpty()) {
            return "未能获取到水果列表";
        }

        return "水果列表: " + String.join("、", fruits);
    }

    /**
     * 演示原生结构化输出 - 单次调用启用
     *
     * <p>该方法展示了如何在单次调用中启用原生结构化输出。 通过在调用时添加特定的 Advisor，让模型强制输出结构化数据。
     *
     * <p>原生结构化输出由模型内部强制执行，可靠性远高于仅靠提示词约束， 尤其适合生产环境。但需要注意的是，并非所有模型都支持此功能。
     *
     * @return 随机演员的电影作品信息的 JSON 表示
     */
    @GetMapping("/native")
    public String nativeStructuredOutput() {
        // 注意：这里的 AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT 是示例
        // 实际使用时需要根据具体的 Spring AI 版本和配置调整
        try {
            ActorsFilms actorFilms =
                    openAiChatClientWithNativeSupport
                            .prompt()
                            .user("生成一个随机演员的电影作品列表。")
                            .call()
                            .entity(ActorsFilms.class);

            if (actorFilms == null) {
                return "未能获取到演员电影信息";
            }

            return String.format(
                    "原生结构化输出 - 演员: %s\n电影: %s", actorFilms.actor(), actorFilms.movies());
        } catch (Exception e) {
            return "原生结构化输出暂不可用，使用默认转换器: " + e.getMessage();
        }
    }

    /**
     * 演示全局配置的原生结构化输出
     *
     * <p>该方法展示了如何通过 ChatClient.builder 的 defaultAdvisors 配置， 全局启用原生结构化输出。所有使用该客户端的调用都会自动应用结构化输出约束。
     *
     * <p>这种方式避免了在每次调用时重复配置，适合整个应用都需要结构化输出的场景。
     *
     * @return 全局配置下的电影推荐信息的 JSON 表示
     */
    @GetMapping("/native-global")
    public String globalNativeStructuredOutput() {
        try {
            ActorsFilms recommendation =
                    openAiChatClientWithNativeSupport
                            .prompt()
                            .user("推荐3部经典电影，并按演员-电影结构格式化")
                            .call()
                            .entity(ActorsFilms.class);

            if (recommendation == null) {
                return "未能获取到电影推荐信息";
            }

            return String.format(
                    "全局原生输出 - 推荐: %s\n电影: %s", recommendation.actor(), recommendation.movies());
        } catch (Exception e) {
            return "全局原生结构化输出暂不可用，使用默认转换器: " + e.getMessage();
        }
    }

    /**
     * 演示对象数组的处理（不启用原生模式）
     *
     * <p>某些模型（如 OpenAI）原生不支持"对象数组"，例如：
     *
     * <pre>
     * [
     *   { "actor": "Tom", "movies": [...] },
     *   { "actor": "Julia", "movies": [...] }
     * ]
     * </pre>
     *
     * <p>在这种情况下，即使启用了原生结构化输出，模型也可能失败或回退到自由文本。 此时应关闭原生模式，改用 Spring AI 默认的结构化输出转换器。
     *
     * @return 多个演员电影列表的 JSON 表示
     */
    @GetMapping("/object-array")
    public String objectArrayWithoutNative() {
        // 不启用原生模式，依赖默认转换器处理对象数组
        List<ActorsFilms> result =
                openAiChatClient
                        .prompt()
                        .user("为两位演员创建电影作品列表：莱昂纳多·迪卡普里奥和凯特·温斯莱特")
                        .call()
                        .entity(new ParameterizedTypeReference<List<ActorsFilms>>() {});

        if (result == null || result.isEmpty()) {
            return "未能获取到演员电影数组信息";
        }

        StringBuilder output = new StringBuilder("对象数组输出（不使用原生模式）:\n");
        result.forEach(
                af ->
                        output.append(
                                String.format(
                                        "演员: %s, 电影数: %d\n", af.actor(), af.movies().size())));

        return output.toString();
    }
}
