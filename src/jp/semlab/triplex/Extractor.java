/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.semlab.triplex;

import java.util.Properties;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author gr0259sh
 */
public class Extractor {

    private FileWriter writer;
    private StanfordCoreNLP pipeline;

    private List<String> entitiesType;

    private List<String> tokens;
    private List<Extraction> extractions;

    private EntityTokenizer retokenizer;

    public Extractor(Map entityRules){
        Properties props = new Properties();
        init(entityRules, props);
    }

    public Extractor(Properties props){
        Map entityRules = new HashMap();
        init(entityRules, props);
    }

    public Extractor(){
        Properties props = new Properties();
        Map entityRules = new HashMap();
        init(entityRules, props);
    }

    public Extractor(Map entityRules, Properties additionalProps){
        init(entityRules, additionalProps);
    }


    private void init(Map entityRules, Properties additionalProps){
        Properties props = new Properties();
        props.setProperty("annotators", 
                    "tokenize,ssplit,pos,lemma,depparse,ner,natlog,openie");
        this.entitiesType = new ArrayList<String>();
        this.entitiesType.add("PERSON");
        this.entitiesType.add("LOCATION");
        this.entitiesType.add("ORGANIZATION");
        // add additional rules, customize TokensRegexNER annotator
        additionalProps.keySet().forEach(key -> {
            props.setProperty((String) key, additionalProps.getProperty((String) key));
        });
        entityRules.keySet().forEach(key ->{
            this.entitiesType.add((String) key);
            props.setProperty("ner.additional.regexner.mapping", 
                    (String) entityRules.get(key));
        });
        this.pipeline = new StanfordCoreNLP(props);

        this.retokenizer = new EntityTokenizer();
    }


    public List<String> getEntitiesType(){
        return this.entitiesType;
    }

    /**
     * 
     * @return tokens of previously text on which the extraction ran. entities
     * are considered as one token
     */
    public List<String> getTokens(){
        return this.tokens;
    }

    public List<Extraction> getExtractions(){
        return this.extractions;
    }


    public List<Extraction> extract(String docText){
        return extract(docText, null, null);
    }


    public List<Extraction> extract(String docText, String docId, String docDate){
        String text = preprocess(docText);
        ArrayList<Extraction> extractions = new ArrayList<Extraction>();
        Annotation annotation = new Annotation(text);
        CoreDocument doc = new CoreDocument(annotation);
        this.pipeline.annotate(annotation);
        this.pipeline.annotate(doc);

        for (CoreMap sentence: annotation.get(CoreAnnotations.SentencesAnnotation.class)){
            Collection<RelationTriple> triples = 
                    sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);
            for (RelationTriple triple: triples){
                Extraction extraction = new Extraction();
                extraction.setSentence(sentence.toString().replace("\"", "\"\""));
                extraction.setSubject(triple.subjectLemmaGloss());
                extraction.setRelation(triple.relationLemmaGloss());
                extraction.setObject(triple.objectLemmaGloss());
                for (CoreEntityMention em: doc.entityMentions()){
                    if (triple.subjectLemmaGloss().contains(em.text()) 
                                && this.entitiesType.contains(em.entityType())){
                        extraction.setSubjEnt(em.text());
                        extraction.setSubjEntType(em.entityType());
                    }
                    if(triple.objectLemmaGloss().contains(em.text())
                                && this.entitiesType.contains(em.entityType())){
                        extraction.setObjEnt(em.text());
                        extraction.setObjEntType(em.entityType());
                    }
                }
                boolean hasEntities = (extraction.getSubjEnt() != null && extraction.getObjEnt() != null);
                boolean isReflexive = (hasEntities && extraction.getSubjEnt().equals(extraction.getObjEnt()));
                boolean isDistinct = hasEntities && !extractions.contains(extraction);
                if(triple.confidence == 1 && !isReflexive && isDistinct ){
                    extractions.add(extraction);
                }
            }
        }
        this.extractions = extractions;
        this.tokens = this.retokenizer.tokenize(doc);
        return this.extractions;
    }


    public String preprocess(String text){
            // TODO any eventual preproc
        return text;
    }


}
