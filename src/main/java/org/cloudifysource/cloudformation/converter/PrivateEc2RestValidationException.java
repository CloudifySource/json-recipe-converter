/*******************************************************************************
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 ******************************************************************************/
package org.cloudifysource.cloudformation.converter;

import org.springframework.validation.BindingResult;

/**
 * Thrown when a validation fails.
 * 
 * @author victor
 * 
 */
public class PrivateEc2RestValidationException extends Exception {

	private static final long serialVersionUID = 1L;

	private BindingResult bindingResult;

	public PrivateEc2RestValidationException(final BindingResult bindingResult) {
		super(bindingResult.toString());
		this.bindingResult = bindingResult;
	}

	public BindingResult getBindingResult() {
		return bindingResult;
	}
}
