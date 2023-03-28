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
import jp.semlab.triplex.Extractor;
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

/**
 *
 * @author gr0259sh
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
        tkOutput.setRequired(false);
        options.addOption(tkOutput);
        Option help = new Option("h", "help", false, "Print help and usage.");
        help.setRequired(false);
        options.addOption(help);
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
       CommandLine cmdArgs = parseArguments(args);
        try{
            String inputFilePath = cmdArgs.getOptionValue("input");
            CSVReader csvReader = new CSVReader(new FileReader(inputFilePath));
            String outputFilePath = cmdArgs.getOptionValue("output");
            String tokensOutputPath = cmdArgs.hasOption("tokens") ? 
                    cmdArgs.getOptionValue("tokens") : "tokens.txt";
            FileWriter tokensWriter = new FileWriter(tokensOutputPath);
            BufferedWriter tokensBuffer = new BufferedWriter(tokensWriter);  
            FileWriter outputWriter = new  FileWriter(outputFilePath);
            BufferedWriter outputBuffer = new BufferedWriter(outputWriter);
            String csvHeader = String.join("\",\"", Arrays.asList(
                    "SENTENCE","SUBJECT","RELATION","OBJECT","SUBJ_ENT",
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
                System.out.println("Processing article " + articleCount);
                //System.out.print("\r");
                String text = csvLine[0];
                var extractions = extractor.extract(text);
                int extractionsCount = extractions.size();
                if(extractionsCount > 0){
                        for (var extraction: extractions){
                                outputBuffer.write(extraction.toCSV());
                                outputBuffer.newLine();
                        }
                        outputBuffer.flush();
                }
                extractionsTotal += extractionsCount;
                if (tokensBuffer != null){
                    var fTokens = extractor.getTokens().stream()
                            .map(String::toLowerCase)
                            .filter(t -> t.matches("(\\w\\s?)+"))
                            .collect(Collectors.toList());
                    tokensBuffer.write(String.join(",",fTokens));
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
