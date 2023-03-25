/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.semlab.triplextract;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.stream.Collectors;
import jp.semlab.triplex.Triplex;
import jp.semlab.triplextract.dataset.Reuters21578;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author gr0259sh
 */
public class Triplextract {
    
    private static CommandLine parseArguments(String[] args){
        Options options = new Options();
        Option input = new Option("i", "input", true, "input file path");
        //input.setRequired(true);
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

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;//not a good practice, it serves it purpose 
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("triplexdemo", options);

            System.exit(1);
        }       
        for (var s: cmd.getArgList()){
            System.out.println(s);
        }
        if (cmd.hasOption(help)){
            var message = String.join(
                "USAGE:\n",
                "\ttriplex source_folder target_folder",
                "DESCRIPTION:\n",
                "\tExtract triplets from text files within 'source_folder' and ",
                "write them as csv files in 'target_folder'.\n",
                "Note: based on "
            );
            formatter.printHelp("triplexdemo", message, options, "");
            System.exit(0);
        }

        return cmd;
    }



   public static void main(String[] args){
       CommandLine cmdArgs = parseArguments(args);
       
       
	//String outputFilePath = "../../data/triples/triples.csv";
        //String tokensOutputPath = "../../data/tokens.csv";
        
	
        try {
            String outputFilePath = cmdArgs.getOptionValue("output");
            String tokensOutputPath = null;
            FileWriter tokensWriter = null;
            BufferedWriter tokensBW = null;
            if (cmdArgs.hasOption("tokens")){
                tokensOutputPath = cmdArgs.getOptionValue("tokens");
                tokensWriter = new FileWriter(tokensOutputPath, true);
		tokensBW = new BufferedWriter(tokensWriter);
            }
            FileWriter writer = new  FileWriter(outputFilePath, true);
            BufferedWriter bw = new BufferedWriter(writer);
            String csvHeader = String.join("\",\"", Arrays.asList(
                    "SENTENCE",
                    "SUBJECT",
                    "RELATION",
                    "OBJECT",
                    "SUBJ_ENT",
                    "SUBJ_ENT_TYPE",
                    "OBJ_ENT",
                    "OBJ_ENT_TYPE"
            ));
            bw.write("\"" + csvHeader +"\"");
            bw.newLine();

            var entityRules = new HashMap();
            entityRules.put("COMMODITY", "../../trace/data/nerrules/commodities.rules");
            Properties props = new Properties();
            props.setProperty("ner.additional.regexner.ignorecase", "true");
            var extractor = new Triplex(entityRules, props);
            int extractionsTotal = 0;
            Reuters21578 reutersDS = new Reuters21578("../../trace/data/reuters21578sgml", ".sgml");
            var docMap = reutersDS.next();
            while(docMap != null){
                String text = docMap.get("BODY").toString();
                var extractions = extractor.extract(text);
                int extractionsCount = extractions.size();
                if(extractionsCount > 0){
                        for (var extraction: extractions){
                                bw.write(extraction.toCSV());
                                bw.newLine();
                        }
                        bw.flush();
                }
                extractionsTotal += extractionsCount;
                //System.out.print(String.format("Extractions: %d\r",
                //	extractionsTotal));
                docMap = reutersDS.next();
                if (tokensBW != null){
                    var fTokens = extractor.getTokens().stream()
                            .map(String::toLowerCase)
                            .filter(t -> t.matches("(\\w\\s?)+"))
                            .collect(Collectors.toList());
                    tokensBW.write(String.join(",",fTokens));
                    tokensBW.newLine();
                    tokensBW.flush();
                }
            }
            bw.close();
            System.out.println(String.format("Extractions: %d, Completed!",
                    extractionsTotal));
	} catch (IOException e) {
		e.printStackTrace();
	}
   }
   
}
