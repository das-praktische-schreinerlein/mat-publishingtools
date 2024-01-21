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

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <h4>FeatureDomain:</h4>
 *     Publishing
 * <h4>FeatureDescription:</h4>
 *     merge pdf-files
 * 
 * @package de.mat.utils.pdftools
 * @author Michael Schreiner <michael.schreiner@your-it-fellow.de>
 * @category Publishing
 * @copyright Copyright (c) 2011-2014, Michael Schreiner
 * @license http://mozilla.org/MPL/2.0/ Mozilla Public License 2.0
 */
public class PdfMerge extends CmdLineJob {
    
    /**
     * Bookmark with sourcefile-data fro merge and TOC
     * @param <String> - SRC=filename / NAME=label / PAGE=pagenum / PAGES=pagecount /
     *                   TYPE=for styling [ue,ue2,master,img]
     * @param <Object> - all
     */
    public static class Bookmark<String, Object> extends HashMap<String, Object> {
    }

    public PdfMerge(String[] args) {
        super(args);
    }

    // Logger
    private static final Logger LOGGER =
            Logger.getLogger(PdfMerge.class);

    // define Patterns
    public static final String CONST_PATTERN =
                    "^(.*)\\t(.*)\\t(.*)\\t(.*)$" ;
    private static final Pattern CONST_BOOKMARK =
                    Pattern.compile(CONST_PATTERN);

    
    @Override
    protected boolean validateCmdLine(CommandLine cmdLine) throws Throwable {
        if (cmdLine.getArgList().size() < 1) {
            return false;
        }
        return true;
    }

