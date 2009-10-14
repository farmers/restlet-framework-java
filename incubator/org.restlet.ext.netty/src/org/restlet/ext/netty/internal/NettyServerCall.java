/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
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

package org.restlet.ext.netty.internal;

import static org.jboss.netty.buffer.ChannelBuffers.dynamicBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.Parameter;
import org.restlet.engine.http.ChunkedOutputStream;
import org.restlet.engine.http.HttpServerCall;
import org.restlet.engine.io.ReadableEntityChannel;
import org.restlet.util.Series;

/**
 * Call that is used by the Netty HTTP server connectors.
 * 
 * @author Gabriel Ciuloaica (gciuloaica@gmail.com)
 */
public class NettyServerCall extends HttpServerCall {

    private final HttpRequest request;

    private final HttpResponse response;

    private final ChannelBuffer content;

    private volatile boolean requestHeadersAdded;

    private final SSLEngine sslEngine;

    /**
     * Constructor.
     * 
     * @param server
     *            The helped server.
     * @param buffer
     *            The content buffer.
     * @param request
     *            The Netty request.
     * @param response
     *            The Netty response.
     * @param isConfidential
     *            Indicates if the call is confidential or not.
     * @param sslEngine
     *            The SSL engine.
     */
    public NettyServerCall(Server server, ChannelBuffer buffer,
            HttpRequest request, HttpResponse response, boolean isConfidential,
            SSLEngine sslEngine) {
        super(server);
        setConfidential(isConfidential);
        this.content = buffer;
        this.request = request;
        this.response = response;
        this.sslEngine = sslEngine;

    }

    @Override
    public String getMethod() {
        return request.getMethod().getName();
    }

    @Override
    public ReadableByteChannel getRequestEntityChannel(long size) {
        if (isRequestChunked()) {
            return null;
        } else {
            return new ReadableEntityChannel(content.toByteBuffer(), null, size);
        }
    }

    @Override
    public InputStream getRequestEntityStream(long size) {
        if (isRequestChunked()) {
            InputStream entity = new ChannelBufferInputStream(content);
            return entity;
        } else {
            return null;
        }
    }

    @Override
    public ReadableByteChannel getRequestHeadChannel() {
        return null;
    }

    @Override
    public Series<Parameter> getRequestHeaders() {
        final Series<Parameter> result = super.getRequestHeaders();

        if (!this.requestHeadersAdded) {
            final Set<String> names = this.request.getHeaderNames();

            for (String name : names) {
                result.add(new Parameter(name, this.request.getHeader(name)));
            }
            this.requestHeadersAdded = true;
        }

        return result;
    }

    @Override
    public InputStream getRequestHeadStream() {
        return null;
    }

    @Override
    public String getRequestUri() {
        return request.getUri();
    }

    @Override
    public WritableByteChannel getResponseEntityChannel() {
        if (isResponseChunked()) {
            return null;
        } else {
            return new WritableByteChannel() {

                public void close() throws IOException {
                    // TODO Auto-generated method stub

                }

                public boolean isOpen() {
                    return true;
                }

                public int write(ByteBuffer src) throws IOException {
                    ChannelBuffer buf = dynamicBuffer();
                    buf.writeBytes(src);
                    response.setContent(buf);
                    return buf.readableBytes();
                }
            };
        }
    }

    @Override
    public OutputStream getResponseEntityStream() {
        if (isResponseChunked()) {
            ChannelBuffer buf = dynamicBuffer();
            this.response.setContent(buf);
            return new ChunkedOutputStream(new ChannelBufferOutputStream(
                    response.getContent()));
        } else {
            return null;
        }

    }

    @Override
    public String getSslCipherSuite() {
        final SSLEngine sslEngine = getSslEngine();
        if (sslEngine != null) {
            final SSLSession sslSession = sslEngine.getSession();
            if (sslSession != null) {
                return sslSession.getCipherSuite();
            }
        }
        return null;
    }

    @Override
    public List<Certificate> getSslClientCertificates() {
        final SSLEngine sslEngine = getSslEngine();
        if (sslEngine != null) {
            final SSLSession sslSession = sslEngine.getSession();
            if (sslSession != null) {
                try {
                    final List<Certificate> clientCertificates = Arrays
                            .asList(sslSession.getPeerCertificates());

                    return clientCertificates;
                } catch (SSLPeerUnverifiedException e) {
                    getLogger().log(Level.FINE,
                            "Can't get the client certificates.", e);
                }
            }
        }
        return null;
    }

    /**
     * Returns the SSL engine.
     * 
     * @return The SSL engine.
     */
    private SSLEngine getSslEngine() {
        return this.sslEngine;
    }

    @Override
    public String getVersion() {
        return request.getProtocolVersion().getText();
    }

    @Override
    protected boolean isClientKeepAlive() {
        return request.isKeepAlive();
    }

    @Override
    public void writeResponseHead(Response restletResponse) throws IOException {
        this.response.clearHeaders();
        for (final Parameter header : getResponseHeaders()) {
            this.response.addHeader(header.getName(), header.getValue());
        }

    }

}