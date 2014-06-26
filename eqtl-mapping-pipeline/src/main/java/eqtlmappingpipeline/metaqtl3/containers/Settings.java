 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eqtlmappingpipeline.metaqtl3.containers;

import eqtlmappingpipeline.Main;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import umcg.genetica.io.Gpio;
import umcg.genetica.io.text.TextFile;
import umcg.genetica.io.trityper.TriTyperGeneticalGenomicsDatasetSettings;
import umcg.genetica.io.trityper.util.ChrAnnotation;

/**
 *
 * @author harmjan
 */
public class Settings extends TriTyperGeneticalGenomicsDatasetSettings {

    // Output
    public String settingsTextToReplace = null;                                // Replace this text in the configuration XML file
    public String settingsTextReplaceWith = null;                              // Replace the text in settingsTextToReplace with this text
    public boolean createSNPPValueSummaryStatisticsFile = false;               // Output SNP P-Value summary statistics
    public boolean createSNPSummaryStatisticsFile = false;                     // Output SNP P-Value summary statistics
    public boolean createEQTLPValueTable = false;                              // Output an eQTL p-value table (only applies for a limited number (500) of SNP)
    public String outputReportsDir;                                            // Output directory for reports
    // SNP QC
    // Analysis settings
    public boolean performParametricAnalysis = false;                          // Perform parametric analysis
    public String performTwoPartModel = null;                          // Perform parametric analysis
    public boolean useAbsoluteZScorePValue = false;                            // Use absolute Z-score? (required for finding opposite allelic effects)
    public int ciseQTLAnalysMaxSNPProbeMidPointDistance = 250000;                       // Midpoint distance for declaring an eQTL effect CIS
    public int maxNrMostSignificantEQTLs = 500000;                             // Max number of results stored in memory
    public boolean performParametricAnalysisGetAccuratePValueEstimates;         // Use an accurate estimation of the P-values
    public Integer nrThreads;                                                      // Use this number of threads
    // Multiple testing correction
    public double fdrCutOff = 0.05;                                                   // Cutoff for FDR procedure
    public int nrPermutationsFDR = 1;                                              // Number of permutations to determine FDR
    // confinements
    public boolean performEQTLAnalysisOnSNPProbeCombinationSubset;             // Confine to a certain set of probe/snp combinations?
    public Byte confineToSNPsThatMapToChromosome;                               // Confine SNP to be assessed to SNPs mapped on this chromosome
    public boolean expressionDataLoadOnlyProbesThatMapToChromosome = false;    // Only load expression data for probes with a known chromosome mapping
    public HashSet<String> tsSNPsConfine;                           // Confine analysis to the SNPs in this hash
    public HashMap<String, HashSet<String>> tsSNPProbeCombinationsConfine;           // Confine analysis to the combinations of SNP and Probes in this hash
    // plots
    public double plotOutputPValueCutOff;                                      // Use this p-value as a cutoff for drawing plots
    public String plotOutputDirectory;                                         // Print the plots in this directory
    public boolean runOnlyPermutations = false;
    public Integer startWithPermutation;
    public Boolean confineSNPsToSNPsPresentInAllDatasets = true;
    public boolean confineProbesToProbesPresentInAllDatasets;
    public ArrayList<TriTyperGeneticalGenomicsDatasetSettings> datasetSettings;
    public String regressOutEQTLEffectFileName;
    public boolean regressOutEQTLEffectsSaveOutput;
    public Double snpQCCallRateThreshold = 0.95;
    public Double snpQCHWEThreshold = 0.0001;
    public Double snpQCMAFThreshold = 0.05;
    public Byte confineToProbesThatMapToChromosome;
    public boolean createBinaryOutputFiles;
    public boolean createTEXTOutputFiles;
    public String strConfineSNP;
    public String strConfineProbe;
    public String strConfineSNPProbe;
    public boolean provideFoldChangeData = false;
    public boolean provideBetasAndStandardErrors = true;
    public boolean equalRankForTies = false;
    public boolean createQQPlot = true;
    public boolean createDotPlot = true;
    public boolean metaAnalyseInteractionTerms = false;
    public boolean metaAnalyseModelCorrelationYHat = false;
    public String pathwayDefinition;
    public boolean snpProbeConfineBasedOnChrPos = false; //Snp in snp confine and snp probe confine list are defined as chr:pos instead of snp ID.
    private static final Pattern TAB_PATTERN = Pattern.compile("\\t");
    public boolean permuteCovariates;

