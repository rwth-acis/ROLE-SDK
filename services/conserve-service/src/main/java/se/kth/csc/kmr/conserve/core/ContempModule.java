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
package se.kth.csc.kmr.conserve.core;

import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.rio.RDFWriterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.csc.kmr.conserve.data.jpa.JPAModule;
import se.kth.csc.kmr.conserve.security.SecurityInfo;
import se.kth.csc.kmr.conserve.util.RDFJSONParserFactory;
import se.kth.csc.kmr.conserve.util.RDFJSONWriterFactory;
import se.kth.csc.kmr.conserve.util.TemplateHTMLEscaper;
import se.kth.csc.kmr.conserve.util.TemplateManager;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletScopes;

public class ContempModule extends AbstractModule {

	private static Logger log = LoggerFactory.getLogger(ContempModule.class);

	@Override
	protected void configure() {
		install(new JPAModule());

		log.info("Configuring Contemp module");

		binder().bind(SecurityInfo.class).in(ServletScopes.REQUEST);

		binder().bind(TemplateManager.class)
				.annotatedWith(Names.named("contemp"))
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

		binder().bind(UUID.class)
				.annotatedWith(Names.named("conserve.session.context"))
				.toInstance(
						UUID.fromString("b465abcf-42e6-4462-b842-f7f1bd859647"));
		binder().bind(UUID.class)
				.annotatedWith(
						Names.named("conserve.oauth.request.token.context"))
				.toInstance(
						UUID.fromString("3a44fe08-9212-4d6b-8de6-6e3175c24924"));
		binder().bind(UUID.class)
				.annotatedWith(
						Names.named("conserve.oauth.access.token.context"))
				.toInstance(
						UUID.fromString("07611a99-22a9-4115-b75b-a96be5182b97"));
		
		binder().bind(UUID.class)
				.annotatedWith(
						Names.named("conserve.oidc.context"))
				.toInstance(
						UUID.fromString("911a07a5-f5dd-403d-9b74-0d4921c19fda"));
		
		binder().bind(UUID.class)
				.annotatedWith(
						Names.named("conserve.oidc.predicate"))
				.toInstance(
						UUID.fromString("6bae17cc-4e6c-496d-ab3d-c6a5f009ee6e"));

		Contapp.newResponderBinder(binder());
		Contapp.newGuardBinder(binder());
		Contapp.newListenerBinder(binder());
		Contapp.newInitializerBinder(binder()).addBinding()
				.to(ConserveTerms.class);

		RDFWriterRegistry writerRegistry = RDFWriterRegistry.getInstance();
		writerRegistry.add(new RDFJSONWriterFactory());
		RDFParserRegistry parserRegistry = RDFParserRegistry.getInstance();
		parserRegistry.add(new RDFJSONParserFactory());
	}

}
