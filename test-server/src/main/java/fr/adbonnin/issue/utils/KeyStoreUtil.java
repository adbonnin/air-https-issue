package fr.adbonnin.issue.utils;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

public class KeyStoreUtil {
    public static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static final Provider CRYPTO_PROVIDER = new BouncyCastleProvider();
    public static final String CRYPTO_PROVIDER_ID = "BC";

    public static final String CERTIFICATE_COMMON_NAME = "Air Https Issue";
    public static final String CERTIFICATE_ORGANIZATIONAL_UNIT = "JVM";
    public static final String CERTIFICATE_ORGANIZATION = "adbonnin.fr";
    public static final String CERTIFICATE_IDENTIFIER = "selfsigned";

    public static final String CERTIFICATE_KEYPAIR_ALGORITHM = "RSA";
    public static final int CERTIFICATE_KEYPAIR_SIZE = 2048;

    private static AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    static {
        if (!INITIALIZED.get()) {
            if (Security.getProvider(CRYPTO_PROVIDER_ID) == null) {
                Security.addProvider(CRYPTO_PROVIDER);
            }

            INITIALIZED.set(true);
        }
    }

    public static void updateWithSelfSignedServerCertificate(KeyStore keyStore) throws GeneralSecurityException {
        final KeyPair keyPair = generateRsaKeyPair();
        final X509Certificate certificate = generateSelfSignedCertificate(keyPair);
        keyStore.setKeyEntry(CERTIFICATE_IDENTIFIER, keyPair.getPrivate(), new char[0], new Certificate[]{certificate});
    }

    public static KeyPair generateRsaKeyPair() throws GeneralSecurityException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(CERTIFICATE_KEYPAIR_ALGORITHM, CRYPTO_PROVIDER_ID);
        keyGen.initialize(CERTIFICATE_KEYPAIR_SIZE, SECURE_RANDOM);
        return keyGen.generateKeyPair();
    }

    public static X509Certificate generateSelfSignedCertificate(KeyPair keyPair) throws GeneralSecurityException {
        final X500Name name = new X500NameBuilder(BCStyle.INSTANCE)
                .addRDN(BCStyle.CN, CERTIFICATE_COMMON_NAME)
                .addRDN(BCStyle.OU, CERTIFICATE_ORGANIZATIONAL_UNIT)
                .addRDN(BCStyle.O, CERTIFICATE_ORGANIZATION)
                .build();

        final long now = System.currentTimeMillis();
        final Date notBefore = new Date(now - DateUtil.MILLIS_PER_DAY);
        final Date notAfter = new Date(now + (5 * 365 * DateUtil.MILLIS_PER_DAY));
        final BigInteger serial = BigInteger.valueOf(now);

        final X509v3CertificateBuilder certificateGenerator = new JcaX509v3CertificateBuilder(name, serial, notBefore, notAfter, name, keyPair.getPublic());

        final ContentSigner signatureGenerator;
        try {
            signatureGenerator = new JcaContentSignerBuilder("SHA256WithRSAEncryption")
                    .setProvider(CRYPTO_PROVIDER)
                    .build(keyPair.getPrivate());
        }
        catch (OperatorCreationException e) {
            throw new GeneralSecurityException(e);
        }

        final X509Certificate certificate = new JcaX509CertificateConverter()
                .setProvider(CRYPTO_PROVIDER)
                .getCertificate(certificateGenerator.build(signatureGenerator));

        certificate.checkValidity(new Date());
        certificate.verify(certificate.getPublicKey());
        return certificate;
    }
}
