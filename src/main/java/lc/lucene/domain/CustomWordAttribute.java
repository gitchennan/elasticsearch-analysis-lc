package lc.lucene.domain;

public class CustomWordAttribute {

    private String nature;

    private int frequency;

    public CustomWordAttribute(String nature, int frequency) {
        this.nature = nature;
        this.frequency = frequency;
    }

    public String getNature() {
        return nature;
    }

    public int getFrequency() {
        return frequency;
    }

    @Override
    public String toString() {
        return nature + " " + frequency;
    }
}
