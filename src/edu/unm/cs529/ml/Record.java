package edu.unm.cs529.ml;

import java.util.HashMap;
import java.util.Map;

/** Objects of this class is used to store each record in a structured form.
 * 
 * @author Prathamesh
 */
class Record {
	private Map<Integer, String> record;
	private int set;

	/** Constructor initializes Attribute index to value Map
	 * 	for eg 'abcd'	we have	Map[0] = a, Map[1] = b, Map[2] = c, Map[3] = d 
	 * @param str
	 * @param value
	 */
	public Record(String str, int value) {	
		if (str.length() != Constants.NO_OF_ATT ) {
			System.out.println("invalid record");
		}else{
			record = new HashMap<Integer, String>();
			for (int i = 0; i < str.length(); i++) {
				record.put(i, ""+str.charAt(i));	// assigning value to attribute index in 'record' Map
			}
			this.set=value;							// set value for the record, 1 for '+' and 0 for '-' label
		}
	}

	public Map<Integer, String> getRecord() {
		return record;
	}

	/**
	 * returns value at from map where attribute index is 'key'
	 * @param value
	 */
	public String getValue(int key) {
		return record.get(key);
	}

	public void setRecord(Map<Integer, String> record) {
		this.record = record;
	}
	public int getSet() {
		return set;
	}
	public void setSet(int value) {
		this.set = value;
	}

	/**
	 * remove Attribute index 'attId' entry from Map
	 * @param attId
	 */
	public void deleteValue(int attId) {
		record.remove(attId);
	}
	public String toString() {
		return "Record [record=" + record + ", set=" + set + "]";
	}
}