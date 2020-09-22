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
			out.write("##maf version=1 \n" +
					"# original dump date: Fri Jun 05 14:22:00 2020\n# ensembl release: 59\n" +
					"# emf comment: Alignments: 11 eutherian mammals EPO\n# emf comment: Region:" +
					" Homo sapiens chromosome:GRCh37\n");

			while (input != args.length) {
				String file = args[input];
				BufferedReader entry = new BufferedReader(new FileReader(file));
				String line;
				while ((line = entry.readLine()) != null) {

					if (line.length() != 0 && line.charAt(0) == 'a') {
						out.write(line);
					} else if (line.length() != 0 && line.charAt(0) == 's') {
						LinkedHashMap<String, String[]> speciesSequences = new LinkedHashMap<>();
						ArrayList<String[]> duplicateSequences = new ArrayList<>();
						while (line.length() != 0 && line.charAt(0) == 's') {
							String[] arraySequenceInfo = line.split("\\s+");
							String nameSpeciesWithChro = arraySequenceInfo[1];
							String nameSpeciesOnly = nameSpeciesWithChro.substring(0,
									nameSpeciesWithChro.indexOf("."));
							if (!nameSpeciesOnly.equals("ancestral_sequences")) {
								if (!(speciesSequences.containsKey(nameSpeciesOnly))) {
									speciesSequences.put(nameSpeciesOnly, arraySequenceInfo);
								} else {
									if (duplicateSequences.size() == 0) {
										duplicateSequences.add(speciesSequences.get("homo_sapiens"));
									}
									duplicateSequences.add(arraySequenceInfo);
								}
							}
							line = entry.readLine();
							if (line == null)
								System.exit(0);
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


							for (String key : speciesSequences.keySet()) {
								String [] value = speciesSequences.get(key);
								ArrayList<String> value2 = new ArrayList<>(Arrays.asList(value));
								int lengthAlig = (value2.get(6)).length();
								if (lengthAlig >= 120000) {
									int indexCut = value2.get(6).indexOf(
											"------------------",lengthAlig/2);
									value2.add(value2.get(6).substring(0, indexCut + 10));
									value2.add("\n");
									value2.add(value2.get(6).substring(indexCut));
									value2.remove(6);
								}


								String eachSpeciesInfo = "";
								for (int counter = 0; counter < value2.size(); counter++) {
									if(counter!= 6) {
										eachSpeciesInfo += value2.get(counter) + "\t";
									} else {
										eachSpeciesInfo += value2.get(counter);
									}
								}

								out.write(eachSpeciesInfo + "\n");
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
