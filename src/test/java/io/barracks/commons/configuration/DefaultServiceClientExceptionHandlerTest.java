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

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.jirutka.spring.exhandler.messages.ErrorMessage;
import io.barracks.commons.exceptions.BarracksServiceClientException;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

/**
 * Created by saiimons on 31/08/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultServiceClientExceptionHandlerTest {
    @Mock
    private HttpServletRequest request;
    private BarracksServiceClientExceptionHandler<BarracksServiceClientException> handler;

    @Before
    public void setUp() {
        final ExceptionHandlingConfiguration configuration = new TestConfiguration();
        handler = new DefaultServiceClientExceptionHandler();
        handler.setMessageSource(configuration.httpErrorMessageSource());
    }

    @Test
    public void parseException_whenNotFormatted_shouldUseResponseBody() throws UnsupportedEncodingException {
        // Given
        final HttpStatus status = HttpStatus.BAD_REQUEST;
        final String body = "BAD REQUEST";
        final String uri = "http://not.barracks.io/service/method";
        BarracksServiceClientException exception = new TestException(new HttpClientErrorException(status, status.getReasonPhrase(), body.getBytes("UTF-8"), Charset.forName("UTF-8")));
        doReturn(uri).when(request).getRequestURI();

        // When
        ErrorMessage result = handler.createBody(exception, request);

        // Then
        assertThat(result.getStatus()).isEqualTo(status.value());
        assertThat(result.getTitle()).isEqualTo(status.getReasonPhrase());
        assertThat(result.getDetail()).isEqualTo(body);
    }

    @Test
    public void parseException_whenAlreadyFormatted_shouldTheSame() throws Exception {
        // Given
        final JSONObject object = getJsonFromResource("errorMessage");
        final HttpStatus status = HttpStatus.valueOf(object.getAsNumber("status").intValue());
        BarracksServiceClientException exception = new TestException(
                new HttpClientErrorException(
                        status,
                        status.getReasonPhrase(),
                        object.toJSONString().getBytes("UTF-8"),
                        Charset.forName("UTF-8")

                )
        );

        // When
        ErrorMessage result = handler.createBody(exception, request);

        // Then
        assertThat(result.getStatus()).isEqualTo(status.value());
        assertThat(result.getTitle()).isEqualTo(object.getAsString("title"));
        assertThat(result.getDetail()).isEqualTo(object.getAsString("detail"));
        assertThat(result.getType().toString()).isEqualTo(object.getAsString("type"));
    }

    @Test
    public void extendedErrorMessage_shouldConvertBothWays() throws Exception {
        // Given
        ObjectMapper mapper = new ObjectMapper();
        final JSONObject original = getJsonFromResource("errorMessage");
        ErrorMessage message = mapper.readValue(original.toJSONString(), ExtendedErrorMessage.class);
        String mapped = mapper.writeValueAsString(message);
        final JSONParser jsonParser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
        // When
        final JSONObject result = (JSONObject) jsonParser.parse(mapped);
        // Then
        assertThat(result).isEqualTo(original);
    }

    private JSONObject getJsonFromResource(String name) throws IOException, ParseException {
        final String fileName = getClass().getSimpleName() + "-" + name + ".json";
        try (
                final InputStream inputStream = getClass().getResourceAsStream(fileName);
                final InputStreamReader isr = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                final BufferedReader reader = new BufferedReader(isr)
        ) {
            final String fileContent = reader.lines().reduce("", (a, b) -> a + b);
            final JSONParser jsonParser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
            return (JSONObject) jsonParser.parse(fileContent);
        }
    }

    private static final class TestException extends BarracksServiceClientException {

        public TestException(HttpStatusCodeException cause) {
            super(cause);
        }
    }

    private static class TestConfiguration extends ExceptionHandlingConfiguration {
        @Override
        protected String getBaseName() {
            return "classpath:/io/barracks/test";
        }
    }
}