    @Override
    protected void printUsage() throws Throwable  {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(this.getJobName() + " DestPdfFile [SrcPdfFile1]... [SrcPdfFileN]",
                        "Desc: merge all SrcPdfFile1-N or the files from Option filelist to DestPdfFile."
                        + " If option trim is set -> trim all empty pages. As result a htmlTOC will print.",
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

        // Dont Parse List
        Option file = new Option("f", "filelist", true,
        "Parse list of pdffiles from tabseaparated file with structure (PDFFILE "
        + "LABEL TYPE PAGE)"
        + "\nTYPE and PAGE can be empty"
        + "\nTYPE is used to style the TOC 'bookmark_line_TYPE' - ue, ue2, master, img are predefined");
        file.setRequired(false);
        availiableCmdLineOptions.addOption(file);

        // trip empty pages
        Option flgTrim = new Option("t", "trim", false,
        "Trim empty pages");
        flgTrim.setRequired(false);
        availiableCmdLineOptions.addOption(flgTrim);

        Option exportToc = new Option("e", "exporttoc", true,
                "export TOC-file");
        exportToc.setRequired(false);
        availiableCmdLineOptions.addOption(exportToc);

        Option tocTemplate = new Option("", "toctemplate", true,
                "toc-template");
        tocTemplate.setRequired(false);
        availiableCmdLineOptions.addOption(tocTemplate);

        return availiableCmdLineOptions;
    }

    @Override
    public void doJob() throws Throwable {
        String fileNew = this.cmdLine.getArgs()[0];

        String listFile = this.cmdLine.getOptionValue("f", null);
        List<Bookmark> lstBookMarks = new ArrayList<>();
        if (listFile != null) {
            // read bookmarks from file
            lstBookMarks = parseBookMarksFromFile(listFile);
        } else {
            // all args are sourcefiles
            for (int zaehler = 1; zaehler < this.cmdLine.getArgs().length; zaehler++) {
                // setup Bookmark
                Bookmark mpBookMark = new Bookmark();
                String fileName = this.cmdLine.getArgs()[zaehler];
                mpBookMark.put("SRC", fileName);
                mpBookMark.put("NAME", this.cmdLine.getArgs()[zaehler]);
                lstBookMarks.add(mpBookMark);
            }
        }

        // merge pdfs
        mergePdfs(lstBookMarks, fileNew, this.cmdLine.hasOption("t"));
        
        // create toc
        String html = showBookMarksAsHtml(lstBookMarks);

        // parse toc-template
        String tocTemplateFile = this.cmdLine.getOptionValue("toctemplate", null);
        if (tocTemplateFile != null && !tocTemplateFile.trim().isEmpty()) {
            html = String.join("\n", Files.readAllLines(new File(tocTemplateFile).toPath()))
                    .replace("{{TOC}}", html);
        }

        // export toc
        String exportTocFile = this.cmdLine.getOptionValue("e", null);
        if (exportTocFile != null && !exportTocFile.trim().isEmpty()) {
            try (PrintWriter out = new PrintWriter(exportTocFile)) {
                out.println(html);
            }
        }

        // print toc
        System.out.println(html);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        PdfMerge me = new PdfMerge(args);
        me.startJobProcessing();
    }

    
    
    /**
     * <h4>FeatureDomain:</h4>
     *     PublishingTools
     * <h4>FeatureDescription:</h4>
     *     merge pdfs from lstBookMarks to fileNew and trim empty pages if flgTrim 
     *     is set
     * <h4>FeatureResult:</h4>
     *   <ul>
     *     <li>create PDF - fileNew
     *     <li>updates lstBookMarks - updates PAGE (firstPageNum) and 
     *                                PAGES (countPage= per Bookmark
     *   </ul> 
     * <h4>FeatureKeywords:</h4>
     *     PDF Publishing
     * @param lstBookMarks - list of Bookmark (files to merge)
     * @param fileNew - destination PDF filename
     * @param flgTrim - trim empty pages
     * @throws Exception
     */
    public static void mergePdfs(List<Bookmark> lstBookMarks, String fileNew, 
                                 boolean flgTrim) throws Exception {
        // FirstFile
        Map curBookMark = lstBookMarks.get(0);
        String curFileName = (String)curBookMark.get("SRC");

        // Neues Dokument anlegen aus 1. Quelldokument anlegen
        PdfReader reader = new PdfReader(curFileName);
        Document documentNew = new Document(reader.getPageSizeWithRotation(1));
        reader.close();
        PdfCopy writerNew = new PdfCopy(documentNew, new FileOutputStream(fileNew));
        documentNew.open();

        int siteNr = 1;
        for (Iterator iter = lstBookMarks.iterator(); iter.hasNext();) {
            curBookMark = (Map)iter.next();
            curFileName = (String)curBookMark.get("SRC");

            if (LOGGER.isInfoEnabled())
                LOGGER.info("add File:" + curFileName);
            
            // copy Page
            reader = new PdfReader(curFileName);
            int newPages = PdfExtractEmptyPages.addTrimmedPages(curFileName, reader, writerNew, (PdfCopy)null, flgTrim);
            reader.close();

            // update BookMark
            curBookMark.put("PAGE", new Integer(siteNr));
            curBookMark.put("PAGES", new Integer(newPages));
            siteNr += newPages;
        }
        documentNew.close();
        writerNew.close();
    }
    
    /**
     * <h4>FeatureDomain:</h4>
     *     PublishingTools
     * <h4>FeatureDescription:</h4>
     *     parse bookmarks from file
     * <h4>FeatureResult:</h4>
     *   <ul>
     *     <li>returnValue lstBookMarks - list of Bookmarks (Filename, Label...)
     *   </ul> 
     * <h4>FeatureKeywords:</h4>
     *     PDF Publishing
     * @param listFile - file with the Bookmarks
     * @return - list of Bookmarks
     * @throws Throwable
     */
    public static List<Bookmark> parseBookMarksFromFile(String listFile) throws Throwable {
        // BookMarkliste aus Datei lesen
        List<Bookmark> lstBookMarks = new ArrayList<>();
        String fileContent = readFromFile(listFile);
        String[] lines = fileContent.split("\n");

        String baseDir = (new File(listFile)).getParentFile().getAbsolutePath();
        if (lines != null) {
            // alle Zeilen durchlaufen
            for (int zaehler = 0; zaehler < lines.length; zaehler++) {
                // 
                String line = lines[zaehler];
                line = line.replace("\r", "");
                line = line.replace("\n", "");

                List<MatchResult> matches = findMatches(CONST_BOOKMARK, line);
                if (matches == null || matches.size() <= 0) {
                    continue;
                }

                //iterate matches
                for (MatchResult match : matches) {

                    // if found: add bookmark to list
                    if (match.groupCount() == 4) {
                        Bookmark mpBookMark = 
                           new Bookmark();
                        String fileName =  match.group(1);
                        fileName = fileName.replaceAll("\\+", "_");
                        fileName = fileName.replaceAll(">", "_");

                        if (!new File(fileName).exists()) {
                            if (fileName.matches("^[a-zA-Z\\.0-9]{1}.*")) {
                                fileName = baseDir + '/' + fileName;
                                System.err.println("remap file with basedir: " + baseDir + " fileName: " + fileName);
                            }
                        }

                        mpBookMark.put("SRC", fileName);
                        mpBookMark.put("NAME", match.group(2));
                        mpBookMark.put("TYPE", match.group(3));
                        mpBookMark.put("PAGE", match.group(4));
                        lstBookMarks.add(mpBookMark);
                    }
                }
            }
        }
        return lstBookMarks;
    }

    
    /**
     * <h4>FeatureDomain:</h4>
     *     PublishingTools
     * <h4>FeatureDescription:</h4>
     *     create html-TOC from Bookmark-list
     * <h4>FeatureResult:</h4>
     *   <ul>
     *     <li>returnValue htmlnippet - html-snippet for TOC 
     *   </ul> 
     * <h4>FeatureKeywords:</h4>
     *     PDF Publishing
     * @param lstBookMarks - list of Bookmarks (Filename, Label...)
     * @return - html-snippet
     * @throws Throwable
     */
    public static String showBookMarksAsHtml(List<Bookmark> lstBookMarks) throws Throwable {
        String res = "";
        
        Object curFileName = null;
        Object curName = null;
        Object curPage = null;
        Object curType = null;
        
        // iterate bookmarks
        for (Bookmark curBookMark : lstBookMarks) {
            // extract data
            curFileName = curBookMark.get("SRC");
            curName = curBookMark.get("NAME");
            curPage = curBookMark.get("PAGE");
            curType = curBookMark.get("TYPE");
            
            // create html
            String lineStyle = "bookmark_line";
            if (curName.toString().startsWith("Region:")) {
                lineStyle += " bookmark_line_region";
            }
            if (curType != null && curType.toString().equalsIgnoreCase("ue")) {
                lineStyle += " bookmark_line_ue";
            } else if (curType != null && curType.toString().equalsIgnoreCase("ue2")) {
                lineStyle += " bookmark_line_ue2";
            } else if (curType != null && curType.toString().equalsIgnoreCase("master")) {
                lineStyle += " bookmark_line_region_master";
            } else if (curType != null && curType.toString().equalsIgnoreCase("img")) {
                lineStyle += " bookmark_line_img";
            } else if (curType != null) {
                lineStyle += " bookmark_line_" + curType;
            }
            res += "<div class='" + lineStyle + "'>"
                + "<div class='bookmark_file'>" + curFileName + "</div>"
                + "<div class='bookmark_name'>" + curName + "</div>"
                + "<div class='bookmark_page'>" + curPage + "</div>"
                + "</div>\n";
        }
        
        return res;
    }
    
    /**
     * <h4>FeatureDomain:</h4>
     *     PublishingTools
     * <h4>FeatureDescription:</h4>
     *     find macthes for pattern in haystack
     * <h4>FeatureResult:</h4>
     *   <ul>
     *     <li>returnValue list - list of Matches
     *   </ul> 
     * <h4>FeatureKeywords:</h4>
     *     PatternMatching
     * @param pattern - pattern to find
     * @param haystack - haystack
     * @return - list of Matches
     */
    public static List<MatchResult> findMatches( Pattern pattern, CharSequence haystack ) {
        List<MatchResult> results = null;

        for ( Matcher m = pattern.matcher(haystack); m.find(); ) {
            if (results == null)
                results = new ArrayList<MatchResult>();
            results.add( m.toMatchResult() );
        }

        return results;
    }
    
    /**
     * <h4>FeatureDomain:</h4>
     *     PublishingTools
     * <h4>FeatureDescription:</h4>
     *     reads the file
     * <h4>FeatureResult:</h4>
     *   <ul>
     *     <li>returnValue String - filecontent
     *   </ul> 
     * <h4>FeatureKeywords:</h4>
     *     Filehandling
     * @param fileName - file to read
     * @return - filecontent
     * @throws Exception
     */
    public static String readFromFile(String fileName) throws Exception {
        // check parameter
        if (fileName == null || fileName.trim().length() <= 0) {
            throw new IllegalArgumentException("FileName must not be empty: '" + fileName + "'");
        }

        // read file
        File file = new File(fileName);
        FileReader fileReader = new FileReader(file);
        BufferedReader br = new BufferedReader(fileReader);
        String fileContent = "";
        String line = null;
        while((line = br.readLine()) != null){
            fileContent += line + "\n";
        }
        br.close();

        return fileContent;
    }

}
