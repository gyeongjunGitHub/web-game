package drowGame.drowGame.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    //view 에서 접근할 경로
    private String resourcePath = "/images/**";

    private String ec2SavePath = "file:////home/images/";
    private String windowSavePath = "file:///C:/images/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        registry.addResourceHandler(resourcePath)
                .addResourceLocations(windowSavePath);
    }
}
