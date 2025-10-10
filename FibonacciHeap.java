package src;

import src.FibonacciHeap.HeapNode;

/**
 * FibonacciHeap
 *
 * An implementation of Fibonacci heap over positive integers.
 *
 */
public class FibonacciHeap
{
	public HeapNode minNode;
	public int total_nodes = 0;
	public int numOfRoots = 0;
	private int totalCutsCnt = 0;
	private int totalLinksCnt = 0;
	private boolean changeMin = true;

///////////////////////////////////////// printers

	/**
	 * Print the Fibonacci heap in a hierarchical format.
	 */
	public void printHeap() {
		if (minNode == null) {
			System.out.println("The heap is empty.");
			return;
		}

		System.out.println("Fibonacci Heap:");
		HeapNode current = minNode;
		do {
			printSubTree(current, 0); // Print each tree starting from the root
			current = current.next;
		} while (current != minNode);
	}


	/**
	 * Helper method to print a subtree rooted at a specific node.
	 *
	 * @param node  The current node to print.
	 * @param depth The depth of the current node (used for indentation).
	 */
	private void printSubTree(HeapNode node, int depth) {
		if (node == null) {
			return;
		}

		// Indent the node based on its depth
		System.out.println("  ".repeat(depth) + "Key: " + node.key + " (Rank: " + node.rank + ")");

		// Recursively print the children of the current node
		HeapNode child = node.child;
		if (child != null) {
			HeapNode currentChild = child;
			do {
				printSubTree(currentChild, depth + 1); // Increase indentation for children
				currentChild = currentChild.next;
			} while (currentChild != child);
		}
	}


public int potential() {
		int t = 0; // Number of trees (roots)
		int m = 0; // Number of marked nodes
		HeapNode current = minNode;

		if (current != null) {
			// Traverse the circular linked list of roots
			do {
				t++; // Each root is a tree
				m += countMarkedNodes(current); // Count marked nodes in the tree
				current = current.next;
			} while (current != minNode);
		}

		return t + 2 * m; // Potential is t + 2 * m
	}

	// Helper method to count marked nodes in a tree
	private int countMarkedNodes(HeapNode node) {
		int count = 0;
		while (node != null) {
			if (node.mark) count++; // Increment if the node is marked
			node = node.child;
		}
		return count;
	}

	public int[] countersRep() {
		int[] counters = new int[calculateMaxRank()]; // Array to store the number of trees of each rank
		HeapNode current = minNode;

		// Traverse the root list and count trees by rank
		if (current != null) {
			do {
				int rank = current.rank;
				counters[rank]++; // Increment the count of trees of this rank
				current = current.next;
			} while (current != minNode);
		}

		return counters;
	}

	// Helper method to calculate the maximum possible rank (based on the number of nodes in the heap)
	private int calculateMaxRank() {
		return (int) Math.ceil(Math.log(total_nodes) / Math.log(2)) + 1;
	}


	///////////////////////////////////////// code starts here

	// set min and increase numOfRoots in case it's not null
	public FibonacciHeap(){
		minNode = null;
	}

	/**
	 *
	 * pre: key > 0
	 *
	 * Insert (key,info) into the heap and return the newly generated HeapNode.
	 *
	 */
	public HeapNode insert(int key, String info) {
		HeapNode node = new HeapNode(key, info); // create node
		if (minNode == null) setMin(node); // If tree is empty set the heap and the new node as the minNode
		else addNodeToList(node, minNode); // Else, add the new node to the root list
		if (key < minNode.key) setMin(node); // Update the minNode pointer if necessary
		// update heap's fields
		total_nodes++;
		numOfRoots++;
		return node;
	}

	/**
	 *
	 * Return the minimal HeapNode, null if empty.
	 *
	 */
	public HeapNode findMin() {
		return minNode;
	}

	/**
	 *
	 * Delete the minimal item
	 *
	 */

