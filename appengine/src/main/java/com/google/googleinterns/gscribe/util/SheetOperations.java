package com.google.googleinterns.gscribe.util;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SheetOperations {

    public String clearSheet ( Sheets service, String spreadsheetId, String sheetName) throws IOException {
        ClearValuesRequest requestBody = new ClearValuesRequest();
        Sheets.Spreadsheets.Values.Clear request = service.spreadsheets().values().clear(spreadsheetId, "Sheet3", requestBody);
        ClearValuesResponse response = request.execute();
        return "";
    }

    public List<String> getAllSheetsTitle (Sheets service, String spreadsheetId ) throws IOException {
        Sheets.Spreadsheets.Get spreadsheet = service.spreadsheets().get(spreadsheetId);
        Spreadsheet sheet = spreadsheet.execute();
        List<Sheet> sheets = sheet.getSheets();
        List<String> sheetsTitle = new ArrayList<>();
        for ( int i=0; i<sheets.size(); i++ ){
            sheetsTitle.add( sheets.get(i).getProperties().getTitle() );
        }
        return sheetsTitle;
    }

    private int getNumberOfQuestions ( Sheets service, String spreadsheetId ) throws IOException {
        final String noOfQuestionsChecker = "A:A";
        ValueRange response = service.spreadsheets().values().get(spreadsheetId, noOfQuestionsChecker).execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            return 0;
        } else {
            return values.size();
        }
    }

    public String processQuestionPaper ( Sheets service, String spreadsheetId ) throws IOException {

        int numberOfQuestions = getNumberOfQuestions( service, spreadsheetId );
        final String range = "A2:G" + Integer.toString( numberOfQuestions );
        ValueRange response = service.spreadsheets().values().get(spreadsheetId,range).execute();
        List<List<Object>> values = response.getValues();
        validateQuestionPaper checkerInstance = new validateQuestionPaper();
        boolean isPaperCorrect = checkerInstance.isCorrect( values );
        if ( !isPaperCorrect ){
            List<List<Object>> allWrites = new ArrayList<List<Object>>();
            List<Object> writeBack = new ArrayList<Object>();
            writeBack.add( "Timestamp");
            writeBack.add( "RollNumber");
            for ( int i=0; i<values.size(); i++ ){
                writeBack.add( values.get(i).get(1) );
            }
            writeBack.add("Final Points");
            allWrites.add(writeBack);
            ValueRange body = new ValueRange()
                    .setValues(allWrites);
            UpdateValuesResponse result =
                    service.spreadsheets().values().update(spreadsheetId, "Sheet3!A10", body)
                            .setValueInputOption("USER_ENTERED")
                            .execute();
            System.out.printf("%d cells updated.", result.getUpdatedCells());
        }
        return "";
    }

}
