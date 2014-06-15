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
package se.kth.csc.kmr.staticrs;

import java.util.HashMap;
import java.util.Map;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;

public class StaticRSModule extends AbstractModule {

	@Override
	protected void configure() {
		newBinder(binder()); // just to ensure the binding is created

		Map<String, String> hrefs = new HashMap<String, String>();
		binder().bind(new TypeLiteral<Map<String, String>>() {
		}).annotatedWith(Names.named("staticrs")).toInstance(hrefs);
	}

	public static MapBinder<String, Static> newBinder(Binder binder) {
		return MapBinder.newMapBinder(binder, String.class, Static.class);
	}

}
