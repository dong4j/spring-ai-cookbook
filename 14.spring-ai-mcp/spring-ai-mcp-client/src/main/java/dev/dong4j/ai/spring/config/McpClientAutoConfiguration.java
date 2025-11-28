package dev.dong4j.ai.spring.config;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.customizer.McpSyncClientCustomizer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * McpClientAutoConfiguration
 *
 * @author haijun
 * @date 2025-11-26 18:04:57
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0
 */
@Configuration
public class McpClientAutoConfiguration {

  /** log */
  private final Logger log = LoggerFactory.getLogger(getClass());

  /**
   * Predefined Questions
   *
   * @param mcpSyncClients mcp sync clients
   * @return the command line runner 1.0.0-SNAPSHOT
   */
  @Bean
  public CommandLineRunner predefinedQuestions(List<McpSyncClient> mcpSyncClients) {
    return args -> {
      mcpSyncClients.forEach(
          mcpSyncClient -> {
            List<String> tools =
                mcpSyncClient.listTools().tools().stream().map(McpSchema.Tool::name).toList();
            log.info("连接到mcp服务，提供的工具：" + tools);

            McpSchema.CallToolResult callTool =
                mcpSyncClient.callTool(
                    new McpSchema.CallToolRequest("queryWeather", Map.of("city", "北京")));
            callTool
                .content()
                .forEach(
                    content -> {
                      if (content instanceof McpSchema.TextContent textContent) {
                        log.info("天气查询结果：" + textContent.text());
                      }
                    });

            List<String> resources =
                mcpSyncClient.listResources().resources().stream()
                    .map(McpSchema.Resource::name)
                    .toList();
            log.info("调用资源：" + resources);

            // 调用资源
            McpSchema.ReadResourceRequest request =
                new McpSchema.ReadResourceRequest("/v1/api/query");
            McpSchema.ReadResourceResult result = mcpSyncClient.readResource(request);
            log.info("调用资源：" + result.contents());

            McpSchema.Root root = new McpSchema.Root("http://127.0.0.1", "端点");
            mcpSyncClient.addRoot(root);
          });
    };
  }

  /**
   * CustomMcpSyncClientCustomizer
   *
   * @author haijun
   * @date 2025-11-26 18:04:57
   * @version 1.0.0-SNAPSHOT
   * @since 1.0.0
   */
  @Component
  public static class CustomMcpSyncClientCustomizer implements McpSyncClientCustomizer {

    /** log */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Customize
     *
     * @param serverConfigurationName server configuration name
     * @param spec spec 1.0.0-SNAPSHOT
     */
    @Override
    public void customize(String serverConfigurationName, McpClient.SyncSpec spec) {

      // Customize the request timeout configuration
      spec.requestTimeout(Duration.ofSeconds(30));
      McpSchema.ClientCapabilities clientCapabilities =
          McpSchema.ClientCapabilities.builder()
              .experimental(Map.of())
              .roots(true)
              .sampling()
              .build();
      spec.capabilities(clientCapabilities);

      spec.sampling(
          (McpSchema.CreateMessageRequest messageRequest) -> {
            List<McpSchema.SamplingMessage> messages = messageRequest.messages();
            McpSchema.CreateMessageResult messageResult = null;
            for (McpSchema.SamplingMessage message : messages) {
              McpSchema.Content content = message.content();
              if ("text".equals(content.type())) {
                McpSchema.TextContent textContent = (McpSchema.TextContent) content;
                log.info("收到服务端的回调函数：" + textContent.text());

                messageResult =
                    McpSchema.CreateMessageResult.builder()
                        .message("我确认：" + textContent.text())
                        .build();
              }
            }
            return messageResult;
          });

      spec.toolsChangeConsumer((List<McpSchema.Tool> tools) -> {});

      spec.resourcesChangeConsumer((List<McpSchema.Resource> resources) -> {});

      spec.promptsChangeConsumer((List<McpSchema.Prompt> prompts) -> {});

      // Adds a consumer to be notified when logging messages are received from the server.
      spec.loggingConsumer(
          (McpSchema.LoggingMessageNotification log) -> {
            System.out.println("消息提醒：" + log.data());
          });
    }
  }
}
