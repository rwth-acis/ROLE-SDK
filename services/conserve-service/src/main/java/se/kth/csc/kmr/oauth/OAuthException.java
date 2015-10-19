/*
 * #%L
 * Conserve Concept Server
 * %%
 * Copyright (C) 2010 - 2011 KMR
 * %%
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
 * #L%
 */
package se.kth.csc.kmr.oauth;

/**
 * 
 * @author Erik Isaksson <erikis@kth.se>
 * 
 */
public class OAuthException extends Exception {

	private static final long serialVersionUID = -5015838790370397012L;

	public OAuthException() {
		super();
	}

	public OAuthException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public OAuthException(String arg0) {
		super(arg0);
	}

	public OAuthException(Throwable arg0) {
		super(arg0);
	}

}
