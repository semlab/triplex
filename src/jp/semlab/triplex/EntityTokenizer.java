/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.semlab.triplex;

import edu.stanford.nlp.pipeline.CoreDocument;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author gr0259sh
 */
public class EntityTokenizer {
		
    private List<String> tokens;


    /**
     * Give the list of tokens with lowered case, matching a regular expression
     * @param regex the regular expresion to match
     * @return list of tokens lowered case matching the regex
     */
    public List<String> getLowerCaseTokens(String regex){
        return this.tokens.stream().map(String::toLowerCase)
                .filter(t -> t.matches(regex))
                .collect(Collectors.toList());
    }


    public List<String> getLowerCaseTokens(){
        return this.tokens.stream().map(String::toLowerCase)
                .collect(Collectors.toList());
    }


    public List<String> getTokens(){
        return this.tokens;
    }


    public List<String> getTokens(String regex){
        return this.tokens.stream()
                .filter(t ->t.matches(regex))
                .collect(Collectors.toList());
    }


    public List<String> tokenize(CoreDocument doc){
        var tkns = doc.tokens();
        this.tokens = new ArrayList<>();
        int i = 0;
        while(i < tkns.size()){
            StringBuilder tkBuilder = new StringBuilder();
            //tkBuilder.append(tokens.get(i).word().toLowerCase());
            tkBuilder.append(tkns.get(i).word());
            int j = 1;
            while(i +j < tkns.size() && !tkns.get(i).ner().equals("O")  
                    && tkns.get(i).ner().equals(tkns.get(i + j).ner())){
                tkBuilder.append(" ");
                //tkBuilder.append(tkns.get(i+j).word().toLowerCase());
                tkBuilder.append(tkns.get(i+j).word());
                j++;
            }
            this.tokens.add(tkBuilder.toString());
            i+= j;
        }
        return this.tokens;
    }


    public String toCSV(){
        var tokensList = this.tokens.stream()
                .map(String::toLowerCase).collect(Collectors.toList());
        String csvStr = String.join(",", tokensList);
        return csvStr;
    }

}
