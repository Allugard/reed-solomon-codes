import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * Created by Verner on 15.03.2017.
 *
 */
public class EncodingMain {
    public static void main(String[] args) throws IOException {


        Encoder galuaField = new Encoder(285, 8 ,3); //8
//        Encoder galuaField = new Encoder(19, 4, 3);  //4
//        Encoder galuaField = new Encoder(11, 3, 2);
//        galuaField.printGF();
//        System.out.println("-------------------");
////
//        galuaField.printAddTable();
//        System.out.println("-------------------");
//        galuaField.printMulTable();

        galuaField.encode("informationSymbols", "encodedMessage");

//        int a = 0;
//        int b = 14;
//
//        System.out.println(galuaField.getPolynom(a)+"   "+galuaField.getPolynom(b)+"   "+galuaField.add(a, b) + "  "+galuaField.getPolynom(galuaField.add(a,b)));



//        DecodingMain.main(null);
//        galuaField.printGeneratingPolynom();
//        galuaField.printControlSymbols();
//        galuaField.printInformationSymbols();

//        try {
//            FileWriter fileWriter = new FileWriter("informationSymbolsRand");
//            Random r = new Random();
//            String text = "";
//            for (int i = 0; i < 249; i++) {
////                text += Integer.toHexString(r.nextInt(254));
//                text += Integer.toHexString(1);
//                text += "\n";
//            }
//            fileWriter.write(text);
//            fileWriter.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
