/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package greedysnake;

/**
 *
 * @author Administrator
 */
public class Util {
    public static String getJDK_Version(){
      String isjdkver1=System.getProperty("java.version");
      return isjdkver1.substring(0, isjdkver1.indexOf("_"));
    }
}
