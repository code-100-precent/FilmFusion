package cn.cxdproject.coder.config;

import cn.cxdproject.coder.interceptor.AdminInterceptor;
import cn.cxdproject.coder.interceptor.AuthInterceptor;
import cn.cxdproject.coder.interceptor.RegistrationInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * 文档、拦截器注册配置
 *
 * @author heathcetide
 */
@Configuration
public class WebConfig extends WebMvcConfigurationSupport {

    private final AuthInterceptor jwtInterceptor;

    private final AdminInterceptor adminInterceptor;

    private final RegistrationInterceptor registrationInterceptor;

    public WebConfig(AuthInterceptor jwtInterceptor, AdminInterceptor adminInterceptor, RegistrationInterceptor registrationInterceptor) {
        this.jwtInterceptor = jwtInterceptor;
        this.adminInterceptor = adminInterceptor;
        this.registrationInterceptor = registrationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 用户认证拦截器 - 需要登录的接口
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/interview-submission/**").excludePathPatterns("/api/interview-submission/list/**")
                .addPathPatterns("/api/courses/add/course/**","/api/courses/course/**")
                .addPathPatterns("/api/articles/**")
                .addPathPatterns("/api/admin/update","/api/admin/consecutive-days","/api/admin/upload-avatar","/api/admin/change-password","/api/admin/set-password","/api/admin/contributions")
                .addPathPatterns("/api/userfollower/**")
                .addPathPatterns("/api/userPreferences/**")
                .addPathPatterns("/api/resume/**")
                .addPathPatterns("/api/feedback/**")
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/register", "/api/admin/register/email", "/api/admin/login/**")
                .addPathPatterns("/api/visitor/info", "/api/visitor/update", "/api/visitor/logout")
                .addPathPatterns("/api/achievement/**")
                // 用户模块 - 排除登录和注册
                .addPathPatterns("/api/user/**")
                .excludePathPatterns("/api/user/login", "/api/user/register")
                // 文章模块 - 排除公开接口
                .addPathPatterns("/api/article/**")
                .excludePathPatterns("/api/article/page", "/api/article/*");
        
        // 管理员权限拦截器
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/api/admin/info")
                // 用户模块的管理员接口
                .addPathPatterns("/api/user/admin/**")
                // 文章模块的管理员接口
                .addPathPatterns("/api/article/admin/**");
        
        registry.addInterceptor(registrationInterceptor)
                .addPathPatterns("/api/admin/register/email");
    }

    @Bean
    public Docket docket() {
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("CodeForge API 文档")  // 标题
                .description("CodeForge 是一个支持博客、编程学习、插件化服务、在线课程的综合性学习平台。")  // 描述
                .version("1.0.0")  // 版本号
                .termsOfServiceUrl("https://www.codeforge.com/terms")  // 服务条款 URL
                .contact(new Contact("CodeForge 开发团队", "https://www.codeforge.com", "support@codeforge.com"))  // 联系信息
                .license("Apache 2.0")  // 许可证
                .licenseUrl("https://www.apache.org/licenses/LICENSE-2.0.html")  // 许可证 URL
                .build();

        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo)
                .select()
                //指定生成接口需要扫描的包
                .apis(RequestHandlerSelectors.basePackage("cn.cxdproject.coder.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/images/**").addResourceLocations("classpath:/static/images/");
        // 配置静态资源访问 - tmp文件夹
        // 使用绝对路径，基于项目根目录
        String projectRoot = System.getProperty("user.dir");
        registry.addResourceHandler("/api/files/**")
                .addResourceLocations("file:" + projectRoot + "/tmp/uploads/");
    }

}
