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

package org.devefx.validator.constraints.multipart;

import org.devefx.validator.ConstraintValidator;
import org.devefx.validator.constraints.annotation.InitParam;
import org.devefx.validator.script.annotation.Script;
import org.devefx.validator.web.multipart.MultipartFile;

@Script
public class MultipartSize implements ConstraintValidator {

	@InitParam
	private int min;
	@InitParam
	private int max;
	
	public MultipartSize(int max) {
		this(1, max);
	}
	
	public MultipartSize(int min, int max) {
		this.min = min;
		this.max = max;
		validateParameters();
	}
	
	@Override
	public boolean isValid(Object value) {
		// null values are valid
		if (value == null) {
			return true;
		}
		// converter type
		MultipartFile file;
		if (value instanceof MultipartFile) {
			file = (MultipartFile) value;
		} else {
			throw new IllegalArgumentException("Unsupported of type [" + value.getClass().getName() + "]");
		}
		long size = file.getSize();
		return size >= min && size <= max;
	}
	
	private void validateParameters() {
		if (min < 1) {
			throw new IllegalArgumentException("The min parameter cannot be less than 1.");
		}
		if (max < 1) {
			throw new IllegalArgumentException("The max parameter cannot be less than 1.");
		}
		if (max < min) {
			throw new IllegalArgumentException("The length cannot be negative.");
		}
	}
}
