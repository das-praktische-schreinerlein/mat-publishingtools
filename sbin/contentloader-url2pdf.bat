rem <h4>FeatureDomain:</h4>
rem     Publishing
rem <h4>FeatureDescription:</h4>
rem     save URL with Webkit or firefox as Pdf/Image to FILENAME.
rem     if cachefile exists use that 
rem 
rem @author Michael Schreiner <michael.schreiner@your-it-fellow.de>
rem @category Publishing
rem @copyright Copyright (c) 2011-2014, Michael Schreiner
rem @license http://mozilla.org/MPL/2.0/ Mozilla Public License 2.0

set TYPE=%1%
set URL=%2%
set FORCE=%3%
set ID=%4%
set FILENAME=%5%
set VERSION=%6%

if "%TYPE%" == "" goto usage
if "%URL%" == "" goto usage
if "%FORCE%" == "" goto usage
if "%ID%" == "" goto usage
if "%FILENAME%" == "" goto usage

rem Params auswerten
set LOADVERSION=FF
if "%HTML2PDFFLGV2%" == "" set LOADVERSION=WK
if "%VERSION%" == "Firefox" set LOADVERSION=FF

set EXTENSION=pdf
if "%HTML2IMGFLG%" == "png" set EXTENSION=png


rem Version stat. belegen
rem set LOADVERSION=WK

rem Vars belegen
set CACHEFILENAME=%CACHEDIR%%TYPE%-%ID%.%EXTENSION%

rem feature nur laden, wenn noch nicht vorhanden
if not exist "%CACHEFILENAME%" goto loadfeature
if "%FORCE%" == "1" goto loadfeature
goto copyfeature

:loadfeature
if "%EXTENSION%" == "png" goto loadfeaturePNG
if "%LOADVERSION%" == "FF" goto loadfeatureFF
goto loadfeatureWK

:loadfeatureFF
call %HTML2PDFV2% -printurl "%URL%"   -printfile %CACHEFILENAME%
sleep 10
goto copyfeature

:loadfeatureWK
%HTML2PDF% %PROXY%  "%URL%"   %CACHEFILENAME%
goto copyfeature

:loadfeaturePNG
%HTML2PNG% %PROXY%  "%URL%"   %CACHEFILENAME%
goto copyfeature


:copyfeature
copy %CACHEFILENAME% %OUTDIR%%FILENAME%
goto finish

:usage
echo usage: %0 TYPE URL FORCE(0/1) ID FILENAME [VERSION=Webkit(default)/Firefox]
echo save URL with Webkit or firefox as Pdf/Image to FILENAME.
echo if cachefile exists use that 
exit /B 1

:finish
exit /B 0

