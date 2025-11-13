package cn.cxdproject.coder.common.generate;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.*;


public class CodeGenerateService {
    public static void main(String[] args) throws Exception {
        CodeGenerateService service = new CodeGenerateService();

        DatabaseConfig config = new DatabaseConfig();
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setUrl("jdbc:mysql://cd-cynosdbmysql-grp-lfa6zfg0.sql.tencentcdb.com:23771/co_code100_db");
        config.setUsername("root");
        config.setPassword("ct288513832##");

        // 获取数据库中的所有表
        List<String> tables = service.getAllTables(config);
        System.out.println("数据库中包含以下表：");
        for (int i = 0; i < tables.size(); i++) {
            System.out.println((i + 1) + ". " + tables.get(i));
        }

        // 用户选择要生成的表（支持输入多个用逗号分隔）
        System.out.println("请输入要生成代码的表名（支持多个，逗号分隔）：");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        String[] selectedTables = input.split(",");

        String packageName = "cn.code100.coder";
        String baseDir = System.getProperty("user.dir") + "/src/main/java";

        for (String tableNameRaw : selectedTables) {
            String tableName = tableNameRaw.trim();
            String simpleName = extractMeaningfulName(tableName);
            String className = NameConverter.toPascalCaseSingular(simpleName);

            List<TableColumn> columns = service.getColumns(config, tableName, true);
            System.out.println("生成代码中：" + tableName);

            String controllerCode = service.generateController(packageName, className);
            service.writeToFile(baseDir, packageName + ".controller", className + "Controller", controllerCode);

            String entityCode = service.generateEntity(packageName + ".model.entity", tableName, className, columns);
            service.writeToFile(baseDir, packageName + ".model.entity", className, entityCode);

            String serviceCode = service.generateServiceInterface(packageName, className);
            service.writeToFile(baseDir, packageName + ".service", className + "Service", serviceCode);

            String serviceImplCode = service.generateServiceImpl(packageName, className);
            service.writeToFile(baseDir, packageName + ".service.impl", className + "ServiceImpl", serviceImplCode);

            String mapperCode = service.generateMapper(packageName, className);
            service.writeToFile(baseDir, packageName + ".mapper", className + "Mapper", mapperCode);

            String mapperXmlCode = service.generateMapperXml(packageName, className, columns);
            String xmlPath = baseDir + File.separator + "resources" + File.separator + "mapper"
                    + File.separator + className + "Mapper.xml";
            service.writeToFileToPath(xmlPath, mapperXmlCode);
        }

        System.out.println("代码生成完毕！");
    }


    private final FreeMarkerTemplateHelper templateHelper = new FreeMarkerTemplateHelper();

    public List<TableColumn> getColumns(DatabaseConfig config, String tableName, Boolean camelCase) throws Exception {
        Class.forName(config.getDriverClassName());
        List<TableColumn> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(config.getUrl(), config.getUsername(), config.getPassword())) {
            DatabaseMetaData metaData = conn.getMetaData();
            // 获取主键列集合
            Set<String> primaryKeys = new HashSet<>();
            try (ResultSet pkRs = metaData.getPrimaryKeys(null, null, tableName)) {
                while (pkRs.next()) {
                    primaryKeys.add(pkRs.getString("COLUMN_NAME"));
                }
            }
            // 获取索引信息：列名 -> 索引属性
            Map<String, IndexInfo> indexMap = new HashMap<>();
            try (ResultSet indexRs = metaData.getIndexInfo(null, null, tableName, false, false)) {
                while (indexRs.next()) {
                    String columnName = indexRs.getString("COLUMN_NAME");
                    if (columnName != null) {
                        IndexInfo info = indexMap.computeIfAbsent(columnName, k -> new IndexInfo());
                        info.setIndexName(indexRs.getString("INDEX_NAME"));
                        info.setUnique(!indexRs.getBoolean("NON_UNIQUE"));
                    }
                }
            }

            // 获取字段信息
            try (ResultSet rs = metaData.getColumns(null, null, tableName, null)) {
                while (rs.next()) {
                    TableColumn col = new TableColumn();
                    String columnName = rs.getString("COLUMN_NAME");
                    col.setOriginalName(columnName);
                    col.setName(camelCase ? NameConverter.toCamelCase(columnName) : columnName);
                    String fullType = SqlTypeConstants.mapSqlType(rs.getInt("DATA_TYPE")); // eg: java.time.LocalDateTime
                    String shortType = fullType.contains(".")
                            ? fullType.substring(fullType.lastIndexOf('.') + 1)
                            : fullType;
                    col.setType(shortType);
                    col.setFullType(fullType);
                    String comment = rs.getString("REMARKS");
                    if (comment == null || comment.trim().isEmpty()) {
                        comment = columnName;
                    }
                    col.setComment(comment);
                    col.setNullable(rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                    col.setLength(rs.getInt("COLUMN_SIZE"));
                    col.setPrecision(rs.getInt("COLUMN_SIZE"));
                    col.setScale(rs.getInt("DECIMAL_DIGITS"));
                    col.setPrimaryKey(primaryKeys.contains(columnName));
                    if (indexMap.containsKey(columnName)) {
                        IndexInfo index = indexMap.get(columnName);
                        col.setIndexed(true);
                        col.setUnique(index.isUnique());
                        col.setIndexName(index.getIndexName());
                    }
                    list.add(col);
                }
            }
        }
        return list;
    }


