package de.redsix.pdfcompare.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import org.apache.pdfbox.pdmodel.PDDocument;

import de.redsix.pdfcompare.CompareResultImpl;
import de.redsix.pdfcompare.PdfComparator;
import de.redsix.pdfcompare.ResourceCacheWithLimitedImages;


public class OnDemandPdfComparator<T extends OnDemandCompareResult> extends PdfComparator<T> {


    private OnDemandPdfComparator(T compareResult) {
        super(compareResult);
    }

    /**
     * Compare two PDFs by providing two filenames for the expected PDF and the actual PDF.
     *
     * @param expectedPdfFilename filename for the expected PDF
     * @param actualPdfFilename   filename for the actual PDF
     */
    public OnDemandPdfComparator(String expectedPdfFilename, String actualPdfFilename) {
        super(expectedPdfFilename, actualPdfFilename);
    }

    /**
     * Compare two PDFs by providing two filenames for the expected PDF and the actual PDF.
     *
     * @param expectedPdfFilename filename for the expected PDF
     * @param actualPdfFilename   filename for the actual PDF
     * @param compareResult       the CompareResult to use during this compare. Allows to provide CompareResultImpl Subtypes with Swapping for example.
     */
    public OnDemandPdfComparator(String expectedPdfFilename, String actualPdfFilename, T compareResult) {
        super(expectedPdfFilename, actualPdfFilename, compareResult);
    }

    /**
     * Compare two PDFs by providing two Path objects for the expected PDF and the actual PDF.
     *
     * @param expectedPath Path for the expected PDF
     * @param actualPath   Path for the actual PDF
     */
    public OnDemandPdfComparator(final Path expectedPath, final Path actualPath) {
        super(expectedPath, actualPath);
    }

    /**
     * Compare two PDFs by providing two Path objects for the expected PDF and the actual PDF.
     *
     * @param expectedPath  Path for the expected PDF
     * @param actualPath    Path for the actual PDF
     * @param compareResult the CompareResult to use during this compare. Allows to provide CompareResultImpl Subtypes with Swapping for example.
     */
    public OnDemandPdfComparator(final Path expectedPath, final Path actualPath, final T compareResult) {
        super(expectedPath, actualPath, compareResult);
    }

    /**
     * Compare two PDFs by providing two File objects for the expected PDF and the actual PDF.
     *
     * @param expectedFile File for the expected PDF
     * @param actualFile   File for the actual PDF
     */
    public OnDemandPdfComparator(final File expectedFile, final File actualFile) {
        this(expectedFile, actualFile, (T) new CompareResultImpl());
    }

    /**
     * Compare two PDFs by providing two File objects for the expected PDF and the actual PDF.
     *
     * @param expectedFile  File for the expected PDF
     * @param actualFile    File for the actual PDF
     * @param compareResult the CompareResult to use during this compare. Allows to provide CompareResultImpl Subtypes with Swapping for example.
     */
    public OnDemandPdfComparator(final File expectedFile, final File actualFile, final T compareResult) {
        super(expectedFile, actualFile, compareResult);
    }

    /**
     * Compare two PDFs by providing two InputStream objects for the expected PDF and the actual PDF.
     *
     * @param expectedPdfIS InputStream for the expected PDF
     * @param actualPdfIS   InputStream for the actual PDF
     */
    public OnDemandPdfComparator(final InputStream expectedPdfIS, final InputStream actualPdfIS) {
        super(expectedPdfIS, actualPdfIS);
    }

    /**
     * Compare two PDFs by providing two InputStream objects for the expected PDF and the actual PDF.
     *
     * @param expectedPdfIS InputStream for the expected PDF
     * @param actualPdfIS   InputStream for the actual PDF
     * @param compareResult the CompareResult to use during this compare. Allows to provide CompareResultImpl Subtypes with Swapping for example.
     */
    public OnDemandPdfComparator(final InputStream expectedPdfIS, final InputStream actualPdfIS, final T compareResult) {
        super(expectedPdfIS, actualPdfIS, compareResult);
    }


    @Override
    protected void compare(final PDDocument expectedDocument, final PDDocument actualDocument) throws IOException {
        expectedDocument.setResourceCache(new ResourceCacheWithLimitedImages(getEnvironment()));
        actualDocument.setResourceCache(new ResourceCacheWithLimitedImages(getEnvironment()));

        T compareResult = getResult();
        compareResult.setExclusions(getExclusions());
        compareResult.setDocuments(expectedDocument, actualDocument);
    }

}
