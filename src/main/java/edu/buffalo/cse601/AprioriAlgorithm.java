package main.java.edu.buffalo.cse601;

//import static main.java.edu.buffalo.cse601.AprioriAlgorithm.grid;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class AprioriAlgorithm {
  //Data structure to store all transactions and items associated with them
  public static int Confidence_counter=0;
  public static ArrayList<HashSet<String>> grid = new ArrayList<HashSet<String>>();
  //Data structure to store current candidacy list
  private static HashMap<String, Integer> currCandidacyList = new HashMap<>();
  //Data structure to store final frequency list
  private static ArrayList<String> freqList = new ArrayList<>();
  //Data structure to store first frequency list i.e. for itemSetCount = 1;
  private static ArrayList<String> freqListAfterFirstIteration = new ArrayList<>();
  //Data structure to store all unique items found in grid
  private static ArrayList<String> finalUniqueItems = new ArrayList<>();
  //Data structure to store all unique items found in grid (will be modified for each support value)
  private static ArrayList<String> uniqueItemsForEachSupp = new ArrayList<>();
  //Data structure to store frequent items whose support>=50 and itemSetCount >=2
  private static HashMap<String,Integer> freqListForAssociation = new HashMap<>();
  //variable to store size of the grid
  private static int sizeOfGrid;
  
  
  public static void main(String[] args) {
    //reading input file and parsing it to get all data structures
    
    double minSup = 30;
    readAndParseInput();
    for(int i=0;i<5;i++){
      double count=0;
      //make sure to add loops for all support values
      System.out.println("Support is set to be " + minSup + "%");
      int itemSetCount = 1;
      if(itemSetCount == 1){
          generateCandidancyList(finalUniqueItems,itemSetCount);
          generateFreqList(minSup,itemSetCount);
          freqListAfterFirstIteration = new ArrayList<>(freqList);
          System.out.println("number of length-"+ itemSetCount+" frequent itemset: "+ freqList.size());
          count += freqList.size();
          itemSetCount++;
      }  
      if(itemSetCount==2){
          String[] data = new String[itemSetCount];
          uniqueItemsForEachSupp.clear();
          generateCandidacyListOfGivenLength(freqListAfterFirstIteration, freqListAfterFirstIteration.size(), itemSetCount, 0, data, 0);
          generateCandidancyList(uniqueItemsForEachSupp, itemSetCount);
          generateFreqList(minSup,itemSetCount);
          System.out.println("number of length-"+ itemSetCount+" frequent itemset: "+ freqList.size());
          count += freqList.size();
          itemSetCount++;
      } 
      if(itemSetCount>=3){
        while(freqList.size()!=0){
          generateCandidacyCartesian(freqList, itemSetCount);
          generateCandidancyList(uniqueItemsForEachSupp, itemSetCount);
          generateFreqList(minSup,itemSetCount);
          System.out.println("number of length-"+ itemSetCount+" frequent itemset: "+ freqList.size());
          count += freqList.size();
          
          itemSetCount++;
        }
      }
      minSup+=10;
      System.out.println("Total: "+ count);
      System.out.println();
    }
    //System.out.println(freqListForAssociation);
    findAssociations(freqListForAssociation);
  }
  
  
  public static void readAndParseInput(){
      String fileName = "InputDataSet/gene_expression.txt";
      HashSet<String> uniqueItemSet = new HashSet<>();
      
      String line = null;
      try {
          FileReader fileReader = new FileReader(fileName);
          @SuppressWarnings("resource")
          BufferedReader bufferedReader = new BufferedReader(fileReader);
          while((line=bufferedReader.readLine())!=null){
            String lineSplit[] = line.split("\\t");
            HashSet<String> lineHashSet = new HashSet<String>();
            int len = lineSplit.length;
            for(int i=1;i<=len-2;i++){
              lineSplit[i] = "G" + i + "_" + lineSplit[i];
              lineHashSet.add(lineSplit[i]);
              uniqueItemSet.add(lineSplit[i]);
            }
            lineHashSet.add(lineSplit[len-1]);
            uniqueItemSet.add(lineSplit[len-1]);
            grid.add(lineHashSet);
          }
           
      } catch (FileNotFoundException e) {
        System.out.println("Unable to open file" + fileName);
      } catch (IOException e) {
        System.out.println("Error reading from file" + fileName);
      }
      
      sizeOfGrid = grid.size();
      finalUniqueItems = new ArrayList<String>(uniqueItemSet);
      
  }
  
  
  public static void generateCandidacyCartesian(ArrayList<String> list, int itemSetCount){
    uniqueItemsForEachSupp.clear();
    for(int i=0;i<list.size();i++){
      String s = list.get(i).trim();
      String s1 = s.substring(0, s.lastIndexOf(" "));
      for(int j=i+1;j<list.size();j++){
        String w = list.get(j).trim();
        String w1 = w.substring(0, w.lastIndexOf(" "));
        if(s1.equals(w1)){
          uniqueItemsForEachSupp.add(s + " " + w.substring(w.lastIndexOf(" ")+1));
        }
      }
    }
  }
  
  public static void generateCandidancyList(ArrayList<String> list,int itemSetCount) {
  if(itemSetCount==1){
    currCandidacyList.clear();
    for(int k=0; k<list.size() ; k++) {
      String s = list.get(k);
      for(int i=0; i<grid.size() ; i++) {
        if(grid.get(i).contains(s)) {
          if(currCandidacyList.containsKey(s) ){
            int val = currCandidacyList.get(s);
            currCandidacyList.put(s,++val);
          }
          else
            currCandidacyList.put(s,1);
        }
      }
    }
  } else {
    currCandidacyList.clear();
    for(int k=0;k<list.size();k++){
      String s = list.get(k);
      String[] sSplit = s.split(" ");
      int len = sSplit.length;
      for(int j=0;j<grid.size();j++){
        boolean flag = true;
        for(int i=0;i<len;i++){
          if(!grid.get(j).contains(sSplit[i])){
            flag=false;
            break;
          }
        }
        if(flag==true){
          if(currCandidacyList.containsKey(s) ){
            int val = currCandidacyList.get(s);
            currCandidacyList.put(s,++val);
          }
          else{
            currCandidacyList.put(s,1);
          }
        }
      }
    }
  }
  }
  
  public static void generateFreqList(double minSup, int itemCount) {
      freqList.clear();
      minSup /=100;
      Set<String> set = currCandidacyList.keySet();
      for(String s : set){
        if( (double) currCandidacyList.get(s)/sizeOfGrid >=minSup){
          freqList.add(s);
          if(minSup*100>=50 && itemCount>=2){
            freqListForAssociation.put(s, currCandidacyList.get(s));
          }
        }
      }
  }
