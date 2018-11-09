package gov.epa.festc.gui;
 
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;


public class ListWidget extends JList {

    private boolean changed;

    private DefaultListModel model;
    
    private List<Integer> indexes;

    public ListWidget(Object[] items) {
        model = model(items);
        this.setModel(model);
        changed = false;
    }

    public ListWidget(Object[] items, Object[] selected) {
        this(items);
        setSelected(selected);
    }

    private DefaultListModel model(Object[] items) {
        DefaultListModel model = new DefaultListModel();
        if (items != null && items.length >0){
            for (int i = 0; i < items.length; i++) {
                model.addElement(items[i]);
            }
        }
        return model;
    }
    
    public void setElements(Object[] items) {
        model.removeAllElements();
        if (items != null && items.length >0){
            for (int i = 0; i < items.length; i++) {
                model.addElement(items[i]);
            }
        }
    }
    

    public void setSelected(Object[] selected) {
        indexes = new ArrayList();
        for (int i = 0; i < selected.length; i++)
            indexes.add(model.indexOf(selected[i]));
        //super.setSelectedIndex(indexes.toArray(new Integer[0]));
    }
    
    public List<Integer> getSelected() {
        return indexes;
    }

    public boolean hasChanges() {
        return changed;
    }

    public void clear() {
        changed = false;
    }

    public boolean contains(Object obj) {
        return model.contains(obj);
    }

    public void addElement(Object obj) {
        model.addElement(obj);
    }

    public void add(int index, Object obj) {
        model.add(index, obj);
    }

    public void removeElements(Object[] removeValues) {
        for (int i = 0; i < removeValues.length; i++) {
            model.removeElement(removeValues[i]);
        }
    }
    
    public void removeAllElements() {
        model.removeAllElements();
//        for (int i = 0; i < model.size(); i++) {
//            model.removeElement(model.get(i));
//        }
    }
    
    public String getElement(int index) {
        return model.elementAt(index).toString();
//        for (int i = 0; i < model.size(); i++) {
//            model.removeElement(model.get(i));
//        }
    }
    
    public void removeSelectedElements() {
        
        Object [] selectedValues = this.getSelectedValues();
       for (int i = 0; i < selectedValues.length; i++) {
           model.removeElement(selectedValues[i]);
       }
    }

    public String[] getAllElements() {
        String[] obj = new String[model.getSize()];
        model.copyInto(obj);
        return obj;
    }

    //Swap two elements in the list.
    public void swap(int a, int b) {
        Object aObject = model.getElementAt(a);
        Object bObject = model.getElementAt(b);
        model.set(a, bObject);
        model.set(b, aObject);
    }

    public void setModelSize(int modelSize) {
        this.model.setSize(modelSize);
        this.model.trimToSize();
    }
}
