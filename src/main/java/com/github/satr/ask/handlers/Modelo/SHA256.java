package com.github.satr.ask.handlers.Modelo;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class SHA256 {

    public SHA256() {
    }

    public String getHash(String entrada){
        String res = null;

        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            digest.update(entrada.getBytes("utf8"));
            res = String.format("%064x", new BigInteger(1, digest.digest()));

        } catch (Exception e){
            e.printStackTrace();
        }


        return res;

    }

}