    public Settings() {
    }

    public void load(String settings) throws IOException, ConfigurationException {

        XMLConfiguration config = new XMLConfiguration(settings);           // Use the apache XML configuration parser

        String analysisType = null;
        String correlationType = null;
        String twoPartModel = null;
        Boolean useAbsPVal = null;
        Integer nrthread = null;
        Integer cisDist = null;
        Double MAF = null;
        Double HWE = null;
        Double callrate = null;
        String correctiontype = null;
        Double mtThreshold = null;
        Integer numPermutations = null;
        String outdir = null;
        Double outputplotthreshold = null;
        String outputplotdirectory = null;

        // load the defaults:
        // QC defaults
        try {
            callrate = config.getDouble("defaults.qc.snpqccallratethreshold");
        } catch (Exception e) {
        }
        if (callrate != null) {
            snpQCCallRateThreshold = callrate;
        } else {
            snpQCCallRateThreshold = 0.95;
        }

        try {
            HWE = config.getDouble("defaults.qc.snpqchwethreshold");
        } catch (Exception e) {
        }

        if (HWE != null) {
            snpQCHWEThreshold = HWE;
        } else {
            snpQCHWEThreshold = 0.0000001;
        }

        try {
            MAF = config.getDouble("defaults.qc.snpqcmafthreshold");
        } catch (Exception e) {
        }
        if (MAF != null) {
            snpQCMAFThreshold = MAF;
        } else {
            snpQCMAFThreshold = 0.05;
        }

        // analysis settings
        try {
            analysisType = config.getString("defaults.analysis.analysistype");
        } catch (Exception e) {
        }

        try {
            createQQPlot = config.getBoolean("defaults.analysis.createqqplot");
        } catch (Exception e) {
        }

        try {
            createDotPlot = config.getBoolean("defaults.analysis.createdotplot");
        } catch (Exception e) {
        }

        try {
            runOnlyPermutations = config.getBoolean("defaults.analysis.onlypermutations", false);
        } catch (Exception e) {
        }

        try {
            String strStartWithPermutation = config.getString("defaults.analysis.startwithpermutation", null);

            if (settingsTextToReplace != null && strStartWithPermutation.contains(settingsTextToReplace)) {
                strStartWithPermutation = strStartWithPermutation.replace(settingsTextToReplace, settingsTextReplaceWith);
            }
            startWithPermutation = Integer.parseInt(strStartWithPermutation);

        } catch (Exception e) {
        }

        if (analysisType != null) {
            if (analysisType.toLowerCase().equals("cis")) {
                cisAnalysis = true;
                transAnalysis = false;
            } else if (analysisType.toLowerCase().equals("trans")) {
                cisAnalysis = false;
                transAnalysis = true;
            } else if (analysisType.toLowerCase().equals("cistrans")) {
                cisAnalysis = true;
                transAnalysis = true;
            }
        } else {
            cisAnalysis = true;
            transAnalysis = false;
        }

        try {
            cisDist = config.getInteger("defaults.analysis.cisanalysisprobedistance", null);
        } catch (Exception e) {
        }
        if (cisDist != null) {
            ciseQTLAnalysMaxSNPProbeMidPointDistance = cisDist;
        } else {
            ciseQTLAnalysMaxSNPProbeMidPointDistance = 250000;
        }

        try {
            correlationType = config.getString("defaults.analysis.correlationtype");
        } catch (Exception e) {
        }
        if (correlationType != null) {
            if (correlationType.toLowerCase().equals("parametric")) {
                performParametricAnalysis = true;
            } else {
                performParametricAnalysis = false;
            }
        } else {
            performParametricAnalysis = true;
        }
        
        try {
            twoPartModel = config.getString("defaults.analysis.TwoPartModel");
        } catch (Exception e) {
        }
        if (twoPartModel != null) {
            if (twoPartModel.toLowerCase().equals("both")) {
                performTwoPartModel = "BOTH";
            } else if (twoPartModel.toLowerCase().equals("continues")) {
                performTwoPartModel = "CONTINUES";
            } else if (twoPartModel.toLowerCase().equals("binary")) {
                performTwoPartModel = "BINARY";
            } else {
                twoPartModel=null;
            }
        }

        Boolean useIdenticalRanksForTies = null;
        try {
            useIdenticalRanksForTies = config.getBoolean("defaults.analysis.equalrankforties");
        } catch (Exception e) {
        }
        if (useIdenticalRanksForTies != null) {
            equalRankForTies = useIdenticalRanksForTies;
        } else {
            equalRankForTies = false;
        }

        /*
         public boolean metaAnalyseInteractionTerms = false;
         public boolean metaAnalyseModelCorrelationYHat = false;
         */
        Boolean metaAnalyzeInteractionTermsB = null;
        try {
            metaAnalyzeInteractionTermsB = config.getBoolean("defaults.analysis.metaAnalyseInteractionTerms");
        } catch (Exception e) {
        }

        permuteCovariates = false;
        try {
            metaAnalyzeInteractionTermsB = config.getBoolean("defaults.analysis.permuteCovariates");
        } catch (Exception e) {
        }
        if (metaAnalyzeInteractionTermsB != null) {
            metaAnalyseInteractionTerms = metaAnalyzeInteractionTermsB;
        } else {
            metaAnalyseInteractionTerms = false;
        }

        Boolean metaAnalyseModelCorrelationYHatB = null;
        try {
            metaAnalyseModelCorrelationYHatB = config.getBoolean("defaults.analysis.metaAnalyseModelCorrelationYHat");
        } catch (Exception e) {
        }
        if (metaAnalyseModelCorrelationYHatB != null) {
            metaAnalyseModelCorrelationYHat = metaAnalyseModelCorrelationYHatB;
        } else {
            metaAnalyseModelCorrelationYHat = false;
        }

        try {
            useAbsPVal = config.getBoolean("defaults.analysis.useabsolutepvalue");
        } catch (Exception e) {
        }

        if (useAbsPVal == null) {
            useAbsoluteZScorePValue = false;
        } else {
            useAbsoluteZScorePValue = useAbsPVal;
        }

        try {
            nrthread = config.getInteger("defaults.analysis.threads", null);
        } catch (Exception e) {
        }

        if (nrthread == null) {
            nrThreads = (Runtime.getRuntime().availableProcessors() - 1);
        } else {
            int numProcs = Runtime.getRuntime().availableProcessors();
            if (nrthread > numProcs || nrthread < 1) {
                nrThreads = numProcs;
            } else {
                nrThreads = nrthread;
            }
        }

        // multiple testing
        try {
            correctiontype = config.getString("defaults.multipletesting.type");
        } catch (Exception e) {
        }
        if (correctiontype != null) {
        } else {
        }

        try {
            mtThreshold = config.getDouble("defaults.multipletesting.threshold", null);
        } catch (Exception e) {
        }

        if (mtThreshold != null) {
            fdrCutOff = mtThreshold;
        } else {
            fdrCutOff = 0.05;
        }

        try {
            numPermutations = config.getInteger("defaults.multipletesting.permutations", null);
        } catch (Exception e) {
        }
        if (numPermutations != null) {
            nrPermutationsFDR = numPermutations;
        } else {
            nrPermutationsFDR = 0;
        }

        // output settings
        try {
            outdir = config.getString("defaults.output.outputdirectory");
            if (settingsTextToReplace != null && outdir.contains(settingsTextToReplace)) {
                outdir = outdir.replace(settingsTextToReplace, settingsTextReplaceWith);
            }
        } catch (Exception e) {
        }

        if (outdir != null) {
            outputReportsDir = outdir;
            if (!outputReportsDir.endsWith("/")) {
                outputReportsDir += "/";
            }

            // check if dir exists. if it does not, create it:
            if (!Gpio.exists(outdir)) {
                Gpio.createDir(outdir);
            }

            config.save(outputReportsDir + "metaqtlsettings.xml");

        } else {
            System.out.println("Error: please supply an output directory.");
            System.exit(-1);
        }

        try {
            String probeConversionFileLoc = config.getString("defaults.analysis.probeconversion", null);
            if (probeConversionFileLoc != null && probeConversionFileLoc.length() > 0) {
                System.out.println(probeConversionFileLoc);

            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        try {
            outputplotthreshold = config.getDouble("defaults.output.outputplotthreshold");
        } catch (Exception e) {
        }

        try {
            createBinaryOutputFiles = config.getBoolean("defaults.output.binaryoutput", false);
            createTEXTOutputFiles = config.getBoolean("defaults.output.textoutput", true);
        } catch (Exception e) {
        }

        if (outputplotthreshold != null) {
            plotOutputPValueCutOff = outputplotthreshold;
        } else {
            plotOutputPValueCutOff = Double.MAX_VALUE;
        }

        try {
            outputplotdirectory = config.getString("defaults.output.outputplotdirectory");
            if (settingsTextToReplace != null && outputplotdirectory.contains(settingsTextToReplace)) {
                outputplotdirectory = outputplotdirectory.replace(settingsTextToReplace, settingsTextReplaceWith);
            }

        } catch (Exception e) {
        }

        if (outputplotdirectory != null) {
            plotOutputDirectory = outputplotdirectory;
            if (!plotOutputDirectory.endsWith("/")) {
                plotOutputDirectory += "/";
            }
        } else {
            plotOutputDirectory = outdir + "/plots/";
        }

        // check if dir exists. if it does not, create it if plots are requested
        if (plotOutputPValueCutOff != Double.MAX_VALUE) {
            if (!Gpio.exists(plotOutputDirectory)) {
                Gpio.createDir(plotOutputDirectory);
            }
        }

        try {
            createSNPPValueSummaryStatisticsFile = config.getBoolean("defaults.output.generatesnppvaluesummarystatistics", false);
        } catch (Exception e) {
        }

        try {
            provideFoldChangeData = config.getBoolean("defaults.output.generatefoldchangevalues", false);
        } catch (Exception e) {
        }

        try {
            provideBetasAndStandardErrors = config.getBoolean("defaults.output.generatebetaandfoldchanges", false);
        } catch (Exception e) {
        }

        try {
            createSNPSummaryStatisticsFile = config.getBoolean("defaults.output.generatesnpsummarystatistics", false);
        } catch (Exception e) {
        }

        try {
            createEQTLPValueTable = config.getBoolean("defaults.output.generateeqtlpvaluetable", false);
        } catch (Exception e) {
        }

        try {
            maxNrMostSignificantEQTLs = config.getInt("defaults.output.maxnreqtlresults", 150000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Load only expression probes that map to a known chromosome:
        try {
            expressionDataLoadOnlyProbesThatMapToChromosome = config.getBoolean("defaults.confine.confineProbesThatMapToKnownChromosome");
        } catch (Exception e) {
        }

        // confinements on snp, probe, or snp-probe
        String confineSNP = null;
        String confineProbe = null;
        String snpProbeConfine = null;

        confineSNPsToSNPsPresentInAllDatasets = null;

        // confine to this list of snp
        try {
            confineSNP = config.getString("defaults.confine.snp");
            if (settingsTextToReplace != null && confineSNP.contains(settingsTextToReplace)) {
                confineSNP = confineSNP.replace(settingsTextToReplace, settingsTextReplaceWith);
            }
        } catch (Exception e) {
        }

        // confine to this list of probes
        try {
            confineProbe = config.getString("defaults.confine.probe");
            if (settingsTextToReplace != null && confineProbe.contains(settingsTextToReplace)) {
                confineProbe = confineProbe.replace(settingsTextToReplace, settingsTextReplaceWith);
            }
        } catch (Exception e) {
        }

        // confine to this list of snp-probe combinations
        try {
            snpProbeConfine = config.getString("defaults.confine.snpProbe");
            if (settingsTextToReplace != null && snpProbeConfine.contains(settingsTextToReplace)) {
                snpProbeConfine = snpProbeConfine.replace(settingsTextToReplace, settingsTextReplaceWith);
            }
        } catch (Exception e) {
        }

        try {
            snpProbeConfineBasedOnChrPos = config.getBoolean("defaults.confine.snpProbeConfineBasedOnChrPos", false);
        } catch (Exception e) {
        }

        if (confineSNP != null && confineSNP.trim().length() > 0 && Gpio.exists(confineSNP)) {
            strConfineSNP = confineSNP;
            TextFile in = new TextFile(confineSNP, TextFile.R);
            tsSNPsConfine = new HashSet<String>();
            String[] data = in.readAsArray();
            for (String d : data) {
                d = d.trim();
                if (d.length() > 0) {
                    String[] elems = TAB_PATTERN.split(d);
                    d = elems[0].trim();
                    tsSNPsConfine.add(d.intern());
                }
            }
            in.close();
        } else if (confineSNP != null && confineSNP.trim().length() > 0 && !Gpio.exists(confineSNP)) {
            throw new IOException("Error! SNP confinement file: " + confineSNP + " could not be found.");
        }

        if (confineProbe != null && confineProbe.trim().length() > 0 && Gpio.exists(confineProbe)) {
            strConfineProbe = confineProbe;
            TextFile in = new TextFile(confineProbe, TextFile.R);
            tsProbesConfine = new HashSet<String>();
            String[] data = in.readAsArray();
            for (String d : data) {
                d = d.trim();
                while (d.startsWith(" ")) {
                    d = d.substring(1);
                }
                tsProbesConfine.add(new String(d.getBytes("UTF-8")));
            }
            in.close();
        } else if (confineProbe != null && confineProbe.trim().length() > 0 && !Gpio.exists(confineProbe)) {
            throw new IOException("Error! Probe confinement file: " + confineProbe + " could not be found.");
        }

        if (snpProbeConfine != null && snpProbeConfine.trim().length() > 0 && Gpio.exists(snpProbeConfine)) {
            loadSNPProbeConfinement(snpProbeConfine);
        } else if (snpProbeConfine != null && snpProbeConfine.trim().length() > 0 && !Gpio.exists(snpProbeConfine)) {
            throw new IOException("Error! SNP-Probe confinement file: " + snpProbeConfine + " could not be found.");
        }

        // confine to snp present in all datasets
        try {
            confineSNPsToSNPsPresentInAllDatasets = config.getBoolean("defaults.confine.confineSNPsToSNPsPresentInAllDatasets");
        } catch (Exception e) {
        }

        // confine to SNP that map to this chromosome
        try {
            String confineStr = config.getString("defaults.confine.confineToSNPsThatMapToChromosome", null);
            if (confineStr == null || confineStr.trim().length() == 0) {
                confineToSNPsThatMapToChromosome = null;
            } else {

                confineToSNPsThatMapToChromosome = ChrAnnotation.parseChr(confineStr);
                if (confineToSNPsThatMapToChromosome < 1) {
                    confineToSNPsThatMapToChromosome = null;
                }
            }

        } catch (Exception e) {
        }

        // confine to probes present in all datasets
        confineProbesToProbesPresentInAllDatasets = false;
        try {
            confineProbesToProbesPresentInAllDatasets = config.getBoolean("defaults.confine.confineToProbesPresentInAllDatasets", false);
        } catch (Exception e) {
        }

        regressOutEQTLEffectFileName = null;
        try {
            regressOutEQTLEffectFileName = config.getString("defaults.analysis.regressOutEQTLEffects");
        } catch (Exception e) {
        }
        regressOutEQTLEffectsSaveOutput = false;
        try {
            regressOutEQTLEffectsSaveOutput = config.getBoolean("defaults.analysis.regressOutEQTLEffectsSaveOutput");
        } catch (Exception e) {
        }

        // is there a pathway definition?
        String pathwayDef = null;
        try {
            pathwayDef = config.getString("defaults.analysis.pathwaydefinition");
        } catch (Exception e) {
        }
        this.pathwayDefinition = pathwayDef;

        // dataset parameters
        int i = 0;

        String dataset = config.getString("datasets.dataset(" + i + ").name");  // see if a dataset is defined
        if (settingsTextToReplace != null && dataset.contains(settingsTextToReplace)) {
            dataset = dataset.replace(settingsTextToReplace, settingsTextReplaceWith);
        }

        datasetSettings = new ArrayList<TriTyperGeneticalGenomicsDatasetSettings>();

//            ArrayList<GeneticalGenomicsDataset> vGG = new ArrayList<GeneticalGenomicsDataset>();                    // create a dataset vector
//            GeneticalGenomicsDataset tmpDataset;                                               // create a temporary dataset object
        while (dataset != null) {

            String expressionData = null;
            String dataloc = null;
            String genToExpCoupling = null;
            Boolean qnorm = false;
            Boolean logtr = false;

            TriTyperGeneticalGenomicsDatasetSettings s = new TriTyperGeneticalGenomicsDatasetSettings();
            s.name = dataset;
            datasetSettings.add(s);

            s.cisAnalysis = this.cisAnalysis;
            s.transAnalysis = this.transAnalysis;
            // get the location of the expression data
            try {
                expressionData = config.getString("datasets.dataset(" + i + ").expressiondata");
                if (settingsTextToReplace != null && expressionData.contains(settingsTextToReplace)) {
                    expressionData = expressionData.replace(settingsTextToReplace, settingsTextReplaceWith);
                }
            } catch (Exception e) {
            }

            String expressionPlatform = null;
            try {
                expressionPlatform = config.getString("datasets.dataset(" + i + ").expressionplatform");
                if (settingsTextToReplace != null && expressionData.contains(settingsTextToReplace)) {
                    expressionPlatform = expressionPlatform.replace(settingsTextToReplace, settingsTextReplaceWith);
                }
            } catch (Exception e) {
            }

            probeannotation = null;
            try {
                probeannotation = config.getString("datasets.dataset(" + i + ").probeannotation");
                if (settingsTextToReplace != null && expressionData.contains(settingsTextToReplace)) {
                    probeannotation = probeannotation.replace(settingsTextToReplace, settingsTextReplaceWith);
                }
                if (probeannotation.length() == 0) {
                    probeannotation = null;
                }
            } catch (Exception e) {
            }

            s.expressionplatform = expressionPlatform;
            s.probeannotation = probeannotation;
            s.expressionLocation = expressionData;

            // get the location of the dataset
            try {
                dataloc = config.getString("datasets.dataset(" + i + ").location");
                if (settingsTextToReplace != null && dataloc.contains(settingsTextToReplace)) {
                    dataloc = dataloc.replace(settingsTextToReplace, settingsTextReplaceWith);
                }
            } catch (Exception e) {
                System.out.println("Please provide a location on your disk where " + dataset + " is located");
                System.exit(-1);
            }

            Gpio.formatAsDirectory(dataloc);

            s.genotypeLocation = dataloc;

            // see if there are covariates to load
            String covariateFile = null;
            try {
                covariateFile = config.getString("datasets.dataset(" + i + ").covariates");
                if (settingsTextToReplace != null && covariateFile.contains(settingsTextToReplace)) {
                    covariateFile = covariateFile.replace(settingsTextToReplace, settingsTextReplaceWith);
                }
            } catch (Exception e) {
            }

            s.covariateFile = covariateFile;

            // see if there is a genotype to expression couplings file
            try {
                genToExpCoupling = config.getString("datasets.dataset(" + i + ").genometoexpressioncoupling");
                if (settingsTextToReplace != null && genToExpCoupling.contains(settingsTextToReplace)) {
                    genToExpCoupling = genToExpCoupling.replace(settingsTextToReplace, settingsTextReplaceWith);
                }
            } catch (Exception e) {
            }

            s.genotypeToExpressionCoupling = genToExpCoupling;

            // quantile normalize the expression data?
            try {
                qnorm = config.getBoolean("datasets.dataset(" + i + ").quantilenormalize", false);
            } catch (Exception e) {
            }

            s.quantilenormalize = qnorm;

//                if (qnorm) {
//                    tmpDataset.quantileNormalize();
//                }
//
            // log2 transform the expression data?
            try {
                logtr = config.getBoolean("datasets.dataset(" + i + ").logtranform", false);
            } catch (Exception e) {
            }
            s.logtransform = logtr;

            dataset = null;
            i++;
            try {
                dataset = config.getString("datasets.dataset(" + i + ").name");
                if (settingsTextToReplace != null && dataset.contains(settingsTextToReplace)) {
                    dataset = dataset.replace(settingsTextToReplace, settingsTextReplaceWith);
                }

            } catch (Exception e) {
            }

            s.confineProbesToProbesMappingToAnyChromosome = confineProbesToProbesMappingToAnyChromosome;
            s.confineProbesToProbesThatMapToChromosome = confineProbesToProbesThatMapToChromosome;
            s.tsProbesConfine = tsProbesConfine;

        }
        // summarize();

    }

    public String summarize() {
        Date currentDataTime = new Date();
        String summary = "QTL mapping was performed using metaqtl version: " + Main.VERSION + "\nCurrent date and time: " + Main.DATE_TIME_FORMAT.format(currentDataTime) + "\n\n"
                + "Following settings will be applied:\n"
                + "Settings\n----\n"
                + "settingsTextToReplace\t" + settingsTextToReplace + "\n"
                + "settingsTextReplaceWith\t" + settingsTextReplaceWith + "\n"
                + "\nOutput\n----\n"
                + "createSNPPValueSummaryStatisticsFile\t" + createSNPPValueSummaryStatisticsFile + "\n"
                + "createEQTLPValueTable\t" + createEQTLPValueTable + "\n"
                + "outputReportsDir\t" + outputReportsDir + "\n"
                + "\nAnalysis\n----\n"
                + "performCiseQTLAnalysis\t" + cisAnalysis + "\n"
                + "performTranseQTLAnalysis\t" + transAnalysis + "\n"
                + "performParametricAnalysis\t" + performParametricAnalysis + "\n"
                + "performTwoPartModel\t" + performTwoPartModel + "\n"
                + "useAbsoluteZScorePValue\t" + useAbsoluteZScorePValue + "\n"
                + "ciseQTLAnalysMaxSNPProbeMidPointDistance\t" + ciseQTLAnalysMaxSNPProbeMidPointDistance + "\n"
                + "maxNrMostSignificantEQTLs\t" + maxNrMostSignificantEQTLs + "\n"
                + "performParametricAnalysisGetAccuratePValueEstimates\t" + performParametricAnalysisGetAccuratePValueEstimates + "\n"
                + "nrThreads\t" + nrThreads + "\n"
                + "fdrCutOff\t" + fdrCutOff + "\n"
                + "nrPermutationsFDR\t" + nrPermutationsFDR + "\n"
                + "regressOutEQTLEffectFileName\t" + regressOutEQTLEffectFileName + "\n"
                + "snpQCCallRateThreshold\t" + snpQCCallRateThreshold + "\n"
                + "snpQCHWEThreshold\t" + snpQCHWEThreshold + "\n"
                + "snpQCMAFThreshold\t" + snpQCMAFThreshold + "\n"
                + "\nConfinements\n----\n"
                + "performEQTLAnalysisOnSNPProbeCombinationSubset\t" + performEQTLAnalysisOnSNPProbeCombinationSubset + "\n"
                + "confineToSNPsThatMapToChromosome\t" + confineToSNPsThatMapToChromosome + "\n"
                + "expressionDataLoadOnlyProbesThatMapToChromosome\t" + expressionDataLoadOnlyProbesThatMapToChromosome + "\n"
                //                + "tsSNPsConfine\t" +tsSNPsConfine+ "\n"
                //                + "tsProbesConfine\t" +tsProbesConfine+ "\n"
                //                + "tsSNPProbeCombinationsConfine\t" +tsSNPProbeCombinationsConfine+ "\n"
                + "confineSNPsToSNPsPresentInAllDatasets\t" + confineSNPsToSNPsPresentInAllDatasets + "\n"
                + "confineProbesToProbesPresentInAllDatasets\t" + confineProbesToProbesPresentInAllDatasets + "\n"
                + "confineToProbesThatMapToChromosome\t" + confineToProbesThatMapToChromosome + "\n"
                + "expressionDataLoadOnlyProbesThatMapToChromosome\t" + expressionDataLoadOnlyProbesThatMapToChromosome + "\n"
                + "\n";

        if (tsProbesConfine != null) {
            summary += "Confining to: " + tsProbesConfine.size() + " probes from: " + strConfineProbe + "\n";
        }

        if (tsSNPProbeCombinationsConfine != null) {
            summary += "Confining to: " + tsSNPProbeCombinationsConfine.size() + " SNP-probe combinations from: " + strConfineSNPProbe + "\n";
        }

        if (tsSNPsConfine != null) {
            summary += "Confining to: " + tsSNPsConfine.size() + " SNPs  from: " + strConfineSNP + "\n";
        }

        summary += "\nDatasets\n----\n";

        for (TriTyperGeneticalGenomicsDatasetSettings settings : this.datasetSettings) {
            summary += "DatasetName\t" + settings.name + "\n";
            summary += "ExpressionData\t" + settings.expressionLocation + "\n";
            summary += "ExpressionPlatform\t" + settings.expressionplatform + "\n";
            summary += "LogTransform\t" + settings.logtransform + "\n";
            summary += "QuantileNormalize\t" + settings.quantilenormalize + "\n";
            summary += "GenotypeData\t" + settings.genotypeLocation + "\n";
            summary += "GTE\t" + settings.genotypeToExpressionCoupling + "\n";
            summary += "ProbaAnnotation\t" + settings.probeannotation + "\n";

        }

        return summary;

    }

    public void writeSettingsToDisk() throws Exception {
        TextFile tf = new TextFile(this.outputReportsDir + "/UsedSettings.txt", TextFile.W);
        tf.write(summarize());
        tf.close();
    }

    public void loadSNPProbeConfinement(String snpProbeConfine) throws IOException {
        strConfineSNPProbe = snpProbeConfine;
        TextFile in = new TextFile(snpProbeConfine, TextFile.R);
        tsSNPProbeCombinationsConfine = new HashMap<String, HashSet<String>>();
        String[] elems = in.readLineElemsReturnReference(TextFile.tab);
        System.out.println("Loading SNP Probe pairs from: " + snpProbeConfine);
        while (elems != null) {

            if (elems.length > 1) {

                String snp = new String(elems[0].getBytes("UTF-8"));
                snp = snp.trim();
                while (snp.startsWith(" ")) {
                    snp = snp.substring(1);
                }
                if ((tsSNPsConfine != null && tsSNPsConfine.contains(snp)) || tsSNPsConfine == null) {
                    HashSet<String> probes = tsSNPProbeCombinationsConfine.get(snp);
                    if (probes == null) {
                        probes = new HashSet<String>();
                    }
                    String probe = new String(elems[1].getBytes("UTF-8"));
                    probe = probe.trim();
                    while (probe.startsWith(" ")) {
                        probe = probe.substring(1);
                    }
                    if ((tsProbesConfine != null && tsProbesConfine.contains(probe)) || tsProbesConfine == null) {
                        probes.add(probe);
                    }
                    tsSNPProbeCombinationsConfine.put(snp, probes);
                }
            }
            elems = in.readLineElemsReturnReference(TextFile.tab);
        }
        in.close();
    }
}
