/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jp.semlab.triplex;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.Mention;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 *
 * @author gr0259sh
 */
public class CorefSolver {
    
    private StanfordCoreNLP pipeline;
    
    public CorefSolver(){
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,coref");
        this.pipeline = new StanfordCoreNLP(props);
    }
    

    /**
     * Group entity mentions by their clusterID
     * @param coreSentences the list of annotated sentences.
     * @return A map with mention's cluster id as key and a list of mentions as value
     */
    private Map<Integer, List<Mention>> groupMentions(List<CoreMap> coreSentences){
        ArrayList<Mention> mentionList = new ArrayList();
        for (CoreMap sentence : coreSentences) {
            for (Mention m : sentence.get(CorefCoreAnnotations.CorefMentionsAnnotation.class)) {
                mentionList.add(m);
            }
        }
        Map<Integer, List<Mention>> mentionsByClusterID = new HashMap();
        for (Mention m: mentionList){
            if(!mentionsByClusterID.containsKey(m.corefClusterID)){
                mentionsByClusterID.put(m.corefClusterID, new ArrayList());
            }
            mentionsByClusterID.get(m.corefClusterID).add(m);
        }
        return mentionsByClusterID;
    }
    
    
    /**
     * Reconstruct the solve text as a string, from the solved sentences
     * and the annotated sentence.
     * @param coreSentences annotated sentence
     * @param solvedSentences sentences with mentions replaced by the representative mention.
     * @return text with solved co-references.
     */
    private String reconstructText(List<CoreMap> coreSentences, 
            Map<Integer, List<String>> solvedSentences){
        List<String> sentences = new ArrayList();
        for (CoreMap sentence: coreSentences){
            var sentIndex = sentence.get(CoreAnnotations.SentenceIndexAnnotation.class);
            if(solvedSentences.containsKey(sentIndex)){
                List<String> sentWords = solvedSentences.get(sentIndex);
                sentences.add(String.join(" ", sentWords));
            } else{
                sentences.add(sentence.get(CoreAnnotations.TextAnnotation.class).toString());
            }
        }
        String solvedText = String.join(" ", sentences);
        return String.join(" ", solvedText);
    }
    
    
    /**
     * Replace each entity mentions with their most representative one.
     * @param text text string to be solved.
     * @return solved text string.
     */
    public String solveCoref(String text){
        Annotation document = new Annotation(text);
        try{
            pipeline.annotate(document);
        } catch(OutOfMemoryError ex){
            System.out.println("\n\tSkipping Coreference(Annotation Out of Memory)");
            return text;
        }
        
        
        List<CoreMap> coreSentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        Map<Integer, List<Mention>> mentionsByClusterID = groupMentions(coreSentences);
        Map<Integer, List<String>> solvedSentences = new HashMap();
        mentionsByClusterID.entrySet().forEach(entry -> {
            // TODO  turn this sections in a method name replaceMentions()
            List<Mention> mentions = entry.getValue();
            if(mentions.size() > 1){
                Mention repMention = mentions.get(0);
                for(Mention m: mentions){ // get the more representative mention
                    if(m.equals(repMention)) continue;
                    if(m.moreRepresentativeThan(repMention)){
                        repMention = m;
                    }
                }
                for(Mention m: mentions){ // replace all mention different from the representative
                    if(m.equals(repMention)) continue;
                    List<String> sentence = solvedSentences.containsKey(m.sentNum) ? 
                            solvedSentences.get(m.sentNum) : 
                            m.sentenceWords.stream().map(word -> word.value()).collect(Collectors.toList()); //coreSentences.get(m.sentNum);
                    List<String> solvedSentence = new ArrayList();
                    for (int i=0; i< sentence.size(); i++){ // sentence words length can change. Use COre map or core label?
                        if (i == m.startIndex){
                            solvedSentence.add(repMention.toString());
                        } else if(i < m.startIndex || m.endIndex <= i){
                            solvedSentence.add(m.sentenceWords.get(i).value());
                        } else {
                            solvedSentence.add("");
                        }
                    }
                    solvedSentences.put(m.sentNum, solvedSentence);
                }
            }
        });//;
        String solvedText = reconstructText(coreSentences, solvedSentences);
        return solvedText;
    }
    
}
