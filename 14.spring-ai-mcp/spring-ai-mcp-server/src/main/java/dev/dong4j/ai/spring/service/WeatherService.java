package dev.dong4j.ai.spring.service;

import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

/**
 * @author haijun
 * @version 1.0.0
 * @email "mailto:zhonghaijun@email.com"
 * @date 2025.04.15 11:17
 * @since 1.0.0
 */
@Service
public class WeatherService {

  /**
   * Query weather
   *
   * @param city city
   * @param toolContext tool context
   * @return the string
   * @since 1.0.0
   */
  @Tool(description = "天气查询")
  public String queryWeather(String city, ToolContext toolContext) {
    System.out.println("回调函数确认：" + this.callMcpSampling(toolContext, city));
    return city + "的天气是18°";
  }

  /**
   * Call mcp sampling
   *
   * @param toolContext tool context
   * @param city city
   * @return the string
   * @since 1.0.0
   */
  private String callMcpSampling(ToolContext toolContext, String city) {
    StringBuilder stringBuilder = new StringBuilder();
    McpToolUtils.getMcpExchange(toolContext)
        .ifPresent(
            exchange -> {
              if (exchange.getClientCapabilities().sampling() != null) {
                McpSchema.CreateMessageRequest messageRequestBuilder =
                    McpSchema.CreateMessageRequest.builder()
                        .messages(
                            List.of(
                                new McpSchema.SamplingMessage(
                                    McpSchema.Role.USER,
                                    new McpSchema.TextContent("你确定要查询:" + city))))
                        .build();
                exchange.createMessage(messageRequestBuilder);
                stringBuilder.append(messageRequestBuilder);
              }
            });
    return stringBuilder.toString();
  }
}
