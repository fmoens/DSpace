/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.rdf.storage;

import org.apache.log4j.Logger;
import org.dspace.content.DSpaceObject;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.identifier.IdentifierException;
import org.dspace.identifier.factory.IdentifierServiceFactory;
import org.dspace.identifier.service.DOIService;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author pbecker
 */
public class DOIURIGenerator
implements URIGenerator
{
    private static final Logger log = Logger.getLogger(DOIURIGenerator.class);

    /*
     * Currently (DSpace 5) DSpace supports DOIs for items only. This fallback
     * will be used to generate an URI, whenever no DOI was found.
     */
    protected final static URIGenerator fallback = new LocalURIGenerator();
    private final DOIService doiService = IdentifierServiceFactory.getInstance().getDOIService();

    private static final List<Integer> ALLOWED_CONSTANTS = Arrays.asList(Constants.SITE, Constants.COMMUNITY, Constants.COLLECTION, Constants.ITEM);
    
    @Override
    public String generateIdentifier(Context context, int type, UUID id, String handle, List<String> identifiers) throws SQLException {
        if (!ALLOWED_CONSTANTS.contains(type)) {
            return null;
        }
        for (String identifier : identifiers)
        {
            try
            {
                return doiService.DOIToExternalForm(identifier);
            } catch (IdentifierException ex) {
                // identifier is not a DOI: no problem, keep on looking.
            }
        }
        log.info("Didn't find a DOI for " + Constants.typeText[type] + ", id " + id.toString()
                + ", will use fallback URIGenerator.");
        return fallback.generateIdentifier(context, type, id, handle, identifiers);
    }

    @Override
    public String generateIdentifier(Context context, DSpaceObject dso)
            throws SQLException
    {
        return generateIdentifier(context, dso.getType(), dso.getID(), dso.getHandle(), ContentServiceFactory.getInstance().getDSpaceObjectService(dso).getIdentifiers(context, dso));
    }
    
}
