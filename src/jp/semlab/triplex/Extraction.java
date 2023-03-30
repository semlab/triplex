/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.semlab.triplex;

import java.util.Arrays;

/**
 *
 * @author gr0259sh
 */
public class Extraction {

	
    private long id;
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

    public long getId(){
        return this.id;
    }


    public void setId(long id){
        this.id = id;
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
                            Arrays.asList(this.
                                    sentence,
                                    subject,
                                    relation,
                                    object,
                                    subjEnt,
                                    subjEntType,
                                    objEnt,
                                    objEntType)) 
            + "\"";
    }

    public String toCSV(){
        return toCSV(",");	
    }

    @Override
    public String toString(){
        return "sentence: " + sentence + "\n"
            + "subject: " + subject + "\n"
            + "relation: " + relation + "\n"
            + "object: "  + object + "\n"
            + "subjEnt: " + subjEnt +"\n"
            + "subjEntType: " + subjEntType + "\n"
            + "objEnt: " +  objEnt + "\n"
            + "objEntType: " + objEntType + "\n";
    }
	
}
