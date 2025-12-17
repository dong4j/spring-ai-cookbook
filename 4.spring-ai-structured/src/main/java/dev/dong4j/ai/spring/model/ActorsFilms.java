package dev.dong4j.ai.spring.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

/**
 * 演员-电影记录类
 *
 * <p>该记录类用于结构化输出转换器示例，演示如何将大模型的自由文本输出转换为强类型的 Java 对象。 包含演员姓名和其出演的电影列表。使用 @JsonPropertyOrder 注解确保
 * JSON Schema 字段顺序一致， 这对某些严格校验 Schema 的模型（如 OpenAI Structured Outputs）很重要。
 *
 * @param actor 演员姓名
 * @param movies 电影列表
 * @author ogromwang
 * @version 1.0.0
 * @email mailto:ogromwang@gmail.com
 * @date 2025.12.17
 * @since 1.0.0
 */
@JsonPropertyOrder({"actor", "movies"})
public record ActorsFilms(String actor, List<String> movies) {}
