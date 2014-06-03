rem <h4>FeatureDomain:</h4>
rem     Publishing
rem <h4>FeatureDescription:</h4>
rem     generate a book with pagenum and TOC from different urls
rem 
rem @author Michael Schreiner <michael.schreiner@your-it-fellow.de>
rem @category Publishing
rem @copyright Copyright (c) 2011-2014, Michael Schreiner
rem @license http://mozilla.org/MPL/2.0/ Mozilla Public License 2.0


rem set mypath
set EXAMPLEBASEPATH=%~dp0
set PUBLISHBASEPATH=%EXAMPLEBASEPATH%\..\..\..\
set PUBLISHSRIPTPATH=%PUBLISHBASEPATH%\sbin\
set PUBLISHCONFIGPATH=%PUBLISHBASEPATH%\config\

rem init configs
call %PUBLISHCONFIGPATH%\config-contentloader.bat
call %PUBLISHCONFIGPATH%\config-pdftools.bat
call %PUBLISHCONFIGPATH%\config-converter.bat

rem configure
set HTML2PDFFLGV2=
set HTML2IMGFLG=
set FILEBASE=example
set FORCEUPDATE=0

rem start
rem goto mergeall
rem goto gentoc
rem goto merge
rem goto load


:load
rem load urls to pdf
call %CONTENTLOADER% page http://www.tagesschau.de/ %FORCEUPDATE% tagesschau %FILEBASE%-tagesschau.pdf WK
call %CONTENTLOADER% page http://www.heise.de/ %FORCEUPDATE% heise %FILEBASE%-heise.pdf WK
call %CONTENTLOADER% page http://www.google.de/ %FORCEUPDATE% google %FILEBASE%-google.pdf WK
call %CONTENTLOADER% page http://www.google.de/ %FORCEUPDATE% google %FILEBASE%-google.pdf WK


:merge
rem merge the files from buch-%FILEBASE%.lst
del %OUTDIR%buch-%FILEBASE%.pdf
%PDFMERGE% %OUTDIR%buch-%FILEBASE%.pdf -t -f %EXAMPLEBASEPATH%\buch-%FILEBASE%.lst > %EXAMPLEBASEPATH%\buch-%FILEBASE%-toc.draft
type %EXAMPLEBASEPATH%\toc-header.html %EXAMPLEBASEPATH%\buch-%FILEBASE%-toc.draft  %EXAMPLEBASEPATH%\toc-footer.html >  %EXAMPLEBASEPATH%\buch-%FILEBASE%-toc.html
 
:pagenum
rem add pagenum to pdf
del %OUTDIR%buch-%FILEBASE%-pagenum.pdf
%PDFADDPAGENUM% %OUTDIR%buch-%FILEBASE%.pdf %OUTDIR%buch-%FILEBASE%-pagenum.pdf 1

:gentoc
rem generate TOC
del %OUTDIR%buch-%FILEBASE%-toc.pdf
%HTML2PDF% %PROXY% %EXAMPLEBASEPATH%\buch-%FILEBASE%-toc.html   %OUTDIR%buch-%FILEBASE%-toc.pdf

:mergeall
rem merge toc and pagenum.pdf
%PDFMERGE% %OUTDIR%buch-%FILEBASE%-a4.pdf -t %OUTDIR%buch-%FILEBASE%-toc.pdf %OUTDIR%buch-%FILEBASE%-pagenum.pdf

:convert
rem convert to printformats
%PDFSORT4PRINT% -p 2 -i %OUTDIR%buch-%FILEBASE%-a4.pdf -o %OUTDIR%buch-%FILEBASE%-a5print.pdf
%PDFSORT4PRINT% -p 4 -i %OUTDIR%buch-%FILEBASE%-a4.pdf -o %OUTDIR%buch-%FILEBASE%-a6print.pdf

rem all other convert for printing at www.wir-machen-druck.de must be done manual
rem manuell %GS% %GS_OPTIONS_ALL% %GS_OPTIONS_UMSCHLAG% -o print-buch-%FILEBASE%-umschlag-aussen.pdf buch-%FILEBASE%-umschlag-aussen.pdf
rem manuell %GS% %GS_OPTIONS_ALL% %GS_OPTIONS_UMSCHLAG% -o print-buch-%FILEBASE%-umschlag-innen.pdf buch-%FILEBASE%-umschlag-innen.pdf
rem manuell %GS% %GS_OPTIONS_ALL% %GS_OPTIONS_INNEN% -o print-buch-%FILEBASE%-druckerei-innenteil.pdf buch-%FILEBASE%-druckerei-innenteil.pdf

:end