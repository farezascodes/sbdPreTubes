package pratubessbd;

import java.io.*;
import static java.lang.Math.ceil;
import java.util.*;

public class PratubesSBD {

    static List<List<String>> usedData = new ArrayList();
    static List<List<String>> anotatedQEP = new ArrayList();
    static String input;

    static void menu(List<List<String>> csv) throws IOException {
        Scanner choose = new Scanner(System.in);
        String choice = null;
        while (!"0".equals(choice)) {
            System.out.println("menu: ");
            System.out.println("1. BFR dan Fan Out Rasio");
            System.out.println("2. Jumlah Block");
            System.out.println("3. Pencarian Record");
            System.out.println("4. QEP dan Cost");
            System.out.println("5. Shared Pool");
            System.out.println("0. exit");
            System.out.print("pilih:");
            choice = choose.nextLine();
            System.out.println("");
            if ("1".equals(choice)) {
                BFRandFanOutRasio(csv);
            } else if ("2".equals(choice)) {
                jumBlock(csv);
            } else if ("3".equals(choice)) { 
                cariRecord(csv);
            } else if ("4".equals(choice)) {
                Scanner sc = new Scanner(System.in);
                System.out.println("Query: ");

                input = sc.nextLine();
                String[] syntax = input.split(" ");
                List<String> initials = new ArrayList();
                if (parserQuery(syntax, initials, csv)) {
                    printSpesifikasi(initials);
                    QEPandCost(initials, csv);
                } else {
                    System.out.println("\nSyntax Error");
                }
            } else if ("5".equals(choice)) {
                readQEP();

            } else if ("0".equals(choice)) {
                System.out.println("terima kasih");
            } else {
                System.out.println("input invalid");
            }
            System.out.println("Press \"ENTER\" to continue...");
            try {
                System.in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        choose.close();
    }

    static boolean parserQuery(String[] kata, List<String> initials, List<List<String>> csv) throws IOException {
        boolean syntax = false;
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
                        boolean whereState = false;
                        boolean join = false;
                        boolean where = false;
                        while (i < kata.length && syntax) { //loop kata sepanjang i
                            if (!kata[i].equalsIgnoreCase("join") && !kata[i].equalsIgnoreCase("on") && !kata[i].equalsIgnoreCase("using") && !kata[i].equalsIgnoreCase("where")) { //cek kalo bukan join atau on
                                if (whereState) {
                                    String[] kataWhere = Arrays.copyOfRange(kata, i, kata.length);
                                    syntax = parserWhere(csv, initials, kataWhere);
                                    i = kata.length - 1;
                                } else if (initialsCheck == true) { //jika ditemukan inisial tabel setelah nama tabel
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
                                join = true;
                            } else if (kata[i].equalsIgnoreCase("on")) { //jika kata ke-i merupakan "on"
                                if (initialsCheck == true) { //jika ditemukan inisial tabel setelah nama tabel
                                    initials.add(" "); //tambahkan whitespace ke list initial
                                    initialsCheck = false; //kembalikan ke semula
                                } else {
                                    System.out.println("SQL Error: Not found defined attribute in table");
                                }
                                i++; //list kata maju
                                if (i < kata.length) { //jika belum mencapai indeks terakhir dari list kata
                                    for (int j = 0; j < initials.size() / 2 + 1; j++) { //looping untuk mendapat nama tabel
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
                            } else if (kata[i].equalsIgnoreCase("where")) {
                                whereState = true;
                                where = true;
                            }
                            i++; //list kata maju
                        }
                        if (syntax) { //jika syntax masih benar
                            for (i = 1; i <= initials.size() + 1 / 2; i++) { //loop sebanyak size dari list initial dibagi 2 untuk mendapat nama tabel
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

    static boolean parserWhere(List<List<String>> csv, List<String> inisial, String[] kata) {
        if(kata[1].equals("=")){
            List<String> temp = new ArrayList();
            for (int j = 0; j < inisial.size(); j = j + 2) {
                for (int k = 0; k < csv.size(); k++) {
                    if (csv.get(k).get(0).equals(inisial.get(j))) {
                        if (csv.get(k).indexOf(kata[0]) != -1) {
                            for (int i = 0; i < kata.length; i++) {
                                temp.add(kata[i]);
                            }
                            usedData.add(temp);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    static boolean parserUsing(String kata, List<List<String>> csv, List<String> inisial) {
        kata = kata.substring(1, kata.length() - 1);
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
        boolean check = false;
        if (temp.length == 2) {
            int j = inisial.indexOf(temp[0]);
            if (j == -1) {
                return false;
            }
            for (int k = 0; k < csv.size(); k++) {
                if (csv.get(k).get(0).equals(inisial.get(j - 1))) {
                    if (csv.get(k).indexOf(temp[1]) != -1) {
                        usedData.get((j - 1) / 2 + 1).add(temp[1]);
                        check = true;
                    }
                }
            }
        } else {
            for (int j = 0; j < inisial.size(); j = j + 2) {
                for (int k = 0; k < csv.size(); k++) {
                    if (csv.get(k).get(0).equals(inisial.get(j))) {
                        if (csv.get(k).indexOf(temp[0]) != -1) {
                            usedData.get(((j) / 2) + 1).add(temp[0]);
                            check = true;
                        }
                    }
                }
            }
        }
        //    }
        if (check) {
            return true;
        }
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

    static void QEPtoText(int optimal) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
                    "sharedPool.txt"), true));
            bw.write("\n\n");
            for (int j = 0; j < anotatedQEP.get(optimal).size(); j++) {
                bw.write(anotatedQEP.get(optimal).get(j));
                bw.newLine();
            }
            bw.newLine();
            bw.close();
        } catch (Exception e) {
        }
    }

    static void readQEP() throws IOException {
        File file = new File("sharedPool.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String st = br.readLine();
        while ((st = br.readLine()) != null) {
            System.out.println(st);
        }
    }

    static void printSpesifikasi(List<String> initials) {
        // print apa yang soal butuhkan
        for (int i = 0; i < initials.size(); i = i + 2) {
            System.out.println("\nTabel : " + initials.get(i));
            System.out.print("Atribut: ");
            for (int j = 0; j < usedData.get(i / 2 + 1).size(); j++) {
                System.out.print(usedData.get(i / 2 + 1).get(j) + ", ");
            }
        }
        System.out.println("\n");
    }

    static void BFRandFanOutRasio(List<List<String>> csv) {
        for (int i = 1; i < csv.size(); i++) {
            double bfr = Math.floor(Float.parseFloat(csv.get(0).get(1)) / Float.parseFloat(csv.get(i).get(csv.get(i).size() - 3)));
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
        Scanner sc = new Scanner(System.in);
        System.out.println(">> Cari rekord ke- :");
        int cari = sc.nextInt();
        System.out.println(">> Nama tabel :");
        String tabel = sc.next();
//        if (tabel.matches("[a-zA-Z_]")) {
        for (int i = 1; i < csv.size(); i++) {
            if (tabel.equalsIgnoreCase(csv.get(i).get(0))) {
                double jumRekord = Float.parseFloat(csv.get(i).get(csv.get(i).size() - 2));
                double rekord = Float.parseFloat(csv.get(i).get(csv.get(i).size() - 3));
                double jumBlok = Math.ceil((jumRekord * rekord) / Float.parseFloat(csv.get(0).get(1))); // (jumlah rekord * size rekord)/block size = banyak blok tersedia
                double FOR = Math.floor(Float.parseFloat(csv.get(0).get(1)) / (Float.parseFloat(csv.get(i).get(csv.get(i).size() - 1)) + Float.parseFloat(csv.get(0).get(0)))); //fan-out ratio
                double BFR = (Math.ceil(Float.parseFloat(csv.get(0).get(1)) / Float.parseFloat(csv.get(i).get(csv.get(i).size() - 3))));
                // ceil(cari/bfr)
                double notIndexed = Math.ceil(cari / BFR);
                double indexed = Math.ceil(notIndexed / FOR) + 1; // banyak blok yang diakses lewat index (jumlah blok di main/fan-out)

                System.out.println("Menggunakan indeks, jumlah blok yang diakses: " + indexed);
                System.out.println("Tanpa indeks, jumlah blok yang diakses: " + notIndexed);
                break;
            }
        }
//        } else {
//            System.out.println("Tipe data inputan tidak sesuai");
    }

    static boolean CheckSharedPool(String query) throws FileNotFoundException, IOException {
        File file = new File("sharedPool.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String st;
        while ((st = br.readLine()) != null) {
            if (query.equals(st)) {
                System.out.println("QEP from Shared Pool: ");
                while (!(st = br.readLine()).equals("")) {
                    System.out.println(st);
                }
                return true;
            }
        }
        return false;
    }

    static void QEPandCost(List<String> inisial, List<List<String>> csv) throws IOException {
        boolean onkey = false;

        List<Double> cost = new ArrayList();
        String selection;
        String projection;
        String join;
        String table;
        String strCost;

        if (CheckSharedPool(input)) {
            System.out.println("");
        } else {
            if (inisial.size() < 3) {
                // ini kalau query tanpa join
                int tabel = -1;
                for (int it = 1; it < csv.size(); it++) {
                    if (inisial.get(0).equalsIgnoreCase(csv.get(it).get(0))) {
                        tabel = it;
                    }
                }
                if (tabel > -1 && !usedData.get(0).isEmpty()) {
                    // jumlah block
                    double Br = Math.ceil(Math.ceil(Float.parseFloat(csv.get(tabel).get(csv.get(tabel).size() - 3)) * Float.parseFloat(csv.get(tabel).get(csv.get(tabel).size() - 2))) / Float.parseFloat(csv.get(0).get(1)));
                    for (int i = 1; i <= 2; i++) {
                        selection = "SELECTION ";
                        projection = "PROJECTION ";
                        table = "";
                        strCost = "Cost: ";
                        List<String> tempAnotatedQEP = new ArrayList();
                        System.out.println("QEP #" + i);
                        System.out.print("PROJECTION ");
                        for (int j = 0; j < usedData.get(1).size(); j++) {
                            projection += usedData.get(1).get(j) + ", ";
                            System.out.print(usedData.get(1).get(j) + ", ");
                        }
                        System.out.print("-- on the fly");
                        projection += "-- on the fly";
                        boolean where = false;
                        if (!usedData.get(0).isEmpty()) { // usedData.get(0) atribut where yg dipake
                            where = true;
                            selection += usedData.get(0).get(0) + usedData.get(0).get(1) + usedData.get(0).get(2) + " -- A" + i;
                            System.out.print("\nSELECTION " + usedData.get(0).get(0) + usedData.get(0).get(1) + usedData.get(0).get(2));
                            System.out.print(" -- A" + i);
                        }
                        if (i == 1 && where) {
                            for (int j = 1; j < csv.size(); j++) {
                                if (usedData.get(0).get(0).equals(csv.get(j).get(1))) {
                                    selection += " key";
                                    System.out.print(" key");
                                    onkey = true;
                                }
                            }
                                    
                        }
                        table = inisial.get(0);
                        System.out.println("\n" + inisial.get(0));
                        System.out.print("Cost: ");
                        if (i == 1) {
                            if (onkey) {
                                // br/2
                                cost.add(Math.ceil(Br / 2));
                                strCost += Double.toString(Math.ceil(Br / 2));
                            } else if (!onkey) {
                                // br
                                cost.add(Br);
                                strCost += Double.toString(Math.ceil(Br));
                            }
                        }
                        if (i == 2) {
                            double FOR = Math.floor(Float.parseFloat(csv.get(0).get(1)) / (Float.parseFloat(csv.get(i).get(csv.get(i).size() - 1)) + Float.parseFloat(csv.get(0).get(0))));
                            double hi = Math.ceil(Math.log10(Float.parseFloat(csv.get(0).get(1)) / Math.log10(FOR)));
                            cost.add(hi + 1);
                            strCost += Double.toString(hi + 1);
                        }
                        strCost += " Blocks";
                        System.out.print(cost.get(i - 1) + " Blocks\n");
                        System.out.println("");

                        tempAnotatedQEP.add(input);
                        tempAnotatedQEP.add(projection);
                        if (where) {
                            tempAnotatedQEP.add(selection);
                        }
                        tempAnotatedQEP.add(table);
                        tempAnotatedQEP.add(strCost);
                        anotatedQEP.add(tempAnotatedQEP);
                    }
                }
            } else if (usedData.get(0).isEmpty() && inisial.size() > 2) {
                List<Integer> usedTable = new ArrayList();
                List<String> alreadyPrinted = new ArrayList(); // list yg menyimpan data yang sudah diambil

                for (int h = 0; h < 3; h = h + 2) {
                    for (int i = 1; i < csv.size(); i++) {
                        if (inisial.get(h).equalsIgnoreCase(csv.get(i).get(0))) {
                            usedTable.add(i);
                        }
                    }
                }
                for (int i = 1; i <= 2; i++) {
                    List<String> tempAnotatedQEP = new ArrayList();
                    projection = "PROJECTION ";
                    join = "JOIN ";
                    table = "";
                    strCost = "Cost: ";

                    System.out.println("QEP #" + i);
                    System.out.print("PROJECTION ");
                    if (i == 1) {
                        for (int j = 1; j <= 2; j++) {
                            for (int k = 0; k < usedData.get(j).size(); k++) {
                                boolean found = false; // found in alreadyPrinted
                                for (int l = 0; l < alreadyPrinted.size(); l++) {
                                    if (alreadyPrinted.get(l).equals(usedData.get(j).get(k))) {
                                        found = true;
                                    }
                                }
                                if (!found) {
                                    System.out.print(usedData.get(j).get(k) + ", ");
                                    projection += usedData.get(j).get(k) + ", ";
                                    alreadyPrinted.add(usedData.get(j).get(k));
                                }
                            }
                        }
                    } else {
                        for (int j = 0; j < alreadyPrinted.size(); j++) {
                            System.out.print(alreadyPrinted.get(j) + ", ");
                        }
                    }
                    projection += "-- on the fly";
                    System.out.print("-- on the fly \n");
                    System.out.print("JOIN ");
                    join += csv.get(usedTable.get(0)).get(0) + "." + csv.get(usedTable.get(0)).get(1) + " = ";
                    System.out.print(csv.get(usedTable.get(0)).get(0) + "." + csv.get(usedTable.get(0)).get(1) + " = ");
                    join += csv.get(usedTable.get(1)).get(0) + "." + csv.get(usedTable.get(0)).get(1) + " -- BNLJ";
                    System.out.print(csv.get(usedTable.get(1)).get(0) + "." + csv.get(usedTable.get(0)).get(1) + " -- BNLJ \n");

                    if (i == 2) {
                        // procedure tukar
                        int temp = usedTable.get(0);
                        usedTable.set(0, usedTable.get(1));
                        usedTable.set(1, temp);
                    }
                    for (int j = 0; j < usedTable.size(); j++) {
                        System.out.print(csv.get(usedTable.get(j)).get(0) + "    ");
                        table += csv.get(usedTable.get(j)).get(0) + "    ";
                    }
                    System.out.print("\nCost: ");
                    double Br = Math.ceil(Math.ceil(Float.parseFloat(csv.get(usedTable.get(0)).get(csv.get(usedTable.get(0)).size() - 3)) * Float.parseFloat(csv.get(usedTable.get(0)).get(csv.get(usedTable.get(0)).size() - 2))) / Float.parseFloat(csv.get(0).get(1)));
                    double Bs = Math.ceil(Math.ceil(Float.parseFloat(csv.get(usedTable.get(1)).get(csv.get(usedTable.get(1)).size() - 3)) * Float.parseFloat(csv.get(usedTable.get(1)).get(csv.get(usedTable.get(1)).size() - 2))) / Float.parseFloat(csv.get(0).get(1)));
                    cost.add(Br * Bs + Br);
                    strCost += Double.toString(Br * Bs + Br) + " Blocks";
                    System.out.print(cost.get(cost.size() - 1) + " Blocks\n");
                    System.out.println("");
                    tempAnotatedQEP.add(input);
                    tempAnotatedQEP.add(projection);
                    tempAnotatedQEP.add(join);
                    tempAnotatedQEP.add(table);
                    tempAnotatedQEP.add(strCost);
                    anotatedQEP.add(tempAnotatedQEP);
                }
            }
            if (!cost.isEmpty()) {
                int smallest;
                if (cost.get(0) < cost.get(1)) {
                    smallest = 0;
                } else {
                    smallest = 1;
                }
                System.out.println("QEP Optimal : QEP#" + (smallest + 1));
                QEPtoText(smallest);
                
            } else {
                System.out.println(usedData);
                System.out.println("\nQEP belum bisa dideteksi");
            }
        }
        anotatedQEP.clear();
        usedData.clear();
    }

    static List bacafile() throws FileNotFoundException, IOException {
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
        List<List<String>> csv = new ArrayList();
        csv = bacafile();//baca file database
        menu(csv);

//        System.out.println("");
    }
}
