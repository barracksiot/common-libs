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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.jirutka.spring.exhandler.messages.ErrorMessage;
import io.barracks.commons.exceptions.BarracksServiceClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by saiimons on 14/09/2016.
 */
public class DefaultServiceClientExceptionHandler extends BarracksServiceClientExceptionHandler<BarracksServiceClientException> {
    private final ObjectMapper mapper;
    private final Logger logger = LoggerFactory.getLogger(BarracksServiceClientExceptionHandler.class);


    public DefaultServiceClientExceptionHandler() {
        super();
        mapper = new ObjectMapper();
    }


    @Override
    public ErrorMessage createBody(BarracksServiceClientException ex, HttpServletRequest req) {
        String message = ex.getCause().getResponseBodyAsString();
        try {
            ErrorMessage mapped = mapper.readValue(message, ExtendedErrorMessage.class);
            if (mapped.getStatus() != null) {
                return mapped;
            }
        } catch (IOException e) {
            logger.warn("Failed to parse client exception returned by {}", req.getRequestURI(), ex);
        }
        ErrorMessage errorMessage = new ErrorMessage(super.createBody(ex, req));
        errorMessage.setStatus(ex.getCause().getStatusCode());
        errorMessage.setTitle(ex.getCause().getStatusText());
        errorMessage.setDetail(message);
        return errorMessage;
    }

}
