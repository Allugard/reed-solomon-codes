import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Verner on 15.03.2017.
 * polynom
 * g(x)=(x2+(a2+a1)x+a(1+2))*(x2+(a3+a4)x+a(3+4))*(x2+x(a5+a6)+a(5+6))
 * = (x2+x*a4+a3)(x2+a6*x+a0)(x2+x*a1+a4)
 * = ((x4+x3*a6+x2*a0)+(x3*a4+x2*a3+x*a4)+(x2*a3+x*a2+a3))(x2+x*a1+a4)
 * =(x4+x3(a6+a4)+x2(a0+a3+a3)+x(a2+a4)+a3)(x2+x*a1+a4)
 * =(x4+x3*a3+x2*a0+x*a1+a3)(x2+x*a1+a4)
 * =((x6+x5*a1+x4*a4)+(x5*a3+x4*a4+x3*a0)+(x4*a0+x3*a1+x2*a4)+(x3*a1+x2*a2+x*a5)+(x2*a3+x*a4+a0))
 * = x6+x5*a0+x4*a0+x3*a0+x2*a0+x*a0+a0
 */

public class Decoder {

    private int m;
    private int k;
    private int polynom;
    private int power;
    private int [][] addTable;
    private int [][] mulTable;
    private int [] galuaElements;
    private int[] decodedMessage;
    private int [] syndrome;
    private List<Integer> betas;
    private int [] sigmas;
    private int [] psis;





    public Decoder(int polynom, int m, int k){
        this.m = m;
        this.k = k;
        power = (int) Math.pow(2, this.m) - 1;
        addTable = new int[power][power];
        mulTable = new int[power][power];
        galuaElements = new int[power];
        betas = new ArrayList<>();
        decodedMessage = new int[power];
        this.polynom = polynom;
        syndrome = new int[2*k];
        generateField();
        fillTables();
    }

    public void decode(String in, String out) throws IOException {
        inputEncodedMessage(new FileReader(in));
        findSyndrome();
        if(syndromeIsZeros()){
            return;
        }
        int[]buf = findSigmas(k);
        sigmas = Arrays.copyOf(buf, buf.length + 1);
        findBetas();
        findPsis();
        outputVectorError(new FileWriter(out));
    }

    private void outputVectorError(FileWriter fileWriter) throws IOException {
        String text = "";
        int j = 0;
        for (int i = 1; i <= power; i++) {
            if(j < betas.size() && i == power - betas.get(j)){
                text += Integer.toHexString(galuaElements[psis[j]]);
                text += "\n";
                j++;
            }else {
                text += 0;
                text += "\n";
            }
        }
        fileWriter.write(text);
        fileWriter.flush();

    }

    private void findPsis(){
        psis = new int[betas.size()];
        int[][] buf = new int[psis.length][psis.length];
        for (int i = 0; i < buf.length; i++) {
            for (int j = 0; j < buf.length; j++) {
                buf[i][j] = mul(betas.get(j) * (i + 1), 0);
            }
        }
        try {
            psis = calcEquationRoots(buf, Arrays.copyOf(syndrome, psis.length));
        } catch (LinearDepentMatrix linearDepentMatrix) {
            linearDepentMatrix.printStackTrace();
        }

    }

    private void findBetas() {
        for (int i = 0; i < power; i++) {
            int buf = calc(i, sigmas);
            if(buf == -1){
                betas.add(power - i);
            }
        }
    }

    private int [] findSigmas(int size) {
        int[] equationRoots;
        try {
            int[][] buf = new int[size][size];
            int[] res = new int[size];
            for (int i = 0; i < buf.length; i++) {
                for (int j = 0; j < buf.length; j++) {
                    buf[i][j] = syndrome[i+j];
                }
                res[i] = syndrome[size+i];
            }
            equationRoots = calcEquationRoots(buf, res);
        } catch (LinearDepentMatrix e){
            equationRoots = findSigmas(size - 1);
        }



//        for (int i = 0; i < buf.length; i++) {
//            System.out.println(Arrays.toString(buf[i])+res[i]);
//        }



        return equationRoots;
    }

    private int[] calcEquationRoots(int[][] buf, int[] res) throws LinearDepentMatrix {
        if (buf[0][0] == -1 && buf.length == 2){
            int a = buf[0][0];
            buf[0][0] = buf[1][0];
            buf[1][0] = a;
            a = buf[0][1];
            buf[0][1] = buf[1][1];
            buf[1][1] = a;
            a = res[0];
            res[0] = res[1];
            res[1] = a;

        }
        for (int i = 0; i < buf.length; i++) {
            int a = power - buf[i][i];
            for (int j = i; j < buf.length; j++) {
                buf[i][j] = mul(buf[i][j], a);
            }
            res[i] = mul(res[i], a);

            for (int j = i + 1; j < buf.length; j++) {
                if(buf[j][i] != -1) {
                    a = buf[j][i];
                    for (int l = i; l < buf.length; l++) {
                        buf[j][l] = add(buf[j][l], mul(buf[i][l], a));
                    }
                    res[j] = add(res[j], mul(res[i], a));
                }
                if(!checkRow(buf[j],res[j])){
                    throw new LinearDepentMatrix();
                }
            }
        }

        for (int i = buf.length - 2; i >= 0 ; i--) {
            for (int j = i + 1; j < buf.length; j++) {
                res[i] = add(res[i], mul(res[j], buf[i][j]));
            }
        }

//        for (int i = buf.length - 1; i > 0; i--) {
//            for (int j = i - 1; j >= 0 ; j--) {
//                int a = buf[j][i];
//                for (int l = i; l > j; l--) {
//                    buf[j][l] = add(buf[j][l], mul(buf[i][l], a));
//                }
//                res[j] = add(res[j], mul(res[i], a));
//            }
//        }
        return res;
    }

