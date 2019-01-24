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
import java.util.ArrayList;
import java.util.HashMap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.replication.ContentBuilder;
import com.day.cq.replication.ReplicationAction;
import com.day.cq.replication.ReplicationContent;
import com.day.cq.replication.ReplicationContentFactory;
import com.day.cq.replication.ReplicationException;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

/**
 * Custom dispatcher flush content builder that sends a list of URIs to be
 * re-fetched immediately upon flushing a page.
 */
@Component(metatype = false)
@Service(ContentBuilder.class)
@Property(name = "name", value = "dispatcher")
public class DispatcherFlushContentBuilder implements ContentBuilder {

	@Reference
	private ResourceResolverFactory rrFactory;

	private static final Logger logger = LoggerFactory.getLogger(DispatcherFlushContentBuilder.class);

	public static final String NAME = "dispatcher";

	public static final String TITLE = "Re-fetch Dispatcher Flush";

	private static final String NT_DAM_ASSET = "dam:Asset";

	/**
	 * {@inheritDoc}
	 */
	public ReplicationContent create(Session session, ReplicationAction action, ReplicationContentFactory factory)
			throws ReplicationException {

		ResourceResolver rr = null;
		PageManager pm = null;

		try {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put(JcrResourceConstants.AUTHENTICATION_INFO_SESSION, session);
			rr = rrFactory.getResourceResolver(map);
			if (rr != null) {
				pm = rr.adaptTo(PageManager.class);
			}

			/*
			 * In this simple example we check whether the page activated has no extension
			 * (e.g. /content/geometrixx/en/services) and add this page plus html extension
			 * to the list of URIs to re-fetch
			 */

			String path = action.getPath();
			if (path != null) {
				int pathSep = path.lastIndexOf('/');
				if (pathSep != -1) {
					int extSep = path.indexOf('.', pathSep);
					if (extSep == -1) {
						ArrayList<String> urisList = new ArrayList<String>();

						// For extension-less URLs under /content, we automatically re-fetch
						// {path}.html.
						// We restrict it to /content as re-fetching {path}.html may not be desirable
						// for all extension-less URLs.
						// For example, /bin/custom-servlet
						if (path.startsWith("/content")) {
							urisList.add(path + ".html");
						}

						// TODO: Add any additional re-fetch URLs for extension-less paths here.

						/*
						 * Commented out as sling:vanityPath and sling:alias are now handled by separate
						 * flush requests in recent versions of AEM. See bundle
						 * com.adobe.granite.replication.core class
						 * com.day.cq.replication.impl.AliasesPreprocessor.
						 */
						if (pm != null) {
							// Check if the link is a page and if it has any vanityPath,
							// and use the mapping url
							// in case any aliases are used or any /etc/mapping
							Page flushedPage = pm.getPage(path);
							if (flushedPage != null) {
								// try to find if that page has also some alias or vanity url
								ValueMap props = flushedPage.getProperties();
								String vanityPath = props.get("sling:vanityPath", "");
								if (vanityPath.length() > 0) {
									urisList.add(vanityPath);
								}
								String mapped = rr.map(path);
								if (!path.equals(mapped)) {
									urisList.add(mapped + ".html");
								}
							}
						}

						String[] uris = urisList.toArray(new String[urisList.size()]);
						return create(factory, uris);
					} else {
						Resource res = rr.getResource(path);
						try {
							Node node = (Node) res.adaptTo(Node.class);
							if (NT_DAM_ASSET.equals(node.getPrimaryNodeType().getName())) {

								// TODO: Re-fetch dam:Asset rendition URLs here.
								// Add paths here instead of returning VOID
								// For example:
								if (path.endsWith(".mov")) {
									String[] uris = new String[] { path + "/renditions/mobile.mov",
											path + "/renditions/desktop.mov" };
									return create(factory, uris);
								}

								return ReplicationContent.VOID;
							} else {
								// TODO: Review and customize re-fetch URLs.
								/*
								 * The code below applies when the file has a file extension, but is not a
								 * dam:Asset. For example, an nt:file. In this case we re-fetch the path.
								 * Customize this as needed, this may not be desirable for some file extensions.
								 */
								String[] uris = new String[] { path };
								return create(factory, uris);
							}
						} catch (RepositoryException e) {
							return ReplicationContent.VOID;
						}
					}

				}
			}
		} catch (LoginException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (rr != null)
				rr.close();
		}
		return ReplicationContent.VOID;

	}

	/**
	 * Create the replication content, containing one or more URIs to be re-fetched
	 * immediately upon flushing a page.
	 *
	 * @param factory factory
	 * @param uris    URIs to re-fetch
	 * @return replication content
	 *
	 * @throws ReplicationException if an error occurs
	 */
	private ReplicationContent create(ReplicationContentFactory factory, String[] uris) throws ReplicationException {

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
				logger.debug("adding " + uris[i]);
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
