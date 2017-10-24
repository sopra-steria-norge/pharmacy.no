package no.pharmacy.infrastructure.dsig;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.Date;

import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

public class CertificateTest {

    public static void main(String[] args) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(512);
        KeyPair caKeyPair = keyPairGenerator.generateKeyPair();

        X509CertImpl cacert = createCertificate(caKeyPair.getPublic(), "CN=Test CA, O=JOHANNES PKI, C=NO", "CN=Test CA, O=JOHANNES PKI, C=NO");
        cacert.sign(caKeyPair.getPrivate(), "SHA1withRSA");
        cacert.verify(cacert.getPublicKey());
        System.out.println(cacert);

        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        X509CertImpl cert = createCertificate(keyPair.getPublic(), "CN=Johannes Test User, O=SOMECORP, C=NO", cacert.getSubjectDN().toString());
        cert.sign(caKeyPair.getPrivate(), "SHA1withRSA");
        cert.verify(cacert.getPublicKey());
        System.out.println(cert);

    }

    private static X509CertImpl createCertificate(PublicKey publicKey, String subjectDN, String issuerDN) throws CertificateException, IOException {
        X509CertInfo info = new sun.security.x509.X509CertInfo();
        info.set(X509CertInfo.VALIDITY, new CertificateValidity(new Date(), new Date(System.currentTimeMillis() + 3600 * 24 * 364)));
        info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(new BigInteger(64, new SecureRandom())));
        info.set(X509CertInfo.SUBJECT, new X500Name(subjectDN));
        info.set(X509CertInfo.ISSUER, new X500Name(issuerDN));
        info.set(X509CertInfo.KEY, new CertificateX509Key(publicKey));
        info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
        info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid)));
        return new X509CertImpl(info);
    }

}
