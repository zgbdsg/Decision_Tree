package auxiliary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 
 * @author zhougb
 */
public class DecisionTree extends Classifier {

	boolean isClassification;
	TreeNode root;
	boolean[] isCategory;

	public DecisionTree() {
	}

	/*
	 * isCategory[k] indicates whether the kth attribut is discrete or
	 * continuous, the last attribute is the label features[i] is the feature
	 * vector of the ith sample labels[i] is the label of he ith sample
	 */
	@Override
	public void train(boolean[] isCategory, double[][] features, double[] labels) {
		isClassification = isCategory[isCategory.length - 1];
		if (isClassification) { // classification
			// ...
			root = build_classification_tree(isCategory, features, labels);
			this.isCategory = isCategory;
			printTree(root);
		} else { // regression
			// ...
			root = build_regression_tree(isCategory, features, labels);
			this.isCategory = isCategory;
			printTree(root);
		}
	}

	/*
	 * features is the feature vector of the test sample you need to return the
	 * label of test sample
	 */
	@Override
	public double predict(double[] features) {
		if (isClassification) { // classification
			// ...
			double result = findLabel(isCategory, root, features);
			return result;
		} else { // regression
			// ...
			double result = findLabel(isCategory, root, features);
			return result;
		}
	}

	public double findLabel(boolean[] isCategory, TreeNode root,
			double[] features) {
		if (root == null)
			System.out.println("cao nima");
		if (root.leftchild == null && root.rightchild == null) {
			return root.label;
		}

		if (isCategory[root.splitfeature]) {
			if (features[root.splitfeature] == root.splitpoint) {
				return findLabel(isCategory, root.leftchild, features);
			} else
				return findLabel(isCategory, root.rightchild, features);
		} else {
			if (features[root.splitfeature] <= root.splitpoint) {
				return findLabel(isCategory, root.leftchild, features);
			} else
				return findLabel(isCategory, root.rightchild, features);
		}
	}

	public TreeNode build_classification_tree(boolean[] isCategory,
			double[][] data, double[] labels) {

		splitPoint sp = get_split_point(isCategory, data, labels);

		TreeNode node = new TreeNode();

		if (data.length < 4) {
			FeatureInfo labelsinfo = process_feature(
					isCategory[data.length - 1], labels);
			int max = 0;
			for (int i = 0; i < labelsinfo.numType; i++) {
				if (labelsinfo.amount[i] > labelsinfo.amount[max])
					max = i;
			}
			node.label = labelsinfo.feature[max];
			node.leftchild = null;
			node.rightchild = null;
			return node;
		}

		int size = 0;

		for (int i = 0; i < sp.info.numType; i++) {
			if (sp.info.feature[i] == sp.point)
				size = sp.info.amount[i];
		}

		double[][] dataleft = new double[size][];
		double[][] dataright = new double[data.length - size][];
		double[] labelleft = new double[size];
		double[] labelright = new double[data.length - size];

		int left = 0;
		int right = 0;

		for (int i = 0; i < data.length; i++) {
			if (data[i][sp.feature] == sp.point) {
				dataleft[left] = data[i];
				labelleft[left] = labels[i];
				left++;
			} else {
				dataright[right] = data[i];
				labelright[right] = labels[i];
				right++;
			}
		}

		FeatureInfo labelsinfo = process_feature(
				isCategory[data[0].length - 1], labels);
		int max = 0;
		for (int i = 0; i < labelsinfo.numType; i++) {
			if (labelsinfo.amount[i] > labelsinfo.amount[max])
				max = i;
		}

		node.splitfeature = sp.feature;
		node.splitpoint = sp.point;
		node.label = labelsinfo.feature[max];

		node.leftchild = build_classification_tree(isCategory, dataleft,
				labelleft);
		node.rightchild = build_classification_tree(isCategory, dataright,
				labelright);

		return node;
	}

