package cn.cxdproject.coder.common.generate;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

import java.io.StringWriter;
import java.util.Map;

public class FreeMarkerTemplateHelper {
    private final Configuration config;

    public FreeMarkerTemplateHelper() {
        config = new Configuration(Configuration.VERSION_2_3_31);
        config.setClassLoaderForTemplateLoading(getClass().getClassLoader(), "templates");
        config.setDefaultEncoding("UTF-8");
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    public String render(String templateName, Map<String, Object> model) {
        try {
            Template template = config.getTemplate(templateName + ".ftl");
            StringWriter writer = new StringWriter();
            template.process(model, writer);
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException("模板渲染失败：" + e.getMessage(), e);
        }
    }
}
