package utils;

import expections.WrongFormatExpection;
import redunduncy.Hamming;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class WriterRedundancy implements WriterInterface{
    public static final int LENGTH_OF_BITS_IN_A_BYTE = 8;
    private BufferedWriter bufferedWriter;
    private FileWriter fileWriter;
    private OutputStream os;
    private String bitsStringControle;
    private String bitsStringControleHamming;

    public WriterRedundancy(String path) throws IOException {
        File output = new File(path);

        if (output.exists()) {
            output.delete();
        }
        this.fileWriter = new FileWriter(output);
        this.bufferedWriter = new BufferedWriter(fileWriter);
        this.os = new FileOutputStream(output);
        this.bitsStringControle = "";
        this.bitsStringControleHamming = "";
    }

    public void write(char letter) throws IOException {
        bufferedWriter.write(letter);
    }

    public void write(String bits) throws IOException, WrongFormatExpection {
        this.bitsStringControle = bitsStringControle.concat(bits);
        updateBitStringHamming();

        while(bitsStringControleHamming.length() >= 8) {
            write8bitsOrConcatZerosToComplete(this.bitsStringControleHamming.substring(0, 8), null);
            this.bitsStringControleHamming = this.bitsStringControleHamming.substring(8);
        }
    }

    public void writeSemHamming(String bits) throws IOException, WrongFormatExpection {
//        write8bitsOrConcatZerosToComplete(bits, null); //TODO pro cabeçalho com CRC
    }

    private void updateBitStringHamming() throws WrongFormatExpection {
        while(bitsStringControle.length() >= 4) {
            this.bitsStringControleHamming = this.bitsStringControleHamming.concat(Hamming.introduceRedunduncy(bitsStringControle.substring(0, 4)));
            this.bitsStringControle = this.bitsStringControle.substring(4);
        }
    }

    private void write8bitsOrConcatZerosToComplete(String bits, Integer qntdBitsSemHammingNoFinal) throws IOException {
//        System.out.println(bits);
        int resto = (bits.length() % LENGTH_OF_BITS_IN_A_BYTE);
        int divisorMenosResto = LENGTH_OF_BITS_IN_A_BYTE - resto;
        if (resto != 0) {
            for (int i = 0; i < divisorMenosResto; i++) {
                bits = bits.concat("0");
            }
        }
        os.write(toByteArray(bits));
        System.out.print(bits);
        if (divisorMenosResto != LENGTH_OF_BITS_IN_A_BYTE) {
            String bitsParaNaoFazerHamming = StringUtils.integerToStringBinary(qntdBitsSemHammingNoFinal,2);
            String bitsParaDescartarNoDecode = StringUtils.integerToStringBinary(divisorMenosResto, LENGTH_OF_BITS_IN_A_BYTE - 2);
            os.write(toByteArray(bitsParaNaoFazerHamming.concat(bitsParaDescartarNoDecode)));
            System.out.println("\n\n"+bitsParaNaoFazerHamming.concat(bitsParaDescartarNoDecode));
        }
    }

    public static byte[] toByteArray(String input) {

        List<String> codewardsSplit = Arrays.asList(input.split("(?<=\\G.{8})"));
        byte[] bitMontados = new byte[codewardsSplit.size()];
        for (int i = 0; i < codewardsSplit.size(); i++) {
            bitMontados[i] = convertBitsToByte(codewardsSplit.get(i));
        }
        return bitMontados;
    }

    private static byte convertBitsToByte(String bits) {
        return (byte) Integer.parseInt(bits, 2);
    }

    public void close() throws IOException {
        if(bitsStringControle.length() > 0 || bitsStringControleHamming.length() > 0){
            write8bitsOrConcatZerosToComplete(bitsStringControleHamming.concat(bitsStringControle), bitsStringControle.length());
            bitsStringControle = "";
            bitsStringControleHamming = "";
        }
        bufferedWriter.close();
        fileWriter.close();
        os.close();
    }
}
