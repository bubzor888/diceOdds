import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiceOdds {

  private static final int NUM_OF_DICE = 2;

  public static void main(String[] args) {

    int numberOfDice = NUM_OF_DICE;
    BigInteger totalPossibilities = BigInteger.valueOf(Dice.values().length);
    totalPossibilities = totalPossibilities.pow(numberOfDice);

    System.out.println("Calculating the odds of " + totalPossibilities + " rolls.");

    // Get a set of dice all set to the 0 side
    int[] dice = setupDice(numberOfDice);
    Result result = new Result();

    while (true) {
      // Need to make a "roll" with each side of the dice
      List<Dice> roll = new ArrayList<Dice>();
      for (int i = 0; i < dice.length; i++) {
        roll.add(Dice.values()[dice[i]]);
      }

      result = processResult(roll, result);

      // Dice 0 is the one that will be incrementing most of the time
      if (dice[0] + 1 < Dice.values().length) {
        dice[0]++;
      } else {
        // We need to find the right dice to increment
        int nextDice = -1;
        for (int i = 1; i < NUM_OF_DICE; i++) {
          if (dice[i] + 1 < Dice.values().length) {
            nextDice = i;
            break;
          }
        }

        // If there's no dice to increment, we're done
        if (nextDice == -1) {
          break;
        }

        // Otherwise increment next dice, and then clear the dice before it
        dice[nextDice]++;
        for (int i = 0; i < nextDice; i++) {
          dice[i] = 0;
        }
      }
    }

    result.print();
  }

  private static int[] setupDice(int numberOfDice) {
    int[] dice = new int[numberOfDice];

    for (int i = 0; i < numberOfDice; i++) {
      dice[i] = 0;
    }

    return dice;
  }

  private static Result processResult(List<Dice> roll, Result result) {
    boolean special = false;
    int failures = 0;
    List<Integer> successes = new ArrayList<Integer>();

    for (Dice dice : roll) {
      switch (dice.getDiceType()) {
        case SPECIAL:
          special = true;
          break;
        case FAIL:
          failures++;
          break;
        case SUCCESS:
          successes.add(dice.getDamage());
        default:
          // Nothing
      }
    }

    if (successes.size() > 0 && successes.size() > failures) {
      // Sort the damage in order
      Collections.sort(successes);

      // Now remove the failures
      for (int i = 0; i < failures; i++) {
        successes.remove(successes.size() - 1);
      }

      int bestResult = successes.get(successes.size() - 1);
      if (bestResult == 3) {
        result.threeDamage(special);
      } else if (bestResult == 2) {
        result.twoDamage(special);
      } else {
        result.oneDamage(special);
      }
    } else {
      result.failure();
    }

    return result;
  }
}


class Result {
  private BigDecimal totalAttempts = new BigDecimal(0);
  private BigDecimal fail = new BigDecimal(0);
  private BigDecimal oneDamage = new BigDecimal(0);
  private BigDecimal oneDamageSpecial = new BigDecimal(0);
  private BigDecimal twoDamage = new BigDecimal(0);
  private BigDecimal twoDamageSpecial = new BigDecimal(0);
  private BigDecimal threeDamage = new BigDecimal(0);
  private BigDecimal threeDamageSpecial = new BigDecimal(0);

  private final BigDecimal ONE = new BigDecimal(1);

  public void failure() {
    fail = fail.add(ONE);
    totalAttempts = totalAttempts.add(ONE);
  }


  public void oneDamage(boolean special) {
    if (special) {
      oneDamageSpecial = oneDamageSpecial.add(ONE);
    }
    oneDamage = oneDamage.add(ONE);

    totalAttempts = totalAttempts.add(ONE);
  }

  public void twoDamage(boolean special) {
    if (special) {
      twoDamageSpecial = twoDamageSpecial.add(ONE);
    }
    twoDamage = twoDamage.add(ONE);
    totalAttempts = totalAttempts.add(ONE);
  }

  public void threeDamage(boolean special) {
    if (special) {
      threeDamageSpecial = threeDamageSpecial.add(ONE);
    }
    threeDamage = threeDamage.add(ONE);
    totalAttempts = totalAttempts.add(ONE);
  }

  private BigDecimal calculatePercent(BigDecimal subtotal) {
    return subtotal.divide(totalAttempts, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
        .setScale(2);
  }

  public void print() {
    System.out.print(new StringBuilder("Results:\n").append("\tFailure: ")
        .append(calculatePercent(fail)).append("%\n").append("\tOne Damage: ")
        .append(calculatePercent(oneDamage)).append("%, with special: ")
        .append(calculatePercent(oneDamageSpecial)).append("%\n").append("\tTwo Damage: ")
        .append(calculatePercent(twoDamage)).append("%, with special: ")
        .append(calculatePercent(twoDamageSpecial)).append("%\n").append("\tThree Damage: ")
        .append(calculatePercent(threeDamage)).append("%, with special: ")
        .append(calculatePercent(threeDamageSpecial)).append("%").toString());
  }
}


enum DiceType {
  FAIL, NOTHING, SPECIAL, SUCCESS;
}


enum Dice {
  SIDE_ONE(DiceType.FAIL, 0), SIDE_TWO(DiceType.SPECIAL, 0), SIDE_THREE(DiceType.NOTHING,
      0), SIDE_FOUR(DiceType.SUCCESS,
          1), SIDE_FIVE(DiceType.SUCCESS, 2), SIDE_SIX(DiceType.SUCCESS, 3);

  private DiceType diceType;
  private int damage;

  private Dice(DiceType diceType, int damage) {
    this.diceType = diceType;
    this.damage = damage;
  }

  public DiceType getDiceType() {
    return diceType;
  }

  public int getDamage() {
    return damage;
  }
}
