package eg.edu.alexu.csd.filestructure.btree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BTreeNode<K extends Comparable<K>, V> implements IBTreeNode<K,V> {

    List<K> keys;
    List<V> values;
    List<IBTreeNode<K, V>> children;
    int numOfKeys;
    boolean leaf;

    BTreeNode(){

        keys = new ArrayList<>();
        values = new ArrayList<>();
        children = new ArrayList<>();
        numOfKeys = 0;
        leaf = true;

    }

    @Override
    public int getNumOfKeys() {
        return numOfKeys = keys.size();
    }

    @Override
    public void setNumOfKeys(int numOfKeys) {
        this.numOfKeys = numOfKeys;
    }

    @Override
    public boolean isLeaf() {
        return leaf;
    }

    @Override
    public void setLeaf(boolean isLeaf) {
        this.leaf = isLeaf;
    }

    @Override
    public List<K> getKeys() {
        return keys;
    }

    @Override
    public void setKeys(List keys) {
        this.keys = newList(keys);
    }

    @Override
    public List<V> getValues() {
        return values;
    }

    @Override
    public void setValues(List values) {
        this.values = newList(newList(values));
    }

    @Override
    public List<IBTreeNode<K, V>> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List children) {
        this.children = newList(children);
    }

    private List<Object> newList(List<Object> list){

        List<Object> list1 = new ArrayList<>();
        Iterator<Object> iterator = list.iterator();

        while (iterator.hasNext()) {
            list1.add(iterator.next());
        }
        return list1;
    }
}
