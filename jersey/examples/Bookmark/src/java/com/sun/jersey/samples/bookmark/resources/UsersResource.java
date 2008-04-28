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

package com.sun.jersey.samples.bookmark.resources;

import com.sun.jersey.samples.bookmark.entities.UserEntity;
import java.net.URI;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.ws.rs.GET;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.codehaus.jettison.json.JSONArray;


/**
 *
 * @author japod
 */
@Path("/users/")
//@Views({"index.jsp"})
public class UsersResource {
    
    @Context UriInfo uriInfo;    

    @PersistenceUnit(unitName = "BookmarkPU")
    EntityManagerFactory emf;

    /** Creates a new instance of Users */
    public UsersResource() {
    }
    
    public List<UserEntity> getUsers() {
        return emf.createEntityManager().createQuery("SELECT u from UserEntity u").getResultList();
    }
    
    @Path("{userid}/")
    public UserResource getUser(@PathParam("userid") String userid) {
        return new UserResource(uriInfo, emf.createEntityManager(), userid);
    }

    @GET
    @ProduceMime("application/json")
    public JSONArray getUsersAsJsonArray() {
        JSONArray uriArray = new JSONArray();
        UriBuilder ub = null;
        for (UserEntity userEntity : getUsers()) {
            ub = (ub == null) ? uriInfo.getAbsolutePathBuilder() : ub.clone();
            URI userUri = ub.
                    path(userEntity.getUserid()).
                    build();
            uriArray.put(userUri.toString());
        }
        return uriArray;
    }
}
