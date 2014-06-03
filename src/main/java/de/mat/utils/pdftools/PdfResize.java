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
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import com.itextpdf.text.Document;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * <h4>FeatureDomain:</h4>
 *     Publishing
 * <h4>FeatureDescription:</h4>
 *     resize pdf-pages
 * 
 * @package de.mat.utils.pdftools
 * @author Michael Schreiner <michael.schreiner@your-it-fellow.de>
 * @category Publishing
 * @copyright Copyright (c) 2011-2014, Michael Schreiner
 * @license http://mozilla.org/MPL/2.0/ Mozilla Public License 2.0
 */
public class PdfResize extends CmdLineJob {

    public PdfResize(String[] args) {
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
        
        Option factorX = new Option("x", "scalex", true, "Scalierung-X");
        factorX.setRequired(true);
        availiableCmdLineOptions.addOption(factorX);

        Option factorY = new Option("y", "scaley", true, "Scalierung-Y");
        factorY.setRequired(true);
        availiableCmdLineOptions.addOption(factorY);

        Option offsetL = new Option("l", "offsetleft", true, "Offset-Left");
        offsetL.setRequired(true);
        availiableCmdLineOptions.addOption(offsetL);

        Option offsetT = new Option("t", "offsettop", true, "Offset-Top");
        offsetT.setRequired(true);
        availiableCmdLineOptions.addOption(offsetT);

       
        return availiableCmdLineOptions;
    }

    @Override
    protected boolean validateCmdLine(CommandLine cmdLine) throws Throwable {
        return true;
    }

    @Override
    protected void printUsage() throws Throwable  {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(this.getJobName() + " ",
                        "Desc: resize pdf-pages from srcfile 'i' to outfile 'o'."
                        + " Scale it and add offset to lft/top-corner"
                        + "",
                        this.availiableCmdLineOptions,
                        "");
    }

    @Override
    public void doJob() throws Throwable {
        String inFile = this.cmdLine.getOptionValue("i", null);
        String outFile = this.cmdLine.getOptionValue("o", null);
        Float scaleX = new Float(this.cmdLine.getOptionValue("x", null));
        Float scaleY = new Float(this.cmdLine.getOptionValue("y", null));
        Float pixelLeft = new Float(this.cmdLine.getOptionValue("l", null));
        Float pixelTop = new Float(this.cmdLine.getOptionValue("t", null));
        
        resizePdf(inFile, outFile, scaleX, scaleY, pixelLeft, pixelTop);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        PdfResize me = new PdfResize(args);
        me.startJobProcessing();
    }

    /**
    /**
     * <h4>FeatureDomain:</h4>
     *     PublishingTools
     * <h4>FeatureDescription:</h4>
     *     scales and move comntents of the pdf pages from fileSrc and output to
     *     fileNew
     * <h4>FeatureResult:</h4>
     *   <ul>
     *     <li>create PDF - fileNew
     *   </ul> 
     * <h4>FeatureKeywords:</h4>
     *     PDF Publishing
     * @param fileSrc - source-pdf
     * @param fileNew - scaled dest-pdf
     * @param factorX - scaling x
     * @param factorY - scaling y
     * @param pixelLeft - move right
     * @param pixelTop - move down
     * @throws Exception
     */
    public static void resizePdf(String fileSrc, String fileNew, 
        float factorX, float factorY, 
        float pixelLeft, float pixelTop) throws Exception {

        // open reader
        PdfReader reader = new PdfReader(fileSrc);
        
        // get pagebasedata
        int pageCount = reader.getNumberOfPages();
        Rectangle psize = reader.getPageSize(1);
        float width = psize.getHeight();
        float height = psize.getWidth();

        // open writer
        Document documentNew = new Document(new Rectangle(height*factorY, width*factorX));
        PdfWriter writerNew = PdfWriter.getInstance(documentNew, new FileOutputStream( fileNew ));
        documentNew.open();
        PdfContentByte cb = writerNew.getDirectContent();

        // iterate pages
        int i = 0;
        while (i < pageCount) {
            i++;
            // imoport page from reader and scale it to writer
            documentNew.newPage();
            PdfImportedPage page = writerNew.getImportedPage(reader, i);
            cb.addTemplate(page, factorX, 0, 0, factorY, pixelLeft, pixelTop);
            
            if (LOGGER.isInfoEnabled())
               LOGGER.info("AddPage " + i + " from:" + fileSrc + " to:" + fileNew);
        }

        documentNew.close();
        writerNew.close();
    }
    
}
