package de.redsix.pdfcompare;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;


public class PageExclusions {

    // order is first by y1, then x1, then y2, then x2
    static private final Comparator<PageArea> COMPARATOR = Comparator
            .comparingInt(PageArea::getY1)
            .thenComparingInt(PageArea::getX1)
            .thenComparingInt(PageArea::getY2)
            .thenComparingInt(PageArea::getX2)
            ;

    private final Collection<PageArea> exclusions = new TreeSet<>(COMPARATOR);
    private final PageExclusions delegate;
    

    public PageExclusions() {
        delegate = null;
    }

    public PageExclusions(final PageExclusions delegate) {
        this.delegate = delegate;
    }

    public void add(final PageArea exclusion) {
        exclusions.add(exclusion);
    }

    public void remove(final PageArea exclusion) {
        exclusions.remove(exclusion);
    }

    public boolean contains(final int x, final int y) {
        for (PageArea exclusion : exclusions) {
            if (exclusion.contains(x, y)) {
                return true;
            }
        }
        if (delegate != null) {
            return delegate.contains(x, y);
        }
        return false;
    }

    public Collection<PageArea> getExclusions() {
        return exclusions;
    }
}
