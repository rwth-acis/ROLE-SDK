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
package se.kth.csc.kmr.conserve.logic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ConfigurableServerChannel;
import org.cometd.bayeux.server.LocalSession;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.server.authorizer.GrantAuthorizer;
import org.openrdf.model.Graph;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.NameBasedGenerator;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Control;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.Resolution;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.core.ServerAttributeListener;
import se.kth.csc.kmr.conserve.iface.jaxrs.RequestImpl;
import se.kth.csc.kmr.conserve.util.Base64UUID;

/**
 * Handler that provides context info and functionality for creating and
 * deleting contexts.
 *
 * @author Erik Isaksson <erikis@kth.se>
 */
public class ContextResponder extends RDFResponder {

    private static Logger log = LoggerFactory.getLogger(ContextResponder.class);

    @Override
    public void addTriples(Graph graph, Request request, Concept context,
                           UriBuilder uriBuilder) {
        final ValueFactory valueFactory = graph.getValueFactory();
        final URI contextUri = valueFactory.createURI(uriBuilder.build()
                .toString());
        final List<Concept> concepts = store.getConcepts(context.getUuid());
        final URI rdfType = valueFactory.createURI(
                "http://www.w3.org/1999/02/22-rdf-syntax-ns#", "type");
        setNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        final URI sameAs = valueFactory.createURI(
                "http://www.w3.org/2002/07/owl#", "sameAs");
        setNamespace("owl", "http://www.w3.org/2002/07/owl#");
        for (Concept concept : concepts) {
            String id = concept.getId();
            if (id == null || id.contains(":") || id.contains("/")) {
                id = Base64UUID.encode(concept.getUuid());
            }
            URI conceptUri = valueFactory.createURI(uriBuilder.clone().path(id)
                    .build().toString());
            Concept predicate = store.getConcept(concept.getPredicate());
            URI predicateUri;
            if (predicate != null) {
                Entry<String, String> predicateNamespace = ((RequestImpl) request)
                        .getNamespace(predicate.getId());
                predicateUri = valueFactory.createURI(
                        predicateNamespace.getValue(),
                        predicate.getId().substring(
                                predicateNamespace.getValue().length()));
                setNamespace(predicateNamespace.getKey(),
                        predicateNamespace.getValue());
            } else {
                predicateUri = valueFactory.createURI("urn:uuid:"
                        + concept.getPredicate());
            }
            graph.add(contextUri, predicateUri, conceptUri);
            List<Control> impliedTypes = store.getControls(
                    concept.getPredicate(), ConserveTerms.range, null);
            for (Control impliedType : impliedTypes) {
                graph.add(conceptUri, rdfType,
                        valueFactory.createURI(impliedType.getUri()));
            }
            List<Control> explicitTypes = store.getControls(concept.getUuid(),
                    ConserveTerms.type, null);
            for (Control explicitType : explicitTypes) {
                graph.add(conceptUri, rdfType,
                        valueFactory.createURI(explicitType.getUri()));
            }
            List<Control> references = store.getControls(concept.getUuid(),
                    ConserveTerms.reference, null);
            for (Control reference : references) {
                graph.add(conceptUri, sameAs,
                        valueFactory.createURI(reference.getUri()));
            }
        }
    }

    @Override
    public Object doPut(Request request, byte[] data) {
        return Response.status(Response.Status.FORBIDDEN)
                .header("Cache-Control", "no-cache").build();
    }

    @Override
    public Object doPost(Request request, byte[] data) {
        Concept parent = request.getContext();
        PathSegment relationPathSegment = ((RequestImpl) request)
                .getPathSegments().get(Resolution.StandardType.RELATION);
        // UUID template = request.getRelation() != null ? request.getRelation()
        // .getUuid() : null;
        String type = relationPathSegment != null ? relationPathSegment
                .getMatrixParameters().getFirst("type") : null;
        if (((RequestImpl) request).getLinkRelations().containsKey(
                "http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
            type = ((RequestImpl) request).getLinkRelations().get(
                    "http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        }
        // if (template != null) {
        // type = (type != null ? type : "") + "\nurn:uuid:"
        // + template.toString();
        // }
        String seeAlso = null;
        if (((RequestImpl) request).getLinkRelations().containsKey(
                "http://www.w3.org/2000/01/rdf-schema#seeAlso")) {
            seeAlso = ((RequestImpl) request).getLinkRelations().get(
                    "http://www.w3.org/2000/01/rdf-schema#seeAlso");
        }
        UUID relation = request.getRelation() == null ? ConserveTerms.hasPart
                : request.getRelation().getUuid(); // null;
        PathSegment contextPathSegment = ((RequestImpl) request)
                .getPathSegments().get(Resolution.StandardType.CONTEXT);
        String alias = contextPathSegment != null ? contextPathSegment
                .getMatrixParameters().getFirst("alias") : null;
        if (((RequestImpl) request).getLinkRelations().containsKey(
                "http://www.w3.org/2000/01/rdf-schema#label")) {
            alias = ((RequestImpl) request).getLinkRelations().get(
                    "http://www.w3.org/2000/01/rdf-schema#label");
        }

        Map<String, String> controlRels = ((RequestImpl) request)
                .getLinkRelations();
        controlRels.remove("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        controlRels.remove("http://www.w3.org/2000/01/rdf-schema#seeAlso");
        controlRels.remove("http://www.w3.org/2000/01/rdf-schema#label");

        Concept context = store().in(parent).sub(relation).create(alias);
        store.putConcept(context);
        request.setResolution(Resolution.StandardType.CREATED, context);
        NameBasedGenerator uriUuidGenerator = Generators
                .nameBasedGenerator(NameBasedGenerator.NAMESPACE_URL);
        if (type != null) {
            store.putControl(store.createControl(context.getUuid(),
                    ConserveTerms.type, uriUuidGenerator.generate(type), type));
        }
        if (seeAlso != null) {
            Concept resolved = store().resolve(seeAlso);
            store.putControl(store.createControl(
                    context.getUuid(),
                    ConserveTerms.reference,
                    resolved != null ? resolved.getUuid() : uriUuidGenerator
                            .generate(seeAlso), seeAlso));
        }
        for (Entry<String, String> controlRel : controlRels.entrySet()) {
            store.putControl(store.createControl(context.getUuid(),
                    uriUuidGenerator.generate(controlRel.getKey()),
                    uriUuidGenerator.generate(controlRel.getValue()),
                    controlRel.getValue()));
        }
        UriBuilder ub = store().in(context).uriBuilder();

        BayeuxServer bayeux = ServerAttributeListener.getBayeux();
        if (bayeux != null) {
            String channelId = store().in(parent).uri().getPath();
            bayeux.createChannelIfAbsent(channelId, new ServerChannel.Initializer() {
                public void configureChannel(ConfigurableServerChannel channel) {
                    channel.addAuthorizer(GrantAuthorizer.GRANT_NONE);
                }
            });
            Map<String, Object> message = new HashMap<String, Object>();
            message.put("location", ub.build().toString());
            LocalSession session = bayeux.newLocalSession("conserve");
            session.handshake();
            ClientSessionChannel channel = session.getChannel(channelId);
            channel.publish(message);
            session.disconnect();
        }

        return Response.created(java.net.URI.create(ub.build().toString()))
                .header("Cache-Control", "no-cache").build();
    }

    @Override
    public Object doDelete(Request request) {
        return Response.status(Response.Status.FORBIDDEN)
                .header("Cache-Control", "no-cache").build();
    }

}
