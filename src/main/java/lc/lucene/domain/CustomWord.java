package lc.lucene.domain;

import java.util.List;

public class CustomWord {

    public static final String type = "custom_word";

    public static final String mapping = "{\n" +
            "  \"custom_word\": {\n" +
            "    \"_all\": {\n" +
            "      \"enabled\": false\n" +
            "    },\n" +
            "    \"properties\": {\n" +
            "      \"word\": {\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"synonyms\": {\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"wordAttributes\": {\n" +
            "        \"type\": \"object\",\n" +
            "        \"properties\": {\n" +
            "          \"nature\": {\n" +
            "            \"type\": \"keyword\"\n" +
            "          },\n" +
            "          \"frequency\": {\n" +
            "            \"type\": \"integer\"\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private String word;

    private List<String> synonyms;

    private List<CustomWordAttribute> wordAttributes;

    public CustomWord(String word, List<CustomWordAttribute> wordAttributes) {
        this(word, wordAttributes, null);
    }

    public CustomWord(String word, List<CustomWordAttribute> wordAttributes, List<String> synonyms) {
        this.word = word;
        this.wordAttributes = wordAttributes;
        this.synonyms = synonyms;
    }

    public String getWord() {
        return word;
    }

    public List<CustomWordAttribute> getWordAttributes() {
        return wordAttributes;
    }

    public List<String> getSynonyms() {
        return synonyms;
    }

    public String getWordAttributeAsString() {
        String wordAttr = "";
        if (wordAttributes != null) {
            StringBuilder wordBuilder = new StringBuilder();
            for (CustomWordAttribute wordAttribute : wordAttributes) {
                if (wordAttribute.getNature() == null) {
                    continue;
                }

                String frequency = wordAttribute.getFrequency() <= 0 ? "1" : String.valueOf(wordAttribute.getFrequency());
                wordBuilder.append(wordAttribute.getNature()).append(" ").append(frequency).append(" ");
            }
            if (wordBuilder.length() > 0) {
                wordBuilder.deleteCharAt(wordBuilder.length() - 1);
            }
            wordAttr = wordBuilder.toString();
        }
        return wordAttr;
    }

    @Override
    public String toString() {
        StringBuilder wordDesc = new StringBuilder();

        wordDesc.append(word).append(" ");
        if (wordAttributes != null) {
            wordDesc.append(wordAttributes.toString());
        }

        if (synonyms != null) {
            wordDesc.append("/").append(synonyms.toString());
        }
        return wordDesc.toString();
    }
}
