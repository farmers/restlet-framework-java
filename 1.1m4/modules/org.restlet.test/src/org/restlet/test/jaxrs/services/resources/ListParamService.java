/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */
package org.restlet.test.jaxrs.services.resources;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.QueryParam;

/**
 * @author Stephan Koops
 * @see ListParamTest
 */
@Path("/listParams")
public class ListParamService {

    @GET
    @Path("cookie")
    @ProduceMime("text/plain")
    public String getCookie(@CookieParam("c") String c, 
            @CookieParam("cc") List<String> cc) {
        return "c=" + c + "\ncc="+cc;
    }

    @GET
    @Path("header")
    @ProduceMime("text/plain")
    public String getHeader(@HeaderParam("h") String h, 
            @HeaderParam("hh") Set<String> hh) {
        return "h=" + h + "\nhh="+hh;
    }

    @GET
    @Path("matrix")
    @ProduceMime("text/plain")
    public String getMatrix(@MatrixParam("m") String m, 
            @MatrixParam("mm") Collection<String> mm) {
        return "m=" + m + "\nmm="+mm;
    }

    @GET
    @Path("path/{p}/{p}/{pp}/{pp}")
    @ProduceMime("text/plain")
    public String getPath(@PathParam("p") String p, 
            @PathParam("pp") SortedSet<String> pp) {
        return "p=" + p + "\npp="+pp;
    }

    @GET
    @Path("query")
    @ProduceMime("text/plain")
    public String getQuery(@QueryParam("q") String q, 
            @QueryParam("qq") List<String> qq) {
        return "q=" + q + "\nqq="+qq;
    }
    
    //@Path("{other}")
    public ListParamService getOther() {
        return new ListParamService();
    }
    
    @GET
    @ProduceMime("text/plain")
    public String getZ(@PathParam("other") String other, 
            @PathParam("other") List<String> others) {
        return "other="+other+"\nothers="+others;
    }
}