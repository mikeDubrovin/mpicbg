package mpicbg.models;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import mpicbg.util.Util;

/**
 * TODO Think about if it should really implement InverseBoundable.  There is
 *   no adequate solution for estimating the bounding box correctly instead of
 *   approximative as implemented here.
 *   
 * @author Stephan Saalfeld <saalfeld@mpi-cbg.de>
 * @version 0.4b
 */
public class InverseCoordinateTransformList< E extends InverseCoordinateTransform > implements InverseBoundable, TransformList< E >
{

	final private List< E > transforms = new ArrayList< E >();
	
	final public void add( E t ){ transforms.add( t ); }
	final public void remove( E t ){ transforms.remove( t ); }
	final public E remove( int i ){ return transforms.remove( i ); }
	final public E get( int i ){ return transforms.get( i ); }
	final public void clear(){ transforms.clear(); }
	final public List< E > getList( final List< E > preAllocatedList )
	{
		final List< E > returnList = ( preAllocatedList == null ) ? new ArrayList< E >() : preAllocatedList;
		returnList.addAll( transforms );
		return returnList;
	}
	
	//@Override
	final public float[] applyInverse( float[] location ) throws NoninvertibleModelException
	{
		final float[] a = location.clone();
		applyInverseInPlace( a );
		return a;
	}

	//@Override
	final public void applyInverseInPlace( float[] location ) throws NoninvertibleModelException
	{
		final ListIterator< E > i = transforms.listIterator( transforms.size() );
		while ( i.hasPrevious() )
			i.previous().applyInverseInPlace( location );
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Estimate the bounds approximately by iteration over a fixed grid of
	 * exemplary locations.
	 * 
	 * TODO Find a better solution.
	 */
	//@Override
	public void estimateInverseBounds( final float[] min, final float[] max ) throws NoninvertibleModelException
	{
		assert min.length == max.length : "min and max have to have equal length.";
		
		final int g = 32;
		
		final float[] minBounds = new float[ min.length ];
		final float[] maxBounds = new float[ min.length ];
		final float[] s = new float[ min.length ];
		final int[] i = new int[ min.length ];
		final float[] l = new float[ min.length ];
		
		for ( int k = 0; k < min.length; ++k )
		{
			minBounds[ k ] = Float.MAX_VALUE;
			maxBounds[ k ] = -Float.MAX_VALUE;
			s[ k ] = ( max[ k ] - min[ k ] ) / ( g - 1 );
			l[ k ] = min[ k ];
		}
		
		final long d = Util.pow( g, min.length );
		
		for ( long j = 0; j < d; ++j )
		{
			final float[] m = applyInverse( l );
			for ( int k = 0; k < min.length; ++k )
			{
				if ( m[ k ] < minBounds[ k ] ) minBounds[ k ] = m[ k ];
				if ( m[ k ] > maxBounds[ k ] ) maxBounds[ k ] = m[ k ];
			}
			
			for ( int k = 0; k < min.length; ++k )
			{
				++i[ k ];
				if ( i[ k ] >= g )
				{
					i[ k ] = 0;
					l[ k ] = min[ k ];
					continue;
				}
				l[ k ] = min[ k ] + i[ k ] * s[ k ];
				break;
			}
		}
		
		for ( int k = 0; k < min.length; ++k )
		{
			min[ k ] = minBounds[ k ];
			max[ k ] = maxBounds[ k ];
		}
	}
}
