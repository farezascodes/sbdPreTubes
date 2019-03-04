package pratubessbd;

import java.io.*;
import java.util.*;

public class PratubesSBD {
    static List<List<String>> usedData=new ArrayList();
    static List<String> tabel=new ArrayList();
    static List<String> kolom=new ArrayList();
    
    static boolean parserQuery(String[] kata, List<String> initials) throws IOException{
        List<List<String>> csv = new ArrayList();
        boolean syntax = false;
        csv = bacafile();
        if(kata.length > 3){
            if(kata[kata.length-1].charAt(kata[kata.length-1].length()-1) == ';'){ // cek untuk ';' diakhir
                kata[kata.length-1] = kata[kata.length-1].substring(0,kata[kata.length-1].length()-1); // menghilangkan indeks terakhir pada kata[kata.length-1]
                if(kata[0].toLowerCase().equals("select")){
                    if(kata[2].toLowerCase().equals("from")){
                        int i = 3;
                        syntax = true;
                        boolean tempSyntax = true; // digunakan ketika membaca join
                        boolean initialsCheck = false; //expect selanjutnya adalah inisial tabel, contoh:"mahasiswa m"
                        boolean joinState = false; //untuk cek kalo kata selanjutnya "join"
                        while(i<kata.length && syntax){ //loop kata sepanjang i
                            if(!kata[i].equalsIgnoreCase("join") && !kata[i].equalsIgnoreCase("on")){ //cek kalo bukan join atau on
                                if(initialsCheck == true){
                                    initials.add(kata[i]);
                                    initialsCheck = false;
                                    
                                } else {
                                    syntax = parserTabel(kata[i], csv);
                                    initialsCheck = true;
                                    initials.add(kata[i]);
                                    if(kata.length-1 == i) initials.add("");
                                }
                            }
                            else if(kata[i].equalsIgnoreCase("join")){
                                if (initialsCheck == true){
                                    initials.add(" ");
                                    initialsCheck = false;
                                }
                                if (joinState == true){
                                    syntax = false;
                                }
                                joinState = true;
                                tempSyntax = false;
                            }
                            else if(kata[i].equalsIgnoreCase("on")){
                                if (initialsCheck == true){
                                    initials.add(" ");
                                    initialsCheck = false;
                                }
                                i++;
                                if (i<kata.length){
                                    for (int j=0;j<initials.size()/2;j++){
                                        List<String> templist = new ArrayList();
                                        usedData.add(templist);
                                    }
                                    syntax = parserOn(kata[i],csv,initials);
                                    usedData.clear();
                                }
                                
                                else {
                                    syntax = false;
                                }
                                joinState = false;
                                tempSyntax = true;
                            }
                            i++;
                        }
                        if(syntax){
                            for (i=0;i<initials.size()/2;i++){
                                List<String> templist = new ArrayList();
                                usedData.add(templist);
                            }
                            String[] temp;
                            temp = kata[1].split(",");
                            for(i=0;i<temp.length;i++){
                                syntax = parserKolom(temp[i], csv, initials);
                                if(!syntax){
                                    break;
                                }
                            }
                        }
                        if(!tempSyntax){
                            syntax = false;
                        }
                    }
                }
            }
        }
        
        return syntax;
    }
    
