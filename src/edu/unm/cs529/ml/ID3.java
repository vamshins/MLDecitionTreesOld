package edu.unm.cs529.ml;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class ID3 {
	/** Entry point of program
	 * @param args
	 */
	public static void main(String[] args) {
		new ID3();
	}

	int impurity_function = 1;
	
	/** A backbone function which will make calls to compute Decision tree 
	 * 	and display output based on user's inputs.
	 */
	public ID3() {
		DataInputStream dis = new DataInputStream(System.in);
		String trainingFile = null, validationFile = null;
		int confidence=0;
		try {
//			System.out.println("Enter training file name : ");
			trainingFile = "training.txt";//dis.readLine();
//			System.out.println("Enter validation file name : ");
			validationFile = "validation.txt";//dis.readLine();
			System.out.println("Select the confidence for chi square statistics : \n1)0%\n2)95%\n3)99%\n");
			confidence = Integer.parseInt(dis.readLine());
			System.out.println("Select the evaluation criteria : \n1)Information gain(entropy impurity)"
					+ "\n2)accuracy(misclassification impurity)\n");
			impurity_function = Integer.parseInt(dis.readLine());

		} catch (Exception e) {
			e.printStackTrace();
		}
		List<Record> trainingRecords = getListOfRecords(trainingFile);
		switch (confidence) {
		case 1:
			Constants.Chi_Square_value = Constants.Chi_Square_value_0;
			break;
		case 2:
			Constants.Chi_Square_value = Constants.Chi_Square_value_95;
			break;
		case 3:
			Constants.Chi_Square_value = Constants.Chi_Square_value_99;
			break;
		default:
			System.out.println("Invalid number");
			break;
		}
		DecisionNode root = new DecisionNode(-1, "root");
		compute(trainingRecords,root);		// Formulate decision tree
		validateTree(trainingFile, root);		// validate training set
		validateTree(validationFile, root);		// validate validation set
		try {	dis.readLine();		} catch (IOException e) {		}
	}

	/**A function which will produce results by calculating correctly classified and misclassified
	 * and producing its result in the form of percent 
	 * @param fileName
	 * @param root
	 */
	private void validateTree(String fileName, DecisionNode root) {
		List<Record> validationRecords = getListOfRecords(fileName);
		int pos=0,err=0;
		for (Record record : validationRecords) {
			if(parseRecordInTree(record,root)){
				pos++;
			}else{
				err++;
			}
		}
		System.out.println("Classification of "+fileName);
		System.out.println("correctly classified:"+pos+" \nmisclassified:"+err);
		double total = pos+err;
		System.out.println("% accuracy : " + (pos/total*100)+"%");
	}

	/** This function traverse the 'record' through decision tree whose root node is 'root'
	 * 	
	 * @param record
	 * @param root
	 * @return
	 */
	private boolean parseRecordInTree(Record record, DecisionNode root) {
		DecisionNode parent = root;	//initially set root as parent node
		while (true) {
			boolean flag = true;	// flag is used for records which cannot be classified using this tree
			for (DecisionNode decisionNode : parent.nextNode) {		// for each child 'classes' of parent 'attribute' node
				if (record.getValue(decisionNode.getAttId()).equals(decisionNode.getValue())) { // if child's Class matches records attribute class
					flag=false;		// flag is set to false as record can be classified till now
					if (decisionNode.nextNode.size()==0) {	// if child node don't have further childs
						if (decisionNode.getOutput()==record.getSet()) { //compare label at decision node with validation records node
							return true;		// if they are same, correctly classified
						}else {
							return false;		//if not same, misclassified
						}
					}else{						// if child node has more child
						parent = decisionNode;	// make child as parent node for next loop
					}
					break;
				}				
			}
			if (flag) {			// if flag remains true, does mean, decision tree cannot classify 'record'
				return false;
			}
		}
	}

	/** Method computes a decision tree for the given 'records' dataset
	 *  using recursive technique.
	 * @param records
	 * @param parent
	 */
	private void compute(List<Record> records, DecisionNode parent) {
		double entropy ;
		if( impurity_function == Constants.Entropy_Impurity){
			entropy = entropy(records);	// Find Entropy of 'records' dataset
		}else {
			entropy = misclassification(records);	// Find Entropy of 'records' dataset
		}
		if (entropy == 0) {						//if entropy is 0 
			parent.setOutput(records.get(0).getSet());	// set output label for the node same as label of first record in dataset(whole dataset will have same labels)
			return;										// stop growing this node further
		}else{
			int attId = selectAttribute(records);	// Select the best attribute using 'infoGain' of 'records'
			if (getStats(records,attId) > Constants.Chi_Square_value) {	// if Chi Square Statistics value is greater the threshold then expand node
				Map<String, List<Record>> map = getDiscreteListsWithDelete(records, attId);	// make discrete subsets of Classes Ci of 'records' dataset based on Attribute index
				Iterator it = map.entrySet().iterator();
				while (it.hasNext()) {					// for each subset Dataset
					Map.Entry<String,List<Record>> pairs = (Map.Entry<String,List<Record>>)it.next();
					List<Record> recordClass = (List<Record>) pairs.getValue();
					DecisionNode child = new DecisionNode(attId, pairs.getKey().toString());	// create a child decision node for each class dataset
					parent.addNextNode(child);			// add this new child node to parent's child node ArrayList
					compute(recordClass, child);		// compute further nodes by same recurring steps
				}
			}else{	// if Chi Square Statistics value is not greater the threshold then stop growing node
				parent.setOutput(whichIsMore(records));	// will set output label for node as same as label which are more in 'records' dataset
				return;											// stop growing this node further
			}
		}
	}

	/** Function loads each sample in *.txt file in to 'Record' data structure 
	 *  and returns ArrayList of it.
	 * @param file
	 * @return ArrayList of loaded Records
	 */
	private List<Record> getListOfRecords(String file) {
		List<Record> records = new ArrayList<Record>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));	
			boolean fstRec = true;
			String line;
			while ((line = br.readLine())!=null) {		// Read from file line by line
				if (fstRec) {
					Constants.NO_OF_ATT = line.substring(0,line.indexOf(" ")).length();	// Set number of Attributes to number of characters in first line of file
					fstRec=false;
				}
				Record record ;
				String value = line.substring(line.indexOf(" ")+1);
				if (value.equals("+")) {
					record = new Record(line.substring(0,line.indexOf(" ")), 1); // '+' label represents 1
				}else{
					record = new Record(line.substring(0,line.indexOf(" ")), 0); // '-' label represents 0
				}
				records.add(record);
			}
			System.out.println();
			br.close();
		} catch (FileNotFoundException e) {
			System.out.println("training.txt : File Not found");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return records;
	}


	/**Calculates entropy of array of records passed as parameter
	 * entropy = summation for each subclass -(p/n)log_2(p/n)
	 * if value of p = n(i.e. number of records) then return entropy = 0
	 * as all labels are same for that record set
	 * @param records
	 * @return Entropy
	 */
	public double entropy(List<Record> records) {
		Map<String, List<Record>> recordMap = getSortedMap(records);		
		double n = records.size();
		double entropy = 0;
		Iterator it = recordMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			List<Record> recordClass = (List<Record>) pairs.getValue();
			double p = recordClass.size();
			if (p==0 || p==n) {
				return 0;
			}
			entropy -= (p/n)*log2(p/n);			
		}
		return entropy;
	}

	/**Calculates 
	 * 
	 * @param records
	 * @return 
	 */
	public double misclassification(List<Record> records) {
		Map<String, List<Record>> recordMap = getSortedMap(records);		
		double p = recordMap.get("+").size();
		double n = recordMap.get("-").size();	
		if (p > n) {
			return n / (p+n);
		}else{
			return p / (p+n);
		}
	}

	/**
	 * returns Log to the base 2 value of parameter x
	 * @param x
	 * @return Log_2(x)
	 */

	public static double log2(double x) {
		return Math.log(x)/Math.log(2);
	}

	/**return a map with Label(here '+' and '-') as Key
	 * and ArrayList of corresponding records sorted
	 * from a rest of records, passed as a parameter
	 * @param records
	 * @return Map containing labels as key and their corresponding records
	 */
	public static Map<String, List<Record>> getSortedMap(List<Record> records){
		List<Record> positiveRec = new ArrayList<Record>();
		List<Record> negativeRec = new ArrayList<Record>();
		for (Record record : records) {
			if (record.getSet()==1) {				// 1 represents for '+' and 0 represents for '-'
				positiveRec.add(record);			//Add positive records to positive ArrayList
			}else{
				negativeRec.add(record);			//Add negative records to negative ArrayList
			}
		}
		Map<String, List<Record>> map = new HashMap<String, List<Record>>();
		map.put("+", positiveRec);					// assign ArrayList with positive records to '+' key
		map.put("-", negativeRec);					// assign ArrayList with negative records to '-' key
		return map;
	}

	/**Returns which labels are majority, are they '+'(represented by 1) or '-'(represented by 0)
	 * taking in the ArrayList of records as parameter
	 * @param records
	 * @return labelid
	 */
	public static int whichIsMore(List<Record> records){
		Map<String, List<Record>> recordMap = getSortedMap(records);
		if (recordMap.get("+").size() > recordMap.get("-").size()) {
			return 1;				// if number of '+' is more than '-', returned is 1(which represents '+')
		}else{
			return 0;				// else returned is 0(which represents '-')
		}
	}

	/** Returns the index of an attribute which is going to be used as splitting node
	 *  Takes in the records based on which decision has to be taken using information Gain value
	 * @param records
	 * @return bestNodeAttribute
	 */
	public int selectAttribute(List<Record> records) {
		double infoGain=-9999999;					//Initialize infoGain to lowest possible value
		int splitterAtt=0;							//Initialize splitting attribute index to 0
		boolean fl=true;							//Use this flag to initialize splitterAttribute to certain number in first loop
		Map<Integer, String> attributes = records.get(0).getRecord();	//Get list of all attributes present for every record
		Iterator it = attributes.entrySet().iterator();	
		while (it.hasNext()) {						//Iterate over each attribute
			Map.Entry pairs = (Map.Entry)it.next();
			Integer integer = (Integer) pairs.getKey();	// integer = Key is index of attribute parsed in loop
			if (fl) {
				splitterAtt=integer;
				fl = false;
			}
			double ig = informationGain(records, integer);	// get information gain of records for given attribute index 'integer'
			if (ig>infoGain) {							// implementation of argmax inforGain of attributes
				infoGain = ig;
				splitterAtt = integer;					// splitterAtt will have index of attribute with highest info gain
			}
		}
		return splitterAtt;								// return splitter Attribute index
	}

	/**	Calculates information gain of records for given attribute index 'integer'
	 * 	It takes in records and attributeIndex as parameter
	 * @param records
	 * @param attId
	 * @return informationGain
	 */
	public double informationGain(List<Record> records, int attId) {
		Map<String, List<Record>> map = getDiscreteLists(records, attId);	//returns a map with 
		//	class Ci as key and class's records ArrayList as value for given attId present in parameter 'records'
		double n = records.size();		
		double infoGain;
		if( impurity_function == Constants.Entropy_Impurity){	//check impurity function selected
			infoGain = entropy(records);			// Initialize infoGain with Entropy impurity value of 'recordClass' dataset
		}else {
			infoGain = misclassification(records);	// Find Misclassifcation impurity value of 'recordClass' dataset
		}
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String,List<Record>> pairs = (Map.Entry<String,List<Record>>)it.next();
			List<Record> recordClass = (List<Record>) pairs.getValue();
			double rcSize = recordClass.size();
			
			double entropy ;
			if( impurity_function == Constants.Entropy_Impurity){
				entropy = entropy(recordClass);		// Find Entropy impurity value of 'recordClass' dataset
			}else {
				entropy = misclassification(recordClass);	// Find Misclassifcation impurity value of 'recordClass' dataset
			}
			infoGain -= (rcSize/n)*entropy;	//calculate value as (Cn/n*entropy(classrecords)) 
			//for each class to get subtracted from entropy to derive information gain of the attribute
		}
		return infoGain;
	}

	/** Slicing dataset into smaller subdataset based on classes in given attribute and also deleting particular attribute column
	 *	for eg we have dataset as D = 	{{a,c,t,g},
	 *									 {c,c,a,t},
	 *									 {a,c,a,t},
	 *									 {c,c,a,t}}
	 *  output will be : 		Map["a"] = {{c,t,g}, 
	 *  									{c,a,t}} 
	 *  				 		Map["c"] = {{c,a,t}, 
	 *  									{c,a,t}}
	 * @param records
	 * @param attId
	 * @return 
	 */
	public Map<String, List<Record>> getDiscreteListsWithDelete(List<Record> records, int attId){
		Map<String, List<Record>> map = new HashMap<String, List<Record>>();
		for (Record record : records) {
			String str = record.getValue(attId);
			record.deleteValue(attId);
			if (map.get(str) == null) {
				List<Record> discrete = new ArrayList<Record>();
				discrete.add(record);
				map.put(str, discrete);
			}else{
				map.get(str).add(record);
			}
		}
		return map;
	}

	/** Slicing dataset into smaller subdataset based on classes in given attribute and also deleting particular attribute column
	 *	for eg we have dataset as D = 	{{a,c,t,g},
	 *									 {c,c,a,t},
	 *									 {a,c,a,t},
	 *									 {c,c,a,t}}
	 *  output will be : 		Map["a"] = {{a,c,t,g}, 
	 *  									{a,c,a,t}} 
	 *  				 		Map["c"] = {{c,c,a,t}, 
	 *  									{c,c,a,t}}
	 * @param records
	 * @param attId
	 * @return 
	 */
	public Map<String, List<Record>> getDiscreteLists(List<Record> records, int attId){
		Map<String, List<Record>> map = new HashMap<String, List<Record>>();
		for (Record record : records) {
			String str = record.getValue(attId);
			if (map.get(str) == null) {
				List<Record> discrete = new ArrayList<Record>();
				discrete.add(record);
				map.put(str, discrete);
			}else{
				map.get(str).add(record);
			}
		}
		return map;
	}

	/** The method returns Chi-square statistics value of 'records' for selected attribute
	 * @param records
	 * @param attId
	 * @return
	 */
	public double getStats(List<Record> records,int attId) {
		Map<String, List<Record>> map = getDiscreteLists(records, attId);	//returns a map with 
		//	class Ci as key and class's records ArrayList as value for given attId present in parameter 'records'
		Map<String, List<Record>> recordMap = getSortedMap(records);	
		double p = recordMap.get("+").size();		//initialize p with count of '+' labeled 'records'
		double n = recordMap.get("-").size();		//initialize n with count of '-' labeled 'records'
		double statistics = 0;						//initialize statistics value to 0
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {						// for every Class Ci in 'records' dataset
			Map.Entry<String,List<Record>> pairs = (Map.Entry<String,List<Record>>)it.next();
			List<Record> recordClass = (List<Record>) pairs.getValue();
			Map<String, List<Record>> rm = getSortedMap(recordClass);	
			double pi = rm.get("+").size();		// initialize pi with count of '+' labeled in each subclass of 'record'
			double ni = rm.get("-").size();		// initialize ni with count of '-' labeled in each subclass of 'record'
			double p_i = p*((pi+ni)/(p+n));
			double n_i = n*((pi+ni)/(p+n));
			statistics += (((pi-p_i)*(pi-p_i)/p_i) + ((ni-n_i)*(ni-n_i)/n_i)  ); // calculate Chi square statistics value wit given formula
		}
		return statistics;
	}
}





