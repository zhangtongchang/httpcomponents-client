/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.hc.client5.http.async.methods;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.AsyncResponseConsumer;
import org.apache.hc.core5.http.nio.entity.AbstractCharDataConsumer;

public abstract class AbstractCharResponseConsumer<T> extends AbstractCharDataConsumer implements AsyncResponseConsumer<T> {

    private volatile FutureCallback<T> resultCallback;

    protected abstract void start(HttpResponse response, ContentType contentType) throws HttpException, IOException;

    protected abstract T buildResult() throws IOException;

    @Override
    public final void consumeResponse(
            final HttpResponse response,
            final EntityDetails entityDetails,
            final FutureCallback<T> resultCallback) throws HttpException, IOException {
        this.resultCallback = resultCallback;
        if (entityDetails != null) {
            final ContentType contentType;
            try {
                contentType = ContentType.parse(entityDetails.getContentType());
            } catch (final UnsupportedCharsetException ex) {
                throw new UnsupportedEncodingException(ex.getMessage());
            }
            Charset charset = contentType != null ? contentType.getCharset() : null;
            if (charset == null) {
                charset = StandardCharsets.US_ASCII;
            }
            setCharset(charset);
            start(response, contentType);
        } else {
            start(response, null);
            completed();
        }
    }

    @Override
    protected final void completed() throws IOException {
        resultCallback.completed(buildResult());
    }

    @Override
    public void failed(final Exception cause) {
    }

}