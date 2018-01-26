import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class DiceOdds {

  private static final boolean ADD_DICE = true;

  enum Dice {
    SIDE_ONE(DiceType.FAIL, 1), SIDE_TWO(DiceType.FAIL, 0), SIDE_THREE(DiceType.SPECIAL,
            0), SIDE_FOUR(DiceType.SUCCESS,
            1), SIDE_FIVE(DiceType.SUCCESS, 1), SIDE_SIX(DiceType.SUCCESS, 1);

    private DiceType diceType;
    private int damage;

    Dice(DiceType diceType, int damage) {
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

  public static void main(String[] args) {
    for (int i=1; i<5; i++) {
      rollDice(i);
    }
  }

  private static void rollDice(int numberOfDice) {
    System.out.println("Rolling for " + numberOfDice + " dice");
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
        for (int i = 1; i < numberOfDice; i++) {
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
          failures+= dice.getDamage();
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

      if (ADD_DICE) {
        //We will add the results this time
        int total = 0;
        for (Integer success : successes) {
          total += success;
        }
        result.addResult(total, special);
      } else {
        //Take only the highest damage
        result.addResult(successes.get(successes.size() - 1), special);
      }
    } else {
      result.addResult(0, false);
    }

    return result;
  }
}


class Result {
  private BigDecimal totalAttempts = new BigDecimal(0);

  Map<Integer, BigDecimal> damage = new HashMap<Integer, BigDecimal>();
  Map<Integer, BigDecimal> special = new HashMap<Integer, BigDecimal>();

  private final BigDecimal ONE = new BigDecimal("1");

  public void addResult(Integer amount, boolean isSpecial) {
    if (damage.get(amount) != null) {
      damage.put(amount, damage.get(amount).add(ONE));
    } else {
      damage.put(amount, ONE);
    }

    if (isSpecial) {
      if (special.get(amount) != null) {
        special.put(amount, special.get(amount).add(ONE));
      } else {
        special.put(amount, ONE);
      }
    }

    totalAttempts = totalAttempts.add(ONE);
  }

  public void print() {
    StringBuilder sb = new StringBuilder("Made ").append(totalAttempts).append(" rolls:\n");

    for (Map.Entry<Integer, BigDecimal> entry : damage.entrySet()) {
      if (entry.getValue() != null) {
//        sb.append(entry.getKey()).append(" damage results: ").append(calculatePercent(entry.getValue())).append("%");
//
//        if (special.get(entry.getKey()) != null) {
//          sb.append(" (special: ").append(calculatePercent(special.get(entry.getKey()))).append("%)");
//        } else {
//          sb.append(" (special: 0%)");
//        }
//        sb.append(" | ");
        if (entry.getKey() == 0) {
          sb.append("0 damage: ").append(calculatePercent(entry.getValue())).append("%");
        } else {
          sb.append(entry.getKey()).append(" damage or more: ")
                  .append(calculatePercent(atLeast(entry.getKey(), damage))).append("%");

          if (special.get(entry.getKey()) != null) {
            sb.append(" (special: ").append(calculatePercent(atLeast(entry.getKey(), special))).append("%)");
          } else {
            sb.append(" (special: 0%)");
          }
        }

        sb.append("\n");
      }
    }

    System.out.println(sb.toString());
  }

  private BigDecimal calculatePercent(BigDecimal amount) {
    return amount.divide(totalAttempts, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
            .setScale(2);
  }

  private BigDecimal atLeast(Integer amount, Map<Integer, BigDecimal> map) {
    BigDecimal total = new BigDecimal("0");
    for (Map.Entry<Integer, BigDecimal> entry : map.entrySet()) {
      if (entry.getKey().compareTo(amount) >= 0 && entry.getValue() != null) {
        total = total.add(entry.getValue());
      }
    }
    return total;
  }
}


enum DiceType {
  FAIL, SPECIAL, SUCCESS;
}



