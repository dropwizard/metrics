package com.codahale.metrics.collectd;

import org.junit.Test;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

public class PacketWriterTest {

    private MetaData metaData = new MetaData.Builder("nw-1.alpine.example.com", 1520961345L, 100)
            .type("gauge")
            .typeInstance("value")
            .get();
    private String username = "scott";
    private String password = "t1_g$r";

    @Test
    public void testSignRequest() throws Exception {
        AtomicBoolean packetVerified = new AtomicBoolean();
        Sender sender = new Sender("localhost", 4009) {
            @Override
            public void send(ByteBuffer buffer) throws IOException {
                short type = buffer.getShort();
                assertThat(type).isEqualTo((short) 512);
                short length = buffer.getShort();
                assertThat(length).isEqualTo((short) 41);
                byte[] packetSignature = new byte[32];
                buffer.get(packetSignature, 0, 32);

                byte[] packetUsername = new byte[length - 36];
                buffer.get(packetUsername, 0, packetUsername.length);
                assertThat(new String(packetUsername, UTF_8)).isEqualTo(username);

                byte[] packet = new byte[buffer.remaining()];
                buffer.get(packet);

                byte[] usernameAndPacket = new byte[username.length() + packet.length];
                System.arraycopy(packetUsername, 0, usernameAndPacket, 0, packetUsername.length);
                System.arraycopy(packet, 0, usernameAndPacket, packetUsername.length, packet.length);
                assertThat(sign(usernameAndPacket, password)).isEqualTo(packetSignature);

                verifyPacket(packet);
                packetVerified.set(true);
            }

            private byte[] sign(byte[] input, String password) {
                Mac mac;
                try {
                    mac = Mac.getInstance("HmacSHA256");
                    mac.init(new SecretKeySpec(password.getBytes(UTF_8), "HmacSHA256"));
                } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                    throw new RuntimeException(e);
                }
                return mac.doFinal(input);
            }

        };
        PacketWriter packetWriter = new PacketWriter(sender, username, password, SecurityLevel.SIGN);
        packetWriter.write(metaData, 42);
        assertThat(packetVerified).isTrue();
    }

    @Test
    public void testEncryptRequest() throws Exception {
        AtomicBoolean packetVerified = new AtomicBoolean();
        Sender sender = new Sender("localhost", 4009) {
            @Override
            public void send(ByteBuffer buffer) throws IOException {
                short type = buffer.getShort();
                assertThat(type).isEqualTo((short) 0x0210);
                short length = buffer.getShort();
                assertThat(length).isEqualTo((short) 134);
                short usernameLength = buffer.getShort();
                assertThat(usernameLength).isEqualTo((short) 5);
                byte[] packetUsername = new byte[usernameLength];
                buffer.get(packetUsername, 0, packetUsername.length);
                assertThat(new String(packetUsername, UTF_8)).isEqualTo(username);

                byte[] iv = new byte[16];
                buffer.get(iv, 0, iv.length);
                byte[] encryptedPacket = new byte[buffer.remaining()];
                buffer.get(encryptedPacket);

                byte[] decryptedPacket = decrypt(iv, encryptedPacket);
                byte[] hash = new byte[20];
                System.arraycopy(decryptedPacket, 0, hash, 0, 20);
                byte[] rawData = new byte[decryptedPacket.length - 20];
                System.arraycopy(decryptedPacket, 20, rawData, 0, decryptedPacket.length - 20);
                assertThat(sha1(rawData)).isEqualTo(hash);

                verifyPacket(rawData);
                packetVerified.set(true);
            }

            private byte[] decrypt(byte[] iv, byte[] input) {
                try {
                    Cipher cipher = Cipher.getInstance("AES_256/OFB/NoPadding");
                    cipher.init(Cipher.DECRYPT_MODE,
                            new SecretKeySpec(sha256(password.getBytes(UTF_8)), "AES"),
                            new IvParameterSpec(iv));
                    return cipher.doFinal(input);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            private byte[] sha256(byte[] input) {
                try {
                    return MessageDigest.getInstance("SHA-256").digest(input);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            }

            private byte[] sha1(byte[] input) {
                try {
                    return MessageDigest.getInstance("SHA-1").digest(input);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        PacketWriter packetWriter = new PacketWriter(sender, username, password, SecurityLevel.ENCRYPT);
        packetWriter.write(metaData, 42);
        assertThat(packetVerified).isTrue();
    }

    private void verifyPacket(byte[] packetArr) {
        ByteBuffer packet = ByteBuffer.wrap(packetArr);

        short hostType = packet.getShort();
        assertThat(hostType).isEqualTo((short) 0);
        short hostLength = packet.getShort();
        assertThat(hostLength).isEqualTo((short) 28);
        byte[] host = new byte[hostLength - 5];
        packet.get(host, 0, host.length);
        assertThat(new String(host, UTF_8)).isEqualTo("nw-1.alpine.example.com");
        assertThat(packet.get()).isEqualTo((byte) 0);

        short timestampType = packet.getShort();
        assertThat(timestampType).isEqualTo((short) 1);
        short timestampLength = packet.getShort();
        assertThat(timestampLength).isEqualTo((short) 12);
        assertThat(packet.getLong()).isEqualTo(1520961345L);

        short typeType = packet.getShort();
        assertThat(typeType).isEqualTo((short) 4);
        short typeLength = packet.getShort();
        assertThat(typeLength).isEqualTo((short) 10);
        byte[] type = new byte[typeLength - 5];
        packet.get(type, 0, type.length);
        assertThat(new String(type, UTF_8)).isEqualTo("gauge");
        assertThat(packet.get()).isEqualTo((byte) 0);

        short typeInstanceType = packet.getShort();
        assertThat(typeInstanceType).isEqualTo((short) 5);
        short typeInstanceLength = packet.getShort();
        assertThat(typeInstanceLength).isEqualTo((short) 10);
        byte[] typeInstance = new byte[typeInstanceLength - 5];
        packet.get(typeInstance, 0, typeInstance.length);
        assertThat(new String(typeInstance, UTF_8)).isEqualTo("value");
        assertThat(packet.get()).isEqualTo((byte) 0);

        short periodType = packet.getShort();
        assertThat(periodType).isEqualTo((short) 7);
        short periodLength = packet.getShort();
        assertThat(periodLength).isEqualTo((short) 12);
        assertThat(packet.getLong()).isEqualTo(100);

        short valuesType = packet.getShort();
        assertThat(valuesType).isEqualTo((short) 6);
        short valuesLength = packet.getShort();
        assertThat(valuesLength).isEqualTo((short) 15);
        short amountOfValues = packet.getShort();
        assertThat(amountOfValues).isEqualTo((short) 1);
        byte dataType = packet.get();
        assertThat(dataType).isEqualTo((byte) 1);
        assertThat(packet.order(ByteOrder.LITTLE_ENDIAN).getDouble()).isEqualTo(42.0, offset(0.01));
    }

}