	public void deleteMin() {
		// if the heap is not empty
		if (minNode != null){
			// if the minNode has child, add childrens to root list
			if (minNode.child != null) {
				HeapNode child = minNode.child;
				do {
					child.parent = null;
					totalCutsCnt++;
					child = child.next;
				} while (child != minNode.child);
				mergeCircularLists(minNode , child);
			}
			numOfRoots += minNode.rank;
			// delete min node and set a new one
			HeapNode nodeToDelete = minNode;
			setMin(nodeToDelete.next); // set temporary minimum
			removeNodeFromCircularList(nodeToDelete); // delete the cuurent minimum form heap
			// update heap's fields
			total_nodes--;
			numOfRoots--;
			// if the heap is empty set the min to be null
			if (total_nodes == 0) {
				minNode = null;
				return;
			}
			consolidate(); //merge trees with same degree
		}
	}

	/**
	 *
	 * pre: 0<diff<x.key
	 *
	 * Decrease the key of x by diff and fix the heap.
	 *
	 */
	public void decreaseKey(HeapNode node, int diff)
	{
		if (node == null) return; //edge case
		node.key = node.key - diff; // decrease key
		if (node.parent != null){ // if the node is not a root
			if (node.key < node.parent.key){ // if the heap rule is violated
				detachNode(node); // recursively detach nodes to maintain heap rule
			}
		} else if (changeMin) {
			// update min if necessary
			if(node.key < minNode.key) {
				setMin(node);
			}
		}
	}

	/**
	 *
	 * Delete the node from the heap.
	 *
	 */
	public void delete(HeapNode node) {
		if (node == null) return; //edge case
		if (node == minNode) { // use the deleteMin
			deleteMin();
			return;
		}
		// perform decreaseKey without changing minNode
		changeMin = false; 
		decreaseKey(node, node.key - minNode.key + 1);
		changeMin = true;
		// add node's children to heap's rootList and remove the node itself
		if (node.child != null) {
				HeapNode child = node.child;
				do {
					child.parent = null;
					child = child.next;
				} while (child != node.child);
				mergeCircularLists(minNode, node.child);
		}
		numOfRoots += node.rank;
		removeNodeFromCircularList(node);
		// update heap's fields
		numOfRoots--;
		total_nodes--;

	}


	/**
	 *
	 * Return the total number of links.
	 *
	 */
	public int totalLinks(){
		return totalLinksCnt;
	}


	/**
	 *
	 * Return the total number of cuts.
	 *
	 */
	public int totalCuts(){
		return totalCutsCnt;
	}


	/**
	 *
	 * Meld the heap with heap2
	 *
	 */
	public void meld(FibonacciHeap heap2){
		// if heap2 is clear, no changed needed
		if (heap2 == null || heap2.minNode == null){
			return;
		}

		// if this heap is empty, copy heap2 into it , later destroy heap2
		if (minNode == null){
			this.minNode = heap2.minNode;
			this.total_nodes = heap2.total_nodes;
			this.numOfRoots = heap2.numOfRoots;
			this.totalCutsCnt = heap2.totalCutsCnt;
			this.totalLinksCnt = heap2.totalLinksCnt;

		// both heaps aren't empty, connect the rootLists and update the heap's fields
		} else {
			mergeCircularLists(minNode, heap2.minNode);
			if (heap2.minNode.key < this.minNode.key) setMin(heap2.minNode);
			this.total_nodes += heap2.total_nodes;
			this.numOfRoots += heap2.numOfRoots;
			this.totalCutsCnt += heap2.totalCutsCnt;
			this.totalLinksCnt += heap2.totalLinksCnt;
		}
		
		// destroy heap2 so it won't be useable afterwards
		heap2.clear();
		return;
	}


	/**
	 *
	 * Return the number of elements in the heap
	 *
	 */
	public int size(){
		return total_nodes;
	}


	/**
	 *
	 * Return the number of trees in the heap.
	 *
	 */
	public int numTrees(){
		return numOfRoots;
	}


