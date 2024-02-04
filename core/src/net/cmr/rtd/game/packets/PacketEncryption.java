package net.cmr.rtd.game.packets;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import net.cmr.rtd.game.packets.Packet.EncryptionType;
import net.cmr.util.Log;

/**
 * A class that processes encrypted packets depending on the encryption type.
 */
public class PacketEncryption {

    SecretKey AESsecretKey = null;
    IvParameterSpec AESiv = null;
    PublicKey RSApublicKey = null;
    PrivateKey RSAprivateKey = null;

    public PacketEncryption() {
        
    }

    public void setRSAPrivate(PrivateKey privateKey) {
        this.RSAprivateKey = privateKey;
    }
    public void setRSAPublic(PublicKey publicKey) {
        this.RSApublicKey = publicKey;
    }
    public OutputStream encryptRSA(OutputStream stream) {
        // Convert the output stream to an encrypted output stream
        OutputStream encryptedStream = new CipherOutputStream(stream, getRSACipher(Cipher.ENCRYPT_MODE));
        return encryptedStream;
    }
    public InputStream decryptRSA(InputStream stream) {
        // Convert the input stream to an encrypted input stream
        InputStream encryptedStream = new CipherInputStream(stream, getRSACipher(Cipher.DECRYPT_MODE));
        return encryptedStream;
    }

    public void setAESData(SecretKey secretKey, IvParameterSpec iv) {
        this.AESsecretKey = secretKey;
        this.AESiv = iv;
    }
    public OutputStream encryptAES(OutputStream stream) {
        // Convert the output stream to an encrypted output stream
        OutputStream encryptedStream = new CipherOutputStream(stream, getAESCipher(Cipher.ENCRYPT_MODE));
        return encryptedStream;
    }
    public InputStream decryptAES(InputStream stream) {
        // Convert the input stream to an encrypted input stream
        InputStream encryptedStream = new CipherInputStream(stream, getAESCipher(Cipher.DECRYPT_MODE));
        return encryptedStream;
    }

    /**
     * Gets a cipher for RSA encryption.
     * @param encryptionMode The mode to use for the cipher. (Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE)
     * @return The cipher for RSA encryption.
     */
    private Cipher getAESCipher(int encryptionMode) {
        if (AESsecretKey == null || AESiv == null) {
            Log.error("AES secret key or IV is null.", new NullPointerException());
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(encryptionMode, AESsecretKey, AESiv);
            return cipher;
        } catch (Exception e) {
            Log.error("Failed to get AES cipher.", e);
            return null;
        }
    }

    /**
     * Gets a cipher for RSA encryption.
     * @param encryptionMode The mode to use for the cipher. (Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE)
     * @return The cipher for RSA encryption.
     */
    private Cipher getRSACipher(int encryptionMode) {
        if (RSApublicKey == null || RSAprivateKey == null) {
            Log.error("RSA keys are (all?) null.", new NullPointerException());
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            if (encryptionMode == Cipher.DECRYPT_MODE) {
                // Use the private key for decryption
                cipher.init(encryptionMode, RSAprivateKey);
            } else if (encryptionMode == Cipher.ENCRYPT_MODE) {
                // Use the public key for encryption
                cipher.init(encryptionMode, RSApublicKey);
            }
            return cipher;
        } catch (Exception e) {
            Log.error("Failed to get RSA cipher.", e);
            return null;
        }
    }

    public OutputStream encrypt(Packet packet, OutputStream stream) {
        switch (packet.getEncryptionType()) {
            case RSA:
                return encryptRSA(stream);
            case AES:
                return encryptAES(stream);
            default:
                break;
        }
        return stream;
    }

    public InputStream decrypt(EncryptionType type, InputStream stream) {
        switch (type) {
            case RSA:
                return decryptRSA(stream);
            case AES:
                return decryptAES(stream);
            default:
                break;
        }
        return stream;
    }

    // Create encryption objects
    public static KeyPair createRSAKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();
            return keyPair;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static SecretKey createAESKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            SecretKey secretKey = keyGen.generateKey();
            return secretKey;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static IvParameterSpec createIV() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    // Converting encryption objects to objects from bytes
    public static PublicKey publicKeyFromBytes(byte[] publicKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);
            PublicKey decodedPublicKey = keyFactory.generatePublic(keySpec);
            return decodedPublicKey;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static SecretKey secretKeyFromBytes(byte[] secretKeyBytes) {
        SecretKey secretKey = new SecretKeySpec(secretKeyBytes, "AES");
        return secretKey;
    }
    public static IvParameterSpec ivFromBytes(byte[] ivBytes) {
        IvParameterSpec iv = new IvParameterSpec(ivBytes);
        return iv;
    }

    // Converting encryption objects to bytes
    public static byte[] publicKeyToBytes(PublicKey publicKey) {
        return publicKey.getEncoded();
    }
    public static byte[] secretKeyToBytes(SecretKey secretKey) {
        return secretKey.getEncoded();
    }
    public static byte[] ivToBytes(IvParameterSpec iv) {
        return iv.getIV();
    }
    
}
