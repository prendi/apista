package pt.iscte.apista.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Allows a class to be parameterizable with the parameters provided in the SystemConfiguration.
 * The parameters must be pre-processed
 */
public interface Parametrizable {

	/**
	 * Setups up the class according to the Parameters provided
	 * @param params used to configure. Must handle null params.
	 */
	void setup(Parameters params);
	
	/**
	 * Class used to access the parameters in the SystemConfiguration
	 */
	public static class Parameters implements Serializable{
		/**
		 * Maps the parameter name and its value
		 */
		private Map<String, String> map = new HashMap<String, String>();
		
		public void addParameter(String name, String value){
			map.put(name, value);
		}
		
		public String getValue(String name) {
			String value = map.get(name);
			if(value != null) 
				return value;
			else 
				throw new IllegalArgumentException("The parameter name (" + name + ") "
					+ "does not have a value associated");
		}
		@Override
		public String toString() {
			return map.toString();
		}
		
		public int getIntValue(String name){
			try{
				return Integer.parseInt(getValue(name));
			}catch(NumberFormatException e){
				throw new UnsupportedOperationException("The parameter name (" + name + ") "
						+ "is not a valid integer value");
			}
		}
		
		public double getDoubleValue(String name){
			try{
				return Double.parseDouble(getValue(name));
			}catch(NumberFormatException e){
				throw new UnsupportedOperationException("The parameter name (" + name + ") "
						+ "is not a valid double value");
			}
		}
		
		public Class getClassValue(String name){
			try{
				return Class.forName(getValue(name));
			}catch(ClassNotFoundException e){
				throw new UnsupportedOperationException("The parameter name (" + name + ") "
						+ "is not a valid class");
			}
		}
	}
	
}
