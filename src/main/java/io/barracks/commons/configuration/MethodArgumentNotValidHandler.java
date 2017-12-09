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
import cz.jirutka.spring.exhandler.messages.ValidationErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by saiimons on 15/09/2016.
 */
public class MethodArgumentNotValidHandler extends ErrorMessageRestExceptionHandler<MethodArgumentNotValidException> {
    protected MethodArgumentNotValidHandler() {
        super(HttpStatus.BAD_REQUEST);
    }

    @Override
    public ErrorMessage createBody(MethodArgumentNotValidException ex, HttpServletRequest req) {
        ErrorMessage template = super.createBody(ex, req);
        ValidationErrorMessage message = new ValidationErrorMessage(template);

        BindingResult result = ex.getBindingResult();
        for (ObjectError err : result.getGlobalErrors()) {
            message.addError(err.getDefaultMessage());
        }
        for (FieldError err : result.getFieldErrors()) {
            message.addError(err.getField(), err.getRejectedValue(), err.getDefaultMessage());
        }
        return message;
    }
}
