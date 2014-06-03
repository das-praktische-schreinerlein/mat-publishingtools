rem <h4>FeatureDomain:</h4>
rem     Publishing
rem <h4>FeatureDescription:</h4>
rem     export variables and config for use of pdftools
rem 
rem @package de.mat.utils.pdftools
rem @author Michael Schreiner <michael.schreiner@your-it-fellow.de>
rem @category Publishing
rem @copyright Copyright (c) 2011-2014, Michael Schreiner
rem @license http://mozilla.org/MPL/2.0/ Mozilla Public License 2.0


rem set mypath
set PDFTOOLCONFIGPATH=%~dp0
set PDFTOOLBASEPATH=%PDFTOOLCONFIGPATH%..\sbin\
set PDFTOOLBASEPATH=%~dp0

rem set cmd
set CP="%PDFTOOLBASEPATH%..\target\matpublishingtools-1.0-SNAPSHOT-jar-with-dependencies.jar"
set JAVAOPTIONS=-Xmx512m -Xms128m -Dlog4j.configuration=file:%PDFTOOLCONFIGPATH%\log4j.properties
set TRIM=java -cp %CP% %JAVAOPTIONS% de.mat.utils.pdftools.PdfExtractEmptyPages
set PDFMERGE=java -cp %CP% %JAVAOPTIONS% de.mat.utils.pdftools.PdfMerge
set PDFADDPAGENUM=java -cp %CP% %JAVAOPTIONS% de.mat.utils.pdftools.PdfAddPageNum
set PDFRESIZE=java -cp %CP% %JAVAOPTIONS% de.mat.utils.pdftools.PdfResize
set PDFSORT4PRINT=java -cp %CP% %JAVAOPTIONS% de.mat.utils.pdftools.PdfSort4Print