	public TreeNode build_regression_tree(boolean[] isCategory,
			double[][] data, double[] labels) {

		splitPoint sp = get_regression_split_point(isCategory, data, labels);

		TreeNode node = new TreeNode();

		if (data.length < 4) {
			double total = 0;

			for (int i = 0; i < labels.length; i++)
				total += labels[i];

			node.label = 1.0 * total / labels.length;
			node.leftchild = null;
			node.rightchild = null;
			return node;
		}

		int size = 0;

		for (int i = 0; i < data.length; i++) {
			if (data[i][sp.feature] <= sp.point)
				size += 1;
		}

		double[][] dataleft = new double[size][];
		double[][] dataright = new double[data.length - size][];
		double[] labelleft = new double[size];
		double[] labelright = new double[data.length - size];

		int left = 0;
		int right = 0;

		for (int i = 0; i < data.length; i++) {
			if (data[i][sp.feature] <= sp.point) {
				dataleft[left] = data[i];
				labelleft[left] = labels[i];
				left++;
			} else {
				dataright[right] = data[i];
				labelright[right] = labels[i];
				right++;
			}
		}

		FeatureInfo labelsinfo = process_feature(
				isCategory[data[0].length - 1], labels);
		double total = 0;

		for (int i = 0; i < labels.length; i++)
			total += labels[i];

		node.label = 1.0 * total / labels.length;

		node.splitfeature = sp.feature;
		node.splitpoint = sp.point;
		// node.label = labelsinfo.feature[max];
		if (size > 0)
			node.leftchild = build_regression_tree(isCategory, dataleft,
					labelleft);
		node.rightchild = build_regression_tree(isCategory, dataright,
				labelright);

		return node;
	}

	public splitPoint get_regression_split_point(boolean[] isCategory,
			double[][] data, double[] labels) {
		double[] result = min_least_square(isCategory, data, labels);
		splitPoint sp = new splitPoint();
		sp.feature = (int) result[1];
		sp.point = result[2];
		return sp;
	}

	public double[] min_least_square(boolean[] isCategory, double[][] data,
			double[] labels) {

		double[] result = new double[3];
		double min = 100000;
		if (data[0] == null)
			System.out.println("outoutoutoutoutoutotuotoutoutoutoutuot");
		int len = data[0].length;
		for (int i = 0; i < len; i++) {
			double[] tmp = count_least_square(isCategory[i], data, i, labels);
			if (min > tmp[0]) {
				min = tmp[0];
				result[1] = i;
				result[2] = tmp[1];
			}
		}
		result[0] = min;
		return result;
	}

	public double[] count_least_square(boolean isCategory, double[][] data,
			int feature, double[] labels) {

		int countleft = 0;
		int countright = 0;
		double cleft = 0;
		double cright = 0;
		double[] target = new double[data.length];

		for (int i = 0; i < data.length; i++) {
			target[i] = data[i][feature];
		}

		FeatureInfo info = process_feature(isCategory, target);

		double min_square = 10000000;

		double[] result = new double[2];
		for (int i = 0; i < info.numType; i++) {
			// set info.feature[i] as split point

			for (int j = 0; j < data.length; j++) {
				if (data[j][feature] <= info.feature[i]) {
					cleft += labels[j];
					countleft++;
				} else {
					cright += labels[j];
					countright++;
				}
			}

			cleft = 1.0 * cleft / countleft;
			cright = 1.0 * cright / countright;

			double lefttotal = 0;
			double righttotal = 0;

			for (int j = 0; j < data.length; j++) {
				if (data[j][feature] <= info.feature[i]) {
					lefttotal += Math.pow((labels[j] - cleft), 2);
				} else {
					righttotal += Math.pow((labels[j] - cright), 2);
				}
			}

			if (min_square > (lefttotal + righttotal)) {
				min_square = lefttotal + righttotal;
				result[0] = min_square;
				result[1] = info.feature[i];
			}
		}
		return result;
	}

