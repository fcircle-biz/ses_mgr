package jp.co.example.sesapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.Locale;

/**
 * WebMVC設定クラス.
 * Spring MVCの設定をカスタマイズします。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * ビューコントローラーの登録.
     * コントローラーを作成せずに、URLとビューのマッピングを行います。
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/dashboard");
        registry.addViewController("/login").setViewName("auth/login");
        registry.addViewController("/access-denied").setViewName("error/403");
    }

    /**
     * 静的リソースハンドラーの設定.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }

    /**
     * ロケールリゾルバーの設定.
     * ユーザーのロケール（言語・地域）をCookieに基づいて解決します。
     *
     * @return CookieLocaleResolver
     */
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver("locale");
        resolver.setDefaultLocale(Locale.JAPAN);
        resolver.setCookieMaxAge(3600); // 1時間
        return resolver;
    }

    /**
     * ロケール変更インターセプターの設定.
     * ?lang=jaのようなリクエストパラメータでロケールを変更可能にします。
     *
     * @return LocaleChangeInterceptor
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    /**
     * インターセプターの登録.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}