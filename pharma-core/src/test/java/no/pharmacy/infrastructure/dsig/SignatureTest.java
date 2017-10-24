package no.pharmacy.infrastructure.dsig;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.KeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

public class SignatureTest {

    private static class DOMX509KeySelector extends KeySelector {

        @Override
        public KeySelectorResult select(KeyInfo keyInfo, Purpose purpose, AlgorithmMethod method,
                XMLCryptoContext context) throws KeySelectorException {

            for (Object objStructure : keyInfo.getContent()) {
                X509Data keyValue = (X509Data) objStructure;
                for (Object object : keyValue.getContent()) {
                    X509Certificate cert = (X509Certificate) object;
                    System.out.println(cert.getSubjectDN());
                    return new KeySelectorResult() {

                        @Override
                        public Key getKey() {
                            return cert.getPublicKey();
                        }
                    };
                }
            }
            throw new KeySelectorException("Key not found");
        }

    }

    public static void main(String[] args) throws Exception {
        signEresept();
        validateEresept();
    }

    private static void signEresept() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, KeyException, TransformerConfigurationException, ParserConfigurationException, SAXException, IOException, MarshalException, XMLSignatureException, TransformerException, TransformerFactoryConfigurationError, CertificateException, NoSuchProviderException, SignatureException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document document = builder.parse(new File("src/test/resources/eResept/M10-unsigned.xml"));

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(512);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        //CertAndKeyGen certGen = new CertAndKeyGen("RSA", "SHA256WithRSA", null);
        X509CertInfo info = new sun.security.x509.X509CertInfo();
        info.set(X509CertInfo.VALIDITY, new CertificateValidity(new Date(), new Date(System.currentTimeMillis() + 3600 * 24 * 364)));
        info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(new BigInteger(64, new SecureRandom())));
        info.set(X509CertInfo.SUBJECT, new X500Name("SERIALNUMBER=965336796, CN=JOHANNES!!!!, OU=Vitusapotek Valdres-971787066, O=NORSK MEDISINALDEPOT AS, C=NO"));
        info.set(X509CertInfo.ISSUER, new X500Name("CN=Buypass Class 3 Test4 CA 3, O=Buypass AS-983163327, C=NO"));
        info.set(X509CertInfo.KEY, new CertificateX509Key(keyPair.getPublic()));
        info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
        info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid)));

        X509CertImpl certificate = new X509CertImpl(info);
        certificate.sign(keyPair.getPrivate(), "SHA1withRSA");
        AlgorithmId algo = (AlgorithmId)certificate.get(X509CertImpl.SIG_ALG);
        info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algo);
        certificate = new X509CertImpl(info);
        certificate.sign(keyPair.getPrivate(), "SHA1withRSA");

        DOMSignContext signContext = new DOMSignContext(keyPair.getPrivate(), document.getDocumentElement());

        XMLSignatureFactory signatureFactory = XMLSignatureFactory.getInstance("DOM");
        Reference newReference = signatureFactory.newReference("",
                signatureFactory.newDigestMethod(DigestMethod.SHA1, null),
                Arrays.asList(
                        signatureFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec)null),
                        signatureFactory.newTransform("http://www.w3.org/TR/2001/REC-xml-c14n-20010315", (TransformParameterSpec)null)
                        ),
                null, null);
        SignedInfo signedInfo = signatureFactory.newSignedInfo(
                signatureFactory.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (CanonicalizationMethod)null),
                signatureFactory.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
                Collections.singletonList(newReference));

        X509Data x509Data = signatureFactory.getKeyInfoFactory().newX509Data(Arrays.asList(certificate));
        KeyInfo keyInfo = signatureFactory.getKeyInfoFactory().newKeyInfo(Arrays.asList(x509Data));

        XMLSignature signature = signatureFactory.newXMLSignature(signedInfo, keyInfo);
        signature.sign(signContext);

        TransformerFactory.newInstance().newTransformer().transform(new DOMSource(document), new StreamResult(System.out));
    }

    private static void validateEresept() throws ParserConfigurationException, SAXException, IOException, MarshalException, XMLSignatureException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document document = builder.parse(new File("src/test/resources/eResept/M10-resigned.xml"));

        Node signature = document.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature").item(0);

        DOMValidateContext validateContext = new DOMValidateContext(new DOMX509KeySelector(), signature);
        XMLSignature xmlSignature = XMLSignatureFactory.getInstance("DOM").unmarshalXMLSignature(validateContext);

        System.out.println("eResept Signature validation: " + xmlSignature.getSignatureValue().validate(validateContext));
        System.out.println("Full validation: " + xmlSignature.validate(validateContext));

        for (Object object : xmlSignature.getSignedInfo().getReferences()) {
            Reference ref = (Reference) object;
            System.out.println(ref.getId() + ": " + ref.validate(validateContext));
        }
    }
}