	// destroy the heap by reset
	private void clear() {
		minNode = null;
		this.total_nodes = 0;
		this.numOfRoots = 0;
		this.totalCutsCnt = 0;
		this.totalLinksCnt = 0;
	}


	// update the minimum
	private void setMin(HeapNode node){
		minNode = node;
	}


	// add new root
	private void addNodeToList(HeapNode node, HeapNode other) {
		node.prev = other;
		node.next = other.next;
		other.next.prev = node;
		other.next = node;
	}

	// connect two circular lists
	private void mergeCircularLists(HeapNode a, HeapNode b) {
		if (a == null || b == null) return;
		HeapNode aNext = a.next;
		HeapNode bPrev = b.prev;
		a.next = b;
		b.prev = a;
		aNext.prev = bPrev;
		bPrev.next = aNext;
	}



	// remove a root from circular list
	public void  removeNodeFromCircularList(HeapNode node) {
		node.prev.next = node.next;
		node.next.prev = node.prev;
		node.next = node;
		node.prev = node;
	}

	// recursive detaching nodes
	private void detachNode(HeapNode node){
		removeNodeFromCircularList(node); // remove the node from rootList
		totalCutsCnt++; //update field
		// save the new child of node parent after cut node
		HeapNode parent = node.parent;
		if (parent != null) {
			if (node.next == node) parent.child = null;
			else parent.child = node.next;
			parent.rank--;
		}
		node.parent = null;
		addNodeToList(node, minNode); // add node to root list
		numOfRoots++;
		node.mark = false;
		if (parent != null) {
			if (parent.mark && parent.parent != null) detachNode(parent);
			else if (parent.parent != null) parent.mark = true;
		}
		if (node.key < minNode.key && changeMin) setMin(node);
	}


	
	private void consolidate() {
		if (minNode == null) return;
		
		//create an array of degrees according to the maxDegree, all set to null
		int maxDegree = (int) (Math.log(total_nodes) / Math.log(2)) + 1;
		HeapNode[] degreeArray = new HeapNode[maxDegree];
		
		// Traverse the circular root list and link trees with same degree
		HeapNode start = minNode;
		HeapNode current = minNode;
		do {
			HeapNode node = current;
			current = current.next;
			int degree = node.rank;

			while (degreeArray[degree] != null) {
				HeapNode other = degreeArray[degree];
				if (other.key < node.key) {
					HeapNode temp = node;
					node = other;
					other = temp;
				}
				link(other, node); // Merge trees
				degreeArray[degree] = null;
				degree++;
			}
			degreeArray[degree] = node;
		} while (current != start);
		
		//reorder pointers between roots
		afterConsolidation(degreeArray);
	}


	private void afterConsolidation(HeapNode[] roots) {
		minNode = null;
		for (HeapNode node : roots) {
			if (node != null) {
				if (minNode == null) {
					minNode = node;
					node.next = node;
					node.prev = node;
				} else {
					addNodeToList(node, minNode);
					if (node.key < minNode.key) {
						setMin(node);
					}
				}
			}
		}
	}

	private void link(HeapNode y, HeapNode x) {
		HeapNode xChild = x.child;
		y.parent = x;
		if (xChild == null) {
			x.child = y;
			y.next = y;
			y.prev = y;
		} else {
			addNodeToList(y, xChild);
			// Make sure child points to the largest key among children
			if (y.key > xChild.key) x.child = y;
		}
		x.rank++;
		numOfRoots--;
		y.mark = false;
		totalLinksCnt++;
	}

	/**
	 * Class implementing a node in a Fibonacci Heap.
	 *
	 */
	public static class HeapNode{
		public int key;
		public String info;
		public HeapNode child;
		public HeapNode next;
		public HeapNode prev;
		public HeapNode parent;
		public int rank = 0;
		public boolean mark;

		public HeapNode(int key, String info){
			this.key = key;
			this.info = info;
			this.mark = false;
			this.next = this;
			this.prev = this;
		}
	}
}