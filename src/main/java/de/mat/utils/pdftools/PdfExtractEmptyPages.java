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

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.log4j.Logger;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;

/**
 * <h4>FeatureDomain:</h4>
 *     Publishing
 * <h4>FeatureDescription:</h4>
 *     extract empty pages
 * 
 * @package de.mat.utils.pdftools
 * @author Michael Schreiner <michael.schreiner@your-it-fellow.de>
 * @category Publishing
 * @copyright Copyright (c) 2011-2014, Michael Schreiner
 * @license http://mozilla.org/MPL/2.0/ Mozilla Public License 2.0
 */
public class PdfExtractEmptyPages extends CmdLineJob {

    public PdfExtractEmptyPages(String[] args) {
        super(args);
    }

    // identify empty pages, set minsize
    public static int blankPdfsize = 15635; // siehe \tmp\buch\meine-3000er-076-tour-paternkofel_-_paternkofelsteig-dontshowtourentalort.pdf alt:1400;
    public static int blankPdfsize_v5 = 800;

    // Logger
    private static final Logger LOGGER =
            Logger.getLogger(PdfExtractEmptyPages.class);

    @Override
    protected boolean validateCmdLine(CommandLine cmdLine) throws Throwable {
        if (cmdLine.getArgList().size() < 3) {
            return false;
        }
        return true;
    }

    @Override
    protected void printUsage() throws Throwable  {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(this.getJobName() + " SrcPdfFile DestPdfFile EmptyPagesPdfFile",
                        "Desc: reads pdfSourceFile and adds pages to pdfRemovedFile "
                        + "if empty, or to pdfDestinationFile if not empty",
                        this.availiableCmdLineOptions,
                        "");
    }

    @Override
    protected void doJob() throws Throwable {
        // get parameter
        String srcFile = this.cmdLine.getArgs()[0];
        String destFile = this.cmdLine.getArgs()[1];
        String emptyFile = this.cmdLine.getArgs()[1];
        
        // start
        PdfExtractEmptyPages.removeBlankPdfPages(srcFile, destFile, emptyFile);
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        PdfExtractEmptyPages me = new PdfExtractEmptyPages(args);
        me.startJobProcessing();
    }

    /**
     * <h4>FeatureDomain:</h4>
     *     PublishingTools
     * <h4>FeatureDescription:</h4>
     *     reads pdfSourceFile and adds pages to pdfRemovedFile if empty, or to 
     *     pdfDestinationFile if not empty
     * <h4>FeatureResult:</h4>
     *   <ul>
     *     <li>updates pdfDestinationFile - add all pages which are not empty
     *     <li>updates pdfRemovedFile - add all empty pages
     *   </ul> 
     * <h4>FeatureKeywords:</h4>
     *     PDF Publishing
     * @param pdfSourceFile - source pdf-file
     * @param pdfDestinationFile - pdf with all not empty pages
     * @param pdfRemovedFile - pdf with all empty pages
     * @throws Exception
     */
    public static void removeBlankPdfPages(String pdfSourceFile,
            String pdfDestinationFile, String pdfRemovedFile) throws Exception {
        // create readerOrig
        PdfReader readerOrig = new PdfReader(pdfSourceFile);

        // create writerTrimmed which bases on readerOrig
        Document documentTrimmed = new Document(readerOrig.getPageSizeWithRotation(1));
        PdfCopy writerTrimmed = new PdfCopy(documentTrimmed, new FileOutputStream(pdfDestinationFile));
        documentTrimmed.open();

        // create writerRemoved which bases on readerOrig
        Document documentRemoved = new Document(readerOrig.getPageSizeWithRotation(1));
        PdfCopy writerRemoved = new PdfCopy(documentRemoved, new FileOutputStream(pdfRemovedFile));
        documentRemoved.open();

        // extract and copy empty pages
        addTrimmedPages(pdfSourceFile, readerOrig, writerTrimmed, writerRemoved, true);

        // close everything
        documentTrimmed.close();
        writerTrimmed.close();
        documentRemoved.close();
        writerRemoved.close();
        readerOrig.close();
    }

