package pratubessbd;

import java.io.*;
import static java.lang.Math.ceil;
import java.util.*;

public class PratubesSBD {

    static List<List<String>> usedData = new ArrayList();

    static void menu() {
        Scanner choose = new Scanner(System.in);
        String choice = null;
        while (!"0".equals(choice)) {
            System.out.println("menu: ");
            System.out.println("1. BFR dan Fan Out Rasio");
            System.out.println("2. Jumlah Block");
            System.out.println("3. Pencarian Record");
            System.out.println("4. QEP dan Cost");
            System.out.println("5. Shared Pool");
            choice = choose.nextLine();
            System.out.println(!"0".equals(choice));
            if ("1".equals(choice)) {
                System.out.println("satu");
            }
            if ("2".equals(choice)) {
                System.out.println("dua");
            }
            if ("3".equals(choice)) {
                System.out.println("tiga");
            }
            if ("4".equals(choice)) {
                System.out.println("empat");
            }
            if ("5".equals(choice)) {
                System.out.println("lima");
            } else {

            }
        }
        choose.close();
    }

    static boolean parserQuery(String[] kata, List<String> initials) throws IOException {
        List<List<String>> csv = new ArrayList();
        boolean syntax = false;
        csv = bacafile();//baca file database
        if (kata.length > 3) {
            if (kata[kata.length - 1].charAt(kata[kata.length - 1].length() - 1) == ';') { // cek untuk ';' diakhir
                kata[kata.length - 1] = kata[kata.length - 1].substring(0, kata[kata.length - 1].length() - 1); // menghilangkan indeks terakhir pada kata[kata.length-1]
                if (kata[0].toLowerCase().equals("select")) { //jika kata pertama select
                    if (kata[2].toLowerCase().equals("from")) { //jika kata ketiga from
                        int i = 3;
                        syntax = true;
                        boolean tempSyntax = true; // digunakan ketika membaca join
                        boolean initialsCheck = false; //expect selanjutnya adalah inisial tabel, contoh:"mahasiswa m"
                        boolean joinState = false; //untuk cek kalo kata selanjutnya "join"
                        while (i < kata.length && syntax) { //loop kata sepanjang i
                            if (!kata[i].equalsIgnoreCase("join") && !kata[i].equalsIgnoreCase("on") && !kata[i].equalsIgnoreCase("using")) { //cek kalo bukan join atau on
                                if (initialsCheck == true) { //jika ditemukan inisial tabel setelah nama tabel
                                    initials.add(kata[i]); //tambahkan ke list initial
                                    initialsCheck = false; //kembalikan ke value semula 

                                } else { //jika kata selanjutnya bukan initial
                                    syntax = parserTabel(kata[i], csv); //lakukan parserTabel yang mengembalikan boolean
                                    initialsCheck = true; //telah dilakukan parserTable maka initial check diisi true
                                    initials.add(kata[i]); //tambahkan nama tabel ke list initial

                                    if (kata.length - 1 == i) {
                                        initials.add(""); //kalau inisial tabel tidak ada tambahkan string kosong 
                                    }
                                }
                            } else if (kata[i].equalsIgnoreCase("join")) { //jika kata indeks ke-i adalah join tanpa peduli uppercase atau lowercase
                                if (initialsCheck == true) { //jika ditemukan inisial tabel setelah nama tabel
                                    initials.add(" "); //tambahkan whitespace di initial
                                    initialsCheck = false; //kembalikan initial check ke value semula
                                }
                                if (joinState == true) { //jika hanya ada join tanpa on maka joinState true
                                    syntax = false; //karna tidak ada "on" maka query dianggap salah
                                }
                                joinState = true;
                                tempSyntax = false; //jika tidak ditemukan join maka tempSyntax false
                            } else if (kata[i].equalsIgnoreCase("on")) { //jika kata ke-i merupakan "on"
                                if (initialsCheck == true) { //jika ditemukan inisial tabel setelah nama tabel
                                    initials.add(" "); //tambahkan whitespace ke list initial
                                    initialsCheck = false; //kembalikan ke semula
                                } else {
                                    System.out.println("SQL Error: Not found defined attribute in table");
                                }
                                i++; //list kata maju
                                if (i < kata.length) { //jika belum mencapai indeks terakhir dari list kata
                                    for (int j = 0; j < initials.size() / 2; j++) { //looping untuk mendapat nama tabel
                                        List<String> templist = new ArrayList();
                                        usedData.add(templist); //append templist ke usedData untuk print
                                    }
                                    syntax = parserOn(kata[i], csv, initials); //lakukan parserOn yang mengembalikan boolean
                                    usedData.clear(); //hapus isi dari usedData
                                } else { //jika list kata sudah mencapai indeks terakhir
                                    syntax = false;
                                    System.out.println("Error");//syntaks dianggap false
                                }
                                joinState = false; //assign joinstate ke false 
                                tempSyntax = true; //jika ditemukan join maka tempSyntax true
                            } else if (kata[i].equalsIgnoreCase("using")) {
                                i++;
                                syntax = parserUsing(kata[i], csv, initials);
                                tempSyntax = true;
                            }
                            i++; //list kata maju
                        }
                        if (syntax) { //jika syntax masih benar
                            for (i = 0; i < initials.size() / 2; i++) { //loop sebanyak size dari list initial dibagi 2 untuk mendapat nama tabel
                                List<String> templist = new ArrayList();
                                usedData.add(templist);
                            }
                            String[] temp; //define list temp
                            temp = kata[1].split(","); //tampung nama kolom atau kata apapun yang sudah dipisahkan berdasar "," (koma)
                            for (i = 0; i < temp.length; i++) { //loop sebanyak list temp 
                                syntax = parserKolom(temp[i], csv, initials); //lakukan parserKolom dari kata yang tertampung di list temp
                                if (!syntax) { //jika parserKolom mengembalikan false karna query salah
                                    break; //hentikan for karna dianggap query salah
                                }
                            }
                        }
                        if (!tempSyntax) { //jika tidak ditemukan join 
                            syntax = false; //query salah
                        }
                    }
                }
            }
        }
        return syntax;
    }

