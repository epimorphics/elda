/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.support.statistics;

/**
    An Interval stores the number of times it has been updated, the
    sum of the values it has been updated with, and the maximum
    and minimum of those values.
*/
public class Interval {
	public long min = Long.MAX_VALUE, total = 0, max = Long.MIN_VALUE, count = 0;

	/**
	    Update with the value of another measurement. 
	*/
	public void update( long duration ) {
		update( duration, false );
	}		
	
	/**
	    Update with the value of another measurement. If <code>suppressMin</code>
	    is true, do not update the record of the minimum value.
	*/
	public void update( long duration, boolean suppressMin ) {
		total += duration;
		count += 1;
		if (duration > max) max = duration;
		if (duration < min && suppressMin == false) min = duration;
	}
	
	/**
	    Answer the mean of all the values updated. If no values have
	    arrived, answer -1.
	*/
	public long mean() {
		return count == 0 ? -1 : total/count;
	}
	
	@Override public String toString() {
		return "<Interval" 
			+ " min: " + min
			+ " max: " + max
			+ " tot: " + total
			+ " num: " + count
			+ ">"
		;
	}
}