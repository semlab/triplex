/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.semlab.triplex;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileReader;
import java.util.ArrayList;

/**
 *
 * @author gr0259sh
 * ok
 */
public class Triplex {
    
    private static CommandLine parseArguments(String[] args){
        Options options = new Options();
        Option input = new Option("i", "input", true, "input file path");
        input.setRequired(true);
        options.addOption(input);
        Option output = new Option("o", "output", true, "output file");
        output.setRequired(true);
        options.addOption(output);
        Option tkOutput = new Option("t", "tokens", true, "tokens output file");
        tkOutput.setRequired(true); // TODO make it optional
        options.addOption(tkOutput);
        Option help = new Option("h", "help", false, "Print help and usage.");
        help.setRequired(false);
        options.addOption(help);
        Option link = new Option("l", "link", false, "Enable (heuristic) entity linking.");
        link.setRequired(false);
        options.addOption(link);
        Option saveLinks = new Option("L","savelinks", true, "Save entity link extractions."
            + " Only works if linking is enabled.");
        link.setRequired(false);
        options.addOption(saveLinks);
        // TODO: git gut with better help message https://commons.apache.org/proper/commons-cli/usage.html
        var helpHeader = String.join(
            "USAGE:\n",
            "\ttriplextract [OPTIONS] [FILE]...\n",
            "DESCRIPTION:\n",
            "\tExtract triplets from (text) files.\n"
        );
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;//not a good practice, it serves it purpose 
        try {
            cmd = parser.parse(options, args);
            /*
            if(cmd.getArgList().isEmpty()){
                throw new ParseException("No input file provided");
            }
            //*/
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("triplexdemo", helpHeader, options, "");
            System.exit(1);
        }
        /*
        System.out.println("ARg list");
        for (var s: cmd.getArgList()){
            System.out.println(s);
        }
        //*/
        if (cmd.hasOption(help)){
            formatter.printHelp("triplexdemo", helpHeader, options, "");
            System.exit(0);
        }
        return cmd;
    }



   public static void main(String[] args){
       /*
       Extraction instance = new Extraction(0, 
                "The government's National Resistance Army (NRA) closed The main road between Kampala and the Kenya border, Uganda's most important trade artery on Friday after the rebel Holy Spirit Movement of priestess Alice Lakwena reached a village on The main road between Kampala and the Kenya border, Uganda's most important trade artery, said.", 
                "National Resistance Army", 
                "be", 
                "NRA", 
                "National Resistance Army", 
                "ORGANIZATION",
                "NRA", 
                "ORGANIZATION");
       System.out.println(instance.hasSubjObjSameType());
       System.out.println(instance.hasEntitiesOnly());
       System.out.println(instance.hasPronouns());
       System.out.println(instance.isEntityLink());
       System.exit(0);
       //*/
       CommandLine cmdArgs = parseArguments(args);
        try{
            String inputFilePath = cmdArgs.getOptionValue("input");
            CSVReader csvReader = new CSVReader(new FileReader(inputFilePath));
            String outputFilePath = cmdArgs.getOptionValue("output");
            boolean isLinkerEnabled = cmdArgs.hasOption("link");
            String linksOutputPath = cmdArgs.getOptionValue("savelinks");
            BufferedWriter linksBufferWriter = linksOutputPath == null ? null : 
                    new BufferedWriter(new FileWriter(linksOutputPath));
            String tokensOutputPath = cmdArgs.hasOption("tokens") ? 
                    cmdArgs.getOptionValue("tokens") : "tokens.txt";
            //FileWriter tokensWriter = new FileWriter(tokensOutputPath);
            BufferedWriter tokensBuffer = new BufferedWriter(new FileWriter(tokensOutputPath));  
            //FileWriter outputWriter = new  FileWriter(outputFilePath);
            BufferedWriter outputBuffer = new BufferedWriter(new  FileWriter(outputFilePath));
            String csvHeader = String.join("\",\"", Arrays.asList(
                    "Id", "SENTENCE","SUBJECT","RELATION","OBJECT","SUBJ_ENT",
                    "SUBJ_ENT_TYPE","OBJ_ENT","OBJ_ENT_TYPE"
            ));
            outputBuffer.write("\"" + csvHeader +"\"");
            outputBuffer.newLine();

            var entityRules = new HashMap();
            entityRules.put("COMMODITY", "./data/nerrules/commodities.rules");
            Properties props = new Properties();
            props.setProperty("ner.additional.regexner.ignorecase", "true");
            var extractor = new Extractor(entityRules, props);
            int extractionsTotal = 0;
            int articleCount = 0;
            String[] csvLine;
            while((csvLine = csvReader.readNext()) != null){
                // TODO Read all and parallelize ? 
                articleCount++;
                System.out.println("Processing article " + articleCount + "\r");
                //System.out.print("\r");
                String text = csvLine[0];
                var extractions = extractor.extract(text);
                for (var e: extractions){
                    e.setArticleId(String.valueOf(articleCount));
                }
                int allExtractionCount = extractions.size();
                if (isLinkerEnabled){
                    System.out.println("Link Enabled");
                    extractions = extractions.stream().filter(
                            e -> !e.isEntityLink()).collect(Collectors.toList());
                    if (linksOutputPath != null){
                        System.out.print("Will write link: ");
                        //var extractionLinks = extractions.stream()
                        //        .filter(e -> e.isEntityLink())
                        //        .collect(Collectors.toList());
                        String linksStr = extractions.stream()
                                .filter(e -> e.isEntityLink())
                                .map(Extraction::toCSV)
                                .collect(Collectors.joining("\n"));
                        System.out.println(linksStr);
                        linksBufferWriter.write(linksStr);
                    }
                    System.out.println("Extracted: "+ allExtractionCount + ", Kept: " + extractions.size());
                }
                if(extractions.size() > 0){
                    for (var extraction: extractions){
                            outputBuffer.write(extraction.toCSV());
                            outputBuffer.newLine();
                    }
                    outputBuffer.flush();
                }
                extractionsTotal += extractions.size();
                if (tokensBuffer != null){
                    var fTokens = extractor.getTokens().stream()
                            .filter(t -> t.matches("(\\w\\s?)+"))
                            .map(String::toLowerCase)
                            .map(t -> t.replaceAll("\\s", "_")) // word2vec style word phrases
                            .collect(Collectors.toList());
                    //tokensBuffer.write(String.join(",",fTokens));
                    tokensBuffer.write(String.join(" ",fTokens)); // space separator
                    tokensBuffer.newLine();
                    tokensBuffer.flush();
                }
            }
            System.out.println(articleCount+ " articles processed!");
            outputBuffer.close();
            System.out.println(String.format("Extractions: %d, Completed!",
                    extractionsTotal));
        } catch (CsvValidationException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
	}
   }
   
}