public static void generateCandidacyListOfGivenLength(ArrayList<String> uniqueItems, int uniqueItemsSize, int itemSize, int index, String[] data, int i ){
        if (index == itemSize)
        {
            String result = "";
            for (int j=0; j<itemSize; j++)
                result = result + data[j]+" ";
        uniqueItemsForEachSupp.add(result);
        return;
        }
 
        if (i >= uniqueItemsSize)
        return;
 
        data[index] = uniqueItems.get(i);
        generateCandidacyListOfGivenLength(uniqueItems, uniqueItemsSize, itemSize, index+1, data, i+1);
 
        generateCandidacyListOfGivenLength(uniqueItems, uniqueItemsSize, itemSize, index, data, i+1);
  }

public static void findAssociations(HashMap<String,Integer> map){
  for(String s : map.keySet()) {
    String[] sA = s.split(" ");
    if(sA.length == 2){
      calculateConfidence(s, map.get(s), sA[0],sA[1]);
      calculateConfidence(s, map.get(s), sA[1],sA[0]);
    }
    else if(sA.length == 3){
      for(int i=0; i<sA.length;i++){
        if(i==0){
          calculateConfidence(s, map.get(s), sA[i], sA[i+1]+" "+sA[i+2]);
          calculateConfidence(s, map.get(s), sA[i+1]+" "+sA[i+2],sA[i]);
        }
        else if(i==1){
          calculateConfidence(s, map.get(s), sA[i], sA[i-1]+" "+sA[i+1]);
          calculateConfidence(s, map.get(s), sA[i-1]+" "+sA[i+1],sA[i]);
        }
        else{
          calculateConfidence(s, map.get(s), sA[i], sA[i-2]+ " "+sA[i-1]);
          calculateConfidence(s, map.get(s), sA[i-2]+ " "+sA[i-1],sA[i]);
        }
      }
    }
  }
}

public static void calculateConfidence(String s1, int count1, String body, String head){
  //System.out.println(body+ "->"+head);
  int count=0;
      String[] sSplit = body.split(" ");
      int len = sSplit.length;
      for(int j=0;j<grid.size();j++){
        boolean flag = true;
        for(int i=0;i<len;i++){
          if(!grid.get(j).contains(sSplit[i])){
            flag=false;
            break;
          }
        }
        if(flag==true){
          count++;
        }
      }
      
      double confidence = (double) count1/count*1.0;
      if(confidence>=0.60){
        //System.out.println(body+ "->" + head+ " C "+confidence);
        Confidence_counter++;
      }
}
}