    private boolean checkRow(int[] ints, int res) {
        if(res != -1){
            return true;
        }
        for (int i = 0; i < ints.length; i++) {
            if(ints[i] != -1){
                return true;
            }
        }
        return false;
    }

    private boolean syndromeIsZeros() {
        for (int i = 0; i < syndrome.length; i++) {
            if(syndrome[i] != -1){
                return false;
            }
        }
        return true;
    }

    private void findSyndrome() {
        for (int i = 0; i < syndrome.length; i++) {
            syndrome[i] = calc(i+1, decodedMessage);
        }
    }

    private int calc(int i, int[] src) {
        int result;
        int [] buf = Arrays.copyOf(src, src.length);
//        System.out.println(Arrays.toString(buf));
//        System.out.println(Arrays.toString(fullMessage));

        for (int j = 0, x = src.length - 1; j < src.length; j++, x--) {
//            System.out.println("x="+(x*i) + "  buf=" + buf[j]);
            buf[j] = mul((x * i), buf[j]);
        }
//        System.out.println(Arrays.toString(buf));
        result = buf[0];
        for (int j = 1; j < src.length; j++) {
            if (result == -1){
//                j++;
                result = buf[j];
            } else {
                result = addTable[buf[j]][result];
            }
        }
//        System.out.println(result);
//        System.out.println("-----------------------------");
        return result;
    }

    private void inputEncodedMessage(FileReader fileReader) throws IOException {
        int i = 0;
        String str;
        BufferedReader inStream = new BufferedReader(fileReader);

        while ((str = inStream.readLine()) != null) {
            decodedMessage[i] = findElement(Integer.parseInt(str, 16));
            i++;
        }
    }

    private void generateField() {
        galuaElements[0] = 1;
        for (int i = 1; i < galuaElements.length; i++) {
            galuaElements[i]=(galuaElements[i-1]<<1);
            if((galuaElements[i]&(int)Math.pow(2, m))!=0){
                galuaElements[i]=(galuaElements[i]^polynom);
            }
        }
    }

    public int mul(int a, int b){
        return (a+b)%power;
    }


    public int add(int a, int b){
        if(a == -1){
            return b;
        }

        if(b == -1){
            return a;
        }

        return addTable[a][b];
    }

    private void fillTables() {
        for (int i = 0; i <addTable.length; i++) {
            for (int j = 0; j <addTable.length; j++) {
//                addTable[i][j]=(galuaElements[i]^galuaElements[j]); //values
                addTable[i][j]=findElement((galuaElements[i]^galuaElements[j]));
            }
        }
        for (int i = 0; i < mulTable.length; i++) {
            for (int j = 0; j <mulTable.length-i; j++) {
                mulTable[i][j] = (j+i); // indexes
//                mulTable[i][j]=galuaElements[j+i]; //values
            }
            for (int j = mulTable.length-i; j <mulTable.length; j++) {
                mulTable[i][j] = (-mulTable.length+j+i);
//                mulTable[i][j]=galuaElements[j+i-mulTable.length];
            }
        }
    }

    private int findElement(int i) {
        for (int j = 0; j <galuaElements.length; j++) {
            if (galuaElements[j] == i){
                return j;
            }
        }
        return -1;
    }

    public void printMulTable(){
        for (int i = 0; i <mulTable.length; i++) {
            for (int j = 0; j <mulTable.length; j++) {
                System.out.printf("%d.%d.%d       ",i,j,mulTable[i][j]);
            }
            System.out.println();
        }
    }

    public void printAddTable(){
        for (int i = 0; i <mulTable.length; i++) {
            for (int j = 0; j <mulTable.length; j++) {
                System.out.printf("%d.%d.%d       ",i,j,addTable[i][j]);
            }
            System.out.println();
        }
    }

    public void printGF(){
        for (int i = 0; i < galuaElements.length; i++) {
            System.out.printf("%d.%s\n",i,Integer.toBinaryString(galuaElements[i]));
        }
    }


    public void printDecodedMessage() {
        System.out.println(Arrays.toString(decodedMessage));
    }

    public void printSyndrome(){
        String s = "";
        for (int i = 0; i < syndrome.length; i++) {
            if(syndrome[i] == -1){
                s+= "S" + (i+1) +  "="+ 0 +"\n";

            }else {
                s += "S" + (i + 1) + "=\u03B1^" + syndrome[i] + "\n";
            }
        }
        System.out.println(s);
    }


}
