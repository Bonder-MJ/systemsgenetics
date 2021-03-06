package nl.systemsgenetics.functionenrichmentoftransqtls;

import cern.colt.function.tdouble.DoubleFunction;
import umcg.genetica.io.text.TextFile;
import umcg.genetica.math.matrix2.DoubleMatrixDataset;

import java.io.IOException;
import java.util.*;

public class ConvertToChiSq {
	
	
	public void run(String traitfile, String zscoreloc, String outputloc, int nrperm) throws IOException, Exception {
		
		// we need to convert traits->set of snps
		HashMap<String, HashSet<String>> snptotrait = readTraitFile(traitfile);
		HashMap<String, LinkedHashSet<String>> traitToSnp = new HashMap<>();
		
		for (String key : snptotrait.keySet()) {
			HashSet<String> traits = snptotrait.get(key);
			for (String trait : traits) {
				
				LinkedHashSet<String> snps = traitToSnp.get(trait);
				if (snps == null) {
					snps = new LinkedHashSet<>();
					traitToSnp.put(trait, snps);
				}
				snps.add(key);
			}
		}
		
		System.out.println(traitToSnp.size() + " traits loaded from: " + traitfile);
		
		// for each permutation
		HashSet<String> colsToExclude = new HashSet<>();
		colsToExclude.add("Alleles");
		colsToExclude.add("AlleleAssessed");
		
		
		for (int perm = 0; perm < nrperm; perm++) {
			
			
			String filename = zscoreloc + "ZScoreMatrix-Permutation" + perm + ".txt.gz";
			String outputfilename = outputloc + "TraitChiSquareMatrix-Permutation" + perm + ".txt.gz";
			if (perm == 0) {
				filename = zscoreloc + "ZScoreMatrix.txt.gz";
				outputfilename = outputloc + "TraitChiSquareMatrix.txt.gz";
			}
			System.out.println("Perm " + perm + " infile: " + filename);
			System.out.println("Output " + outputfilename);
			
			// load z-score matrix
			DoubleMatrixDataset<String, String> zmat = DoubleMatrixDataset.loadDoubleTextDoubleDataExlcudeCols(filename, '\t', colsToExclude);
			
			// replace column names
			ArrayList<String> colnames = zmat.getColObjects();
			LinkedHashMap<String, Integer> colHash = new LinkedHashMap<>();
			for (int c = 0; c < colnames.size(); c++) {
				String name = colnames.get(c);
				name = name.split("_")[0];
				colHash.put(name, c);
			}
			zmat.setHashCols(colHash);
			
			LinkedHashMap<String, Integer> traitindex = new LinkedHashMap<>();
			int ctr = 0;
			for (Map.Entry<String, LinkedHashSet<String>> set : traitToSnp.entrySet()) {
				traitindex.put(set.getKey(), ctr);
				ctr++;
			}
			
			DoubleFunction square = new DoubleFunction() {
				@Override
				public double apply(double v) {
					return (v * v);
				}
			};
			zmat.getMatrix().assign(square);
			
			DoubleMatrixDataset<String, String> output = new DoubleMatrixDataset<>(zmat.getHashCols(), traitindex);
			DoubleMatrixDataset<String, String> outputct = new DoubleMatrixDataset<>(zmat.getHashCols(), traitindex);
			
			// combine trait snps into single column
			for (Map.Entry<String, LinkedHashSet<String>> set : traitToSnp.entrySet()) {
				String trait = set.getKey();
				int colindex = traitindex.get(trait);
				LinkedHashSet<String> snps = set.getValue();
				LinkedHashSet<String> snpselect = new LinkedHashSet<>();
				for (String snp : snps) {
					if (zmat.containsRow(snp)) {
						snpselect.add(snp);
					}
				}
				DoubleMatrixDataset subzmat = zmat.viewRowSelection(snpselect);
				for (int col = 0; col < subzmat.columns(); col++) {
					double sum = 0;
					int nrsnps = 0;
					for (int row = 0; row < subzmat.rows(); row++) {
						double val = subzmat.getElementQuick(row, col);
						if (!Double.isNaN(val)) {
							sum += val;
							nrsnps++;
						}
					}
					
					output.getMatrix().setQuick(col, colindex, sum);
					outputct.getMatrix().setQuick(col, colindex, nrsnps);
				}
			}
			
			// write resulting matrix
			output.save(outputfilename);
			output.save(outputfilename + "-nrSNPs.txt.gz");
		}
		
	}
	
	public HashMap<String, HashSet<String>> readTraitFile(String traitfile) throws IOException {
		TextFile tf2 = new TextFile(traitfile, TextFile.R);
		tf2.readLine();
		HashMap<String, HashSet<String>> snptotrait = new HashMap<>();
		String[] elems = tf2.readLineElems(TextFile.tab);
		while (elems != null) {
			String snp = elems[0];
			String traits = elems[5];
			String[] traitelems = traits.split("; ");
			
			HashSet<String> snpset = snptotrait.get(snp);
			if (snpset == null) {
				snpset = new HashSet<>();
			}
			for (String trait : traitelems) {
				if (trait.trim().length() > 0) {
					snpset.add(trait);
				}
			}
			snptotrait.put(snp, snpset);
			
			
			elems = tf2.readLineElems(TextFile.tab);
		}
		tf2.close();
		System.out.println(snptotrait.size() + " trait snps ");
		return snptotrait;
	}
}
