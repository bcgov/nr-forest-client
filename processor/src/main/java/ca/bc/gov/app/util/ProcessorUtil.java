package ca.bc.gov.app.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProcessorUtil {

  public static String extractLetters(String input) {
    Pattern pattern = Pattern.compile("[A-Z]+");
    return getStringFromPattern(input, pattern);
  }

  public static String extractNumbers(String input) {
    Pattern pattern = Pattern.compile("\\d+");
    return getStringFromPattern(input, pattern);
  }

  public static String[] splitName(String input) {
    //If is null or empty return empty array
    if (StringUtils.isBlank(input)) {
      return new String[]{StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY};
    }

    //If contains comma, split by comma and join back together with space, else just use the input
    String cleanedInput = input.contains(",") ?
        String.join(" ",
            input
                //Remove the commas
                .replace(",", StringUtils.EMPTY)
                //Remove the first word
                .replace(input.split(",")[0], StringUtils.EMPTY),
            //Add the first word to the end
            input.split(",")[0].trim()
        ).trim() :
        input;

    //Split by space
    String[] words = cleanedInput.replace(",", StringUtils.EMPTY).split("\\s+");

    //Keeping the OG switch statement as native has some issues with new switch
    switch (words.length) {
      case 1:
        //Return the word for first 2 elements and empty string for the last
        return new String[]{words[0].trim(), words[0].trim(), StringUtils.EMPTY};
      case 2:
        //Return the second word for first, first word for second and empty string for the last
        return new String[]{words[1].trim(), words[0].trim(), StringUtils.EMPTY};
      default:
        //Return the last word for first, first word for second and the rest for the last
        return new String[]{
            words[words.length - 1].trim(),
            words[0].trim(),
            StringUtils.join(words, ' ', 1, words.length - 1).trim()
        };
    }
  }

  private static String getStringFromPattern(String input, Pattern pattern) {
    Matcher matcher = pattern.matcher(input);
    if (matcher.find()) {
      return matcher.group();
    } else {
      return StringUtils.EMPTY;
    }
  }


}
