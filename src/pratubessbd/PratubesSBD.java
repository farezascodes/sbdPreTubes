package pratubessbd;

import java.io.*;
import java.util.*;

public class PratubesSBD {
    
    static boolean parserQuery(String[] kata) throws IOException{
        List<List<String>> csv = new ArrayList();
        csv = bacafile();
        if(kata.length > 3){
            if(kata[kata.length-1].charAt(kata[kata.length-1].length()-1) == ';'){
                if(kata[0].toLowerCase() == "select"){
                    if(kata[2].toLowerCase() == "from"){
                        int i = 3;
                        boolean syntax = true;
                        boolean joinState = false;
                        List<String> initial = new ArrayList();
                        while(i<kata.length && syntax){
                            if(kata[i].toLowerCase() != "join"){
                                syntax = parserTabel(kata[i], csv);
                                initial.add(kata[i-1]);
                                i++;
                                if(kata[i].toLowerCase() != "join"){
                                    initial.add(kata[i]);
                                    i++;
                                }
                                if(joinState){
                                    if(kata[i].toLowerCase() == "on"){
                                        i++;
                                        syntax = parserOn(kata[i], csv, initial);
                                    } else {
                                        syntax = false;
                                    }
                                    joinState = false;
                                }
                            } else {
                                joinState = true;
                                i++;
                            }
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
                    if(temp[0]==inisial.get(j)){
                        for(int k=0;k<csv.size();k++){
                            if(csv.get(k).get(0)==inisial.get(j-1)){
                                //search temp[1] pada csv.get(k).get(1) sampai ke csv.get(k).get(csv.get(k).size())
                                for(int l=1;l<csv.get(k).size();l++){
                                    if(temp[1]==csv.get(k).get(l)){
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }else if(temp.length==1){
                for(int j=0;j<inisial.size();j=j+2){
                    if(temp[0]==inisial.get(j)){
                        for(int k=0;k<csv.size();k++){
                            if(csv.get(k).get(0)==inisial.get(j)){
                                //search temp[0] pada csv.get(k).get(1) sampai ke csv.get(k).get(csv.get(k).size())
                                for(int l=1;l<csv.get(k).size();l++){
                                    if(temp[0]==csv.get(k).get(l)){
                                        return true;
                                    }
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
            if(kata==csv.get(i).get(0)){
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
//        Scanner sc = new Scanner(System.in);
//        String input = sc.nextLine();
//        String[] syntax = input.split(" ");

        List<List<String>> csv = new ArrayList();
        csv = bacafile();
        System.out.println(csv);
        
//        for(int i = 0; i < syntax.length;i++){
//            System.out.println(syntax[i]);
//        }

// C:\\Users\\ahmad\\Documents\\NetBeansProjects\\pratubesSBD\\

//        if(parser(syntax)){
//            printSpesifikasi();
//        } else {
//            System.out.println("syntax Error");
//        }
    }
}