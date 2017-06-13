import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

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

public class Encoder {

    private int m;
    private int k;
    private int polynom;
    private int power;
    private int [][] addTable;
    private int [][] mulTable;
    private int [] galuaElements;
    private int [] generatingPolynom;
    private int [] informationSymbols;
    private int [] controlSymbols;



    public Encoder(int polynom, int m, int k){
        this.m = m;
        this.k = k;
        power = (int) Math.pow(2, this.m) - 1;
        addTable = new int[power][power];
        mulTable = new int[power][power];
        galuaElements = new int[power];
        controlSymbols = new int[2*k];
        informationSymbols = new int [(power-2*k)];
        this.polynom = polynom;
        generateField();
        fillTables();
        generatingPolynom = fillGeneratingPolynom();
    }

    public void encode(String input, String output) throws IOException {
        inputInformationSymbolsFromFile(new FileReader(input));
//        inputInformationSymbolsFromFile(new FileReader("informationSymbols3"));
        generateControlSymbols();
        outputEncodedMessage(new FileWriter(output));
    }

    private void outputEncodedMessage(FileWriter fileWriter) throws IOException {
            String text = "";
            for (int i = 0; i <informationSymbols.length; i++) {
                text += Integer.toHexString(galuaElements[informationSymbols[i]]);
                text += "\n";
            }
            for (int i = 0; i <controlSymbols.length; i++) {
                text += Integer.toHexString(galuaElements[controlSymbols[i]]);
                text += "\n";
            }
            fileWriter.write(text);
            fileWriter.flush();
    }




    private void generateControlSymbols() {
        int[] buf = Arrays.copyOf(informationSymbols, informationSymbols.length + controlSymbols.length);
        for (int i = informationSymbols.length; i < buf.length; i++) {
            buf[i] = -1;
        }
        for (int i = 0; i < informationSymbols.length; i++) {
            int element = mul(buf[i], generatingPolynom[0]);
            int k = 0;
            boolean first = true;
            for (int j = 0; j < generatingPolynom.length; j++) {
                if (j == 0) {
                    buf[i + j] = -1;
                    continue;
                }
                if (buf[i + j] == -1) {
                    buf[i + j] = mulTable[element][generatingPolynom[j]];
                    continue;
                }
                int second = mulTable[element][generatingPolynom[j]];
                if (addTable[buf[i + j]][second] == -1 && first) {
                    k++;
                }
                buf[i + j] = addTable[buf[i + j]][second];
                first = false;
            }
            i += k;
//            System.out.println(Arrays.toString(buf));
        }
        for (int i = 0, j = informationSymbols.length; i < controlSymbols.length; i++, j++) {
            controlSymbols[i] = buf[j];
        }
    }

    private void inputInformationSymbolsFromFile(FileReader fileReader) throws IOException {
        int i = 0;
        String str;
        BufferedReader inStream = new BufferedReader(fileReader);

        while ((str = inStream.readLine()) != null) {
            informationSymbols[i] = findElement(Integer.parseInt(str, 16));
            i++;
//            System.out.println(i);
        }
    }

    private int [] fillGeneratingPolynom() {
        int [] buf = {0, 1};
        for (int i = 1; i < 2*k; i++) {
            buf = multiplicationOfPolynomials(buf, new int[] {0, i + 1});
        }
        return buf;
    }

    private int[] multiplicationOfPolynomials(int[] buf, int[] ints) {
        int mul [][] = new int[buf.length][];
        for (int i = 0; i < buf.length; i++) {
            mul[i] = new int[buf.length + ints.length - (i+1)];
            for (int j = ints.length; j < mul[i].length; j++) {
                mul[i][j] = -1;
            }
        }

        for (int i = 0; i < mul.length; i++) {
            for (int j = 0; j < ints.length; j++) {
                mul[i][j] = mul(buf[i], ints[j]);
            }
        }

        for (int i = mul.length - 1; i > 0; i--) {
            for (int j = 1; j < mul[i - 1].length; j++) {
                mul[i-1][j] = add(mul[i-1][j], mul[i][j-1]);
            }
        }

        return mul[0];
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

    public int getPolynom(int a){
        return galuaElements[a];
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


    public void printGeneratingPolynom() {
        System.out.println(Arrays.toString(generatingPolynom));
    }

    public void printControlSymbols() {
        System.out.println(Arrays.toString(controlSymbols));
    }

    public void printInformationSymbols() {
        System.out.println(Arrays.toString(informationSymbols));
    }
}
