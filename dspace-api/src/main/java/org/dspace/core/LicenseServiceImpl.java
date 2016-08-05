/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.core;

import java.io.*;

import org.dspace.core.service.LicenseService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulate the deposit license.
 *
 * @author mhwood
 */
public class LicenseServiceImpl implements LicenseService
{
    private final Logger log = LoggerFactory.getLogger(LicenseServiceImpl.class);

    /** The default license */
    protected String license;

    protected LicenseServiceImpl() {}

    @Override
    public void writeLicenseFile(String licenseFile,
                                 String newLicense)
    {
        // 1. Duplicate code to write to a file: same as in org/dspace/core/NewsServiceImpl.java
        // 2. AutoCloseables (Java 7) could be used here
        FileOutputStream fos = null;
        OutputStreamWriter osr = null;
        PrintWriter out = null;
        try
        {
            fos = new FileOutputStream(licenseFile);
            osr = new OutputStreamWriter(fos, "UTF-8");
            out = new PrintWriter(osr);
            out.print(newLicense);
            // Only out was being closed, osr and fos weren't, in contrast with the closing in for instance getLicenseText
            // where all streams / writers are being closed.
            // out.close();
        } catch (IOException e)
        {
            log.warn("license_write: " + e.getLocalizedMessage());
        } finally {
            closeStreams(out, osr, fos);
        }
        license = newLicense;
    }

    @Override
    public String getLicenseText(String licenseFile)
    {
        // AutoCloseables could be used here
        InputStream is = null;
        InputStreamReader ir = null;
        BufferedReader br = null;
        try
        {
            is = new FileInputStream(licenseFile);
            ir = new InputStreamReader(is, "UTF-8");
            br = new BufferedReader(ir);
            String lineIn;
            license = "";
            while ((lineIn = br.readLine()) != null)
            {
                license = license + lineIn + '\n';
            }
        } catch (IOException e)
        {
            log.error("Can't load configuration", e);
            throw new IllegalStateException("Failed to read default license.", e);
        } finally
        {
            closeStreams(br, ir, is);
        }
        return license;
    }

    /**
     * Get the site-wide default license that submitters need to grant
     *
     * @return the default license
     */
    @Override
    public String getDefaultSubmissionLicense()
    {
        if (null == license)
        {
            init();
        }
        return license;
    }

    /**
     * Load in the default license.
     */
    protected void init()
    {
        File licenseFile = new File(DSpaceServicesFactory.getInstance().getConfigurationService().getProperty("dspace.dir")
                + File.separator + "config" + File.separator + "default.license");

        // AutoCloseables could be used here
        FileInputStream  fir = null;
        InputStreamReader ir = null;
        BufferedReader br = null;
        try
        {

            fir = new FileInputStream(licenseFile);
            ir = new InputStreamReader(fir, "UTF-8");
            br = new BufferedReader(ir);
            String lineIn;
            license = "";

            while ((lineIn = br.readLine()) != null)
            {
                license = license + lineIn + '\n';
            }
            // Why is this here? All streams and readers are being closed in the finally block
            br.close();

        }
        catch (IOException e)
        {
            log.error("Can't load license: " + licenseFile.toString() , e);

            // FIXME: Maybe something more graceful here, but with the
            // configuration we can't do anything
            throw new IllegalStateException("Cannot load license: "
                    + licenseFile.toString(),e);
        }
        finally
        {
            closeStreams(br, ir, fir);
        }
    }

    private void closeStream(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ioe) {
            }
        }
    }

    private void closeStreams(Closeable ...closeables) {
        for (Closeable closeable : closeables) {
            closeStream(closeable);
        }
    }
}
