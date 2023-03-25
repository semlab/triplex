/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.semlab.triplextract.dataset;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author gr0259sh
 */
public class Reuters21578 {

	private String root;
	private List<String> filePaths;
	private int fileIdx; 
	private int articleIdx;
	//private Document sgmDocument;
	private NodeList articleElements;


	public Reuters21578(String root){
		init(root, "");
	}

	public Reuters21578(String root, String fileExt){
		init(root, fileExt);
	}

	private void init(String root, String fileExt){
		this.root = root;
		this.filePaths = listFiles(fileExt);
		this.fileIdx = 0;
		this.articleIdx = 0;
		//this.sgmDocument = null;
		this.articleElements = null;
	}

	public String getRoot(){
		return this.root;
	}

	public List<String> getFilePaths(){
		return this.filePaths;
	}

	public void restart(){
		this.fileIdx = 0;
		this.articleIdx = 0;
		//this.sgmDocument = null;
		this.articleElements = null;
	}


	public Map next(){
		if(this.filePaths.size() <= 0){
			Logger.getLogger(Reuters21578.class.getName()).log(Level.WARNING, 
					"No files in the folder {0}", this.root);
			return null;
		//} else  if (this.sgmDocument == null && this.fileIdx == 0 
		} else  if (this.articleElements == null && this.fileIdx == 0 
			&& this.articleIdx == 0){ // initial conditions
			Logger.getLogger(Reuters21578.class.getName()).log(Level.INFO, 
					"Loading first file {0}", this.filePaths.get(this.fileIdx));
			//this.sgmDocument = this.loadFile(this.filePaths.get(this.fileIdx));
			this.articleElements = this.loadFile(this.filePaths.get(this.fileIdx));
		} 
		//var doc = this.sgmDocument;
		//NodeList articlesNodes = doc.getElementsByTagName("REUTERS");
		//int nodeCount = articlesNodes.getLength();
		int nodeCount = this.articleElements == null ? 0 : this.articleElements.getLength();
		if(nodeCount <= this.articleIdx){
			this.fileIdx += 1;
			this.articleIdx = 0;
			if(filePaths.size() <= this.fileIdx) {
				Logger.getLogger(Reuters21578.class.getName()).log(Level.INFO, 
						"End of dataset");
				return null;
			}
			Logger.getLogger(Reuters21578.class.getName()).log(Level.INFO, 
					"Loading file {0}", this.filePaths.get(this.fileIdx));
			//this.sgmDocument = this.loadFile(this.filePaths.get(this.fileIdx));
			this.articleElements =  this.loadFile(this.filePaths.get(this.fileIdx));
		}
		System.out.println("file = "+this.filePaths.get(this.fileIdx) + "["
			+this.fileIdx +"/" + this.filePaths.size() + "]"
			+", article = "+ this.articleIdx +"/"+nodeCount);
		Map articleMap = new HashMap();
		Node articleNode = this.articleElements.item(this.articleIdx);
		if (articleNode != null && articleNode.getNodeType() == Node.ELEMENT_NODE) {
			var artElement = (Element) articleNode;
			var bodyTag = artElement.getElementsByTagName("BODY").item(0); 
			String body = bodyTag != null ? bodyTag.getTextContent() : "";
			String id = artElement.getAttribute("NEWID");
			articleMap.put("ID", id);
			articleMap.put("BODY", body);
			articleMap.put("DATE", null);// TODO
		}
		
		this.articleIdx++;
		return articleMap; 
	}


	private List<String> listFiles(String fileExt){
		var path = Paths.get(this.root);
		if(!Files.isDirectory(path)){
		    throw new IllegalArgumentException("Path should be a directory");
		}
		List<String> paths;
		try (Stream<Path> walk = Files.walk(path)) {
		    paths = walk
				.filter(p -> !Files.isDirectory(p))
				.map(p -> p.toString().toLowerCase())
				.filter(f -> f.endsWith(fileExt))
				.collect(Collectors.toList());
		} catch (IOException e){
			paths = new ArrayList();
			e.printStackTrace();
		}
		paths.sort(null);
		return paths;
	}


	//private Document loadFile(String filePath){
	private NodeList loadFile(String filePath){
		String sgml;
		NodeList articleElmts = null;
		try{
			sgml = Files.readString(Paths.get(filePath));
			ByteArrayInputStream input = new ByteArrayInputStream(
				sgml.getBytes("UTF-8"));
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setAttribute(
				"http://apache.org/xml/features/nonvalidating/load-external-dtd", 
				false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(input);
			doc.getDocumentElement().normalize();
			articleElmts = doc.getElementsByTagName("REUTERS");
			//return doc;
		} catch (IOException | SAXException | ParserConfigurationException ex){
			Logger.getLogger(Reuters21578.class.getName()).log(Level.SEVERE, null, ex);
                        return null;
		}
		return articleElmts;
	}



		

}