	public splitPoint get_split_point(boolean[] isCategory, double[][] data,
			double[] labels) {

		List<double[]> ginidata = new ArrayList<double[]>();
		splitPoint sp = new splitPoint();

		for (int i = 0; i < data[0].length - 1; i++) {
			ginidata.add(gini(isCategory, data, labels, i));
			// System.out.println(Arrays.toString(gini(data,labels,i)));
		}

		double min = 1;
		for (int i = 0; i < ginidata.size(); i++) {
			double[] tmp = ginidata.get(i);
			double tmpmin = 1;
			int point = 0;
			for (int j = 0; j < tmp.length; j++) {
				if (tmpmin > tmp[j]) {
					tmpmin = tmp[j];
					point = j;
				}
			}
			if (min > tmpmin) {
				sp.feature = i;
				min = tmpmin;
				double[] target = new double[data.length];

				for (int k = 0; k < data.length; k++) {
					target[k] = data[k][i];
				}

				FeatureInfo info = process_feature(isCategory[i], target);
				sp.point = info.getFeature()[point];
				sp.info = info;
			}
		}

		// System.out.println("split point:"+sp.getFeature()+"    "+sp.getPoint());

		return sp;
	}

	public double[] gini(boolean[] isCategory, double[][] data,
			double[] labels, int feature) {
		double[] target = new double[data.length];

		for (int i = 0; i < data.length; i++) {
			target[i] = data[i][feature];
		}

		FeatureInfo info = process_feature(isCategory[feature], target);
		FeatureInfo infoLabel = process_feature(isCategory[data[0].length - 1],
				labels);

		double[] gininum = new double[info.getNumType()];

		if (info.getNumType() == 2) {
			double gini1 = 0;
			double gini2 = 0;

			int d1 = info.getAmount()[0];
			int d2 = info.getAmount()[1];
			int d = d1 + d2;
			if (infoLabel.getNumType() == 2) {
				int prob1 = 0;
				int prob2 = 0;

				for (int i = 0; i < data.length; i++) {
					if (isCategory[feature]) {
						if (data[i][feature] == info.getFeature()[0]) {
							if (labels[i] == infoLabel.getFeature()[0])
								prob1 += 1;
						} else {
							if (labels[i] == infoLabel.getFeature()[0])
								prob2 += 1;
						}
					} else {
						if (data[i][feature] <= info.getFeature()[0]) {
							if (labels[i] == infoLabel.getFeature()[0])
								prob1 += 1;
						} else {
							if (labels[i] == infoLabel.getFeature()[0])
								prob2 += 1;
						}
					}
				}

				gini1 = (1.0 * d1 / d)
						* (2.0 * prob1 / d1 * (1 - 1.0 * prob1 / d1));
				gini2 = (1.0 * d2 / d)
						* (2.0 * prob2 / d2 * (1 - 1.0 * prob2 / d2));

				gininum[0] = gininum[1] = gini1 + gini2;
			} else {

				// we have more than two kinds value in the feature
				// !!!!!!!!!
				int[][] prob = new int[2][infoLabel.getNumType()];

				for (int i = 0; i < data.length; i++) {
					if (isCategory[feature]) {
						if (data[i][feature] == info.getFeature()[0]) {
							for (int j = 0; j < info.getNumType(); i++) {
								if (labels[j] == infoLabel.getFeature()[j]) {
									prob[0][j] += 1;
									break;
								}
							}
						} else {
							for (int j = 0; j < info.getNumType(); i++) {
								if (labels[j] == infoLabel.getFeature()[j]) {
									prob[1][j] += 1;
									break;
								}
							}
						}
					} else {
						if (data[i][feature] <= info.getFeature()[0]) {
							for (int j = 0; j < info.getNumType(); i++) {
								if (labels[j] == infoLabel.getFeature()[j]) {
									prob[0][j] += 1;
									break;
								}
							}
						} else {
							for (int j = 0; j < info.getNumType(); i++) {
								if (labels[j] == infoLabel.getFeature()[j]) {
									prob[1][j] += 1;
									break;
								}
							}
						}
					}
				}

				for (int i = 0; i < infoLabel.getNumType(); i++) {
					gini1 += Math.pow(1.0 * prob[0][i] / d1, 2);
					gini2 += Math.pow(1.0 * prob[1][i] / d2, 2);
				}

				gininum[0] = gininum[1] = (1.0 * d1 / d) * (1 - 1.0 * gini1)
						+ (1.0 * d2 / d) * (1 - 1.0 * gini2);
			}

		} else {

			double gini1 = 0;
			double gini2 = 0;

			// System.out.println("get in  1");
			for (int k = 0; k < info.getNumType(); k++) {
				int dk = info.getAmount()[k];
				int dleft = 0;

				int[] dsize = info.getAmount();
				for (int a = 0; a < info.getNumType(); a++) {
					dleft += dsize[a];
				}

				dleft = dleft - dk;// get the d1 and d2 in the fomular

				// System.out.println("get in  2");
				if (infoLabel.getNumType() == 2) {
					int prob1 = 0;
					int prob2 = 0;

					for (int i = 0; i < data.length; i++) {
						if (isCategory[feature]) {
							if (data[i][feature] == info.getFeature()[k]) {
								if (labels[i] == infoLabel.getFeature()[0])
									prob1 += 1;
							} else {
								if (labels[i] == infoLabel.getFeature()[0])
									prob2 += 1;
							}
						} else {
							if (data[i][feature] <= info.getFeature()[k]) {
								if (labels[i] == infoLabel.getFeature()[0])
									prob1 += 1;
							} else {
								if (labels[i] == infoLabel.getFeature()[0])
									prob2 += 1;
							}
						}
					}

					gini1 = (1.0 * dk / (dleft + dk))
							* (2.0 * prob1 / dk * (1 - 1.0 * prob1 / dk));
					gini2 = (1.0 * dleft / (dleft + dk))
							* (2.0 * prob2 / dleft * (1.0 - 1.0 * prob2 / dleft));

					gininum[k] = gini1 + gini2;
				} else {
					// System.out.println("get in  3");
					int[][] prob = new int[2][infoLabel.getNumType()];

					for (int i = 0; i < data.length; i++) {
						if (isCategory[feature]) {
							if (data[i][feature] == info.getFeature()[k]) {
								for (int j = 0; j < info.getNumType(); i++) {
									if (labels[j] == infoLabel.getFeature()[j]) {
										prob[0][j] += 1;
										break;
									}
								}
							} else {
								for (int j = 0; j < info.getNumType(); i++) {
									if (labels[j] == infoLabel.getFeature()[j]) {
										prob[1][j] += 1;
										break;
									}
								}
							}
						} else {
							if (data[i][feature] <= info.getFeature()[k]) {
								for (int j = 0; j < info.getNumType(); i++) {
									if (labels[j] == infoLabel.getFeature()[j]) {
										prob[0][j] += 1;
										break;
									}
								}
							} else {
								for (int j = 0; j < info.getNumType(); i++) {
									if (labels[j] == infoLabel.getFeature()[j]) {
										prob[1][j] += 1;
										break;
									}
								}
							}
						}

						double ginid1 = 0;
						double ginid2 = 0;
						for (int x = 0; x < infoLabel.getNumType(); x++) {
							ginid1 += Math.pow(1.0 * prob[0][x] / dk, 2);
							ginid2 += Math.pow(1.0 * prob[1][x] / dleft, 2);
						}

						gininum[k] = (1.0 * dk / (dleft + dk))
								* (1 - 1.0 * ginid1)
								+ (1.0 * dleft / (dk + dleft))
								* (1 - 1.0 * ginid2);
					}
				}
			}
		}
		return gininum;
	}

