/*
 * YorkDEV Project
 * 최주원 sch2307@nate.com
 * 주니 leejun860@nate.com
 */

package packet.crypto;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SecureRandom;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class MapleCrypto {

    private byte iv[];
    private Cipher cipher;
    private final short mapleVersion;
    
    //1:239.1 :: '9F' E4 18 9F '15' 42 00 26 'FE' 4C D1 21 '04' 93 2F B3 '8F' 73 53 40 '43' 8A AF 7E 'CA' 6F D5 CF 'D3' A1 95 CE
    //1:240.1 :: '29' 23 BE 84 'E1' 6C D6 AE '52' 90 49 F1 'F1' BB E9 EB 'B3' A6 DB 3C '87' 0C 3E 99 '24' 5E 0D 1C '06' B7 47 DE
    //1.241.1 :: 'B3' 12 4D C8 '43' BB 8B A6 '1F' 03 5A 7D '09' 38 25 1F '5D' D4 CB FC '96' F5 45 3B '13' 0D 89 0A '1C' DB AE 32
    //1.242.2 :: '88' 81 38 61 '6B' 68 12 62 'F9' 54 D0 E7 '71' 17 48 78 '0D' 92 29 1D '86' 29 99 72 'DB' 74 1C FA '4F' 37 B8 B5
    //1.243.2 :: '20' 9A 50 EE '40' 78 36 FD '12' 49 32 F6 '9E' 7D 49 DC 'AD' 4F 14 F2 '44' 40 66 D0 '6B' C4 30 B7 '32' 3B A1 22
    //1.244.2 :: '18' 70 92 DA '64' 54 CE B1 '85' 3E 69 15 'F8' 46 6A 04 '96' 73 0E D9 '16' 2F 67 68 'D4' F7 4A 4A 'D0' 57 68 76
    //1.250.3 :: '76' 2D D0 C2 'C9' CD 68 D4 '49' 6A 79 25 '08' 61 40 14 'B1' 3B 6A A5 '11' 28 C1 8C 'D6' A9 0B 87 '97' 8C 2F F1
    //1.251.1 :: '10' 50 9B C8 '81' 43 29 28 '8A' F6 E9 9E '47' A1 81 48 '31' 6C CD A4 '9E' DE 81 A3 '8C' 98 10 FF '9A' 43 CD CF
    //1.252.1 :: '5E' 4E E1 30 '9C' FE D9 71 '9F' E2 A5 E2 '0C' 9B B4 47 '65' 38 2A 46 '89' A9 82 79 '7A' 76 78 C2 '63' B1 26 DF
    //1.253.1 :: 'DA' 29 6D 3E '62' E0 96 12 '34' BF 39 A6 '3F' 89 5E F1 '6D' 0E E3 6C '28' A1 1E 20 '1D' CB C2 03 '3F' 41 07 84 

    private static final byte[] sSecretKey = new byte[] {
        (byte) 0xDA, 0x00, 0x00, 0x00, (byte) 0x62, 0x00, 0x00, 0x00, (byte) 0x34, 0x00, 0x00, 0x00, (byte) 0x3F, 0x00, 0x00, 0x00, 
        (byte) 0x6D, 0x00, 0x00, 0x00, (byte) 0x28, 0x00, 0x00, 0x00, (byte) 0x1D, 0x00, 0x00, 0x00, (byte) 0x3F, 0x00, 0x00, 0x00};   
    
    private static final byte[] sShiftKey = new byte[]{(byte) 0xEC, (byte) 0x3F, (byte) 0x77, (byte) 0xA4, (byte) 0x45, (byte) 0xD0, (byte) 0x71, (byte) 0xBF, (byte) 0xB7, (byte) 0x98, (byte) 0x20, (byte) 0xFC,
        (byte) 0x4B, (byte) 0xE9, (byte) 0xB3, (byte) 0xE1, (byte) 0x5C, (byte) 0x22, (byte) 0xF7, (byte) 0x0C, (byte) 0x44, (byte) 0x1B, (byte) 0x81, (byte) 0xBD, (byte) 0x63, (byte) 0x8D, (byte) 0xD4, (byte) 0xC3,
        (byte) 0xF2, (byte) 0x10, (byte) 0x19, (byte) 0xE0, (byte) 0xFB, (byte) 0xA1, (byte) 0x6E, (byte) 0x66, (byte) 0xEA, (byte) 0xAE, (byte) 0xD6, (byte) 0xCE, (byte) 0x06, (byte) 0x18, (byte) 0x4E, (byte) 0xEB,
        (byte) 0x78, (byte) 0x95, (byte) 0xDB, (byte) 0xBA, (byte) 0xB6, (byte) 0x42, (byte) 0x7A, (byte) 0x2A, (byte) 0x83, (byte) 0x0B, (byte) 0x54, (byte) 0x67, (byte) 0x6D, (byte) 0xE8, (byte) 0x65, (byte) 0xE7,
        (byte) 0x2F, (byte) 0x07, (byte) 0xF3, (byte) 0xAA, (byte) 0x27, (byte) 0x7B, (byte) 0x85, (byte) 0xB0, (byte) 0x26, (byte) 0xFD, (byte) 0x8B, (byte) 0xA9, (byte) 0xFA, (byte) 0xBE, (byte) 0xA8, (byte) 0xD7,
        (byte) 0xCB, (byte) 0xCC, (byte) 0x92, (byte) 0xDA, (byte) 0xF9, (byte) 0x93, (byte) 0x60, (byte) 0x2D, (byte) 0xDD, (byte) 0xD2, (byte) 0xA2, (byte) 0x9B, (byte) 0x39, (byte) 0x5F, (byte) 0x82, (byte) 0x21,
        (byte) 0x4C, (byte) 0x69, (byte) 0xF8, (byte) 0x31, (byte) 0x87, (byte) 0xEE, (byte) 0x8E, (byte) 0xAD, (byte) 0x8C, (byte) 0x6A, (byte) 0xBC, (byte) 0xB5, (byte) 0x6B, (byte) 0x59, (byte) 0x13, (byte) 0xF1,
        (byte) 0x04, (byte) 0x00, (byte) 0xF6, (byte) 0x5A, (byte) 0x35, (byte) 0x79, (byte) 0x48, (byte) 0x8F, (byte) 0x15, (byte) 0xCD, (byte) 0x97, (byte) 0x57, (byte) 0x12, (byte) 0x3E, (byte) 0x37, (byte) 0xFF,
        (byte) 0x9D, (byte) 0x4F, (byte) 0x51, (byte) 0xF5, (byte) 0xA3, (byte) 0x70, (byte) 0xBB, (byte) 0x14, (byte) 0x75, (byte) 0xC2, (byte) 0xB8, (byte) 0x72, (byte) 0xC0, (byte) 0xED, (byte) 0x7D, (byte) 0x68,
        (byte) 0xC9, (byte) 0x2E, (byte) 0x0D, (byte) 0x62, (byte) 0x46, (byte) 0x17, (byte) 0x11, (byte) 0x4D, (byte) 0x6C, (byte) 0xC4, (byte) 0x7E, (byte) 0x53, (byte) 0xC1, (byte) 0x25, (byte) 0xC7, (byte) 0x9A,
        (byte) 0x1C, (byte) 0x88, (byte) 0x58, (byte) 0x2C, (byte) 0x89, (byte) 0xDC, (byte) 0x02, (byte) 0x64, (byte) 0x40, (byte) 0x01, (byte) 0x5D, (byte) 0x38, (byte) 0xA5, (byte) 0xE2, (byte) 0xAF, (byte) 0x55,
        (byte) 0xD5, (byte) 0xEF, (byte) 0x1A, (byte) 0x7C, (byte) 0xA7, (byte) 0x5B, (byte) 0xA6, (byte) 0x6F, (byte) 0x86, (byte) 0x9F, (byte) 0x73, (byte) 0xE6, (byte) 0x0A, (byte) 0xDE, (byte) 0x2B, (byte) 0x99,
        (byte) 0x4A, (byte) 0x47, (byte) 0x9C, (byte) 0xDF, (byte) 0x09, (byte) 0x76, (byte) 0x9E, (byte) 0x30, (byte) 0x0E, (byte) 0xE4, (byte) 0xB2, (byte) 0x94, (byte) 0xA0, (byte) 0x3B, (byte) 0x34, (byte) 0x1D,
        (byte) 0x28, (byte) 0x0F, (byte) 0x36, (byte) 0xE3, (byte) 0x23, (byte) 0xB4, (byte) 0x03, (byte) 0xD8, (byte) 0x90, (byte) 0xC8, (byte) 0x3C, (byte) 0xFE, (byte) 0x5E, (byte) 0x32, (byte) 0x24, (byte) 0x50,
        (byte) 0x1F, (byte) 0x3A, (byte) 0x43, (byte) 0x8A, (byte) 0x96, (byte) 0x41, (byte) 0x74, (byte) 0xAC, (byte) 0x52, (byte) 0x33, (byte) 0xF0, (byte) 0xD9, (byte) 0x29, (byte) 0x80, (byte) 0xB1, (byte) 0x16,
        (byte) 0xD3, (byte) 0xAB, (byte) 0x91, (byte) 0xB9, (byte) 0x84, (byte) 0x7F, (byte) 0x61, (byte) 0x1E, (byte) 0xCF, (byte) 0xC5, (byte) 0xD1, (byte) 0x56, (byte) 0x3D, (byte) 0xCA, (byte) 0xF4, (byte) 0x05,
        (byte) 0xC6, (byte) 0xE5, (byte) 0x08, (byte) 0x49};
    
    public MapleCrypto(byte iv[], short mapleVersion) {
        Key pKey = new SecretKeySpec(sSecretKey, "AES");
        SecureRandom pRandom = new SecureRandom();
        pRandom.nextBytes(iv);
        Security.addProvider(new BouncyCastleProvider());
        try {
            cipher = Cipher.getInstance("AES/ECB/NoPadding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, pKey);
        } catch (NoSuchAlgorithmException | NoSuchProviderException
                 | NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
        setIv(iv);
        this.mapleVersion = (short) (((mapleVersion >> 8) & 0xFF) | ((mapleVersion << 8) & 0xFF00));
    }

    private static byte[] multiplyBytes(byte[] in, int count, int mul) {
        byte[] ret = new byte[count * mul];
        for (int x = 0; x < count * mul; x++) {
            ret[x] = in[x % count];
        }
        return ret;
    }

    public byte[] crypt(byte[] data) {
        int remaining = data.length;
        int llength = 0x5B0;
        int start = 0;
        try {
            while (remaining > 0) {
                byte[] myIv = multiplyBytes(this.iv, 4, 4);
                if (remaining < llength) {
                    llength = remaining;
                }
                for (int x = start; x < (start + llength); x++) {
                    if ((x - start) % myIv.length == 0) {
                        byte[] newIv = cipher.doFinal(myIv);
                        for (int j = 0; j < myIv.length; j++) {
                            myIv[j] = newIv[j];
                        }
                    }
                    data[x] ^= myIv[(x - start) % myIv.length];
                }
                start += llength;
                remaining -= llength;
                llength = 0x5B4;
            }
            updateIv();
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return data;
    }
    
    private void setIv(byte[] iv) {
	this.iv = iv;
    }
    
    private void updateIv() {
        this.iv = getNewIv(this.iv);
    }
    
    public byte[] getPacketHeader(int length) {
        int iiv = (iv[3]) & 0xFF;
        iiv |= (iv[2] << 8) & 0xFF00;
        iiv ^= mapleVersion;
        int mlength = ((length << 8) & 0xFF00) | (length >>> 8);
        int xoredIv = iiv ^ mlength;
        byte[] ret = new byte[4];
        ret[0] = (byte) ((iiv >>> 8) & 0xFF);
        ret[1] = (byte) (iiv & 0xFF);
        ret[2] = (byte) ((xoredIv >>> 8) & 0xFF);
        ret[3] = (byte) (xoredIv & 0xFF);
        
        return ret;
    }
    
    public static int getPacketLength(int packetHeader) {
	int packetLength = ((packetHeader >>> 16) ^ (packetHeader & 0xFFFF));
	packetLength = ((packetLength << 8) & 0xFF00) | ((packetLength >>> 8) & 0xFF);
        
	return packetLength;
    }
    
    public boolean checkPacket(byte[] packet) {
	return ((((packet[0] ^ iv[2]) & 0xFF) == ((mapleVersion >> 8) & 0xFF)) && (((packet[1] ^ iv[3]) & 0xFF) == (mapleVersion & 0xFF)));
    }
    
    public boolean checkPacket(int packetHeader) {
	byte packetHeaderBuf[] = new byte[2];
	packetHeaderBuf[0] = (byte) ((packetHeader >> 24) & 0xFF);
	packetHeaderBuf[1] = (byte) ((packetHeader >> 16) & 0xFF);
        
	return checkPacket(packetHeaderBuf);
    }
    
    public static byte[] getNewIv(byte oldIv[]) {
        byte[] newIv = {(byte) 0xF2, (byte) 0x53, (byte) 0x50, (byte) 0xC6};
        for (int i = 0; i < 4; i++) {
            Shuffle(oldIv[i], newIv);
        }
        return newIv;
    }

    private static byte[] Shuffle(byte inputValue, byte[] newIV) {
        byte elina = newIV[1];
        byte anna = inputValue;
        byte moritz = sShiftKey[(int) elina & 0xFF];
        moritz -= inputValue;
        newIV[0] += moritz;
        moritz = newIV[2];
        moritz ^= sShiftKey[(int) anna & 0xFF];
        elina -= (int) moritz & 0xFF;
        newIV[1] = elina;
        elina = newIV[3];
        moritz = elina;
        elina -= (int) newIV[0] & 0xFF;
        moritz = sShiftKey[(int) moritz & 0xFF];
        moritz += inputValue;
        moritz ^= newIV[2];
        newIV[2] = moritz;
        elina += (int) sShiftKey[(int) anna & 0xFF] & 0xFF;
        newIV[3] = elina;

        int merry = ((int) newIV[0]) & 0xFF;
        merry |= (newIV[1] << 8) & 0xFF00;
        merry |= (newIV[2] << 16) & 0xFF0000;
        merry |= (newIV[3] << 24) & 0xFF000000;
        int ret_value = merry >>> 0x1d;
        merry <<= 3;
        ret_value |= merry;

        newIV[0] = (byte) (ret_value & 0xFF);
        newIV[1] = (byte) ((ret_value >> 8) & 0xFF);
        newIV[2] = (byte) ((ret_value >> 16) & 0xFF);
        newIV[3] = (byte) ((ret_value >> 24) & 0xFF);
        
        return newIV;
    }
}
