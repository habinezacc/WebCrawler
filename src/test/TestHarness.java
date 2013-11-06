/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package test;

import java.net.MalformedURLException;
import crawler.SearchT;
import java.net.URL;

/**
 *
 * @author iwainaina
 */
public class TestHarness {

    /**
     * @param args the command line arguments
     */
    public static void main2(String[] args) throws MalformedURLException {
        // TODO code application logic here
       SearchT smap = new SearchT();
       smap.addString("today",new URL("http://www.today.com"));
       smap.addString("today",new URL("http://www.today.com"));
       smap.addString("today",new URL("http://www.today.com"));
       smap.addString("vivens", new URL("http://www.vivens.com"));
       smap.addString("vivens", new URL("http://www.vivens.fr"));
       smap.addString("today", new URL("http://www.history.com/this-day-in-history"));
       smap.addString("today", new URL("http://www.todayonline.com"));
       smap.addString("kesho", new URL("http://support.kesho.co.uk"));
       smap.addString("kesho", new URL("http://keshokenya.org"));
       smap.addString("kesho", new URL("http://keshokenya.org"));
       smap.addString("kesho", new URL("http://kesho.co.nz"));
       
       smap.printHashMap();
       
    }
    
}
