package com.epimorphics.lda.jmx;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.epimorphics.lda.support.statistics.Interval;
import com.epimorphics.lda.support.statistics.StatsValues;

public class Statistics implements ServletContextListener {
    
	public void contextDestroyed(ServletContextEvent s) {
    }

    public void contextInitialized(ServletContextEvent s) {
    	JMXSupport.register("com.epimorphics.lda.jmx:type=statistics", new Stats());
    }

	public interface StatsMBean {
    	
    	public long getRequestCount();
    	
    	public long getTotalViewCacheHits();
    	
    	public long getTotalSelectCacheHits();
    	
    	public long getTotalTime();
    	
    	public long getFailedMatchCount();
    	
    	public Map<String, Object> getTotalSelectionTime();
    	
    	public Map<String, Object> getTotalViewerTime();
    	
    	public Map<String, Object> getTotalRenderTime();
    	
    	public Map<String, Object> getTotalRenderSize();
    	
    	public Map<String, Object> getTotalSelectQuerySize();
    	
    	public Map<String, Object> getTotalViewQuerySize();
    	
    	public Map<String, Object> getTotalStylesheetCompileTime();
    	
    	public Map<String, Object> getRenderingDurations();
    	
    	public Map<String, Object> getRenderingSizes();
    }
    
    public static class Stats implements StatsMBean {
    	
    	public long getRequestCount() {
    		return StatsValues.requestCount;
    	}
    	
    	public long getTotalViewCacheHits() {
    		return StatsValues.totalViewCacheHits;
    	}
    	
    	public long getTotalSelectCacheHits() {
    		return StatsValues.totalSelectCacheHits;
    	}
    	
    	public long getTotalTime() {
    		return StatsValues.totalTime;
    	}
    	
    	public long getFailedMatchCount() {
    		return StatsValues.failedMatchCount;
    	}

		@Override public Map<String, Object> getTotalSelectionTime() {
			return canonise( StatsValues.totalSelectionTime );
		}

		@Override public Map<String, Object> getTotalViewerTime() {
			return canonise( StatsValues.totalSelectionTime );
		}

		@Override public Map<String, Object> getTotalRenderTime() {
			return canonise( StatsValues.totalRenderTime );
		}

		@Override public Map<String, Object> getTotalRenderSize() {
			return canonise( StatsValues.totalRenderSize );
		}

		@Override public Map<String, Object> getTotalSelectQuerySize() {
			return canonise( StatsValues.totalSelectQuerySize );
		}

		@Override public Map<String, Object> getTotalViewQuerySize() {
			return canonise( StatsValues.totalViewQuerySize );
		}

		@Override public Map<String, Object> getTotalStylesheetCompileTime() {
			return canonise( StatsValues.totalStylesheetCompileTime );
		}
		
		@Override public Map<String, Object> getRenderingDurations() {
			return canonise( StatsValues.formatDurations );
		}
		
		@Override public Map<String, Object> getRenderingSizes() {
			return canonise( StatsValues.formatDurations );
		}
    	
    	private Map<String, Object> canonise( Map<String, Interval> map ) {
			Map<String, Object> result = new HashMap<String, Object>();
			for (Map.Entry<String, Interval> e: map.entrySet()) 
				result.put( e.getKey(), canonise( e.getValue() ) );
			return result;
		}

