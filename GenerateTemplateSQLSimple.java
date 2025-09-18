import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenerateTemplateSQLSimple {
    
    public static void main(String[] args) throws IOException {
        // 读取 JSON 文件
        String jsonContent = new String(Files.readAllBytes(Paths.get("enlighten-platform-biz/src/main/resources/template.json")));
        
        System.out.println("-- 插入模板数据");
        System.out.println("-- 生成时间: " + java.time.LocalDateTime.now());
        System.out.println();
        
        // 提取 header 和 footer
        String header = extractJsonValue(jsonContent, "\"header\"");
        String footer = extractJsonValue(jsonContent, "\"footer\"");
        
        // 处理通用模板 (common)
        processTemplates(jsonContent, "common", null, "通用", header, footer);
        
        // 处理妇科模板 (gynecology)  
        processTemplates(jsonContent, "gynecology", "妇科", "妇科", header, footer);
    }
    
    private static void processTemplates(String jsonContent, String sectionKey, String department, String namePrefix, String header, String footer) {
        // 查找对应section的内容
        String sectionPattern = "\"" + sectionKey + "\"\\s*:\\s*\\{([^}]+(?:\\{[^}]*\\}[^}]*)*)\\}";
        Pattern pattern = Pattern.compile(sectionPattern, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(jsonContent);
        
        if (!matcher.find()) {
            System.out.println("-- 未找到 " + sectionKey + " 部分");
            return;
        }
        
        String sectionContent = matcher.group(1);
        
        // 查找所有 template_ 开头的模板
        Pattern templatePattern = Pattern.compile("\"(template_\\d+)\"\\s*:\\s*\\{([^}]+(?:\\{[^}]*\\}[^}]*)*)\\}", Pattern.DOTALL);
        Matcher templateMatcher = templatePattern.matcher(sectionContent);
        
        int sort = 1;
        while (templateMatcher.find()) {
            String templateKey = templateMatcher.group(1);
            String templateContent = templateMatcher.group(2);
            
            // 构建模板名称
            String templateName = namePrefix + "模板" + templateKey.substring(9); // 去掉 "template_" 前缀
            
            // 提取各个字段
            String text = extractTemplateField(templateContent, "text");
            String image = extractTemplateField(templateContent, "image");
            String singleTitle = extractTemplateField(templateContent, "single_title");
            String doubleTitle = extractTemplateField(templateContent, "double_title");
            String textCard = extractTemplateField(templateContent, "text_card");
            String blockCard = extractTemplateField(templateContent, "block_card");
            String numberedTitle = extractTemplateField(templateContent, "numbered_title");
            
            // 生成 INSERT 语句
            System.out.println("INSERT INTO `typesetting_template` (");
            System.out.println("    `name`, `hospital`, `department`, `tag`, `sort`,");
            System.out.println("    `header`, `footer`, `text`, `image`, `single_title`,");
            System.out.println("    `double_title`, `text_card`, `block_card`, `numbered_title`");
            System.out.println(") VALUES (");
            System.out.printf("    '%s', '赣州人民医院', %s, NULL, %d,%n", 
                templateName, 
                department != null ? "'" + department + "'" : "NULL", 
                sort);
            System.out.printf("    %s, %s, %s, %s, %s,%n",
                header != null ? "'" + escapeSQL(header) + "'" : "NULL",
                footer != null ? "'" + escapeSQL(footer) + "'" : "NULL",
                text != null ? "'" + escapeSQL(text) + "'" : "NULL",
                image != null ? "'" + escapeSQL(image) + "'" : "NULL",
                singleTitle != null ? "'" + escapeSQL(singleTitle) + "'" : "NULL");
            System.out.printf("    %s, %s, %s, %s%n",
                doubleTitle != null ? "'" + escapeSQL(doubleTitle) + "'" : "NULL",
                textCard != null ? "'" + escapeSQL(textCard) + "'" : "NULL",
                blockCard != null ? "'" + escapeSQL(blockCard) + "'" : "NULL",
                numberedTitle != null ? "'" + escapeSQL(numberedTitle) + "'" : "NULL");
            System.out.println(");");
            System.out.println();
            
            sort++;
        }
    }
    
    private static String extractJsonValue(String jsonContent, String key) {
        Pattern pattern = Pattern.compile(key + "\\s*:\\s*\"([^\"]+(?:\\\\.[^\"]*)*?)\"", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(jsonContent);
        if (matcher.find()) {
            return unescapeJson(matcher.group(1));
        }
        return null;
    }
    
    private static String extractTemplateField(String templateContent, String fieldName) {
        Pattern pattern = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*\"([^\"]+(?:\\\\.[^\"]*)*?)\"", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(templateContent);
        if (matcher.find()) {
            return unescapeJson(matcher.group(1));
        }
        return null;
    }
    
    private static String unescapeJson(String value) {
        if (value == null) return null;
        return value.replace("\\\"", "\"")
                   .replace("\\\\", "\\")
                   .replace("\\n", "\n")
                   .replace("\\r", "\r")
                   .replace("\\t", "\t");
    }
    
    private static String escapeSQL(String value) {
        if (value == null) {
            return null;
        }
        // 转义单引号（用双单引号）
        return value.replace("'", "''");
    }
}
