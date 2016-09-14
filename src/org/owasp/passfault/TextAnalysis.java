/* ©Copyright 2011 Cameron Morris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.owasp.passfault;

import org.apache.commons.cli.*;
import org.owasp.passfault.dictionary.DictionaryPatternsFinder;
import org.owasp.passfault.dictionary.ExactWordStrategy;
import org.owasp.passfault.dictionary.FileDictionary;
import org.owasp.passfault.finders.ParallelFinder;
import org.owasp.passfault.dictionary.Dictionary;

import java.io.*;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;


/**
 * Command line password evaluator.
 * @author cam
 */
public class TextAnalysis {

  public static final String WORD_LIST_EXTENSION = ".words";
  public static TimeToCrack crack = TimeToCrack.GPU1;
  private static Dictionary cDict;
  private static boolean time2crackGPU, time2crackSpeed, input, output, customDict, verbose;
  private static String password, inputPath, outputPath, customDictPath;
  private static int machineNum, hashNum;
  private static float hashSpeed;

  private final CompositeFinder finder;

  public static void main(String[] args) throws Exception {
    if (args.length == 0){
      System.out.println("CLI error: you must provide some information. See help for more info.");
      System.exit(0);
    }
    cli(args);

    TextAnalysis analyzer = new TextAnalysis();
    analyzer.printBanner();
    analyzer.process();
  }

  public TextAnalysis() throws IOException {
    Collection<PatternFinder> finders = new FinderByPropsBuilder().
        loadDefaultWordLists().
        isInMemory(true).
        build();
    if (customDict)
      finders.add(new DictionaryPatternsFinder(cDict, new ExactWordStrategy()));

    finder = new ParallelFinder(finders);
  }

  private static void cli(String[] args){
    Options options = new Options();
    options.addOption("p", "password", true, "password to be analyzed");
    options.addOption("i", "input", true, "path to input file");
    options.addOption("o", "output", true, "path to output file");
    options.addOption("g", "gpu", true, "number of GPUs for Time to Crack analysis");
    options.addOption("d", "customDictionary", true, "path to custom dictionary");
    options.addOption("f", "hashFunction", true, "hash function for Time to Crack analysis");
    options.addOption("s", "hashspeed", true, "hashes per second for Time to Crack analysis");
    options.addOption("v", "verbose", false, "verbose mode");
    options.addOption("h", "help", false, "help menu");

    try {
      CommandLineParser parser = new DefaultParser();
      CommandLine line = parser.parse(options, args);
      boolean exit = false;

      if (line.hasOption("help")){
        System.out.println("help instructions");

        System.exit(0);
      }

      if(line.hasOption("input")){
        inputPath = line.getOptionValue("input");
        //read input file

        input = true;
      }

      if (line.hasOption("output")){
        outputPath = line.getOptionValue("output");
        //create output file

        output = true;
      }

      if(line.hasOption("customDictionary")){
        customDictPath = line.getOptionValue("customDictionary");

        //create output file
        try {
          cDict = FileDictionary.newInstance(customDictPath, "customDict");
        }catch (IOException e){
          System.out.println("CLI error: invalid path in -d option. See help for more info.");
          exit = true;
        }

        customDict = true;
      }

      if(line.hasOption("password")){
        if (line.hasOption("input")){
          System.out.println("CLI error: too many input options! Use either -p or -i, never both! See help for more info.");
          exit = true;
        }

        password = line.getOptionValue("password");
        if (password.length() < 4){
          System.out.println("CLI error: password too small!");
          exit = true;
        }
      }

      if (line.hasOption("hashspeed") || line.hasOption("gpu") || line.hasOption("hashFunction")){
        if ((line.hasOption("gpu") && !line.hasOption("hashFunction")) || (!line.hasOption("gpu") && line.hasOption("hashFunction"))) {
          System.out.println("CLI error: in order to get Time to Crack analysis, you need to provide either only -s, or both -g and -f options. See help for more info.");
          exit = true;

        }else if(line.hasOption("hashspeed") && line.hasOption("gpu") && line.hasOption("hashFunction")) {
          System.out.println("CLI error: in order to get Time to Crack analysis, you need to provide either only -s, or both -g and -f options. See help for more info.");
          exit = true;

        }else if(line.hasOption("hashspeed")){
          time2crackSpeed = true;

          String hps = line.getOptionValue("hashspeed");
          hashSpeed = new Float(hps);

          if (hashSpeed <= 0){
            System.out.println("CLI error: you must provide a number in the right format for -s option. See help for more info.");
            exit = true;
          }

        }else if (line.hasOption("gpu") && line.hasOption("hashFunction")){
          time2crackGPU = true;

          String gpu = line.getOptionValue("gpu");
          machineNum = new Integer(gpu);

          String hashFunction = line.getOptionValue("hashFunction");
          hashNum = new Integer(hashFunction);

          if (machineNum < 1 || machineNum > 4 || hashNum < 1 || hashNum > 4){
            System.out.println("CLI error: you must provide a number between 1 and 4 for -g and -f options. See help for more info.");
            exit = true;
          }
        }
      }

      if (exit){
        System.out.println("Leaving.");
        System.exit(0);
      }

    }catch(ParseException exp){
      System.out.println("CLI error: " + exp.getMessage());
      System.out.println("Leaving.");
      System.exit(0);
    }
  }