	public FeatureInfo process_feature(boolean isCategory, double[] data) {
		// find the not repeat element , its amount
		int numType = 0;
		List<Object> feature = new ArrayList<Object>();
		int[] amount;

		if (isCategory) {
			for (int i = 0; i < data.length; i++) {
				if (!feature.contains((Object) data[i])) {
					if (!Double.isNaN(data[i])) {
						numType++;
						feature.add(data[i]);
					}

				}
			}

			amount = new int[numType];

			for (int i = 0; i < data.length; i++) {
				int k = feature.indexOf((Object) data[i]);
				if (k >= 0)
					amount[k]++;
			}
		} else {
			Arrays.sort(data);
			List<Object> tmp = new ArrayList<Object>();
			for (int i = 0; i < data.length; i++) {
				if (!feature.contains((Object) data[i])) {
					if (!Double.isNaN(data[i])) {
						numType++;
						tmp.add(data[i]);
					}
				}
			}

			for (int i = 0; i < tmp.size() - 1; i++) {
				feature.add((Object) (1.0 * ((double) tmp.get(i) + (double) tmp
						.get(i + 1)) / 2.0));
			}

			numType = numType - 1;
			if (numType > 0) {
				amount = new int[numType];

				if (isCategory) {
					for (int i = 0; i < data.length; i++) {
						int k = feature.indexOf((Object) data[i]);
						if (k >= 0)
							amount[k]++;
					}
				} else {

					for (int k = 0; k < feature.size(); k++) {
						for (int i = 0; i < data.length; i++) {
							if (data[i] <= (double) feature.get(k)) {
								amount[k]++;
							} else
								break;
						}
					}
				}
			} else
				amount = null;
		}
		// System.out.println(numType);
		// System.out.println(Arrays.toString(amount));
		FeatureInfo info = new FeatureInfo(numType, feature, amount);
		// System.out.println(info.toString());
		return info;
	}

