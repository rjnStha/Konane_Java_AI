/*

    *********************************************************
    * Name:  Rojan Shrestha                                 *
    * Project:  Project1, Konane                            *
    * Class: CMPS 331 - Artificial Intelligence             *
    * Date:  2/14/2018                                     *
    *********************************************************

*/

package edu.ramapo.rshresth.konane;

import java.util.ArrayList;
import java.util.List;

class Node<T> {
    private List<Node<T>> children = new ArrayList<>();
    private Node<T> parent = null;
    private T dataRow = null, dataCol = null;

    //Constructor
    public Node(T dataRow,T dataCol) {
        this.dataRow = dataRow;
        this.dataCol = dataCol;
    }

    public Node(T dataRow,T dataCol, Node<T> parent) {
        this.dataRow = dataRow;
        this.dataCol = dataCol;
        this.parent = parent;
        parent.children.add(this);
    }

    public List<Node<T>> getChildren() {
        return children;
    }

    public void setParent(Node<T> parent) {
        parent.addChild(this);
        this.parent = parent;
    }

    public Node<T> getParent(){
        return this.parent;
    }

    public void addChild(T dataRow,T dataCol) {
        Node<T> child = new Node<>(dataRow,dataCol);
        child.setParent(this);
        this.children.add(child);
    }

    public void addChild(Node<T> child) {
        child.setParent(this);
        this.children.add(child);
    }

    public T getDataRow() { return this.dataRow; }

    public T getDataCol() {return this.dataCol; }

    public void setData(T dataRow,T dataCol) {
        this.dataRow = dataRow;
        this.dataCol = dataCol;
    }

    public boolean isRoot() {
        return (this.parent == null);
    }

    public boolean isLeaf() {
        if(this.children.size() == 0)
            return true;
        else
            return false;
    }

    public void removeParent() {
        this.parent = null;
    }
}

public class Tree {
    private Node<Integer> parentNode;
    public Tree() {
       this.parentNode = new Node<>(-1,-1);
    }

    public Node<Integer> getRoot(){
        return parentNode;
    }
}
