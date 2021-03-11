package fr.adbonnin.issue.dto;

import static fr.adbonnin.issue.utils.ArrayUtil.EMPTY_STRING_ARRAY;

public class ConfigureDTO {

    private String[] cipherSuites = EMPTY_STRING_ARRAY;

    private String[] protocols = EMPTY_STRING_ARRAY;

    public String[] getCipherSuites() {
        return cipherSuites;
    }

    public void setCipherSuites(String[] cipherSuites) {
        this.cipherSuites = cipherSuites;
    }

    public String[] getProtocols() {
        return protocols;
    }

    public void setProtocols(String[] protocols) {
        this.protocols = protocols;
    }
}
