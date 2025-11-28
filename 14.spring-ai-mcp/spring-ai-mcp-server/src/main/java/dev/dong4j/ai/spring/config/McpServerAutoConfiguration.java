package dev.dong4j.ai.spring.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.dong4j.ai.spring.service.WeatherService;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Mcp Server Auto Configuration
 *
 * @author haijun
 * @date 2025-11-26 18:14:07
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Configuration
public class McpServerAutoConfiguration {

  /**
   * Weather tools
   *
   * @param weatherService weather service
   * @return the tool callback provider
   * @since 1.0.0
   */
  @Bean
  public ToolCallbackProvider weatherTools(WeatherService weatherService) {
    return MethodToolCallbackProvider.builder().toolObjects(weatherService).build();
  }

  /**
   * Resource1
   *
   * @return the list< sync resource specification>
   * @since 1.0.0-SNAPSHOT
   */
  @Bean
  public List<McpServerFeatures.SyncResourceSpecification> resource1() {
    McpSchema.Annotations annotations =
        new McpSchema.Annotations(List.of(McpSchema.Role.USER), 0.1);
    var systemInfoResource =
        new McpSchema.Resource("/v1/api/query", "queryDatabase", "数据库查询", "", annotations);
    var resourceSpecification =
        new McpServerFeatures.SyncResourceSpecification(
            systemInfoResource,
            (exchange, request) -> {
              try {
                var systemInfo = Map.of("order", "叔叔叔叔");
                String jsonContent = new ObjectMapper().writeValueAsString(systemInfo);
                return new McpSchema.ReadResourceResult(
                    List.of(
                        new McpSchema.TextResourceContents(
                            request.uri(), "application/json", jsonContent)));
              } catch (Exception e) {
                throw new RuntimeException("Failed to generate system info", e);
              }
            });
    return List.of(resourceSpecification);
  }

  /**
   * Prompts 1
   *
   * @return the list
   * @since 2025.4.22
   */
  @Bean
  public List<McpServerFeatures.SyncPromptSpecification> prompts() {
    var prompt =
        new McpSchema.Prompt(
            "greeting", "一个友好的提示词模板", List.of(new McpSchema.PromptArgument("name", "名称", true)));

    var promptSpecification =
        new McpServerFeatures.SyncPromptSpecification(
            prompt,
            (exchange, getPromptRequest) -> {
              // 获取到里面的参数
              String nameArgument = (String) getPromptRequest.arguments().get("name");
              if (nameArgument == null) {
                nameArgument = "朋友";
              }
              var userMessage =
                  new McpSchema.PromptMessage(
                      McpSchema.Role.USER,
                      new McpSchema.TextContent("你好，%s，请问我有什么可以帮助你的？".formatted(nameArgument)));
              return new McpSchema.GetPromptResult("个性化问候语", List.of(userMessage));
            });
    return List.of(promptSpecification);
  }

  /**
   * Roots Change Handler
   *
   * @return the bi consumer< mcp sync server exchange, list< root>>
   * @since 1.0.0-SNAPSHOT
   */
  @Bean
  public BiConsumer<McpSyncServerExchange, List<McpSchema.Root>> rootsChangeHandler() {
    return (exchange, roots) -> {
      roots.forEach(
          root -> {
            String uri = root.uri();
            System.out.println("roots资源:" + uri);

            McpSchema.LoggingMessageNotification notification =
                McpSchema.LoggingMessageNotification.builder()
                    .data("资源发生了变化")
                    .level(McpSchema.LoggingLevel.INFO)
                    .logger("rootsChangeHandler")
                    .build();
            exchange.loggingNotification(notification);
          });
    };
  }
}
