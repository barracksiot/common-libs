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

import cz.jirutka.spring.exhandler.handlers.ErrorMessageRestExceptionHandler;
import cz.jirutka.spring.exhandler.messages.ErrorMessage;
import io.barracks.commons.exceptions.BarracksServiceClientException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by saiimons on 31/08/2016.
 */
public abstract class BarracksServiceClientExceptionHandler<T extends BarracksServiceClientException> extends ErrorMessageRestExceptionHandler<T> {

    public BarracksServiceClientExceptionHandler() {
        super(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<ErrorMessage> handleException(T ex, HttpServletRequest req) {
        ResponseEntity<ErrorMessage> responseEntity = super.handleException(ex, req);
        return new ResponseEntity<>(responseEntity.getBody(), responseEntity.getHeaders(), ex.getCause().getStatusCode());
    }
}
