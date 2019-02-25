package pratubessbd;

import java.io.*;
import java.util.*;

public class PratubesSBD {
    
    static boolean parserQuery(String[] kata) throws IOException{
        List<List<String>> csv = new ArrayList();
        csv = bacafile();
        if(kata.length > 3){
            if(kata[kata.length-1].charAt(kata[kata.length-1].length()-1) == ';'){
                kata[kata.length-1] = kata[kata.length-1].substring(0,kata[kata.length-1].length()-1);
                // menghilangkan indeks terakhir pada kata[kata.length-1]
                if(kata[0].toLowerCase().equals("select")){
                    if(kata[2].toLowerCase().equals("from")){
                        int i = 3;
                        boolean syntax = true;
                        boolean joinState = false;
                        boolean initialState = false;
                        boolean afterInitialState = false;
                        List<String> initial = new ArrayList();
                        while(i<kata.length && syntax){
                            if(initialState){
                                if(!kata[i].toLowerCase().equals("join")){
                                    initial.add(kata[i]);
                                } else {
                                    initial.add(" ");
                                }
                                initialState = false;
                                afterInitialState = true;
                                
                            }
                            else if(!kata[i].toLowerCase().equals("join")){
                                syntax = parserTabel(kata[i], csv);
                                initial.add(kata[i]);
                                initialState = true;
                            } 
                            else if(joinState && afterInitialState){
                                syntax = parserOn(kata[i],csv,initial);
                                joinState = false;
                                afterInitialState = false;
                            } else {
                                joinState = true;
                                afterInitialState = false;
                            }
                            i++;
                        }
                        if(syntax){
                            syntax = parserKolom(kata[1], csv, initial);
                            return syntax;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    static boolean parserKolom(String kata, List<List<String>> csv, List<String> inisial){
        String[] pisahKoma;
        String[] temp;
        
        pisahKoma=kata.split(",");
        
        for(int i=0;i<pisahKoma.length;i++){
            temp=pisahKoma[i].split("\\.");
            if(temp.length==2){
                for(int j=1;j<inisial.size();j=j+2){
                    if(temp[0].equals(inisial.get(j))){
                        for(int k=0;k<csv.size();k++){
                            if(csv.get(k).get(0).equals(inisial.get(j-1))){
                                //search temp[1] pada csv.get(k).get(1) sampai ke csv.get(k).get(csv.get(k).size())
                                for(int l=1;l<csv.get(k).size();l++){
                                    if(temp[1].equals(csv.get(k).get(l))){
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }else if(temp.length==1){
                for(int j=0;j<inisial.size();j=j+2){
                        for(int k=0;k<csv.size();k++){
                            if(csv.get(k).get(0).equals(inisial.get(j))){
                                for(int l=1;l<csv.get(k).size();l++){
                                    if(temp[0].equals(csv.get(k).get(l))){
                                        return true;
                                    }
                                }
                            }
                        }
                }
            }
        }
        return false;
    }
    
    static boolean parserTabel(String kata, List<List<String>> csv){
        for(int i=0;i<=csv.size();i++){            
            if(kata.equals(csv.get(i).get(0))){
                return true;
            }            
        }
        return false;
    }
    
    static boolean parserOn(String kata, List<List<String>> csv, List<String> initial){
        return false;
    }
    
    static void printSpesifikasi(){
        // print apa yang soal butuhkan
    }
    
    static List bacafile() throws FileNotFoundException, IOException{
        BufferedReader reader;
        File file = new File("Data Dictionary.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        
        String st;
        List<List<String>> b = new ArrayList();
        List<String> a = new ArrayList();
        while ((st = br.readLine()) != null){
            String temp = "";
            a = Arrays.asList(st.split(";"));
            temp = a.get(a.size()-1);
            temp = temp.replace(temp, temp.substring(0, temp.length()-1));
            a.set(a.size()-1, temp);
            b.add(a);
        }
        
//        for(int i = 0; i < b.size(); i++){
//            System.out.println(b.get(i));
//        }
        
        return b;
    }
    
    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        String input = sc.nextLine();
        String[] syntax = input.split(" ");

        List<List<String>> csv = new ArrayList();
        csv = bacafile();
        
//        for(int i = 0; i < syntax.length;i++){
//            System.out.println(syntax[i]);
//        }

// C:\\Users\\ahmad\\Documents\\NetBeansProjects\\pratubesSBD\\

        if(parserQuery(syntax)){
            System.out.println("MATA PANCING");
        } else {
            System.out.println("BANGSAT");
        }
    }
}