package no.pharmacy.infrastructure.dsig;

import java.io.File;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;
import org.eaxy.Element;
import org.eaxy.ElementPath;
import org.eaxy.Namespace;
import org.eaxy.Xml;
import org.eaxy.XmlFormatter;
import org.jcp.xml.dsig.internal.DigesterOutputStream;
import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

public class SignatureEaxyTest {


    private static Namespace XMLDSIG = new Namespace("http://www.w3.org/2000/09/xmldsig#");

    public static void main(String[] args) throws Exception {
        signEreseptEaxy();
    }

    private static void signEreseptEaxy() throws Exception {

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(512);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        X509CertInfo info = new sun.security.x509.X509CertInfo();
        info.set(X509CertInfo.VALIDITY, new CertificateValidity(new Date(), new Date(System.currentTimeMillis() + 3600 * 24 * 364)));
        info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(new BigInteger(64, new SecureRandom())));
        info.set(X509CertInfo.SUBJECT, new X500Name("SERIALNUMBER=965336796, CN=JOHANNES!!!!, OU=Vitusapotek Valdres-971787066, O=NORSK MEDISINALDEPOT AS, C=NO"));
        info.set(X509CertInfo.ISSUER, new X500Name("CN=Buypass Class 3 Test4 CA 3, O=Buypass AS-983163327, C=NO"));
        info.set(X509CertInfo.KEY, new CertificateX509Key(keyPair.getPublic()));
        info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
        info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(new AlgorithmId(AlgorithmId.sha1WithRSAEncryption_oid)));

        X509CertImpl certificate = new X509CertImpl(info);
        certificate.sign(keyPair.getPrivate(), "SHA1withRSA");

        org.eaxy.Document doc = Xml.read(new File("src/test/resources/eResept/M10-unsigned.xml"));

        Element signedInfo = XMLDSIG.el("SignedInfo",
                XMLDSIG.el("CanonicalizationMethod").attr("Algorithm", "http://www.w3.org/TR/2001/REC-xml-c14n-20010315"),
                XMLDSIG.el("SignatureMethod").attr("Algorithm", "http://www.w3.org/2000/09/xmldsig#rsa-sha1"));

        Element transforms = XMLDSIG.el("Transforms",
                XMLDSIG.el("Transform").attr("Algorithm", "http://www.w3.org/2000/09/xmldsig#enveloped-signature"),
                XMLDSIG.el("Transform").attr("Algorithm", "http://www.w3.org/TR/2001/REC-xml-c14n-20010315")
                );
        String digestMethod = "http://www.w3.org/2000/09/xmldsig#sha1";
        Element signatureInput = null;
        for (Element transform : transforms.elements()) {
            if (transform.attr("Algorithm").equals("http://www.w3.org/2000/09/xmldsig#enveloped-signature")) {
                signatureInput = doc.getRootElement();
            } else if (transform.attr("Algorithm").equals("http://www.w3.org/TR/2001/REC-xml-c14n-20010315")) {
                MessageDigest md = MessageDigest.getInstance(getDigestMethod(digestMethod));
                DigesterOutputStream output = new DigesterOutputStream(md, true);

                OutputStreamWriter writer = new OutputStreamWriter(output);
                XmlFormatter.canonical(transform.attr("Algorithm")).format(writer, new ElementPath(null, signatureInput));
                writer.flush();
                signatureInput = null;
                signedInfo.add(XMLDSIG.el("Reference",
                        transforms,
                        XMLDSIG.el("DigestMethod").attr("Algorithm", digestMethod),
                        XMLDSIG.el("DigestValue", Base64.getMimeEncoder().encodeToString(md.digest()))));
            } else {
                throw new IllegalArgumentException();
            }
        }

        Element signatureValue = XMLDSIG.el("SignatureValue");
        Element signature = XMLDSIG.el("Signature",
                signedInfo,
                signatureValue,
                XMLDSIG.el("KeyInfo", XMLDSIG.el("X509Data",
                        XMLDSIG.el("X509Certificate",
                                Base64.getMimeEncoder().encodeToString(certificate.getEncoded())))));

        doc.getRootElement().add(signature);

        byte[] digestValue = XmlFormatter.canonical(signedInfo.find(XMLDSIG.name("CanonicalizationMethod")).first().attr("Algorithm"))
                .toXML(doc.find(XMLDSIG.name("Signature"), XMLDSIG.name("SignedInfo")).firstPath()).getBytes();

        Signature signatureVal = Signature.getInstance("SHA1withRSA");
        signatureVal.initSign(keyPair.getPrivate());
        signatureVal.update(digestValue);
        signatureValue.text(Base64.getMimeEncoder().encodeToString(signatureVal.sign()));

        System.out.println(doc.toXML());

    }



    private static void validateEreseptEaxy() throws Exception {
        org.eaxy.Document doc = Xml.read(new File("src/test/resources/eResept/M10-resigned2.xml"));

        Element sig = doc.find(XMLDSIG.name("Signature")).first();

        byte[] digestValue = XmlFormatter.canonical(sig.find(XMLDSIG.name("SignedInfo"), XMLDSIG.name("CanonicalizationMethod")).first().attr("Algorithm"))
                .toXML(doc.find(XMLDSIG.name("Signature"), XMLDSIG.name("SignedInfo")).firstPath()).getBytes();


        Signature signatureCheck = Signature.getInstance("SHA1withRSA");
        X509Certificate cert = new X509CertImpl(Base64.getMimeDecoder().decode(sig.find(XMLDSIG.name("KeyInfo"), XMLDSIG.name("X509Data"), XMLDSIG.name("X509Certificate")).first().text()));

        cert.verify(cert.getPublicKey());

        PublicKey publicKey = cert.getPublicKey();
        signatureCheck.initVerify(publicKey);
        signatureCheck.update(digestValue);
        System.out.println(signatureCheck.verify(Base64.getMimeDecoder().decode(sig.find(XMLDSIG.name("SignatureValue")).first().text())));

        for (Element reference : sig.find(XMLDSIG.name("SignedInfo"), XMLDSIG.name("Reference"))) {
            Element signatureInput = doc.getRootElement().copy();
            System.out.println(signatureInput.toXML());
            for (Element transform : reference.find(XMLDSIG.name("Transforms"), XMLDSIG.name("Transform"))) {
                if (transform.attr("Algorithm").equals("http://www.w3.org/2000/09/xmldsig#enveloped-signature")) {
                    Element tmp = signatureInput.copyElement();
                    for (org.eaxy.Node node : signatureInput.children()) {
                        if (node instanceof Element) {
                            if (((Element)node).getName().equals(XMLDSIG.name("Signature"))) {
                                continue;
                            }
                        }
                        tmp.add(node);
                    }
                    signatureInput = tmp;
                } else if (transform.attr("Algorithm").equals("http://www.w3.org/TR/2001/REC-xml-c14n-20010315")) {
                    MessageDigest md = MessageDigest.getInstance(getDigestMethod(reference.find(XMLDSIG.name("DigestMethod")).first().attr("Algorithm")));
                    DigesterOutputStream output = new DigesterOutputStream(md, true);

                    OutputStreamWriter writer = new OutputStreamWriter(output);
                    XmlFormatter.canonical(transform.attr("Algorithm")).format(writer, new ElementPath(null, signatureInput));
                    writer.flush();
                    System.out.println(XmlFormatter.canonical(transform.attr("Algorithm")).toXML(new ElementPath(null, signatureInput)));
                    System.out.println(Base64.getMimeEncoder().encodeToString(md.digest()));
                    signatureInput = null;
                } else {
                    throw new IllegalArgumentException();
                }
            }
            System.out.println(reference.find("DigestValue").first().text());
        }
    }

    private static String getDigestMethod(String xmlAlgorithm) {
        if (xmlAlgorithm.equals("http://www.w3.org/2000/09/xmldsig#sha1")) {
            return "SHA1";
        } else if (xmlAlgorithm.equals("http://www.w3.org/2000/09/xmldsig#rsa-sha1")) {
            return "SHA1";
        } else {
            throw new UnsupportedOperationException(xmlAlgorithm);
        }
    }

}
