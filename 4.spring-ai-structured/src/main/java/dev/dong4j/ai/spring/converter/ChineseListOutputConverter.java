package dev.dong4j.ai.spring.converter;

import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.lang.NonNull;

/**
 * 中文顿号分隔列表输出转换器
 *
 * <p>该转换器继承自 ListOutputConverter，专门用于处理中文场景下的列表输出。 与默认的逗号分隔不同，该转换器使用中文顿号（、）作为分隔符，更符合中文书写习惯。
 *
 * <p>使用场景：
 *
 * <ul>
 *   <li>中文关键词列表提取
 *   <li>中文选项生成
 *   <li>中文标签或分类列表
 * </ul>
 *
 * <p>示例输出：
 *
 * <pre>
 * 苹果、香蕉、橙子、葡萄
 * </pre>
 *
 * @author ogromwang
 * @version 1.0.0
 * @email mailto:ogromwang@gmail.com
 * @date 2025.12.17
 * @since 1.0.0
 */
public class ChineseListOutputConverter extends ListOutputConverter {

    /**
     * 构造函数
     *
     * <p>使用默认的转换服务创建中文顿号分隔列表转换器
     */
    public ChineseListOutputConverter() {
        super(new DefaultConversionService());
    }

    /**
     * 获取格式指令字符串
     *
     * <p>该方法返回中文格式的指令，指导模型使用中文顿号（、）作为分隔符， 而不是默认的逗号分隔。指令明确要求不要添加编号和解释文字。
     *
     * @return 格式指令字符串
     */
    @Override
    @NonNull
    public String getFormat() {
        return """
        请返回使用中文顿号（、）分隔的列表，不要编号，不要解释，不要使用逗号。
        例如：苹果、香蕉、橙子、葡萄
        """;
    }
}
