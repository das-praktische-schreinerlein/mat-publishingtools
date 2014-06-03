/**
 * <h4>FeatureDomain:</h4>
 *     Publishing
 *
 * <h4>FeatureDescription:</h4>
 *     software for publishing<br>
 *     pdftools
 * 
 * @author Michael Schreiner <michael.schreiner@your-it-fellow.de>
 * @category collaboration
 * @copyright Copyright (c) 2011-2014, Michael Schreiner
 * @license http://mozilla.org/MPL/2.0/ Mozilla Public License 2.0
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package de.mat.utils.pdftools;

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.log4j.Logger;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * <h4>FeatureDomain:</h4>
 *     Publishing
 * <h4>FeatureDescription:</h4>
 *     add pagenum to pdf
 * 
 * @package de.mat.utils.pdftools
 * @author Michael Schreiner <michael.schreiner@your-it-fellow.de>
 * @category Publishing
 * @copyright Copyright (c) 2011-2014, Michael Schreiner
 * @license http://mozilla.org/MPL/2.0/ Mozilla Public License 2.0
 */
public class PdfAddPageNum extends CmdLineJob {

    public PdfAddPageNum(String[] args) {
        super(args);
    }

    // Logger
    private static final Logger LOGGER =
            Logger.getLogger(PdfAddPageNum.class);

    @Override
    protected boolean validateCmdLine(CommandLine cmdLine) throws Throwable {
        if (cmdLine.getArgList().size() < 2) {
            return false;
        }
        return true;
    }

    @Override
    protected void printUsage() throws Throwable  {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(this.getJobName() + " SrcPdfFile DestPdfFile [PageOffset]", 
            "Desc: read SrcPdfFile, adds pagenum (+PageOffset) and writes pages to DestPdfFile",
            this.availiableCmdLineOptions,
            "");
    }

    @Override
    protected void doJob() throws Throwable {
        // get parameter
        String srcFile = this.cmdLine.getArgs()[0];
        String destFile = this.cmdLine.getArgs()[1];
        int pageOffset = new Integer(this.cmdLine.getArgs()[2]).intValue();
        
        // start
        this.addPageNumber(srcFile, destFile, pageOffset);
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        PdfAddPageNum me = new PdfAddPageNum(args);
        me.startJobProcessing();
    }

    /**
     * <h4>FeatureDomain:</h4>
     *     PublishingTools
     * <h4>FeatureDescription:</h4>
     *     adds pagenum with stamper to pages from reader 
     * <h4>FeatureResult:</h4>
     *   <ul>
     *     <li>updates stamper - updates the stamper
     *   </ul> 
     * <h4>FeatureKeywords:</h4>
     *     PDF Publishing
     * @param reader - reader with the pages
     * @param stamper - stamper to add the canvas
     * @param pageOffset - add to pagenumber
     * @throws DocumentException
     * @throws IOException
     */
    public void addPageNumber(PdfReader reader, PdfStamper stamper, 
                              int pageOffset) throws DocumentException, IOException  {
        // ierate all pages from reader
        for (int zaehler = 1; zaehler <= reader.getNumberOfPages(); zaehler++) {
            // read pagesize
            Rectangle pageSize = reader.getPageSize(zaehler);
            float xpos = pageSize.getLeft() + pageSize.getWidth()/2;
            float ypos = 20;
            float fontSize = 7;

            // Default-Positions for --page-width 150mm --page-height 212mm == 601px
            if (pageSize.getHeight() > 602 || pageSize.getHeight() < 598) {
                // correct it relative
                float factor = pageSize.getHeight() / 601;
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug(" PageHeight:" + pageSize.getHeight() 
                                 + " Factor:" + factor);
                ypos = ypos * factor;
                fontSize = fontSize * factor;
            }

            // add pagenumber-canvas
            PdfContentByte canvas = stamper.getOverContent(zaehler);
            BaseFont bf_helv = BaseFont.createFont(BaseFont.HELVETICA, "Cp1252", false);
            canvas.setFontAndSize(bf_helv, fontSize);
            canvas.beginText();
            canvas.showTextAligned(PdfContentByte.ALIGN_CENTER,
                    "" + new Integer(zaehler + pageOffset - 1),
                    xpos, ypos, 0);
            canvas.endText();
        }
    }
    
    /**
     * <h4>FeatureDomain:</h4>
     *     PublishingTools
     * <h4>FeatureDescription:</h4>
     *     read srcFile, adds pagenum and writes pages to destFile 
     * <h4>FeatureResult:</h4>
     *   <ul>
     *     <li>creates destFile - output to destFile
     *   </ul> 
     * <h4>FeatureKeywords:</h4>
     *     PDF Publishing
     * @param srcFile - source-file
     * @param destFile - destination-file
     * @param pageOffset - offset added to pagenumber
     * @throws Exception 
     */
    public void addPageNumber(String srcFile, String destFile, 
                              int pageOffset) throws Exception {
        PdfReader reader = null;
        PdfStamper stamper = null;
        try {
            // open files
            reader = new PdfReader(srcFile);
            stamper = new PdfStamper(reader, new FileOutputStream(destFile));
            
            // add pagenum
            addPageNumber(reader, stamper, pageOffset);

        } catch (Exception ex) {
            // return Exception
            throw new Exception(ex);
        } finally {
            //close everything
            if (stamper != null) {
                stamper.close();
            }
            if (reader != null) {
                reader.close();
            }
        }
        
    }
}
