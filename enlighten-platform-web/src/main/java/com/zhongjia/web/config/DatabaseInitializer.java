package com.zhongjia.web.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Override
    public void run(String... args) throws Exception {
        if (datasourceUrl == null || !datasourceUrl.startsWith("jdbc:mysql:")) {
            log.info("跳过数据库初始化：当前非 MySQL 或未配置数据源 URL");
            return;
        }

        ParsedMysqlUrl parsed = parseMysqlUrl(datasourceUrl);
        if (parsed.databaseName == null || parsed.databaseName.isEmpty()) {
            log.warn("数据源 URL 未包含数据库名，将仅尝试运行初始化脚本");
        }

        if (databaseExists(parsed, datasourceUsername, datasourcePassword)) {
            log.info("数据库已存在：{}，跳过初始化", parsed.databaseName);
            return;
        }

        log.warn("数据库不存在：{}，将执行初始化脚本", parsed.databaseName);
        String sql = loadInitSqlFromClasspath("db/database_init.sql");
        if (sql == null || sql.isEmpty()) {
            log.error("无法读取初始化脚本 db/database_init.sql（classpath）");
            return;
        }
        executeSqlMultiStatements(parsed, datasourceUsername, datasourcePassword, sql);

        if (databaseExists(parsed, datasourceUsername, datasourcePassword)) {
            log.info("数据库初始化完成：{}", parsed.databaseName);
        } else {
            log.error("数据库初始化后仍不可用：{}", parsed.databaseName);
        }
    }

    private static class ParsedMysqlUrl {
        String host;
        int port;
        String databaseName; // 可能为 null
        String query;        // 原 URL 的查询参数（不包含 ?）
    }

    private ParsedMysqlUrl parseMysqlUrl(String url) {
        // 形如 jdbc:mysql://host:port/dbname?param=... 或 jdbc:mysql://host:port/dbname
        String raw = url.substring("jdbc:".length()); // mysql://...
        if (!raw.startsWith("mysql://")) {
            throw new IllegalArgumentException("不支持的 MySQL URL：" + url);
        }
        String httpLike = "http://" + raw.substring("mysql://".length());
        try {
            URI uri = new URI(httpLike);
            ParsedMysqlUrl p = new ParsedMysqlUrl();
            p.host = uri.getHost();
            p.port = (uri.getPort() == -1) ? 3306 : uri.getPort();
            String path = uri.getPath();
            if (path != null && path.length() > 1) {
                p.databaseName = path.substring(1);
            } else {
                p.databaseName = null;
            }
            String query = uri.getQuery();
            p.query = (query == null) ? "" : query;
            return p;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("解析 MySQL URL 失败：" + url, e);
        }
    }

    private boolean databaseExists(ParsedMysqlUrl p, String user, String pass) {
        String serverUrl = buildServerJdbcUrl(p);
        String sql = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = ?";
        try (Connection conn = DriverManager.getConnection(serverUrl, user, pass);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.databaseName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            log.warn("检测数据库是否存在失败，将尝试继续初始化。原因: {}", ex.getMessage());
            return false;
        }
    }

    private String buildServerJdbcUrl(ParsedMysqlUrl p) {
        // 连接到系统库 mysql，以便可以执行 CREATE DATABASE / USE 等语句
        StringBuilder sb = new StringBuilder();
        sb.append("jdbc:mysql://").append(p.host).append(":").append(p.port).append("/mysql");
        String query = p.query;
        if (query == null || query.isEmpty()) {
            query = "allowMultiQueries=true&useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai";
        } else if (!query.contains("allowMultiQueries")) {
            query = query + "&allowMultiQueries=true";
        }
        sb.append("?").append(query);
        return sb.toString();
    }

    private String loadInitSqlFromClasspath(String classpathLocation) {
        ClassPathResource resource = new ClassPathResource(classpathLocation);
        if (!resource.exists()) {
            return null;
        }
        try (InputStream in = resource.getInputStream();
             InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        } catch (IOException e) {
            log.error("读取初始化 SQL 失败: {}", e.getMessage());
            return null;
        }
    }

    private void executeSqlMultiStatements(ParsedMysqlUrl p, String user, String pass, String sqlText) {
        String serverUrl = buildServerJdbcUrl(p);
        List<String> statements = splitSqlStatements(sqlText);
        try (Connection conn = DriverManager.getConnection(serverUrl, user, pass)) {
            conn.setAutoCommit(false);
            try (java.sql.Statement st = conn.createStatement()) {
                for (String s : statements) {
                    String trimmed = s.trim();
                    if (trimmed.isEmpty()) continue;
                    st.execute(trimmed);
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            log.error("执行初始化 SQL 失败: {}", e.getMessage());
        }
    }

    private List<String> splitSqlStatements(String sqlText) {
        // 简单按分号拆分，适用于当前脚本。若将来包含存储过程，需要更健壮的解析器。
        List<String> list = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < sqlText.length(); i++) {
            char c = sqlText.charAt(i);
            current.append(c);
            if (c == ';') {
                list.add(current.toString());
                current.setLength(0);
            }
        }
        if (current.length() > 0) {
            list.add(current.toString());
        }
        return list;
    }
}


