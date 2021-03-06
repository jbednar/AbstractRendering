package ar.glyphsets;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import ar.Glyph;
import ar.Glyphset;
import ar.glyphsets.implicitgeometry.Valuer;
import ar.util.axis.DescriptorPair;
import ar.util.Util;

/***Reduce a glyphset by only returning values that pass a given filter.**/
public final class FilterGlyphs<G, I> implements Glyphset<G,I> {
	private final Glyphset<G,I> base;
	private final Valuer<Glyph<G,I>, Boolean> predicate;

	private final boolean tightBounds;
	
	public FilterGlyphs(Glyphset<G,I> base, Valuer<Glyph<G,I>, Boolean> predicate) {this(base, predicate, false);}
	
	/**
	 * 
	 * @param base
	 * @param predicate
	 * @param tightBounds Should the bounds be for the subset (true) or is the base set a sufficient approximation (false) 
	 */
	public FilterGlyphs(Glyphset<G,I> base, Valuer<Glyph<G,I>, Boolean> predicate, boolean tightBounds) {
		this.base = base;
		this.predicate = predicate;
		this.tightBounds = tightBounds;
	}

	
	@Override public Iterator<Glyph<G, I>> iterator() {return new FilterIterator<>(base.iterator(), predicate);}
	@Override public boolean isEmpty() {return base.isEmpty();}
	@Override public Rectangle2D bounds() {
		if (tightBounds) {return Util.bounds(this);}
		else {return base.bounds();}
	}

	@Override public long size() {return base.size();}

	@Override
	public List<Glyphset<G, I>> segment(int count) throws IllegalArgumentException {
		return base.segment(count).stream()
				.map(s -> new FilterGlyphs<>(s, predicate))
				.collect(Collectors.toList());
	}
	
	
	@Override public DescriptorPair<?,?> axisDescriptors() {return base.axisDescriptors();}
	@Override public void axisDescriptors(DescriptorPair<?,?> descriptor) {base.axisDescriptors(descriptor);}

	public static final class FilterIterator<G, I> implements Iterator<Glyph<G,I>> {
		private final Iterator<Glyph<G,I>> base;
		private Glyph<G,I> next;
		final Valuer<Glyph<G,I>, Boolean> predicate;

		public FilterIterator(Iterator<Glyph<G,I>> base, Valuer<Glyph<G,I>, Boolean> predicate) {
			this.base = base;
			this.predicate = predicate;
		}
		
		@Override 
		public boolean hasNext() {
			if (!base.hasNext()) {return false;}
			while (base.hasNext() && next == null) {
				next = base.next();
				if (predicate.apply(next)) {break;}
				next = null;
			}
			return next != null;
		}
		
		@Override
		public Glyph<G,I> next() {
			if (next == null) {throw new NoSuchElementException();}
			Glyph<G,I> v = next;
			next = null;
			return v;
		}
		
		@Override public void remove() {throw new UnsupportedOperationException();}
	}
}