package no.pharmacy.infrastructure.messages;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.Base64;

import org.eaxy.Element;
import org.eaxy.ElementPath;
import org.eaxy.Namespace;
import org.eaxy.Node;
import org.eaxy.XmlFormatter;
import org.jcp.xml.dsig.internal.DigesterOutputStream;

import lombok.SneakyThrows;
import no.pharmacy.infrastructure.CryptoUtil;

public class EbXmlMessage {

    private static final String ENVELOPED_SIGNATURE = "http://www.w3.org/2000/09/xmldsig#enveloped-signature";

    private static final String C14N_WITHOUT_COMMENTS = "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";

    private static Namespace XMLDSIG = new Namespace("http://www.w3.org/2000/09/xmldsig#");

    private final Element message;

    public EbXmlMessage(Element message) {
        this.message = message;
    }

    public void verifySignature() {
        verifyReferences();
        verifySignatureValue();
    }

    private void verifySignatureValue() {
        // TODO Auto-generated method stub

    }

    @SneakyThrows({IOException.class, GeneralSecurityException.class})
    private void verifyReferences() {
        for (Element reference : message.find(XMLDSIG.name("Signature"), XMLDSIG.name("SignedInfo"), XMLDSIG.name("Reference")).check()) {
            Element signatureInput = message.copy();
            for (Element transform : reference.find(XMLDSIG.name("Transforms"), XMLDSIG.name("Transform"))) {
                String algo = transform.attr("Algorithm");
                if (algo.equals(ENVELOPED_SIGNATURE)) {
                    Element tmp = signatureInput.copyElement();
                    for (Node node : signatureInput.children()) {
                        if (!XMLDSIG.name("Signature").matches(node)) {
                            tmp.add(node);
                        }
                    }
                    signatureInput = tmp;
                } else if (algo.equals(C14N_WITHOUT_COMMENTS)) {
                    MessageDigest md = MessageDigest.getInstance(getDigestMethod(reference.find(XMLDSIG.name("DigestMethod")).first().attr("Algorithm")));
                    try(OutputStreamWriter writer = new OutputStreamWriter(new DigesterOutputStream(md))) {
                        XmlFormatter.canonical(algo).format(writer, new ElementPath(null, signatureInput));
                        writer.flush();
                    }
                    String calculatedValue = Base64.getMimeEncoder().encodeToString(md.digest());
                    String providedValue = reference.find(XMLDSIG.name("DigestValue")).first().text();
                    if (!calculatedValue.equals(providedValue)) {
                        throw new IllegalArgumentException("Failed to validate message [" + calculatedValue + "] vs [" + providedValue + "]");
                    }
                } else {
                    throw new UnsupportedOperationException("Unsupported Algorithm " + algo);
                }
            }
        }
    }

    public Element getElement() {
        return message;
    }

    public Element getContent() {
        return message.find("Document", "RefDoc", "Content").first();
    }

    public X509Certificate getCertificate() {
        return CryptoUtil.decodeCertificate(message.find("Signature", "KeyInfo", "X509Data", "X509Certificate").first().text());
    }

    public static String getDigestMethod(String xmlAlgorithm) {
        if (xmlAlgorithm.equals("http://www.w3.org/2000/09/xmldsig#sha1")) {
            return "SHA1";
        } else if (xmlAlgorithm.equals("http://www.w3.org/2000/09/xmldsig#rsa-sha1")) {
            return "SHA1";
        } else {
            throw new UnsupportedOperationException("Unsupported digest method " + xmlAlgorithm);
        }
    }

}
