/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import org.dspace.core.service.NewsService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulate access to the news texts.
 *
 * @author mhwood
 */
public class NewsServiceImpl implements NewsService
{
    private final Logger log = LoggerFactory.getLogger(NewsServiceImpl.class);

	private List<String> acceptableFilenames;
	
	public void setAcceptableFilenames(List<String> acceptableFilenames) {
		this.acceptableFilenames = acceptableFilenames;
	}

    /** Not instantiable. */
    protected NewsServiceImpl() {}

    @Override
    public String readNewsFile(String newsFile)
    {
    	if (!validate(newsFile)) {
    		throw new IllegalArgumentException("The file "+ newsFile + " is not a valid news file");
    	}
        String fileName = getNewsFilePath();

        fileName += newsFile;

        StringBuilder text = new StringBuilder();

        try
        {
            // retrieve existing news from file
            FileInputStream fir = new FileInputStream(fileName);
            // Why not use constants or Enums for "UTF-8"? The project contains "UTF-8" about a hundred times.
            InputStreamReader ir = new InputStreamReader(fir, "UTF-8");
            BufferedReader br = new BufferedReader(ir);

            String lineIn;

            while ((lineIn = br.readLine()) != null)
            {
                text.append(lineIn);
            }

            // Different closing mechanism from LicenseServiceImpl: not using finally block here.
            br.close();
            ir.close();
            fir.close();
        }
        catch (IOException e)
        {
            log.warn("news_read: " + e.getLocalizedMessage());
        }

        return text.toString();
    }

    @Override
    public String writeNewsFile(String newsFile, String news)
    {
    	if (!validate(newsFile)) {
    		throw new IllegalArgumentException("The file "+ newsFile + " is not a valid news file");
    	}
        String fileName = getNewsFilePath();

        fileName += newsFile;

        try
        {
            // write the news out to the appropriate file
            FileOutputStream fos = new FileOutputStream(fileName);
            OutputStreamWriter osr = new OutputStreamWriter(fos, "UTF-8");
            PrintWriter out = new PrintWriter(osr);
            out.print(news);
            // Different closing mechanism from LicenseServiceImpl: not using finally block here.
            // osr and fos are not being closed
            out.close();
        }
        catch (IOException e)
        {
            log.warn("news_write: " + e.getLocalizedMessage());
        }

        return news;
    }

    @Override
    public String getNewsFilePath()
    {
        // Why not use constants or Enums for "dspace.dir" and other similar properties? The project contains "dspace.dir" about fifty times.
        return DSpaceServicesFactory.getInstance().getConfigurationService().getProperty("dspace.dir")
                + File.separator + "config" + File.separator;
    }
    
	@Override
	public boolean validate(String newsName) {
        // Shouldn't this throw an exception when acceptableFilenames == null?
		if (acceptableFilenames != null) {
			return acceptableFilenames.contains(newsName);
		}
		return false;
	}
}
