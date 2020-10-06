import java.util.*;import java.io.*;
/**
 * This program filters multiple sequence alignments and remove duplicates species within alignment blocks
 * @author Martin Smith on 7/07/09
 * Modified by Vanda Gaonach-Lovejoy 05/06/20
 * Copyright 2009 __MyCompanyName__. All rights reserved.
 */

public class MergeNFilter {

	public static void main(String[] args) throws IOException {


		if (args.length == 0) {
			System.out.println("Warning: maf files are expected");
			System.exit(-1);
		}

		try {

			int input = 0;
			BufferedWriter segDups = new BufferedWriter(new FileWriter(args[0].substring(0,
					args[0].lastIndexOf("_")) + "_segmtl_dups.txt"));
			BufferedWriter out = new BufferedWriter(new FileWriter(args[0].substring(0, args[0].lastIndexOf(
					"_")) + ".maf"));
			;
			out.write("##maf version=1 \n" +
					"# original dump date: Fri Jun 05 14:22:00 2020\n# ensembl release: 59\n" +
					"# emf comment: Alignments: 11 eutherian mammals EPO\n# emf comment: Region:" +
					" Homo sapiens chromosome:GRCh37\n");

			while (input != args.length) {
				String file = args[input];
				BufferedReader entry = new BufferedReader(new FileReader(file));
				String line;
				while ((line = entry.readLine()) != null) {

					if(line.length() != 0 && line.charAt(0) == 's') {
						LinkedHashMap<String, String[]> speciesSequences = new LinkedHashMap<>();
						ArrayList<String[]> duplicateSequences = new ArrayList<>();
						while (line.length() != 0 && line.charAt(0) == 's') {
							String[] arraySequenceInfo = line.split("\\s+");
							String nameSpeciesWithChro = arraySequenceInfo[1];
							String nameSpeciesOnly = nameSpeciesWithChro.substring(0,
									nameSpeciesWithChro.indexOf("."));
							if (!nameSpeciesOnly.equals("ancestral_sequences")) {
								if (!(speciesSequences.containsKey(nameSpeciesOnly))) {
									//String nullTerminated = arraySequenceInfo[6] + "\0";
									//arraySequenceInfo[6] = nullTerminated;
									speciesSequences.put(nameSpeciesOnly, arraySequenceInfo);
								} else {
									/*if (duplicateSequences.size() == 0) {
										duplicateSequences.add(speciesSequences.get("homo_sapiens"));
									}*/
									duplicateSequences.add(arraySequenceInfo);
								}
							}
							line = entry.readLine();
							if (line == null)
								System.exit(0);
						}
						if (speciesSequences.size() == 1) {
							duplicateSequences.add((speciesSequences.get("homo_sapiens")));
						}

						for (int i = 0; i < duplicateSequences.size(); i++) {
							String[] arraySpecies = duplicateSequences.get(i);
							if (i == 0) {
								segDups.write("\na score=0\n");
							}
							String stringSpecies = Arrays.toString(arraySpecies);
							String noBrackets = stringSpecies.replace("[", "")
									.replace("]", "")
									.replace(",", "\t");


							segDups.write(noBrackets + "\n");
						}

						if (speciesSequences.size() > 1){
							out.write( "a" +"\n");
							for (String key : speciesSequences.keySet()) {
								String[] value = speciesSequences.get(key);
								String eachSpeciesInfo = Arrays.toString(value);

								//remove the right and left bracket
								String noBrackets = eachSpeciesInfo.replace("[", "")
										.replace("]", "")
										.replace(",", "\t");
								System.out.print(noBrackets+ "\0"+ "\n");
								out.write(noBrackets + "\0");
							}
						}
					}
					out.write("\n");
				}
				input++;
				entry.close();
			}

			out.close();
			segDups.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}