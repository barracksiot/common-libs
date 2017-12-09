/*
 * MIT License
 *
 * Copyright (c) 2017 Barracks Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.barracks.commons.configuration;

import cz.jirutka.spring.exhandler.RestHandlerExceptionResolver;
import cz.jirutka.spring.exhandler.RestHandlerExceptionResolverBuilder;
import cz.jirutka.spring.exhandler.support.HttpMessageConverterUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import java.util.List;

/**
 * Created by saiimons on 22/08/2016.
 */
public abstract class ExceptionHandlingConfiguration extends WebMvcConfigurerAdapter {
    public ExceptionHandlingConfiguration() {
    }

    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        resolvers.add(exceptionHandlerExceptionResolver()); // resolves @ExceptionHandler
        resolvers.add(restExceptionResolver().build());
    }

    @Bean
    public RestHandlerExceptionResolverBuilder restExceptionResolver() {
        return RestHandlerExceptionResolver.builder()
                .messageSource(httpErrorMessageSource())
                .defaultContentType(MediaType.APPLICATION_JSON)
                .addHandler(new BindExceptionHandler())
                .addHandler(new MethodArgumentNotValidHandler())
                .addHandler(new DefaultServiceClientExceptionHandler())
                .addErrorMessageHandler(IllegalArgumentException.class, HttpStatus.BAD_REQUEST);
    }

    @Bean
    public MessageSource httpErrorMessageSource() {
        ReloadableResourceBundleMessageSource common = new ReloadableResourceBundleMessageSource();
        common.setBasename("classpath:/io/barracks/commons/configuration/messages");
        common.setFallbackToSystemLocale(false);
        common.setDefaultEncoding("UTF-8");
        String baseName = getBaseName();
        if (baseName != null) {
            ReloadableResourceBundleMessageSource m = new ReloadableResourceBundleMessageSource();
            m.setBasename(getBaseName());
            m.setDefaultEncoding("UTF-8");
            m.setParentMessageSource(common);
            return m;
        } else {
            return common;
        }
    }

    protected String getBaseName() {
        return null;
    }

    @Bean
    public ExceptionHandlerExceptionResolver exceptionHandlerExceptionResolver() {
        ExceptionHandlerExceptionResolver resolver = new ExceptionHandlerExceptionResolver();
        resolver.setMessageConverters(HttpMessageConverterUtils.getDefaultHttpMessageConverters());
        return resolver;
    }

}
