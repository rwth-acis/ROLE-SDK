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
package se.kth.csc.kmr.conserve;

import java.util.List;
import java.util.UUID;

import se.kth.csc.kmr.conserve.dsl.ContempDSL;

public interface Contemp {

	ContempDSL query();

	void disconnect();

	void rollback();

	void endEntry();

	UUID getRootUuid();

	Concept createConcept(UUID context, UUID predicate);

	Concept createConcept(UUID context, UUID predicate, UUID uuid);

	void putConcept(Concept concept);

	void deleteConcept(Concept concept);

	Concept loadConcept(UUID uuid);

	Concept getConcept(UUID uuid);

	Concept getConcept(UUID context, UUID uuid);

	Concept getConcept(UUID context, String id);

	Concept getConcept(UUID context, UUID predicate, String id);

	Content createContent(UUID uuid, UUID topic);

	void putContent(Content content);

	void deleteContent(Content content);

	Content getContent(UUID uuid, UUID topic);

	Content loadContent(UUID uuid, UUID topic);

	List<Concept> getConcepts(UUID context);

	List<Content> getContents(UUID concept);

	Control createControl(UUID subject, UUID predicate, UUID object, String uri);

	Control getControl(UUID subject, UUID predicate, UUID object);

	List<Control> getControls(UUID subject);

	List<Control> getControls(UUID subject, UUID predicate);

	List<Control> getControls(UUID subject, UUID predicate, UUID object);

	List<Control> getControls(UUID subject, UUID predicate1, UUID predicate2,
			UUID object);

	void putControl(Control control);

	void deleteControl(Control control);

}
