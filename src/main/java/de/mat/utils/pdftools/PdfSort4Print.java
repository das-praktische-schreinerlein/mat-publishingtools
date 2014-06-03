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

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * <h4>FeatureDomain:</h4>
 *     Publishing
 * <h4>FeatureDescription:</h4>
 *     sort pdf-pages for print (2/4/6/8 per page or broschuere...)
 * 
 * @package de.mat.utils.pdftools
 * @author Michael Schreiner <michael.schreiner@your-it-fellow.de>
 * @category Publishing
 * @copyright Copyright (c) 2011-2014, Michael Schreiner
 * @license http://mozilla.org/MPL/2.0/ Mozilla Public License 2.0
 */
public class PdfSort4Print extends CmdLineJob {

    public PdfSort4Print(String[] args) {
        super(args);
    }

    // Logger
    private static final Logger LOGGER =
            Logger.getLogger(PdfResize.class);

    // define Patterns
    public static final String CONST_PATTERN =
        "^(.*)\\t(.*)\\t(.*)\\t(.*)$" ;
    private static final Pattern CONST_BOOKMARK =
        Pattern.compile(CONST_PATTERN);


    @Override
    protected void printUsage() throws Throwable  {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(this.getJobName() + " ",
                        "Desc: sort page for printing it in modus"
                        + " PerPage (2=a5/4=a6) from srcfile 'i' to outfile 'o'."
                        + " Soprt it for print in broschuere."
                        + "",
                        this.availiableCmdLineOptions,
                        "");
    }

    @Override
    protected Options genAvailiableCmdLineOptions() throws Throwable {
        Options availiableCmdLineOptions = new Options();

        // Hilfe-Option
        Option helpOption = new Option("h", "help", false, "usage");
        helpOption.setRequired(false);
        availiableCmdLineOptions.addOption(helpOption);

        Option inFile = new Option("i", "in", true, "Input-File");
        inFile.setRequired(true);
        availiableCmdLineOptions.addOption(inFile);

        Option outFile = new Option("o", "out", true, "Outputfile");
        outFile.setRequired(true);
        availiableCmdLineOptions.addOption(outFile);

        Option perpage = new Option("p", "perPage", true, "pro Seite");
        perpage.setRequired(true);
        availiableCmdLineOptions.addOption(perpage);
        
        return availiableCmdLineOptions;
    }

    @Override
    public void doJob() throws Throwable {
        String inFile = this.cmdLine.getOptionValue("i", null);
        String outFile = this.cmdLine.getOptionValue("o", null);
        int perPage = new Integer(this.cmdLine.getOptionValue("p", null)).intValue();
        
        sortPdfPages(inFile, outFile, perPage);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        PdfSort4Print me = new PdfSort4Print(args);
        me.startJobProcessing();
    }

    public static void sortPdfPages(String pdfSourceFile,
            String pdfDestinationFile, int perPage) throws Exception {
        PdfImportedPage page = null;
        
        if (perPage != 2 && perPage != 4) {
            throw new IllegalArgumentException("Sorry, perPage must only be "
                            + "2 or 4. All other is not implemented yet :-(");
        }

        
        // #######
        // # fill to odd pagecount
        // #######
        
        // create reader
        PdfReader readerOrig = new PdfReader(pdfSourceFile);
        
        // calc data
        int countPage = readerOrig.getNumberOfPages();
        int blaetter = new Double(Math.ceil((countPage + 0.0) / perPage / 2)).intValue();
        int zielPages = (blaetter * perPage * 2) - countPage; 

        if (LOGGER.isInfoEnabled())
            LOGGER.info("CurPages: " + countPage + " Blaetter:" 
                        + blaetter + " AddPage:" + zielPages); 
        
        // add sites
        String oddFile = pdfDestinationFile + ".filled.pdf";
        PdfStamper stamper = new PdfStamper(readerOrig, 
                        new FileOutputStream(oddFile));
        // add empty pages
        for (int i=1; i<=zielPages; i++) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("addEmptyPage: " + i); 
            stamper.insertPage(readerOrig.getNumberOfPages() + 1, 
                            readerOrig.getPageSizeWithRotation(1));
        }
        stamper.close();
        readerOrig.close();

        
        // ########
        // # read new odd document and sort pages
        // ########
        // step 1: create new reader
        PdfReader readerOdd = new PdfReader(oddFile);

        // create writerSorted
        String sortedFile = pdfDestinationFile;
        Document documentSorted = new Document(
                        readerOrig.getPageSizeWithRotation(1));
        PdfCopy writerSorted = new PdfCopy(documentSorted, 
                        new FileOutputStream(sortedFile));
        documentSorted.open();
        
        // add pages in calced order
        List<Integer> lstPageNr = new ArrayList<Integer>();
        int pageCount = readerOdd.getNumberOfPages();
        int startseite = 1;
        for (int i=1; i<=blaetter; i++) {
            if (perPage == 2) {
                startseite = ((i-1) * perPage)  + 1;
                
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("Blatt:" + i + " Startseite: " + startseite); 
                // front top
                lstPageNr.add(new Integer(pageCount - startseite + 1));
                // front bottom
                lstPageNr.add(new Integer(startseite));

                // back top
                lstPageNr.add(new Integer(startseite + 1));
                // back bottom
                lstPageNr.add(new Integer(pageCount - startseite + 1 - 1));
            } else if (perPage == 4) {
                startseite = ((i-1) * perPage)  + 1;

                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("Blatt:" + i + " Startseite: " + startseite); 
                
                // front top left
                lstPageNr.add(new Integer(pageCount - startseite + 1));
                // front top right
                lstPageNr.add(new Integer(startseite));
                // front bottom lefts
                lstPageNr.add(new Integer(pageCount - startseite + 1 - 2));
                // front bottom right
                lstPageNr.add(new Integer(startseite + 2));

                // back top left
                lstPageNr.add(new Integer(startseite + 1));
                // back top right
                lstPageNr.add(new Integer(pageCount - startseite + 1 - 1));
                // back bottom left
                lstPageNr.add(new Integer(startseite + 1 + 2));
                // back bottom right
                lstPageNr.add(new Integer(pageCount - startseite + 1 - 1 - 2));
            } else {
                throw new IllegalArgumentException("Sorry, perPage must "
                   + "only be 2 or 4. All other is not implemented yet :-(");
            }
        }
        if (LOGGER.isInfoEnabled())
            LOGGER.info("Seiten:" + lstPageNr.size()); 
        
        // copy pages
        for (Iterator iter = lstPageNr.iterator(); iter.hasNext(); ) {
            int pageNum = ((Integer) iter.next()).intValue();
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("addSortPage: " + pageNum); 
            page = writerSorted.getImportedPage(readerOdd, pageNum);
            writerSorted.addPage(page);
        }

        // close everything
        documentSorted.close();
        writerSorted.close();
        readerOdd.close();
        
        // delete Tmp-File
        File file = new File(oddFile);
        file.delete();
    }
}
