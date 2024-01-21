mat-publishingtools
=====================

# Desc
This is a subset of publishing-tools created for Michas-Books on www.michas-ausflugstipps.de/portal-bucherstellung.html

1. PDF-tools for converting, merging, trimming and so on
2. batchfiles for support
3. batchfiles for generating a book with pagenum and TOC from different webpages

# TODO for me
- [ ] documentation
- [ ] use and optimize it :-)

# History and milestones
- 2023
   - added webshot2pdf
   - added nodejs-wrapper for pdf-tools
   - improved pdf-tools (added option to save toc to file, remap relative filenames with bookmarkpath)
- 2014 
   - prepared the tools for going public (documentation...) 
   - separated the public-tools
- 2011
   - initial version for www.michas-ausflugstipps.de

# Requires
- for building
   - maven
   - IDE (I built it with eclipse)
- to manage pdfs
   - java
- to generate pdfs
   - wkhtmltopdf-0.9.9
   - optional wkhtmltopdf-0.11
   - optional firefox + https://github.com/das-praktische-schreinerlein/Mat-CmdPrinting
- to convert pdfs
   - optional FreePDF 4.12
   - optional ghostScript 9.07

# Install
- save the project to 
```bat
d:\public_projects\MatPublishingTools
```

- import project to Eclipse

- run maven 
```bat
cd d:\public_projects\MatPublishingTools
mvn compile
mvn org.apache.maven.plugins:maven-assembly-plugin:assembly
```

# Configure
- update pathes in 
   - config/config-contentloader.bat
   - config/config-converter.bat
   - config/config-pdftools.bat

# Example
- change  
   - src/test/example/buch-example.bat (define urls in section load)
   - src/test/example/buch-example.lst (list your file from example.bat section load)

- run test
```bat
cd d:\public_projects\MatPublishingTools
src\test\example\buch-example.bat
```

# Thanks to
- https://github.com/itext/itextpdf
- https://github.com/wkhtmltopdf/wkhtmltopdf 

# License
```
/**
 * @author Michael Schreiner <ich@michas-ausflugstipps.de>
 * @category publishing
 * @copyright Copyright (c) 2010-2014, Michael Schreiner
 * @license http://mozilla.org/MPL/2.0/ Mozilla Public License 2.0
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
```
