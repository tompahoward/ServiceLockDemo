package service.lock.demo;

import java.util.HashMap;
import java.util.Map;

import org.ff4j.web.FF4jInitServlet;
import org.ff4j.web.embedded.ConsoleConstants;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

@Configuration
public class ServiceLockDemoConfiguration {

    @Bean
    public ServletRegistrationBean ff4jServletRegistration() {
        ServletRegistrationBean registration = new ServletRegistrationBean(
                new FF4jInitServlet(), "/ff4j-console/*");
        Map<String, String> params = new HashMap<String, String>();
        params.put(ConsoleConstants.PROVIDER_PARAM_NAME,
                "service.lock.demo.Provider");
        registration.setInitParameters(params);
        return registration;
    }

    @Bean
    public ServletRegistrationBean dispatcherServletRegistration() {
        AnnotationConfigWebApplicationContext servletContext = new AnnotationConfigWebApplicationContext();

        // servletContext.register(ServletConfig.class);

        ServletRegistrationBean registration = new ServletRegistrationBean(
                new DispatcherServlet(servletContext));

        Map<String, String> params = new HashMap<String, String>();
        params.put("contextClass",
                "org.springframework.web.context.support.AnnotationConfigWebApplicationContext");
        registration.setInitParameters(params);

        return registration;
    }
}
