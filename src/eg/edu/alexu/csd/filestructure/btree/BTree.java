package eg.edu.alexu.csd.filestructure.btree;

import javax.management.RuntimeErrorException;
import java.util.*;

public class BTree<K extends Comparable<K>, V> implements IBTree<K,V> {

    private IBTreeNode <K,V> root;
    private int minKey;
    private int maxKey;
    private int minChild;
    private int maxChild;
    private Stack<IBTreeNode<K,V>> nodes;
    private Stack<Integer> indexes;

    BTree (int t) {
        if (t < 2) throwException(null);
        minChild = t; maxChild = minChild*2;
        minKey = minChild - 1; maxKey = maxChild - 1;
        root = new BTreeNode<>();
        nodes = new Stack<>();
        indexes = new Stack<>();
    }

    private void throwException(Object element){
        if (element == null) throw new RuntimeErrorException(null);
    }

    private IBTreeNode<K,V> newNode() {
        return new BTreeNode<>();
    }

    private void clear(){
        nodes.clear(); indexes.clear();
    }

    @Override
    public int getMinimumDegree() {
        return minKey+1;
    }

    @Override
    public IBTreeNode<K, V> getRoot() {
        return (root.getKeys().size() == 0) ? null : root;
    }

    @Override
    public void insert(K key, V value) {
        throwException(key);  throwException(value);

        V val = search(key);
        if (val != null) return;
        while (!nodes.isEmpty()) {

            IBTreeNode<K,V> node = nodes.pop(); int index = indexes.pop();
            List<K> keys =  node.getKeys(); List<V> values = node.getValues();
            List<IBTreeNode<K,V>> children = node.getChildren();

            keys.add(index,key); values.add(index,value);
            if (keys.size() < maxChild) return;

            int index1 = 0; if (key.compareTo(keys.get(minChild)) >= 0) index1 = -1;

            IBTreeNode<K,V> anotherNode = newNode(); anotherNode.setLeaf(node.isLeaf());
            anotherNode.setKeys(keys.subList(minChild+1+index1,keys.size())); anotherNode.setValues(values.subList(minChild+1+index1,values.size()));

            for (int i = maxKey; i >= minChild+1+index1; i--) {
                keys.remove(i); values.remove(i);
            }

            key = keys.remove(minChild+index1); value = values.remove(minChild+index1);
            if (!node.isLeaf()) {
                anotherNode.setChildren(children.subList(minChild+1+index1,children.size()));
                children.removeAll(anotherNode.getChildren());
            }
            if (!nodes.empty()) {
                nodes.peek().getChildren().add(indexes.peek()+1,anotherNode);
            } else {
                IBTreeNode<K,V> rootNode = newNode();
                rootNode.getKeys().add(key);
                rootNode.getValues().add(value);
                rootNode.getChildren().add(node);
                rootNode.getChildren().add(anotherNode);
                rootNode.setLeaf(false);
                root = rootNode;
            }
            
        }

    }

    @Override
    public V search(K key) {
        throwException(key);  clear();

        IBTreeNode<K,V> treeNode = root; V value = null;

        while (true){

            List<K> keys = treeNode.getKeys(); int i = 0;
            List<IBTreeNode<K,V>> children = treeNode.getChildren();
            Iterator<K> iterator = keys.iterator();

            for(;iterator.hasNext();i++) {

                int compare = key.compareTo(iterator.next());

                if (compare > 0) continue;

                if (compare == 0) {
                    List<V> values = treeNode.getValues();
                    value = values.get(i);
                } break;
            }
            nodes.push(treeNode); indexes.push(i);
            if (!treeNode.isLeaf() && value == null) {
                treeNode = children.get(i);
                continue;
            }
            break;
        }

        return value;
    }

    @Override
    public boolean delete(K key) {
        throwException(key);

        V value = search(key);
        if (value == null) return false;
        if (!nodes.peek().isLeaf()) {

            List<K> keys = nodes.peek().getKeys(); List<V> values = nodes.peek().getValues();
            int index = indexes.pop(); indexes.push(index+1);
            nodes.push(nodes.peek().getChildren().get(index+1)); indexes.push(0);
            while(!nodes.peek().isLeaf()) {
                nodes.push(nodes.peek().getChildren().get(0)); indexes.push(0);
            }
            keys.set(index,nodes.peek().getKeys().remove(0)); values.set(index,nodes.peek().getValues().remove(0));
        } else {
            int temp = indexes.peek();
            nodes.peek().getKeys().remove(temp); nodes.peek().getValues().remove(temp);
        }
        indexes.pop();
        remove(nodes.pop());

        return true;
    }

    private void remove(IBTreeNode<K,V> node) {

        if (node.getNumOfKeys() >= minKey || node == root) return;

        IBTreeNode<K,V> parent = nodes.pop(); int parentIndex = indexes.pop();

        IBTreeNode<K,V> left = null,right = null; int index = -1;

        if (parentIndex > 0) {
            left = parent.getChildren().get(parentIndex-1);
            if (left.getNumOfKeys() > minKey) {
                index = 0;
            }
        }
        if (parentIndex < parent.getNumOfKeys()) {
            right = parent.getChildren().get(parentIndex+1);
            if (right.getNumOfKeys() > minKey) {
                index = node.getNumOfKeys();
            }
        }

        if (index != -1) {
            if (index == 0) parentIndex--;
            node.getKeys().add(index,parent.getKeys().get(parentIndex));
            node.getValues().add(index,parent.getValues().get(parentIndex));
            IBTreeNode<K,V> child = (index == 0) ? left : right;
            if (!node.isLeaf()) {
                node.getChildren().add((index == 0) ? 0 : node.getNumOfKeys(),child.getChildren().remove((index == 0) ? child.getNumOfKeys() : 0));
            }
            index = (index == 0) ? child.getNumOfKeys()-1 : 0;
            parent.getKeys().set(parentIndex,child.getKeys().remove(index));
            parent.getValues().set(parentIndex,child.getValues().remove(index));
            return;
        }

        if (left == null){
            left = node;
        }
        else{
            right = node; parentIndex--;
        }


        left.getKeys().add(parent.getKeys().remove(parentIndex));
        left.getValues().add(parent.getValues().remove(parentIndex));
        left.getKeys().addAll(right.getKeys());
        left.getValues().addAll(right.getValues());
        left.getChildren().addAll(right.getChildren());
        parent.getChildren().remove(right);

        if (parent.getNumOfKeys() > 0) remove(parent);
        else root = left;
    }

}
