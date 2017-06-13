import java.io.IOException;

/**
 * Created by Verner on 15.03.2017.
 *
 */
public class DecodingMain {
    public static void main(String[] args) throws IOException {


        Decoder galuaField = new Decoder(285, 8 ,3); //8
//        Decoder galuaField = new Decoder(19, 4, 3);  //4
//        Decoder galuaField = new Decoder(11, 3, 2);
//        galuaField.printGF();
//        System.out.println("-------------------");
//
//        galuaField.printAddTable();
//        System.out.println("-------------------");
//        galuaField.printMulTable();

        galuaField.decode("encodedMessage", "error");
//        galuaField.printDecodedMessage();
//        galuaField.printSyndrome();


    }
}