    public String generateEntity(String packageName, String tableName, String className, List<TableColumn> columns) {
        Map<String, Object> model = new HashMap<>();
        model.put("packageName", packageName);
        model.put("className", className);
        model.put("tableName", tableName);
        model.put("fields", columns);
        return templateHelper.render("entity", model);
    }

    public String generateServiceInterface(String packageName, String className) {
        Map<String, Object> model = new HashMap<>();
        model.put("packageName", packageName);
        model.put("className", className);
        return templateHelper.render("service", model);
    }

    public String generateServiceImpl(String packageName, String className) {
        Map<String, Object> model = new HashMap<>();
        model.put("packageName", packageName);
        model.put("className", className);
        return templateHelper.render("serviceImpl", model);
    }

    public String generateMapper(String packageName, String className) {
        Map<String, Object> model = new HashMap<>();
        model.put("packageName", packageName);
        model.put("className", className);
        return templateHelper.render("mapper", model);
    }

    public String generateMapperXml(String packageName, String className, List<TableColumn> fields) {
        Map<String, Object> model = new HashMap<>();
        model.put("packageName", packageName);
        model.put("className", className);
        model.put("fields", fields); // 这行非常关键！
        return templateHelper.render("mapperXml", model);
    }

    public String generateController(String packageName, String className) {
        Map<String, Object> model = new HashMap<>();
        model.put("packageName", packageName);
        model.put("className", className);
        return templateHelper.render("controller", model);
    }

    public void writeToFile(String baseDir, String packageName, String className, String code) {
        // 1. 把 packageName 转为路径：com.example.entity → com/example/entity
        String path = packageName.replace(".", File.separator);
        // 2. 拼接完整路径
        String fullPath = baseDir + File.separator + path + File.separator + className + ".java";
        // 3. 创建目录
        File file = new File(fullPath);
        file.getParentFile().mkdirs();
        // 4. 写入文件
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(code);
            System.out.println("已生成文件：" + fullPath);
        } catch (IOException e) {
            throw new RuntimeException("写文件失败：" + fullPath, e);
        }
    }

    public void writeToFileToPath(String fullPath, String content) {
        File file = new File(fullPath);
        file.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
            System.out.println("已生成文件：" + fullPath);
        } catch (IOException e) {
            throw new RuntimeException("写文件失败：" + fullPath, e);
        }
    }

    public List<String> getAllTables(DatabaseConfig config) throws Exception {
        List<String> tables = new ArrayList<>();
        Class.forName(config.getDriverClassName());
        try (Connection conn = DriverManager.getConnection(config.getUrl(), config.getUsername(), config.getPassword())) {
            DatabaseMetaData metaData = conn.getMetaData();
            String databaseName = conn.getCatalog(); // 获取当前连接的数据库名
            try (ResultSet rs = metaData.getTables(databaseName, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
            }
        }
        return tables;
    }

    public static String extractMeaningfulName(String rawTableName) {
        int index = rawTableName.indexOf("_");
        if (index != -1 && index < rawTableName.length() - 1) {
            return rawTableName.substring(index + 1);
        }
        return rawTableName;
    }
}