package org.apache.pdfbox.multipdf;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.COSObjectable;

public class XmapPDFCloneUtility extends PDFCloneUtility {

    public XmapPDFCloneUtility(PDDocument dest) { // make constructor public
        super(dest);
    }
    
    @Override
    public void cloneMerge(COSObjectable base, COSObjectable target) throws IOException { // make public
        super.cloneMerge(base, target);
    }
}
