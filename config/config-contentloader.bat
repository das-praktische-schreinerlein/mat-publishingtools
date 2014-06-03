rem <h4>FeatureDomain:</h4>
rem     Publishing
rem <h4>FeatureDescription:</h4>
rem     export variables and config for use contenttools (Browser...)
rem 
rem @author Michael Schreiner <michael.schreiner@your-it-fellow.de>
rem @category Publishing
rem @copyright Copyright (c) 2011-2014, Michael Schreiner
rem @license http://mozilla.org/MPL/2.0/ Mozilla Public License 2.0


rem set mypath
set CONTENTTOOLCONFIGPATH=%~dp0
set CONTENTTOOLBASEPATH=%CONTENTTOOLCONFIGPATH%..\sbin\

rem set cmd
set HTML2PDF="C:\ProgrammePortable\wkhtmltopdf\wkhtmltopdf.exe" --debug-javascript --page-width 150 --page-height 212 --redirect-delay 20000 
set HTML2PNG="C:\ProgrammePortable\wkhtmltopdf0.11\wkhtmltoimage.exe" --debug-javascript --width 606  --disable-smart-width --crop-w 600 --crop-x 4 --javascript-delay 20000 
set HTML2PDFV2="C:\ProgrammePortable\PortableApps\PortableApps\FirefoxPortable\App\Firefox"\firefox -printmode pdf -printdelay 10
set CONTENTLOADER=%CONTENTTOOLBASEPATH%\contentloader-url2pdf.bat
rem set HTML2PDFFLGV2=

rem set Url-Config
rem set PROXY=-p 10.0.1.200:5528
set PROXY=
set BASEURL=http://localhost/

rem set path
set OUTDIR=D:\tmp\buch\
set CACHEDIR=D:\tmp\buchcache\
if not exist "%OUTDIR%" mkdir %OUTDIR%
if not exist "%CACHEDIR%" mkdir %CACHEDIR%
