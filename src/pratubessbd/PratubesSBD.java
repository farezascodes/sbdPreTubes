package pratubessbd;

import java.io.*;
import java.util.*;

public class PratubesSBD {
    static List<List<String>> usedData=new ArrayList();
    
    static boolean parserQuery(String[] kata, List<String> initials) throws IOException{
        List<List<String>> csv = new ArrayList();
        boolean syntax = false;
        csv = bacafile();
        System.out.println(csv);
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
                                    System.out.println("This is Initials");
                                    initials.add(kata[i]);
                                    initialsCheck = false;
                                } else {
                                    syntax = parserTabel(kata[i], csv);
                                    System.out.println("This is Tabel");
                                    initialsCheck = true;
                                    initials.add(kata[i]);
                                }
                            }
                            else if(kata[i].equalsIgnoreCase("join")){
                                System.out.println("This is Join");
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
                                    System.out.println(i);
                                    System.out.println(kata[i]);
                                    syntax = parserOn(kata[i],csv,initials);
                                    usedData.clear();
                                }
                                else {
                                    syntax = false;
                                }
                                tempSyntax = true;
                            }
                            System.out.println(syntax);
                            i++;
                        }
                        if(syntax){
                            for (i=0;i<initials.size()/2;i++){
                                List<String> templist = new ArrayList();
                                usedData.add(templist);
                            }
                            System.out.println(usedData);
                            String[] temp;
                            temp = kata[1].split(",");
                            for(i=0;i<temp.length;i++){
                                System.out.println("TEMP KE I: ");
                                System.out.println(temp[i]);
                                syntax = parserKolom(temp[i], csv, initials);
                                if(!syntax){
                                    System.out.println("kok salah");
                                    break;
                                }
                            }
                        }
                        if(!tempSyntax){
                            syntax = false;
                            System.out.println("Ketangkep bro");
                        }
                    }
                }
            }
        }
        System.out.println(initials);
        System.out.println(usedData);
        
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
            System.out.println("j: " + j);
            if (j==-1)return false;
            for(int k=0;k<csv.size();k++){
                if (csv.get(k).get(0).equals(inisial.get(j-1))){
                    if(csv.get(k).indexOf(temp[1]) == -1){
                        return false;
                    }
                    else{
                        usedData.get(j/2).add(temp[1]);
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
        System.out.println("---- INI BAGIAN PARSERON ----");
        String[] pisahtitik=null;
        String[] temp;
        ArrayList<String> temp2= new ArrayList();
        System.out.println(kata);
        kata = kata.substring(1, kata.length()-1);
        System.out.println(kata);
        temp = kata.split("=");
        System.out.println(temp[0]);
        System.out.println(temp.length);
        
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
            System.out.println("ini size temp2: ");
            System.out.println(temp2.size());
            if(temp2.size()==2){
                System.out.println(temp2.get(0));
                System.out.println(temp2.get(1));
                if(!temp2.get(0).equals(temp2.get(1))){
                    System.out.println("ini bener");
                    return true;
                }
            }           
        }
        System.out.println("---- END OF PARSER ON ----");
        return false;
    }
    
    static void printSpesifikasi(String kata[], List<String> initials){
        // print apa yang soal butuhkan
        for(int j=1;j<initials.size();j=j+2){
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
        String input = sc.nextLine();
        String[] syntax = input.split(" ");
        List<String> initials = new ArrayList();
        List<List<String>> csv = new ArrayList();
        csv = bacafile();
        
// C:\\Users\\ahmad\\Documents\\NetBeansProjects\\pratubesSBD\\

        if(parserQuery(syntax, initials)){
            printSpesifikasi(syntax, initials);
            System.out.println("MATA PANCING");
        } else {
            System.out.println("BANGSAT");
        }
    }
}