    static boolean parserUsing(String kata, List<List<String>> csv, List<String> inisial) {
        kata = kata.substring(1, kata.length() - 1);
        int[] check;
        int jmlcheck = 0;

        for (int i = 0; i < inisial.size(); i = i + 2) {
            int a = 0;
            for (int l = 0; l < csv.size(); l++) {
                a = csv.get(l).indexOf(kata);
                if (a == 1) {
                    jmlcheck++;
                    break;
                }
            }
        }

        if (jmlcheck == 2) {
            return true;
        }

        return false;
    }

    static boolean parserKolom(String kata, List<List<String>> csv, List<String> inisial) { //dipakai kalo dipanggil nama kolomnya
        String[] temp;
        temp = kata.split("\\.");
        boolean check = true;
        if (temp.length == 2) {
            int j = inisial.indexOf(temp[0]);
            if (j == -1) {
                return false;
            }
            for (int k = 0; k < csv.size(); k++) {
                if (csv.get(k).get(0).equals(inisial.get(j - 1))) {
                    if (csv.get(k).indexOf(temp[1]) == -1) {
                        return false;
                    } else {
                        usedData.get((j - 1) / 2).add(temp[1]);
                        return true;
                    }
                }
            }
        } else {
            for (int j = 0; j < inisial.size(); j = j + 2) {
                for (int k = 0; k < csv.size(); k++) {
                    if (csv.get(k).get(0).equals(inisial.get(j))) {
                        if (csv.get(k).indexOf(temp[0]) == -1) {
                            return false;
                        } else {
                            usedData.get((j) / 2).add(temp[0]);
                            return true;
                        }
                    }
                }
            }
        }
        //    }
        return false;
    }

    static boolean parserTabel(String kata, List<List<String>> csv) {
        for (int i = 0; i < csv.size(); i++) { // looping untuk mencari tabel
            if (kata.equals(csv.get(i).get(0))) {
                return true;
            }
        }
        return false;
    }

    static boolean parserOn(String kata, List<List<String>> csv, List<String> initial) {
        // input berupa (m.mhs=r.mhs)
        String[] pisahtitik = null;
        String[] temp;
        ArrayList<String> temp2 = new ArrayList();
        kata = kata.substring(1, kata.length() - 1);
        temp = kata.split("=");

        if (temp.length == 2) {
            for (int i = 0; i < temp.length; i++) {
                if (parserKolom(temp[i].toString(), csv, initial)) {
                    pisahtitik = temp[i].split("\\.");
                    if (pisahtitik.length == 2) {
                        temp2.add(pisahtitik[0]);
                    } else {
                        return false;
                    }
                }
            }
            if (temp2.size() == 2) {
                if (!temp2.get(0).equals(temp2.get(1))) {
                    return true;
                }
            }
        }
        return false;
    }

    static void printSpesifikasi(List<String> initials) {
        // print apa yang soal butuhkan
        for (int i = 0; i < initials.size(); i = i + 2) {
            System.out.println("\nTabel : " + initials.get(i));
            System.out.print("Atribut: ");
            for (int j = 0; j < usedData.get(i / 2).size(); j++) {
                System.out.print(usedData.get(i / 2).get(j) + ", ");
            }
            System.out.println("");
        }
    }

    static void BFRandFanOutRasio(List<List<String>> csv) {
        for (int i = 1; i < csv.size(); i++) {
            double bfr = Math.ceil(Float.parseFloat(csv.get(0).get(1)) / Float.parseFloat(csv.get(i).get(csv.get(i).size() - 3)));
            double FOR = Math.floor(Float.parseFloat(csv.get(0).get(1)) / (Float.parseFloat(csv.get(i).get(csv.get(i).size() - 1)) + Float.parseFloat(csv.get(0).get(0))));
            // block / pointer 
            System.out.println("BFR " + csv.get(i).get(0) + " : " + bfr);
            // FOR floor(B/(V+P)
            System.out.println("Fan Out Rasio " + csv.get(i).get(0) + " : " + FOR);
        }
    }