  public void printBanner(){
    System.out.print(
"                                         /******                    /**   /**                \n"+
"                                        /**__  **                  | **  | **                \n"+
"  /******   /******   /******* /*******| **  \\__//******  /**   /**| ** /******              \n"+
" /**__  ** |____  ** /**_____//**_____/| ****   |____  **| **  | **| **|_  **_/              \n"+
"| **  \\ **  /*******|  ******|  ****** | **_/    /*******| **  | **| **  | **                \n"+
"| **  | ** /**__  ** \\____  **\\____  **| **     /**__  **| **  | **| **  | ** /**            \n"+
"| *******/|  ******* /*******//*******/| **    |  *******|  ******/| **  |  ****/            \n"+
"| **____/  \\_______/|_______/|_______/ |__/     \\_______/ \\______/ |__/   \\___/              \n"+
"| **                                                                                         \n"+
"| **                                                                                         \n"+
"|__/                                                                                         \n"+
"\n");

  }

  private void process()
      throws Exception {
    PasswordAnalysis analysis = new PasswordAnalysis(password);
    
    switch (machineNum) {
      case 1: crack = TimeToCrack.GPU1;
              break;
      case 2: crack = TimeToCrack.GPU10;
              break;
      case 3: crack = TimeToCrack.GPU100;
              break;
      case 4: crack = TimeToCrack.GPU1000;
              break;
      default: crack = TimeToCrack.GPU1;
               break;
    }
    
    switch (hashNum) {
      case 1: crack.setHashType("bcrypt", hashNum);
              break;        
      case 2: crack.setHashType("md5crypt", hashNum);
              break;
      case 3: crack.setHashType("sha512crypt", hashNum);
              break;
      case 4: crack.setHashType("Password Safe", hashNum);
              break;
      default: crack.setHashType("bcrypt", hashNum);
              break;
    }

    long then = System.currentTimeMillis();
    finder.blockingAnalyze(analysis);
    PathCost worst = analysis.calculateHighestProbablePatterns();
    long now = System.currentTimeMillis();

    List<PasswordPattern> path = worst.getPath();
    System.out.println("\n\nMost crackable patterns:");
    double costSum = 0;
    for (PasswordPattern subPattern : path) {
      //get the sum of pattern costs:
      costSum += subPattern.getCost();
    }

    if (!output) {
      for (PasswordPattern subPattern : path) {
        System.out.format("'%s' matches the pattern '%s' in %s\n", subPattern.getMatchString(), subPattern.getDescription(), subPattern.getClassification());
        System.out.format("\t%s passwords in the pattern\n", TimeToCrack.getRoundedSizeString(subPattern.getCost()));
        System.out.format("\tcontains %3.2f percent of password strength\n", subPattern.getCost() / costSum * 100);
      }

      //System.out.print("Total passwords in all finders: ");
      //System.out.println(TimeToCrack.getRoundedSizeString(worst.getTotalCost()));

      if (time2crackGPU || time2crackSpeed) {
        System.out.format("Estimated time to crack with %s GPU(s): %s\n",
        crack.getNumberOfGPUs(), crack.getTimeToCrackString(worst.getTotalCost()));
      }

      System.out.format("Analysis Time: %f seconds\n", (now - then) / (double) 1000);
      System.exit(0);
    }
  }
}
