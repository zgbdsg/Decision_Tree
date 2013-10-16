package dm13;

import java.util.Arrays;

import auxiliary.DataSet;
import auxiliary.DecisionTree;

public class Testclass {
	public static void main(String[] args) {
		 DataSet dataset = new DataSet("D:\\workspace\\decision_tree\\src\\data/breast-cancer.data");
		 double ddd = 10;
		 System.out.println("object:  "+((Object)ddd).toString());
		 double[][] data = dataset.getFeatures();
		 double[] test = new double[data.length];
		 for(int i=0;i < data.length;i ++) {
			 System.out.println(Arrays.toString(data[i]));
			 test[i] = data[i][7];
		 }
		 
		 DecisionTree tree = new DecisionTree();
		 tree.process_feature(true, test);
		 System.out.println("-------------------test-------------------");
		 tree.get_split_point(dataset.getIsCategory(), data, dataset.getLabels());
		 tree.train(dataset.getIsCategory(),data, dataset.getLabels());
	}
}
