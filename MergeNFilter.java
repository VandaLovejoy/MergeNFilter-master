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

					if (line.length() != 0 && line.charAt(0) == 's') {
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

						boolean toolong = true;
						int counter = 0;
						int indexCut = 0;
						String[] value = speciesSequences.get("homo_sapiens");
						int lengthAlig = value[6].length();

						while (toolong && speciesSequences.size() > 1) {
							out.write("a" + "\n");
							if (lengthAlig <= 160000) {
								toolong = false;
							} else {
								indexCut = value[6].indexOf("------------------", lengthAlig / 2);
							}
							String[] mammal;
							ArrayList<String> value2 = new ArrayList<>();
							for (String key : speciesSequences.keySet()) {
								mammal = speciesSequences.get(key);
								value2 = new ArrayList<>(Arrays.asList(mammal));
								if (counter == 0 && toolong) {
									value2.add(value2.get(6).substring(0, indexCut + 10));
									value2.remove(6);
								} else if (counter == 1) {
									value2.add(value2.get(6).substring(indexCut));
									value2.remove(6);
								}
								String eachSpeciesInfo ="";
								for (int i = 0; i < value2.size(); i++) {
									if(i!= (value2.size()-1)) {
										eachSpeciesInfo += (value2.get(i)) + "\t";
									} else {
										eachSpeciesInfo += (value2.get(i));
									}
								}
									/*//remove the right and left bracket
									String noBrackets = eachSpeciesInfo.replace("[", "")
											.replace("]", "")
											.replace(",", "\t");
*/
									out.write(eachSpeciesInfo + "\n");

							}
							/*for (int i = 0; i < value2.size(); i++) {
								String eachSpeciesInfo = (value2.get(i));

								//remove the right and left bracket
								String noBrackets = eachSpeciesInfo.replace("[", "")
										.replace("]", "")
										.replace(",", "\t");

								out.write(noBrackets + "\n");*/
								if(counter ==1 ){
									toolong =false;
								} else if ( counter == 0 && toolong){
								counter ++;
								}


						}
						out.write("\n");
					}
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
