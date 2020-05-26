package com.google.googleinterns.gscribe.util;

import java.util.List;

public class validateQuestionPaper {

    boolean validPoints ( String pointsAllotted ){
        try {
            int points = Integer.parseInt(pointsAllotted);
            if ( points<=0 || points>100 ){
                System.out.print("Points for the question not a valid range");
                return false;
            }
        } catch ( NumberFormatException e ){
            System.out.print("Points for the question not a valid integer");
            return false;
        }
        return true;
    }

    boolean checkMCQQuestion ( List<Object> question ){
        boolean questionCorrect = true;
        if ( question.get(1).equals("") ) {
            questionCorrect = false;
            System.out.print("Question statement missing ");
        }
        for ( int i=2; i<6; i++ ){
            if ( question.get(i).equals("") ) {
                questionCorrect = false;
                System.out.print("Question option missing ");
            }
        }
        questionCorrect = questionCorrect & validPoints( (String) question.get(6) );
        System.out.println();
        return questionCorrect;
    }

    boolean checkSubjectiveQuestion ( List<Object> question ){
        boolean questionCorrect = true;
        if ( question.get(1).equals("") ) {
            questionCorrect = false;
            System.out.print("Question statement missing ");
        }
        for ( int i=2; i<6; i++ ){
            if ( ! question.get(i).equals("") ) {
                questionCorrect = false;
                System.out.print("Question option extra ");
            }
        }
        questionCorrect = questionCorrect & validPoints( (String) question.get(6) );
        System.out.println();
        return questionCorrect;
    }

    boolean isCorrect( List<List<Object>> paper ){
        int totalQuestions = paper.size();
        boolean paperCorrect = true;
        for ( int i=0; i<totalQuestions; i++ ){
            List<Object> currentQuestion = paper.get(i);
            if ( currentQuestion.size()!=7 ) {
                System.out.println("Question " + i + "fields are not 7");
                paperCorrect = false;
            } else {
                if  ( currentQuestion.get(0).equals("1") ){
                    paperCorrect = paperCorrect & checkMCQQuestion ( currentQuestion );
                } else if ( currentQuestion.get(0).equals("0") ){
                    paperCorrect = paperCorrect & checkSubjectiveQuestion ( currentQuestion );
                } else {
                    paperCorrect = false;
                    System.out.println( "Wrong value at isMCQ field" );
                }
            }
        }
        return paperCorrect;
    }

}
