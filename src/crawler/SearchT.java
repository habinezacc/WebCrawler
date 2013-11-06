package crawler;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author chabineza
 */
public class SearchT {

    private HashMap<String, LinkedList> docSearch;

    public SearchT() {
        docSearch = new HashMap<>();
    }

    public boolean isEmpty() {
        return docSearch.isEmpty();
    }

    public void addString(String word, URL url) {
        LinkedList l = new LinkedList();
        if (docSearch.containsKey(word)) {
            l = docSearch.get(word);
            if(!(l.contains(url))){
                l.add(url);
            }
            
        } else {
            l.add(url);           
        }
        docSearch.put(word, l);

    }
    public HashMap getHashMap(){
        return docSearch;
    }
    public void printHashMap()
    {
        Set<String> keyset = docSearch.keySet();
        for (String key : keyset) {
                        System.out.println(key + " : "+ docSearch.get(key));
                    }
    }
}