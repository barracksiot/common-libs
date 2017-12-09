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

package io.barracks.commons.integration.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.jirutka.spring.exhandler.messages.ErrorMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by saiimons on 14/09/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration({Application.class, ExtendedExceptionConfiguration.class})
@WebIntegrationTest(randomPort = true)
public class ExtendedHandlerConfigurationTest {
    @Value("${local.server.port}")
    int port;
    private RestTemplate restTemplate = new RestTemplate();

    protected String getBaseUrl() {
        return "http://localhost:" + this.port;
    }

    @Test
    public void subclassHandler_shouldPrevailOnSuperClass() throws IOException {
        try {
            restTemplate.getForObject(getBaseUrl() + "/test", String.class);
        } catch (HttpStatusCodeException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            ObjectMapper mapper = new ObjectMapper();
            ErrorMessage message = mapper.readValue(e.getResponseBodyAsString(), ErrorMessage.class);
            assertThat(message.getTitle()).isEqualTo(ExtendedExceptionHandler.class.getSimpleName());
        }
    }
}
