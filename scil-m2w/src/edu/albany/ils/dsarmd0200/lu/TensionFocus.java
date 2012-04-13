/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.albany.ils.dsarmd0200.lu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author ting
 */
public class TensionFocus {

    public TensionFocus(Speaker parent) {
        parent_ = parent;
    }

    /**
     * *********************get information**********************
     */
    public int getCount() {
        return count_;
    }

    public HashMap<String, Integer> getDistributions() {
        return distributions_;
    }

    public Speaker getParent() {
        return parent_;
    }

    public double getScore() {
        return score_;
    }
    public String showDistributions() {
        StringBuffer out = new StringBuffer();
        out.append("+++++++++++++++++speaker: " + parent_.getName() + "\n");
        ArrayList<String> keys = new ArrayList(Arrays.asList(distributions_.keySet().toArray()));
        for (String key: keys) {
            out.append(key + ": " + ((double)distributions_.get(key))/count_ + "\n");
        }
        return out.toString();
    }
    /**
     * *********************set information**********************
     */
    public void setCount(int count_) {
        this.count_ = count_;
    }

    public void addDistribution(String speaker) {
        Integer count = distributions_.get(speaker);
        if (count == null) {
            count = new Integer(0);
            distributions_.put(speaker, count);
        }
        count++;
    }

    public void setParent(Speaker parent_) {
        this.parent_ = parent_;
    }

    public void setScore(double score_) {
        this.score_ = score_;
    }
    
    /**
     * *********************Attributes**********************
     */
    private Speaker parent_ = null;
    private double score_ = 0;
    private int count_ = 0;
    private HashMap<String, Integer> distributions_ = new HashMap<String, Integer>();
}
