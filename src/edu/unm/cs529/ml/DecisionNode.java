package edu.unm.cs529.ml;

import java.util.ArrayList;
import java.util.List;

/**
 * Object of this Class represents a node of the decision tree
 * @author Prathamesh
 *
 */
class DecisionNode {
	List<DecisionNode> nextNode;	// Array of Reference to child nodes
	int attId;						// Attributeindex of Attribute used classify
	String value;					// Class at Attribute at Attributeindex
	int output;						// Output Label number, 1 for '+' and 0 for '-'
	public DecisionNode( int attId, String value) {
		super();
		nextNode = new ArrayList<DecisionNode>();
		this.attId = attId;
		this.value = value;
		output = -1;
	}
	public int getAttId() {
		return attId;
	}
	public void setAttId(int attId) {
		this.attId = attId;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public DecisionNode getNextNode(int index) {
		return nextNode.get(index);
	}
	public void addNextNode(DecisionNode nextNode) {
		this.nextNode.add(nextNode);
	}
	public int getOutput() {
		return output;
	}
	public void setOutput(int value) {
		this.output = value;
	}
	public String toString() {
		return "DecisionNode [nextNode=" + nextNode + ", attId=" + attId
				+ ", value=" + value + ", output=" + output + "]";
	}	
}