	public void printTree(TreeNode root) {
		/*
		 * if(root == null) return;
		 * System.out.println("printTree   :"+root.splitfeature + "    "+
		 * root.splitpoint + "     " + root.label); printTree(root.leftchild);
		 * printTree(root.rightchild);
		 */

		Queue<TreeNode> q = new LinkedBlockingQueue<TreeNode>();
		q.add(root);

		while (q.size() > 0) {
			int size = q.size();

			for (int i = 0; i < size; i++) {
				TreeNode tmp = q.poll();
				System.out.print("   " + tmp.splitfeature);
				TreeNode left = tmp.leftchild;
				TreeNode right = tmp.rightchild;

				if (left != null)
					q.add(left);
				if (right != null)
					q.add(right);
			}
			System.out.println();
		}
	}
}

class TreeNode {
	// double[][] data;
	int splitfeature;
	double splitpoint;
	TreeNode leftchild;
	TreeNode rightchild;
	double label;

	public TreeNode() {
	}

	public TreeNode(int splitfeature, double splitpoint) {
		this.splitfeature = splitfeature;
		this.splitpoint = splitpoint;
		this.leftchild = null;
		this.rightchild = null;
	}

}

class FeatureInfo {
	int numType;
	double[] feature;
	int[] amount;

	public FeatureInfo(int numType, List<Object> feature, int[] amount) {
		this.numType = numType;
		if (numType > 0) {
			this.feature = new double[numType];
			for (int i = 0; i < numType; i++) {
				this.feature[i] = Double.parseDouble(feature.get(i).toString());
			}
		} else
			this.feature = null;

		this.amount = amount;
	}

	public int getNumType() {
		return numType;
	}

	public void setNumType(int numType) {
		this.numType = numType;
	}

	public double[] getFeature() {
		return feature;
	}

	public void setFeature(double[] feature) {
		this.feature = feature;
	}

	public int[] getAmount() {
		return amount;
	}

	public void setAmount(int[] amount) {
		this.amount = amount;
	}

	@Override
	public String toString() {
		return "FeatureInfo [numType=" + numType + ", feature="
				+ Arrays.toString(feature) + ", amount="
				+ Arrays.toString(amount) + "]";
	}

}

class splitPoint {
	int feature;
	double point;
	FeatureInfo info;

	public splitPoint() {
	}

	public splitPoint(int feature, double point) {
		this.feature = feature;
		this.point = point;
	}

	public int getFeature() {
		return feature;
	}

	public void setFeature(int feature) {
		this.feature = feature;
	}

	public double getPoint() {
		return point;
	}

	public void setPoint(double point) {
		this.point = point;
	}

}