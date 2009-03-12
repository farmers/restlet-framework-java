/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.jaxrs.internal.wrappers;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;

import org.restlet.ext.jaxrs.InstantiateException;
import org.restlet.ext.jaxrs.ObjectFactory;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertRepresentationException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalBeanSetterTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalConstrParamTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalFieldTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathOnClassException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathParamTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.ImplementationException;
import org.restlet.ext.jaxrs.internal.exceptions.InjectException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingConstructorException;
import org.restlet.ext.jaxrs.internal.wrappers.provider.ExtensionBackwardMapping;
import org.restlet.ext.jaxrs.internal.wrappers.provider.JaxRsProviders;

/**
 * @author Stephan
 *
 */
public class PerRequestRootResourceClass extends RootResourceClass {

    /**
     * @param jaxRsClass
     * @param tlContext
     * @param jaxRsProviders
     * @param extensionBackwardMapping
     * @param logger
     * @throws IllegalArgumentException
     * @throws MissingAnnotationException
     * @throws IllegalPathOnClassException
     * @throws MissingConstructorException
     * @throws IllegalConstrParamTypeException
     * @throws IllegalFieldTypeException
     * @throws IllegalBeanSetterTypeException
     * @throws IllegalPathParamTypeException
     */
    PerRequestRootResourceClass(Class<?> jaxRsClass,
            ThreadLocalizedContext tlContext, JaxRsProviders jaxRsProviders,
            ExtensionBackwardMapping extensionBackwardMapping, Logger logger)
            throws IllegalArgumentException, MissingAnnotationException,
            IllegalPathOnClassException, MissingConstructorException,
            IllegalConstrParamTypeException, IllegalFieldTypeException,
            IllegalBeanSetterTypeException, IllegalPathParamTypeException {
        super(jaxRsClass, tlContext, jaxRsProviders, extensionBackwardMapping, logger);
    }

    /**
     * Creates an or gets the instance of this root resource class.
     * 
     * @param objectFactory
     *                object responsible for instantiating the root resource
     *                class. Optional, thus can be null.
     * @return the instantiated root resource object.
     * @throws InvocationTargetException
     * @throws InstantiateException
     * @throws MissingAnnotationException
     * @throws WebApplicationException
     */
    @Override
    public ResourceObject getInstance(ObjectFactory objectFactory)
            throws InstantiateException, InvocationTargetException {
        Object instance = null;
        if (objectFactory != null) {
            instance = objectFactory.getInstance(this.jaxRsClass);
        }
        if (instance == null) {
            try {
                final Object[] args = this.constructorParameters.get();
                instance = WrapperUtil.createInstance(this.constructor, args);
            } catch (ConvertRepresentationException e) {
                // is (or should be :-) ) not possible
                throw new ImplementationException("Must not be possible", e);
            }
        }
        final ResourceObject rootResourceObject;
        rootResourceObject = new ResourceObject(instance, this);
        try {
            this.injectHelper.injectInto(instance, true);
        } catch (InjectException e) {
            throw new InstantiateException(e);
        }
        return rootResourceObject;
    }

}
