/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler;

import gui.Canvas;

/**
 *
 * @author chabineza
 */
public class Main {

    public static void main(String args[]) {
        
        CrawlerEngine wc = new CrawlerEngine();
        wc.start();
 
        new Canvas().setVisible(true);
    }
    
}
