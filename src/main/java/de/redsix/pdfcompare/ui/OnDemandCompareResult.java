package de.redsix.pdfcompare.ui;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.multipdf.PDFCloneUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.redsix.pdfcompare.CompareResultWithExpectedAndActual;
import de.redsix.pdfcompare.DiffImage;
import de.redsix.pdfcompare.Exclusions;
import de.redsix.pdfcompare.ImageWithDimension;
import de.redsix.pdfcompare.PageArea;
import de.redsix.pdfcompare.PageDiffCalculator;
import de.redsix.pdfcompare.PdfComparator;
import de.redsix.pdfcompare.StacktraceImage;


public class OnDemandCompareResult extends CompareResultWithExpectedAndActual {
    
    private static final Logger LOG = LoggerFactory.getLogger(OnDemandCompareResult.class);
    
    private PDDocument expectedDocument;
    private PDDocument actualDocument;
    private PDFRenderer expectedPdfRenderer;
    private PDFRenderer actualPdfRenderer;
    private Exclusions exclusions;
    private Set<Integer> pages = new HashSet<>();
    
    
    /** NB: this is not a stable clone mechanism and barely does the job. */
    static private PDDocument clone(PDDocument doc) throws IOException {
        
        PDDocument target= new PDDocument();
        
        PDFCloneUtility util = new PDFCloneUtility(target);
//        util.cloneMerge(util.cloneForNewDocument(doc.getDocument()), target.getDocument());
        util.cloneMerge(doc.getDocumentCatalog(), target.getDocumentCatalog());

        for (int i = 0; i < doc.getNumberOfPages(); i++) {
            PDPage page = doc.getDocumentCatalog().getPages().get(i);
            PDPage newPage = new PDPage( (COSDictionary) util.cloneForNewDocument( page.getCOSObject() ) );
            target.importPage(newPage);
        }

        return target;
    }
    
    public void setDocuments(PDDocument expectedDocument, PDDocument actualDocument) {
        try {
            this.expectedDocument = clone(expectedDocument);
            this.actualDocument = clone(actualDocument);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.expectedPdfRenderer = new PDFRenderer(this.expectedDocument);
        this.actualPdfRenderer = new PDFRenderer(this.actualDocument);
    }
    
    public void setExclusions(Exclusions exclusions) {
        this.exclusions = exclusions;
    }
    
    public Exclusions getExclusions() {
        return this.exclusions;
    }

    @Override
    public synchronized void addPage(PageDiffCalculator diffCalculator, int pageIndex, ImageWithDimension expectedImage, ImageWithDimension actualImage, ImageWithDimension diffImage) {
        this.pages.add(Integer.valueOf(pageIndex));
        super.addPage(diffCalculator, pageIndex, expectedImage, actualImage, diffImage);
    }
    
    @Override
    public BufferedImage getExpectedImage(int pageIndex) {
        if (! pages.contains(pageIndex)) {
            compare(pageIndex);
        }
        
        return super.getExpectedImage(pageIndex);
    }
    
    @Override
    public BufferedImage getActualImage(int pageIndex) {
        if (! pages.contains(pageIndex)) {
            compare(pageIndex);
        }
        
        return super.getActualImage(pageIndex);
    }
    
    @Override
    public BufferedImage getDiffImage(int pageIndex) {
        if (! pages.contains(pageIndex)) {
            compare(pageIndex);
        }
        
        return super.getDiffImage(pageIndex);
    }
    
    @Override
    public int getNumberOfPages() {
        if (expectedDocument == null || actualDocument == null) {
            return 0;
        }
        
        int pageCount = Math.max(expectedDocument.getNumberOfPages(), actualDocument.getNumberOfPages());
        return pageCount;
    }
    
    private void compare(int pageIndex) {
        if (expectedDocument == null || actualDocument == null) {
            return;
        }
        if (pageIndex < 0) {
            return;
        }
        
        final int minPageCount = Math.min(expectedDocument.getNumberOfPages(), actualDocument.getNumberOfPages());
        
        try {
            if (pageIndex < minPageCount) {
                drawImage(pageIndex);
            } else {
                if (expectedDocument.getNumberOfPages() > minPageCount) {
                    addExtraPage(expectedDocument, expectedPdfRenderer, pageIndex, environment.getActualColor().getRGB(), true);
                } else
                if (actualDocument.getNumberOfPages() > minPageCount) {
                    addExtraPage(actualDocument, actualPdfRenderer, pageIndex, environment.getExpectedColor().getRGB(), false);
                }
            }
        } catch (Throwable t) {
            addErrorPage(pageIndex, "An error occurred, while rendering this page", t);
        }
    }

    private void drawImage(final int pageIndex) throws IOException {
        if (expectedDocument == null || actualDocument == null) {
            return;
        }
        
        LOG.trace("Drawing page {}", pageIndex);
        final ImageWithDimension expectedImage = PdfComparator.renderPageAsImage(expectedDocument, expectedPdfRenderer, pageIndex, environment);
        final ImageWithDimension actualImage = PdfComparator.renderPageAsImage(actualDocument, actualPdfRenderer, pageIndex, environment);
        final DiffImage diffImage = new DiffImage(expectedImage, actualImage, pageIndex, environment, getExclusions(), this);
        LOG.trace("Enqueueing page {}.", pageIndex);

        LOG.trace("Diffing page {}", diffImage);
        try {
            diffImage.diffImages();
        } catch (Throwable t) {
            addErrorPage(pageIndex, "An error occurred, while diffing this page", t);
        }
        LOG.trace("DONE Diffing page {}", diffImage);

        LOG.trace("DONE drawing page {}", pageIndex);
    }

    private void addExtraPage(final PDDocument document, final PDFRenderer pdfRenderer, int pageIndex, final int color, final boolean expected) throws IOException {
        ImageWithDimension image = PdfComparator.renderPageAsImage(document, pdfRenderer, pageIndex, environment);
        final DataBuffer dataBuffer = image.bufferedImage.getRaster().getDataBuffer();
        for (int i = 0; i < image.bufferedImage.getWidth() * PdfComparator.MARKER_WIDTH; i++) {
            dataBuffer.setElem(i, color);
        }
        for (int i = 0; i < image.bufferedImage.getHeight(); i++) {
            for (int j = 0; j < PdfComparator.MARKER_WIDTH; j++) {
                dataBuffer.setElem(i * image.bufferedImage.getWidth() + j, color);
            }
        }
        if (expected) {
            addPage(new PageDiffCalculator(new PageArea(pageIndex + 1)), pageIndex, image, blank(image), image);
        } else {
            addPage(new PageDiffCalculator(new PageArea(pageIndex + 1)), pageIndex, blank(image), image, image);
        }
    }

    private void addErrorPage(int pageIndex, String message, Throwable t) {
        LOG.error(message, t);
        StacktraceImage stacktraceImage = new StacktraceImage(message, t, environment);
        ImageWithDimension errorImage = stacktraceImage.getImage();
        addPage(new PageDiffCalculator(new PageArea(pageIndex + 1)), pageIndex, stacktraceImage.getBlankImage(), errorImage, errorImage);
    }
    
    private static ImageWithDimension blank(final ImageWithDimension image) {
        return new ImageWithDimension(
                new BufferedImage(image.bufferedImage.getWidth(), image.bufferedImage.getHeight(), image.bufferedImage.getType()),
                image.width, image.height);
    }

}
