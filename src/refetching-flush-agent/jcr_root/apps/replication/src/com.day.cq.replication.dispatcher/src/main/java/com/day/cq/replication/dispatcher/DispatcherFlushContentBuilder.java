/*
* Copyright 2013 Adobe Systems Incorporated
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.day.cq.replication.dispatcher;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import com.day.cq.replication.ContentBuilder;
import com.day.cq.replication.ReplicationAction;
import com.day.cq.replication.ReplicationContent;
import com.day.cq.replication.ReplicationContentFactory;
import com.day.cq.replication.ReplicationException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Property;

/**
 * Custom dispatcher flush content builder that sends a list of URIs
 * to be re-fetched immediately upon flushing a page.
 */
@Component(metatype=false)
@Service(ContentBuilder.class)
@Property(name="name",value="dispatcher")
public class DispatcherFlushContentBuilder implements ContentBuilder {

    public static final String NAME = "dispatcher";

    public static final String TITLE = "Re-fetch Dispatcher Flush";

    /**
     * {@inheritDoc}
     */
    public ReplicationContent create(Session session, ReplicationAction action,
                                     ReplicationContentFactory factory)
            throws ReplicationException {
        
        /* In this simple example we check whether the page activated has
           no extension (e.g. /content/geometrixx/en/services) and add
           this page plus html extension to the list of URIs to re-fetch */
           
        String path = action.getPath();
        if (path != null) {
            int pathSep = path.lastIndexOf('/');
            if (pathSep != -1) {
                int extSep = path.indexOf('.', pathSep);
                if (extSep == -1) {
                    String[] uris = new String[] { path + ".html" };
                    return create(factory, uris);
                } else {    
                    String[] uris = new String[] { path };
                    return create(factory, uris);
                }

            }
        }
        return ReplicationContent.VOID;
    }
    
    /**
     * Create the replication content, containing one or more URIs to be
     * re-fetched immediately upon flushing a page.
     *
     * @param factory factory
     * @param uris URIs to re-fetch
     * @return replication content
     *
     * @throws ReplicationException if an error occurs
     */
    private ReplicationContent create(ReplicationContentFactory factory, String[] uris) 
            throws ReplicationException {
            
        File tmpFile;
        BufferedWriter out = null;
        
        try {
            tmpFile = File.createTempFile("cq5", ".post");
        } catch (IOException e) {
            throw new ReplicationException("Unable to create temp file", e);
        }
        
        try {
            out = new BufferedWriter(new FileWriter(tmpFile));
            for (int i = 0; i < uris.length; i++) {
                out.write(uris[i]);
                out.newLine();
            }
            out.close();
            IOUtils.closeQuietly(out);
            out = null;
            return factory.create("text/plain", tmpFile, true);
        } catch (IOException e) {
            if (out != null) {
                IOUtils.closeQuietly(out);
            }
            tmpFile.delete();
            throw new ReplicationException("Unable to create repository content", e);
        }
    }
    
    /**
     * {@inheritDoc}
     *
     * @return {@value #NAME}
     */
    public String getName() {
        return NAME;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@value #TITLE}
     */
    public String getTitle() {
        return TITLE;
    }
}