		private Map<String, Object> canonise(Interval i) {
    		Map<String, Object> c = new HashMap<String, Object>();
    		if (i.count > 0) {
    			c.put( "smallest", i.min );
    			c.put( "biggest", i.max );
    			c.put( "total", i.total );
    			c.put( "hits", i.count );
    		} else {
    			c.put( "hits", 0 );
    		}
    		return c;
//			CompositeType type;
//			try {
//				OpenType<?> longType = SimpleType.LONG;
//				type = new CompositeType
//					( "typeName"
//					, "descrotion"
//					, new String[] {"min", "max", "tot", "num" }
//					, new String[] {"min", "max", "tot", "num" }
//					, new OpenType<?> [] {longType, longType, longType, longType}
//					);
//			} catch (OpenDataException e) {
//				throw new WrappedException( e );
//			}
//			try { return new CompositeDataSupport( type, c );
//			} catch (OpenDataException e) {
//				throw new WrappedException( e );
//			}
    	}
    	
    }
    
//    public static class My implements DynamicMBean {
//    	
//		@Override public Object getAttribute( String name ) 
//			throws AttributeNotFoundException, MBeanException, ReflectionException {
//			Map<String, Object> currentValues = new HashMap<String, Object>();
//			loadCurrentValues( currentValues );
//			return currentValues.get( name );
//		}
//
//		@Override public void setAttribute(Attribute attribute)
//			throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
//			
//		}
//
//		@Override public AttributeList getAttributes( String[] attributes ) {
//			Map<String, Object> currentValues = new HashMap<String, Object>();
//			loadCurrentValues( currentValues );
//			AttributeList al = new AttributeList();
//			for (String name: attributes) {
//				System.err.println( ">> getAttributes: " + name + " = " + currentValues.get( name ) );
//				al.add( new Attribute( name, currentValues.get( name ) ) );
//			}
//			return al;
//		}
//
//		private void loadCurrentValues( final Map<String, Object> currentValues ) {
//			doItems( new Thing() {
//				public void item( String name, Object value ) {
//					currentValues.put( name, value );
//				}
//			});
//		}
//		
//		interface Thing {
//			void item( String name, Object value );
//		}
//		
//		private void doItems( Thing t ) {
//			t.item( "requestCount", StatsValues.requestCount );	
//			t.item( "totalViewCacheHits", StatsValues.totalViewCacheHits );
//			t.item( "totalSelectCacheHits", StatsValues.totalSelectCacheHits );
//			t.item( "totalTime", StatsValues.totalTime );
//			t.item( "failedMatchCount", StatsValues.failedMatchCount );
//		//
//			t.item( "totalSelectionTime", canonise( StatsValues.totalSelectionTime ) );
//			t.item( "totalViewTime", canonise( StatsValues.totalViewTime ) );
//			t.item( "totalRenderTime", canonise( StatsValues.totalRenderTime ) );
//			t.item( "totalRenderSize", canonise( StatsValues.totalRenderSize ) );
//			t.item( "totalSelectQuerySize", canonise( StatsValues.totalSelectQuerySize ) );
//			t.item( "totalViewQuerySize", canonise( StatsValues.totalViewQuerySize ) );
//			t.item( "totalStylesheetCompileTime", canonise( StatsValues.totalStylesheetCompileTime ) );
//		//
//			System.err.println( ">> formatDurations: " + StatsValues.formatDurations );
//			for (String format: StatsValues.formatDurations.keySet()) {
//				t.item( format + "-duration", canonise( StatsValues.formatDurations.get( format ) ));
//			}
//			for (String format: StatsValues.formatSizes.keySet()) {
//				t.item( format + "-size", canonise( StatsValues.formatSizes.get( format ) ));
//			}
//		}
//
//
//		@Override public AttributeList setAttributes(AttributeList attributes) {
//			return null;
//		}
//
//		@Override public Object invoke(String actionName, Object[] params, String[] signature) 
//			throws MBeanException, ReflectionException {
//			System.err.println( ">> invoke: " + actionName );
//			return null;
//		}
//		
//		public void XXX() {
//			System.err.println( ">> BONK." );
//		}
//
//		@Override public MBeanInfo getMBeanInfo() {
//			final List<MBeanAttributeInfo> items = new ArrayList<MBeanAttributeInfo>();
//		//	
//			doItems( new Thing() {
//				public void item( String name, Object value ) {
//					items.add( xitem( name, value ) );
//				}
//			});
//		//
//			System.err.println( ">> CONSTRUCTING OPLIST" );
//			List<MBeanOperationInfo> oplist = new ArrayList<MBeanOperationInfo>();
//			Method m = null;
//			try {
//				m = this.getClass().getMethod( "XXX" );
//			} catch (Exception e) {
//				throw new WrappedException( e );
//			}
//			MBeanOperationInfo op = new MBeanOperationInfo( "poke", m );
//			oplist.add(op);
//			MBeanOperationInfo [] operations = oplist.toArray( new MBeanOperationInfo[oplist.size()] );
//		//
//			MBeanAttributeInfo [] attributes = new MBeanAttributeInfo[items.size()];
//			items.toArray( attributes );
//			return new MBeanInfo( this.getClass().getName(), "Elda statistics", attributes, null, operations, null);
//		}
//
//		private MBeanAttributeInfo xitem( String name, Object value ) {
//			System.err.println( ">> item: " + name + " = " + value );
//			return new MBeanAttributeInfo( name, value.getClass().getName(), name, true, false, false );
//		}
//    	
//    	
//    }
}
