rem <h4>FeatureDomain:</h4>
rem     Publishing
rem <h4>FeatureDescription:</h4>
rem     export variables and config for use converter (GS......)
rem 
rem @author Michael Schreiner <michael.schreiner@your-it-fellow.de>
rem @category Publishing
rem @copyright Copyright (c) 2011-2014, Michael Schreiner
rem @license http://mozilla.org/MPL/2.0/ Mozilla Public License 2.0


rem set mypath
set CONTENTTOOLCONFIGPATH=%~dp0
set CONTENTTOOLBASEPATH=%CONTENTTOOLCONFIGPATH%..\sbin\

rem set pathes
set FREEPDF="C:\Program Files (x86)\FreePDF_XP\freepdf.exe"
set GS="C:\Program Files (x86)\gs\gs8.71\bin\gswin32"

rem set options
set GS_OPTIONS_ALL=-q -dNOSAFER -dNOPAUSE -dBATCH -sDEVICE=pdfwrite -dCompatibilityLevel=1.3 -dPDFSETTINGS=/prepress -dLockDistillerParams=false -dAutoRotatePages=/PageByPage -dEmbedAllFonts=true -dSubsetFonts=true -r600 -dDownsampleMonoImages=true -dMonoImageDownsampleThreshold=1.5 -dMonoImageDownsampleType=/Bicubic -dMonoImageResolution=600 -dDownsampleGrayImages=true -dGrayImageDownsampleThreshold=1.5 -dGrayImageDownsampleType=/Bicubic -dGrayImageResolution=300 -dDownsampleColorImages=true -dColorImageDownsampleThreshold=1.5 -dColorImageDownsampleType=/Bicubic -dColorImageResolution=150 -dConvertCMYKImagesToRGB=false
set GS_OPTIONS_UMSCHLAG=-sDEVICE=pdfwrite -dDEVICEWIDTHPOINTS=859 -dDEVICEHEIGHTPOINTS=1242 -dFIXEDMEDIA 
set GS_OPTIONS_INNEN=-sDEVICE=pdfwrite -dDEVICEWIDTHPOINTS=612 -dDEVICEHEIGHTPOINTS=859 -dFIXEDMEDIA 
set GS_OPTIONS_UMSCHLAG_A5=-sDEVICE=pdfwrite -dDEVICEWIDTHPOINTS=430 -dDEVICEHEIGHTPOINTS=621 -dFIXEDMEDIA 
set GS_OPTIONS_INNEN_A5=-sDEVICE=pdfwrite -dDEVICEWIDTHPOINTS=306 -dDEVICEHEIGHTPOINTS=430 -dFIXEDMEDIA 
set GS_OPTIONS_UMSCHLAG_A6=-sDEVICE=pdfwrite -dDEVICEWIDTHPOINTS=215 -dDEVICEHEIGHTPOINTS=311 -dFIXEDMEDIA 
set GS_OPTIONS_INNEN_A6=-sDEVICE=pdfwrite -dDEVICEWIDTHPOINTS=153 -dDEVICEHEIGHTPOINTS=215 -dFIXEDMEDIA 