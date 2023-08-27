/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.semlab.triplex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author gr0259sh
 */
public class Extraction {

	
    private long id;
    private String articleId;
    private String sentence;
    private String subject;
    private String relation;
    private String object;
    private String subjEnt;
    private String subjEntType;
    private String objEnt;
    private String objEntType;
    private String sentenceTokens[];
    private String subjectTokens[];
    private String relationTokens[];
    private String objectTokens[];
    
    private Boolean isLink = null;
    private final ArrayList<String> pronouns = new ArrayList<>(Arrays.asList(
                "he", "she", "it", "they"));
    
    public Extraction(){}
    
    
    public Extraction(long id, String sentence, String subject, 
            String relation, String object, String subjEnt, String subjEntType,
            String objEnt, String objEntType){
        this.id = id;
        this.sentence = sentence;
        this.subject = subject;
        this.relation = relation;
        this.object = object;
        this.subjEnt = subjEnt;
        this.subjEntType = subjEntType;
        this.objEnt = objEnt;
        this.objEntType = objEntType;
        
    }

    public long getId(){
        return this.id;
    }


    public void setId(long id){
        this.id = id;
    }
    
    public String getArticleId(){
        return this.articleId;
    }
    
    public void setArticleId(String articleId){
        this.articleId = articleId;
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getSubjEnt() {
        return subjEnt;
    }

    public void setSubjEnt(String subjEnt) {
        this.subjEnt = subjEnt;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relationText) {
        this.relation= relationText;
    }

    public String getObjEnt() {
        return objEnt;
    }

    public void setObjEnt(String objEnt) {
        this.objEnt = objEnt;
    }

    public String getSubjEntType() {
        return subjEntType;
    }

    public void setSubjEntType(String subjEntType) {
        this.subjEntType = subjEntType;
    }

    public String getObjEntType() {
        return objEntType;
    }

    public void setObjEntType(String objEntType) {
        this.objEntType = objEntType;
    }


    public String[] getSentenceTokens(){
        return  this.sentenceTokens;
    }

    public void setSentenceTokens(String sentenceTokens[]){
        this.sentenceTokens = sentenceTokens;
    }

    public String[] getSubjectTokens(){
        return this.subjectTokens;
    }

    public void setSubjectTokens(String subjectTokens[]){
        this.subjectTokens = subjectTokens;
    }


    public String[] getRelationTokens(){
        return this.relationTokens;
    }

    public void setRelationTokens(String relationTokens[]){
        this.relationTokens = relationTokens;
    }

    public String[] getObjecTokens(){
        return this.objectTokens;
    }

    public void setObjecTokens(String objectTokens[]){
        this.objectTokens = objectTokens;
    }
    
    /**
     * Verify if the extraction subject and object are of the same entity type.
     * @return true if the subject and object entity type are the same false else.
     */
    public boolean hasSubjObjSameType(){
        return this.subjEntType.equals(this.objEntType);
    }
    
    /**
     * Verify if the subject and object of the extraction contains only named
     * entities
     * @return true if subject and object contains only named entities.
     */
    public boolean hasEntitiesOnly(){
        return (subject.equals(subjEnt) && object.equals(objEnt));
    }
    
    
    /**
     * Check if the subject or the object is a pronoun.
     * @return 
     */
    public boolean hasPronouns(){
        return pronouns.contains(subject) || pronouns.contains(object);
    }
    
    
    /**
     * is this extraction an entity link
     * @return 
     */
    public boolean isEntityLink(){
        if (this.isLink == null) {
            this.isLink = relation.equals("be") && hasSubjObjSameType()
                && !hasPronouns() && hasEntitiesOnly() ; 
        }
        return this.isLink;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null) return false;
        if (this.getClass() != o.getClass()) return false;
        Extraction ex = (Extraction) o;
            boolean equal = this.subjEnt.equals(ex.getSubjEnt())
                    && this.objEnt.equals(ex.getObjEnt())
                    && this.relation.equals(ex.getRelation());
            return equal;
    }

    public String toCSV(String separator){
        return "\"" 
            + String.join("\"" + separator + "\"", 
                    Arrays.asList(
                            this.articleId,
                            this.sentence,
                            this.subject,
                            this.relation,
                            this.object,
                            this.subjEnt,
                            this.subjEntType,
                            this.objEnt,
                            this.objEntType)) 
            + "\"";
    }

    public String toCSV(){
        return toCSV(",");	
    }
    
    public String toJSON(){
        return "{"
            + "\"articleId\": \"" + articleId + "\"\n"
            + "\"sentence\": \"" + sentence + "\"\n"
            + "\"subject\": \"" + subject + "\"\n"
            + "\"relation\": \"" + relation + "\"\n"
            + "\"object\": \""  + object + "\"\n"
            + "\"subjEnt\": \"" + subjEnt +"\"\n"
            + "\"subjEntType\": \"" + subjEntType + "\"\n"
            + "\"objEnt\": \"" +  objEnt + "\"\n"
            + "\"objEntType\": \"" + objEntType + "\""
                + "\n}";
    }
    

    @Override
    public String toString(){
        return toString();
    }
	
}
