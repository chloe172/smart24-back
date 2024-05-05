package com.playit.backend.config;

import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.playit.backend.metier.model.DifficulteActivite;
import com.playit.backend.metier.model.MiniJeu;
import com.playit.backend.metier.model.Plateau;
import com.playit.backend.metier.model.Proposition;
import com.playit.backend.metier.model.Question;
import com.playit.backend.repository.PlateauRepository;

@Configuration
public class ExcelParser {

    @Bean
    CommandLineRunner parseExcel(PlateauRepository plateauRepository) {
        return args -> {
            System.out.println("Parsing excel file");

            String path = "Questions Play IT.xlsx";

            FileInputStream file = new FileInputStream(new File(path));
            Workbook workbook = new XSSFWorkbook(file);
            for (Sheet sheet : workbook) {
                String sheetName = sheet.getSheetName();
                Plateau plateau = new Plateau(sheetName);

                Iterator<Row> rowIterator = sheet.iterator();
                rowIterator.next(); // Skip the header
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    String type = getCellAString(row.getCell(0));
                    String intitule = getCellAString(row.getCell(1));
                    String niveau = getCellAString(row.getCell(2));
                    String reponse1 = getCellAString(row.getCell(3));
                    String reponse2 = getCellAString(row.getCell(4));
                    String reponse3 = getCellAString(row.getCell(5));
                    String reponse4 = getCellAString(row.getCell(6));
                    List<String> propositions = List.of(reponse1, reponse2, reponse3, reponse4);
                    String bonneReponse = getCellAString(row.getCell(7));
                    String explication = getCellAString(row.getCell(8));
                    // Lire la premiere colonne, selon le type, créer la question ou le mini-jeu

                    switch (type.toLowerCase()) {
                        case "question":
                            DifficulteActivite difficulte = DifficulteActivite.getDifficulteFromString(niveau);
                            Question question = new Question();
                            question.setDifficulte(difficulte);
                            question.setExplication(explication);
                            question.setIntitule(intitule);
                            question.setNumeroActivite(row.getRowNum());

                            for (String proposition : propositions) {
                                if (proposition.equals("")) {
                                    break;
                                }
                                Proposition p = new Proposition(proposition);
                                question.addProposition(p);
                            }

                            if (!propositions.stream().anyMatch(p -> p.equals(bonneReponse))) {
                                throw new IllegalArgumentException(
                                        path + " : La bonne réponse n'est pas dans les propositions "
                                                + bonneReponse
                                                + " - line "
                                                + row.getRowNum());
                            }
                            Proposition bonneProposition = question.getListePropositions().stream()
                                    .filter(p -> p.getIntitule().equals(bonneReponse)).findFirst().get();
                            question.setBonneProposition(bonneProposition);

                            plateau.addActivite(question);
                            System.out.println(path + " : Question " + intitule + " ajoutée");
                            break;

                        case "mini jeu":
                            MiniJeu miniJeu = new MiniJeu();
                            miniJeu.setDifficulte(DifficulteActivite.getDifficulteFromString(niveau));
                            miniJeu.setIntitule(intitule);
                            miniJeu.setNumeroActivite(row.getRowNum());
                            miniJeu.setCode(reponse1);

                            plateau.addActivite(miniJeu);
                            System.out.println(path + " : Mini jeu " + intitule + " ajouté");
                            break;

                        case "":
                            break;
                        default:
                            throw new IllegalArgumentException(path + " : Invalid type value " + type);
                    }
                }
                plateauRepository.saveAndFlush(plateau);
            }
            workbook.close();
            file.close();

        };
    }

    private String getCellAString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();

            case NUMERIC: {
                Double numericValue = cell.getNumericCellValue();
                if (numericValue >= 0 && numericValue <= 1) { // Pourcentage
                    int percentage = (int) (numericValue * 100);
                    return String.valueOf(percentage) + "%";
                } else {
                    String format;
                    if (numericValue % 1 == 0) {
                        format = "#";
                    } else {
                        format = "#.##";
                    }
                    return new DecimalFormat(format).format(numericValue);
                }
            }

            case BOOLEAN:
                return cell.getBooleanCellValue() ? "Vrai" : "Faux";

            case BLANK:
                return "";

            case FORMULA:
                return cell.getCellFormula();

            default:
                throw new IllegalArgumentException("Invalid cell type " + cell.getCellType());
        }
    }

}