    /**
     * <h4>FeatureDomain:</h4>
     *     PublishingTools
     * <h4>FeatureDescription:</h4>
     *     reads readerOrig and adds pages to writerRemoved if empty, or to 
     *     writerTrimmed if not empty
     * <h4>FeatureResult:</h4>
     *   <ul>
     *     <li>updates writerTrimmed - add all pages which are not empty
     *     <li>updates writerRemoved - add all empty pages
     *   </ul> 
     * <h4>FeatureKeywords:</h4>
     *     PDF Publishing
     * @param origFileName - orig filename of the sourcepdf
     * @param readerOrig - reader of source
     * @param writerTrimmed - writer for trimmed pages
     * @param writerRemoved - writer for empty pages
     * @param flgTrim - ??
     * @return - count of trimmed pages
     * @throws Exception
     */
    public static int addTrimmedPages(String origFileName, PdfReader readerOrig,
        PdfCopy writerTrimmed, PdfCopy writerRemoved, boolean flgTrim) throws Exception {
        PdfImportedPage page = null;
        int countTrimmedPages = 0;
        
        //loop each page
        for (int i=1;i<=readerOrig.getNumberOfPages();i++) {
            boolean flgIsEmpty = true;

            // get dictionary
            PdfDictionary pageDict = readerOrig.getPageN(i);

            // every pdf-version has its own way :-(
            char version = readerOrig.getPdfVersion();
            
            if (version == '3') {
                // PDF-Version: 3
                
                // examine the resource dictionary for /Font or
                // /XObject keys.  If either are present, they're almost
                // certainly actually used on the page -> not blank.
                PdfObject myObj = pageDict.get( PdfName.RESOURCES );
                PdfDictionary resDict = null;
                if (myObj instanceof PdfDictionary) {
                    resDict = (PdfDictionary)myObj;
                } else {
                    resDict = (PdfDictionary)PdfReader.getPdfObject(myObj);
                }
                if (resDict != null) {
                    flgIsEmpty = resDict.get( PdfName.FONT ) == null 
                                 && resDict.get( PdfName.XOBJECT ) == null
                                 ;
                    if (LOGGER.isInfoEnabled()) {
                        if (flgIsEmpty) {
                            LOGGER.info("probably empty page "+i + " Version: 1." + version 
                                        + " FONT/XOBJECT found in File:" + origFileName);
                        } else {
                            LOGGER.info("normal page "+i + " Version: 1." + version 
                                        + " no FONT/XOBJECT found in File:" + origFileName);
                        }
                    }
                }
            } else if (version == '4') {
                // PDF-Version: 4
                // check the contentsize.

                // get the page content
                byte bContent [] = readerOrig.getPageContent(i);
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                // write the content to an output stream
                bs.write(bContent);
                
                flgIsEmpty = true;
                if (bs.size() > blankPdfsize) {
                    if (LOGGER.isInfoEnabled()) 
                        LOGGER.info("normal page "+i + " Version: 1." + version 
                            + " BS:" + bs.size() + " File:" + origFileName);
                    flgIsEmpty = false;
                } else {
                    if (LOGGER.isInfoEnabled()) 
                        LOGGER.info("probably empty page "+i + " Version: 1." 
                            + version + " BS:" + bs.size() + " File:" + origFileName);
                }
            } else if (version == '5') {
                // PDF-Version: 5
                // check the contentsize.

                // get the page content
                byte bContent [] = readerOrig.getPageContent(i);
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                // write the content to an output stream
                bs.write(bContent);
                
                flgIsEmpty = true;
                if (bs.size() > blankPdfsize_v5) {
                    if (LOGGER.isInfoEnabled()) 
                        LOGGER.info("normal page "+i + " Version: 1." + version 
                             + " BS:" + bs.size() + " File:" + origFileName);
                    flgIsEmpty = false;
                } else {
                    if (LOGGER.isInfoEnabled()) 
                        LOGGER.info("probably empty page "+i + " Version: 1." 
                             + version + " BS:" + bs.size() + " File:" + origFileName);
                }
            }

            // add page to removed or trimmed document
            if (! flgIsEmpty || ! flgTrim) {
                if (LOGGER.isInfoEnabled()) 
                    LOGGER.info("add page "+i);
                page = writerTrimmed.getImportedPage(readerOrig, i);
                writerTrimmed.addPage(page);
                countTrimmedPages++;
            } else {
                if (LOGGER.isInfoEnabled()) 
                    LOGGER.info("skip page "+i + " Version: 1." + version 
                        + " File:" + origFileName);
                if (writerRemoved != null) {
                    page = writerRemoved.getImportedPage(readerOrig, i);
                    writerRemoved.addPage(page);
                }
            }
        }

        return countTrimmedPages;
    }
}