    static boolean parserKolom(String kata, List<List<String>> csv, List<String> inisial){ //dipakai kalo dipanggil nama kolomnya
        //String[] pisahKoma;
        String[] temp;
        //System.out.println(kata);
        //pisahKoma=kata.split(",");
        
        // for(int i=0;i<pisahKoma.length;i++){
        //     temp=pisahKoma[i].split("\\.");
        //     if(temp.length==2){ // dilakukan ketika ada inisial pada kolom cth: m.nim
        //         for(int j=1;j<inisial.size();j=j+2){ // looping pada list inisial
        //             if(temp[0].equals(inisial.get(j))){
        //         for(int k=0;k<csv.size();k++){ // looping pada csv(tabel) untuk mengecek inisial benar atau tidak
        //                     if(csv.get(k).get(0).equals(inisial.get(j-1))){
        //                         for(int l=1;l<csv.get(k).size();l++){ // looping pada csv(kolom) untuk mengecek nama kolom pada csv
        //                             if(temp[1].equals(csv.get(k).get(l))){
                                        
        //                                 return true;
        //                             }
        //                         }
        //                         return false;
        //                     }
        //                 }
        //             }
        //         }
        //     }else if(temp.length==1){
        //         for(int j=0;j<inisial.size();j=j+2){ // looping pada list inisial
        //                 for(int k=0;k<csv.size();k++){ // looping pada csv(tabel) untuk mengecek kolom pada setiap tabel
        //                     if(csv.get(k).get(0).equals(inisial.get(j))){
        //                         for(int l=1;l<csv.get(k).size();l++){ // looping pada csv(kolom) untuk mengecek nama kolom pada csv
        //                             if(temp[0].equals(csv.get(k).get(l))){
        //                                 return true;
        //                             }
        //                         }
        //                         return false;
        //                     }
        //                 }
        //         }
        //     }
        // }
        // return false;

        //for (String word:pisahKoma){ ap rating
        temp = kata.split("\\.");
        boolean check = true;
        if (temp.length==2) {
            int j =inisial.indexOf(temp[0]);
            if (j==-1)return false;
            for(int k=0;k<csv.size();k++){
                if (csv.get(k).get(0).equals(inisial.get(j-1))){
                    if(csv.get(k).indexOf(temp[1]) == -1){
                        return false;
                    }
                    else{
                        usedData.get((j-1)/2).add(temp[1]);
                        return true;
                    }
                }
            }
        } else {
            for(int k=0;k<csv.size();k++){
                int j = inisial.indexOf(csv.get(k).get(0));
                if(csv.get(k).indexOf(temp[0]) != -1)
                {
                    usedData.get(j/2).add(temp[0]);
                    return true;
                }
            }
            return false;
        }
    //    }
    return false;
    }
    
    static boolean parserTabel(String kata, List<List<String>> csv){
        for(int i=0;i<csv.size();i++){ // looping untuk mencari tabel
            if(kata.equals(csv.get(i).get(0))){
                return true;
            }
        }
        return false;
    }
    
    static boolean parserOn(String kata, List<List<String>> csv, List<String> initial){
        // input berupa (m.mhs=r.mhs)
        String[] pisahtitik=null;
        String[] temp;
        ArrayList<String> temp2= new ArrayList();
        kata = kata.substring(1, kata.length()-1);
        temp = kata.split("=");
        
        //ini kerjaan fareza:
        if(temp.length==2){
            for(int i=0;i<temp.length;i++){
                if(parserKolom(temp[i].toString(),csv,initial)){
                    pisahtitik=temp[i].split("\\.");
                    if (pisahtitik.length == 2){
                        temp2.add(pisahtitik[0]);
                    }
                    else{
                        return false;
                    }
                }
            }
            if(temp2.size()==2){
                if(!temp2.get(0).equals(temp2.get(1))){
                    return true;
                }
            }           
        }
        return false;
    }
    
    static void printSpesifikasi(List<String> initials){
        // print apa yang soal butuhkan
        for(int i=0;i<initials.size();i=i+2){
            System.out.println("\nTabel : "+ initials.get(i));
            System.out.print("Atribut: ");
            for(int j=0;j<usedData.get(i/2).size();j++){
                System.out.print(usedData.get(i/2).get(j) + ", ");
            }
            System.out.println("");
        }
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
        System.out.println("Query: ");
        String input = sc.nextLine();
        String[] syntax = input.split(" ");
        List<String> initials = new ArrayList();
        List<List<String>> csv = new ArrayList();
        csv = bacafile();
        
// C:\\Users\\ahmad\\Documents\\NetBeansProjects\\pratubesSBD\\

        if(parserQuery(syntax, initials)){
            printSpesifikasi(initials);
        } else {
            System.out.println("\nSyntax Error");
        }
    }
}