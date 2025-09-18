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
        // 使用更简单的方法查找section
        String sectionStart = "\"" + sectionKey + "\"";
        int sectionStartIndex = jsonContent.indexOf(sectionStart);
        if (sectionStartIndex == -1) {
            System.out.println("-- 未找到 " + sectionKey + " 部分");
            return;
        }
        
        System.out.println("-- 处理 " + sectionKey + " 部分，找到位置: " + sectionStartIndex);
        
        // 查找section开始的花括号
        int openBraceIndex = jsonContent.indexOf("{", sectionStartIndex);
        if (openBraceIndex == -1) {
            System.out.println("-- " + sectionKey + " 格式错误");
            return;
        }
        
        // 找到匹配的结束花括号，忽略字符串中的花括号
        int braceCount = 1;
        int currentIndex = openBraceIndex + 1;
        int closeBraceIndex = -1;
        boolean inString = false;
        
        while (currentIndex < jsonContent.length() && braceCount > 0) {
            char c = jsonContent.charAt(currentIndex);
            
            if (c == '"' && !isEscaped(jsonContent, currentIndex)) {
                inString = !inString;
            } else if (!inString) {
                if (c == '{') {
                    braceCount++;
                } else if (c == '}') {
                    braceCount--;
                    if (braceCount == 0) {
                        closeBraceIndex = currentIndex;
                        break;
                    }
                }
            }
            currentIndex++;
        }
        
        if (closeBraceIndex == -1) {
            System.out.println("-- " + sectionKey + " 格式错误：未找到结束花括号");
            return;
        }
        
        String sectionContent = jsonContent.substring(openBraceIndex + 1, closeBraceIndex);
        
        // 查找所有 template_ 开头的模板
        Pattern templatePattern = Pattern.compile("\"(template_\\d+)\"\\s*:\\s*\\{", Pattern.DOTALL);
        Matcher templateMatcher = templatePattern.matcher(sectionContent);
        
        int sort = 1;
        int templateCount = 0;
        while (templateMatcher.find()) {
            templateCount++;
            String templateKey = templateMatcher.group(1);
            int templateStart = templateMatcher.end() - 1; // 回到 {
            
            // 找到这个template的结束位置，忽略字符串中的花括号
            int templateBraceCount = 1;
            int templateCurrentIndex = templateStart + 1;
            int templateEnd = -1;
            boolean templateInString = false;
            
            while (templateCurrentIndex < sectionContent.length() && templateBraceCount > 0) {
                char c = sectionContent.charAt(templateCurrentIndex);
                
                if (c == '"' && !isEscaped(sectionContent, templateCurrentIndex)) {
                    templateInString = !templateInString;
                } else if (!templateInString) {
                    if (c == '{') {
                        templateBraceCount++;
                    } else if (c == '}') {
                        templateBraceCount--;
                        if (templateBraceCount == 0) {
                            templateEnd = templateCurrentIndex;
                            break;
                        }
                    }
                }
                templateCurrentIndex++;
            }
            
            if (templateEnd == -1) continue;
            
            String templateContent = sectionContent.substring(templateStart + 1, templateEnd);
            
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
        
        System.out.println("-- " + sectionKey + " 部分共找到 " + templateCount + " 个模板");
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
        // 查找字段名
        String fieldStart = "\"" + fieldName + "\"";
        int fieldIndex = templateContent.indexOf(fieldStart);
        if (fieldIndex == -1) {
            return null;
        }
        
        // 查找冒号后的引号
        int colonIndex = templateContent.indexOf(":", fieldIndex);
        if (colonIndex == -1) {
            return null;
        }
        
        // 跳过空白字符到引号
        int currentIndex = colonIndex + 1;
        while (currentIndex < templateContent.length() && Character.isWhitespace(templateContent.charAt(currentIndex))) {
            currentIndex++;
        }
        
        if (currentIndex >= templateContent.length() || templateContent.charAt(currentIndex) != '"') {
            return null;
        }
        
        // 开始解析字符串内容
        currentIndex++; // 跳过开始的引号
        StringBuilder result = new StringBuilder();
        
        while (currentIndex < templateContent.length()) {
            char c = templateContent.charAt(currentIndex);
            
            if (c == '\\') {
                // 处理转义字符
                if (currentIndex + 1 < templateContent.length()) {
                    char nextChar = templateContent.charAt(currentIndex + 1);
                    if (nextChar == '"') {
                        result.append('"');
                        currentIndex += 2;
                    } else if (nextChar == '\\') {
                        result.append('\\');
                        currentIndex += 2;
                    } else if (nextChar == 'n') {
                        result.append('\n');
                        currentIndex += 2;
                    } else if (nextChar == 'r') {
                        result.append('\r');
                        currentIndex += 2;
                    } else if (nextChar == 't') {
                        result.append('\t');
                        currentIndex += 2;
                    } else {
                        result.append(c);
                        currentIndex++;
                    }
                } else {
                    result.append(c);
                    currentIndex++;
                }
            } else if (c == '"') {
                // 字符串结束
                break;
            } else {
                result.append(c);
                currentIndex++;
            }
        }
        
        return result.toString();
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
    
    private static boolean isEscaped(String str, int index) {
        if (index == 0) return false;
        
        int backslashCount = 0;
        int i = index - 1;
        while (i >= 0 && str.charAt(i) == '\\') {
            backslashCount++;
            i--;
        }
        
        // 如果反斜杠数量是奇数，则字符被转义
        return backslashCount % 2 == 1;
    }
}