    static void jumBlock(List<List<String>> csv) { //menu nomer 2
        for (int i = 1; i < csv.size(); i++) {
            double jumRekord = Float.parseFloat(csv.get(i).get(csv.get(i).size() - 2));
            double rekord = Float.parseFloat(csv.get(i).get(csv.get(i).size() - 3));
            double jumBlok = Math.ceil((jumRekord * rekord) / Float.parseFloat(csv.get(0).get(1))); // (jumlah rekord * size rekord)/block size= banyak blok tersedia

            double FOR = Math.floor(Float.parseFloat(csv.get(0).get(1)) / (Float.parseFloat(csv.get(i).get(csv.get(i).size() - 1)) + Float.parseFloat(csv.get(0).get(0)))); //fan-out
            double jumIndex = Math.ceil(jumBlok / FOR); // jumlah blok / (pointer size+primary key size) = banyak rekord yang disimpan di index

            System.out.println("Tabel Data " + csv.get(i).get(0) + ": " + jumBlok);
            System.out.println("Indeks " + csv.get(i).get(0) + ": " + jumIndex);
        }
    }

    static void cariRecord(List<List<String>> csv) { //belom dikoreksi rumusnya, menu nomer 3
        Scanner cari = new Scanner(System.in);
        System.out.println(">> Cari rekord ke- :");
        String hasil = cari.nextLine().replaceAll("\\s+", "");
        System.out.println(">> Nama tabel :");
        String tabel = cari.nextLine().replaceAll("\\s+", "");

        if (hasil.matches("[0-9]+") && tabel.matches("[a-zA-Z_]")) {
            for (int i = 1; i < csv.size(); i++) {
                if (tabel.equalsIgnoreCase(csv.get(i).get(0))) {
                    double jumRekord = Float.parseFloat(csv.get(i).get(csv.get(i).size() - 2));
                    double rekord = Float.parseFloat(csv.get(i).get(csv.get(i).size() - 3));
                    double jumBlok = Math.ceil((jumRekord * rekord) / Float.parseFloat(csv.get(0).get(1))); // (jumlah rekord * size rekord)/block size = banyak blok tersedia
                    double FOR = Math.floor(Float.parseFloat(csv.get(0).get(1)) / (Float.parseFloat(csv.get(i).get(csv.get(i).size() - 1)) + Float.parseFloat(csv.get(0).get(0)))); //fan-out ratio
                    double jumIndex = Math.ceil(jumBlok / FOR); //banyak rekord di index
                    
                    double notIndexed = Math.ceil(Integer.parseInt(hasil) / (Math.ceil(Float.parseFloat(csv.get(0).get(1)) / Float.parseFloat(csv.get(i).get(csv.get(i).size() - 3)))));
                    double indexed = Math.ceil(notIndexed/jumIndex)+1; // banyak blok yang diakses lewat index (jumlah blok di main/fan-out)
                    
                    System.out.println("Menggunakan indeks, jumlah blok yang diakses: "+indexed);
                    System.out.println("Tanpa indeks, jumlah blok yang diakses: "+notIndexed);
                }
            }
        } else {
            System.out.println("Tipe data inputan tidak sesuai");
        }
    }

    static List bacafile() throws FileNotFoundException, IOException {
        BufferedReader reader;
        File file = new File("Data Dictionary.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String st = br.readLine();
        List<List<String>> b = new ArrayList();
        List<String> a = new ArrayList();
        List<String> tempList = new ArrayList();
        List<String> pointerandblock = new ArrayList();
        String temp = "";
        a = Arrays.asList(st.split(";"));
        for (String content : a) {
            tempList = Arrays.asList(content.split(" "));
            pointerandblock.add(tempList.get(1).replace("#", ""));
        }
        b.add(pointerandblock);
        while ((st = br.readLine()) != null) {
            temp = "";
            a = Arrays.asList(st.split(";"));
            temp = a.get(a.size() - 1);
            temp = temp.replace(temp, temp.substring(0, temp.length() - 1));

            a.set(a.size() - 1, temp);

            for (int i = 0; i < a.size(); i++) {
                tempList = Arrays.asList(a.get(i).split(" "));
                if (tempList.size() == 2) {
                    a.set(i, tempList.get(1));
                }
            }
            b.add(a);
        }
        return b; //tadinya b
    }

    public static void main(String[] args) throws IOException {
//        Scanner sc = new Scanner(System.in);
//        System.out.println("Query: ");
//        menu();
//System.out.println("dsdsds");
        List<List<String>> csv = bacafile();
        System.out.println(csv);
//        jumBlock(csv);
        BFRandFanOutRasio(csv);

//        String input = sc.nextLine();
//        String[] syntax = input.split(" ");
//        List<String> initials = new ArrayList();
//
//        if(parserQuery(syntax, initials)){
//            printSpesifikasi(initials);
//        } else {
//            System.out.println("\nSyntax Error");
//        }
    }
}
