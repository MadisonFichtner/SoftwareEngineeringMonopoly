package src;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class GUIHelper {
    private static MonopolyController monopoly;
    private static final String COMMA_DELIMITER = ",";
    private static int users = 4;
    private static Player[] players = new Player[users];
    private static Board board;
    private static int playerTurn = 0;
    private static String boardMode;


    public static void setBoardMode(String boardMode) {
        GUIHelper.boardMode = boardMode;
    }


    public static void setMessage(String string) {
        monopoly.message.setText(string);
    }

    public static void setMonopoly(MonopolyController monopolyCont) {
        monopoly = monopolyCont;
    }

    public static void enableRollGUI() {
        monopoly.disableButtons();
    }

    public static void enableTurnGUI() {
        monopoly.enableButtons();
    }

    public static void moveTokenImg(int player_num, int spot) {
        try {
            monopoly.moveToken(player_num, spot);
        } catch (Exception e) {
            System.out.println("You might be testing, otherwise your GUI has crashed");
            e.printStackTrace();
        }
    }

    public static int getNumPlayers() {
        return players.length;
    }

    public static Player getPlayer(int index) {
        return players[index];
    }

    public static Player[] getPlayers() {
        return players;
    }

    public static Board getBoard() {
        return board;
    }

    public static void openWindow(String name) {
        try {
            Parent root = FXMLLoader.load(Board.class.getResource(name + ".fxml"));
            Stage trade_stage = new Stage();
            trade_stage.setTitle(name);
            trade_stage.setScene(new Scene(root));
            trade_stage.show();
        } catch (Exception e) {
            System.out.println("Something went wrong when opening " + name + " window.");
        }
    }

    public static void nextTurn() {
        enableRollGUI();
        playerTurn++;
        if (playerTurn == users) {
            playerTurn = 0;
        }
    }

    public static void takeTurn() {
        board.beginTurn(players[playerTurn]);
    }

    //MAKE RECEIVE MONOPOLY BOARD OR MSU BOARD
    //USE APPROPRIATE BOARD.JPG, DEEDS.CSV AND CARDS.CSV
    //is called by users.fxml
    public static void setPlayers(CharSequence[] users) {

        String deedsFileName;
        String communityFileName;
        String chanceFileName;
        
        switch (boardMode) {
            case "msu":
                System.out.println(boardMode);
                deedsFileName = "MSUdeeds.csv";
                communityFileName = "MSUChestCards.csv";
                chanceFileName = "MSUChanceCards.csv";
                monopoly.setBoardImage("MSUboard.png");
                break;
            case "english":
                System.out.println(boardMode);
                deedsFileName = "monopolyDeeds.csv";
                communityFileName = "monopolyChestCards.csv";
                chanceFileName = "monopolyChanceCards.csv";
                monopoly.setBoardImage("board.jpg");
                break;
            default:
                System.out.println("Invalid mode: defaulted to msu");
                deedsFileName = "MSUdeeds.csv";
                communityFileName = "MSUChestCards.csv";
                chanceFileName = "MSUChanceCards.csv";
                monopoly.setBoardImage("MSUboard.png");
                break;
        }


        Deed[] deeds = new Deed[40];
        ArrayList<Card> communityChest = new ArrayList<Card>();
        ArrayList<Card> chance = new ArrayList<Card>();

        //if(users choice == monopoly)
        //String deedsFileName = "monopolyDeeds.csv";
        //else
        File csvDeedsFile = new File(deedsFileName);
        try {
            deeds = parseDeedsCSV(csvDeedsFile);
        } catch (FileNotFoundException e) {
            System.out.println("No CSV with that name found!");
        }

        //if(users choice == monopoly)
        //String communityFileName = "monopolyChestCards.csv";
        //else
        File csvCommunityFile = new File(communityFileName);
        try {
            communityChest = parseCommunityCSV(csvCommunityFile);
        } catch (FileNotFoundException e) {
            System.out.println("No CSV with that name found!");
        }

        //if(users choice == monopoly)
        //String chanceFileName = "monopolyChanceCards.csv";
        //else
        File csvChanceFile = new File(chanceFileName);
        try {
            chance = parseChanceCSV(csvChanceFile);
        } catch (FileNotFoundException e) {
            System.out.println("No CSV with that name found!");
        }

        for (int i = 0; i < users.length; i++) {
            players[i] = new Player(users[i].toString(), "token" + 1, i);
        }

        //Randomizing cards
        Collections.shuffle(communityChest);
        Collections.shuffle(chance);

        //Board now takes in the communityChest and chance cards
        board = new Board(players, deeds, communityChest, chance);
    }

    /*
     * Parses the inputted .csv file to populate the game board with deeds
     */
    public static Deed[] parseDeedsCSV(File csv_file) throws FileNotFoundException {
        Deed[] deeds = new Deed[40];
        Scanner in = new Scanner(csv_file);
        in.useDelimiter(COMMA_DELIMITER);
        for (int i = 0; i < 40; i++) {
            int position = in.nextInt();
            String name = in.next();
            int property_group = in.nextInt();
            String color = in.next();
            int price = in.nextInt();
            int mortgage_value = in.nextInt();
            int rent = in.nextInt();
            int rent1 = in.nextInt();
            int rent2 = in.nextInt();
            int rent3 = in.nextInt();
            int rent4 = in.nextInt();
            int rent_hotel = in.nextInt();
            int build_cost = in.nextInt();
            in.next();
            String deed_type = in.next();

            deeds[i] = new Deed(position, name, property_group, color, price, mortgage_value, rent, rent1, rent2, rent3,
                    rent4, rent_hotel, build_cost, deed_type);
        }
        // populate deeds giving csv_file
        in.close();
        return deeds;
    }

    public static ArrayList<Card> parseCommunityCSV(File csv_file) throws FileNotFoundException {
        //Card[] community = new Card[16];
        ArrayList<Card> community = new ArrayList<Card>();
        Scanner in = new Scanner(csv_file);
        in.useDelimiter(COMMA_DELIMITER);
        for (int i = 0; i < 16; i++) {
            String name = in.next();
            int type = in.nextInt();
            in.next();

            Card card = new Card(name, type);
            community.add(card);
        }
        in.close();
        return community;
    }

    public static ArrayList<Card> parseChanceCSV(File csv_file) throws FileNotFoundException {
        //Card[] chance = new Card[15];
        ArrayList<Card> chance = new ArrayList<Card>();
        Scanner in = new Scanner(csv_file);
        in.useDelimiter(COMMA_DELIMITER);
        for (int i = 0; i < 15; i++) {
            String name = in.next();
            int type = in.nextInt();
            in.next();

            Card card = new Card(name, type);
            chance.add(card);
        }
        in.close();
        return chance;
    }
}
