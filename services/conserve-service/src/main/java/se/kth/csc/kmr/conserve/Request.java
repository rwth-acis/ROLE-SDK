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
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;

public interface Request {

	Map<Resolution.Type, Concept> getResolutions();

	void setResolution(Resolution.Type type, Concept context);

	Concept getRoot();

	Concept getContext();

	Concept getGuardContext();

	Concept getRelation();

	Concept getTopic();

	void setTopic(String uri);

	Concept getCreated();
	
	PathSegment getId();

	Responder resolveResponder(Concept context);

	Set<Guard.GuardContext> resolveGuard(Concept context);

	List<Resolution> getResolutionPath();

	void mapResponder(UUID fromTopic, UUID toTopic);

	Responder getResponder(UUID topic);

	UUID mapResponder(UUID fromTopic);
	
	MultivaluedMap<String, String> getFormMap();
}