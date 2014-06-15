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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.csc.kmr.conserve.util.TemplateHTMLEscaper;
import se.kth.csc.kmr.conserve.util.TemplateManager;
import se.kth.csc.kmr.conserve.util.TemplateLayout.ParamFilter;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * Guice module for the OAuth 1.0 implementation. Implementations of OAuth1Store
 * and OAuthPrincipal need to be bound separately.
 * 
 * @author Erik Isaksson <erikis@kth.se>
 * 
 */
public class OAuth1Module extends AbstractModule {

	private static final Logger log = LoggerFactory
			.getLogger(OAuth1Module.class);

	@Override
	protected void configure() {
		log.info("Configuring OAuth 1.0 module");

		binder().bind(OAuth1.class);
		binder().bind(OAuth1Endpoints.class);

		TypeLiteral<Map<String, ParamFilter>> oauthAttrType = new TypeLiteral<Map<String, ParamFilter>>() {
		};
		Map<String, ParamFilter> oauthAttrMap = new HashMap<String, ParamFilter>();
		oauthAttrMap.put("scope", new ParamFilter() {
			@Override
			public Object filter(Object value, Locale locale) {
				return ((String) value).split("\\s");
			}
		});
		binder().bind(oauthAttrType)
				.annotatedWith(Names.named("oauth.attributes"))
				.toInstance(oauthAttrMap);

		binder().bind(TemplateManager.class)
				.annotatedWith(Names.named("oauth"))
				.toProvider(new Provider<TemplateManager>() {
					@Inject
					@Named("staticrs")
					Map<String, String> staticrs;

					@Override
					public TemplateManager get() {
						return new TemplateManager("templates.contemp")
								.renderer(new TemplateHTMLEscaper())
								.map("static", staticrs).newLayout("standard")
								.literal("title").include("body").build();
					}
				}).in(Scopes.SINGLETON);

	}

}
