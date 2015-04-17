package pt.iscte.apista.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public interface Parametrizable {

	/**
	 * Allows to configure the class with the parameters provided in the properties file
	 * The parameters must be pre-processed
	 * @param params used to configure. Must handle null params
	 */
	void setup(Parameters params);
	
	public static class Parameters implements Serializable{
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
