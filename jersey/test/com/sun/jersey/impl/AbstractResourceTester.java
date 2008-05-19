/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License. 
 * 
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */

package com.sun.jersey.impl;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import junit.framework.TestCase;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientFilter;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.impl.application.WebApplicationImpl;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.service.ComponentProvider;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class AbstractResourceTester extends TestCase {
    protected static final URI BASE_URI = URI.create("test:/base/");

    protected WebApplication w;
    
    protected AbstractResourceTester(String testName) {
        super(testName);
    }
    
    protected void initiateWebApplication(ComponentProvider cp, Class... classes) {
        w = createWebApplication(cp, classes);
    }
    
    protected void initiateWebApplication(Class... classes) {
        w = createWebApplication(classes);
    }
    
    protected void initiateWebApplication(ResourceConfig c) {
        w = createWebApplication(c);
    }
    
    private WebApplication createWebApplication(Class... classes) {
        return createWebApplication(null, classes);
    }
    
    private WebApplication createWebApplication(ComponentProvider cp, Class... classes) {
        return createWebApplication(cp, new HashSet<Class>(Arrays.asList(classes)));
    }
    
    private WebApplication createWebApplication(ComponentProvider cp, Set<Class> classes) {
        ResourceConfig rc = new DefaultResourceConfig(
                getMatchingClasses(classes, Path.class));
        rc.getProviderClasses().addAll(
                getMatchingClasses(classes, Provider.class));
        
        return createWebApplication(rc, cp);
    }
    
    private WebApplication createWebApplication(ResourceConfig c) {
        return createWebApplication(c, null);
    }
    
    private WebApplication createWebApplication(ResourceConfig c, ComponentProvider cp) {
        WebApplicationImpl a = new WebApplicationImpl();
        a.initiate(c, cp);
        return a;
    }

    protected WebResource resource(String relativeUri) {
        return resource(relativeUri, true);
    }
    
    protected WebResource resource(String relativeUri, ClientConfig clientConfig) {
        return resource(relativeUri, true, clientConfig);
    }
    
    protected WebResource resource(String relativeUri, boolean checkStatus) {
        return resource(relativeUri, checkStatus, null);        
    }
    
    protected WebResource resource(String relativeUri, boolean checkStatus, 
            ClientConfig clientConfig) {
        Client c = (clientConfig == null) 
            ? new Client(new TestResourceClientHandler(BASE_URI, w))
            : new Client(new TestResourceClientHandler(BASE_URI, w), clientConfig);

        if (checkStatus) {
            c.addFilter(new ClientFilter() {
                public ClientResponse handle(ClientRequest ro) {
                    ClientResponse r = getNext().handle(ro);
                    assertTrue(r.getStatus() < 300);
                    return r;
                }
            });
        }
        WebResource r = c.resource(createCompleteUri(BASE_URI, relativeUri));
        
        return r;
    }
    
    private URI createCompleteUri(URI baseUri, String relativeUri) {
        if (relativeUri.startsWith("/"))
            relativeUri = relativeUri.substring(1);
        
        return URI.create(baseUri.toString() + relativeUri);
    }
    
    private static Set<Class<?>> getMatchingClasses(Set<Class> classes, Class... annotations) {
        Set<Class<?>> s = new HashSet<Class<?>>();
        for (Class c : classes) {
            if (hasAnnotations(c, annotations))
                s.add(c);
        }
        return s;
    }
    
    @SuppressWarnings("unchecked")
    private static boolean hasAnnotations(Class c, Class... annotations) {
        Annotation[] _as = c.getAnnotations();
        for (Class a : annotations) {
            if (c.getAnnotation(a) == null) return false;
        }
        
        return true;
    }    
}
