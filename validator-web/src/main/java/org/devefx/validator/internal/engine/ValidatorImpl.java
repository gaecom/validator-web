/*
 * Copyright 2016-2017, Youqian Yue (devefx@163.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.devefx.validator.internal.engine;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.devefx.validator.ConstraintViolation;
import org.devefx.validator.InvalidHandler;
import org.devefx.validator.ValidationContext;
import org.devefx.validator.ValidationException;
import org.devefx.validator.Validator;
import org.devefx.validator.ValidatorContext;
import org.devefx.validator.ValidatorDelegate;
import org.devefx.validator.ValidatorFactory;
import org.devefx.validator.ValueContext;
import org.devefx.validator.http.extract.HttpMessageReaderExtractor;
import org.devefx.validator.http.extract.RequestExtractor;
import org.devefx.validator.util.Assert;
import org.devefx.validator.web.View;

public class ValidatorImpl implements Validator {
	
	private final RequestExtractor requestExtractor;
	private final Map<AnnotatedElement, ValidElement> validElementCache;
	private final ValidatorContext validatorContext;
	
	protected ValidatorImpl(ValidatorFactory validatorFactory) {
		this.validatorContext = new ValidatorContext(validatorFactory);
		this.requestExtractor = new HttpMessageReaderExtractor(validatorFactory.getMessageReaders());
		this.validElementCache = new ConcurrentHashMap<>();
	}
	
	@Override
	public ValidatorContext getValidatorContext() {
		return validatorContext;
	}
	
	@Override
	public boolean validate(AnnotatedElement validElement,
			HttpServletRequest request, HttpServletResponse response) {
		Assert.notNull(validElement, "validElement must not be null.");
		
		ValidElement element = getOrCreateValidElement(validElement);
		if (!element.isConstrained()) {
			return true;
		}
		
		ValidationContext.Accessor validationContext = validatorContext.getOrCreateValidationContext(element.getValidationClass());
		ValueContext valueContext = createValueContext(
				request,
				element.getRequestType());
		
		return validateInContext(validationContext, valueContext, element.getGroups(), request, response);
	}
	
	private boolean validateInContext(ValidationContext.Accessor context, ValueContext valueContext, Class<?>[] groups,
            HttpServletRequest request, HttpServletResponse response) {
		ValidatorDelegate validatorDelegate = context.getValidatorDelegate();
		
		List<ConstraintViolation> constraintViolations = validatorDelegate.validate(valueContext, context, groups);
		if (constraintViolations == null || constraintViolations.isEmpty()) {
			return true;
		}
		
		validateInvalidProcessing(context, constraintViolations, request, response);
		return false;
	}
	
	private void validateInvalidProcessing(ValidationContext.Accessor context,
			List<ConstraintViolation> constraintViolations,
			HttpServletRequest request, HttpServletResponse response) {
		InvalidHandler invalidHandler = context.getInvalidHandler();
		
		View view = invalidHandler.renderInvalid(constraintViolations);
		if (view != null) {
			try {
				view.render(request, response);
			} catch (Exception e) {
				throw new ValidationException("Unable to render:" + e.getMessage(), e);
			}
		}
	}
	
	private ValidElement getOrCreateValidElement(AnnotatedElement annotatedElement) {
		ValidElement validElement = validElementCache.get(annotatedElement);
		if (validElement == null) {
			validElement = ValidElement.by(annotatedElement);
			validElementCache.put(annotatedElement, validElement);
		}
		return validElement;
	}
	
	private ValueContext createValueContext(HttpServletRequest request, Class<?> requiredClass) {
		try {
			Object bean = requestExtractor.extractData(requiredClass, request);
			return new ValueContext(bean, requiredClass);
		} catch (IOException e) {
			throw new ValidationException("I/O error, can't extract " + requiredClass
					+ " from the request:" + e.getMessage(), e);
		}
	}
}
