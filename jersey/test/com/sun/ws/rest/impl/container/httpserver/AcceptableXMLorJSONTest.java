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

package com.sun.ws.rest.impl.container.httpserver;

import com.sun.net.httpserver.HttpHandler;
import com.sun.ws.rest.impl.client.RequestOutBound;
import com.sun.ws.rest.impl.client.ResponseInBound;
import java.net.URI;
import javax.ws.rs.UriTemplate;
import com.sun.ws.rest.impl.client.ResourceProxy;
import com.sun.ws.rest.impl.client.ResourceProxyFilter;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.xml.bind.annotation.XmlRootElement;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class AcceptableXMLorJSONTest extends AbstractHttpServerTester {
    @XmlRootElement
    public static class JAXBBean {

        public String value; 
        
        public JAXBBean() {}
        
        public JAXBBean(String str) {
            value = str;
        }

        public boolean equals(Object o) {
            if (!(o instanceof JAXBBean)) 
                return false;
            return ((JAXBBean) o).value.equals(value);
        }

        public String toString() {
            return "JAXBClass: "+value;
        }
    }
    
    @UriTemplate("/resource")
    public static class WebResource {
        @HttpMethod
        @ProduceMime({"application/xml", "application/json"})
        public JAXBBean get() {
            return new JAXBBean("test");
        }
    }
        
    public AcceptableXMLorJSONTest(String testName) {
        super(testName);
    }
    
    public void testExpliciWebResourceReference() {
        startServer(HttpHandler.class, WebResource.class);

        ResourceProxy r = ResourceProxy.create(getUri().path("resource").build());
        r.addFilter(new ResourceProxyFilter() {
            public ResponseInBound invoke(URI u, String method, RequestOutBound ro) {
                ResponseInBound ri = getNext().invoke(u, method, ro);
                
                assertEquals(200, ri.getStatus());
                assertEquals("application/xml", ri.getMetadata().getFirst("Content-Type"));
                return ri;
            }
        });        
        String content = r.acceptable("application/xml").get(String.class);
        assertTrue(content.contains("<jaxbBean><value>test</value></jaxbBean>"));
        content = r.acceptable("application/*").get(String.class);
        assertTrue(content.contains("<jaxbBean><value>test</value></jaxbBean>"));
        content = r.acceptable("*/*").get(String.class);
        assertTrue(content.contains("<jaxbBean><value>test</value></jaxbBean>"));

        
        r.removeAllFilters();
        r.addFilter(new ResourceProxyFilter() {
            public ResponseInBound invoke(URI u, String method, RequestOutBound ro) {
                ResponseInBound ri = getNext().invoke(u, method, ro);
                
                assertEquals(200, ri.getStatus());
                assertEquals("application/json", ri.getMetadata().getFirst("Content-Type"));
                return ri;
            }
        });
        content = r.acceptable("application/json").get(String.class);
        assertTrue(content.contains("{\"jaxbBean\":{\"value\":{\"$\":\"test\"}}}"));
                
        stopServer();
    }
}