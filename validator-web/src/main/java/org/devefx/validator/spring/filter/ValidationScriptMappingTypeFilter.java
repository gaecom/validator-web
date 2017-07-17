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

package org.devefx.validator.spring.filter;

import java.io.IOException;

import org.devefx.validator.Validation;
import org.devefx.validator.script.annotation.ScriptMapping;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

public class ValidationScriptMappingTypeFilter implements TypeFilter {

	private AnnotationTypeFilter annotationTypeFilter = new AnnotationTypeFilter(ScriptMapping.class);
	private AssignableTypeFilter assignableTypeFilter = new AssignableTypeFilter(Validation.class);
	
	@Override
	public boolean match(MetadataReader metadataReader,
			MetadataReaderFactory metadataReaderFactory) throws IOException {
		return annotationTypeFilter.match(metadataReader, metadataReaderFactory) &&
				assignableTypeFilter.match(metadataReader, metadataReaderFactory);
	}
}
