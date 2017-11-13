package test;

import java.util.Arrays;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Board {

	public int numHotelsRemaining;
	public int numHousesRemaining;

	public static Deed[] board = new Deed[40];
	public static Player current;
	public static int position;
	public static int[] bids = {0, 0, 0, 0};
	public static int highest_bid = 50;
	private static boolean is_free_parking;// Is true of player lands on GO, Jail, or Free Parking
	private static int double_roll_counter; // Keeps track of how many times player has rolled doubles
	private static boolean was_in_jail;

	public Board(Player[] users, Deed[] new_board) {
		this.numHotelsRemaining = 12;
		this.numHousesRemaining = 32;
		board = new_board;
	}

	// Reset all the variables for a players turn and gives them the option to roll
	// if they are not in jail
	// The ui for escaping jail still needs to be created and connected to
	// moveToSpace()
	public void start_turn(Player player) {
		current = player;
		Main.monopoly.set_message("It is " + current.name + "'s turn.");
		position = current.position;
		is_free_parking = false;// Is true of player lands on GO, Jail, or Free Parking
		double_roll_counter = 0; // Keeps track of how many times player has rolled doubles
		was_in_jail = false;
		if (player.in_jail == true) {
			// give option to roll dice for doubles or pay $50
			player.turns_in_jail++;
			if(player.turns_in_jail >= 3) {
				player.pay_bail();
			} else {
				try {
					Parent root = FXMLLoader.load(Board.class.getResource("jail.fxml"));
					Stage trade_stage = new Stage();
					trade_stage.setTitle("jail");
					trade_stage.setScene(new Scene(root));
					trade_stage.show();
				} catch (Exception e) {
					System.out.println("Something went wrong");
				}
			}
		} else {
			moveToSpace(false);
		}
	}

	// Move the player and deals with what happens at that space. If the player
	// started the turn in jail, don't roll.
	// The buy Property ui hasn't been created or connected to auctionProperty()
	public static void moveToSpace(boolean fromJail) {
		if (fromJail && current.in_jail == true) {
			current.roll_dice();
			
			if (current.dice[0] == current.dice[1]) // If doubles are rolled
			{
				was_in_jail = true;
				Main.monopoly.set_message("You rolled doubles. Gratz.");
				current.in_jail = false;
				current.turns_in_jail = 0;
				current.move();
			} else // If doubles are not rolled
			{
				Main.monopoly.set_message("You did not roll doubles...");
				Main.monopoly.enable_buttons();
				return;
			}
		} else {
			current.roll_dice(); // Player rolls dice, dice values are stored in player class
			current.move();
		}

		position = current.position;

		// If player lands on free parking, GO, or Jail set is_free_parking to true, and
		// there is no property to buy
		if (board[position].name.equals("Free Parking") || board[position].name.equals("Jail")
				|| board[position].name.equals("GO")) {
			if (current.in_jail == false) {
				System.out.println("No property to buy. Landed on space is " + board[position].name + "\n");
				Main.monopoly.set_message("No property to buy. " + current.name + " landed on " + board[position].name);
			}
		}

		// If player lands on "Go to Jail" send the player to jail
		else if (board[position].name.equals("Go to Jail")) {
			current.move_to_jail();
			Main.monopoly.set_message("You're going to jail!");
		}

		// If player lands on "Income Tax", give them the option to pay $200 or %10 of
		// their net worth (without letting them calculate net worth prior to
		else if (board[position].name.equals("Income Tax")) {
			System.out.println("You landed on Income Tax, you piece of shit have to pay 10% of your net worth, or $200. (1. 10% / 2. $200)");
			Main.monopoly.set_message("You have to pay income tax.");
			// int answer = in.nextInt(); Fix this with a a prompt later
			current.pay_tax(1);
		}
		// If player lands on property owned by somebody else, they pay rent
		else if (board[current.position].owner != null) {
			Deed deed = board[current.position];
			current.pay_rent(deed);
		}

		// If there is no owner of the deed landed on and it's not free parking, allow
		// user to buy the property, or it will be auctioned
		else if (board[current.position].owner == null) {// && is_free_parking == false)
			if (current.money > board[current.position].purchase_price) {
				try {
					Parent root = FXMLLoader.load(Board.class.getResource("purchase.fxml"));
					Stage trade_stage = new Stage();
					trade_stage.setTitle("purchase");
					trade_stage.setScene(new Scene(root));
					trade_stage.show();
				} catch (Exception e) {
					System.out.println("Something went wrong");
				}
			} else {
				auctionProperty();
			}

			// boolean bought = false;
			// bought = current.buy_property(deed);

		}
		check_for_doubles();
	}

	// This determines if an auction should be held after a player has had the
	// chance to buy it
	// The ui for auction has been started but the code has not.
	// After the auction is done Main.test.enableButtons() need to be called.
	public static void auctionProperty() {
		if (current.money < board[current.position].purchase_price) {
			Main.monopoly.set_message(current.name + " did not have enough money to buy " + board[position].name + " so "
					+ board[position].name + " will be auctioned.");
		} else {
			Main.monopoly.set_message(current.name + " did not buy " + board[position].name + " so " + board[position].name
					+ " will be auctioned.");
		}
		try {
			Parent root = FXMLLoader.load(Board.class.getResource("auction.fxml"));
			Stage trade_stage = new Stage();
			trade_stage.setTitle("auction");
			trade_stage.setScene(new Scene(root));
			trade_stage.show();
		} catch (Exception e) {
			System.out.println("Something went wrong with auctioning");
		}
	}

	public static void check_for_doubles() {
		// Doubles were rolled, does the same thing as above, just repeats if doubles
		// are rolled
		if (was_in_jail == false && current.dice[0] == current.dice[1]) {
			double_roll_counter++;
			if (double_roll_counter == 2) { // if doubles have been rolled twice, go to jail
				current.move_to_jail();
			} else {
				moveToSpace(false);
			}
		}
		Main.monopoly.enable_buttons();
	}
	
	public static void gui_auction(int[] bids, int highest_bid, Deed deed) {
		int displayed_highest = highest_bid;
		int players_interested = 0;
		for(int i = 0; i < bids.length; i++) {
			if(bids[i] >= highest_bid) {
				highest_bid = bids[i];
				players_interested++;
			} else if(bids[i] > displayed_highest) {
				players_interested++;
			}
		}
		if(players_interested == 1) {
			for(int i = 0; i < bids.length; i++) {
				if(highest_bid == bids[i]) {
					Arrays.fill(bids, 0);
					Main.monopoly.set_message(monopoly_controller.players[i].name + " won " + deed.name + " for $" + highest_bid);
					monopoly_controller.players[i].buy_auction(deed, highest_bid);
					highest_bid = 50;
				}
			}
		} else if(players_interested > 1) {
			Board.highest_bid = highest_bid;
			Board.bids = bids;
			try {
				Parent root = FXMLLoader.load(Board.class.getResource("auction.fxml"));
				Stage trade_stage = new Stage();
				trade_stage.setTitle("Auction");
				trade_stage.setScene(new Scene(root));
				trade_stage.show();
			} catch (Exception e) {
				System.out.println("Something went wrong");
			}
		} else {
			highest_bid = 50;
		}
	}

	public Player game_over() {
		int current_high = -1;
		int high_player = -1;
		for (int i = 0; i < monopoly_controller.players.length; i++) {
			monopoly_controller.players[i].calculate_net_worth();
			if (monopoly_controller.players[i].overall_net_worth > current_high) {
				high_player = i;
				current_high = monopoly_controller.players[i].overall_net_worth;
			}
		}
		System.out.println(monopoly_controller.players[high_player].name + " is the winner with a net worth of: " + current_high);
		return monopoly_controller.players[high_player];
	}
}
